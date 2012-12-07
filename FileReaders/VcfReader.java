package FileReaders;


import java.io.IOException;
import java.util.*;
//import org.broad.tribble.readers.TabixReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

class VcfReader implements Consts{
	TabixReader vcf_tb;
	Annotations track=null;
	VcfReader(Annotations track, String Chr){
		try {
			vcf_tb=new TabixReader(track.get_Path(Chr));
			if (!(track.Parameter.containsKey(VCF_HEADER_FILTER) || track.Parameter.containsKey(VCF_HEADER_FORMAT) || 
					track.Parameter.containsKey(VCF_HEADER_INFO) || track.Parameter.containsKey(VCF_HEADER_SAMPLE))){
				HashMap<String,Boolean> filter_header=new HashMap<String,Boolean>();
				Hashtable<String,String[]> info_header;
				Hashtable<String,String[]> format_header;
				String[] samples=null;
				String line="";
				filter_header.put("PASS", true);
				info_header=new Hashtable<String, String[]>();
				format_header=new Hashtable<String,String[]>();
				while((line=vcf_tb.readLine()).startsWith("#")){
					if(line.substring(2,8).equalsIgnoreCase("FILTER")){
						int left=line.indexOf('<');
						int right=line.lastIndexOf('>');
						String[] line_temp=line.substring(left+1, right).split("ID=|,Description=");
						filter_header.put(line_temp[1], false);
					}
					else if(line.substring(2,6).equalsIgnoreCase("INFO")){
						int left=line.indexOf('<');
						int right=line.lastIndexOf('>');
						String[] line_temp=line.substring(left+1, right).split("ID=|,Number=|,Type=|,Description=");
						info_header.put(line_temp[1], Arrays.copyOfRange(line_temp, 2, 5));
					}
					else if(line.substring(2, 8).equalsIgnoreCase("FORMAT")){
						int left=line.indexOf('<');
						int right=line.lastIndexOf('>');
						String[] line_temp=line.substring(left+1, right).split("ID=|,Number=|,Type=|,Description=");
						format_header.put(line_temp[1], Arrays.copyOfRange(line_temp, 2, 5));
					}
					else if(line.startsWith("#CHROM")){
						String[] line_temp=line.split("\t");
						if(line_temp.length>9){
							samples=new String[line_temp.length-9];
							for(int i=9;i<line_temp.length;i++){
								samples[i-9]=line_temp[i];
							}
						}
					}
					boolean chromprefix=false;
					if(line.startsWith("chr"))
						chromprefix=true;
					track.Parameter.put(VCF_HEADER_FILTER, filter_header);
					track.CurrentSetting.put(VCF_HEADER_FILTER, null);
					track.Parameter.put(VCF_HEADER_FORMAT, format_header);
					track.CurrentSetting.put(VCF_HEADER_FORMAT, null);
					track.Parameter.put(VCF_HEADER_INFO, info_header);
					track.CurrentSetting.put(VCF_HEADER_INFO, null);
					track.Parameter.put(VCF_HEADER_SAMPLE, samples);
					track.CurrentSetting.put(VCF_HEADER_SAMPLE, null);
					track.Parameter.put(VCF_CHROM_PREFIX, chromprefix);
					track.CurrentSetting.put(VCF_CHROM_PREFIX, null);
				}
			}
			this.track=track;
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	void set_Parameters(){
		String[] params=new String[track.Parameter.size()];
		track.Parameter.keySet().toArray(params);
		for(int i=0;i<params.length;i++)
			if(track.CurrentSetting.get(params[i])!=null && !track.CurrentSetting.get(params[i]).equals("")){
				/* put new parameters in track.CurrentSetting into track.Parameter
				 * There will be one single string in track.CurrentSetting
				 * track.CurrentSetting
				 * null means no change for the parameter params[i] after initializing (do nothing)
				 * "" means no change since the last effective parameter setting (do nothing)
				 * "SOME CONTENT(STRING)" means the settings have been changed since the last effective parameter setting
				 * And these changes need to be update to the track.Parameters
				 * You need to analyze the String in track.CurrentSetting.get(params[i]),
				 * and update track.Parameters.get(params[i])
				 */
			}
	}
	Vcf[] extract_vcf(String chr,long start,long end){
		
		StringBuffer querystr=new StringBuffer();
		if((Boolean)(track.Parameter.get(VCF_CHROM_PREFIX)))
			querystr.append(chr);
		else
			querystr.append(chr.substring(3));
		querystr.append(':');
		querystr.append(start);
		querystr.append('-');
		querystr.append(end);
		
		/* 
		 * This is stupid, TabixReader read colon-hyphen seperated genomic region
		 * AND three integers represent chr, start, end respectively by over-loading, which is also public.
		 * AND in TabixReader, genomic region in colon-hyphen form is going to be transformed to three-integers form first.
		 * The reason we use this stupid method because:
		 * 1. TabixReader.chr2tid method is private, we can't obtain corresponding tid for a chromosome by maintaining Heng Li's TabixReader as it is.
		 * 2. TabixReader.query require integer type for both start and end coordinate, while we define genomic coordinate as long type.
		 */
		ArrayList<Vcf> vcf_internal=new ArrayList<Vcf>();
		String line;
		try{
			TabixReader.Iterator Query=vcf_tb.query(querystr.toString());
			while(Query !=null && (line=Query.next()) != null){
				Vcf vcf_temp;
				if(((String[])(track.Parameter.get(VCF_HEADER_SAMPLE)))==null)
					vcf_temp=new Vcf(line);
				else
					vcf_temp=new Vcf(line,((String[])(track.Parameter.get(VCF_HEADER_SAMPLE))).length);
				vcf_internal.add(vcf_temp);
			}
			vcf_tb.TabixReaderClose();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return vcf_internal.toArray(new Vcf[vcf_internal.size()]);
	}
	Element write_vcf2variants(Document doc, String track, String mode, double bpp, String chr, long start, long end){//bpp:bases per pixel
		Vcf[] vcf=extract_vcf(chr,start,end);
		Element Variants=doc.createElement(XML_TAG_VARIANTS);
		Variants.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Variants);
		long lastpos=0;
		for(int i=0;i<vcf.length;i++){
			String[] alt_temp=vcf[i].Alt.split(",");
			int range=vcf[i].Ref.length()-alt_temp[0].length();
			if(range<0)
				range=0;
			if(mode.equals(MODE_DENSE)&&vcf[i].Pos+range-lastpos<bpp)
				continue;
			Element Variant=doc.createElement(XML_TAG_VARIANT);
			Variant.setAttribute(XML_TAG_ID, vcf[i].ID);
			if(alt_temp.length>1)
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_MULTIPLE);
			else if(alt_temp[0].length()<vcf[i].Ref.length())
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_DELETION);
			else if(alt_temp[0].length()>vcf[i].Ref.length())
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_INSERTION);
			else if(alt_temp[0].length()==vcf[i].Ref.length())
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_SNV);
			else
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_OTHERS);
			XmlWriter.append_text_element(doc,Variant,XML_TAG_FROM,String.valueOf(vcf[i].Pos));
			XmlWriter.append_text_element(doc,Variant,XML_TAG_TO,String.valueOf(vcf[i].Pos+range));
			lastpos=vcf[i].Pos+range;
			if(!mode.equals(MODE_DENSE)&&bpp<0.5)
				XmlWriter.append_text_element(doc,Variant,XML_TAG_LETTER,vcf[i].Alt);
			if(mode.equals(MODE_DETAIL)){
				StringBuffer Description=new StringBuffer();
				Description.append("QUAL:");
				Description.append(vcf[i].Qual);		
				Description.append(";FILTER:");
				Description.append(vcf[i].Filter);		
				Description.append(";INFO:");
				Description.append(vcf[i].Info);		
				if(vcf[i].Samples.length>0){
					Description.append(";FORMAT:");
					Description.append(vcf[i].Format);
					Description.append(";SAMPLES:");
					for(int j=0;j<vcf[i].Samples.length;j++){
						Description.append(vcf[i].Samples[j]);
						if(j<vcf[i].Samples.length-1)
							Description.append(",");
					}
				}
				XmlWriter.append_text_element(doc,Variant,XML_TAG_DESCRIPTION,Description.toString());
			}
			Variants.appendChild(Variant);
		}
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Variants);
		return Variants;
	}
}