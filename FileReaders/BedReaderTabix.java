package FileReaders;

import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * BedReader implemented by Tabix
 * Require sorted and indexed bed file
 */
class BedReaderTabix implements Consts {
	TabixReader bed_tb;

	BedReaderTabix(String bed) {
		try {
			bed_tb = new TabixReader(bed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Bed[] extract_bed(String chr, long start, long end) {
		StringBuffer querystr = new StringBuffer();
		querystr.append(chr);
		querystr.append(':');
		querystr.append(start);
		querystr.append('-');
		querystr.append(end);
		ArrayList<Bed> bed_internal = new ArrayList<Bed>();
		String line;
		try {
			TabixReader.Iterator Query = bed_tb.query(querystr.toString());
			while ((line = Query.next()) != null) {
				bed_internal.add(new Bed(line.split("\t")));
			}
			bed_tb.TabixReaderClose();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bed_internal.toArray(new Bed[bed_internal.size()]);
	}

	Element write_bed2elements(Document doc, String track, String chr,
			long regionstart, long regionend) {
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
								&& regionend - regionstart < 10000000) {
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
	
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Bed[] beds = new BedReaderTabix("BED/knownGene.hg19.sorted.bed.gz").extract_bed(
				"chr1", 100, 1000000);
		System.out.println("索引读取共用时：" + (System.currentTimeMillis() - start));
		System.out.println("共有：" + beds.length + "个对象");

	}
}