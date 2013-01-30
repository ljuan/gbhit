package FileReaders;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

public class FastaReader {
	Map<String, Integer> seq_name;
	long[][] fasta_index;
	RandomAccessFile raf = null;

	public FastaReader(String fasta) throws IOException {
		String temp = "";
		File idx_file = new File(fasta + ".fai");
		ByteBufferChannel bbc = new ByteBufferChannel(idx_file, 0,
				idx_file.length());
		temp = bbc.ToString(Consts.DEFAULT_ENCODE);

		String[] index_temp = temp.split("\n");
		fasta_index = new long[index_temp.length][4];
		seq_name = new HashMap<String, Integer>(index_temp.length, 1);
		for (int i = 0; i < index_temp.length; i++) {
			String[] line_temp = index_temp[i].split("\t");
			seq_name.put(line_temp[0], i);
			for (int j = 0; j < 4; j++)
				fasta_index[i][j] = Long.parseLong(line_temp[j + 1]);
		}

		raf = new RandomAccessFile(new File(fasta), "r");
	}

	/**
	 * Extract bases fom <code>start</code> to <code>end</code>. All bases will
	 * be expressed in capital letter.
	 * 
	 * @param chr	chromosome name
	 * @param start	1-base
	 * @param end	1-base
	 * @return
	 * @throws IOException
	 */
	public String extract_seq(String chr, long start, long end)
			throws IOException {
		Integer chr_info = seq_name.get(chr);
		if(chr_info == null) return null;
		start--;
		end--;// Genomic coordinate is 1-based, java index is 0-based.

		int bases1Line = (int) fasta_index[chr_info][2];
		int enterLen = (int) (fasta_index[chr_info][3] - bases1Line);
		start += (start / bases1Line) * enterLen;
		end += (end / bases1Line) * enterLen;
		byte[] bytes = new byte[(int) (end - start + 1)];
		raf.seek(start + fasta_index[chr_info][1]);
		raf.read(bytes, 0, bytes.length);
		return delEnterAndToUpperCase(bytes);
	}

	/**
	 * Extract just one base fom <code>pos</code>. The base will be expressed in
	 * capital letter.
	 * 
	 * @param chr	chromosome name
	 * @param pos	1-base
	 * @return
	 * @throws IOException
	 */
	public char extract_char(String chr, int pos) throws IOException {
		Integer chr_info = seq_name.get(chr);
		if(chr_info == null) return '\0';
		pos--;// Genomic coordinate is 1-based, java index is 0-based.
		int bases1Line = (int) fasta_index[chr_info][2];
		int enterLen = (int) (fasta_index[chr_info][3] - bases1Line);
		long start = pos + ((pos / bases1Line) * enterLen);
		raf.seek(start + fasta_index[chr_info][1]);
		int b = raf.read();
		return (b >= 'A' && b <= 'Z') ? (char) b : (char) (b - 32);
	}

	/*
	 * equal: new String(bytes, 0, raf.read(bytes)).replaceAll("\n", "").toUpperCase()
	 */
	private String delEnterAndToUpperCase(byte[] bytes) {
		int len = bytes.length;
		byte[] copy = new byte[len];
		byte b = 0;
		int index = 0;
		for (int i = 0; i < len; i++) {
			b = bytes[i];
			if (b >= 'A' && b <= 'Z') {
				copy[index++] = b;
			} else if (b >= 'a' && b <= 'z') {
				copy[index++] = (byte) (b - 32);
			}
		}
		return new String(copy, 0, index);
	}

	/**
	 * If you needn't to use FastaReader anymore, don't forget to close it!
	 */
	public void close() {
		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * Get the designate chromosome length.
	 * @param chr chromosome name
	 * @return
	 */
	public long getChromosomeLength(String chr){
		Integer chr_info = seq_name.get(chr);
		if(chr_info == null) return 0;
		return fasta_index[chr_info][0];
	}

	public Element write_sequence(Document doc, String chr, long start,
			long end, String id) throws IOException {
		String seq = extract_seq(chr, start, end);
		Element sequence = XmlWriter.append_text_element(doc, doc
				.getElementsByTagName(Consts.DATA_ROOT).item(0),
				Consts.XML_TAG_SEQUENCE, seq);
		sequence.setAttribute(Consts.XML_TAG_ID, id);
		return sequence;
	}
}