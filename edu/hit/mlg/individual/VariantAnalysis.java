package edu.hit.mlg.individual;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
	private Element variants;
	private String dbsnpURI;
	private String chr;
	private int start;
	private int end;

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
	 * @param dbsnpURI
	 *            DBSNP URI
	 * @param chr
	 *            chromosome name
	 * @param start
	 *            1-base
	 * @param end
	 *            1-base
	 */
	public VariantAnalysis(Document doc, FastaReader fr, Element elements, Element ctrlArea, Element variants, String dbsnpURI,
			String chr, int start, int end) {
		this.doc = doc;
		this.fr = fr;
		this.elements = elements;
		this.ctrlAreas = ctrlArea;
		this.variants = variants;
		this.dbsnpURI = dbsnpURI;
		this.chr = chr;
		this.start = start;
		this.end = end;
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
		List<VariantMapToDBSNP> mergeVariants = new Individual(variants).merge(dbsnpURI, chr, start, end);
		// If mergeVariants==null, return untreated Elements
		if (mergeVariants == null || mergeVariants.size() == 0){
			return new Element[]{ new EctypalElements(doc, fr, elements, ctrlAreas, chr, false).write2XML() };
		}
		
		Object[] divide = VariantMapToDBSNP.divide(mergeVariants);
		Element first = null;
		Element second = null;

		if(divide.length == 1){
			List<VariantMapToDBSNP> firstList = (List<VariantMapToDBSNP>)divide[0];
			EctypalElements ee = new EctypalElements(doc, fr, elements, ctrlAreas, chr, false);
			first = firstList.size() > 0
					? ee.deal(firstList) : 
						ee.write2XML();
			

			return new Element[]{ first };
		}else{
			List<VariantMapToDBSNP> firstList = (List<VariantMapToDBSNP>)divide[0];
			EctypalElements ee1 = new EctypalElements(doc, fr, elements, ctrlAreas, chr, true);
			first = firstList.size() > 0 ? ee1.deal(firstList) : ee1.write2XML();
			List<VariantMapToDBSNP> secondList = (List<VariantMapToDBSNP>)divide[1];
			EctypalElements ee2 = new EctypalElements(doc, fr, elements, ctrlAreas, chr, true);
			second = secondList.size() > 0 ? ee2.deal(secondList) : ee2.write2XML();

			return new Element[]{ first, second };
		}
	}

	static class ControlArea {
		String id;
		int from;
		int to;
	}
}
