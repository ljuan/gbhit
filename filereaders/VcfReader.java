package filereaders;

import java.io.File;
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
import filereaders.tools.StringSplit;



import static filereaders.Consts.*;

public class VcfReader {
	private Annotations track = null;
	private String Chr = null;
	/**
	 * limit of bpp
	 */
	private double bppLimit;

	public VcfReader(Annotations track, String Chr) {
		this.bppLimit = 0.5;
		String check = null;
		TabixReaderForVCF vcf_tb = null;
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
					
					if(new File(track.get_Path(Chr)+".ped").exists())
						((VcfSample)track.get_Parameter(VCF_HEADER_SAMPLE)).loadPedigree(track.get_Path(Chr)+".ped");
					
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
			this.Chr = Chr;
		} catch (IOException e) {
			check = "Cannot access to the data/index file!";
			track.set_Check(check);
			e.printStackTrace();
		} catch(Exception e){
			check = "Invalid data!";
			track.set_Check(check);
			e.printStackTrace();
		} finally{
			if(vcf_tb != null){
				try {
					vcf_tb.TabixReaderClose();
				} catch (IOException e) {
				}
			}
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
		TabixReaderForVCF vcf_tb = null;

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
			if (track.has_Parameter(VCF_INDEX_LOCAL) && track.get_Parameter(VCF_INDEX_LOCAL) != null)
				vcf_tb = new TabixReaderForVCF(track.get_Path(chr),(String)track.get_Parameter(VCF_INDEX_LOCAL));
			else
				vcf_tb = new TabixReaderForVCF(track.get_Path(chr),null);
			TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + start + "-" + end);
			if (Query != null) {
				ArrayList<Variant> vs_list = new ArrayList<Variant>();
				Variant[] vs;
				while (Query.next() != null) {
					vcf = new Vcf(vcf_tb.lineInChars, vcf_tb.numOfChar, samplesNum, selectedIndexes);
					Variant[] vs_temp; 
					if (sample_id != null && vcfSample!=null && vcfSample.ifSelected(sample_id)) 
						vs_temp = vcf.getVariants(vcfSample.getSelectedIndex(sample_id));
					else 
						vs_temp = vcf.getVariants(true);
					if (vs_temp != null)
						for (int vs_i = 0; vs_i < vs_temp.length; vs_i++)
							if (vcf!=null && vcf.getID().equals(id) && vs_temp[vs_i].getFrom() == start && vs_temp[vs_i].getTo() == end)
								vs_list.add(vs_temp[vs_i]);
					if (!vs_list.isEmpty()) {
						vs = new Variant[vs_list.size()];
						vs_list.toArray(vs);
						if (sample_id != null && vcfSample!=null && vcfSample.ifSelected(sample_id)) 
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
		TabixReaderForVCF vcf_tb = null;
		HashMap<String, String> vs = new HashMap<String, String>();
		String[] sampleNames = null;
		int[] sampleVotes = null;
		String[][] sampleGT = null;
		VcfSample vcfSample = null;
		int threshold = 0;
		if (this.track.has_Parameter(VCF_HEADER_SAMPLE)) {
			vcfSample = (VcfSample) this.track.get_Parameter(VCF_HEADER_SAMPLE);
			sampleNames = vcfSample.getSampleNames();
			sampleVotes = new int[sampleNames.length];
			sampleGT = new String[sampleNames.length][];
			for(int i=0;i<sampleVotes.length;i++)
				sampleVotes[i] = 0;
		}
		else
			return null;
//		String[] vs_temp = variants.split("[\n\r]");
		String[] vs_temp = variants.split(",");
		
		for(int i=0;i<sampleNames.length;i++){
			sampleGT[i] = new String[vs_temp.length];
			for(int j=0;j<vs_temp.length;j++){
				sampleGT[i][j] = "";
			}
		}
		
		try {
			for (int i=0;i<vs_temp.length;i++){
				if(vs_temp[i]==null||vs_temp[i].equals(""))
					continue;
				
/*			These codes recieve colon-seperated variants list*/
			////////////////////	
				String[] temp = vs_temp[i].split(":");
				threshold++;
				if(!temp[0].startsWith("chr")){
					temp[0]="chr"+temp[0];
					vs_temp[i] = "chr"+vs_temp[i];
				}
				int start = Integer.parseInt(temp[1]);
				int end = Integer.parseInt(temp[2]);
				vs.put(vs_temp[i], Consts.VARIANT_TYPE_SNV);
			///////////////////	
				
/*			These codes recieve VCF lines for variants input	
 * 				String[] temp = vs_temp[i].split("\t");
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
*/				
				String chrom = (Boolean) this.track.get_Parameter(VCF_CHROM_PREFIX) ? temp[0] : temp[0].substring(3);
				if ("M".equalsIgnoreCase(chrom)) 
					chrom = "MT";
				end = start = Integer.parseInt(temp[1]);
				if (this.track.has_Parameter(VCF_INDEX_LOCAL) && this.track.get_Parameter(VCF_INDEX_LOCAL) != null)
					vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),(String)this.track.get_Parameter(VCF_INDEX_LOCAL));
				else
					vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),null);
				TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + (start-5)	+ "-" + (end+5));
				
