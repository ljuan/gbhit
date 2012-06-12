package FileReaders;

import java.io.IOException;
import java.util.ArrayList;
/*
 * BedReader implemented by Tabix
 * Require sorted and indexed bed file
 */
class BedReaderTabix{
	TabixReader bed_tb;
	BedReaderTabix(String bed){
		try{
			bed_tb=new TabixReader(bed);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	Bed[] extract_bed(String chr,long start,long end){
		StringBuffer querystr=new StringBuffer();
		querystr.append(chr);
		querystr.append(':');
		querystr.append(start);
		querystr.append('-');
		querystr.append(end);
		TabixReader.Iterator Query=bed_tb.query(querystr.toString());
		ArrayList<Bed> bed_internal=new ArrayList<Bed>();
		String line;
		try{
			while((line=Query.next())!=null){
				bed_internal.add(new Bed(line));
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return bed_internal.toArray(new Bed[bed_internal.size()]);
	}
}