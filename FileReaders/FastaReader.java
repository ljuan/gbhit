package FileReaders;

import java.io.*;
import java.util.*;

/* 
 * This class is for reading fasta file, i.e. Reference Genome
 * We also keep a instance of FastaReader in user instance class,
 * to facilitate querries related to chromosomes and their length.
 * Different from other formats, e.g. VCF, BED, etc., 
 * there is no individual class for fasta format because it is so simple.
 * Simple to read, simple to represent.
 * The extract_seq method is not limited regarding request length. 
 * It can return the whole chromosome if necessary.
 * But such kind of limitation must be applied in the upper level, 
 * to save unnecessary operation.
 */

class FastaReader implements Consts{
	Hashtable<String, Integer> seq_name;
	long[][] fasta_index;
	File fasta_file;
	FastaReader(String fasta){
		String temp="";
		File idx_file=new File(fasta+".fai");
		this.fasta_file=new File(fasta);
		ByteBufferChannel bbc=new ByteBufferChannel(idx_file,0,idx_file.length());
		temp=bbc.ToString(DEFAULT_ENCODE);

		String[] index_temp=temp.split("\n");
		fasta_index=new long[index_temp.length][4];
		seq_name=new Hashtable<String, Integer>(index_temp.length,1);
		for(int i=0;i<index_temp.length;i++){
			String[] line_temp=index_temp[i].split("\t");
			seq_name.put(line_temp[0], i);
			for(int j=0;j<4;j++)
				fasta_index[i][j]=Long.parseLong(line_temp[j+1]);
		}
	}
	String extract_seq(String chr, long start, long end){
		String seq="";
		int chr_info=(int)seq_name.get(chr);
		start--;end--;//Genomic coordinate is 1-based, java index is 0-based.
		start+=start/fasta_index[chr_info][2]*(fasta_index[chr_info][3]-fasta_index[chr_info][2]);
		end+=end/fasta_index[chr_info][2]*(fasta_index[chr_info][3]-fasta_index[chr_info][2]);
		ByteBufferChannel bbc=new ByteBufferChannel(fasta_file,start+fasta_index[chr_info][1],end-start+1);
		seq=bbc.ToString(DEFAULT_ENCODE).replaceAll("\n", "");
		return seq.toUpperCase();
	}
	
}