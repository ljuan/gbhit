package edu.hit.mlg.individual;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static FileReaders.Consts.*;

/**
 * 
 * @author Chengwu Yan
 *
 */
public class GdfElementSelector {
	private Document doc;
	private Set<String> variantIDs;
	private Set<String> elementSymbols;
	
	/**
	 * 
	 * @param doc XML <code>Document</code> instance
	 * @param elements	BED elements
	 * @param variants	VCF variants
	 */
	public GdfElementSelector(Document doc, Element[] elements, Element variants){
		this.doc = doc;
		getIdsFromVariant(variants);
		getIdsFromElement(elements);
	}
	
	/**
	 * Select element from <code>gdf</code> if the element satisfy one of the conditions below:</br>
	 * 1:The element contains attribute:Variant and the value is one of the id of <code>variants</code>;</br>
	 * 2:The element doesn't contain attribute:Variant but contains attribute:Symbol and the value is one of
	 * the id of <code>elements</code> which has beed affected by some variants.
	 * @param gdf	GDF elements
	 * @return elements after select.
	 */
	public Element select(Element gdf){
		Element eles = doc.createElement(XML_TAG_ELEMENTS);
		eles.setAttribute(XML_TAG_ID, gdf.getAttribute(XML_TAG_ID));
		String ifParam = eles.getAttribute(XML_TAG_IFP);
		if(ifParam != null && !ifParam.isEmpty())
			eles.setAttribute(XML_TAG_IFP, ifParam);
		NodeList nodes = gdf.getChildNodes();//All the gdf elements
		Element ele = null;
		String variant = null;
		String symbol = null;
		for(int i=0, num=nodes.getLength(); i<num; i++){
			ele = (Element)nodes.item(i);
			variant = ele.getAttribute(XML_TAG_VARIANT);
			if(variant != null && !variant.equals("")){
				if(variantIDs.contains(ele.getAttribute(XML_TAG_ID))){
					//The element contains attribute:Variant and
					//the value is one of the id of variants.
					eles.appendChild(new EctypalElement(ele, null, null, true).write2XML(doc));
				}
				continue;
			}
			
			symbol = ele.getAttribute(XML_TAG_SYMBOL);
			if(symbol != null && !symbol.equals("")){
				if(elementSymbols.contains(ele.getAttribute(XML_TAG_ID))){
					//The element doesn't contain attribute:Variant
					//but contains attribute:Symbol and the value is one of
					//the id of elements which has beed affected by some variants
					eles.appendChild(new EctypalElement(ele, null, null, true).write2XML(doc));
				}
			}
		}
		return eles;
	}
	
	/**
	 * Extract all id from Variant.
	 * @param variants
	 */
	private void getIdsFromVariant(Element variants){
		variantIDs = new HashSet<String>();
		NodeList nodes = variants.getChildNodes();
		for(int i=0, num=nodes.getLength(); i<num; i++)
			variantIDs.add(((Element)nodes.item(i)).getAttribute(XML_TAG_ID));
	}
	
	/**
	 * Extract all id from Element which has been affected by some variants.
	 * @param elements
	 */
	private void getIdsFromElement(Element[] elements){
		elementSymbols = new HashSet<String>();
		NodeList nodes = null;
		Element ele = null;
		String symbol = null;
		for(Element eles : elements){
			nodes = eles.getChildNodes();
			for(int i=0, num=nodes.getLength(); i<num; i++){
				ele = (Element)nodes.item(i);
				if(ele.getElementsByTagName(XML_TAG_VARIANT).getLength() > 0 ){
					symbol = ele.getAttribute(XML_TAG_SYMBOL);
					if(symbol != null && !"".equals(symbol))
						elementSymbols.add(symbol);
				}
			}
		}
	}
}
