package filereaders.individual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import filereaders.individual.vcf.TabixReaderForVCF;
import filereaders.individual.vcf.Variant;
import filereaders.individual.vcf.Vcf;


import static filereaders.Consts.*;

/**
 * 
 * @author Chengwu Yan
 * 
 */
public class Individual {
	private HashMap<Integer, Variant[]> int2Variants = new HashMap<Integer, Variant[]>();
	private HashMap<Variant, VariantMapToDBSNP> result = null;
	private int variantNumLimit = 1000;
	private boolean isDetail;
	private String id;
	private String superid;
	private String ifParam;

	/**
	 * 
	 * @param variants
	 *            The variants may come from VCF file or GVF file.
	 * @param isDetail whether the <code>variants</code> read by get_detail           
	 */
	public Individual(Element variants, boolean isDetail) {
		this.id = variants.getAttribute(XML_TAG_ID);
		if(variants.hasAttribute(XML_TAG_SUPERID))
			this.superid = variants.getAttribute(XML_TAG_SUPERID);
		this.ifParam = variants.getAttribute(XML_TAG_IFP);
		this.isDetail = isDetail;
		NodeList nodeList = variants.getChildNodes();
		int nodeNum = nodeList.getLength();

		result = new HashMap<Variant, VariantMapToDBSNP>(nodeNum > 0 ? nodeNum : 16);
		Variant v = null;
		Variant[] vs = null;
		int from = 0;
		for (int i = 0, num = nodeList.getLength(); i < num; i++) {
			v = Variant.convertElement2Variant((Element) nodeList.item(i));
			result.put(v, new VariantMapToDBSNP(v, null, null));
			from = v.getFrom();
			vs = int2Variants.get(from);
			if (vs == null) {
				int2Variants.put(from, new Variant[] { v });
			} else {
				int vsLen = vs.length;
				Variant[] newVs = new Variant[vsLen + 1];

				System.arraycopy(vs, 0, newVs, 0, vsLen);
				newVs[vsLen] = v;
				vs = null;
				int2Variants.put(from, newVs);
			}
		}
	}

	/**
	 * 
	 * @param variants
	 *            The variants may come from VCF file or GVF file.
	 */
	public Individual(Element variants) {
		this(variants, false);
	}

	/**
	 * 
	 * @param dbsnpURI
	 *            The DBSNP URI.
	 * @param chr
	 *            Chromosome name.
	 * @param start
	 *            1-base.
	 * @param end
	 *            1-base.
	 * @return Null if no any variant comes from VCF file or GVF file, or number
	 *         of variants greater than <code>variantNumLimit</code>, or
	 *         Exception throwed when reading from file.
	 */
	List<VariantMapToDBSNP> merge(String dbsnpURI, String chr, long start, long end) {
		if(isDetail || (result.size() > 0 && result.size() <= variantNumLimit)){
			TabixReaderForVCF tabix = null;
			try {
				tabix = new TabixReaderForVCF(dbsnpURI);
				String chrom = tabix.hasChromPrefix() ? chr : chr.substring(3);
				if ("M".equalsIgnoreCase(chrom)) {
					chrom = "MT";
				}
				TabixReaderForVCF.Iterator Query = tabix.query(chrom + ":" + start + "-" + end);
				Vcf vcf = null;
				Variant variant = null;
				Variant[] variants = null;
				if (Query != null) {
					while (Query.next() != null) {
						vcf = new Vcf(tabix.lineInChars, tabix.numOfChar, 0, null);
						variants = vcf.getVariants();
						if(variants == null){
							continue;
						}
						for (Variant v : variants) {
							if ((variant = variantInMap(v)) != null) {
								result.get(variant).dbsnp = vcf.getDBSnpInfo();
								result.get(variant).dbsnpId = vcf.getID();
							}
						}
					}
				}
			} catch (IOException e) {
				List<VariantMapToDBSNP> list = new ArrayList<VariantMapToDBSNP>(result.values());
				Collections.sort(list);
				return list;
			} finally{
				if(tabix != null){
					try {
						tabix.TabixReaderClose();
					} catch (IOException e) {
					}
				}
			}
		}
		List<VariantMapToDBSNP> list = new ArrayList<VariantMapToDBSNP>(result.values());
		Collections.sort(list);
		return list;
	}

	/**
	 * Merge variations come from VCF file or GVF file with variations come from DBSNP file.
	 * @param dbsnpURI
	 *            The DBSNP URI.
	 * @param chr
	 *            Chromosome name.
	 * @param start
	 *            1-base.
	 * @param end
	 *            1-base.
	 * @param doc           
	 * @return Null if no any variant comes from VCF file or GVF file, or number
	 *         of variants greater than <code>variantNumLimit</code>, or
	 *         Exception throwed when reading from file.
	 */
	public Element mergeWithDBSNP(String dbsnpURI, String chr, long start, long end, Document doc) {
		List<VariantMapToDBSNP> mergeResult = merge(dbsnpURI, chr, start, end);
		Element variants = doc.createElement(XML_TAG_VARIANTS);
		if(this.id != null && !this.id.isEmpty())
			variants.setAttribute(XML_TAG_ID, this.id);
		if(this.superid != null && !this.superid.isEmpty())
			variants.setAttribute(XML_TAG_SUPERID, this.superid);
		if(this.ifParam != null && !this.ifParam.isEmpty())
			variants.setAttribute(XML_TAG_IFP, this.ifParam);
		for(VariantMapToDBSNP mr : mergeResult)
			mr.write2xml(doc, variants, isDetail);

		return variants;
	}

	private Variant variantInMap(Variant variant) {
		Variant[] vs = int2Variants.get(variant.getFrom());
		if (vs == null)
			return null;
		Variant v = vs[0];
		String letter = null;
		if (v.getTo() == variant.getTo()) {
			letter = v.getLetter();
			if (letter == null) {
				if (variant.getLetter() == null) {
					return v;
				}
			} else {
				if (letter.equals(variant.getLetter())) {
					return v;
				}
			}
		}
		for (int i = 1, num = vs.length; i < num; i++) {
			v = vs[i];
			if (v.getTo() == variant.getTo()) {
				letter = v.getLetter();
				if (letter == null) {
					if (variant.getLetter() == null) {
						return v;
					}
				} else {
					if (letter.equals(variant.getLetter())) {
						return v;
					}
				}
			}
		}
		return null;
	}
}