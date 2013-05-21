package FileReaders;

import java.io.IOException;
import java.util.*;
//import org.broad.tribble.readers.TabixReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.hit.mlg.individual.vcf.TabixReaderForVCF;
import edu.hit.mlg.individual.vcf.Variant;
import edu.hit.mlg.individual.vcf.Variants;
import edu.hit.mlg.individual.vcf.Vcf;

import static FileReaders.Consts.*;

/*
 * This is for reading a Consts.VCF format file.
 * Represents one Consts.VCF file.
 * The class can primarily analyze the header of Consts.VCF file
 * to obtain INFO and FILTER and FORMAT information.
 * But the subsequence analysis function for each record is not implemented yet.
 * The Consts.VCF file reader rely on Heng Li's Tabix API,
 * thus Consts.VCF must be sorted, must be compressed using Tabix's bgzip commandline tool,
 * must be indexed by Tabix.
 * We believe Tabix is a good way to save time when big files in remote servers,
 * and to save space when big file in local servers.
 * This also let us avoid the underlying binary file format designing ,binary index designing,
 * and binary file reader coding, which won't be easily admit by peers in a short time.
 * AND the performance of Tabix looks good.
 * Besides, Heng Li is not some random guy in this area, this tool is reliable.
 * Challenging his coding ability means challenging more than half NGS researches.
 */

public class VcfReader {
	TabixReaderForVCF vcf_tb;
	private Annotations track = null;
	/**
	 * limit of bpp
	 */
	private double bppLimit;

	public VcfReader(Annotations track, String Chr) {
		this.bppLimit = 0.5;
		try {
			vcf_tb = new TabixReaderForVCF(track.get_Path(Chr));
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
						String[] line_temp = line.substring(left + 1, right).split("ID=|,Description=");
						filter_header.put(line_temp[1], false);
					} else if (line.substring(2, 6).equalsIgnoreCase("INFO")) {
						int left = line.indexOf('<');
						int right = line.lastIndexOf('>');
						String[] line_temp = line.substring(left + 1, right).split("ID=|,Number=|,Type=|,Description=");
						info_header.put(line_temp[1], Arrays.copyOfRange(line_temp, 2, 5));
					} else if (str.equalsIgnoreCase("FORMAT")) {
						int left = line.indexOf('<');
						int right = line.lastIndexOf('>');
						String[] line_temp = line.substring(left + 1, right).split("ID=|,Number=|,Type=|,Description=");
						format_header.put(line_temp[1], Arrays.copyOfRange(line_temp, 2, 5));
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
				if (samples != null){
					track.initialize_Parameter(VCF_HEADER_SAMPLE, new VcfSample(samples), PARAMETER_TYPE_VCFSAMPLE);
					if(samples.length==1)
						((VcfSample)(track.get_Parameter(VCF_HEADER_SAMPLE))).setSamples(samples[0]);
				}
				if (!filter_header.isEmpty())
					track.initialize_Parameter(VCF_HEADER_FILTER, filter_header, PARAMETER_TYPE_CHECKBOX);
				if (!format_header.isEmpty())
					track.initialize_Parameter(VCF_HEADER_FORMAT, format_header, PARAMETER_TYPE_INVISABLE);
				if (!info_header.isEmpty())
					track.initialize_Parameter(VCF_HEADER_INFO, info_header, PARAMETER_TYPE_INVISABLE);
				track.initialize_Parameter(VCF_CHROM_PREFIX, vcf_tb.hasChromPrefix(), PARAMETER_TYPE_INVISABLE);
				track.initialize_Parameter(VCF_QUAL_LIMIT, "-1", PARAMETER_TYPE_STRING);
			}
			this.track = track;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Change the limit of bpp
	 * 
	 * @param bppLimit
	 *            limit of bpp
	 */
	public void changeBppLimit(double bppLimit) {
		this.bppLimit = bppLimit;
	}

	public void printSamples() {
		VcfSample vcfSample = (VcfSample) this.track.get_Parameter(VCF_HEADER_SAMPLE);
		if (vcfSample == null) {
			System.out.println("The track is dbsnp!");
			return;
		}
		System.out.println("Number of samples:" + vcfSample.getSamplesNum());
		for (String sample : vcfSample.getSampleNames())
			System.out.println(sample);
	}

	public Element get_detail(Document doc, Annotations track, String id,
			String chr, long start, long end) {
		Variants[] variants = new Variants[1];
		int samplesNum = 0;
		int[] selectedIndexes = null;
		VcfSample vcfSample = null;

		if (track.has_Parameter(VCF_HEADER_SAMPLE)) {
			vcfSample = (VcfSample) track.get_Parameter(VCF_HEADER_SAMPLE);
			samplesNum = vcfSample.getSamplesNum();
			selectedIndexes = vcfSample.getSelectedIndexes();
		}
		variants[0] = new Variants(track.get_ID(), null, doc, MODE_DETAIL, 0.1, bppLimit, -1, null);
		Vcf vcf = null;
		try {
			String chrom = (Boolean) this.track .get_Parameter(VCF_CHROM_PREFIX) ? chr : chr.substring(3);
			if ("M".equalsIgnoreCase(chrom)) {
				chrom = "MT";
			}
			TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + start + "-" + end);
			if (Query != null) {
				ArrayList<Variant> vs_list = new ArrayList<Variant>();
				Variant[] vs;
				while (Query.next() != null) {
					vcf = new Vcf(vcf_tb.lineInChars, vcf_tb.numOfChar, samplesNum, null);
					Variant[] vs_temp = vcf.getVariants();
					for (int vs_i = 0; vs_i < vs_temp.length; vs_i++){
						if (vcf.getID().equals(id) && vs_temp[vs_i].getFrom() == start && vs_temp[vs_i].getTo() == end)
							vs_list.add(vs_temp[vs_i]);
					}
					if (!vs_list.isEmpty()) {
						vs = new Variant[vs_list.size()];
						vs_list.toArray(vs);
						variants[0].addVariant(vcf, vs);
						break;
					}
				}
			}
			vcf_tb.TabixReaderClose();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(vcf_tb != null){
				try {
					vcf_tb.TabixReaderClose();
				} catch (IOException e) {
				}
			}
		}
		Element e1 = variants[0].getVariantsElement();
		variants = null;
		return e1;
	}
	
