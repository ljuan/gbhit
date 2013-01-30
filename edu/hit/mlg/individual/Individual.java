package edu.hit.mlg.individual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hit.mlg.individual.vcf.TabixReaderForVCF;
import edu.hit.mlg.individual.vcf.Variant;
import edu.hit.mlg.individual.vcf.Vcf;
import edu.hit.mlg.individual.vcf.Vcf.DBSnpInfo;
import FileReaders.Consts;

/**
 * 
 * @author Chengwu Yan
 * 
 */
class Individual {
	private HashMap<Integer, Variant[]> int2Variants = new HashMap<Integer, Variant[]>();
	private HashMap<Variant, VariantMapToDBSNP> result = null;
	private int variantNumLimit = 1000;

	/**
	 * 
	 * @param variants
	 *            The variants may come from VCF file or GVF file.
	 * @param isVCF
	 *            Whether the variants come from VCF file.
	 */
	Individual(Element variants, boolean isVCF) {
		NodeList nodeList = variants.getChildNodes();
		int nodeNum = nodeList.getLength();
		if (nodeNum > 0 && nodeNum <= variantNumLimit) {
			result = new HashMap<Variant, VariantMapToDBSNP>(nodeNum);
			Variant v = null;
			Variant[] vs = null;
			int from = 0;
			for (int i = 0, num = nodeList.getLength(); i < num; i++) {
				v = node2Variant((Element) nodeList.item(i));
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
	List<VariantMapToDBSNP> merge(String dbsnpURI, String chr, long start,
			long end) {
		if (result == null)
			return null;

		try {
			TabixReaderForVCF tabix = new TabixReaderForVCF(dbsnpURI);
			String chrom = tabix.hasChromPrefix() ? chr : chr.substring(3);
			if ("M".equalsIgnoreCase(chrom)) {
				chrom = "MT";
			}
			TabixReaderForVCF.Iterator Query = tabix.query(chrom + ":" + start
					+ "-" + end);
			Vcf vcf = null;
			Variant variant = null;
			if (Query != null) {
				while (Query.next() != null) {
					vcf = new Vcf(tabix.lineInChars, tabix.numOfChar, 0, null);
					for (Variant v : vcf.getVariants()) {
						if ((variant = variantInMap(v)) != null) {
							result.get(variant).dbsnp = vcf.getDBSnpInfo();
							result.get(variant).dbsnpId = vcf.getID();
						}
					}
				}
			}

		} catch (IOException e) {
			return null;
		}

		List<VariantMapToDBSNP> list = new ArrayList<VariantMapToDBSNP>(
				result.values());
		Collections.sort(list);
		return list;
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

	/**
	 * Convert <code>Node</code> to <code>Variant</code>. We just need
	 * <code>id</code>, <code>type</code>, <code>from</code>, <code>to</code>
	 * and <code>letter</code>.
	 * 
	 * @param node
	 * @return
	 */
	private Variant node2Variant(Element ele) {
		Variant v = new Variant();
		v.setId(ele.getAttribute(Consts.XML_TAG_ID));
		v.setType(ele.getAttribute(Consts.XML_TAG_TYPE));
		NodeList children = ele.getChildNodes();
		v.setFrom(Integer.parseInt(children.item(0).getTextContent()));
		v.setTo(Integer.parseInt(children.item(1).getTextContent()));
		if (children.getLength() > 2) {
			//If the Node has more than 3 children, the third child must be Letter
			Element e = (Element) children.item(2);
			if (e.getTagName().equals(Consts.XML_TAG_LETTER))
				v.setLetter(e.getTextContent());
		}
		return v;
	}

	static class VariantMapToDBSNP implements Comparable<VariantMapToDBSNP> {
		Variant variant;
		DBSnpInfo dbsnp;
		String dbsnpId;

		public VariantMapToDBSNP(Variant variant, DBSnpInfo dbsnp, String dbsnpId) {
			this.variant = variant;
			this.dbsnp = dbsnp;
			this.dbsnpId = dbsnpId;
		}

		@Override
		public int compareTo(VariantMapToDBSNP o) {
			return this.variant.compareTo(o.variant);
		}
	}
}
