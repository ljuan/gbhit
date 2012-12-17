package FileReaders.vcf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static FileReaders.Consts.VARIANT_TYPE_SNV;
import static FileReaders.Consts.VARIANT_TYPE_INSERTION;
import static FileReaders.Consts.VARIANT_TYPE_DELETION;
import static FileReaders.Consts.VARIANT_TYPE_CNV;
import static FileReaders.Consts.VARIANT_TYPE_DUPLICATION;
import static FileReaders.Consts.VARIANT_TYPE_INVERSION;
import static FileReaders.Consts.VARIANT_TYPE_BLS;

/**
 * 
 * Variant Call Format (VCF) Format Class. Represent one VCF record.
 * 
 * @author Liran Juan
 * @author Chengwu Yan
 * 
 */
public class Vcf {
	/**
	 * Database in VCF
	 */
	public static final Map<String, Integer> dbSnapInfos_M = new HashMap<String, Integer>();
	/**
	 * Personal genomic VCF
	 */
	public static final Map<String, Integer> PGInfos_M = new HashMap<String, Integer>();
	/**
	 * All variantion types
	 */
	public static final Set<String> variantTypes = new HashSet<String>();
	/**
	 * Chromosomes
	 */
	public static final Map<String, String> chroms = new HashMap<String, String>();

	static {
		dbSnapInfos_M.put("GENEINFO", new Integer(0));
		dbSnapInfos_M.put("SCS", new Integer(1));
		dbSnapInfos_M.put("CLN", new Integer(2));
		dbSnapInfos_M.put("PM", new Integer(3));
		dbSnapInfos_M.put("NSF", new Integer(4));
		dbSnapInfos_M.put("NSM", new Integer(5));
		dbSnapInfos_M.put("NSN", new Integer(6));
		dbSnapInfos_M.put("U3", new Integer(7));
		dbSnapInfos_M.put("U5", new Integer(8));
		dbSnapInfos_M.put("ASS", new Integer(9));
		dbSnapInfos_M.put("DSS", new Integer(10));
		dbSnapInfos_M.put("INT", new Integer(11));
		dbSnapInfos_M.put("R3", new Integer(12));
		dbSnapInfos_M.put("R5", new Integer(13));

		PGInfos_M.put("IMPRECISE", new Integer(0));
		PGInfos_M.put("END", new Integer(1));
		PGInfos_M.put("SVLEN", new Integer(2));

		variantTypes.add(VARIANT_TYPE_SNV);
		variantTypes.add(VARIANT_TYPE_INSERTION);
		variantTypes.add(VARIANT_TYPE_DELETION);
		variantTypes.add(VARIANT_TYPE_CNV);
		variantTypes.add(VARIANT_TYPE_DUPLICATION);
		variantTypes.add(VARIANT_TYPE_INVERSION);
		variantTypes.add(VARIANT_TYPE_BLS);

		chroms.put("x", "chrX");
		chroms.put("X", "chrX");
		chroms.put("y", "chrY");
		chroms.put("Y", "chrY");
		chroms.put("m", "chrM");
		chroms.put("M", "chrM");
		chroms.put("mt", "chrM");
		chroms.put("MT", "chrM");
		for (int i = 1; i <= 23; i++) {
			chroms.put(i + "", "chr" + i);
			chroms.put("chr" + i, "chr" + i);
		}
	}

	private String Chr = null;
	private int Pos;
	private String ID = null;
	private String Ref = null;
	private String Alt = null;
	private float Qual;
	private String Filter = null;
	private Set<String> FilterSet = null;
	private String Info = null;
	private VCFRecordSamples samples = null;
	private boolean whetherAltIsDot = false;

	/**
	 * A VCF record may contain zero, one or many variantions.
	 */
	private Variant[] variants;
	private DBSnapInfo dbSnapInfo = null;
	private PGInfo pgInfo = null;

