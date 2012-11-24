package FileReaders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.broad.igv.bbfile.BBFileHeader;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BedFeature;
import org.broad.igv.bbfile.BigBedIterator;
import org.broad.tribble.util.SeekableStream;

import FileReaders.wiggle.CustomSeekableBufferedStream;
import FileReaders.wiggle.CustomSeekableHTTPStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Get Bed lines from bigBed.
 * 
 * <pre>
 * Usage:
 * new BigBedReader(String uri).getBigBed(String chrom, 
 * int start, int end, boolean contained)
 * @author Chengwu Yan
 * revised by Liran Juan
 * 
 */
public class BigBedReader implements Consts{
	// open big file
	private BBFileReader reader;
	// get the big header
	private BBFileHeader bbFileHdr;

	/**
	 * 
	 * @param uri
	 *            url or file path
	 * @throws IOException
	 */
	public BigBedReader(String uri) throws IOException {
		// open big file
		if (uri.startsWith("http://") || uri.startsWith("ftp://")
				|| uri.startsWith("https://")) {
			if (!BAMReader.isRemoteFileExists(uri))
				throw new FileNotFoundException("can't find file for:" + uri);
			SeekableStream ss = new CustomSeekableBufferedStream(
					new CustomSeekableHTTPStream(new URL(uri)));
			reader = new BBFileReader(uri, ss);
		} else {
			reader = new BBFileReader(uri);
		}
		// get the big header
		bbFileHdr = reader.getBBFileHeader();
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
	 * @return A list of Bed instances.
	 * @throws IOException
	 */
	public List<Bed> getBigBed(String chrom, int start, int end,
			boolean contained) throws IOException {
		List<Bed> bigBeds = new ArrayList<Bed>();

		if (chrom == null || !bbFileHdr.isHeaderOK() || !bbFileHdr.isBigBed())
			return bigBeds;
		// chromosome was specified, test if it exists in this file
		if (!(new HashSet<String>(reader.getChromosomeNames()).contains(chrom)))
			return bigBeds;

		// get an iterator for BigBed features which occupy a chromosome
		// selection region.
		BigBedIterator iter = reader.getBigBedIterator(chrom, start, chrom,
				end, contained);

		// loop over iterator
		while (iter.hasNext()) {
			BedFeature f = iter.next();
			ArrayList<String> bed = new ArrayList<String>();
			bed.add(f.getChromosome());
			bed.add(f.getStartBase() + "");
			bed.add(f.getEndBase() + "");
			for (String str : f.getRestOfFields()) {
				bed.add(str);
			}
			bigBeds.add(new Bed(bed.toArray(new String[bed.size()])));
		}
		return bigBeds;
	}
	Element write_bed2elements(Document doc, String track, String chr,
			long regionstart, long regionend, double bpp) throws IOException {
		List<Bed> bed_list;
			bed_list = getBigBed(chr, (int) regionstart, (int) regionend, false);
		Bed[] bed=new Bed[bed_list.size()];
		bed_list.toArray(bed);
		Element Elements = doc.createElement(XML_TAG_ELEMENTS);
		Elements.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Elements); // Elements
																			// node

		for (int i = 0; i < bed.length; i++) {
			Element Ele = doc.createElement(XML_TAG_ELEMENT);
			XmlWriter.append_text_element(doc, Ele, XML_TAG_FROM,
					String.valueOf(bed[i].chromStart + 1));
			XmlWriter.append_text_element(doc, Ele, XML_TAG_TO,
					String.valueOf(bed[i].chromEnd));

			if (bed[i].fields > 3) {
				Ele.setAttribute(XML_TAG_ID, bed[i].name);
				if (bed[i].fields > 4) {
					XmlWriter.append_text_element(doc, Ele, XML_TAG_DIRECTION,
							bed[i].strand);
					if (bed[i].fields > 5) {
						if (bed[i].itemRgb != null
								&& !bed[i].itemRgb.ToString().equals("0,0,0"))
							XmlWriter.append_text_element(doc, Ele,
									XML_TAG_COLOR, bed[i].itemRgb.ToString());
						else if (bed[i].score > 0)
							XmlWriter.append_text_element(doc, Ele,
									XML_TAG_COLOR,
									new Rgb(bed[i].score).ToString());
						if (bed[i].fields > 11
						//		&& regionend - regionstart < 10000000) {
								&& (bed[i].chromEnd-bed[i].chromStart)/bpp > bed[i].blockCount*2) {
							for (int j = 0; j < bed[i].blockCount; j++) {
								long substart = bed[i].blockStarts[j]
										+ bed[i].chromStart;
								long subend = bed[i].blockStarts[j]
										+ bed[i].chromStart
										+ bed[i].blockSizes[j];
								if (substart < regionend
										&& subend >= regionstart) {
									deal_thick(doc, Ele, substart, subend,
											bed[i].thickStart, bed[i].thickEnd);
								}
								if (j < bed[i].blockCount - 1
										&& subend < regionend
										&& (bed[i].blockStarts[j + 1] + bed[i].chromStart) >= regionstart) {
									append_subele(
											doc,
											Ele,
											String.valueOf(subend + 1),
											String.valueOf(bed[i].blockStarts[j + 1]
													+ bed[i].chromStart),
											SUBELEMENT_TYPE_LINE);
								}
							}
						} else if (bed[i].fields > 7) {
							deal_thick(doc, Ele, bed[i].chromStart,
									bed[i].chromEnd, bed[i].thickStart,
									bed[i].thickEnd);
						}
					}
				}
			}
			Elements.appendChild(Ele);
		}
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Elements);
		return Elements;
	}

	private void deal_thick(Document doc, Element Ele, long substart,
			long subend, long thickStart, long thickEnd) {
		if (subend <= thickStart || substart >= thickEnd) {
			append_subele(doc, Ele, String.valueOf(substart + 1),
					String.valueOf(subend), SUBELEMENT_TYPE_BAND);
		} else if (substart >= thickStart && subend <= thickEnd) {
			append_subele(doc, Ele, String.valueOf(substart + 1),
					String.valueOf(subend), SUBELEMENT_TYPE_BOX);
		} else if (substart < thickStart && subend > thickEnd) {
			append_subele(doc, Ele, String.valueOf(substart + 1),
					String.valueOf(thickStart), SUBELEMENT_TYPE_BAND);
			append_subele(doc, Ele, String.valueOf(thickStart + 1),
					String.valueOf(thickEnd), SUBELEMENT_TYPE_BOX);
			append_subele(doc, Ele, String.valueOf(thickEnd + 1),
					String.valueOf(subend), SUBELEMENT_TYPE_BAND);
		} else if (substart < thickStart && subend > thickStart) {
			append_subele(doc, Ele, String.valueOf(substart + 1),
					String.valueOf(thickStart), SUBELEMENT_TYPE_BAND);
			append_subele(doc, Ele, String.valueOf(thickStart + 1),
					String.valueOf(subend), SUBELEMENT_TYPE_BOX);
		} else if (substart < thickEnd && subend > thickEnd) {
			append_subele(doc, Ele, String.valueOf(substart + 1),
					String.valueOf(thickEnd), SUBELEMENT_TYPE_BOX);
			append_subele(doc, Ele, String.valueOf(thickEnd + 1),
					String.valueOf(subend), SUBELEMENT_TYPE_BAND);
		}
	}

	private void append_subele(Document doc, Element Ele, String from,
			String to, String type) {
		Element subele = doc.createElement(XML_TAG_SUBELEMENT);
		XmlWriter.append_text_element(doc, subele, XML_TAG_FROM, from);
		XmlWriter.append_text_element(doc, subele, XML_TAG_TO, to);
		subele.setAttribute(XML_TAG_TYPE, type);
		Ele.appendChild(subele);
	}
}
