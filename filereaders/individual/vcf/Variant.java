package filereaders.individual.vcf;

import static filereaders.Consts.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import filereaders.Consts;
import filereaders.XmlWriter;


/**
 * An Instance of Variant represent one variantion.
 * 
 * @author Chengwu Yan
 * 
 */
public class Variant implements Comparable<Variant> {
	public static final String LARGE_VARIANTION = "LargeSV_affected";
	public static final int hash_SNV = VARIANT_TYPE_SNV.hashCode();
	public static final int hash_INS = VARIANT_TYPE_INSERTION.hashCode();
	public static final int hash_DEL = VARIANT_TYPE_DELETION.hashCode();
	public static final int hash_INV = VARIANT_TYPE_INVERSION.hashCode();
	public static final int hash_CNV = VARIANT_TYPE_CNV.hashCode();
	public static final int hash_DUP = VARIANT_TYPE_DUPLICATION.hashCode();
	public static final int hash_BLS = VARIANT_TYPE_BLS.hashCode();

	private String id;// Attribute
	private String dbsnpid;// Attribute
	private String type;// Attribute
	private int from;// Tag
	private int to;// Tag
	private String letter;// Tag
	private String toChr;// Tag
	private String direction;// Tag
	private String description;// Tag
	private String homo;// //Attribute
	private String dbsnpInfo;// Tag

	public Variant() {
		this.homo = "";
	}

	public Variant(String id, String type, int from, int to) {
		this.id = id;
		this.type = type;
		this.from = from;
		this.to = to;
		homo = "";
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
	
	public void setDbsnpid(String dbsnpid) {
		this.dbsnpid = dbsnpid;
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

	public void setHomo(String homo) {
		this.homo = homo;
	}
	
	public String getHomo(){
		return homo;
	}

	public void setDbsnpInfo(String dbsnpInfo){
		this.dbsnpInfo = dbsnpInfo;
	}
	
	public String getDbsnpInfo(){
		return this.dbsnpInfo;
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
		Element v = doc.createElement(XML_TAG_VARIANT);
		parent.appendChild(v);
		v.setAttribute(XML_TAG_ID, id);
		v.setAttribute(XML_TAG_TYPE, type);
		if(dbsnpid != null)
			v.setAttribute(Consts.XML_TAG_DBSNPID, dbsnpid);
		if (!"".equals(homo))
			v.setAttribute(XML_TAG_HOMO, homo);
		XmlWriter.append_text_element(doc, v, XML_TAG_FROM, from + "");
		XmlWriter.append_text_element(doc, v, XML_TAG_TO, to + "");
		if (letter != null && outputLetter)
			XmlWriter.append_text_element(doc, v, XML_TAG_LETTER, letter);
		if (toChr != null)
			XmlWriter.append_text_element(doc, v, XML_TAG_TOCHR, toChr);
		if (direction != null)
			XmlWriter.append_text_element(doc, v, XML_TAG_DIRECTION, direction);
		if (description != null)
			XmlWriter.append_text_element(doc, v, XML_TAG_DESCRIPTION, description);
		if(dbsnpInfo != null)
			XmlWriter.append_text_element(doc, v, XML_TAG_DBSNP, dbsnpInfo);
	}
	
	public static Variant convertElement2Variant(Element ele){
		Variant v = new Variant();
		v.setId(ele.getAttribute(XML_TAG_ID));
		v.setType(ele.getAttribute(XML_TAG_TYPE));
		String homo = ele.getAttribute(XML_TAG_HOMO);
		v.setHomo(homo);
		
		NodeList children = ele.getChildNodes();
		v.setFrom(Integer.parseInt(children.item(0).getTextContent()));
		v.setTo(Integer.parseInt(children.item(1).getTextContent()));
		for(int index = 2, len = children.getLength(); index < len; index++){
			Element e = (Element) children.item(index);
			if (e.getTagName().equals(XML_TAG_LETTER))
				v.setLetter(e.getTextContent());
			else if (e.getTagName().equals(XML_TAG_TOCHR))
				v.setToChr(e.getTextContent());
			else if (e.getTagName().equals(XML_TAG_DIRECTION))
				v.setDirection(e.getTextContent());
			else if (e.getTagName().equals(XML_TAG_DESCRIPTION))
				v.setDescription(e.getTextContent());
			else if (e.getTagName().equals(XML_TAG_DBSNP))
				v.setDbsnpInfo(e.getTextContent());
		}
		
		return v;
	}

	@Override
	public int compareTo(Variant o) {
		return (this.from != o.from) ? (this.from - o.from) : (this.to - o.to);
	}
	
	public static Variant copy(Variant obj){
		if(obj == null) return null;
		Variant newObj = new Variant();
		newObj.id = obj.id;
		newObj.type = obj.type;
		newObj.from = obj.from;
		newObj.to = obj.to;
		newObj.letter = obj.letter;
		newObj.toChr = obj.toChr;
		newObj.direction = obj.direction;
		newObj.description = obj.description;
		newObj.homo = obj.homo;
		return newObj;
	}
}
