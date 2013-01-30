package FileReaders;

import java.io.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.hit.mlg.Tools.StringSplit;

/**
 * This is for reading BED files. We don't interpret the specific content in
 * this level, because a lot of genomic data can be stored in BED formats. BED
 * itself is display-oriented. BED is also a typical tab-delimited file format,
 * which can be compressed, indexed and read by Tabix API. But we consider BED
 * as a light-weight up-to-2-level genomic region representation format here,
 * and believe it is convenient if we keep BED as Text-based format, not
 * compressed binary format, and thus we can tolerant unsorted or unindexed
 * files to display. For this purpose, we read the whole file into the memory
 * and traverse all of the records, to find the ones should be displayed in the
 * current window when a query is performed.
 */

class BedReader {
	String beds[];
	String bedPath;

	BedReader(String bed) {
		this.bedPath = bed;
	}

	private boolean overlaps(long region_start, long region_end,
			long chr_start, long chr_end) {
		if (chr_start >= region_start && chr_start <= region_end)
			return true;
		if (chr_end >= region_start && chr_end <= region_end)
			return true;
		if (chr_start <= region_start && chr_end >= region_end)
			return true;

		return false;
	}
	Element get_detail(Document doc, String track, String id, String chr, long regionstart, long regionend) throws IOException{
		Element Elements = doc.createElement(Consts.XML_TAG_ELEMENTS);
		Elements.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Elements); // Elements
		Element Ele = null;
		BufferedReader br = new BufferedReader(new FileReader(bedPath));
		String line = null;
		StringSplit split = new StringSplit('\t');
		while ((line = br.readLine()) != null) {
			if (line.startsWith(chr + "\t")) {
				split.split(line);
				if (regionstart==Integer.parseInt(split.getResultByIndex(1))+1&&regionend==Integer.parseInt(split.getResultByIndex(2))&&split.getResultByIndex(3).equals(id)) {
					Ele = doc.createElement(Consts.XML_TAG_ELEMENT);
					Bed bed=new Bed(split.getResult(), split.getResultNum());
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
		}
		br.close();
		
		return Elements;
	}
	Element write_bed2elements(Document doc, String track, String chr,
			long regionstart, long regionend, double bpp)
			throws NumberFormatException, IOException {
		Element Elements = doc.createElement(Consts.XML_TAG_ELEMENTS);
		Elements.setAttribute(Consts.XML_TAG_ID, track);
		// node
		Element Ele = null;

		BufferedReader br = new BufferedReader(new FileReader(bedPath));
		String line = null;
		StringSplit split = new StringSplit('\t');
		while ((line = br.readLine()) != null) {
			if (line.startsWith(chr + "\t")) {
				split.split(line);
				if (overlaps(regionstart, regionend,
						Integer.parseInt(split.getResultByIndex(1)),
						Integer.parseInt(split.getResultByIndex(2)))) {
					Ele = doc.createElement(Consts.XML_TAG_ELEMENT);
					append2Element(doc, regionstart, regionend, bpp, Ele,
							new Bed(split.getResult(), split.getResultNum()));
					Elements.appendChild(Ele);
				}
			}
		}
		br.close();

		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Elements);
		return Elements;
	}

	static void append2Element(Document doc, long regionstart, long regionend,
			double bpp, Element Ele, Bed bed) {
		XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_FROM,
				Integer.toString(bed.chromStart + 1, 10));
		XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_TO,
				Integer.toString(bed.chromEnd, 10));

		if (bed.fields > 3) 
			Ele.setAttribute(Consts.XML_TAG_ID, bed.name);
		if (bed.fields > 4) 
			if (bed.itemRgb != null	&& !bed.itemRgb.ToString().equals("0,0,0"))
				XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_COLOR, bed.itemRgb.ToString());
			else if (bed.score > 0)
				XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_COLOR, new Rgb(bed.score).ToString());
		if (bed.fields > 5) 
			XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_DIRECTION, bed.strand);
		if (bed.fields > 11	&& (bed.chromEnd - bed.chromStart) / bpp > bed.blockCount * 2) {
			for (int j = 0; j < bed.blockCount; j++) {
				long substart = bed.blockStarts[j] + bed.chromStart;
				long subend = bed.blockStarts[j] + bed.chromStart + bed.blockSizes[j];
				if (substart < regionend && subend >= regionstart) 
					deal_thick(doc, Ele, substart, subend, bed.thickStart, bed.thickEnd);
				if (j < bed.blockCount - 1 && subend < regionend && (bed.blockStarts[j + 1] + bed.chromStart) >= regionstart) 
					append_subele(doc, Ele,	Long.toString(subend + 1, 10), Long.toString(bed.blockStarts[j + 1]	+ bed.chromStart, 10), Consts.SUBELEMENT_TYPE_LINE);
			}
		} 
		else if (bed.fields > 7 && (bed.chromEnd - bed.chromStart) / bpp > bed.blockCount * 2) 
			deal_thick(doc, Ele, bed.chromStart, bed.chromEnd, bed.thickStart, bed.thickEnd);
	}

	static void deal_thick(Document doc, Element Ele, long substart,
			long subend, long thickStart, long thickEnd) {
		if (subend <= thickStart || substart >= thickEnd) {
			append_subele(doc, Ele, Long.toString(substart + 1, 10),
					Long.toString(subend, 10), Consts.SUBELEMENT_TYPE_BAND);
		} else if (substart >= thickStart && subend <= thickEnd) {
			append_subele(doc, Ele, Long.toString(substart + 1, 10),
					Long.toString(subend, 10), Consts.SUBELEMENT_TYPE_BOX);
		} else if (substart < thickStart && subend > thickEnd) {
			append_subele(doc, Ele, Long.toString(substart + 1, 10),
					Long.toString(thickStart, 10), Consts.SUBELEMENT_TYPE_BAND);
			append_subele(doc, Ele, Long.toString(thickStart + 1, 10),
					Long.toString(thickEnd), Consts.SUBELEMENT_TYPE_BOX);
			append_subele(doc, Ele, Long.toString(thickEnd + 1, 10),
					Long.toString(subend, 10), Consts.SUBELEMENT_TYPE_BAND);
		} else if (substart < thickStart && subend > thickStart) {
			append_subele(doc, Ele, Long.toString(substart + 1, 10),
					Long.toString(thickStart), Consts.SUBELEMENT_TYPE_BAND);
			append_subele(doc, Ele, Long.toString(thickStart + 1, 10),
					Long.toString(subend, 10), Consts.SUBELEMENT_TYPE_BOX);
		} else if (substart < thickEnd && subend > thickEnd) {
			append_subele(doc, Ele, Long.toString(substart + 1, 10),
					Long.toString(thickEnd, 10), Consts.SUBELEMENT_TYPE_BOX);
			append_subele(doc, Ele, Long.toString(thickEnd + 1, 10),
					Long.toString(subend, 10), Consts.SUBELEMENT_TYPE_BAND);
		}
	}

	static void append_subele(Document doc, Element Ele, String from,
			String to, String type) {
		Element subele = doc.createElement(Consts.XML_TAG_SUBELEMENT);
		XmlWriter.append_text_element(doc, subele, Consts.XML_TAG_FROM, from);
		XmlWriter.append_text_element(doc, subele, Consts.XML_TAG_TO, to);
		subele.setAttribute(Consts.XML_TAG_TYPE, type);
		Ele.appendChild(subele);
	}
}