	/**
	 * 
	 * @param vcf
	 *            VCF line
	 * @param samplenum
	 *            Number of SAMPLEs defined int the header lines. If the track
	 *            is DBSnp, samplenum=0
	 * @param samplesFilter
	 *            Samples filter. Only sample whose index in samples exists in
	 *            samplesFilter will leave. If the track is DBSnp,
	 *            samplesFilter=null
	 */
	public Vcf(String vcf, int samplenum, int[] samplesFilter) {
		String[] temp = resolveVCFLine(vcf, samplenum, samplesFilter);
		Chr = (temp[0].startsWith("chr") ? "" : "chr") + temp[0];
		Pos = getIntValue(temp[1]);
		ID = temp[2];
		Ref = temp[3];
		Alt = temp[4];
		whetherAltIsDot = '.' == Alt.charAt(0);
		// For NO Variation, Alt is dot ‘.’. Wo should filter this vcf
		if (whetherAltIsDot)
			return;
		Qual = ".".equals(temp[5]) ? -1 : Float.parseFloat(temp[5]);

		Filter = temp[6];
		FilterSet = new HashSet<String>();

		for (String s : splitVCFByChar(temp[6], ','))
			FilterSet.add(s);

		Info = temp[7];
		if (samplenum > 0) {
			samples = new VCFRecordSamples(temp);
		}
		resolveInfo();
		resolveAlt();
	}

	private String[] resolveVCFLine(String vcf, int samplenum,
			int[] samplesFilter) {
		int filterLen = 0;
		if (samplesFilter != null)
			filterLen = samplesFilter.length;
		String[] temp = new String[samplenum == 0 ? 8 : 9 + filterLen];
		char[] cs = vcf.toCharArray();
		int len = cs.length;
		int n = 0;
		int i = 0;
		int baseNum = samplenum == 0 ? 8 : 9;

		char[] tempCs = new char[300];
		int count = 0;
		int capacity = 300;

		for (; i < len && n < baseNum; i++) {
			if ('\t' == cs[i]) {
				temp[n++] = new String(tempCs, 0, count);
				count = 0;
				continue;
			}
			if (count == capacity) {
				tempCs = expandCapacity(tempCs, capacity);
				capacity += capacity;
			}
			tempCs[count++] = cs[i];
		}
		if (samplenum == 0) {
			temp[n] = new String(tempCs, 0, count);
		} else {
			int sIndex = 0;
			int nMinus9 = n - 9;
			boolean skip = nMinus9 < filterLen
					&& sIndex != samplesFilter[nMinus9];
			for (; i < len; i++) {
				if (skip) {
					for (; i < len; i++) {
						if ('\t' == cs[i]) {
							sIndex++;
							skip = nMinus9 < filterLen
									&& sIndex != samplesFilter[nMinus9];
							break;
						}
					}
					continue;
				}
				if ('\t' == cs[i]) {
					if (nMinus9 < filterLen) {
						temp[n++] = new String(tempCs, 0, count);
						nMinus9 = n - 9;
					}
					count = 0;
					sIndex++;
					skip = nMinus9 < filterLen
							&& sIndex != samplesFilter[nMinus9];
					continue;
				}
				if (count == capacity) {
					tempCs = expandCapacity(tempCs, capacity);
					capacity += capacity;
				}
				tempCs[count++] = cs[i];
			}
			if (nMinus9 < filterLen && sIndex == samplesFilter[nMinus9])
				temp[n] = new String(tempCs, 0, count);
		}

		return temp;
	}

	private char[] expandCapacity(char[] src, int len) {
		char[] dest = new char[len + len];
		System.arraycopy(src, 0, dest, 0, len);
		return dest;
	}

	private String[] expandCapacity(String[] src, int len) {
		String[] dest = new String[len + len];
		System.arraycopy(src, 0, dest, 0, len);
		return dest;
	}

	private String[] splitVCFByChar(String vcf, int length, char c) {
		String[] strs = new String[length];
		char[] cs = vcf.toCharArray();
		int len = cs.length;
		int n = 0;

		char[] tempCs = new char[300];
		int count = 0;
		int capacity = 300;

		for (int i = 0; i < len; i++) {
			if (c == cs[i]) {
				strs[n++] = new String(tempCs, 0, count);
				count = 0;
				continue;
			}
			if (count == capacity) {
				tempCs = expandCapacity(tempCs, capacity);
				capacity += capacity;
			}
			tempCs[count++] = cs[i];
		}
		strs[n] = new String(tempCs, 0, count);

		return strs;
	}

	private String[] splitVCFByChar(String vcf, char c) {
		char[] cs = vcf.toCharArray();
		int len = cs.length;

		String[] strs = new String[100];
		int strCount = 0;
		int strLen = 100;

		char[] tempCs = new char[100];
		int count = 0;
		int capacity = 100;

		for (int i = 0; i < len; i++) {
			if (c == cs[i]) {
				if (strCount == strLen) {
					strs = expandCapacity(strs, strLen);
					strLen += strLen;
				}
				strs[strCount++] = new String(tempCs, 0, count);
				count = 0;
				continue;
			}
			if (count == capacity) {
				tempCs = expandCapacity(tempCs, capacity);
				capacity += capacity;
			}
			tempCs[count++] = cs[i];
		}
		if (strCount == strLen) {
			strs = expandCapacity(strs, strLen);
			strLen += strLen;
		}
		strs[strCount++] = new String(tempCs, 0, count);

		String[] dest = new String[strCount];
		System.arraycopy(strs, 0, dest, 0, strCount);

		return dest;
	}

