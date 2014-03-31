package filereaders.individual.vcf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static filereaders.Consts.*;

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
	 /**
	  * Whether mode equals "DETAIL"
	  */
	 private boolean isModeDETAIL = false;

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
		this.isModeDETAIL = mode.equals(MODE_DETAIL);
		this.outputLetter = !isModeDENSE || bpp < bppLimit;
		ele = doc.createElement(XML_TAG_VARIANTS);
		ele.setAttribute(XML_TAG_ID, id);
		if (subId != null){
			ele.setAttribute(XML_TAG_ID, subId);
			ele.setAttribute(XML_TAG_SUPERID, id);
		}
		else
			ele.setAttribute(XML_TAG_ID, id); 
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(ele);
	}

	/**
	 * Add some variantions. Maybe one, maybe many, maybe zero from vcf.
	 * 
	 * @param vcf
	 * @param index
	 *            Index of SAMPLEs selected. For DBSnp, it doesn't work
	 */
	public void addVariant(Vcf vcf, int selectedIndex, Variant[] vs) {
		for (Variant v : vs) {
			if (isModeDENSE && v.getTo() - lastpos < bpp)
				continue;
			if (isModeDETAIL) {
				v.setDescription(vcf.getDetail());
				if(selectedIndex >= 0 )
					v.setSampleInfo(vcf.getSampleInfo(selectedIndex));
			}
			v.write2xml(doc, ele, outputLetter);
			lastpos = v.getTo();
		}
	}

	public Element getVariantsElement() {
		return ele;
	}
}