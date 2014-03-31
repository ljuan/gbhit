package filereaders;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
//import org.broad.tribble.readers.TabixReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import filereaders.individual.vcf.TabixReaderForVCF;
import filereaders.individual.vcf.Variant;
import filereaders.individual.vcf.Variants;
import filereaders.individual.vcf.Vcf;



import static filereaders.Consts.*;

public class VcfReader {
	TabixReaderForVCF vcf_tb;
	private Annotations track = null;
	/**
	 * limit of bpp
	 */
	private double bppLimit;

	public VcfReader(Annotations track, String Chr) {
		this.bppLimit = 0.5;
		String check = null;
		try {
			if (track.has_Parameter(VCF_INDEX_LOCAL) && track.get_Parameter(VCF_INDEX_LOCAL) != null)
				vcf_tb = new TabixReaderForVCF(track.get_Path(Chr),(String)track.get_Parameter(VCF_INDEX_LOCAL));
			else
				vcf_tb = new TabixReaderForVCF(track.get_Path(Chr),null);
			if (track.get_Parameter(VCF_CHROM_PREFIX) == null) {
				Map<String, Boolean> filter_header = new HashMap<String, Boolean>();
				Map<String, String[]> info_header = new HashMap<String, String[]>();
				Map<String, String[]> format_header = new HashMap<String, String[]>();
				String[] samples = null;
				String line = null;
				filter_header.put("PASS", false);
				String str = null;
				while ((line = vcf_tb.readLine())!=null&&line.startsWith("#")) {
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
					} else if (line.startsWith("#CHROM") || line.substring(0,6).equalsIgnoreCase("#CHROM")) {
						String[] line_temp = line.split("\t");
						if (line_temp.length > 9) {
							samples = new String[line_temp.length - 9];
							for (int i = 9; i < line_temp.length; i++) {
								if(line_temp[i].indexOf("\r")>=0)
									line_temp[i]=line_temp[i].substring(0, line_temp[i].indexOf("\r"));
								samples[i - 9] = line_temp[i];
							}
						}
					}
				}
				if(line!=null) {
					String[] line_temp = line.split("\t");
					if(line_temp.length<8)
						check = "Parsing file failed!";
					else if(!vcf_tb.hasRegularChr())
						check = "No valid contig name exists!\n(such as chr20, X, chrY... case sensitive)";
					else if(!Pattern.matches("[0-9]+", line_temp[1]))
						check = "Illegal non-numeric position!";
					else if(samples!=null&&line_temp.length!=9+samples.length)
						check = "Incorrect data field number!";
//Temporarily do not check errors in FORMAT and SAMPLE fields
//					else if(samples!=null&&line_temp[8].startsWith("GT")&&!(Pattern.matches("^GT(:.*|$)",line_temp[8])))
//						check = "Illegal seperator in FORMAT field!";
//					else if(samples!=null&&Pattern.matches("^GT(:.*|$)",line_temp[8]))
//						for(int i = 0;i < samples.length;i++){
//							if(line_temp[i+9].indexOf("\r")>=0)
//								line_temp[i+9]=line_temp[i+9].substring(0, line_temp[i+9].indexOf("\r"));
//							if(!Pattern.matches("^[|/0-9]+(:.*|$)",line_temp[9+i]))
//								check = "Illegal seperator or inappropriate characters in "+samples[i]+" field!";
//						}
//					else{
//						TabixReaderForVCF.Iterator Query= vcf_tb.query(line_temp[0] + ":" + line_temp[1] + "-" + line_temp[1]);
//						if(Query!=null)
//							Query.next();
//					}
				}
				else
					check = "No data record!";
				track.set_Check(check);
				if (samples != null){
					track.initialize_Parameter(VCF_HEADER_SAMPLE, new VcfSample(samples), PARAMETER_TYPE_VCFSAMPLE);
				/* This is for automatically select THE sample when load single sample VCF file, 
				 * cooperate with the sentence in Instance add_Tracks VCF branch, 
				 * which is also annotated in this version.
				 * 
				 * 
				 * 	if(samples.length==1)
						((VcfSample)(track.get_Parameter(VCF_HEADER_SAMPLE))).setSamples(samples[0]);*/
				}
				if (!filter_header.isEmpty())
					track.initialize_Parameter(VCF_HEADER_FILTER, filter_header, PARAMETER_TYPE_CHECKBOX);
				if (!format_header.isEmpty())
					track.initialize_Parameter(VCF_HEADER_FORMAT, format_header, PARAMETER_TYPE_INVISABLE);
				if (!info_header.isEmpty())
					track.initialize_Parameter(VCF_HEADER_INFO, info_header, PARAMETER_TYPE_INVISABLE);
				track.initialize_Parameter(VCF_CHROM_PREFIX, vcf_tb.hasChromPrefix(), PARAMETER_TYPE_INVISABLE);
				track.initialize_Parameter(VCF_QUAL_LIMIT, "0", PARAMETER_TYPE_STRING);
			}
			this.track = track;
		} catch (IOException e) {
			check = "Cannot access to the data/index file!";
			track.set_Check(check);
			e.printStackTrace();
		} catch(Exception e){
			check = "Invalid data!";
			track.set_Check(check);
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

	public Element get_detail(Document doc, Annotations track, String sample_id, String id,
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
		if(sample_id == null || vcfSample == null || !vcfSample.ifSelected(sample_id))
			variants[0] = new Variants(track.get_ID(), null, doc, MODE_DETAIL, 0.1, bppLimit, -1, null);
		else
			variants[0] = new Variants(track.get_ID(), sample_id, doc, MODE_DETAIL, 0.1, bppLimit, -1, null);
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
					vcf = new Vcf(vcf_tb.lineInChars, vcf_tb.numOfChar, samplesNum, selectedIndexes);
					Variant[] vs_temp; 
					if (sample_id != null && vcfSample.ifSelected(sample_id)) 
						vs_temp = vcf.getVariants(vcfSample.getSelectedIndex(sample_id));
					else 
						vs_temp = vcf.getVariants();
					if (vs_temp != null)
						for (int vs_i = 0; vs_i < vs_temp.length; vs_i++)
							if (vcf!=null && vcf.getID().equals(id) && vs_temp[vs_i].getFrom() == start && vs_temp[vs_i].getTo() == end)
								vs_list.add(vs_temp[vs_i]);
					if (!vs_list.isEmpty()) {
						vs = new Variant[vs_list.size()];
						vs_list.toArray(vs);
						if (sample_id != null && vcfSample.ifSelected(sample_id)) 
							variants[0].addVariant(vcf, vcfSample.getSelectedIndex(sample_id), vs);
						else
							variants[0].addVariant(vcf, -1, vs);
						break;
					}
				}
			}
			vcf_tb.TabixReaderClose();
		} catch (IOException e) {
			this.track.set_Check("Cannot access to the data/index file!");
			e.printStackTrace();
//		} catch (Exception e) {
//			this.track.set_Check("Invalid data!");
//			e.printStackTrace();
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
	public String[] write_individuals(String track, String variants){
		HashMap<String, String> vs = new HashMap<String, String>();
		String[] sampleNames = null;
		int[] sampleVotes = null;
		VcfSample vcfSample = null;
		int threshold = 0;
		if (this.track.has_Parameter(VCF_HEADER_SAMPLE)) {
			vcfSample = (VcfSample) this.track.get_Parameter(VCF_HEADER_SAMPLE);
			sampleNames = vcfSample.getSampleNames();
			sampleVotes = new int[sampleNames.length];
			for(int i=0;i<sampleVotes.length;i++)
				sampleVotes[i] = 0;
		}
		else
			return null;
		String[] vs_temp = variants.split("[\n\r]");
		try {
			for (int i=0;i<vs_temp.length;i++){
				if(vs_temp[i]==null||vs_temp[i].equals(""))
					continue;
				String[] temp = vs_temp[i].split("\t");
				if(temp.length<5 || temp[4].equals("."))
					continue;
				threshold++;
				String[] alts = temp[4].split(",");
				if(!temp[0].startsWith("chr"))
					temp[0]="chr"+temp[0];
				int start = Integer.parseInt(temp[1]);
				int end = start;
				
				for(int j=0;j<alts.length;j++){
					if(temp[3].length() < alts[j].length()){
						start += temp[3].length()-1;
						end = start+1;
						vs.put(temp[0]+":"+start+":"+end+":"+alts[j].substring(temp[3].length()),Consts.VARIANT_TYPE_INSERTION);
					}
					else if (temp[3].length() > alts[j].length()){
						start += alts[j].length();
						end += temp[3].length()-1;
						vs.put(temp[0]+":"+start+":"+end+":-",Consts.VARIANT_TYPE_DELETION);
					}
					else if (temp[3].length() > 1){
						for(int k=0;k<temp[3].length();k++)
							if(temp[3].charAt(k) != alts[j].charAt(k))
								vs.put(temp[0]+":"+(start+k)+":"+(end+k)+":"+alts[j].charAt(k),Consts.VARIANT_TYPE_SNV);
					}
					else{
						vs.put(temp[0]+":"+start+":"+end+":"+alts[j],Consts.VARIANT_TYPE_SNV);
					}
				}
				
				String chrom = (Boolean) this.track.get_Parameter(VCF_CHROM_PREFIX) ? temp[0] : temp[0].substring(3);
				if ("M".equalsIgnoreCase(chrom)) 
					chrom = "MT";
				end = start = Integer.parseInt(temp[1]);
				TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + (start-5)	+ "-" + (start+5));
				
				String line = "";
				if (Query != null) {
					while ((line=Query.next()) != null) {
						String[] line_temp=line.split("\t");
						if(line_temp[4].equals(".") || line_temp.length<10 || !line_temp[8].startsWith("GT"))
							continue;
						String[] line_alts = line_temp[4].split(",");
						
						for(int j=0;j<line_alts.length;j++){
							if(line_temp[3].length() < line_alts[j].length()){
								start += line_temp[3].length()-1;
								end = start+1;
								if(vs.containsKey(temp[0]+":"+start+":"+end+":"+line_alts[j].substring(line_temp[3].length()))){
									for(int n=0;n<sampleNames.length;n++){
										int temp_len=line_temp[n+9].indexOf(":")==-1?line_temp[n+9].length():line_temp[n+9].indexOf(":");
										if(line_temp[n+9].substring(0,temp_len).indexOf(String.valueOf(j+1))>0)
											sampleVotes[n]++;
									}
									vs.remove(temp[0]+":"+start+":"+end+":"+line_alts[j].substring(line_temp[3].length()));
								}
							}
							else if (line_temp[3].length() > line_alts[j].length()){
								start += line_alts[j].length();
								end += line_temp[3].length()-1;
								if(vs.containsKey(temp[0]+":"+start+":"+end+":-")){
									for(int n=0;n<sampleNames.length;n++){
										int temp_len=line_temp[n+9].indexOf(":")==-1?line_temp[n+9].length():line_temp[n+9].indexOf(":");
										if(line_temp[n+9].substring(0,temp_len).indexOf(String.valueOf(j+1))>0)
											sampleVotes[n]++;
									}
									vs.remove(temp[0]+":"+start+":"+end+":-");
								}
							}
							else if (line_temp[3].length() > 1){
								for(int k=0;k<line_temp[3].length();k++)
									if(line_temp[3].charAt(k) != line_alts[j].charAt(k))
										if(vs.containsKey(temp[0]+":"+(start+k)+":"+(end+k)+":"+line_alts[j].charAt(k))){
											for(int n=0;n<sampleNames.length;n++){
												int temp_len=line_temp[n+9].indexOf(":")==-1?line_temp[n+9].length():line_temp[n+9].indexOf(":");
												if(line_temp[n+9].substring(0,temp_len).indexOf(String.valueOf(j+1))>0)
													sampleVotes[n]++;
											}
											vs.remove(temp[0]+":"+(start+k)+":"+(end+k)+":"+line_alts[j].charAt(k));
										}
							}
							else{
								if(vs.containsKey(temp[0]+":"+start+":"+end+":"+line_alts[j])){
									for(int n=0;n<sampleNames.length;n++){
										int temp_len=line_temp[n+9].indexOf(":")>0?line_temp[n+9].indexOf(":"):line_temp[n+9].length();
										if(line_temp[n+9].substring(0,temp_len).indexOf(String.valueOf(j+1))>0)
											sampleVotes[n]++;
									}
									vs.remove(temp[0]+":"+start+":"+end+":"+line_alts[j]);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			this.track.set_Check("Cannot access to the data/index file!");
			e.printStackTrace();
		} finally{
			if(vcf_tb != null){
				try {
					vcf_tb.TabixReaderClose();
				} catch (IOException e) {
				}
			}
		}
		
		int threshold_lim = threshold>4 ? threshold - 2 : 1;
		ArrayList<String> result_temp = new ArrayList<String>();
		for(int i=threshold;i>=threshold_lim;i--)
			for(int j=0;j<sampleNames.length;j++)
				if(sampleVotes[j]==i)
					result_temp.add(sampleNames[j]+":"+i);
		String[] result = new String[result_temp.size()];
		result_temp.toArray(result);
		return result;
	}
	public Element write_intersection(Document doc, String track, String set_a, String chr, long start, long end){
		float qualLimit = Float.parseFloat((String) (this.track.get_Parameter(VCF_QUAL_LIMIT)));
		String[] filterLimit = getFilterLimit();

		int samplesNum = 0;
		int[] selectedIndexes = null;
		VcfSample vcfSample = null;
		Variants[] variants;
		String mode=Consts.MODE_PACK;
		double bpp = 0.5;

		if (this.track.has_Parameter(VCF_HEADER_SAMPLE)) {
			vcfSample = new VcfSample(((VcfSample) this.track.get_Parameter(VCF_HEADER_SAMPLE)).getSampleNames());
			vcfSample.setSamples(set_a);
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
				Variant[][] vs = new Variant[len][];
				boolean siNotNull = selectedIndexes != null;
				while (Query.next() != null) {
					vcf = new Vcf(vcf_tb.lineInChars, vcf_tb.numOfChar, samplesNum, selectedIndexes);
					if (vcf.altIsDot())
						continue;
					if (vcf.shouldBeFilteredByQualLimit(qualLimit) || vcf.shouldBeFilteredByFilterLimit(filterLimit))
						continue;
					if (!vcf.isDBSnp() && siNotNull) {
						// Personal Genemic VCF
						vs = vcf.getVariants_intersection(selectedIndexes.length);
						if(vs == null)
							continue;
						for (int i = 0; i < len; i++) 
							if (vs[i] != null)
								variants[i].addVariant(vcf,i, vs[i]);
						vs = null;
					} 
				}
			}
		} catch (IOException e) {
			this.track.set_Check("Cannot access to the data/index file!");
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
	public Element write_difference(Document doc, String track, String set_a, String set_b, String chr, long start, long end){
		float qualLimit = Float.parseFloat((String) (this.track.get_Parameter(VCF_QUAL_LIMIT)));
		String[] filterLimit = getFilterLimit();

		int samplesNum = 0;
		int[] selectedIndexes = null;
		int[] selectedIndexes_a = null;
		int[] selectedIndexes_b = null;
		VcfSample vcfSample = null;
		Variants[] variants;
		String mode=Consts.MODE_PACK;
		double bpp = 0.5;

		if (this.track.has_Parameter(VCF_HEADER_SAMPLE)) {
			vcfSample = new VcfSample(((VcfSample) this.track.get_Parameter(VCF_HEADER_SAMPLE)).getSampleNames());
			vcfSample.setSamples(set_a+":"+set_b);
			samplesNum = vcfSample.getSamplesNum();
			selectedIndexes = vcfSample.getSelectedIndexes();
			String[] selectedSamples=vcfSample.getSelectedNames();
			
			int num_a=0;
			int num_b=0;
			
			for(int i=0;i<selectedSamples.length;i++)
				if(set_a.matches("(^|.*:)"+selectedSamples[i]+"(:.*|$)"))
					num_a++;
				else if(set_b.matches("(^|.*:)"+selectedSamples[i]+"(:.*|$)"))
					num_b++;
			selectedIndexes_a=new int[num_a];
			selectedIndexes_b=new int[num_b];
			num_a = 0;
			num_b = 0;
			
			for(int i=0;i<selectedSamples.length;i++)
				if(set_a.matches("(^|.*:)"+selectedSamples[i]+"(:.*|$)"))
					selectedIndexes_a[num_a++]=i;
				else if(set_b.matches("(^|.*:)"+selectedSamples[i]+"(:.*|$)"))
					selectedIndexes_b[num_b++]=i;
			
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
				Variant[][] vs = new Variant[len][];
				boolean siNotNull = selectedIndexes != null && selectedIndexes_a != null && selectedIndexes_b != null;
				while (Query.next() != null) {
					vcf = new Vcf(vcf_tb.lineInChars, vcf_tb.numOfChar, samplesNum, selectedIndexes);
					if (vcf.altIsDot())
						continue;
					if (vcf.shouldBeFilteredByQualLimit(qualLimit) || vcf.shouldBeFilteredByFilterLimit(filterLimit))
						continue;
					if (!vcf.isDBSnp() && siNotNull) {
						// Personal Genome VCF
						vs = vcf.getVariants_difference(selectedIndexes_a,selectedIndexes_b);
						if(vs == null)
							continue;
						for (int i = 0; i < len; i++) 
							if (vs[i] != null)
								variants[i].addVariant(vcf,i, vs[i]);
						vs = null;
					} 
				}
			}
		} catch (IOException e) {
			this.track.set_Check("Cannot access to the data/index file!");
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
		float qualLimit = Float.parseFloat((String) (this.track.get_Parameter(VCF_QUAL_LIMIT)));
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
					if (vcf.shouldBeFilteredByQualLimit(qualLimit) || vcf.shouldBeFilteredByFilterLimit(filterLimit))
						continue;
					if (!vcf.isDBSnp() && siNotNull) {
						// Personal Genemic VCF
						for (int i = 0; i < len; i++) {
							vs = vcf.getVariants(i);
							if (vs != null)
								variants[i].addVariant(vcf,i, vs);
						}

						vs = vcf.getVariants(0);
					} else {
						// DBSnp or Personal genomic VCF without choose any
						// SAMPLEs, variants.length must be 1.
						vs = vcf.getVariants();
						if (vs != null)
							variants[0].addVariant(vcf,-1, vs);
					}
				}
			}
		} catch (IOException e) {
			this.track.set_Check("Cannot access to the data/index file!");
			e.printStackTrace();
//		} catch (Exception e) {
//			this.track.set_Check("Invalid data!");
//			e.printStackTrace();
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
