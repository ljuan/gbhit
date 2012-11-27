package FileReaders;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import net.sf.samtools.*;
import net.sf.samtools.SAMRecord.SAMTagAndValue;
import net.sf.samtools.util.SeekableBufferedStream;
import net.sf.samtools.util.SeekableHTTPStream;

/**
 * <pre>
 * usage:
 * Firstly, Get an instance of this class:
 *   public BAMReader(String filePath)
 *   if index file exists, use getIndexFilePath() to get .bai filepath through; else, throw FileNotFoundException 
 * Secondly: insert elements into Document Object:
 * 	 readBAM(Document doc, String chr, int start, int end, int windowSize, int step, String mode, String track)
 * 
 * @author Chengwu Yan
 * 
 */
public class BAMReader implements Consts {

	/**
	 * BAM file path or url.
	 */
	private String filePath;
	/**
	 * BAM index file path
	 */
	private String indexFilePath;

	/**
	 * If the BAM file comes from remote server: fromRemoteServer=true, false
	 * else. If fromRemoteServer=false, filePath is a path of a local BAM file,
	 * else an url of a remote BAM file.
	 */
	private boolean fromRemoteServer;

	private SAMFileReader samReader = null;

	private static final int SIXTEENK = 16 * 1024;;

	/**
	 * position of each chromosome's first read's first base in the BAM file.
	 * Number of this array's elements is (sequenceNum + 1), the last one is the
	 * BAM file size.
	 */
	private long[] chrFileSize = null;

	/**
	 * last linear bin span of each chromosome
	 */
	private long[] chrLastLinearSpan = null;
	/**
	 * number of reads of the given chromosome in the BAM file
	 */
	private int chrReadNum;

	/**
	 * total number of linear bin of each chromosome in the BAM file
	 */
	private int[] chrLinearBinNum = null;

	/**
	 * number of chromosome in the BAM file
	 */
	private int sequenceNum = 0;

	public BAMReader() {
	}

	/**
	 * 
	 * @param filePath
	 *            file path or url of the BAM file.
	 * @param indexFilePath
	 *            file path of the BAM index file in the server.
	 * @param fromRemoteServer
	 *            If the BAM file comes from remote server,
	 *            fromRemoteServer=true, else false.
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public BAMReader(String filePath, String indexFilePath,
			boolean fromRemoteServer) throws MalformedURLException,
			URISyntaxException {
		this.filePath = filePath;
		this.indexFilePath = indexFilePath;
		this.fromRemoteServer = fromRemoteServer;
	}

	/**
	 * if the index file does't exist, throw FileNotFoundException
	 * 
	 * @param filePath
	 *            file path or url of the BAM file.
	 * @param indexFilePath
	 *            Temp path to store the index file. Influential only when BAM
	 *            file is from remote server.
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws FileNotFoundException
	 */
	public BAMReader(String filePath) throws MalformedURLException,
			URISyntaxException, FileNotFoundException {
		this.filePath = filePath;
		if (filePath.startsWith("http://") || filePath.startsWith("ftp://")
				|| filePath.startsWith("https://")) {
			this.fromRemoteServer = true;

			if (isRemoteFileExists(filePath + ".bai"))
				this.indexFilePath = filePath + ".bai";
			else if (BAMReader.isRemoteFileExists(filePath.replace(".bam",
					".bai")))
				this.indexFilePath = filePath.replace(".bam", ".bai");
			else
				throw new FileNotFoundException();
		} else {
			this.fromRemoteServer = false;
			if (new File(filePath + ".bai").exists())
				this.indexFilePath = filePath + ".bai";
			else if (new File(filePath.replace(".bam", ".bai")).exists())
				this.indexFilePath = filePath.replace(".bam", ".bai");
			else
				throw new FileNotFoundException();
		}
	}

