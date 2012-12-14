package FileReaders.vcf;

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
public class Variant implements Consts {
	private int from;
	private int to;
	private String letter;
	private String toChr;
	private String direction;
	private String description;
	private String id = "id";
	private String type;

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
	 * Write to xml in an Variants element.
	 * 
	 * @param doc
	 */
	public void write2xml(Document doc, Element parent) {
		Element v = doc.createElement(XML_TAG_VARIANT);
		parent.appendChild(v);
		v.setAttribute(XML_TAG_ID, id);
		v.setAttribute(XML_TAG_TYPE, type);
		XmlWriter.append_text_element(doc, v, XML_TAG_FROM, from + "");
		XmlWriter.append_text_element(doc, v, XML_TAG_TO, to + "");
		if (letter != null)
			XmlWriter.append_text_element(doc, v, XML_TAG_LETTER, letter);
		if (toChr != null)
			XmlWriter.append_text_element(doc, v, XML_TAG_TOCHR, toChr);
		if (direction != null)
			XmlWriter.append_text_element(doc, v, XML_TAG_DIRECTION, direction);
		if (description != null)
			XmlWriter.append_text_element(doc, v, XML_TAG_DESCRIPTION,
					description);
	}
}
