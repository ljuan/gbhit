package edu.hit.mlg.individual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hit.mlg.individual.Individual.VariantMapToDBSNP;
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
	private Element elements;
	private List<ControlArea> ctrlAreas;
	private Element variants;
	private boolean isVCF;
	private String dbsnpURI;
	private String chr;
	private int start;
	private int end;
	private String id = null; // Attribute
	private String ifParam = null; // Attribute
	private EctypalElement[] eles; // All Elements of this Elements
	private HashSet<EctypalElement> needToDealEles; // All Elements need to deal
													// of
													// this Elements

	/**
	 * Remember that each deal should construct a new
	 * <code>EctypalElements</code> instance.
	 * 
	 * @param doc
	 *            XML <code>Document</code> instance
	 * @param fr
	 *            <code>FastaReader</code> instance
	 * @param elements
	 *            All <code>Element</code>s designated to be dealed
	 * @param ctrlArea
	 *            Control areas
	 * @param variants
	 *            All variantions designated to be dealed
	 * @param isVCF
	 *            Whether the <code>variants</code> come from VCF file
	 * @param dbsnpURI
	 *            DBSNP URI
	 * @param chr
	 *            chromosome name
	 * @param start
	 *            1-base
	 * @param end
	 *            1-base
	 */
	public EctypalElements(Document doc, FastaReader fr, Element elements,
			Element ctrlArea, Element variants, boolean isVCF, String dbsnpURI,
			String chr, int start, int end) {
		this.doc = doc;
		this.elements = elements;
		this.ctrlAreas = Element2ControlAreas(ctrlArea);
		this.variants = variants;
		this.isVCF = isVCF;
		this.dbsnpURI = dbsnpURI;
		this.chr = chr;
		this.start = start;
		this.end = end;

		this.id = elements.getAttribute(XML_TAG_ID);
		this.ifParam = elements.getAttribute("ifParam");
		if ("".equals(this.ifParam)) {
			ifParam = null;
		}

		NodeList nodes = elements.getChildNodes();
		int nodeNum = nodes.getLength();
		this.eles = new EctypalElement[nodeNum];
		this.needToDealEles = new HashSet<EctypalElement>();
		for (int i = 0; i < nodeNum; i++) {
			eles[i] = new EctypalElement((Element) nodes.item(i), fr, chr);
			needToDealEles.add(eles[i]);
		}
	}

	/**
	 * Start to deal.
	 * 
	 * @throws IOException
	 */
	public Element deal() throws IOException {
		List<VariantMapToDBSNP> mergeVariants = new Individual(variants, isVCF)
				.merge(dbsnpURI, chr, start, end);
		// If mergeVariants==null, return untreated Elements
		if (mergeVariants == null || mergeVariants.size() == 0)
			return this.elements;
		
		/*for(VariantMapToDBSNP v : mergeVariants)
			System.out.println(v.variant);

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		
		for (EctypalElement ee : eles) {
			ee.printVariantsInSubElement(mergeVariants);
		}*/
		
		for (EctypalElement ee : eles) {
			ee.preDeal(mergeVariants);
			if (!ee.stillNeedToDeal()){
			//	System.out.println(ee.getId() + "不需要再处理");
				needToDealEles.remove(ee);
			}
		}
		if (needToDealEles.size() > 0) {
			List<ControlArea> cas = ctrlArea2Variants(mergeVariants);
			if (cas != null && cas.size() > 0) {
				for (EctypalElement ee : needToDealEles) {
					ee.dealCtrlAreas(cas);
				}
			}
			//TODO 只需测试下列部分
			for (EctypalElement ee : needToDealEles) {
				ee.deal(mergeVariants);
			}
		}
		
		/*boolean b = false;
		for (EctypalElement ee : eles) {
			if(ee.canBeTest()){
				System.out.println(this.id + "(" + this.start + "-" + this.end + ")可以用来测试！");
				b = true;
				break;
			}
		}
		if(!b){
			System.out.println(this.id + "(" + this.start + "-" + this.end + ")不可用来测试！");
		}*/
		
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
	 * @param mergeVariants
	 * @return
	 */
	private List<ControlArea> ctrlArea2Variants(List<VariantMapToDBSNP> mergeVariants) {
		if (ctrlAreas == null || ctrlAreas.size() == 0)
			return null;
		List<ControlArea> ca2vs = new ArrayList<ControlArea>();
		int size = ctrlAreas.size();
		int index = 0;
		ControlArea cur = ctrlAreas.get(index++);
		Variant v = null;
		for (int i = 0, num = mergeVariants.size(); i < num; i++) {
			v = mergeVariants.get(i).variant;
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
			} else if (EctypalElement.overlap(v.getFrom(), v.getTo(), cur.from, cur.to)) {
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
				// Now v.getFrom()<curTo
				i--;
			}
		}

		return ca2vs;
	}

	public void print() {
		System.out.println("id=" + id);
		System.out.println("ifParam=" + ifParam);
		for (int i = 0, nodeNum = eles.length; i < nodeNum; i++) {
			eles[i].print();
		}
	}

	private Element write2XML() {
		Element elements = doc.createElement(XML_TAG_ELEMENTS);
		if (id != null)
			elements.setAttribute(XML_TAG_ID, id);
		if (ifParam != null)
			elements.setAttribute("ifParam", ifParam);
		Element ele = null;
		for (int i = 0, len = eles.length; i < len; i++) {
			//ENST00000366973, ENST00000531963, ENST00000526997, ENST00000525569
			//if(eles[i].getId().equals("ENST00000366973") || eles[i].getId().equals("ENST00000531963") ||
				//	eles[i].getId().equals("ENST00000526997") || eles[i].getId().equals("ENST00000525569")){
				ele = eles[i].write2XML(doc);
				elements.appendChild(ele);
			//}
		}
		return elements;
	}

	static class ControlArea {
		String id;
		int from;
		int to;
	}
}
