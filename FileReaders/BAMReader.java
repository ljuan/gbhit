package FileReaders;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.*;
import org.w3c.dom.*;
import net.sf.samtools.*;
import net.sf.samtools.SAMRecord.SAMTagAndValue;
import FileReaders.*;

/**
 * <pre>
 * usage:
 * Firstly, Get an instance of this class:
 *   public BAMReader(String filePath, String indexFilePath, boolean fromRemoteServer)
 * Secondly: insert elements into Document Object:
 * 	 readBAM(Document doc, String chr, int start, int end, int size, String mode, String track)
 * 
 * @author YanChengWu
 * 
 */
public class BAMReader {

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
	 * else. If fromRemoteServer=true, filePath is a path of a local BAM file,
	 * else an url of a remote BAM file.
	 */
	private boolean fromRemoteServer;

	private SAMFileReader samReader = null;

	private static final int SIXTEENK = 16 * 1024;;

	/**
	 * total number of reads in the BAM file
	 */
	private int totalReadNum = 0;

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
	 * total number of reads of each chromosome in the BAM file
	 */
	private int[] chrReadNum = null;

	/**
	 * total number of linear bin of each chromosome in the BAM file
	 */
	private int[] chrLinearBinNum = null;

	/**
	 * number of chromosome in the BAM file
	 */
	private int sequenceNum = 0;

	/**
	 * If it is the first time use the method to read big region,
	 * firstReadHead=true. Else false.
	 */
	private boolean firstReadHead = true;

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
	
	public BAMReader(String filePath) throws MalformedURLException, URISyntaxException {
		this.filePath=filePath;
		this.indexFilePath=filePath+".bai";
		if(filePath.startsWith("http://") || filePath.startsWith("ftp://") || filePath.startsWith("https://")){
			this.fromRemoteServer= true;
		}
		else {
			this.fromRemoteServer= false;
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
	 * @param size
	 *            window size, in pixel. Make sure that size Can be divided by 2
	 * @param track
	 * @param mode
	 *            <pre>
	 * 1:detail
	 * 2:NotDetailSmall
	 * 3:NotDetailBig
	 * @throws IOException
	 */
	public void readBAM(Document doc, String chr, int start, int end, int size,
			String mode, String track) throws IOException {
		double bpp = 0;
		boolean already = false;

		bpp = (end - start + 1) / (double) size;

		if (bpp <= 25) {
			List<SAMRecord> list = readSmallRegion(chr, start, end);

			// has more than 20000 reads
			if (list == null) {
				bpp = 1024;
			} else {
				if (mode.equalsIgnoreCase("detail")) {
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
			int[] result = readMiddleRegion(chr, start, end, size);
			writeBigRegion(doc, start, end, result, track);
			already = true;
		}
		// big region
		if (bpp >= 8 * 1024 && !already) {
			int[] result = readBigRegion(chr, start, end, size);
			writeBigRegion(doc, start, end, result, track);
		}
	}

	private void writeDetail(Document doc, List<SAMRecord> list, String track) {
		Element reads = doc.createElement("Reads");
		setA(reads, "id", track);

		Iterator<SAMRecord> itor = list.iterator();
		while (itor.hasNext()) {
			SAMRecord rec = itor.next();
			Element read = doc.createElement("Read");
			setA(read, "id", rec.getReadName()).addC(doc, read, "From",
					rec.getAlignmentStart() + "").addC(doc, read, "To",
					rec.getAlignmentEnd() + "");

			addC(doc, read, "Direction",
					((rec.getFlags() & 0x10) == 0x10) ? "+" : "-")
					.addC(doc, read, "Mapq", rec.getMappingQuality() + "")
					.addC(doc, read, "Cigar", rec.getCigarString())
					.addC(doc, read, "Rnext", rec.getMateReferenceName())
					.addC(doc, read, "Pnext", rec.getMateAlignmentStart() + "")
					.addC(doc, read, "Tlen", rec.getInferredInsertSize() + "")
					.addC(doc, read, "Seq", rec.getReadString())
					.addC(doc, read, "Qual", rec.getBaseQualityString());

			StringBuilder remain = new StringBuilder();
			List<SAMTagAndValue> l = rec.getAttributes();
			for (int i = 0; i < l.size(); i++) {
				remain.append(l.get(i).tag + ":" + l.get(i).value);
				if (i < l.size() - 1)
					remain.append(";");
			}
			addC(doc, read, "Description", remain.toString());

			reads.appendChild(read);
		}
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(reads);//�������ڵ���ڸ�Ŀ¼��
		//doc.appendChild(reads);
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
		Element reads = doc.createElement("Reads");

		setA(reads, "id", track);

		Iterator<SAMRecord> itor = list.iterator();
		while (itor.hasNext()) {
			SAMRecord rec = itor.next();
			Element read = doc.createElement("Read");

			if (mode.equalsIgnoreCase("full") || mode.equalsIgnoreCase("pack"))
				setA(read, "id", rec.getReadName());

			addC(doc, read, "From", rec.getAlignmentStart() + "").addC(doc,
					read, "To", rec.getAlignmentEnd() + "");

			addC(doc, read, "Direction",
					((rec.getFlags() & 0x10) == 0x10) ? "+" : "-").addC(doc,
					read, "Mapq", rec.getMappingQuality() + "");

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
						Element ele = doc.createElement("Variant");
						setA(ele, "Type", "SNV");
						addC(doc, ele, "From", "" + next.intValue());
						addC(doc, ele, "To", ""
								+ (next.intValue() + s.length() - 1));
						addC(doc, ele, "Letter", s);
						read.appendChild(ele);
					}
				}
			}
			reads.appendChild(read);
		}
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(reads);//�������ڵ���ڸ�Ŀ¼��
		//doc.appendChild(reads);
	}

	private void writeBigRegion(Document doc, int start, int end, int[] list,
			String track) {
		Element values = doc.createElement("Values");
		setA(values, "id", track).setA(values, "Type", "REN");

		addC(doc, values, "From", start + "").addC(doc, values, "To", end + "")
				.addC(doc, values, "Step", "2");

		StringBuilder vl = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
			vl.append(list[i]);
			if (i < list.length - 1)
				vl.append(";");
		}

		addC(doc, values, "ValueList", vl.toString());
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(values);//�������ڵ���ڸ�Ŀ¼��
		//doc.appendChild(values);
	}

