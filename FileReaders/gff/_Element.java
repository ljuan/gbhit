package FileReaders.gff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import FileReaders.Consts;
import FileReaders.XmlWriter;

/**
 * Write a group of GFF or GTF to XML, with form of Element.
 * 
 * @author Chengwu Yan
 * 
 */
public class _Element implements Consts {
	private List<GFF> gffs = null;
	private String id;
	private long from = Long.MAX_VALUE;
	private long to = Long.MIN_VALUE;
	private String direction;
	private List<SubElement> ses = new ArrayList<SubElement>();
	private boolean isGTF = false;

	_Element(String id, List<GFF> gffs, boolean isGTF) {
		this.id = id;
		this.gffs = gffs;
		this.isGTF = isGTF;
		if (this.isGTF) {
			for (GFF gff : gffs) {
				if (gff.feature.equals("exon")) {
					this.from = gff.start;
					break;
				}
			}
			this.to = this.gffs.get(gffs.size() - 1).end;
		} else {
			for (GFF gff : gffs) {
				if (gff.start < this.from)
					this.from = gff.start;
				if (gff.end > this.to)
					this.to = gff.end;
			}
		}
		this.direction = this.gffs.get(0).strand;
		for (GFF gff : gffs) {
			if (!gff.strand.equals(this.direction)) {
				this.direction = ".";
				break;
			}
		}
	}

	/**
	 * Write a group of GFF or GTF to XML
	 * 
	 * @param doc
	 *            Document instance
	 * @param elements
	 *            Element instance
	 */
	void addToElements(Document doc, Element elements) {
		analyse();

		Element element = doc.createElement(XML_TAG_ELEMENT);
		element.setAttribute(XML_TAG_ID, this.id);
		XmlWriter.append_text_element(doc, element, XML_TAG_FROM, this.from
				+ "");
		XmlWriter.append_text_element(doc, element, XML_TAG_TO, this.to + "");
		XmlWriter.append_text_element(doc, element, XML_TAG_DIRECTION,
				this.direction);

		for (SubElement sub : ses) {
			Element subE = doc.createElement(XML_TAG_SUBELEMENT);
			subE.setAttribute(XML_TAG_TYPE, sub.type);
			XmlWriter.append_text_element(doc, subE, XML_TAG_FROM, sub.from
					+ "");
			XmlWriter.append_text_element(doc, subE, XML_TAG_TO, sub.to + "");
			element.appendChild(subE);
		}

		elements.appendChild(element);
	}

	private void analyse() {
		if (isGTF) {
			analyse_GTF();
		} else {
			analyse_GFF();
		}
	}

	/**
	 * Deal all GTF object into a continuous region with many sub region, and
	 * all of the sub region in correct form.
	 */
	private void analyse_GTF() {
		List<SubElement> CDSs = new ArrayList<SubElement>();
		List<GFF> exons = new ArrayList<GFF>();

		for (GFF gff : gffs) {
			if (gff.feature.equals("CDS")) {
				CDSs.add(new SubElement(SUBELEMENT_TYPE_BOX, gff.start, gff.end));
			} else if (gff.feature.equals("exon")) {
				exons.add(gff);
			}
		}

		// add all CDS
		ses.addAll(CDSs);
		// add all exon
		int j = 0;
		for (int i = 0; i < exons.size(); i++) {
			List<SubElement> list = new ArrayList<SubElement>();
			GFF exon = exons.get(i);
			for (; j < CDSs.size(); j++) {
				SubElement se = CDSs.get(j);
				if (se.to <= exon.end)
					list.add(se);
				else
					break;
			}
			exon_CDS(exon, list, ses);
		}
		Collections.sort(ses);
		// add all line
		int size = ses.size();
		for (int k = 0; k < size - 1; k++) {
			if (ses.get(k).to + 1 < ses.get(k + 1).from)
				ses.add(new SubElement(SUBELEMENT_TYPE_LINE, ses.get(k).to + 1,
						ses.get(k + 1).from - 1));
		}

		Collections.sort(ses);
	}

	private void exon_CDS(GFF exon, List<SubElement> CDS, List<SubElement> ses) {
		long cur_pos = exon.start;
		for (SubElement se : CDS) {
			if (cur_pos < se.from)
				ses.add(new SubElement(SUBELEMENT_TYPE_BAND, cur_pos,
						se.from - 1));
			cur_pos = se.to + 1;
		}
		if (cur_pos <= exon.end)
			ses.add(new SubElement(SUBELEMENT_TYPE_BAND, cur_pos, exon.end));
	}

	private void analyse_GFF() {
		for (GFF gff : gffs)
			ses.add(new SubElement(SUBELEMENT_TYPE_BOX, gff.start, gff.end));
		Collections.sort(ses);
	}

	private class SubElement implements Comparable<SubElement> {
		String type;
		long from;
		long to;

		SubElement(String type, long from, long to) {
			this.type = type;
			this.from = from;
			this.to = to;
		}

		@Override
		public int compareTo(SubElement o) {
			return (this.from > o.from) ? 1 : ((this.from == o.from) ? 0 : -1);
		}
	}
}
