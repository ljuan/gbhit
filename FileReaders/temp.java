package FileReaders;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

import java.util.*;
import net.sf.samtools.*;
public class temp {
	public temp(){
		Cf ccff=new Cf();
		ccff.compare_filereaders();
		LinearIndex li=new LinearIndex(0, 0, null);
	}
}

class Cf{
	public Cf(){
		
	}
	public void compare_filereaders(){

		File file=new File("input/hg19.fa");
		Long filelength=file.length();
		System.out.println(filelength);
		long averagetime2=0;
		long averagetime4=0;
		long averagetime5=0;
		String meannings2="";
		String meannings4="";
		String meannings5="";
		int limit=1000;
		Long[][] seqinfo=new Long[limit][2];
		for(int times=0;times<limit;times++){
			seqinfo[times][0]=Math.round(Math.random()*40000);
			seqinfo[times][1]=Math.round(Math.random()*(filelength-40000));
		}
		for(int times=0;times<limit;times++){
			random_access(file,seqinfo[times][0],seqinfo[times][1]);
			averagetime2+=random_access(file,seqinfo[times][0],seqinfo[times][1]);
			averagetime4+=map_random(file,seqinfo[times][0],seqinfo[times][1]);
			averagetime5+=map_is(file,seqinfo[times][0],seqinfo[times][1]);
		}
		
		System.out.println("RandomAccess seek and Read");
		System.out.println(averagetime2/limit);
	//	System.out.println(meannings2);
		System.out.println("RandomAccess channel-map and bytebuffer");
		System.out.println(averagetime4/limit);
	//	System.out.println(meannings4);
		System.out.println("FileInputStream channel-map and decode");
		System.out.println(averagetime5/limit);
	//	System.out.println(meannings5);
	}
	long map_is(File file,Long seqlength, Long seqoffset){
		long begintime=System.nanoTime();
		try{
			FileChannel fc2=new FileInputStream(file).getChannel();
			ByteBuffer bb2=fc2.map(FileChannel.MapMode.READ_ONLY, seqoffset, seqlength);
			fc2.close();
			String meannings=Charset.forName("ISO-8859-1").newDecoder().decode(bb2).toString().replace("\n","");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		long endtime=System.nanoTime();
		return endtime-begintime;
	}
	long random_access(File file,Long seqlength, Long seqoffset){
		long begintime=System.nanoTime();
		try{				
			RandomAccessFile rf=new RandomAccessFile(file,"r");
			rf.seek(seqoffset);
			byte[] filecontent2=new byte[seqlength.intValue()];
			rf.read(filecontent2);
			rf.close();
			String meannings2=new String(filecontent2).replace("\n", "");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		long endtime=System.nanoTime();
		return endtime-begintime;
	}
	long map_random(File file,Long seqlength, Long seqoffset){
		long begintime=System.nanoTime();
		try{
			FileChannel fc=new RandomAccessFile(file,"r").getChannel();
			ByteBuffer bb=fc.map(FileChannel.MapMode.READ_ONLY, seqoffset, seqlength);
			fc.close();
			Charset charset2=null;
			CharsetDecoder decoder2=null;
			CharBuffer cb2=null;
			charset2=Charset.forName("ISO-8859-1");
			decoder2=charset2.newDecoder();
			cb2=decoder2.decode(bb);
			String meannings4=cb2.toString().replace("\n", "");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		long endtime=System.nanoTime();
		return endtime-begintime;
	}
}