package edu.hit.mlg.individual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hit.mlg.individual.vcf.Variant;

import FileReaders.FastaReader;

/**
 * Create an ectypal Object of XML Element, and deal this ectypal Object by
 * Variants.
 * 
 * @author Chengwu Yan
 * 
 */
public class VariantAnalysis {
	private Document doc;
	private FastaReader fr;
	private Element elements;
	private Element ctrlAreas;
	private List<Variant> variants;
	private String chr;

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
	 *            All variantions designated to be dealed, after merge with DBSNP.
	 * @param chr
	 *            chromosome name
	 */
	public VariantAnalysis(Document doc, FastaReader fr, Element elements, Element ctrlArea, Element variants, String chr) {
		this.doc = doc;
		this.fr = fr;
		this.elements = elements;
		this.ctrlAreas = ctrlArea;
		this.variants = extractVariantsFromNode(variants);
		this.chr = chr;
	}

	/**
	 * Start to deal.<br />
	 * The result may contain just one Object or two Object.
	 * The result will contain two Object just when all the variants' homo is separate by '|' 
	 * and just contain one '|'.
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public Element[] deal() throws IOException {
		if (variants.size() == 0){
			return new Element[]{ new EctypalElements(doc, fr, elements, ctrlAreas, chr, false).write2XML() };
		}
		
		Object[] divide = VariantMapToDBSNP.divide(variants);
		Element first = null;
		Element second = null;

		if(divide.length == 1){
			List<Variant> firstList = (List<Variant>)divide[0];
			EctypalElements ee = new EctypalElements(doc, fr, elements, ctrlAreas, chr, false);
			first = firstList.size() > 0 ? ee.deal(firstList) : ee.write2XML();

			return new Element[]{ first };
		}else{
			List<Variant> firstList = (List<Variant>)divide[0];
			EctypalElements ee1 = new EctypalElements(doc, fr, elements, ctrlAreas, chr, true);
			first = firstList.size() > 0 ? ee1.deal(firstList) : ee1.write2XML();
			List<Variant> secondList = (List<Variant>)divide[1];
			EctypalElements ee2 = new EctypalElements(doc, fr, elements, ctrlAreas, chr, true);
			second = secondList.size() > 0 ? ee2.deal(secondList) : ee2.write2XML();

			return new Element[]{ first, second };
		}
		
	}
	
	/**
	 * Copy all variants from Element:variants
	 * @param variants
	 * @return
	 */
	private static List<Variant> extractVariantsFromNode(Element variants){
		List<Variant> list = new ArrayList<Variant>();
		NodeList nodeList = variants.getChildNodes();
		for (int i = 0, num = nodeList.getLength(); i < num; i++)
			list.add(Variant.convertElement2Variant((Element)nodeList.item(i)));
		return list;
	}

	static class ControlArea {
		String id;
		int from;
		int to;
	}
}
