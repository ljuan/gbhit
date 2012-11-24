package FileReaders.wiggle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import org.broad.igv.bbfile.*;
import org.broad.tribble.util.SeekableStream;

import FileReaders.BAMReader;

/**
 * Get dataValues from BigWig.
 * 
 * <pre>
 *  Usage: new BigWigReader(String uri).(String chrom, int start, 
 *  	   int end, boolean contained, List<DataValue> bigWigs)
 * 
 * @author Chengwu Yan
 * 
 */
public class BigWigReader {
	// open big file
	private BBFileReader reader;
	// get the big header
	private BBFileHeader bbFileHdr;

	SeekableStream ss = null;

	/**
	 * 
	 * @param uri
	 *            url or file path
	 * @throws IOException
	 */
	public BigWigReader(String uri) throws IOException {
		// open big file
		if (uri.startsWith("http://") || uri.startsWith("ftp://")
				|| uri.startsWith("https://")) {
			if (!BAMReader.isRemoteFileExists(uri))
				throw new FileNotFoundException("can't find file for:" + uri);
			ss = new CustomSeekableBufferedStream(new CustomSeekableHTTPStream(
					new URL(uri)));
			reader = new BBFileReader(uri, ss);
		} else {
			reader = new BBFileReader(uri);
		}
	}

	/**
	 * 
	 * @param chrom
	 * @param start
	 *            0-base
	 * @param end
	 *            0-base
	 * @param contained
	 *            whether allow overlaps
	 *            true: not allow overlaps(completely contained only)
	 *            false: allow overlaps
	 * @param bigWigs
	 * @return
	 * @throws IOException
	 */
	public void getBigWig(String chrom, int start, int end, boolean contained,
			List<DataValue> bigWigs) {
		// get the big header
		bbFileHdr = reader.getBBFileHeader();

		if (chrom == null || !bbFileHdr.isHeaderOK() || !bbFileHdr.isBigWig())
			return;
		// chromosome was specified, test if it exists in this file
		if (!(new HashSet<String>(reader.getChromosomeNames()).contains(chrom)))
			return;
		// iterator for BigWig values which occupy the specified
		BigWigIterator iter = reader.getBigWigIterator(chrom, start, chrom,
				end, contained);
		// startChromosome region.
		// loop over iterator
		while (iter.hasNext()) {
			WigItem f = iter.next();
			bigWigs.add(new DataValue(f.getChromosome(), f.getStartBase(), f
					.getEndBase(), f.getWigValue() + ""));
		}
		this.close();
	}

	private void close() {
		try {
			if (ss != null)
				this.ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