				String line = "";
				if (Query != null) {
					while ((line=Query.next()) != null) {
						String[] line_temp=line.split("\t");
						if(line_temp[4].equals(".") || line_temp.length<10 || !line_temp[8].startsWith("GT"))
							continue;
						String[] line_alts = line_temp[4].split(",");
						
						end = start = Integer.parseInt(line_temp[1]);
						
						for(int j=0;j<line_alts.length;j++){
							if(line_alts[j].indexOf("<")>=0){
							//process <INS> <INV> <TRA> <DEL> <DUP> <CNV> ...
								Variant var  = new Vcf(vcf_tb.lineInChars, vcf_tb.numOfChar, 0, null).getVariants(false)[0];
								if (var==null)
									continue;
								start = var.getFrom();
								end = var.getTo();
								if(vs.containsKey(temp[0]+":"+start+":"+end+":-")){
									for(int n=0;n<sampleNames.length;n++){
										int temp_len=line_temp[n+9].indexOf(":")==-1?line_temp[n+9].length():line_temp[n+9].indexOf(":");
										if(line_temp[n+9].substring(0,temp_len).indexOf(String.valueOf(j+1))>=0)
											sampleVotes[n]++;
										sampleGT[n][i]=line_temp[n+9].substring(0,temp_len);
									}
									vs.remove(temp[0]+":"+start+":"+end+":-");
								}
							}
							else if(line_temp[3].length() < line_alts[j].length()){
								start += line_temp[3].length()-1;
								end = start+1;
								if(vs.containsKey(temp[0]+":"+start+":"+end+":"+line_alts[j].substring(line_temp[3].length()))){
									for(int n=0;n<sampleNames.length;n++){
										int temp_len=line_temp[n+9].indexOf(":")==-1?line_temp[n+9].length():line_temp[n+9].indexOf(":");
										if(line_temp[n+9].substring(0,temp_len).indexOf(String.valueOf(j+1))>=0)
											sampleVotes[n]++;
										sampleGT[n][i]=line_temp[n+9].substring(0,temp_len);
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
										if(line_temp[n+9].substring(0,temp_len).indexOf(String.valueOf(j+1))>=0)
											sampleVotes[n]++;
										sampleGT[n][i]=line_temp[n+9].substring(0,temp_len);
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
												if(line_temp[n+9].substring(0,temp_len).indexOf(String.valueOf(j+1))>=0)
													sampleVotes[n]++;
												sampleGT[n][i]=line_temp[n+9].substring(0,temp_len);
											}
											vs.remove(temp[0]+":"+(start+k)+":"+(end+k)+":"+line_alts[j].charAt(k));
										}
							}
							else{
								if(vs.containsKey(temp[0]+":"+start+":"+end+":"+line_alts[j])){
									for(int n=0;n<sampleNames.length;n++){
										int temp_len=line_temp[n+9].indexOf(":")>0?line_temp[n+9].indexOf(":"):line_temp[n+9].length();
										if(line_temp[n+9].substring(0,temp_len).indexOf(String.valueOf(j+1))>=0)
											sampleVotes[n]++;
										sampleGT[n][i]=line_temp[n+9].substring(0,temp_len);
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
			for(int j=0;j<sampleNames.length;j++){
				if(sampleVotes[j]==i){
					String temp_s = sampleNames[j]+":"+i+":";
					for(int k=0;k<vs_temp.length;k++){
						if(k>0)
							temp_s+=";";
						temp_s+=sampleGT[j][k];
					}
					result_temp.add(temp_s);
				}
			}
		String[] result = new String[result_temp.size()];
		result_temp.toArray(result);
		return result;
	}
	public Element write_intersection(Document doc, String track, String set_a, String chr, long start, long end){
		float qualLimit = Float.parseFloat((String) (this.track.get_Parameter(VCF_QUAL_LIMIT)));
		TabixReaderForVCF vcf_tb = null;
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
			variants = new Variants[1];
			variants[0] = new Variants(track, "Intersection", doc, mode, bpp, bppLimit, qualLimit, filterLimit);
		}
		Vcf vcf = null;
		try {
			String chrom = (Boolean) this.track.get_Parameter(VCF_CHROM_PREFIX) ? chr : chr.substring(3);
			if ("M".equalsIgnoreCase(chrom)) {
				chrom = "MT";
			}
			if (this.track.has_Parameter(VCF_INDEX_LOCAL) && this.track.get_Parameter(VCF_INDEX_LOCAL) != null)
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),(String)this.track.get_Parameter(VCF_INDEX_LOCAL));
			else
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),null);
			TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + start
					+ "-" + end);
			if (Query != null) {
				Variant[] vs = null;
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
						if (vs != null)
							variants[0].addVariant(vcf,-1, vs);
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
		TabixReaderForVCF vcf_tb = null;
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
			variants = new Variants[1];
			variants[0] = new Variants(track, "Difference", doc, mode, bpp, bppLimit, qualLimit, filterLimit);
		}
		Vcf vcf = null;
		try {
			String chrom = (Boolean) this.track.get_Parameter(VCF_CHROM_PREFIX) ? chr : chr.substring(3);
			if ("M".equalsIgnoreCase(chrom)) {
				chrom = "MT";
			}
			if (this.track.has_Parameter(VCF_INDEX_LOCAL) && this.track.get_Parameter(VCF_INDEX_LOCAL) != null)
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),(String)this.track.get_Parameter(VCF_INDEX_LOCAL));
			else
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),null);
			TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + start
					+ "-" + end);
			if (Query != null) {
				Variant[] vs = null;
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
						if (vs != null)
							variants[0].addVariant(vcf,-1, vs);
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
	public Element[] write_trio(Document doc, String oid, String chr, long start, long end, boolean scan){
		float qualLimit = Float.parseFloat((String) (this.track.get_Parameter(VCF_QUAL_LIMIT)));
		TabixReaderForVCF vcf_tb = null;
		String[] filterLimit = getFilterLimit();

		int samplesNum = 0;
		VcfSample vcfSample_real = null;
		VcfSample vcfSample = null;
		String[] pids = null;
		int o=0,f=0,m=0;
		int[] selectedIndexes = null;
		String[] selectedNames = null;
		Variants[] variants;
		String mode=Consts.MODE_PACK;
		double bpp = 0.5;

		if (this.track.has_Parameter(VCF_HEADER_SAMPLE)) {
			vcfSample_real = ((VcfSample) this.track.get_Parameter(VCF_HEADER_SAMPLE));
			if(!vcfSample_real.ifTrioAvailable(oid))
				return null;
			vcfSample = new VcfSample(vcfSample_real.getSampleNames());
			pids = vcfSample_real.getParents(oid);
			vcfSample.setSamples(oid+":"+pids[0]+":"+pids[1]);
			samplesNum = 3;
			selectedIndexes = vcfSample.getSelectedIndexes();
			selectedNames=vcfSample.getSelectedNames();
			for (int i=0 ; i<selectedNames.length ; i++){
				if(selectedNames[i].equals(oid))
					o = i;
				else if(selectedNames[i].equals(pids[0]))
					f = i;
				else if(selectedNames[i].equals(pids[1]))
					m = i;
			}
		}
		else
			return null;
		
		variants = new Variants[selectedIndexes.length];
		int selectedLen = selectedIndexes.length;
		
		for (int i = 0; i < selectedLen; i++) {
			variants[i] = new Variants(track.get_ID(), selectedNames[i], doc, mode, bpp, bppLimit, qualLimit, filterLimit);
		}
		
		Vcf vcf = null;
		try {
			String chrom = (Boolean) this.track.get_Parameter(VCF_CHROM_PREFIX) ? chr : chr.substring(3);
			if ("M".equalsIgnoreCase(chrom)) {
				chrom = "MT";
			}
			if (this.track.has_Parameter(VCF_INDEX_LOCAL) && this.track.get_Parameter(VCF_INDEX_LOCAL) != null)
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),(String)this.track.get_Parameter(VCF_INDEX_LOCAL));
			else
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),null);
			TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + start	+ "-" + end);
			if (Query != null) {
				int len = variants.length;
				Variant[][] vs = new Variant[len][];
				while (Query.next() != null) {
					vcf = new Vcf(vcf_tb.lineInChars, vcf_tb.numOfChar, samplesNum, selectedIndexes,true);
					if (vcf.altIsDot())
						continue;
					if (vcf.shouldBeFilteredByQualLimit(qualLimit) || vcf.shouldBeFilteredByFilterLimit(filterLimit))
						continue;
					
					vs = vcf.getVariants_trio(o,f,m,scan);
					if(vs == null)
						continue;
					for (int i = 0; i < len; i++) 
						if (vs[i] != null)
							variants[i].addVariant(vcf,i, vs[i]);
					vs = null;
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
		Element[] e = new Element[3];
		e[0] = variants[o].getVariantsElement();
		e[1] = variants[f].getVariantsElement();
		e[2] = variants[m].getVariantsElement();
		return e;
	}
	public String[] write_ld(String id, String chr, long start, long end, String Assembly) {
		float qualLimit = Float.parseFloat((String) (this.track.get_Parameter(VCF_QUAL_LIMIT)));
		TabixReaderForVCF vcf_tb = null;
		String[] filterLimit = getFilterLimit();

		int samplesNum = 0;
		VcfSample vcfSample_real = null;
		VcfSample vcfSample = null;
		int[] selectedIndexes = null;

		if (this.track.has_Parameter(VCF_HEADER_SAMPLE)) {
			vcfSample_real = ((VcfSample) this.track.get_Parameter(VCF_HEADER_SAMPLE));
			vcfSample = new VcfSample(vcfSample_real.getSampleNames());
			vcfSample.setSamples(id);
			samplesNum = 1;
			selectedIndexes = vcfSample.getSelectedIndexes();
		}
		else
			return null;
		
		if(selectedIndexes==null)
			return null;
		
		ArrayList<String> result_temp = new ArrayList<String>();
		
		Vcf vcf = null;
		try {
			String chrom = (Boolean) this.track.get_Parameter(VCF_CHROM_PREFIX) ? chr : chr.substring(3);
			if ("M".equalsIgnoreCase(chrom)) {
				chrom = "MT";
			}
			if (this.track.has_Parameter(VCF_INDEX_LOCAL) && this.track.get_Parameter(VCF_INDEX_LOCAL) != null)
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),(String)this.track.get_Parameter(VCF_INDEX_LOCAL));
			else
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),null);
			
			TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + start	+ "-" + end);
			List<Vcf> vcfs = new ArrayList<Vcf>();
			List<String[]> ld_all = new LdReader(CfgReader.getBasic(Assembly, Consts.FORMAT_LD).get_Path(chr)).write_ld2matrix(chr, start, end);
			
			if (Query != null && ld_all.size() > 0) {
				HashMap<Integer,Integer> vsx = new HashMap<Integer,Integer>();
				int i = 0;
				while (Query.next() != null) {
					vcf = new Vcf(vcf_tb.lineInChars, vcf_tb.numOfChar, samplesNum, selectedIndexes,true);
					if (vcf.altIsDot())
						continue;
					if (vcf.shouldBeFilteredByQualLimit(qualLimit) || vcf.shouldBeFilteredByFilterLimit(filterLimit))
						continue;
					
					vsx.put((int)vcf.getPos(), i);
					i++;
					vcfs.add(vcf);
				}
				
				String[] ld_temp = null;
				StringSplit splitcomma = new StringSplit(',');
				StringSplit splitslash = new StringSplit('/');
				StringSplit splitpipe = new StringSplit('|');
				String[] phase ;
				String homo_temp;
				String ref1 = null;
				String ref2 = null;
				int[] homo1 = {-2,-2};
				int[] homo2 = {-2,-2};
				
				for(i = 0 ; i < ld_all.size() ; i++){
					ld_temp = ld_all.get(i);
					if(vsx.containsKey(Integer.parseInt(ld_temp[1])) && vsx.containsKey(Integer.parseInt(ld_temp[2]))){
						int v1 = vsx.get(Integer.parseInt(ld_temp[1]));
						int v2 = vsx.get(Integer.parseInt(ld_temp[2]));
						Variant[] vs_cur1 = vcfs.get(v1).getVariants(0);
						Variant[] vs_cur2 = vcfs.get(v2).getVariants(0);
						if(vs_cur1 == null || vs_cur2 == null)
							continue;
						ref1 = vcfs.get(v1).getRef();
						ref2 = vcfs.get(v2).getRef();
						String[] alts1 = new String[1];
						String[] alts2 = new String[1];
						alts1[0] = vcfs.get(v1).getAlt();
						alts2[0] = vcfs.get(v2).getAlt();
						
						splitslash.split(ld_temp[3]);
						phase = splitslash.getResult();
						
						int[] p0 = {-1,-1};
						int[] p1 = {-1,-1};
						
						/* example: phase AT/CG
						 * 
						 * case1:
						 * v1 ref:C alt:A
						 * v2 ref:G alt:T
						 * p0[0] = 1; p0[1] = 1
						 * p1[0] = 0; p1[1] = 0
						 * 
						 * case2:
						 * v1 ref:C alt:A
						 * v2 ref:T alt:G
						 * p0[0] = 1; p0[1] = 0
						 * p1[0] = 0; p1[1] = 1
						 * 
						 * v1 1|1
						 * v2 0|1
						 * 
						 * homo1[0] = 0; homo1[1] = 1
						 * homo2[0] = 1; homo2[1] = 1
						 * 
						 * in case 1: allele 1 : no, allele 2 : yes
						 * in case 2: allele 1 : yes, allele 2 : no
						 * 
						 * v1 1|0
						 * v2 0|1
						 * 
						 * homo1[0] = 0; homo1[1] = 1
						 * homo2[0] = 1; homo2[1] = 1
						 * 
						 * in case 1: allele 1 : no, allele 2 : no
						 * in case 2: allele 1 : yes, allele 2 : yes 
						 */
						
						//map(translate) ***phase data in ld*** to ref/alt genotype code in Vcf.
						//thus we obtain the actual LD mates in Vcf ref/alt context
						if(!Vcf.containChar(alts1[0], ',') && !Vcf.containChar(alts2[0], ',')){
							if (phase[0].equals(alts1[0]+alts2[0]) && phase[1].equals(ref1+ref2)){
								p0[0] = 1; p0[1] = 1; p1[0] = 0; p1[1] = 0;
							}
							else if (phase[0].equals(alts1[0]+ref2) && phase[1].equals(ref1+alts2[0])){
								p0[0] = 1; p0[1] = 0; p1[0] = 0; p1[1] = 1;
							}
							else if (phase[0].equals(ref1+alts2[0]) && phase[1].equals(alts1[0]+ref2)){
								p0[0] = 0; p0[1] = 1; p1[0] = 1; p1[1] = 0;
							}
							else if (phase[0].equals(ref1+ref2) && phase[1].equals(alts1[0]+alts2[0])){
								p0[0] = 0; p0[1] = 0; p1[0] = 1; p1[1] = 1;
							}
						}
						else {
							splitcomma.split(alts1[0]);
							alts1 = new String[splitcomma.getResultNum()+1];
							alts1[0] = ref1;
							for(int j = 0 ; j < alts1.length-1 ; j++)
								alts1[j+1] = splitcomma.getResultByIndex(j);
							
							splitcomma.split(alts2[0]);
							alts2 = new String[splitcomma.getResultNum()+1];
							alts2[0] = ref2;
							for(int j = 0 ; j < alts2.length-1 ; j++)
								alts2[j+1] = splitcomma.getResultByIndex(j);
							
							for(int i1 = 0 ; i1 < alts1.length ; i1++)
								for(int i2 = 0 ; i2 < alts2.length ; i2++){
									if(phase[0].equals(alts1[i1]+alts2[i2])){
										p0[0] = i1; p0[1] = i2;
									}
									if(phase[1].equals(alts1[i1]+alts2[i2])){
										p1[0] = i1;	p1[1] = i2;
									}
								}
						}
						
						//map(translate) ***sample genotype data in vcf sampleInfo*** to ref/alt genotype code in Vcf.
						// v1
						homo_temp = vs_cur1[0].getHomo();
						if(Vcf.containChar(homo_temp,'|')){
							splitpipe.split(homo_temp);
							homo1[0] = Integer.parseInt(splitpipe.getResultByIndex(0));
							homo1[1] = Integer.parseInt(splitpipe.getResultByIndex(1));
						} 
						else if (Vcf.containChar(homo_temp, '/')){
							splitslash.split(homo_temp);
							homo1[0] = Integer.parseInt(splitslash.getResultByIndex(0));
							homo1[1] = Integer.parseInt(splitslash.getResultByIndex(1));
						}
						else{
							homo1[0] = Integer.parseInt(homo_temp);
							homo1[1] = Integer.parseInt(homo_temp);
						}
						
						//v2
						homo_temp = vs_cur2[0].getHomo();
						if(Vcf.containChar(homo_temp,'|')){
							splitpipe.split(homo_temp);
							homo2[0] = Integer.parseInt(splitpipe.getResultByIndex(0));
							homo2[1] = Integer.parseInt(splitpipe.getResultByIndex(1));
						} 
						else if (Vcf.containChar(homo_temp, '/')){
							splitslash.split(homo_temp);
							homo2[0] = Integer.parseInt(splitslash.getResultByIndex(0));
							homo2[1] = Integer.parseInt(splitslash.getResultByIndex(1));
						}
						else{
							homo2[0] = Integer.parseInt(homo_temp);
							homo2[1] = Integer.parseInt(homo_temp);
						}
						
						Variant[] vs1 = vcfs.get(v1).getVariants(false);
						Variant[] vs2 = vcfs.get(v2).getVariants(false);
						if(p0[0] == homo1[0] && p0[1] == homo2[0] || p1[0] == homo1[0] && p1[1] == homo2[0]){
							//allele 1 yes
							int idx1 = homo1[0]>0?homo1[0]-1:homo1[0];
							int idx2 = homo2[0]>0?homo2[0]-1:homo2[0];
							result_temp.add(
									chr+":"+vs1[idx1].getFrom()+":"+vs1[idx1].getTo()+":"+(vs1[idx1].getLetter()!=null?vs1[idx1].getLetter():"-")+";"+
									chr+":"+vs2[idx2].getFrom()+":"+vs2[idx2].getTo()+":"+(vs2[idx2].getLetter()!=null?vs2[idx2].getLetter():"-")+";"+
									"0"+";"+
									ld_temp[4]
											);
							
						}
						if(p0[0] == homo1[1] && p0[1] == homo2[1] || p1[0] == homo1[1] && p1[1] == homo2[1]){
							//allele 2 yes
							int idx1 = homo1[1]>0?homo1[1]-1:homo1[1];
							int idx2 = homo2[1]>0?homo2[1]-1:homo2[1];
							result_temp.add(
									chr+":"+vs1[idx1].getFrom()+":"+vs1[idx1].getTo()+":"+(vs1[idx1].getLetter()!=null?vs1[idx1].getLetter():"-")+";"+
									chr+":"+vs2[idx2].getFrom()+":"+vs2[idx2].getTo()+":"+(vs2[idx2].getLetter()!=null?vs2[idx2].getLetter():"-")+";"+
									"1"+";"+
									ld_temp[4]
											);
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
		String[] result = new String[result_temp.size()];
		result_temp.toArray(result);
		return result;
	}
	public Element write_vcf2variants(Document doc, String track, String mode,
			double bpp/* bases per pixel */, String chr, long start, long end) {
		float qualLimit = Float.parseFloat((String) (this.track.get_Parameter(VCF_QUAL_LIMIT)));
		TabixReaderForVCF vcf_tb = null;
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
			if (this.track.has_Parameter(VCF_INDEX_LOCAL) && this.track.get_Parameter(VCF_INDEX_LOCAL) != null)
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),(String)this.track.get_Parameter(VCF_INDEX_LOCAL));
			else
				vcf_tb = new TabixReaderForVCF(this.track.get_Path(this.Chr),null);
			TabixReaderForVCF.Iterator Query = vcf_tb.query(chrom + ":" + start	+ "-" + end);
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
						vs = vcf.getVariants(true);
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