	// ///////////////////////////////////////////////////////////resolve INFO
	/**
	 * Resolve the Info. Each key-value is seperate by semicolon.<br />
	 * 
	 */
	public void resolveInfo() {
		if ('.' == Info.charAt(0))
			return;
		String[] many = splitVCFByChar(Info, ';');
		Integer index = null;
		String[] keyValue = null;
		char firstChar;
		if (samples != null) {
			// Personal Genemic VCF
			pgInfo = new PGInfo();
			for (String one : many) {
				keyValue = splitVCFByChar(one, 2, '=');
				if ((index = PGInfos_M.get(keyValue[0])) != null)
					pgInfo.pgInfos[index] = true;

				firstChar = keyValue[0].charAt(0);
				if ('E' == firstChar) {
					if ("END".equals(keyValue[0])) {
						pgInfo.end = getIntValue(keyValue[1]);
					}
				} else if ('S' == firstChar) {
					if ("SVLEN".equals(keyValue[0])) {
						pgInfo.svlen = getIntValue(keyValue[1]);
					}
				}
			}
		} else {
			// DBSnap
			dbSnapInfo = new DBSnapInfo();
			for (String one : many) {
				keyValue = splitVCFByChar(one, 2, '=');
				if ((index = dbSnapInfos_M.get(keyValue[0])) != null) {
					dbSnapInfo.dbSnapInfos[index] = true;

					firstChar = keyValue[0].charAt(0);
					if ('G' == firstChar) {
						if ("GENEINFO".equals(keyValue[0])) {
							dbSnapInfo.GENEINFO = keyValue[1];
						}
					} else if ('S' == firstChar) {
						if ("SCS".equals(keyValue[0])) {
							dbSnapInfo.SCS = keyValue[1];
						}
					}
				}
			}
		}
	}

	/**
	 * We can affirm that strValue is a positive integer !
	 * 
	 * @param strValue
	 * @return
	 */
	private static int getIntValue(String strValue) {
		int num = 0;
		char[] cs = strValue.toCharArray();
		for (char c : cs) {
			num = num * 10 + (int) c - 48;
		}
		return num;
	}

	// //////////////////////////////////////////////////end of resolve INFO

	// ///////////////////////////////////////////////////////////resolve Alt
	/**
	 * Resolve Alt to Variant instances, we must call resolveInfo first.
	 */
	private void resolveAlt() {
		// We know that most variantions are SNV
		if (Alt.length() <= 2) {
			resolveSmallVariants(false);
		}
		// <INS> or C<ctg1>, only Personal Genemic VCF will appear.
		if (containChar(Alt, '<')) {
			resolveLtGt();
		}
		// G]17:198982], only Personal Genemic VCF will appear.
		else if (containChar(Alt, ']')) {
			resolveBracket(true);
		} else if (containChar(Alt, '[')) {
			resolveBracket(false);
		} else {
			resolveSmallVariants(true);
		}
	}

	/**
	 * Whether str contains c. We use this function instead of
	 * String.contains(String) because we can ensure that str!=null, and we just
	 * need to find a char. String.contains(String) can't satisfy our high-speed
	 * need.
	 * 
	 * @param str
	 * @param c
	 * @return True if str contains c, false else.
	 */
	public static boolean containChar(String str, char c) {
		int length = str.length();
		for (int i = 0; i < length; i++) {
			if (str.charAt(i) == c)
				return true;
		}
		return false;
	}

	/**
	 * Call this function when Alt appears as &lt;INS&gt; or C&lt;ctg1&gt;
	 */
	private void resolveLtGt() {
		String type = takeOutAltType();

		variants = new Variant[1];
		variants[0] = new Variant();
		Variant v = variants[0];
		v.setId(ID);
		v.setType(type);
		if (VARIANT_TYPE_INSERTION.equals(type)) {
			// big INS
			v.setFrom(Pos);
			v.setTo(Pos + 1);
		} else {
			// big DEL, INV, CNV, OTH, DUP
			v.setFrom(Pos + 1);
			if (-1 != pgInfo.end) {
				v.setTo(pgInfo.end);
			} else {
				v.setTo(Pos + pgInfo.svlen - 1);
			}
		}
	}