	private BAMReader setA(Element e, String name, String value) {
		e.setAttribute(name, value);
		return this;
	}

	private BAMReader addC(Document doc, Element e, String name, String value) {
		Element ele = doc.createElement(name);
		Text text = doc.createTextNode(value);
		ele.appendChild(text);
		e.appendChild(ele);

		return this;
	}

	private void addVariant(Document doc, Element read, SAMRecord record) {
		List<CigarElement> li = record.getCigar().getCigarElements();

		Element ele = doc.createElement("Variant");

		boolean hasException = false;
		int pos = 1;
		for (CigarElement ce : li) {
			String name = ce.getOperator().name();
			if (name.equals("D")) {
				setA(ele, "Type", "DEL");
				addC(doc, ele, "From", "" + pos);
				addC(doc, ele, "To", "" + (pos + ce.getLength() - 1));
				hasException = true;
			} else if (name.equals("I")) {
				setA(ele, "Type", "INS");
				addC(doc, ele, "From", "" + (pos - 1));
				addC(doc,
						ele,
						"Letter",
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
	 */
	private List<SAMRecord> readSmallRegion(String chr, int start, int end)
			throws MalformedURLException {
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

		/*
		 * Iterator<SAMRecord> it = list.iterator(); while (it.hasNext()) {
		 * SAMRecord next = it.next(); if (next.getAttribute("MD") != null) {
		 * String MD = (String) next.getAttribute("MD");
		 * System.out.println("id:" + next.getReadName() + "\tMD:" + MD +
		 * "\tCigar:" + next.getCigarString() + "\tstart:" +
		 * next.getAlignmentStart()); } }
		 */

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
	 * @return
	 * @throws MalformedURLException
	 */
	private int[] readMiddleRegion(String chr, int start, int end,
			int windowSize) throws MalformedURLException {

		SAMRecordIterator itor = getIterator(chr, start, end);
		// System.out.println(itor);

		double smallRegionWidth = 0;
		int[] starts = null;

		smallRegionWidth = (end - start + 1) / (windowSize / 2.0);
		starts = new int[(windowSize / 2) + 1];
		for (int i = 0; i <= windowSize / 2; i++)
			starts[i] = (int) Math.round(i * smallRegionWidth);
		starts[windowSize / 2] = end - start + 1;

		// result in double
		double[] regions = new double[windowSize / 2];

		for (int i = 0; i < windowSize / 2; i++)
			regions[i] = 0;

		int blockIndex = 0;
		while (itor.hasNext()) {
			SAMRecord read = itor.next();
			while (read.getAlignmentStart() - start > starts[blockIndex + 1])
				blockIndex++;

			int overlapNum = 1;
			int temppos = blockIndex;
			while (starts[temppos + 1] < read.getAlignmentEnd() - start
					&& temppos + 1 < windowSize / 2) {
				overlapNum++;
				temppos++;
			}
			for (int i = blockIndex; i < blockIndex + overlapNum; i++)
				regions[i] += 1.0 / overlapNum;

		}
		close();

		int[] num = new int[windowSize / 2];
		for (int i = 0; i < windowSize / 2; i++)
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
	 * 
	 * @return
	 */
	private int[] readBigRegion(String chr, int start, int end, int windowSize)
			throws IOException {

		open(false);

		FileInputStream fileStream = new FileInputStream(indexFilePath);
		FileChannel fileChannel = fileStream.getChannel();
		MappedByteBuffer mbBuffer = fileChannel.map(
				FileChannel.MapMode.READ_ONLY, 0L, fileChannel.size());
		mbBuffer.order(ByteOrder.LITTLE_ENDIAN);
		fileChannel.close();
		fileStream.close();

		if (firstReadHead) {
			readReference();
			readLinearIndex(mbBuffer);
			firstReadHead = false;
		}
		int startLinear = LinearIndex.convertToLinearIndexOffset(start);
		int endLinear = LinearIndex.convertToLinearIndexOffset(end);

		/*
		 * System.out.println("start:" + start + ", end:" + end +
		 * ", startLinear:" + startLinear + ", endLinear:" + endLinear);
		 */

		int nLinearBins = endLinear - startLinear + 1;

		long[] pos = new long[nLinearBins + 1];

		for (int i = 0; i <= nLinearBins; i++)
			pos[i] = 0;

		int refIndex = samReader.getFileHeader().getSequenceIndex(chr);

		location(refIndex, mbBuffer, startLinear);

		for (int i = 0; i < nLinearBins; i++) {
			pos[i] = mbBuffer.getLong() >> 16;
		}
		// ���򸲸ǵ����һ�������������������Ⱦɫ���ϵ����һ����������.
		// ��ʱ��Ҫ��������Ⱦɫ��ֱ���ҵ���һ����0��������
		if (endLinear + 1 == chrLinearBinNum[refIndex]) {
			for (int i = refIndex + 1; i <= sequenceNum; i++) {
				if (chrFileSize[i] != 0) {
					pos[nLinearBins] = chrFileSize[i];
					break;
				}
			}
		} else {
			pos[nLinearBins] = mbBuffer.getLong() >> 16;
		}

		int[] regions = new int[nLinearBins];
		for (int i = 0; i < nLinearBins; i++)
			regions[i] = 0;

		// ����һ������������0������Ҫ��ǰ��������Ⱦɫ������һ����0��������
		// ֱ���ҵ���һ����0�����������п���һ��Ⱦɫ����û���κ�Read��
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

		/*
		 * for (int i = 0; i < pos.length; i++) System.out.println("��" + (i + 1)
		 * + "��pos��" + pos[i]);
		 */

		long refSpan = chrFileSize[refIndex + 1] - chrFileSize[refIndex];
		boolean firstZero = true;
		int firstZeroIndex = 0;

		// regions[0] = (int) (((pos[1] - pos[0]) * chrReadNum[refIndex]) /
		// refSpan);
		for (int i = 1; i < nLinearBins; i++) {
			if (pos[i - 1] == pos[i]) {
				if (firstZero) {
					firstZeroIndex = i;
					firstZero = false;
				}
				regions[i] = 0;
			} else {
				if (!firstZero) {
					regions[firstZeroIndex - 1] = (int) (((pos[i] - pos[i - 1]) * chrReadNum[refIndex]) / refSpan);
					firstZero = true;
				}
				regions[i] = (int) (((pos[i + 1] - pos[i]) * chrReadNum[refIndex]) / refSpan);
			}
		}

		/*
		 * for (int i = 0; i < regions.length; i++) System.out.println((i + 1) +
		 * ":" + regions[i]);
		 */

		return getSpan(start % SIXTEENK,
				(end - start + 1) / (windowSize / 2.0), windowSize / 2, regions);
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

	private void location(int refIndex, MappedByteBuffer mbBuffer, int start) {
		mbBuffer.position(8);

		// System.out.println("refIndex:" + refIndex);
		for (int i = 0; i < refIndex; i++) {
			final int nBins = mbBuffer.getInt();
			for (int j = 0; j < nBins; j++) {
				mbBuffer.getInt();
				final int nChunks = mbBuffer.getInt();
				mbBuffer.position(mbBuffer.position() + 16 * nChunks);
			}
			final int nLinearBins = mbBuffer.getInt();
			mbBuffer.position(mbBuffer.position() + 8 * nLinearBins);
		}

		final int nBins = mbBuffer.getInt();
		for (int j = 0; j < nBins; j++) {
			mbBuffer.getInt();
			final int nChunks = mbBuffer.getInt();
			mbBuffer.position(mbBuffer.position() + 16 * nChunks);
		}

		mbBuffer.position(mbBuffer.position() + 4 + 8 * start);
	}

	private void readReference() {
		AbstractBAMFileIndex index = (AbstractBAMFileIndex) samReader
				.getIndex();

		sequenceNum = index.getNumberOfReferences();
		chrReadNum = new int[sequenceNum];
		chrLinearBinNum = new int[sequenceNum];
		chrLastLinearSpan = new long[sequenceNum];
		chrFileSize = new long[sequenceNum + 1];

		totalReadNum = 0;

		for (int i = 0; i < sequenceNum; i++) {
			chrReadNum[i] = index.getMetaData(i).getAlignedRecordCount();
			totalReadNum += chrReadNum[i];
		}
	}

	private void readLinearIndex(MappedByteBuffer mbBuffer)
			throws MalformedURLException {
		mbBuffer.position(8);

		for (int i = 0; i < sequenceNum; i++) {
			final int nBins = mbBuffer.getInt();
			for (int j = 0; j < nBins; j++) {
				mbBuffer.getInt();
				final int nChunks = mbBuffer.getInt();
				mbBuffer.position(mbBuffer.position() + 16 * nChunks);
			}

			final int nLinearBins = mbBuffer.getInt();
			chrLinearBinNum[i] = nLinearBins;

			int k = 0;
			for (k = 1; k <= nLinearBins; k++) {
				chrFileSize[i] = mbBuffer.getLong() >> 16;
				if (chrFileSize[i] != 0)
					break;
			}
			if (k < nLinearBins) {
				mbBuffer.position(mbBuffer.position() + 8
						* (nLinearBins - k - 1));
				chrLastLinearSpan[i] = mbBuffer.getLong() >> 16;
			} else {
				chrLastLinearSpan[i] = chrFileSize[i];
			}
		}

		chrFileSize[sequenceNum] = samReader.getIndex()
				.getStartOfLastLinearBin();
	}

	private SAMRecordIterator getIterator(String chr, int start, int end)
			throws MalformedURLException {
		try {
			open(false);
			// samReader.enableFileSource(true);

			SAMRecordIterator samRecItor = samReader.queryOverlapping(chr,
					start, end);

			return samRecItor;
		} catch (SAMException e) {
			// System.out.println(e.getMessage());
			return null;
		}
	}

	private void open(boolean b) throws MalformedURLException {
		if (fromRemoteServer)
			samReader = new SAMFileReader(new URL(filePath), new File(
					indexFilePath), b);
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
}
