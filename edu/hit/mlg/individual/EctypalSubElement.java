package edu.hit.mlg.individual;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hit.mlg.Tools.LinkedArrayList;
import edu.hit.mlg.Tools.LinkedArrayList.Entry;
import edu.hit.mlg.individual.vcf.Variant;

import static FileReaders.Consts.*;
import FileReaders.XmlWriter;

public class EctypalSubElement {
	////////////////////////////////static field
	final static Map<String, Map<String, String>> currentNeedToMap = new HashMap<String, Map<String,String>>();
	final static Map<String, String> currentIsBOX = new HashMap<String, String>();
	final static Map<String, String> currentIsExtendBOX = new HashMap<String, String>();
	final static Map<String, String> currentIsSkipBOX = new HashMap<String, String>();
	static{
		currentNeedToMap.put(SUBELEMENT_TYPE_BOX, currentIsBOX);
		currentNeedToMap.put(SUBELEMENT_TYPE_EXTEND_BOX, currentIsExtendBOX);
		currentNeedToMap.put(SUBELEMENT_TYPE_SKIP_BOX, currentIsSkipBOX);
		///////////////////////////
		currentIsBOX.put(SUBELEMENT_TYPE_BOX, SUBELEMENT_TYPE_BOX);
		currentIsBOX.put(SUBELEMENT_TYPE_LOST_BOX, SUBELEMENT_TYPE_LOST_BOX);
		currentIsBOX.put(SUBELEMENT_TYPE_EXTEND_BAND, SUBELEMENT_TYPE_LOST_BOX);
		currentIsBOX.put(SUBELEMENT_TYPE_SKIP_BAND, SUBELEMENT_TYPE_LOST_BOX);
		currentIsBOX.put(SUBELEMENT_TYPE_SHIFT_BOX, SUBELEMENT_TYPE_SHIFT_BOX);
		currentIsBOX.put(SUBELEMENT_TYPE_EXTEND_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		currentIsBOX.put(SUBELEMENT_TYPE_SKIP_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		currentIsBOX.put(SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		currentIsBOX.put(SUBELEMENT_TYPE_SHIFT_EXTEND_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		currentIsBOX.put(SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		/////////////////////////
		currentIsExtendBOX.put(SUBELEMENT_TYPE_BOX, SUBELEMENT_TYPE_EXTEND_BOX);
		currentIsExtendBOX.put(SUBELEMENT_TYPE_LOST_BOX, SUBELEMENT_TYPE_EXTEND_BAND);
		currentIsExtendBOX.put(SUBELEMENT_TYPE_EXTEND_BAND, SUBELEMENT_TYPE_EXTEND_BAND);
		currentIsExtendBOX.put(SUBELEMENT_TYPE_SKIP_BAND, SUBELEMENT_TYPE_EXTEND_BAND);
		currentIsExtendBOX.put(SUBELEMENT_TYPE_SHIFT_BOX, SUBELEMENT_TYPE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(SUBELEMENT_TYPE_EXTEND_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(SUBELEMENT_TYPE_SKIP_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(SUBELEMENT_TYPE_SHIFT_EXTEND_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX, SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		//////////////////////////////
		currentIsSkipBOX.put(SUBELEMENT_TYPE_BOX, SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(SUBELEMENT_TYPE_LOST_BOX, SUBELEMENT_TYPE_SKIP_BAND);
		currentIsSkipBOX.put(SUBELEMENT_TYPE_EXTEND_BAND, SUBELEMENT_TYPE_SKIP_BAND);
		currentIsSkipBOX.put(SUBELEMENT_TYPE_SKIP_BAND, SUBELEMENT_TYPE_SKIP_BAND);
		currentIsSkipBOX.put(SUBELEMENT_TYPE_SHIFT_BOX, SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(SUBELEMENT_TYPE_EXTEND_BOX, SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(SUBELEMENT_TYPE_SKIP_BOX, SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX, SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(SUBELEMENT_TYPE_SHIFT_EXTEND_BOX, SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX, SUBELEMENT_TYPE_SKIP_BOX);
	}
	/**
	 * There is 10 types of SubElement, but only 4 types of them need to deal. And this 4 types of
	 * SubElement are divided into 2 category.
	 */
	final static Map<String, Integer> currentNeedToDeal = new HashMap<String, Integer>();
	static{
		currentNeedToDeal.put(SUBELEMENT_TYPE_BOX, 1);
		currentNeedToDeal.put(SUBELEMENT_TYPE_EXTEND_BOX, 2);
		currentNeedToDeal.put(SUBELEMENT_TYPE_SHIFT_BOX, 3);
		currentNeedToDeal.put(SUBELEMENT_TYPE_SHIFT_EXTEND_BOX, 4);
	}
	////////////////////////////////end of static field
	
	private MultiFromVariant[] variants;// All variation of this SubElement
	private int variantsNum;// Variantion number of this SubElement
	private String id = null;// Attribute
	private String type = null;// Attribute
	private int from;// Tag
	private int to;// Tag
	private String direction = null;// Tag
	private String description = null;// Tag

	EctypalSubElement() {
		this.variants = new MultiFromVariant[4];
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
		this.variants = new MultiFromVariant[4];
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
	 * Add a MultiFromVariant to this SubElement.
	 * 
	 * @param id	variation id
	 * @param type	variation type
	 * @param from	start base of this variation, 1-base. May contain many from.
	 * @param to	end base of this variation, 1-base. May contain many to.
	 * @param letter	letter of this variation. Null if this variation doesn't has
	 *       			any letter.
	 */
	void addMultiFromVariant(String id, String type, int[] from, int[] to, String letter) {
		ensureCapacity();
		variants[variantsNum++] = new MultiFromVariant(id, type, from, to, letter);
	}
	
	void addMultiFromVariant(MultiFromVariant variant) {
		ensureCapacity();
		variants[variantsNum++] = variant;
	}
	
	void addMultiFromVariant(Variant v){
		addMultiFromVariant(v.getId(), v.getType(), new int[]{v.getFrom()}, new int[]{v.getTo()}, v.getLetter());
	}

	private void ensureCapacity() {
		if (variantsNum == variants.length) {
			MultiFromVariant[] vs = new MultiFromVariant[variants.length * 2];
			System.arraycopy(variants, 0, vs, 0, variants.length);
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
	 * @param direction The direction of the Element, true for "+", false else.
	 * @return
	 */
	static EctypalSubElement[] divideInto2SubElements(EctypalSubElement subEle, int sepPos,
			String firstType, String secondType, boolean sepPosBelongFirst, boolean direction) {
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
		right.id = subEle.id;
		right.type = secondType;
		right.from = reallySepPos + 1;
		right.to = subEle.to;
		right.direction = subEle.direction;
		right.description = subEle.description;

		for (MultiFromVariant v : subEle.variants) {
			if(v == null) break;
			if (v.getFirstTo() <= reallySepPos) {
				// Add the variation to the left SubElement
				left.addMultiFromVariant(v);
			} else if (v.getFirstFrom() > reallySepPos) {
				// Add the variation to the right SubElement
				right.addMultiFromVariant(v);
			} else {//Must be INS variant
				if(direction) left.addMultiFromVariant(v);
				else right.addMultiFromVariant(v);
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
			for(int i=0; i<variantsNum; i++){
				variants[i].write2xml(doc, subEle);
			}
		}

		return subEle;
	}
	
	public String toString(){
		return this.type + ":" + this.from + "-" + this.to;
	}
	
	/**
	 * Judge that whether [from1, to1] overlaps with [from2, to2].
	 * @return
	 */
 	public static boolean overlap(int from1, int to1, int from2, int to2) {
 		return to1 >= from2 && from1 <= to2;
	}

	/**
	 * Judge that whether [from1, to1] contained in [from2, to2].
	 * @return
	 */
	public static boolean contained(int from1, int to1, int from2, int to2) {
		return from1 >= from2 && to1 <= to2;
	}
	
	/**
	 * Whether the type is not "Line" and not "Band".
	 * @param type
	 * @return
	 */
	static boolean notLineNotBand(String type){
		return !SUBELEMENT_TYPE_LINE.equals(type) && !SUBELEMENT_TYPE_BAND.equals(type);
	}
	
	static boolean shouldAddBoxBases(String type, boolean hasEffect){
		return (hasEffect && currentNeedToDeal.containsKey(type)) || (!hasEffect && notLineNotBand(type));
	}
	
	/**
	 * Get the previous Box of the <code>cur</code>. For <code>hasEffect=true</code>, the BOX is one of "Box", "Extend_Box", "Shift_Box", 
	 * "Shift_Extend_Box"; for <code>hasEffect=false</code>, the BOX is one of the type which is not "Line" and "Band".
	 * @return
	 */
	static Entry<EctypalSubElement> getPreviousBox(LinkedArrayList<EctypalSubElement> subEles, Entry<EctypalSubElement> cur, boolean hasEffect){
		while(true){
			cur = subEles.getPrevious(cur);
			if(cur == null) return null;
			if(shouldAddBoxBases(cur.getElement().getType(), hasEffect))
				return cur;
		}
	}
	
	/**
	 * Get the next Box of <code>cur</code>. For <code>hasEffect=true</code>, the BOX is one of "Box", "Extend_Box", "Shift_Box", 
	 * "Shift_Extend_Box"; for <code>hasEffect=false</code>, the BOX is one of the type which is not "Line" and "Band".
	 * @return
	 */
	static Entry<EctypalSubElement> getNextBox(LinkedArrayList<EctypalSubElement> subEles, Entry<EctypalSubElement> cur, boolean hasEffect){
		while(true){
			cur = subEles.getNext(cur);
			if(cur == null) return null;
			if(shouldAddBoxBases(cur.getElement().getType(), hasEffect))
				return cur;
		}
	}
}