	/**
	 * Call this function when Alt appears as G]17:198982];
	 * 
	 * @param isLeft
	 *            True if Alt contains "]", false else.
	 */
	private void resolveBracket(boolean isLeft) {
		String[] fields = null;
		int chrBase = 0;

		variants = new Variant[1];
		variants[0] = new Variant();
		Variant v = variants[0];
		v.setId(ID);
		v.setType(VARIANT_TYPE_BLS);
		v.setFrom(Pos);
		if (isLeft) {
			v.setDirection("]");
			fields = Alt.split("\\]|:");
			if (Alt.charAt(0) != ']')
				chrBase++;
		} else {
			v.setDirection("[");
			fields = Alt.split("\\[|:");
			if (Alt.charAt(0) != '[')
				chrBase++;
		}
		v.setToChr(chroms.get(fields[chrBase++]));
		v.setTo(getIntValue(fields[chrBase]));
	}

	/**
	 * Call this function when Alt appears as A(containComma=false) or
	 * AT(containComma=false) or ATC(containComma=true) or
	 * A,AT,ATC...(containComma=true)<br />
	 * Each variant can only be SNV, small INS or small DEL
	 * 
	 * @param containComma
	 *            If containComma=false, Alt doesn't contain comma; else if
	 *            containComma=true, Alt may contain comma.
	 */
	private void resolveSmallVariants(boolean containComma) {
		String[] many = null;
		String one = null;
		Variant curV = null;

		if (!containComma) {
			many = new String[1];
			many[0] = Alt;
		} else {
			many = splitVCFByChar(Alt, ',');
		}

		variants = new Variant[many.length];
		for (int i = 0; i < variants.length; i++) {
			variants[i] = new Variant();
			curV = variants[i];
			one = many[i];
			curV.setId(ID);
			if (one.length() == Ref.length()) {
				// SNV
				int index = firstNotEqualPos(Ref, one);
				curV.setType(VARIANT_TYPE_SNV);
				curV.setFrom(Pos + index);
				curV.setTo(Pos + index);
				curV.setLetter(one.charAt(index) + "");
			} else if (one.length() < Ref.length()) {
				// small DEL
				curV.setType(VARIANT_TYPE_DELETION);
				curV.setFrom(Pos + one.length());
				curV.setTo(Pos + Ref.length() - 1);
			} else {
				// small INS
				int startBase = firstNotEqualPos(Ref, one);
				int length = one.length() - Ref.length();
				curV.setType(VARIANT_TYPE_INSERTION);
				curV.setFrom(Pos + startBase - 1);
				curV.setTo(Pos + startBase);
				curV.setLetter(new String(one.toCharArray(), startBase, length));
			}
		}
	}

	/**
	 * Get first position=index where s1.charAt(index)!=s2.charAt(index).<br />
	 * <strong>Note: </strong>We Must ensure that both s1 and s2 are not null
	 * and s1.length()<=s2.length() before we call this function.
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int firstNotEqualPos(String s1, String s2) {
		int index = 0;
		int length = s1.length();
		for (; index < length; index++) {
			if (s1.charAt(index) != s2.charAt(index))
				break;
		}

		return index;
	}

	/**
	 * Take out variantion type of the alt.<br />
	 * For example, &lt;INS&gt;, &lt;DUP&gt;, C&lt;ctg1&gt; will take out INS,
	 * DUP, INS separately. <br />
	 * <strong>Note: </strong>We must affirm that Alt appears as &lt;INS&gt; or
	 * C&lt;ctg1&gt;<br />
	 * 
	 * @return
	 */
	private String takeOutAltType() {
		char[] cs = Alt.toCharArray();
		int i = 0;
		int j = 0;
		for (;; i++) {
			if (cs[i] == '<')
				break;
		}
		for (j = i + 1;; j++) {
			if (cs[j] == '>')
				break;
		}
		String type = new String(cs, i + 1, j - i - 1);

		return variantTypes.contains(type) ? type : (i == 0 ? "OTH" : "INS");
	}

	// //////////////////////////////////////////////////end of resolve ALT
	public String getChr() {
		return Chr;
	}

	public String getAlt() {
		return Alt;
	}

	public String getRef() {
		return Ref;
	}

	public long getPos() {
		return Pos;
	}

	public String getID() {
		return ID;
	}

	public float getQual() {
		return Qual;
	}

	public String getFilter() {
		return Filter;
	}

