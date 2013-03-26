package edu.hit.mlg.individual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hit.mlg.individual.VariantAnalysis.ControlArea;
import edu.hit.mlg.individual.vcf.Variant;
import FileReaders.FastaReader;
import static FileReaders.Consts.*;

/**
 * Create an ectypal Object of XML Element, and deal this ectypal Object by
 * Variants.
 * 
 * @author Chengwu Yan
 * 
 */
public class EctypalElements {
	private Document doc;
	private List<ControlArea> ctrlAreas;
	private String id = null; // Attribute
	private String ifParam = null; // Attribute
	private EctypalElement[] eles; // All Elements of this Elements
	private HashSet<EctypalElement> needToDealEles; // All Elements need to deal of this Elements

	/**
	 * Remember that each deal should construct a new <code>EctypalElements</code> instance.
	 * 
	 * @param doc
	 *            XML <code>Document</code> instance
	 * @param fr
	 *            <code>FastaReader</code> instance
	 * @param elements
	 *            All <code>Element</code>s designated to be dealed
	 * @param ctrlArea
	 *            Control areas
	 * @param chr
	 *            chromosome name
	 * @param hasEffect 
	 * 			  Whether the upstream SubElement has an effect on the downstream SubElement.
	 */
	public EctypalElements(Document doc, FastaReader fr, Element elements, Element ctrlArea, String chr, boolean hasEffect) {
		this.doc = doc;
		this.ctrlAreas = Element2ControlAreas(ctrlArea);

		this.id = elements.getAttribute(XML_TAG_ID) ;
		this.ifParam = elements.getAttribute(XML_TAG_IFP);
		NodeList nodes = elements.getChildNodes();
		int nodeNum = nodes.getLength();
		this.eles = new EctypalElement[nodeNum];
		this.needToDealEles = new HashSet<EctypalElement>();
		for (int i = 0; i < nodeNum; i++) {
			eles[i] = new EctypalElement((Element) nodes.item(i), fr, chr, hasEffect);
			needToDealEles.add(eles[i]);
		}
	}

	/**
	 * Start to deal.
	 * @param variants Al variants need to deal
	 * @return
	 * @throws IOException
	 */
	public Element deal(List<Variant> variants) throws IOException {
		List<ControlArea> cas = ctrlArea2Variants(variants);
		if (cas != null && cas.size() > 0) {
			for (EctypalElement ee : eles)
				ee.dealCtrlAreas(cas);
		}
		
		for (EctypalElement ee : eles) {
			ee.preDeal(variants);
			if (!ee.stillNeedToDeal())
				needToDealEles.remove(ee);
		}
		
		for (EctypalElement ee : needToDealEles) {
			ee.deal(variants);
		}
		
		return write2XML();
	}

	/**
	 * Convert Element to ControlArea.
	 * 
	 * @param ele
	 * @return Null if <code>ele==null</code> or <code>ele</code> has no any
	 *         child nodes. Or a list of ControlArea contains All child nodes.
	 */
	private List<ControlArea> Element2ControlAreas(Element ele) {
		if (ele == null)
			return null;

		NodeList nodes = ele.getChildNodes();
		int num = nodes.getLength();
		if (num == 0)
			return null;
		List<ControlArea> cas = new ArrayList<ControlArea>(num);
		Element e = null;
		NodeList nl = null;
		ControlArea ca = null;
		for (int index = 0; index < num; index++) {
			e = (Element) nodes.item(index);
			nl = e.getChildNodes();
			ca = new ControlArea();
			ca.id = e.getAttribute(XML_TAG_ID);
			ca.from = Integer.parseInt(nl.item(0).getTextContent());
			ca.to = Integer.parseInt(nl.item(1).getTextContent());
			cas.add(ca);
		}

		return cas;
	}

	/**
	 * Return control areas where some variantions effect at them.
	 * 
	 * @param variants
	 * @return
	 */
	private List<ControlArea> ctrlArea2Variants(List<Variant> variants) {
		if (ctrlAreas == null || ctrlAreas.size() == 0)
			return null;
		List<ControlArea> ca2vs = new ArrayList<ControlArea>();
		int size = ctrlAreas.size();
		int index = 0;
		ControlArea cur = ctrlAreas.get(index++);
		Variant v = null;
		for (int i = 0, num = variants.size(); i < num; i++) {
			v = variants.get(i);
			if (v.getTo() < cur.from) {
				continue;
			}
			if (VARIANT_TYPE_INSERTION.equals(v.getType()) || VARIANT_TYPE_BLS.equals(v.getType())) {
				int minus = (v.getType().equals(VARIANT_TYPE_INSERTION)) ? 1 : 0;
				if (v.getFrom() >= cur.from - minus && v.getFrom() <= cur.to) {
					ca2vs.add(cur);
					if (index == size)
						break;
					cur = ctrlAreas.get(index++);
				}
			} else if (EctypalSubElement.overlap(v.getFrom(), v.getTo(), cur.from, cur.to)) {
				ca2vs.add(cur);
				if (index == size)
					break;
				cur = ctrlAreas.get(index++);
			}
			if(v.getFrom() > cur.to){
				while (v.getFrom() > cur.to) {
					if (index == size)
						break;
					cur = ctrlAreas.get(index++);
				}
				if(v.getFrom() > cur.to)
					break;
				i--;
			}
		}

		return ca2vs;
	}

	public Element write2XML() {
		Element elements = doc.createElement(XML_TAG_ELEMENTS);
		if (id != null)
			elements.setAttribute(XML_TAG_ID, id);
		if (ifParam != null && !ifParam.isEmpty())
			elements.setAttribute(XML_TAG_IFP, ifParam);
		Element ele = null;
		for (int i = 0, len = eles.length; i < len; i++) {
				ele = eles[i].write2XML(doc);
				elements.appendChild(ele);
		}
		return elements;
	}
}
