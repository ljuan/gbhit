package edu.hit.mlg.individual.vcf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static FileReaders.Consts.*;

/**
 * An instance of Variants represent DBSnp or an individual of SAMPLEs.
 * 
 * @author Chengwu Yan
 * 
 */
public class Variants {
	/**
	 * For DBSnp, it is track; For Personal Genemic, it is Track_Sample.
	 */
	private Element ele;

	private Document doc;
	/**
	 * bases per pixel
	 */
	private double bpp;
	/**
	 * Whether to output LETTER
	 */
	private boolean outputLetter;
	/**
	 * Whether mode equals "DENSE"
	 */
	private boolean isModeDENSE = false;

	private int lastpos = 0;

	/**
	 * 
	 * @param id
	 *            For DBSnp, it is track; For Personal Genemic, it is
	 *            Track_Sample.
	 * @param subId
	 *            For Personal Genemic, every Variants has a subId.
	 * @param doc
	 * @param mode
	 * @param bpp
	 *            bases per pixel
	 * @param bppLimit limit of bpp
	 * @param qualLimit
	 *            Qual limit
	 * @param filterLimit
	 *            Filter Limit
	 */
	public Variants(String id, String subId, Document doc, String mode,
			double bpp, double bppLimit, float qualLimit, String[] filterLimit) {
		this.doc = doc;
		this.bpp = bpp;
		this.isModeDENSE = mode.equals(MODE_DENSE);
		this.outputLetter = !isModeDENSE || bpp < bppLimit;
		ele = doc.createElement(XML_TAG_VARIANTS);
		ele.setAttribute(XML_TAG_ID, id);
		if (subId != null)
			ele.setAttribute(XML_TAG_SUBID, subId);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(ele);
	}

	/**
	 * Add some variantions. Maybe one, maybe many, maybe zero from vcf.
	 * 
	 * @param vcf
	 * @param index
	 *            Index of SAMPLEs selected. For DBSnp, it doesn't work
	 */
	public void addVariant(Vcf vcf, Variant[] vs) {
		for (Variant v : vs) {
			if (isModeDENSE && v.getTo() - lastpos < bpp)
				continue;
			v.write2xml(doc, ele, outputLetter);
			lastpos = v.getTo();
		}
	}

	public Element getVariantsElement() {
		return ele;
	}
}
