package FileReaders;

import java.util.*;
import java.io.*;

/*
 * This is for reading BED files.
 * We don't interpret the specific content in this level,
 * because a lot of genomic data can be stored in BED formats.
 * BED itself is display-oriented.
 * BED is also a typical tab-delimited file format, 
 * which can be compressed, indexed and read by Tabix API.
 * But we consider BED as a light-weight up-to-2-level genomic region representation format here,
 * and believe it is convenient if we keep BED as Text-based format, not compressed binary format,
 * and thus we can tolerant unsorted or unindexed files to display.
 * For this purpose, we read the whole file into the memory and traverse all of the records, to find 
 * the ones should be displayed in the current window when a query is executed.
 */

class BedReader implements Consts{
	String beds[];
	BedReader(String bed){
		File bed_file=new File(bed);
		ByteBufferChannel bbc=new ByteBufferChannel(bed_file,0,bed_file.length());
		String temp=bbc.ToString(DEFAULT_ENCODE);
		beds=temp.split("\n");
	}

	Bed[] extract_bed(String chr, long start, long end){
		ArrayList<Bed> beds_internal=new ArrayList<Bed>();
		for(int i=0;i<beds.length;i++){
			Bed bed_temp=new Bed(beds[i]);
			if (bed_temp.chrom.equals(chr)&&bed_temp.chromStart<end&&bed_temp.chromEnd>=start){
				beds_internal.add(bed_temp);
			}
		}
		return beds_internal.toArray(new Bed[beds_internal.size()]);
	}
}