	/**
	 * Judge remote file exists
	 */
	public static boolean isRemoteFileExists(String url) {
		URL serverUrl;
		try {
			serverUrl = new URL(url);
			HttpURLConnection urlcon = (HttpURLConnection) serverUrl
					.openConnection();

			String message = urlcon.getHeaderField(0);
			if (message != null && message.startsWith("HTTP/1.1 200 OK")) {
				return true;
			}
			return false;
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * get reads overlaps the given region.
	 * 
	 * @param doc
	 *            Document object
	 * @param chr
	 *            chromosome name
	 * @param start
	 *            start base of the given region in the chromosome. 1-base
	 * @param end
	 *            end base of the given region in the chromosome. 1-base
	 * @param step
	 *            A step defines how many pixes show a grid.
	 * @param windowSize
	 *            window size, in pixel. Make sure that size Can be divided by 2
	 * @param track
	 * @param mode
	 *            <pre>
	 * 1:detail
	 * 2:NotDetailSmall
	 * 3:NotDetailBig
	 * @throws IOException
	 */
	public void readBAM(Document doc, String chr, int start, int end,
			int windowSize, int step, String mode, String track)
			throws IOException {
		double bpp = 0;
		boolean already = false;

		bpp = (end - start + 1) / (double) windowSize;

		if (bpp <= 25) {
			List<SAMRecord> list = readSmallRegion(chr, start, end);

			// has more than 20000 reads
			if (list == null) {
				bpp = 1024;
			} else {
				if (mode.equalsIgnoreCase(MODE_DETAIL)) {
					// output all field to the XML file
					writeDetail(doc, list, track);
				} else {
					if (bpp <= 0.5) {
						writeNotDetail(doc, list, track, mode, true);
					} else {
						writeNotDetail(doc, list, track, mode, false);
					}
				}
				already = true;
			}
		}
		// middle region
		if (bpp < 8 * 1024 && !already) {
			int[] result = readMiddleRegion(chr, start, end, windowSize, step);
			writeBigRegion(doc, start, end, step, result, track);
			already = true;
		}
		// big region
		if (bpp >= 8 * 1024 && !already) {
			int[] result = readBigRegion(chr, start, end, windowSize, step);
			writeBigRegion(doc, start, end, step, result, track);
		}
	}

	private void writeDetail(Document doc, List<SAMRecord> list, String track) {
		Element reads = doc.createElement(XML_TAG_READS);
		reads.setAttribute(XML_TAG_ID, track);

		Iterator<SAMRecord> itor = list.iterator();
		while (itor.hasNext()) {
			SAMRecord rec = itor.next();
			Element read = doc.createElement(XML_TAG_READ);
			read.setAttribute(XML_TAG_ID, rec.getReadName());
			XmlWriter.append_text_element(doc, read, XML_TAG_FROM,
					rec.getAlignmentStart() + "");
			XmlWriter.append_text_element(doc, read, XML_TAG_TO,
					rec.getAlignmentEnd() + "");

			XmlWriter.append_text_element(doc, read, XML_TAG_DIRECTION,
					((rec.getFlags() & 0x10) == 0x10) ? "+" : "-");
			XmlWriter.append_text_element(doc, read, "Mapq",
					rec.getMappingQuality() + "");
			XmlWriter.append_text_element(doc, read, "Cigar",
					rec.getCigarString());
			XmlWriter.append_text_element(doc, read, "Rnext",
					rec.getMateReferenceName());
			XmlWriter.append_text_element(doc, read, "Pnext",
					rec.getMateAlignmentStart() + "");
			XmlWriter.append_text_element(doc, read, "Tlen",
					rec.getInferredInsertSize() + "");
			XmlWriter
					.append_text_element(doc, read, "Seq", rec.getReadString());
			XmlWriter.append_text_element(doc, read, "Qual",
					rec.getBaseQualityString());

			StringBuilder remain = new StringBuilder();
			List<SAMTagAndValue> l = rec.getAttributes();
			for (int i = 0; i < l.size(); i++) {
				remain.append(l.get(i).tag + ":" + l.get(i).value);
				if (i < l.size() - 1)
					remain.append(";");
			}

			XmlWriter.append_text_element(doc, read, XML_TAG_DESCRIPTION,
					remain.toString());

			reads.appendChild(read);
		}
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(reads);
	}

	/**
	 * 
	 * @param doc
	 * @param list
	 * @param track
	 * @param mode
	 * @param lt0point5
	 *            True if bpp less than 0.5, false else
	 */
	private void writeNotDetail(Document doc, List<SAMRecord> list,
			String track, String mode, boolean lt0point5) {
		Element reads = doc.createElement(XML_TAG_READS);

		reads.setAttribute(XML_TAG_ID, track);

		Iterator<SAMRecord> itor = list.iterator();
		while (itor.hasNext()) {
			SAMRecord rec = itor.next();
			Element read = doc.createElement(XML_TAG_READ);

			if (mode.equalsIgnoreCase(MODE_FULL)
					|| mode.equalsIgnoreCase(MODE_PACK))
				read.setAttribute(XML_TAG_ID, rec.getReadName());

			XmlWriter.append_text_element(doc, read, XML_TAG_FROM,
					rec.getAlignmentStart() + "");
			XmlWriter.append_text_element(doc, read, XML_TAG_TO,
					rec.getAlignmentEnd() + "");
			XmlWriter.append_text_element(doc, read, XML_TAG_DIRECTION,
					((rec.getFlags() & 0x10) == 0x10) ? "+" : "-");
			XmlWriter.append_text_element(doc, read, "Mapq",
					rec.getMappingQuality() + "");

			if (lt0point5) {
				addVariant(doc, read, rec);

				if (rec.getAttribute("MD") != null) {
					String MD = (String) rec.getAttribute("MD");

					// <pos, replacedStr>
					SortedMap<Integer, String> snv = new TreeMap<Integer, String>();
					int MDpos = 1;
					String str = "";
					for (int i = 0; i < MD.length(); i++) {
						char c = MD.charAt(i);
						// read a digit
						if (c >= '0' && c <= '9') {// match
							str += c;
						} else if (c == '^') {// DEL
							// read number finish
							MDpos += Integer.parseInt(str);
							str = "";
							for (int j = i + 1; j < MD.length(); j++) {
								if (MD.charAt(j) >= '0' && MD.charAt(j) <= '9')
									break;
								i++;
								MDpos++;
							}
						} else {// SNV
							// read number finish
							MDpos += Integer.parseInt(str);
							str = "";
							if (snv.size() == 0
									|| snv.lastKey().intValue()
											+ snv.get(snv.lastKey()).length() != MDpos) {
								snv.put(new Integer(MDpos), c + "");
							} else {
								snv.put(snv.lastKey(), snv.get(snv.lastKey())
										+ c);
							}
							MDpos++;
						}
					}

					Set<Integer> set = snv.keySet();
					Iterator<Integer> it = set.iterator();
					while (it.hasNext()) {
						Integer next = it.next();
						String s = snv.get(next);
						Element ele = doc.createElement(XML_TAG_VARIANT);
						ele.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_SNV);
						XmlWriter.append_text_element(doc, ele, XML_TAG_FROM,
								"" + next.intValue());
						XmlWriter.append_text_element(doc, ele, XML_TAG_TO, ""
								+ (next.intValue() + s.length() - 1));
						XmlWriter.append_text_element(doc, ele, XML_TAG_LETTER,
								s);
						read.appendChild(ele);
					}
				}
			}
			reads.appendChild(read);
		}
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(reads);
	}

	private void writeBigRegion(Document doc, int start, int end, int step,
			int[] list, String track) {
		Element values = doc.createElement(XML_TAG_VALUES);
		values.setAttribute(XML_TAG_ID, track);
		values.setAttribute(XML_TAG_TYPE, "REN");

		XmlWriter.append_text_element(doc, values, XML_TAG_FROM, start + "");
		XmlWriter.append_text_element(doc, values, XML_TAG_TO, end + "");
		XmlWriter.append_text_element(doc, values, XML_TAG_STEP, step + "");

		StringBuilder vl = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
			vl.append(list[i]);
			if (i < list.length - 1)
				vl.append(";");
		}
		XmlWriter.append_text_element(doc, values, "ValueList", vl.toString());

		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(values);
	}

