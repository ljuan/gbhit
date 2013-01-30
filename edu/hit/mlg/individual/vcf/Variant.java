package edu.hit.mlg.individual.vcf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import FileReaders.Consts;
import FileReaders.XmlWriter;

/**
 * An Instance of Variant represent one variantion.
 * 
 * @author Chengwu Yan
 * 
 */
public class Variant implements Comparable<Variant> {
	private String id;// Attribute
	private String type;// Attribute
	private int from;// Tag
	private int to;// Tag
	private String letter;// Tag
	private String toChr;// Tag
	private String direction;// Tag
	private String description;// Tag
	private int homo;// //Attribute

	public Variant() {
		this.homo = 0;
	}

	public Variant(String id, String type, int from, int to) {
		this.id = id;
		this.type = type;
		this.from = from;
		this.to = to;
		homo = 0;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(64);
		builder.append(id);
		builder.append("\t");
		builder.append(type);
		builder.append("\t");
		builder.append(from);
		builder.append("\t");
		builder.append(to);
		builder.append("\t");
		if (letter != null) {
			builder.append(letter);
			builder.append("\t");
		}
		if (toChr != null) {
			builder.append(toChr);
			builder.append("\t");
		}
		if (direction != null) {
			builder.append(direction);
			builder.append("\t");
		}
		if (description != null) {
			builder.append(description);
			builder.append("\t");
		}

		return builder.toString();
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public void setLetter(String letter) {
		this.letter = letter;
	}

	public void setToChr(String toChr) {
		this.toChr = toChr;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public String getLetter() {
		return letter;
	}

	public String getToChr() {
		return toChr;
	}

	public String getDirection() {
		return direction;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	/**
	 * <pre>
	 * If <code>homo</code>=0: Don't need to set attribute of homo.
	 * If <code>homo</code>=1: Set attribute of homo=true.
	 * If <code>homo</code>=2: Set attribute of homo=false.
	 * @param homo
	 */
	public void setHomo(int homo) {
		this.homo = homo;
	}

	/**
	 * Write to xml in an Variants element of BAMRecords.
	 * 
	 * @param doc
	 * @param parent
	 */
	public void write2xml(Document doc, Element parent) {
		this.write2xml(doc, parent, true);
	}

	/**
	 * Write to xml in an Variants element of VCFRecords.
	 * 
	 * @param doc
	 * @param parent
	 * @param outputLetter
	 *            Whether to output LETTER
	 */
	public void write2xml(Document doc, Element parent, boolean outputLetter) {
		Element v = doc.createElement(Consts.XML_TAG_VARIANT);
		parent.appendChild(v);
		v.setAttribute(Consts.XML_TAG_ID, id);
		v.setAttribute(Consts.XML_TAG_TYPE, type);
		if (homo != 0)
			v.setAttribute(Consts.XML_TAG_HOMO, homo == 1 ? Consts.TEXT_TRUE : Consts.TEXT_FALSE);
		XmlWriter.append_text_element(doc, v, Consts.XML_TAG_FROM, from + "");
		XmlWriter.append_text_element(doc, v, Consts.XML_TAG_TO, to + "");
		if (letter != null && outputLetter)
			XmlWriter
					.append_text_element(doc, v, Consts.XML_TAG_LETTER, letter);
		if (toChr != null)
			XmlWriter.append_text_element(doc, v, Consts.XML_TAG_TOCHR, toChr);
		if (direction != null)
			XmlWriter.append_text_element(doc, v, Consts.XML_TAG_DIRECTION,
					direction);
		if (description != null)
			XmlWriter.append_text_element(doc, v, Consts.XML_TAG_DESCRIPTION,
					description);
	}

	@Override
	public int compareTo(Variant o) {
		return (this.from != o.from) ? (this.from - o.from) : (this.to - o.to);
	}
}
