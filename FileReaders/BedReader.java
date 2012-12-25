package FileReaders;

import java.util.*;
import java.io.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

class BedReader implements Consts {
	String beds[];
	String bedPath;

	BedReader(String bed) {
		File bed_file = new File(bed);
		ByteBufferChannel bbc = new ByteBufferChannel(bed_file, 0,
				bed_file.length());
		String temp = bbc.ToString(DEFAULT_ENCODE);
		beds = temp.split("\n");
	}

	BedReader(String bedPath, boolean b) {
		this.bedPath = bedPath;
	}

	Bed[] extract_bed(String chr, long start, long end) {
		ArrayList<Bed> beds_internal = new ArrayList<Bed>();
		for (int i = 0; i < beds.length; i++) {
			Bed bed_temp = new Bed(beds[i].split("\t"));
			if (bed_temp.chrom.equals(chr) && bed_temp.chromStart < end
					&& bed_temp.chromEnd >= start) {
				beds_internal.add(bed_temp);
			}
		}
		return beds_internal.toArray(new Bed[beds_internal.size()]);
	}

	Bed[] extract_bed(String chr, long start, long end, boolean b)
			throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(bedPath));
		ArrayList<Bed> beds = new ArrayList<Bed>();

		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith(chr + "\t")) {
				String[] str = line.split("\t");
				if (overlaps(start, end, Long.parseLong(str[1]),
						Long.parseLong(str[2])))
					beds.add(new Bed(str));
			}
		}
		return beds.toArray(new Bed[beds.size()]);
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

	Element write_bed2elements(Document doc, String track, String chr,
			long regionstart, long regionend, double bpp) {
		Bed[] bed = extract_bed(chr, regionstart, regionend);
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

			if (bed[i].fields > 3) 
				Ele.setAttribute(XML_TAG_ID, bed[i].name);
			if (bed[i].fields > 4) 
				if (bed[i].itemRgb != null
						&& !bed[i].itemRgb.ToString().equals("0,0,0"))
					XmlWriter.append_text_element(doc, Ele,
							XML_TAG_COLOR, bed[i].itemRgb.ToString());
				else if (bed[i].score > 0)
					XmlWriter.append_text_element(doc, Ele,
							XML_TAG_COLOR,
							new Rgb(bed[i].score).ToString());
			if (bed[i].fields > 5) 
				XmlWriter.append_text_element(doc, Ele, XML_TAG_DIRECTION,
						bed[i].strand);
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
			else {
				deal_thick(doc, Ele, bed[i].chromStart, bed[i].chromEnd, bed[i].chromStart, bed[i].chromEnd);
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