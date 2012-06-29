package FileReaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/* 
 * ByteBufferChannel Class
 * A component in FileReader System.
 * We applied nio.channel.map method in some of queries, including Fasta and Wig(Planning).
 * This method shows high and stable performance in various scale of querries.
 * However, since we plan to use Heng Li's Tabix/Samtools API to read tab-delimited and bam format,
 * which are implemented by traditional io methods,
 * this strategy won't completely replace the old io methods.
 */

class ByteBufferChannel {
	ByteBuffer file_byte_buffer;
	ByteBufferChannel(File file,long offset,long length){
		try{
			FileChannel file_channel=new FileInputStream(file).getChannel();
			file_byte_buffer=file_channel.map(FileChannel.MapMode.READ_ONLY, offset, length);
			file_channel.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	String ToString(String Encode){
		String str="";
		try{
			str=Charset.forName(Encode).newDecoder().decode(file_byte_buffer).toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return str;
	}
	ByteBuffer getByteBuffer(){
			return file_byte_buffer;
	}
}