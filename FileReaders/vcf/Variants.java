package FileReaders.vcf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import FileReaders.Consts;

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
	 * // * Whether mode equals "DETAIL"
	 */
	private boolean isModeDETAIL = false;

	private int lastpos = 0;

	/**
	 * 
	 * @param id
	 *            For DBSnp, it is track; For Personal Genemic, it is
	 *            Track_Sample.
	 * @param doc
	 * @param mode
	 * @param bpp
	 *            bases per pixel
	 * @param qualLimit
	 *            Qual limit
	 * @param filterLimit
	 *            Filter Limit
	 */
	public Variants(String id, Document doc, String mode, double bpp,
			float qualLimit, String[] filterLimit) {
		this.doc = doc;
		this.bpp = bpp;
		this.isModeDENSE = mode.equals(Consts.MODE_DENSE);
		this.isModeDETAIL = mode.equals(Consts.MODE_DETAIL);
		this.outputLetter = !isModeDENSE && bpp < 0.5;
		ele = doc.createElement(Consts.XML_TAG_VARIANTS);
		ele.setAttribute(Consts.XML_TAG_ID, id);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(ele);
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
			if (isModeDETAIL) {
				StringBuilder Description = new StringBuilder();
				Description.append("QUAL:");
				Description.append(vcf.getQual());
				Description.append(";FILTER:");
				Description.append(vcf.getFilter());
				Description.append(";INFO:");
				Description.append(vcf.getInfo());
				String samples = null;
				if ((samples = vcf.getSamples()) != null) {
					Description.append(";FORMAT:");
					Description.append(vcf.getFormat());
					Description.append(";SAMPLES:");
					Description.append(samples);
				}
				v.setDescription(Description.toString());
			}
			v.write2xml(doc, ele, outputLetter);
			lastpos = v.getTo();
		}
	}

	public Element getVariantsElement() {
		return ele;
	}
}
