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

	private SeekableStream ss = null;

	private List<BBZoomLevelHeader> zoomLevelHeaders;

	/**
	 * zoomLevelBases limit. zoomLevelBases must less than:<br>
	 * ((end - (start - 1)) / (float) width) / zoomLevelBases;
	 */
	private int zoomLevelBasesLimit = 20;

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

		zoomLevelHeaders = reader.getZoomLevels().getZoomLevelHeaders();
	}

	/**
	 * 
	 * @param chrom
	 * @param start
	 *            1-base
	 * @param end
	 *            1-base
	 * @param contained
	 *            whether allow contained
	 * @param bigWigs
	 * @param windowSize
	 *            width of screen
	 * @param step
	 *            A step defines how many pixes show in a grid
	 * @return A reference of this instance.
	 * @throws IOException
	 */
	public BigWigReader getBigWig(String chrom, int start, int end,
			boolean contained, DataValueList bigWigs, int windowSize, int step) {
		// get the big header
		bbFileHdr = reader.getBBFileHeader();

		if (chrom == null || !bbFileHdr.isHeaderOK() || !bbFileHdr.isBigWig())
			return this;
		// chromosome was specified, test if it exists in this file
		if (!(new HashSet<String>(reader.getChromosomeNames()).contains(chrom)))
			return this;
		int zoomLevel = getZoomLevel(start, end, windowSize, step);
		if (zoomLevel == 0) {
			bigWigs.setZoomLevelBases(1);
			// iterator for BigWig values which occupy the specified
			BigWigIterator iter = reader.getBigWigIterator(chrom, start - 1,
					chrom, end, contained);
			// startChromosome region.
			// loop over iterator
			DataValue dv = null;
			while (iter.hasNext()) {
				WigItem f = iter.next();
				dv = new DataValue(f.getChromosome(), f.getStartBase(),
						f.getEndBase(), f.getWigValue());
				bigWigs.update(dv);
			}
		} else {
			// iterator for ZoomLevels which occupy the specified
			bigWigs.setZoomLevelBases(zoomLevelHeaders.get(zoomLevel)
					.getReductionLevel());
			ZoomLevelIterator iter = reader.getZoomLevelIterator(zoomLevel,
					chrom, start - 1, chrom, end, contained);
			// startChromosome region.
			// loop over iterator
			DataValue dv = null;
			while (iter.hasNext()) {
				ZoomDataRecord f = iter.next();
				dv = new DataValue(f.getChromName(), f.getChromStart(),
						f.getChromEnd(), f.getMeanVal());
				bigWigs.update(dv);
			}
		}

		return this;
	}

	/**
	 * Return ZoomLevel. If return 0, we should use BigWigIterator; else if
	 * return greater than 0, we should use ZoomLevelIterator.
	 * 
	 * @param start
	 * @param end
	 * @param windowSize
	 * @param step
	 * @return
	 */
	private int getZoomLevel(int start, int end, int windowSize, int step) {
		if (zoomLevelHeaders == null || zoomLevelHeaders.size() == 0)
			return 0;
		int width = ((end - (start - 1)) < (windowSize / step)) ? (end - (start - 1))
				: (windowSize / step);
		float basePerWidthDivided = ((end - (start - 1)) / (float) width)
				/ zoomLevelBasesLimit;
		for (int index = 0, zoomLevelCount = zoomLevelHeaders.size(); index < zoomLevelCount; index++) {
			if (zoomLevelHeaders.get(index).getReductionLevel() > basePerWidthDivided)
				return index;
		}
		return zoomLevelHeaders.size();
	}

	public void close() {
		try {
			if (ss != null)
				this.ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
