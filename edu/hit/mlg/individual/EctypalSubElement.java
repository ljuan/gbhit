package edu.hit.mlg.individual;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hit.mlg.individual.vcf.Variant;
import static FileReaders.Consts.*;
import FileReaders.XmlWriter;

public class EctypalSubElement {
	private Variant[] variants;// All variation of this SubElement
	private int variantsNum;// Variantion number of this SubElement
	private String id = null;// Attribute
	private String type = null;// Attribute
	private int from;// Tag
	private int to;// Tag
	private String direction = null;// Tag
	private String description = null;// Tag

	EctypalSubElement() {
		this.variants = new Variant[4];
		this.variantsNum = 0;
	}

	void print() {
		System.out.println("----SubElement----");
		if(id != null) 	System.out.println("id=" + id);
		if(type != null) 	System.out.println("type=" + type);
		System.out.println("from=" + from);
		System.out.println("to=" + to);
		if(direction != null)	System.out.println("direction=" + direction);
		if(description != null)	System.out.println("description=" + description);
		System.out.println("----End----");
	}

	EctypalSubElement(Element ele) {
		this.variants = new Variant[4];
		this.variantsNum = 0;
		this.id = ele.getAttribute(XML_TAG_ID);
		if ("".equals(this.id))	this.id = null;
		this.type = ele.getAttribute(XML_TAG_TYPE);
		NodeList nodes = ele.getChildNodes();// All Children
		// The first child must be "From"
		this.from = Integer.parseInt(nodes.item(0).getTextContent());
		// The second child must be "To"
		this.to = Integer.parseInt(nodes.item(1).getTextContent());
		retrieveTag(nodes, 2);
		retrieveTag(nodes, 3);
	}

	private void retrieveTag(NodeList nodes, int index) {
		if (nodes.getLength() > index) {
			Element e = (Element) nodes.item(index);
			if (e.getTagName().equals(XML_TAG_DESCRIPTION)) {
				this.description = e.getTextContent();
			} else {
				this.direction = e.getTextContent();
			}
		}
	}

	/**
	 * Get the attribute of Type of this SubElement.
	 * 
	 * @return
	 */
	String getType() {
		return this.type;
	}

	/**
	 * Change the attribute of Type of this SubElement.
	 * 
	 * @param type	new Type value
	 */
	void setType(String type) {
		this.type = type;
	}

	/**
	 * Get number of bases of this SubElement.
	 * 
	 * @return
	 */
	int getLength() {
		return to - from + 1;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	/**
	 * Add a variation to this SubElement.
	 * 
	 * @param id	variation id
	 * @param type	variation type
	 * @param from	start base of this variation, 1-base.
	 * @param to	end base of this variation, 1-base.
	 * @param letter	letter of this variation. Null if this variation doesn't has
	 *       			any letter.
	 */
	void addVariantion(String id, String type, int from, int to, String letter) {
		ensureCapacity();
		variants[variantsNum] = new Variant(id, type, from, to);
		variants[variantsNum++].setLetter(letter);
	}

	/**
	 * Add a variation to this SubElement.
	 * 
	 * @param variant
	 */
	void addVariantion(Variant variant) {
		ensureCapacity();
		variants[variantsNum++] = variant;
	}

	private void ensureCapacity() {
		if (variantsNum == variants.length) {
			Variant[] vs = new Variant[variants.length + variants.length];
			System.arraycopy(variants, 0, vs, 0, variants.length);
			variants = null;
			variants = vs;
		}
	}

	/**
	 * Divide this SubElement into two SubElements. The first SubElement's type is <code>firstType</code>,
	 * the second SubElement'type is <code>secondType</code>.<br />
	 * The <code>sepPos</code> must at <code>subEle</code>, and if <code>sepPosBelongFirst==true</code>,
	 * <code>sepPos</code> belongs the first SubElement, else <code>sepPos</code> belongs the second SubElement. 
	 * 
	 * @param subEle 
	 * 				The SubElement need to be divided.
	 * @param sepPos 
	 * 				The absolute separate position of <code>subEle</code>.
	 * @param firstType 
	 * 				The first SubElement's type of the divide result.
	 * @param secondType 
	 * 				The second SubElement's type of the divide result.
	 * @param sepPosBelongFirst
	 * 				Whether the <code>sepPos</code> is belonging the first SubElement.
	 * 				True if the <code>sepPos</code> is belonging the first SubElement, false else.
	 * @return
	 */
	static EctypalSubElement[] divideInto2SubElements(EctypalSubElement subEle, int sepPos,
			String firstType, String secondType, boolean sepPosBelongFirst) {
		EctypalSubElement left = null;
		EctypalSubElement right = null;
		int reallySepPos = (sepPosBelongFirst ? sepPos : sepPos-1); //reallySepPos belongs the first SubElement 
		// Left SubElement
		left = new EctypalSubElement();
		left.id = subEle.id;
		left.type = firstType;
		left.from = subEle.from;
		left.to = reallySepPos;
		left.direction = subEle.direction;
		left.description = subEle.description;
		// Right SubElement
		right = new EctypalSubElement();
		left.id = subEle.id;
		left.type = secondType;
		left.from = reallySepPos + 1;
		left.to = subEle.to;
		left.direction = subEle.direction;
		left.description = subEle.description;

		for (Variant v : subEle.variants) {
			if (v.getTo() <= reallySepPos) {
				// Add the variation to the left SubElement
				left.addVariantion(v);
			} else if (v.getFrom() > reallySepPos) {
				// Add the variation to the right SubElement
				right.addVariantion(v);
			} else {
				// Divide the variation into two variations and add the first variation to the 
				// left SubElement, add the second variation to the right SubElement.
				left.addVariantion(v.getId(), v.getType(), v.getFrom(), reallySepPos, v.getLetter());
				right.addVariantion(v.getId(), v.getType(), reallySepPos + 1, v.getTo(), v.getLetter());
			}
		}
		return new EctypalSubElement[] { left, right };
	}

	Element write2XML(Document doc) {
		Element subEle = doc.createElement(XML_TAG_SUBELEMENT);
		if (id != null)	subEle.setAttribute(XML_TAG_ID, id);
		subEle.setAttribute(XML_TAG_TYPE, type);
		XmlWriter.append_text_element(doc, subEle, XML_TAG_FROM, String.valueOf(from));
		XmlWriter.append_text_element(doc, subEle, XML_TAG_TO, String.valueOf(to));
		if (direction != null)
			XmlWriter.append_text_element(doc, subEle, XML_TAG_DIRECTION, direction);
		if (description != null)
			XmlWriter.append_text_element(doc, subEle, XML_TAG_DESCRIPTION, description);
		//Add all variations
		if(variantsNum > 0){
			Element variant = null;
			Variant v = null;
			for(int i=0; i<variantsNum; i++){
				v = variants[i];
				variant = doc.createElement(XML_TAG_VARIANT);
				variant.setAttribute(XML_TAG_TYPE, v.getType());
				XmlWriter.append_text_element(doc, variant, XML_TAG_FROM, String.valueOf(v.getFrom()));
				XmlWriter.append_text_element(doc, variant, XML_TAG_TO, String.valueOf(v.getTo()));
				if(v.getLetter() != null)
					XmlWriter.append_text_element(doc, variant, XML_TAG_LETTER, String.valueOf(v.getLetter()));
				subEle.appendChild(variant);
			}
		}

		return subEle;
	}
}