	public String getInfo() {
		return Info;
	}

	/**
	 * Whether the track is DBSnp
	 * 
	 * @return
	 */
	public boolean isDBSnp() {
		return dbSnapInfo != null;
	}

	/**
	 * Whether the Info contains key.<br />
	 * <strong>Note:</strong>Ensure that you had call resolveInfo().
	 * 
	 * @param key
	 * @param dbSnap
	 *            Whether the track is dbSnap
	 * @return
	 */
	public boolean containKey(String key, boolean dbSnap) {
		if (key == null)
			return false;
		Integer index = dbSnap ? (Integer) dbSnapInfos_M.get(key)
				: (Integer) PGInfos_M.get(key);
		return index == null ? false : (dbSnap ? dbSnapInfo.dbSnapInfos[index]
				: pgInfo.pgInfos[index]);
	}

	/**
	 * Call containKey("GENEINFO", true) before call getGENEINFO();
	 * 
	 * @return
	 */
	public String getGENEINFO() {
		return dbSnapInfo.GENEINFO;
	}

	/**
	 * Call containKey("SCS", true) before call getGENEINFO();
	 * 
	 * @return
	 */
	public String getSCS() {
		return dbSnapInfo.SCS;
	}

	/**
	 * Whether this VCF instance should be filtered by FilterLimit.
	 * 
	 * @param filters
	 * @return Return true if this VCF instance should be filtered. False else.
	 */
	public boolean shouldBeFilteredByFilterLimit(String[] filters) {
		if (filters == null || FilterSet.contains("PASS"))
			return false;
		for (String f : filters) {
			if (FilterSet.contains(f))
				return true;
		}
		return false;
	}

	/**
	 * Whether this VCF instance should be filtered by QualLimit.
	 * 
	 * @param filter
	 * @return Return true if this VCF instance should be filtered. False else.
	 */
	public boolean shouldBeFilteredByQualLimit(float filter) {
		return this.Qual < filter;
	}

	/**
	 * For NO Variation, Alt is dot ‘.’. Wo should filter this vcf.
	 * 
	 * @return
	 */
	public boolean whetherAltIsDot() {
		return whetherAltIsDot;
	}

	/**
	 * Get variantions of appointed SAMPLE. We must affirm that the track is
	 * Personal Genemic VCF and index>=0 and index less than number of SAMPLEs
	 * selected.
	 * 
	 * @param index
	 *            Index of SAMPLEs selected
	 * @return
	 */
	public Variant[] getVariants(int index) {
		if (!samples.containGT())
			return variants;
		int[] vIndexes = samples.getVariantIndexes(index);
		if (vIndexes == null)
			return null;
		Variant[] vs = new Variant[variants.length];
		int len = 0;
		for (int vIndex : vIndexes) {
			vs[len++] = variants[vIndex - 1];
		}

		return vs;
	}

	/**
	 * Get all variantions this instance has.
	 * 
	 * @return
	 */
	public Variant[] getVariants() {
		return variants;
	}

	public String getFormat() {
		return samples == null ? null : samples.getFormat();
	}

	public String getSamples() {
		return samples == null ? null : samples.toString();
	}

	private class DBSnapInfo {
		/**
		 * Every element map to dbSnapInfos_M. True represent INFO contains the
		 * key, false else.
		 */
		boolean[] dbSnapInfos = new boolean[14];
		/**
		 * Only when the track is Database in VCF and the INFO contains
		 * "GENEINFO", GENEINFO is effective.
		 */
		private String GENEINFO = null;
		/**
		 * Only when the track is Database VCF and the INFO contains "SCS", SCS
		 * is effective.
		 */
		private String SCS = null;
	}

	private class PGInfo {
		/**
		 * Every element map to PGInfos_M. True represent INFO contains the key,
		 * false else.
		 */
		boolean[] pgInfos = new boolean[3];
		/**
		 * The variant end base
		 */
		int end = -1;
		/**
		 * Number of the variant bases
		 */
		int svlen = -1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.Pos);
		builder.append('\t');
		builder.append(this.ID);
		builder.append('\t');
		builder.append(this.Ref);
		builder.append('\t');
		builder.append(this.Alt);
		builder.append('\t');
		builder.append(this.Qual);
		builder.append('\t');
		builder.append(this.Filter);
		builder.append('\t');
		builder.append(this.Info);
		builder.append('\t');
		if (pgInfo != null) {
			builder.append(this.getFormat());
			builder.append('\t');
			builder.append(this.getSamples());
		}

		return builder.toString();
	}
}