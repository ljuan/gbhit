package filereaders.gff;

import java.util.HashMap;
import java.util.Map;

import filereaders.tools.StringSplit;



import static filereaders.Consts.*;

/**
 * @author Chengwu Yan
 */
class GVF extends GFF {
	private static Map<String, String> variantType = new HashMap<String, String>();
	static {
		variantType.put("insertion", VARIANT_TYPE_INSERTION);
		variantType.put("copy_number_variation", VARIANT_TYPE_CNV);
		variantType.put("inversion", VARIANT_TYPE_INVERSION);
		variantType.put("deletion", VARIANT_TYPE_DELETION);
		variantType.put("substitution", VARIANT_TYPE_SNV);
		variantType.put("SNV", VARIANT_TYPE_SNV);
		variantType.put("translocation", VARIANT_TYPE_BLS);
	}

	String id;
	String type;
	String letter = "";
	String[][] variants;
	int variantNum = 0;
	String homo = "";
	String ref = "";
	String phased = null;

	GVF(String[] str) {
		super(str);

		this.type = variantType.get(str[2]);
		variants = new String[0][2];
		resolveVariant_seq(str[8]);
	}

	private void resolveVariant_seq(String str8) {
		StringSplit attrSplit = new StringSplit(';').split(str8);
		StringSplit split = new StringSplit('=');
		String s = null;
		boolean hasReadID = false, hasReadSeq = false, hasReadGenotype = false;
		for (int index = 0, len = attrSplit.getResultNum(); index < len; index++) {
			s = attrSplit.getResultByIndex(index);
			if (s.startsWith("ID=")) {
				this.id = split.split(s).getResultByIndex(1);
				if (hasReadSeq && hasReadGenotype)
					break;
				hasReadID = true;
				continue;
			} else if (s.startsWith("Variant_seq=")) {
				// All variants
				String[] strs = new StringSplit(',').split(split.split(s).getResultByIndex(1)).getResult();
				char c;
				variants = new String[strs.length][2];
				if (type != null) {
					// We can identify this type of variant
					boolean isSNVOrINS = type.equals(VARIANT_TYPE_SNV) || type.equals(VARIANT_TYPE_INSERTION);
					for (String variant : strs) {
						c = variant.charAt(0);
						if (c != '.' && c != '~' && c != '!' && c != '^') {
							variants[variantNum][0] = type;
							variants[variantNum++][1] = isSNVOrINS ? variant : null;
						}
					}
				} else {
					int compResult = 0;
					// We can't identify this type of variant
					for (String variant : strs) {
						c = variant.charAt(0);
						if (c != '.' && c != '~' && c != '!' && c != '^') {
							if (c == '-') {
								// DEL
								variants[variantNum][0] = VARIANT_TYPE_DELETION;
								variants[variantNum++][1] = null;
								continue;
							}
							compResult = end - start + 1 - variant.length();
							if (compResult == 0) {
								// SNV
								variants[variantNum][0] = VARIANT_TYPE_SNV;
								variants[variantNum++][1] = variant;
							} else if (compResult < 0) {
								// INS
								variants[variantNum][0] = VARIANT_TYPE_INSERTION;
								variants[variantNum++][1] = variant;
							} else {
								// DEL
								variants[variantNum][0] = VARIANT_TYPE_DELETION;
								variants[variantNum++][1] = null;
							}
						}
					}
				}

				if (hasReadID && hasReadGenotype)
					break;
				hasReadSeq = true;
				continue;
			} else if (s.startsWith("Genotype=")) {
				this.homo = split.split(s).getResultByIndex(1);
			} else if (s.startsWith("phased=")) {
				this.phased = split.split(s).getResultByIndex(1);
			} else if (s.startsWith("Reference_seq=")) {
				this.ref = split.split(s).getResultByIndex(1);
			}
		}
	}

	/**
	 * Return all variants from this GVF line.
	 * 
	 * @return
	 */
	String[][] getVariants() {
		if (variants.length == variantNum)
			return variants;
		String[][] dest = new String[variantNum][2];
		for (int index = 0; index < variantNum; index++) {
			dest[index][0] = variants[index][0];
			dest[index][1] = variants[index][1];
		}
		return dest;
	}

	@Override
	public int compareTo(GFF o) {
		return (int) ((this.start != o.start) ? (this.start - o.start)
				: (this.end - o.end));
	}
}
