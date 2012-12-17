package FileReaders;

import java.io.IOException;
import java.util.*;
//import org.broad.tribble.readers.TabixReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import FileReaders.vcf.Variant;
import FileReaders.vcf.Variants;
import FileReaders.vcf.Vcf;

/*
 * This is for reading a VCF format file.
 * Represents one VCF file.
 * The class can primarily analyze the header of VCF file 
 * to obtain INFO and FILTER and FORMAT information.
 * But the subsequence analysis function for each record is not implemented yet.
 * The VCF file reader rely on Heng Li's Tabix API, 
 * thus VCF must be sorted, must be compressed using Tabix's bgzip commandline tool,
 * must be indexed by Tabix.
 * We believe Tabix is a good way to save time when big files in remote servers,
 * and to save space when big file in local servers.
 * This also let us avoid the underlying binary file format designing ,binary index designing,
 * and binary file reader coding, which won't be easily admit by peers in a short time.
 * AND the performance of Tabix looks good. 
 * Besides, Heng Li is not some random guy in this area, this tool is reliable.
 * Challenging his coding ability means challenging more than half NGS researches.
 */

class VcfReader implements Consts {
	private TabixReader vcf_tb;
	private Annotations track = null;

	VcfReader(Annotations track, String Chr) {
		try {
			vcf_tb = new TabixReader(track.get_Path(Chr));
			if (track.get_Parameter(VCF_CHROM_PREFIX) == null) {
				Map<String, Boolean> filter_header = new HashMap<String, Boolean>();
				Map<String, String[]> info_header = new HashMap<String, String[]>();
				Map<String, String[]> format_header = new HashMap<String, String[]>();
				String[] samples = null;
				String line = "";
				filter_header.put("PASS", true);
				String str = null;
				while ((line = vcf_tb.readLine()).startsWith("#")) {
					str = line.substring(2, 8);
					if (str.equalsIgnoreCase("FILTER")) {
						int left = line.indexOf('<');
						int right = line.lastIndexOf('>');
						String[] line_temp = line.substring(left + 1, right)
								.split("ID=|,Description=");
						filter_header.put(line_temp[1], false);
					} else if (line.substring(2, 6).equalsIgnoreCase("INFO")) {
						int left = line.indexOf('<');
						int right = line.lastIndexOf('>');
						String[] line_temp = line.substring(left + 1, right)
								.split("ID=|,Number=|,Type=|,Description=");
						info_header.put(line_temp[1],
								Arrays.copyOfRange(line_temp, 2, 5));
					} else if (str.equalsIgnoreCase("FORMAT")) {
						int left = line.indexOf('<');
						int right = line.lastIndexOf('>');
						String[] line_temp = line.substring(left + 1, right)
								.split("ID=|,Number=|,Type=|,Description=");
						format_header.put(line_temp[1],
								Arrays.copyOfRange(line_temp, 2, 5));
					} else if (line.startsWith("#CHROM")) {
						String[] line_temp = line.split("\t");
						if (line_temp.length > 9) {
							samples = new String[line_temp.length - 9];
							for (int i = 9; i < line_temp.length; i++) {
								samples[i - 9] = line_temp[i];
							}
						}
					}
				}
				if(samples!=null)
					track.initialize_Parameter(VCF_HEADER_SAMPLE, new VcfSample(
							samples), PARAMETER_TYPE_VCFSAMPLE);
				if(!filter_header.isEmpty())
					track.initialize_Parameter(VCF_HEADER_FILTER, filter_header,
							PARAMETER_TYPE_CHECKBOX);
				if(!format_header.isEmpty())
					track.initialize_Parameter(VCF_HEADER_FORMAT, format_header,
							PARAMETER_TYPE_INVISABLE);
				if(!info_header.isEmpty())
					track.initialize_Parameter(VCF_HEADER_INFO, info_header,
							PARAMETER_TYPE_INVISABLE);
				track.initialize_Parameter(VCF_CHROM_PREFIX,
						vcf_tb.hasChromPrefix(), PARAMETER_TYPE_INVISABLE);
				track.initialize_Parameter("QUALLIMIT", "-1",
						PARAMETER_TYPE_STRING);
			}
			this.track = track;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Element write_vcf2variants(Document doc, String track, String mode,
			double bpp/* bases per pixel */, String chr, long start, long end) {
		
		float qualLimit = Float.parseFloat((String) (this.track
				.get_Parameter("QUALLIMIT")));
		String[] filterLimit = getFilterLimit();
		Variants[] vss;
		int samplesNum = 0;
		int[] selectedIndexes = null;
		VcfSample vcfSample = null;
		
		if(this.track.has_Parameter(VCF_HEADER_SAMPLE)){
			vcfSample = (VcfSample) this.track
				.get_Parameter(VCF_HEADER_SAMPLE);
		// vcfSample.setSamples("MCF7");
			samplesNum = vcfSample.getSamplesNum();
			selectedIndexes = vcfSample.getSelectedIndexes();
		}
		if (samplesNum == 0 || selectedIndexes == null) {
			// DBSnp or Personal genemic VCF whitout choose any SAMPLEs
			vss = new Variants[1];
			vss[0] = new Variants(track, doc, mode, bpp, -1, null);
		} else {
			// Personal Genemic VCF
			vss = new Variants[selectedIndexes.length];
			String[] selectedNames = vcfSample.getSelectedNames();
			String prefix = track + "_";
			int selectedLen = selectedIndexes.length;
			for (int i = 0; i < selectedLen; i++) {
				vss[i] = new Variants(prefix + selectedNames[i], doc, mode,
						bpp, qualLimit, filterLimit);
			}
		}

		String line = null;
		Vcf vcf = null;
		try {
			String chrom = vcf_tb.hasChromPrefix() ? chr : chr.substring(3);
			if ("M".equalsIgnoreCase(chrom)) {
				chrom = "MT";
			}
			TabixReader.Iterator Query = vcf_tb.query(chrom + ":" + start + "-"
					+ end);
			if (Query != null) {
				int len = vss.length;
				Variant[] vs;
				while ((line = Query.next()) != null) {
					vcf = new Vcf(line, samplesNum, selectedIndexes);
					if (vcf.whetherAltIsDot())
						continue;
					if (!vcf.isDBSnp() && selectedIndexes != null) {
						// Personal Genemic VCF
						if (vcf.shouldBeFilteredByQualLimit(qualLimit)
								|| vcf.shouldBeFilteredByFilterLimit(filterLimit))
							continue;
						for (int i = 0; i < len; i++) {
							vs = vcf.getVariants(i);
							if (vs != null)
								vss[i].addVariant(vcf, vs);
						}

					} else {
						// DBSnp or Personal genemic VCF whitout choose any
						// SAMPLEs, vss.length must be 1.
						vs = vcf.getVariants();
						if (vs != null)
							vss[0].addVariant(vcf, vs);
					}
				}
			}
			vcf_tb.TabixReaderClose();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return vss[0].getVariantsElement();
	}

	private String[] getFilterLimit() {
		Object o = this.track.get_Parameter(VCF_HEADER_FILTER);
		if (o == null)
			return null;
		String[] filterLimit = new String[20];
		int count = 0;
		int len = 20;
		@SuppressWarnings("unchecked")
		Map<String, Boolean> filters = (Map<String, Boolean>) o;
		Iterator<String> itor = filters.keySet().iterator();
		String key;
		Boolean selected = false;
		while (itor.hasNext()) {
			key = itor.next();
			selected = (Boolean) filters.get(key);
			if (selected) {
				if (count == len) {
					filterLimit = expandCapacity(filterLimit, len);
					len += len;
				}
				filterLimit[count++] = key;
			}
		}

		if (count == 0)
			return null;
		String[] dest = new String[count];
		System.arraycopy(filterLimit, 0, dest, 0, count);
		return dest;
	}

	private String[] expandCapacity(String[] src, int len) {
		String[] dest = new String[len + len];
		System.arraycopy(src, 0, dest, 0, len);
		return dest;
	}
}