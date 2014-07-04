package filereaders.individual.vcf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import filereaders.tools.StringSplit;



import static filereaders.Consts.VARIANT_TYPE_BLS;
import static filereaders.Consts.VARIANT_TYPE_CNV;
import static filereaders.Consts.VARIANT_TYPE_DELETION;
import static filereaders.Consts.VARIANT_TYPE_DUPLICATION;
import static filereaders.Consts.VARIANT_TYPE_INSERTION;
import static filereaders.Consts.VARIANT_TYPE_INVERSION;
import static filereaders.Consts.VARIANT_TYPE_MULTIPLE;
import static filereaders.Consts.VARIANT_TYPE_SNV;

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
	public static final Map<String, Integer> dbSnpInfos_M = new HashMap<String, Integer>();
	/**
	 * Personal genomic VCF
	 */
	public static final Map<String, Integer> PGInfos_M = new HashMap<String, Integer>();
	/**
	 * All variantion types
	 */
	public static final Map<String, String> variantTypes = new HashMap<String, String>();
	/**
	 * Chromosomes
	 */
	public static final Map<String, String> chroms = new HashMap<String, String>();

	static {
		dbSnpInfos_M.put("GENEINFO", new Integer(0));
		dbSnpInfos_M.put("SCS", new Integer(1));
		dbSnpInfos_M.put("CLN", new Integer(2));
		dbSnpInfos_M.put("PM", new Integer(3));
		dbSnpInfos_M.put("NSF", new Integer(4));
		dbSnpInfos_M.put("NSM", new Integer(5));
		dbSnpInfos_M.put("NSN", new Integer(6));
		dbSnpInfos_M.put("U3", new Integer(7));
		dbSnpInfos_M.put("U5", new Integer(8));
		dbSnpInfos_M.put("ASS", new Integer(9));
		dbSnpInfos_M.put("DSS", new Integer(10));
		dbSnpInfos_M.put("INT", new Integer(11));
		dbSnpInfos_M.put("R3", new Integer(12));
		dbSnpInfos_M.put("R5", new Integer(13));
		dbSnpInfos_M.put("REF", new Integer(14));
		dbSnpInfos_M.put("SYN", new Integer(15));
		dbSnpInfos_M.put("OTH", new Integer(16));

		PGInfos_M.put("IMPRECISE", new Integer(0));
		PGInfos_M.put("END", new Integer(1));
		PGInfos_M.put("SVLEN", new Integer(2));
		PGInfos_M.put("MINAF", new Integer(3));

		variantTypes.put(VARIANT_TYPE_SNV, VARIANT_TYPE_SNV);
		variantTypes.put(VARIANT_TYPE_INSERTION, VARIANT_TYPE_INSERTION);
		variantTypes.put(VARIANT_TYPE_DELETION, VARIANT_TYPE_DELETION);
		variantTypes.put(VARIANT_TYPE_CNV, VARIANT_TYPE_CNV);
		variantTypes.put(VARIANT_TYPE_DUPLICATION, VARIANT_TYPE_DUPLICATION);
		variantTypes.put(VARIANT_TYPE_INVERSION, VARIANT_TYPE_INVERSION);
		variantTypes.put(VARIANT_TYPE_BLS, VARIANT_TYPE_BLS);
		variantTypes.put(VARIANT_TYPE_MULTIPLE, VARIANT_TYPE_MULTIPLE);

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
	private DBSnpInfo dbSnpInfo = null;
	private PGInfo pgInfo = null;

	/**
	 * 
	 * @param vcfInCharArray
	 *            VCF line in char array
	 * @param lenOfCharArray
	 *            effective number of char of vcfInCharArray
	 * @param samplenum
	 *            Number of SAMPLEs defined int the header lines. If the track
	 *            is DBSnp, samplenum=0, else samplenum>0
	 * @param samplesFilter
	 *            Samples filter. Only sample whose index in samples exists in
	 *            samplesFilter will leave. If the track is DBSnp,
	 *            samplesFilter=null
	 */
	public Vcf(char[] vcfInCharArray, int lenOfCharArray, int samplenum, int[] samplesFilter) {
		init(vcfInCharArray, lenOfCharArray, samplenum, samplesFilter,false);
	}
	public Vcf(char[] vcfInCharArray, int lenOfCharArray, int samplenum, int[] samplesFilter, boolean isResolvInfo) {
		init(vcfInCharArray, lenOfCharArray, samplenum, samplesFilter,isResolvInfo);
	}
	void init(char[] vcfInCharArray, int lenOfCharArray, int samplenum, int[] samplesFilter, boolean isResolvInfo) {
		String[] temp = resolveVCFLine(vcfInCharArray, lenOfCharArray, samplenum, samplesFilter);
		Chr = (temp[0].startsWith("chr") ? "" : "chr") + temp[0];
		Pos = getIntValue(temp[1]);
		ID = temp[2];
		Ref = temp[3];
		Alt = temp[4];
		whetherAltIsDot = '.' == Alt.charAt(0);
		// For NO Variation, Alt is dot ‘.’. Wo should filter this vcf
		if (whetherAltIsDot)
			return;
		Qual = ".".equals(temp[5]) ? 0 : Float.parseFloat(temp[5]);

		Filter = temp[6];
		FilterSet = new HashSet<String>();

		StringSplit split = new StringSplit(',');
		split.split(temp[6]);
		if(!Filter.equals("."))
			for(int index=0, len=split.getResultNum(); index<len; index++)
				FilterSet.add(split.getResultByIndex(index));

		Info = temp[7];
		if (samplesFilter != null) {
			samples = new VCFRecordSamples(temp);
		}
		if(isResolvInfo)
			resolveInfo(samplenum == 0);
		
		resolveAlt();
	}

	private String[] resolveVCFLine(char[] cs, int len, int samplenum,
			int[] samplesFilter) {
		int filterLen = 0;
		boolean sampleFilterIsNull = samplesFilter == null;
		if (!sampleFilterIsNull)
			filterLen = samplesFilter.length;
		String[] temp = new String[sampleFilterIsNull ? 8 : 9 + filterLen];
		int n = 0;
		int i = 0;
		int baseNum = sampleFilterIsNull ? 8 : 9;

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
		if (sampleFilterIsNull) {
			if (i == len)
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
							skip = sIndex != samplesFilter[nMinus9];
							break;
						}
					}
					continue;
				}
				if ('\t' == cs[i]) {
					temp[n++] = new String(tempCs, 0, count);
					nMinus9 = n - 9;
					if (nMinus9 >= filterLen)
						break;

					count = 0;
					sIndex++;
					skip = sIndex != samplesFilter[nMinus9];
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

	// ///////////////////////////////////////////////////////////resolve INFO
	/**
	 * Resolve the Info. Each key-value is seperate by semicolon.<br />
	 * 
	 * @param samplesNumEQ0
	 *            Whether samplesNum==0
	 */
	public void resolveInfo(boolean samplesNumEQ0) {
		if ('.' == Info.charAt(0))
			return;
		String[] many = new StringSplit(';').split(Info).getResult();
		Integer index = null;
		String[] keyValue = null;
		char firstChar;
		StringSplit split = new StringSplit('=');
		// Personal Genemic VCF
		pgInfo = new PGInfo();
		for (String one : many) {
			keyValue = split.split(one).getResult();
			if ((index = PGInfos_M.get(keyValue[0])) != null)
				pgInfo.pgInfos[index] = true;

			firstChar = keyValue[0].charAt(0);
			char lastChar1 =keyValue[0].charAt(keyValue[0].length()-1);
			char lastChar2 =keyValue[0].charAt(keyValue[0].length()-2);
			if ('E' == firstChar) {
				if ("END".equals(keyValue[0])) {
					pgInfo.end = getIntValue(keyValue[1]);
				}
			} else if ('S' == firstChar) {
				if ("SVLEN".equals(keyValue[0])) {
					pgInfo.svlen = getIntValue(keyValue[1]);
				}
			} else if ('F' == lastChar1 && 'A' == lastChar2) {
				if(keyValue.length>1){
					pgInfo.pgInfos[3] = true;
					String[] afs = keyValue[1].split(",");
					for(int i = 0 ; i < afs.length ; i++)
						pgInfo.maxAF = pgInfo.maxAF<Float.parseFloat(afs[i])?Float.parseFloat(afs[i]):pgInfo.maxAF;
				}
			}
		}

		if (samplesNumEQ0) { //samplesNumEQ0==0
			// DBSnp
			dbSnpInfo = new DBSnpInfo();
			for (String one : many) {
				keyValue = split.split(one).getResult();
				if ((index = dbSnpInfos_M.get(keyValue[0])) != null) {
					dbSnpInfo.dbSnpInfos[index] = true;

					firstChar = keyValue[0].charAt(0);
					if ('G' == firstChar) {
						if ("GENEINFO".equals(keyValue[0])) {
							dbSnpInfo.GENEINFO = keyValue[1];
						}
					} else if ('S' == firstChar) {
						if ("SCS".equals(keyValue[0])) {
							dbSnpInfo.SCS = keyValue[1];
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
			if (pgInfo != null) {
				if (-1 != pgInfo.end) {
					v.setTo(pgInfo.end);
				} else {
					v.setTo(Pos + pgInfo.svlen - 1);
				}
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
		variants = new Variant[1];
		variants[0] = new Variant();
		Variant v = variants[0];
		v.setId(ID);
		v.setType(VARIANT_TYPE_BLS);
		v.setFrom(Pos);
		v.setTo(Pos);
		v.setToChr(Alt);
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
			many = new StringSplit(',').split(Alt).getResult();
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

		return variantTypes.containsKey(type) ? type : (i == 0 ? "OTH" : "INS");
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
		return dbSnpInfo != null;
	}

	/**
	 * Whether the Info contains key.<br />
	 * <strong>Note:</strong>
	 * 
	 * @param key
	 * @param dbSnp
	 *            Whether the track is dbSnp
	 * @return
	 */
	public boolean containKey(String key, boolean dbSnp) {
		if (key == null || (dbSnp && dbSnpInfo == null)
				|| (!dbSnp && pgInfo == null))
			return false;
		Integer index = dbSnp ? (Integer) dbSnpInfos_M.get(key)
				: (Integer) PGInfos_M.get(key);
		return index == null ? false : (dbSnp ? dbSnpInfo.dbSnpInfos[index]
				: pgInfo.pgInfos[index]);
	}

	/**
	 * @return
	 */
	public String getGENEINFO() {
		return dbSnpInfo == null ? null : dbSnpInfo.GENEINFO;
	}

	/**
	 * Call containKey("SCS", true) before call getGENEINFO();
	 * 
	 * @return
	 */
	public String getSCS() {
		return dbSnpInfo == null ? null : dbSnpInfo.SCS;
	}
	public float getMaxAF(){
		return pgInfo == null ? null : pgInfo.maxAF;
	}

	public DBSnpInfo getDBSnpInfo() {
		return dbSnpInfo == null ? null : dbSnpInfo;
	}

	/**
	 * Whether this VCF instance should be filtered by FilterLimit.
	 * 
	 * @param filters
	 * @return Return true if this VCF instance should be filtered. False else.
	 */
	public boolean shouldBeFilteredByFilterLimit(String[] filters) {
		if (filters == null)// || FilterSet.contains("PASS")) --By Liran
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
	public boolean altIsDot() {
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
		Variant[] vs = new Variant[vIndexes.length];
		int len = 0;

		for (int vIndex : vIndexes) {
			vs[len] = variants[vIndex - 1];
			vs[len++].setHomo(samples.getHome(index));
		}
		
		if (len > 1) {
			Arrays.sort(vs);
		}

		return vs;
	}
	public Variant[][] getVariants_intersection(int indexlen) {
		if (!samples.containGT()){
			Variant[][] vs=new Variant[1][];
			vs[0]=variants;
			return vs;
		}
		int[] votes={0,0,0,0,0,0,0,0,0,0};
		
		for(int i=0;i<indexlen;i++){
			int[] variantIndexes = samples.getVariantIndexes(i);
			if(variantIndexes == null)
				continue;
			for(int j : variantIndexes)
				votes[j]++;
		}
		
		int num=0;
		for(int i=0;i<10;i++)
			if(votes[i]==indexlen)
				num++;
		if (num==0)
			return null;
		int[] vIndexes = new int[num];
		num=0;
		for(int i=0;i<10;i++)
			if(votes[i]==indexlen)
				vIndexes[num++]=i;
		Variant[][] vs = new Variant[indexlen][];
		for(int i=0;i<indexlen;i++)
			vs[i]=new Variant[vIndexes.length];
		
		int len = 0;

		for (int vIndex : vIndexes) {
			for(int i=0;i<indexlen;i++){
				vs[i][len] = Variant.copy(variants[vIndex - 1]);
				vs[i][len].setHomo(samples.getHome(i));
			}
			len++;
		}
		if (len > 1) 
			for(int i=0;i<indexlen;i++)
				Arrays.sort(vs[i]);

		return vs;
	}
	public Variant[][] getVariants_trio(int o, int f, int m) {
		if (!samples.containGT())
			return null;
		
		int[] vIndexeso = samples.getVariantIndexes(o);
		int[] vIndexesf = samples.getVariantIndexes(f);
		int[] vIndexesm = samples.getVariantIndexes(m);
		
		String ohomo = samples.getHome(o); 
		String fhomo = samples.getHome(f);
		String mhomo = samples.getHome(m);

		
		Variant[][] vs = new Variant[3][];
		vs[0]=null;vs[1]=null;vs[2]=null;
		if(vIndexeso != null && vIndexesf == null && vIndexesm == null){
			vs[o]=new Variant[vIndexeso.length];
			int len = 0;
			for (int vIndex : vIndexeso) {
				vs[o][len] = variants[vIndex - 1];
				vs[o][len].setHomo(samples.getHome(o));
				vs[o][len].setMaxAF(getMaxAF());
				len++;
			}
			if (len > 1) 
				Arrays.sort(vs[o]);
		}
		else if(vIndexeso != null && vIndexesf != null && vIndexesm == null){
			vs[f]=new Variant[vIndexesf.length];
			int len = 0;
			for (int vIndex : vIndexesf) {
				vs[f][len] = variants[vIndex - 1];
				vs[f][len].setHomo(samples.getHome(f));
				vs[f][len].setMaxAF(getMaxAF());
				len++;
			}
			if (len > 1) 
				Arrays.sort(vs[f]);
		}
		else if(vIndexeso != null && vIndexesf == null && vIndexesm != null){
			vs[m]=new Variant[vIndexesm.length];
			int len = 0;
			for (int vIndex : vIndexesm) {
				vs[m][len] = variants[vIndex - 1];
				vs[m][len].setHomo(samples.getHome(m));
				vs[m][len].setMaxAF(getMaxAF());
				len++;
			}
			if (len > 1) 
				Arrays.sort(vs[m]);
		}
	/*	else if(vIndexeso != null && vIndexesf != null && vIndexesm != null){
			boolean om = false;
			boolean of = false;
			if(mhomo.indexOf("0")<0 && ohomo.indexOf("0")>=0)
				om = true;
			else if(fhomo.indexOf("0")<0 && ohomo.indexOf("0")>=0)
				of = true;
			if(om){
				vs[m]=new Variant[vIndexesm.length];
				int len = 0;
				for (int vIndex : vIndexesm) {
					vs[m][len] = variants[vIndex - 1];
					vs[m][len].setHomo(samples.getHome(m));
					vs[m][len].setMaxAF(getMaxAF());
					len++;
				}
				if (len > 1) 
					Arrays.sort(vs[m]);
			}
			else if(of){
				vs[f]=new Variant[vIndexesf.length];
				int len = 0;
				for (int vIndex : vIndexesf) {
					vs[f][len] = variants[vIndex - 1];
					vs[f][len].setHomo(samples.getHome(f));
					vs[f][len].setMaxAF(getMaxAF());
					len++;
				}
				if (len > 1) 
					Arrays.sort(vs[f]);
			}
			else
				return null;
		}
	*/	else
			return null;

		return vs;
	}
	public Variant[][] getVariants_difference(int[] indexes_a,int[] indexes_b) {
		if (!samples.containGT()){
			Variant[][] vs=new Variant[1][];
			vs[0]=variants;
			return vs;
		}
		boolean[] votes_a={true,true,true,true,true,true,true,true,true,true};
		int[] votes_b={0,0,0,0,0,0,0,0,0,0};
		
		for(int i=0;i<indexes_a.length;i++){
			int[] variantIndexes = samples.getVariantIndexes(indexes_a[i]);
			if(variantIndexes == null)
				continue;
			for(int j : variantIndexes)
				votes_a[j]=false;
		}
		for(int i=0;i<indexes_b.length;i++){
			int[] variantIndexes = samples.getVariantIndexes(indexes_b[i]);
			if(variantIndexes == null)
				continue;
			for(int j : variantIndexes)
				votes_b[j]++;
		}
		int num=0;
		for(int i=0;i<10;i++)
			if(votes_a[i]&&votes_b[i]==indexes_b.length)
				num++;
		if (num==0)
			return null;
		int[] vIndexes = new int[num];
		num=0;
		for(int i=0;i<10;i++)
			if(votes_a[i]&&votes_b[i]==indexes_b.length)
				vIndexes[num++]=i;
		
		int[] indexes = new int[indexes_a.length+indexes_b.length];
		System.arraycopy(indexes_a, 0, indexes, 0, indexes_a.length);
		System.arraycopy(indexes_b, 0, indexes, indexes_a.length, indexes_b.length);
		
		Variant[][] vs = new Variant[indexes.length][];
		for(int i=0;i<indexes.length;i++)
			vs[i]=new Variant[vIndexes.length];
		
		int len = 0;

		for (int vIndex : vIndexes) {
			for(int i=0;i<indexes.length;i++){
				vs[i][len] = Variant.copy(variants[vIndex - 1]);
				vs[i][len].setHomo(samples.getHome(indexes[i]));
			}
			len++;
		}
		if (len > 1) 
			for(int i=0;i<indexes.length;i++)
				Arrays.sort(vs[i]);

		return vs;
	}

	/**
	 * Get all variantions this instance has.
	 * 
	 * @return
	 */
	public Variant[] getVariants() {
		if (variants == null || variants.length == 1)
			return variants;
		Variant[] vs = (Variant[]) variants.clone();
		Arrays.sort(vs);
		return vs;
	}

	public String getDetail() {
		StringBuilder description = new StringBuilder();
//		description.append("REF:").append(this.Ref).append(";QUAL:").append(this.Qual).append(";FILTER:")
		description.append("QUAL:").append(this.Qual).append(";FILTER:")
				.append(this.Filter).append(";").append(this.Info);

		return description.toString();
	}
	public String getSampleInfo(int selectedIndex){
		String[] sampleFormat = samples.getFormat().split(":");
		String[] sampleContent = samples.getSample(selectedIndex).split(":");
		StringBuilder sampleInfo = new StringBuilder();
		for(int i=0;i<sampleFormat.length;i++)
			sampleInfo.append(sampleFormat[i]+":"+sampleContent[i]+";");
		
		return sampleInfo.toString();
	}
}
