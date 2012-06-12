package FileReaders;


import java.io.IOException;
import java.util.*;
//import org.broad.tribble.readers.TabixReader;

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

class VcfReader{
	TabixReader vcf_tb;
	Hashtable<String,String> filter_header;
	Hashtable<String,String[]> info_header;
	Hashtable<String,String[]> format_header;
	String[] samples;
	boolean chromprefix=false;
	VcfReader(String vcf){
		try {
			vcf_tb=new TabixReader(vcf);
			String line="";
			filter_header=new Hashtable<String,String>();
			filter_header.put("PASS", "PASS");
			info_header=new Hashtable<String, String[]>();
			format_header=new Hashtable<String,String[]>();
			while((line=vcf_tb.readLine()).startsWith("#")){
				if(line.substring(2,8).equalsIgnoreCase("FILTER")){
					int left=line.indexOf('<');
					int right=line.lastIndexOf('>');
					String[] line_temp=line.substring(left+1, right).split("ID=|,Description=");
					filter_header.put(line_temp[1],line_temp[2].substring(1, line_temp[2].length()-1));
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
					else
						samples=new String[0];
				}
			}
			if(line.startsWith("chr"))
				chromprefix=true;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	Vcf[] extract_vcf(String chr,long start,long end){
		
		StringBuffer querystr=new StringBuffer();
		if(chromprefix)
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
		TabixReader.Iterator Query=vcf_tb.query(querystr.toString());
		ArrayList<Vcf> vcf_internal=new ArrayList<Vcf>();
		String line;
		try{
			while((line=Query.next())!=null){
				Vcf vcf_temp;
				if(samples.length==0)
					vcf_temp=new Vcf(line);
				else
					vcf_temp=new Vcf(line,samples.length);
				vcf_internal.add(vcf_temp);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return vcf_internal.toArray(new Vcf[vcf_internal.size()]);
	}
}