	private void addVariant(Document doc, Element read, SAMRecord record) {
		List<CigarElement> li = record.getCigar().getCigarElements();

		Element ele = doc.createElement(XML_TAG_VARIANT);

		boolean hasException = false;
		int pos = 1;
		for (CigarElement ce : li) {
			String name = ce.getOperator().name();
			if (name.equals("D")) {
				ele.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_DELETION);
				XmlWriter.append_text_element(doc, ele, XML_TAG_FROM, "" + pos);
				XmlWriter.append_text_element(doc, ele, XML_TAG_TO, ""
						+ (pos + ce.getLength() - 1));
				hasException = true;
			} else if (name.equals("I")) {
				ele.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_INSERTION);
				XmlWriter.append_text_element(doc, ele, XML_TAG_FROM, ""
						+ (pos - 1));
				XmlWriter.append_text_element(
						doc,
						ele,
						XML_TAG_LETTER,
						record.getReadString().substring(pos - 1,
								pos + ce.getLength() - 1));
				hasException = true;
			}
			pos += ce.getLength();
		}

		if (hasException)
			read.appendChild(ele);
	}

	/**
	 * return all reads overlaps in the given region. If reads more than 20000,
	 * return null. Remember that if you want to read all reads in the list,
	 * don't use the method: get(index) because it will reduce the efficiency.
	 * Just get an iterator from the list.
	 * 
	 * @param chr
	 *            name of chromosome
	 * @param start
	 *            start base int the chromosome. 1-base
	 * @param end
	 *            end base int the chromosome. 1-base
	 * @return
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	private List<SAMRecord> readSmallRegion(String chr, int start, int end)
			throws MalformedURLException, FileNotFoundException {
		SAMRecordIterator itor = getIterator(chr, start, end);

		if (itor == null) {
			close();
			return new LinkedList<SAMRecord>();
		}

		List<SAMRecord> list = new LinkedList<SAMRecord>();

		int num = 0;
		while (itor.hasNext()) {
			num++;
			if (num > 20000) {
				list = null;
				break;
			}
			list.add(itor.next());
		}

		close();

		if (null == list)
			return null;

		return list;
	}

	/**
	 * lack exception
	 * 
	 * @param chr
	 *            name of chromosome
	 * @param start
	 *            start base int the chromosome. 1-base
	 * @param end
	 *            end base int the chromosome. 1-base
	 * @param windowSize
	 *            size of the browser in pixel
	 * @param step
	 *            A step defines how many pixes show a grid.
	 * @return
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	private int[] readMiddleRegion(String chr, int start, int end,
			int windowSize, int step) throws MalformedURLException,
			FileNotFoundException {

		SAMRecordIterator itor = getIterator(chr, start, end);
		int harfWindowSize = windowSize / 2;

		double smallRegionWidth = 0;
		int[] starts = null;

		smallRegionWidth = (end - start + 1) / (windowSize / ((double) step));
		starts = new int[(harfWindowSize) + 1];
		for (int i = 0; i <= harfWindowSize; i++)
			starts[i] = (int) Math.round(i * smallRegionWidth);
		starts[harfWindowSize] = end - start + 1;

		// result in double
		double[] regions = new double[harfWindowSize];

		for (int i = 0; i < harfWindowSize; i++)
			regions[i] = 0;

		int blockIndex = 0;
		while (itor.hasNext()) {
			SAMRecord read = itor.next();
			while (read.getAlignmentStart() - start > starts[blockIndex + 1])
				blockIndex++;

			int overlapNum = 1;
			int temppos = blockIndex;
			while (starts[temppos + 1] < read.getAlignmentEnd() - start
					&& temppos + 1 < harfWindowSize) {
				overlapNum++;
				temppos++;
			}
			for (int i = blockIndex; i < blockIndex + overlapNum; i++)
				regions[i] += 1.0 / overlapNum;

		}
		close();

		int[] num = new int[harfWindowSize];
		for (int i = 0; i < harfWindowSize; i++)
			num[i] = (int) regions[i];

		return num;
	}

	/**
	 * @param chr
	 *            name of chromosome
	 * @param start
	 *            start base. 1-base
	 * @param end
	 *            end base. 1-base
	 * @param windowSize
	 * @param step
	 *            A step defines how many pixes show a grid.
	 * 
	 * @return
	 */
	private int[] readBigRegion(String chr, int start, int end, int windowSize,
			int step) throws IOException {
		open(false);
		/*
		 * index of the given chromosome in BAM file order
		 */
		int refIndex = samReader.getFileHeader().getSequenceIndex(chr);
		readReference(refIndex);

		InputStream is1 = getInputStream(indexFilePath);
		readLinearIndex(is1);
		is1.close();

		int startLinear = LinearIndex.convertToLinearIndexOffset(start);
		int endLinear = LinearIndex.convertToLinearIndexOffset(end);

		int nLinearBins = endLinear - startLinear + 1;

		long[] pos = new long[nLinearBins + 1];

		for (int i = 0; i <= nLinearBins; i++)
			pos[i] = 0;

		InputStream is2 = getInputStream(indexFilePath);

		location(refIndex, is2, startLinear);

		for (int i = 0; i < nLinearBins; i++) {
			pos[i] = TabixReader.readLong(is2) >> 16;
		}
		// The last linear index of the given region may be the last linear
		// index of the chromosome,
		// so we must find the first linear index which != 0 from the next
		// chromosomes backward.
		if (endLinear + 1 == chrLinearBinNum[refIndex]) {
			for (int i = refIndex + 1; i <= sequenceNum; i++) {
				if (chrFileSize[i] != 0) {
					pos[nLinearBins] = chrFileSize[i];
					break;
				}
			}
		} else {
			pos[nLinearBins] = TabixReader.readLong(is2) >> 16;
		}
		is2.close();

		int[] regions = new int[nLinearBins];
		for (int i = 0; i < nLinearBins; i++)
			regions[i] = 0;

		// If the first linear index equals 0, we must find the first linear
		// index which != 0 from the previous
		// chromosomes forward.
		if (pos[0] == 0) {
			long l = 0;
			for (int i = refIndex - 1; i >= 0; i++) {
				if (chrLastLinearSpan[i] != 0) {
					l = chrLastLinearSpan[i];
					break;
				}
			}
			for (int i = 0; pos[i] == 0 && i < pos.length - 1; i++)
				pos[i] = l;
		}

		long refSpan = chrFileSize[refIndex + 1] - chrFileSize[refIndex];
		boolean firstZero = true;
		int firstZeroIndex = 0;

		for (int i = 1; i < nLinearBins; i++) {
			if (pos[i - 1] == pos[i]) {
				if (firstZero) {
					firstZeroIndex = i;
					firstZero = false;
				}
				regions[i] = 0;
			} else {
				if (!firstZero) {
					regions[firstZeroIndex - 1] = (int) (((pos[i] - pos[i - 1]) * chrReadNum) / refSpan);
					firstZero = true;
				}
				regions[i] = (int) (((pos[i + 1] - pos[i]) * chrReadNum) / refSpan);
			}
		}

		close();

		return getSpan(start % SIXTEENK, (end - start + 1)
				/ (windowSize / ((double) step)), windowSize / step, regions);
	}

	/**
	 * 
	 * @param startTh
	 *            startTh = start%16K;
	 * @param width
	 *            width=(end - start + 1)/(windowSize/2.0);
	 * @param size
	 *            size=windowSize/2;
	 * @param regions
	 * @return
	 */
	private int[] getSpan(int startTh, double width, int size, int[] regions) {
		double[] result = new double[size];
		for (int i = 0; i < size; i++)
			result[i] = 0;

		int bin = 0;
		double curpos = startTh;

		for (int i = 0; i < size; i++) {
			boolean b = true;
			double endpos = startTh + (i + 1) * width - 1;
			while (b) {
				double len = 0;
				double binEndPos = (bin + 1) * SIXTEENK;
				if (endpos > binEndPos) {
					len = binEndPos - curpos;
					result[i] += (len / SIXTEENK) * regions[bin];
					curpos = binEndPos;
					bin++;
				} else {
					len = endpos - curpos;
					result[i] += (len / SIXTEENK) * regions[bin];
					curpos = endpos;
					b = false;
				}
			}
		}

		int[] res = new int[size];
		for (int i = 0; i < size; i++)
			res[i] = (int) result[i];

		return res;
	}

	private void location(int refIndex, InputStream is, int start)
			throws IOException {
		is.skip(8);

		for (int i = 0; i < refIndex; i++) {
			final int nBins = TabixReader.readInt(is);
			for (int j = 0; j < nBins; j++) {
				is.skip(4);
				final int nChunks = TabixReader.readInt(is);
				is.skip(16 * nChunks);
			}
			final int nLinearBins = TabixReader.readInt(is);
			is.skip(8 * nLinearBins);
		}

		final int nBins = TabixReader.readInt(is);
		for (int j = 0; j < nBins; j++) {
			is.skip(4);
			final int nChunks = TabixReader.readInt(is);
			is.skip(16 * nChunks);
		}

		is.skip(4 + 8 * start);
	}

	private void readReference(int refIndex) {
		AbstractBAMFileIndex index = (AbstractBAMFileIndex) samReader
				.getIndex();
		sequenceNum = index.getNumberOfReferences();
		chrLinearBinNum = new int[sequenceNum];
		chrLastLinearSpan = new long[sequenceNum];
		chrFileSize = new long[sequenceNum + 1];

		// read num of given chromosome
		chrReadNum = index.getMetaData(refIndex).getAlignedRecordCount();
	}

	private void readLinearIndex(InputStream is) throws IOException {
		is.skip(8);

		for (int i = 0; i < sequenceNum; i++) {
			final int nBins = TabixReader.readInt(is);
			for (int j = 0; j < nBins; j++) {
				is.skip(4);
				final int nChunks = TabixReader.readInt(is);
				is.skip(16 * nChunks);
			}

			final int nLinearBins = TabixReader.readInt(is);
			chrLinearBinNum[i] = nLinearBins;

			int k = 0;
			for (k = 1; k <= nLinearBins; k++) {
				chrFileSize[i] = TabixReader.readLong(is) >> 16;
				if (chrFileSize[i] != 0) {
					break;
				}
			}
			if (k < nLinearBins) {
				is.skip(8 * (nLinearBins - k - 1));
				chrLastLinearSpan[i] = TabixReader.readLong(is) >> 16;
			} else {
				chrLastLinearSpan[i] = chrFileSize[i];
			}
		}

		chrFileSize[sequenceNum] = samReader.getIndex()
				.getStartOfLastLinearBin();
	}

	private SAMRecordIterator getIterator(String chr, int start, int end)
			throws MalformedURLException, FileNotFoundException {
		try {
			open(false);

			SAMRecordIterator samRecItor = samReader.queryOverlapping(chr,
					start, end);

			return samRecItor;
		} catch (SAMException e) {
			return null;
		}
	}

	private void open(boolean b) throws MalformedURLException,
			FileNotFoundException {
		if (fromRemoteServer)
			samReader = new SAMFileReader(new SeekableBufferedStream(
					new SeekableHTTPStream(new URL(filePath))),
					new SeekableBufferedStream(new SeekableHTTPStream(new URL(
							indexFilePath))), b);
		else
			samReader = new SAMFileReader(new File(filePath), new File(
					indexFilePath), b);
	}

	private void close() {
		if (samReader != null) {
			samReader.close();
			samReader = null;
		}
	}

	private InputStream getInputStream(String path)
			throws MalformedURLException, FileNotFoundException {
		if (fromRemoteServer)
			return new SeekableBufferedStream(new SeekableHTTPStream(new URL(
					path)));
		else
			return new FileInputStream(new File(path));
	}
}
