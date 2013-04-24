package FileReaders;

import static FileReaders.BedReader.append2Element;
import static FileReaders.BedReader.append_subele;
import static FileReaders.BedReader.deal_thick;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

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
public class BigBedReader implements Consts {
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
		reader = new BBFileReader(uri);
		
	/*	if (uri.startsWith("http://") || uri.startsWith("ftp://")
				|| uri.startsWith("https://")) {
			if (!BAMReader.isRemoteFileExists(uri))
				throw new FileNotFoundException("can't find file for:" + uri);
			SeekableStream ss = new CustomSeekableBufferedStream(
					new CustomSeekableHTTPStream(new URL(uri)));
			reader = new BBFileReader(uri, ss);
		} else {
			reader = new BBFileReader(uri);
		}*/
		// get the big header
		bbFileHdr = reader.getBBFileHeader();
	}
	Element get_detail(Document doc, String track, String id,String chr,long regionstart, long regionend ) throws IOException {
		Element Elements = doc.createElement(Consts.XML_TAG_ELEMENTS);
		Elements.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Elements); // Elements
		
		if (chr == null || !bbFileHdr.isHeaderOK() || !bbFileHdr.isBigBed())
			return Elements;
		// chromosome was specified, test if it exists in this file
		if (!(new HashSet<String>(reader.getChromosomeNames()).contains(chr)))
			return Elements;
		
		// get an iterator for BigBed features which occupy a chromosome
		// selection region.
			BigBedIterator iter = reader.getBigBedIterator(chr, (int) regionstart,
					chr, (int) regionend, false);
		
			String[] temp = new String[12];
			int fields = 0;
			Element Ele = null;
			BedFeature f = null;
			// loop over iterator
			while (iter.hasNext()) {
				f = iter.next();
				fields = 0;
				temp[fields++] = f.getChromosome();
				temp[fields++] = f.getStartBase() + "";
				temp[fields++] = f.getEndBase() + "";
				for (String str : f.getRestOfFields())
					temp[fields++] = str;
		
				if (regionstart==f.getStartBase()+1&&regionend==f.getEndBase()&&temp[3].equals(id)) {
					Ele = doc.createElement(Consts.XML_TAG_ELEMENT);
					Bed bed=new Bed(temp,fields);
					XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_FROM,Integer.toString(bed.chromStart + 1, 10));
					XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_TO,Integer.toString(bed.chromEnd, 10));
				if (bed.fields > 3)
					Ele.setAttribute(Consts.XML_TAG_ID, bed.name);
				if (bed.fields > 4)
					if (bed.itemRgb != null	&& !bed.itemRgb.ToString().equals("0,0,0"))
						XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_COLOR, bed.itemRgb.ToString());
					else if (bed.score > 0)
						XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_COLOR, new Rgb(bed.score).ToString());
				if (bed.fields > 5)
					XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_DIRECTION, bed.strand);
				if (bed.fields > 11) {
					for (int j = 0; j < bed.blockCount; j++) {
						long substart = bed.blockStarts[j] + bed.chromStart;
						long subend = bed.blockStarts[j] + bed.chromStart + bed.blockSizes[j];
						deal_thick(doc, Ele, substart, subend, bed.thickStart, bed.thickEnd);
						if (j < bed.blockCount - 1)
							append_subele(doc, Ele,	Long.toString(subend + 1, 10), Long.toString(bed.blockStarts[j + 1]	+ bed.chromStart, 10), Consts.SUBELEMENT_TYPE_LINE);
					}
				}
				else if (bed.fields > 7)
					deal_thick(doc, Ele, bed.chromStart, bed.chromEnd, bed.thickStart, bed.thickEnd);
				Elements.appendChild(Ele);
				break;
			}
		}
		return Elements;
	}
	Element write_bed2elements(Document doc, String track, String chr,
			long regionstart, long regionend, double bpp) throws IOException {
		Element Elements = doc.createElement(XML_TAG_ELEMENTS);
		Elements.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Elements); // Elements

		if (chr == null || !bbFileHdr.isHeaderOK() || !bbFileHdr.isBigBed())
			return Elements;
		// chromosome was specified, test if it exists in this file
		if (!(new HashSet<String>(reader.getChromosomeNames()).contains(chr)))
			return Elements;

		// get an iterator for BigBed features which occupy a chromosome
		// selection region.
		BigBedIterator iter = reader.getBigBedIterator(chr, (int) regionstart,
				chr, (int) regionend, false);

		String[] temp = new String[12];
		int fields = 0;
		Element Ele = null;
		BedFeature f = null;
		// loop over iterator
		while (iter.hasNext()) {
			f = iter.next();
			fields = 0;
			temp[fields++] = f.getChromosome();
			temp[fields++] = f.getStartBase() + "";
			temp[fields++] = f.getEndBase() + "";
			for (String str : f.getRestOfFields()) {
				temp[fields++] = str;
			}
			Ele = doc.createElement(XML_TAG_ELEMENT);
			append2Element(doc, regionstart, regionend, bpp, Ele, new Bed(temp,
					fields));
			Elements.appendChild(Ele);
		}

	//	close();
		
		return Elements;
	}
	
/*	private void close() {
		try {
			if (reader != null)
				this.reader.getBBFis().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
}