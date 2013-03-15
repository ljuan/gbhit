package edu.hit.mlg.individual;

import static FileReaders.Consts.XML_TAG_FROM;
import static FileReaders.Consts.XML_TAG_ID;
import static FileReaders.Consts.XML_TAG_LETTER;
import static FileReaders.Consts.XML_TAG_TO;
import static FileReaders.Consts.XML_TAG_TYPE;
import static FileReaders.Consts.XML_TAG_VARIANT;

import static FileReaders.bam.BAMValueList.intArray2IntString;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import FileReaders.XmlWriter;

/*
 *We need this class because the Variant may contain many from and to.
 */
public class MultiFromVariant {
	private String id;// Attribute
	private String type;// Attribute
	private int[] from;// Tag
	private int[] to;// Tag
	private String letter;// Tag
	private int firstFrom;
	private int firstTo;
	int num;

	public MultiFromVariant(String id, String type, int[] from, int[] to, String letter){
		this.id = id;
		this.type = type;
		this.letter = letter;
		this.num = 0;
		this.from = new int[4];
		this.to = new int[4];
		for(int i=0; i<from.length; i++){
			addFromTo(from[i], to[i]);
		}
	}
	
	public void addFromTo(int from, int to){
		if(num == 0){
			this.firstFrom = from;
			this.firstTo = to;
		}
		if(this.num == this.from.length){
			this.from = addCapacity(this.from);
			this.to = addCapacity(this.to);
		}
		this.from[num] = from;
		this.to[num++] = to;
	}
	
	private int[] addCapacity(int[] array){
		int[] newArray = new int[array.length * 2];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}
	
	public String getId(){
		return this.id;
	}
	
	public String getType(){
		return this.type;
	}
	
	public String getLetter(){
		return this.letter;
	}
	
	public int getFirstFrom(){
		return this.firstFrom;
	}
	
	public int getFirstTo(){
		return this.firstTo;
	}
	
	public int[] getFrom(){
		return this.from;
	}
	
	public int[] getTo(){
		return this.to;
	}
	
	/**
	 * Write to xml.
	 * @return
	 */
	public void write2xml(Document doc, Element parent) {
		Element v = doc.createElement(XML_TAG_VARIANT);
		parent.appendChild(v);
		v.setAttribute(XML_TAG_ID, id);
		v.setAttribute(XML_TAG_TYPE, type);
		XmlWriter.append_text_element(doc, v, XML_TAG_FROM, intArray2IntString(from, 0, num - 1));
		XmlWriter.append_text_element(doc, v, XML_TAG_TO, intArray2IntString(to, 0, num - 1));
		if (letter != null)
			XmlWriter.append_text_element(doc, v, XML_TAG_LETTER, letter);
	}
}