	public Element write_vcf2variants(Document doc, String track, String mode,
			double bpp/* bases per pixel */, String chr, long start, long end) {
		float qualLimit = Float.parseFloat((String) (this.track
				.get_Parameter(VCF_QUAL_LIMIT)));
		String[] filterLimit = getFilterLimit();

		int samplesNum = 0;
		int[] selectedIndexes = null;
		VcfSample vcfSample = null;
		Variants[] variants;

		if (this.track.has_Parameter(VCF_HEADER_SAMPLE)) {
			vcfSample = (VcfSample) this.track
					.get_Parameter(VCF_HEADER_SAMPLE);
			// vcfSample.setSamples("MCF7");
			samplesNum = vcfSample.getSamplesNum();
			selectedIndexes = vcfSample.getSelectedIndexes();
		}
		if (samplesNum == 0 || selectedIndexes == null) {
			// DBSnp or Personal genemic VCF without choose any SAMPLEs
			variants = new Variants[1];
			variants[0] = new Variants(track, null, doc, mode, bpp, bppLimit, -1, null);
		} else {
			// Personal Genemic VCF
			variants = new Variants[selectedIndexes.length];
			String[] selectedNames = vcfSample.getSelectedNames();
			int selectedLen = selectedIndexes.length;
			for (int i = 0; i < selectedLen; i++) {
				variants[i] = new Variants(track, selectedNames[i], doc, mode, bpp, bppLimit, qualLimit, filterLimit);
			}
		}

		Vcf vcf = null;
		try {
			String chrom = (Boolean) this.track.get_Parameter(VCF_CHROM_PREFIX) ? chr : chr.substring(3);
			if ("M".equalsIgnoreCase(chrom)) {
				chrom = "MT";
			}
			TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + start
					+ "-" + end);
			if (Query != null) {
				int len = variants.length;
				Variant[] vs;
				boolean siNotNull = selectedIndexes != null;
				while (Query.next() != null) {
					vcf = new Vcf(vcf_tb.lineInChars, vcf_tb.numOfChar, samplesNum, selectedIndexes);
					if (vcf.altIsDot())
						continue;
					if (!vcf.isDBSnp() && siNotNull) {
						// Personal Genemic VCF
						if (vcf.shouldBeFilteredByQualLimit(qualLimit) || vcf.shouldBeFilteredByFilterLimit(filterLimit))
							continue;
						for (int i = 0; i < len; i++) {
							vs = vcf.getVariants(i);
							if (vs != null)
								variants[i].addVariant(vcf, vs);
						}

						vs = vcf.getVariants(0);
					} else {
						// DBSnp or Personal genomic VCF without choose any
						// SAMPLEs, variants.length must be 1.
						vs = vcf.getVariants();
						if (vs != null)
							variants[0].addVariant(vcf, vs);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(vcf_tb != null){
				try {
					vcf_tb.TabixReaderClose();
				} catch (IOException e) {
				}
			}
		}
		Element e1 = variants[0].getVariantsElement();
		variants = null;
		return e1;
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
		java.util.Iterator<String> itor = filters.keySet().iterator();
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