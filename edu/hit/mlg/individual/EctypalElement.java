package edu.hit.mlg.individual;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hit.mlg.Tools.LinkedArrayList;
import edu.hit.mlg.Tools.LinkedArrayList.Entry;
import edu.hit.mlg.individual.EctypalElements.ControlArea;
import edu.hit.mlg.individual.Individual.VariantMapToDBSNP;
import edu.hit.mlg.individual.vcf.Variant;

import FileReaders.Consts;
import FileReaders.FastaReader;
import FileReaders.XmlWriter;
import static FileReaders.Consts.*;

public class EctypalElement {
	private static final String LARGE_VARIANTION = "LargeVariation";
	private static final int hash_SNV = VARIANT_TYPE_SNV.hashCode();
	private static final int hash_INS = VARIANT_TYPE_INSERTION.hashCode();
	private static final int hash_DEL = VARIANT_TYPE_DELETION.hashCode();
	private static final int hash_INV = VARIANT_TYPE_INVERSION.hashCode();
	private static final int hash_CNV = VARIANT_TYPE_CNV.hashCode();
	private static final int hash_DUP = VARIANT_TYPE_DUPLICATION.hashCode();
	private static final int hash_BLS = VARIANT_TYPE_BLS.hashCode();
	private static final float BOX_LIMIT = 1 / 10.0f;

	/**
	 * <div id="mw-content-text" dir="ltr" class="mw-content-ltr" lang="en">
	 * <p>
	 * The <a href="http://en.wikipedia.org/wiki/Genetic_code"
	 * title="Genetic code">genetic code</a> is traditionally represented as a
	 * RNA codon table due to the biochemical nature of the <a
	 * href="http://en.wikipedia.org/wiki/Translation_%28genetics%29"
	 * title="Translation (genetics)" class="mw-redirect">protein
	 * translation</a> process. However, with the rise of computational biology
	 * and genomics, proteins have become increasingly studied at a genomic
	 * level. As a result, the practice of representing the genetic code as a
	 * <b>DNA codon table</b> has become more popular. The DNA codons in such
	 * tables occur on the <a
	 * href="http://en.wikipedia.org/wiki/Sense_%28molecular_biology%29"
	 * title="Sense (molecular biology)">sense DNA strand</a> and are arranged
	 * in a <a href=
	 * "http://en.wikipedia.org/wiki/Directionality_%28molecular_biology%29"
	 * title="Directionality (molecular biology)">5' → 3' direction</a>.
	 * </p>
	 * 
	 * <pre>
	 * 	    <table class="wikitable">
	 * 		<tbody><tr>
	 * 		<td bgcolor="#FFE75F">nonpolar</td>
	 * 		<td bgcolor="#B3DEC0">polar</td>
	 * 		<td bgcolor="#BBBFE0">basic</td>
	 * 		<td bgcolor="#F8B7D3">acidic</td>
	 * 		<td bgcolor="#B0B0B0">(stop codon)</td>
	 * 		</tr>
	 * 		</tbody></table>
	 * <table class="wikitable">
	 	<caption>Standard genetic code</caption>
		<tbody><tr>
		<th rowspan="2">1st<br>
		base</th>
		<th colspan="8">2nd base</th>
		<th rowspan="2">3rd<br>
		base</th>
		</tr>
		<tr>
		<th colspan="2">T</th>
		<th colspan="2">C</th>
		<th colspan="2">A</th>
		<th colspan="2">G</th>
		</tr>
		<tr>
		<th rowspan="4">T</th>
		<td>TTT</td>
		<td rowspan="2" bgcolor="#FFE75F">(Phe/F) <a href="http://en.wikipedia.org/wiki/Phenylalanine" title="Phenylalanine">Phenylalanine</a></td>
		<td>TCT</td>
		<td rowspan="4" bgcolor="#B3DEC0">(Ser/S) <a href="http://en.wikipedia.org/wiki/Serine" title="Serine">Serine</a></td>
		<td>TAT</td>
		<td rowspan="2" bgcolor="#B3DEC0">(Tyr/Y) <a href="http://en.wikipedia.org/wiki/Tyrosine" title="Tyrosine">Tyrosine</a></td>
		<td>TGT</td>
		<td rowspan="2" bgcolor="#B3DEC0">(Cys/C) <a href="http://en.wikipedia.org/wiki/Cysteine" title="Cysteine">Cysteine</a></td>
		<th>T</th>
		</tr>
		<tr>
		<td>TTC</td>
		<td>TCC</td>
		<td>TAC</td>
		<td>TGC</td>
		<th>C</th>
		</tr>
		<tr>
		<td>TTA</td>
		<td rowspan="6" bgcolor="#FFE75F">(Leu/L) <a href="http://en.wikipedia.org/wiki/Leucine" title="Leucine">Leucine</a></td>
		<td>TCA</td>
		<td>TAA</td>
		<td bgcolor="#B0B0B0"><a href="http://en.wikipedia.org/wiki/Stop_codon" title="Stop codon">Stop</a> (<i>Ochre</i>)</td>
		<td>TGA</td>
		<td bgcolor="#B0B0B0">Stop (<i>Opal</i>)</td>
		<th>A</th>
		</tr>
		<tr>
		<td>TTG</td>
		<td>TCG</td>
		<td>TAG</td>
		<td bgcolor="#B0B0B0">Stop (<i>Amber</i>)</td>
		<td>TGG</td>
		<td bgcolor="#FFE75F">(Trp/W) <a href="http://en.wikipedia.org/wiki/Tryptophan" title="Tryptophan">Tryptophan</a>&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<th>G</th>
		</tr>
		<tr>
		<th rowspan="4">C</th>
		<td>CTT</td>
		<td>CCT</td>
		<td rowspan="4" bgcolor="#FFE75F">(Pro/P) <a href="http://en.wikipedia.org/wiki/Proline" title="Proline">Proline</a></td>
		<td>CAT</td>
		<td rowspan="2" bgcolor="#BBBFE0">(His/H) <a href="http://en.wikipedia.org/wiki/Histidine" title="Histidine">Histidine</a></td>
		<td>CGT</td>
		<td rowspan="4" bgcolor="#BBBFE0">(Arg/R) <a href="http://en.wikipedia.org/wiki/Arginine" title="Arginine">Arginine</a></td>
		<th>T</th>
		</tr>
		<tr>
		<td>CTC</td>
		<td>CCC</td>
		<td>CAC</td>
		<td>CGC</td>
		<th>C</th>
		</tr>
		<tr>
		<td>CTA</td>
		<td>CCA</td>
		<td>CAA</td>
		<td rowspan="2" bgcolor="#B3DEC0">(Gln/Q) <a href="http://en.wikipedia.org/wiki/Glutamine" title="Glutamine">Glutamine</a></td>
		<td>CGA</td>
		<th>A</th>
		</tr>
		<tr>
		<td>CTG</td>
		<td>CCG</td>
		<td>CAG</td>
		<td>CGG</td>
		<th>G</th>
		</tr>
		<tr>
		<th rowspan="4">A</th>
		<td>ATT</td>
		<td rowspan="3" bgcolor="#FFE75F">(Ile/I) <a href="http://en.wikipedia.org/wiki/Isoleucine" title="Isoleucine">Isoleucine</a></td>
		<td>ACT</td>
		<td rowspan="4" bgcolor="#B3DEC0">(Thr/T) <a href="http://en.wikipedia.org/wiki/Threonine" title="Threonine">Threonine</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<td>AAT</td>
		<td rowspan="2" bgcolor="#B3DEC0">(Asn/N) <a href="http://en.wikipedia.org/wiki/Asparagine" title="Asparagine">Asparagine</a></td>
		<td>AGT</td>
		<td rowspan="2" bgcolor="#B3DEC0">(Ser/S) Serine</td>
		<th>T</th>
		</tr>
		<tr>
		<td>ATC</td>
		<td>ACC</td>
		<td>AAC</td>
		<td>AGC</td>
		<th>C</th>
		</tr>
		<tr>
		<td>ATA</td>
		<td>ACA</td>
		<td>AAA</td>
		<td rowspan="2" bgcolor="#BBBFE0">(Lys/K) <a href="http://en.wikipedia.org/wiki/Lysine" title="Lysine">Lysine</a></td>
		<td>AGA</td>
		<td rowspan="2" bgcolor="#BBBFE0">(Arg/R) Arginine</td>
		<th>A</th>
		</tr>
		<tr>
		<td>ATG<sup class="reference" id="ref_methionineA"><a href="#endnote_methionineA">[A]</a></sup></td>
		<td bgcolor="#FFE75F">(Met/M) <a href="http://en.wikipedia.org/wiki/Methionine" title="Methionine">Methionine</a></td>
		<td>ACG</td>
		<td>AAG</td>
		<td>AGG</td>
		<th>G</th>
		</tr>
		<tr>
		<th rowspan="4">G</th>
		<td>GTT</td>
		<td rowspan="4" bgcolor="#FFE75F">(Val/V) <a href="http://en.wikipedia.org/wiki/Valine" title="Valine">Valine</a></td>
		<td>GCT</td>
		<td rowspan="4" bgcolor="#FFE75F">(Ala/A) <a href="http://en.wikipedia.org/wiki/Alanine" title="Alanine">Alanine</a></td>
		<td>GAT</td>
		<td rowspan="2" bgcolor="#F8B7D3">(Asp/D) <a href="http://en.wikipedia.org/wiki/Aspartic_acid" title="Aspartic acid">Aspartic acid</a></td>
		<td>GGT</td>
		<td rowspan="4" bgcolor="#FFE75F">(Gly/G) <a href="http://en.wikipedia.org/wiki/Glycine" title="Glycine">Glycine</a></td>
		<th>T</th>
		</tr>
		<tr>
		<td>GTC</td>
		<td>GCC</td>
		<td>GAC</td>
		<td>GGC</td>
		<th>C</th>
		</tr>
		<tr>
		<td>GTA</td>
		<td>GCA</td>
		<td>GAA</td>
		<td rowspan="2" bgcolor="#F8B7D3">(Glu/E) <a href="http://en.wikipedia.org/wiki/Glutamic_acid" title="Glutamic acid">Glutamic acid</a></td>
		<td>GGA</td>
		<th>A</th>
		</tr>
		<tr>
		<td>GTG</td>
		<td>GCG</td>
		<td>GAG</td>
		<td>GGG</td>
		<th>G</th>
		</tr>
		</tbody>
	 */
	private final static Map<String, Character> standardGeneticCode = new HashMap<String, Character>();
	static {
		standardGeneticCode.put("TTT", 'F');
		standardGeneticCode.put("TTC", 'F');
		standardGeneticCode.put("TTA", 'L');
		standardGeneticCode.put("TTG", 'L');
		standardGeneticCode.put("TCT", 'S');
		standardGeneticCode.put("TCC", 'S');
		standardGeneticCode.put("TCA", 'S');
		standardGeneticCode.put("TCG", 'S');
		standardGeneticCode.put("TAT", 'Y');
		standardGeneticCode.put("TAC", 'Y');
		standardGeneticCode.put("TAA", '$');
		standardGeneticCode.put("TAG", '$');
		standardGeneticCode.put("TGT", 'C');
		standardGeneticCode.put("TGC", 'C');
		standardGeneticCode.put("TGA", '$');
		standardGeneticCode.put("TGG", 'W');
		standardGeneticCode.put("CTT", 'L');
		standardGeneticCode.put("CTC", 'L');
		standardGeneticCode.put("CTA", 'L');
		standardGeneticCode.put("CTG", 'L');
		standardGeneticCode.put("CCT", 'P');
		standardGeneticCode.put("CCC", 'P');
		standardGeneticCode.put("CCA", 'P');
		standardGeneticCode.put("CCG", 'P');
		standardGeneticCode.put("CAT", 'H');
		standardGeneticCode.put("CAC", 'H');
		standardGeneticCode.put("CAA", 'Q');
		standardGeneticCode.put("CAG", 'Q');
		standardGeneticCode.put("CGT", 'R');
		standardGeneticCode.put("CGC", 'R');
		standardGeneticCode.put("CGA", 'R');
		standardGeneticCode.put("CGG", 'R');
		standardGeneticCode.put("ATT", 'L');
		standardGeneticCode.put("ATC", 'L');
		standardGeneticCode.put("ATA", 'L');
		standardGeneticCode.put("ATG", 'M');
		standardGeneticCode.put("ACT", 'T');
		standardGeneticCode.put("ACC", 'T');
		standardGeneticCode.put("ACA", 'T');
		standardGeneticCode.put("ACG", 'T');
		standardGeneticCode.put("AAT", 'N');
		standardGeneticCode.put("AAC", 'N');
		standardGeneticCode.put("AAA", 'K');
		standardGeneticCode.put("AAG", 'K');
		standardGeneticCode.put("AGT", 'S');
		standardGeneticCode.put("AGC", 'S');
		standardGeneticCode.put("AGA", 'R');
		standardGeneticCode.put("AGG", 'R');
		standardGeneticCode.put("GTT", 'V');
		standardGeneticCode.put("GTC", 'V');
		standardGeneticCode.put("GTA", 'V');
		standardGeneticCode.put("GTG", 'V');
		standardGeneticCode.put("GCT", 'A');
		standardGeneticCode.put("GCC", 'A');
		standardGeneticCode.put("GCA", 'A');
		standardGeneticCode.put("GCG", 'A');
		standardGeneticCode.put("GAT", 'D');
		standardGeneticCode.put("GAC", 'D');
		standardGeneticCode.put("GAA", 'E');
		standardGeneticCode.put("GAG", 'E');
		standardGeneticCode.put("GGT", 'G');
		standardGeneticCode.put("GGC", 'G');
		standardGeneticCode.put("GGA", 'G');
		standardGeneticCode.put("GGG", 'G');
	}
	
	private Set<String> status = null;

	private final static Map<String, Map<String, String>> currentNeedToMap = new HashMap<String, Map<String,String>>();
	private final static Map<String, String> currentIsBOX = new HashMap<String, String>();
	private final static Map<String, String> currentIsExtendBOX = new HashMap<String, String>();
	private final static Map<String, String> currentIsSkipBOX = new HashMap<String, String>();
	static{
		currentNeedToMap.put(Consts.SUBELEMENT_TYPE_BOX, currentIsBOX);
		currentNeedToMap.put(Consts.SUBELEMENT_TYPE_EXTEND_BOX, currentIsExtendBOX);
		currentNeedToMap.put(Consts.SUBELEMENT_TYPE_SKIP_BOX, currentIsSkipBOX);
		///////////////////////////
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_BOX, Consts.SUBELEMENT_TYPE_BOX);
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_LOST_BOX, Consts.SUBELEMENT_TYPE_LOST_BOX);
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_EXTEND_BAND, Consts.SUBELEMENT_TYPE_LOST_BOX);
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_SKIP_BAND, Consts.SUBELEMENT_TYPE_LOST_BOX);
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_SHIFT_BOX, Consts.SUBELEMENT_TYPE_SHIFT_BOX);
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_EXTEND_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_SKIP_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_SHIFT_EXTEND_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		currentIsBOX.put(Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX);
		/////////////////////////
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_BOX, Consts.SUBELEMENT_TYPE_EXTEND_BOX);
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_LOST_BOX, Consts.SUBELEMENT_TYPE_EXTEND_BAND);
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_EXTEND_BAND, Consts.SUBELEMENT_TYPE_EXTEND_BAND);
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_SKIP_BAND, Consts.SUBELEMENT_TYPE_EXTEND_BAND);
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_SHIFT_BOX, Consts.SUBELEMENT_TYPE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_EXTEND_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_SKIP_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_SHIFT_EXTEND_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		currentIsExtendBOX.put(Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX, Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX);
		//////////////////////////////
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_BOX, Consts.SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_LOST_BOX, Consts.SUBELEMENT_TYPE_SKIP_BAND);
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_EXTEND_BAND, Consts.SUBELEMENT_TYPE_SKIP_BAND);
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_SKIP_BAND, Consts.SUBELEMENT_TYPE_SKIP_BAND);
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_SHIFT_BOX, Consts.SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_EXTEND_BOX, Consts.SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_SKIP_BOX, Consts.SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX, Consts.SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_SHIFT_EXTEND_BOX, Consts.SUBELEMENT_TYPE_SKIP_BOX);
		currentIsSkipBOX.put(Consts.SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX, Consts.SUBELEMENT_TYPE_SKIP_BOX);
	}
	/**
	 * There is 10 types of SubElement, but only 4 types of them need to deal. And this 4 types of
	 * SubElement are divided into 2 category.
	 */
	private final static Map<String, Integer> currentNeedToDeal = new HashMap<String, Integer>();
	static{
		currentNeedToDeal.put(Consts.SUBELEMENT_TYPE_BOX, 1);
		currentNeedToDeal.put(Consts.SUBELEMENT_TYPE_EXTEND_BOX, 1);
		currentNeedToDeal.put(Consts.SUBELEMENT_TYPE_SHIFT_BOX, 2);
		currentNeedToDeal.put(Consts.SUBELEMENT_TYPE_SHIFT_EXTEND_BOX, 2);
	}
	
	/**
	 * Get base's complementary base
	 */
	private static char[] complementations = new char[] { 'T', 'G', '0', 'C',
			'0', '0', 'N', '0', '0', 'A' };
	/////////////////////////////////////////////////////////The fields this Element has
	private String id = null;// Attribute
	private String type = null;// Attribute
	private int from;// Tag
	private int to;// Tag
	/**
	 * True if the direction of this Element is "+"; false else.
	 */
	private boolean direction;// Tag
	private String description = null;// Tag
	private String color = null;// Tag
	private LinkedArrayList<EctypalSubElement> subEles = null;
	private FastaReader fr;
	/**
	 * Whether this Element still need to do in the current stage.
	 */
	private boolean stillNeedToDeal = true;
	/**
	 * Total bases at all Boxes Initially
	 */
	private int initBoxLen = 0;
	/**
	 * Which chromosome this SubElement at.
	 */
	private String chr;
	/**
	 * The region of initiator and terminator.
	 * If the first Box's first three bases' position are 8,9,10. And the last Box's last three bases' position are 98,99,100.
	 * Now if direction=true, initiatorSmall=8 and initiatorLarge=10 and terminatorSmall=98 and terminatorLarge=100;
	 * If direction=false, initiatorSmall=100 and initiatorLarge=98 and terminatorSmall=10 and terminatorLarge=8.
	 * If initiator=0, there is no any box.
	 * When any INS or DEL variation appear in the initiator or terminator, we should change some of the four fields below.
	 */
	private int initiatorSmall = 0;
	private int initiatorLarge = 0;
	private int terminatorSmall = 0;
	private int terminatorLarge = 0;
	/**
	 * Length of chromosome <code>chr</code>
	 */
	private int chrLen;
	/**
	 * Number of bases at "BOX" current from the first base at "BOX".
	 * The "BOX" is one of <code>Consts.SUBELEMENT_TYPE_BOX</code>, <code>Consts.SUBELEMENT_TYPE_EXTEND_BOX<code>, 
	 * <code>Consts.SUBELEMENT_TYPE_SHIFT_BOX</code> or <code>Consts.SUBELEMENT_TYPE_SHIFT_EXTEND_BOX</code>.
	 */
	private int boxBaseNumFromFirstBoxBase = 0;
	/**
	 * Total number of bases of variations at "BOX" current we have dealed.  
	 */
	private int boxBaseNumDealed = 0;
	/**
	 * The previous SubElement of current SubElement which is not Line and not Band. We need to
	 * record this SubElement because in the third stage of processing, the upstream's SubElement
	 * have an impact on the downstream's Box.
	 * preSubEleNotLineNotBand=null for the initialization.
	 */
	private EctypalSubElement preSubEleNotLineNotBand = null;
	/**
	 * The upstream's variation has an impact on the downstream's variation, so we must record all
	 * the variations dealed in the upstream.
	 */
	private LinkedArrayList<Variant> dealedVariations = null;
	
	public String getId(){
		return id;
	}
	
	void print() {
		System.out.println("---------------Element-------------------");
		if(id != null)
			System.out.println("id=" + id);
		if(type != null)
			System.out.println("type=" + type);
		System.out.println("from=" + from);
		System.out.println("to=" + to);
		System.out.println("direction=" + (direction?"+":"-"));
		if(description != null)
			System.out.println("description=" + description);
		if(color != null)
			System.out.println("color=" + color);
		for(String s : status){
			System.out.println("status:" + s);
		}
		Entry<EctypalSubElement> next = subEles.getFirst();
		while (next != null) {
			next.getElement().print();
			next = subEles.getNext(next);
		}
		System.out.println("---------------End of Element-------------------");
	}

	/**
	 * Create an ectypal Object of <code>node</code>.
	 * 
	 * @param ele
	 * @param fr
	 * @param chr
	 *            Which chromosome this SubElement at.
	 */
	EctypalElement(Element ele, FastaReader fr, String chr) {
		subEles = new LinkedArrayList<EctypalSubElement>();
		status = new HashSet<String>();
		this.fr = fr;
		this.chr = chr;
		this.chrLen = (int) fr.getChromosomeLength(chr);
		this.id = ele.getAttribute(XML_TAG_ID);
		if ("".equals(this.id)) {
			this.id = null;
		}
		this.type = ele.getAttribute(XML_TAG_TYPE);
		if ("".equals(this.type)) {
			this.type = null;
		}
		this.direction = ele.getElementsByTagName(XML_TAG_DIRECTION).item(0).getTextContent().equals("+");
		NodeList nodes = ele.getChildNodes();// All Children
		// The first child must be "From"
		this.from = Integer.parseInt(nodes.item(0).getTextContent());
		// The second child must be "To"
		this.to = Integer.parseInt(nodes.item(1).getTextContent());
		retriveTags(nodes);
	}

	/**
	 * @return Whether this Element still need to deal.
	 */
	public boolean stillNeedToDeal() {
		return stillNeedToDeal;
	}

	private void retriveTags(NodeList nodes) {
		Element e = null;
		String t = null;
		EctypalSubElement ese = null;
		boolean firstBox = true;
		for (int index = 2, len = nodes.getLength(); index < len; index++) {
			e = (Element) nodes.item(index);
			t = e.getTagName();
			if (t.equals(Consts.XML_TAG_SUBELEMENT)) {
				// SubElement
				ese = new EctypalSubElement(e);
				subEles.addLast(ese);
				if (Consts.SUBELEMENT_TYPE_BOX.equals(ese.getType())) {
					initBoxLen += ese.getLength();
					if(direction){
						terminatorSmall = ese.getTo() - 2;
						terminatorLarge = ese.getTo();
					} else{
						initiatorSmall = ese.getTo() - 2;
						initiatorLarge = ese.getTo();
					}
					if(firstBox){
						if(direction){
							initiatorSmall = ese.getFrom();
							initiatorLarge = ese.getFrom() + 2;
						} else{
							terminatorSmall = ese.getFrom();
							terminatorLarge = ese.getFrom() + 2;
						}
						firstBox = false;
					}
				}
			} else if (t.equals(Consts.XML_TAG_DESCRIPTION)) {
				// Description
				this.description = e.getTextContent();
			} else if (t.equals(Consts.XML_TAG_COLOR)) {
				// Color
				this.color = e.getTextContent();
			}
		}
	}

	/**
	 * Deal structural variations and variations at ctrlAreas effect the Element
	 * and variations at ASS and ASS.
	 * 
	 * @param mergeVariants	All the variations need to be dealed.
	 * @throws IOException
	 */
	public void preDeal(List<VariantMapToDBSNP> mergeVariants)
			throws IOException {
		Map<Entry<EctypalSubElement>, String> tempAssDss = new HashMap<Entry<EctypalSubElement>, String>();
		Variant v = null;
		if (direction) {
			// Deal from the first variation
			Entry<EctypalSubElement> next = subEles.getFirst();
			if(next == null){ //No any SubElement
				this.stillNeedToDeal = false;
				return;
			}

			for (int i = 0, num = mergeVariants.size(); i < num; i++) {
				v = mergeVariants.get(i).variant;
				if(v.getTo() < next.getElement().getFrom())
					continue;
				next = dealAVariationPreDeal(v, tempAssDss,	next);
				if (!stillNeedToDeal || next == null)
					break;
			}
		} else {
			// Deal from the last variation
			Entry<EctypalSubElement> pre = subEles.getLast();
			if(pre == null){ //No any SubElement
				this.stillNeedToDeal = false;
				return;
			}
			
			for (int i = mergeVariants.size() - 1; i >= 0; i--) {
				v = mergeVariants.get(i).variant;
				if(v.getFrom() > pre.getElement().getTo())
					continue;
				pre = dealAVariationPreDeal(v, tempAssDss, pre);
				if (!stillNeedToDeal || pre == null)
					break;
			}
		}
		if (stillNeedToDeal){
			for (Entry<EctypalSubElement> ese : tempAssDss.keySet()) {
				ese.getElement().setType(tempAssDss.get(ese));
			}
			if(initBoxLen == 0)
				stillNeedToDeal = false;
		}
	}

	public void deal(List<VariantMapToDBSNP> mergeVariants) throws IOException{
		Variant v = null;
		int typeHash = 0;
		Integer curNeedToDealType = -1;
		dealedVariations = new LinkedArrayList<Variant>();
		if (direction) {
			// direction=true, deal from the first variation
			Entry<EctypalSubElement> next = subEles.getFirst();
			for (int i = 0, num = mergeVariants.size(); i < num; i++) {
				v = mergeVariants.get(i).variant;
				if(v.getTo() < next.getElement().getFrom()) 
					continue;
				if(v.getFrom() > next.getElement().getTo()){
					next = backToVariantFrom(v.getFrom(), next);
					if(next == null) break;
				}
				////////////Now next is the SubElement where v.getFrom() at
				typeHash = v.getType().hashCode();
				curNeedToDealType = currentNeedToDeal.get(next.getElement().getType());
				if(curNeedToDealType != null){ //May need to deal
					if(typeHash == hash_SNV){
						if(curNeedToDealType == 1)
							next = dealASNV(next, v);
					}else if(typeHash == hash_INS){
						
					}else if(typeHash == hash_DEL){
						
					}
				}
				if (!stillNeedToDeal || next == null)
					break;
			}
			/////////////////////////////////////////////////////////////End of direction=true
		} else {
			// direction=false, deal from the last variation
			Entry<EctypalSubElement> pre = subEles.getLast();
			for (int i = mergeVariants.size() - 1; i >= 0; i--) {
				v = mergeVariants.get(i).variant;
				if(v.getFrom() > pre.getElement().getTo())
					continue;
				if(v.getTo() < pre.getElement().getFrom()){
					pre = moveToVariantFrom(v.getTo(), pre);
					if(pre == null) break;
				}
				////////////Now pre is the SubElement where v.getTo() at
				typeHash = v.getType().hashCode();
				curNeedToDealType = currentNeedToDeal.get(pre.getElement().getType());
				if(curNeedToDealType != null){ //May need to deal
					if(typeHash == hash_SNV){
						if(curNeedToDealType == 1)
							pre = dealASNV(pre, v);
					}else if(typeHash == hash_INS){
						
					}else if(typeHash == hash_DEL){
						
					}
				}
				if (!stillNeedToDeal || pre == null)
					break;
			}
			////////////////////////////////////////////////End of direction=false
		}
	}

	/**
	 * Deal a SNV variation in the deal stage.
	 * @param cur The SubElement which the variation at.
	 * @param v The current variation we need to deal.
	 * @return
	 * @throws IOException
	 */
	private Entry<EctypalSubElement> dealASNV(Entry<EctypalSubElement> cur, Variant v) throws IOException{
		EctypalSubElement ese = cur.getElement();
		int vFromAtThisBox = direction ? (v.getFrom() - ese.getFrom() + 1) : (ese.getTo() - v.getFrom() + 1);
		int remainder = (boxBaseNumFromFirstBoxBase + vFromAtThisBox) % 3;
		int relativeFrom = 0;
		int relativeTo = 0;
		if(direction){
			relativeFrom = (remainder==0 ? -2 : (remainder==1 ? 0 : -1));
			relativeTo = (remainder==0 ? 0 : (remainder==1 ? 2 : 1));
		}else{
			relativeFrom = (remainder==0 ? 0 : (remainder==1 ? -2 : -1));
			relativeTo = (remainder==0 ? 2 : (remainder==1 ? 0 : 1));
		}
		Extract3Bases result = extractFromFasta(cur, v.getFrom(), relativeFrom, relativeTo); 
		if (result == null) return cur;
		result = dealBasesFromFastaFile(result);
		if (result == null) return cur;
		// Now we have get the sequence from the fasta file.
		char transcription = standardGeneticCode.get(result.sequence);
		//Record this variation in SubElement
		recordVariant(result, v, transcription);
		
		if(v.getFrom()>=initiatorSmall && v.getTo()<=initiatorLarge && !result.sequence.equals("ATG")){
			//Change the initiator
			if(ese.getType().equals(Consts.SUBELEMENT_TYPE_BOX)){
				ese.setType(Consts.SUBELEMENT_TYPE_LOST_BOX);
			}else if(ese.getType().equals(Consts.SUBELEMENT_TYPE_EXTEND_BOX)){
				ese.setType(Consts.SUBELEMENT_TYPE_EXTEND_BAND);
			}
		}
		if(v.getFrom()>=terminatorSmall && v.getTo()<=terminatorLarge && transcription != '$'){
			//Change the terminator
			if(ese.getType().equals(Consts.SUBELEMENT_TYPE_BOX)){
				ese.setType(Consts.SUBELEMENT_TYPE_EXTEND_BOX);
			}
		}

		if(transcription == '$' && ((direction&&v.getTo()<terminatorSmall)||(!direction&&v.getFrom()>terminatorLarge))){
			terminatorHasAppeared(v, result, cur);
		}

		return cur;
	}
	
	/**
	 * Because the upriver variations has an impact on the downstream variations, we must check the <code>result</code>
	 * whether the positions overlap with some of the variations we has dealed before and deal the <code>result</code>.
	 * After this operation, this function will rollback and complemente <code>result.sequence</code>
	 * @param result
	 * @return
	 */
	private Extract3Bases dealBasesFromFastaFile(Extract3Bases result){
		//TODO 现在只处理SNV
		Entry<Variant> upstream = dealedVariations.getLast();
		if(upstream == null)
			return result;
		char[] newChars = new char[]{'\0', '\0', '\0'};
		boolean update = false;
		int upFrom = upstream.getElement().getFrom();
		while(upFrom >= result.firstPos && upFrom <= result.thirdPos){
			if(result.firstPos == upFrom){
				update = true;
				newChars[0] = upstream.getElement().getLetter().charAt(0);
			} else if(result.secondPos == upFrom){
				update = true;
				newChars[1] = upstream.getElement().getLetter().charAt(0);
			} else if(result.thirdPos == upFrom){
				update = true;
				newChars[2] = upstream.getElement().getLetter().charAt(0);
			}
			upstream = dealedVariations.getPrevious(upstream);
			if(upstream == null)
				break;
			upFrom = upstream.getElement().getFrom();
		}
		if(update){
			if(newChars[0] == '\0') newChars[0] = result.sequence.charAt(0);
			if(newChars[1] == '\0') newChars[1] = result.sequence.charAt(1);
			if(newChars[2] == '\0') newChars[2] = result.sequence.charAt(2);
			result.sequence = new String(newChars);
		}
		
		if (!direction) {
			// Inverse and complement the sequence
			char[] cs = new char[3];
			cs[0] = complementations[((int) result.sequence.charAt(2) - 65) / 2];
			cs[1] = complementations[((int) result.sequence.charAt(1) - 65) / 2];
			cs[2] = complementations[((int) result.sequence.charAt(0) - 65) / 2];
			result.sequence = new String(cs);
		}
		return result;
	}

	private Entry<EctypalSubElement> terminatorHasAppeared(Variant v, Extract3Bases result, Entry<EctypalSubElement> cur){
		//TODO
		/*
		 * 现在处理的只有SNV，当处理INS和SNV的时候，以正链为例:
		 * 即使firstPos不是所在的SubElement的第一个碱基，也有可能需要查找之前的SubElement，因为firstPos之前可能有一个DEL了；
		 * 或者即使firstPos是所在的SubElement的第一个碱基，也有可能不需要查找之前的SubElement，因为firstPos之前可能有一个INS。
		 * 若查找前面的SubElement，也不一定是查找到的碱基的第一个base，因为可能在这个SubElement的末端有DEL。
		 */
		//terminator has appeared before the normal terminator
		int sepPos = -1;
		boolean sameSE = false;
		if(direction){
			Entry<EctypalSubElement> e = result.first;
			EctypalSubElement firstE = e.getElement();
			sameSE = e == cur;
			if(firstE.getFrom() < result.firstPos){
				sepPos = result.firstPos - 1;
				EctypalSubElement[] divideResultSubEles = EctypalSubElement.divideInto2SubElements(firstE, sepPos, Consts.SUBELEMENT_TYPE_BOX,
						firstE.getType().equals(Consts.SUBELEMENT_TYPE_BOX) ? Consts.SUBELEMENT_TYPE_LOST_BOX : Consts.SUBELEMENT_TYPE_EXTEND_BAND, true);
				Entry<EctypalSubElement> preOne = subEles.removeAndReturnPrevious(e);
				preOne = preOne != null ? subEles.addAfter(divideResultSubEles[0], preOne) : subEles.addFirst(divideResultSubEles[0]); 
				if(sameSE)
					cur = subEles.addAfter(divideResultSubEles[1], preOne);
				else
					subEles.addAfter(divideResultSubEles[1], preOne);
			}else{
				// e.getElement().getFrom() == result.firstPos
				firstE.setType(firstE.getType().equals(Consts.SUBELEMENT_TYPE_BOX) ? Consts.SUBELEMENT_TYPE_LOST_BOX : Consts.SUBELEMENT_TYPE_EXTEND_BAND);
				do{
					e = subEles.getPrevious(e);
					if(e == null) break;
					firstE = e.getElement();
				}while(currentNeedToDeal.containsKey(firstE.getType()));
				if(e != null)
					firstE.setType(Consts.SUBELEMENT_TYPE_BOX);//TODO 当变异上有del的时候，不一定是这个SubElement最后一个base
			}
		}else{
			Entry<EctypalSubElement> e = result.third;
			EctypalSubElement thirdE = e.getElement();
			sameSE = e == cur;
			if(thirdE.getTo() > result.thirdPos){
				sepPos = result.thirdPos + 1;
				EctypalSubElement[] divideResultSubEles = EctypalSubElement.divideInto2SubElements(thirdE, sepPos, 
						thirdE.getType().equals(Consts.SUBELEMENT_TYPE_BOX) ? Consts.SUBELEMENT_TYPE_LOST_BOX : Consts.SUBELEMENT_TYPE_EXTEND_BAND, 
						Consts.SUBELEMENT_TYPE_BOX, false);
				Entry<EctypalSubElement> nextOne = subEles.removeAndReturnNext(e);
				nextOne = nextOne != null ? subEles.addBefore(divideResultSubEles[1], nextOne) : subEles.addLast(divideResultSubEles[1]); 
				if(sameSE)
					cur = subEles.addBefore(divideResultSubEles[0], nextOne);
				else
					subEles.addBefore(divideResultSubEles[0], nextOne);
			}else{
				// e.getElement().getTo() == result.thirdPos
				thirdE.setType(Consts.SUBELEMENT_TYPE_BOX);
				do{
					e = subEles.getNext(e);
					if(e == null) break;
					thirdE = e.getElement();
				}while(currentNeedToDeal.containsKey(thirdE.getType()));
				if(e != null)
					thirdE.setType(thirdE.getType().equals(Consts.SUBELEMENT_TYPE_BOX) ? Consts.SUBELEMENT_TYPE_LOST_BOX : Consts.SUBELEMENT_TYPE_EXTEND_BAND);

			}
		}
		
		return cur;
	}
	
	private void recordVariant(Extract3Bases e3b, Variant v, char transcription){
		boolean fstSndContinuous = e3b.firstPos == e3b.secondPos || e3b.firstPos + 1 == e3b.secondPos;
		boolean sndRdContinuous = e3b.secondPos == e3b.thirdPos || e3b.secondPos + 1 == e3b.thirdPos;
		if(fstSndContinuous){
			if(sndRdContinuous){
				// the three bases are all continuous
				e3b.first.getElement().addVariantion(v.getId(), v.getType(), e3b.firstPos, e3b.thirdPos, transcription+"");				
			}else{
				// the first and the second bases are continuous
				e3b.first.getElement().addVariantion(v.getId(), v.getType(), e3b.firstPos, e3b.secondPos, transcription+"");
				e3b.third.getElement().addVariantion(v.getId(), v.getType(), e3b.thirdPos, e3b.thirdPos, transcription+"");
			}
		}else{
			if(sndRdContinuous){
				// the second and the third bases are continuous
				e3b.first.getElement().addVariantion(v.getId(), v.getType(), e3b.firstPos, e3b.firstPos, transcription+"");
				e3b.second.getElement().addVariantion(v.getId(), v.getType(), e3b.secondPos, e3b.thirdPos, transcription+"");
			}else{
				// the three bases are all not continuous
				e3b.first.getElement().addVariantion(v.getId(), v.getType(), e3b.firstPos, e3b.firstPos, transcription+"");
				e3b.second.getElement().addVariantion(v.getId(), v.getType(), e3b.secondPos, e3b.secondPos, transcription+"");
				e3b.third.getElement().addVariantion(v.getId(), v.getType(), e3b.thirdPos, e3b.thirdPos, transcription+"");
			}
		}
	}
	
	/**
	 * Deal a variation in previous deal.
	 * @throws IOException
	 */
	private Entry<EctypalSubElement> dealAVariationPreDeal(Variant v,
			Map<Entry<EctypalSubElement>, String> tempAssDss,
			Entry<EctypalSubElement> cur) throws IOException {
		String type = v.getType();
		int _type = type.hashCode();
		if(_type == hash_SNV || _type == hash_INS || _type == hash_DEL || _type == hash_CNV || _type == hash_DUP){
			if(direction && (v.getFrom() > cur.getElement().getTo()))
				cur = variantFrom(v.getFrom(), cur);
			if(!direction && (v.getTo() < cur.getElement().getFrom()))
				cur = variantTo(v.getTo(), cur);
			if(cur == null) 
				return null;
		}
		if (_type == hash_SNV){
			if (cur.getElement().getType().equals(Consts.SUBELEMENT_TYPE_LINE))
				dealLineInPreDeal(v.getFrom(), v.getTo(), 1, null, cur, tempAssDss);
			return cur;
		}
		if (_type == hash_INS){
			if (insEffectBox(v, cur))
				return null;
			if (cur.getElement().getType().equals(Consts.SUBELEMENT_TYPE_LINE))
				dealLineInPreDeal(v.getFrom(), v.getTo(), 2, v.getLetter(), cur, tempAssDss);
			return cur;
		}
		if (_type == hash_BLS) {// BLS
			if (v.getFrom() >= from && v.getFrom() <= to)
				terminate(type, false);
			return cur;
		} 
		// DEL, CNV, INV or DUP
		if (variantContainElement(v)) {
			terminate(type, false);
			return null;
		}
		if (_type == hash_DEL){
			if (getBasesInBoxOfDEL(v, cur) > BOX_LIMIT * initBoxLen) {
				terminate(LARGE_VARIANTION, false);
				return null;
			}
			dealLineInPreDeal(v.getFrom(), v.getTo(), 3, null, cur, tempAssDss);
			return cur;
		}
		if (_type == hash_INV){
			if (overlap(v.getFrom(), v.getTo(), from, to))
				terminate(LARGE_VARIANTION, false);
			return cur;
		}
		if (_type == hash_CNV || _type == hash_DUP){
			EctypalSubElement ese = cur.getElement();
			if (contained(v.getFrom(), v.getTo(), from, to)
					&& !(ese.getType().equals(Consts.SUBELEMENT_TYPE_LINE) 
						 && contained(v.getFrom(), v.getTo(), ese.getFrom(), ese.getTo()))) {
				terminate(LARGE_VARIANTION, false);
				return null;
			}
			return cur;
		}

		return cur;
	}

	/**
	 * Record the <code>status</code> and <code>stillNeedToDeal</code>.
	 * 
	 * @param status
	 * @param stillNeedToDeal
	 */
	private void terminate(String status, boolean stillNeedToDeal) {
		this.status.add(status);
		this.stillNeedToDeal = stillNeedToDeal;
	}

	/**
	 * If the variation type is "SNV" or "INS" or "DEL", and the variation is
	 * located at the SubElement which type is "Line", then we should deal it
	 * because it may cause ASS variation or DSS variation.
	 * 
	 * @param from
	 *            <code>from</code> = Variant.from
	 * @param to
	 *            <code>to</code> = Variant.to
	 * @param type
	 *            <code>type</code> = 1 when Variant.type="SNV" or
	 *            <code>type</code> = 2 when Variant.type="INS" or
	 *            <code>type</code> = 3 when Variant.type="DEL"
	 * @param basesOfINS
	 *            Bases of INS' Letter. Effective only when <code>type</code>=2.
	 * @param cur
	 * @param tempAssDss
	 * @throws IOException
	 */
	private void dealLineInPreDeal(int from, int to, int type, String basesOfINS,
			Entry<EctypalSubElement> cur,
			Map<Entry<EctypalSubElement>, String> tempAssDss)
			throws IOException {
		EctypalSubElement ese = cur.getElement();
		if (type == 1) {
			if (from == ese.getFrom() || from == ese.getFrom() + 1) {
				recordTempAssDss(tempAssDss, cur, true);
				return;
			}
			if (from == ese.getTo() || from == ese.getTo() - 1) {
				recordTempAssDss(tempAssDss, cur, false);
				return;
			}
		} else if (type == 2) {
			if (from == ese.getFrom()) {
				if (basesOfINS.charAt(0) != fr.extract_char(chr, from + 1))
					recordTempAssDss(tempAssDss, cur, true);
				return;
			}
			if (to == ese.getTo()) {
				if (basesOfINS.charAt(basesOfINS.length() - 1) != fr
						.extract_char(chr, from))
					recordTempAssDss(tempAssDss, cur, false);
				return;
			}
		} else {
			// Find the SubElement where from and to at respectively
			Entry<EctypalSubElement> fromCur = null;
			Entry<EctypalSubElement> toCur = null;
			if (direction) {
				fromCur = cur;
				toCur = variantFrom(to, cur);
				if (toCur == null) {
					// If to>this.to, set to=this.to
					to = this.to;
					toCur = subEles.getLast();
				}
			} else {
				toCur = cur;
				fromCur = variantTo(from, cur);
				if (fromCur == null) {
					// If from<this.from, set from=this.from
					from = this.from;
					fromCur = subEles.getFirst();
				}
			}
			dealDELASSOrDSS(from, to, fromCur, toCur, tempAssDss);
		}
	}
	
	private void dealDELASSOrDSS(int from, int to,
			Entry<EctypalSubElement> fromCur, Entry<EctypalSubElement> toCur,
			Map<Entry<EctypalSubElement>, String> tempAssDss) {
		EctypalSubElement fromEse = fromCur.getElement();
		EctypalSubElement toEse = toCur.getElement();
		boolean isFromTypeLine = fromEse.getType().equals(Consts.SUBELEMENT_TYPE_LINE);
		boolean isToTypeLine = toEse.getType().equals(Consts.SUBELEMENT_TYPE_LINE);
		if (isFromTypeLine) {
			if (isToTypeLine) {
				//Line to Line
				boolean isFromFS = (from <= fromEse.getFrom() + 1);
				boolean isToLP = (to >= toEse.getTo() - 1);
				if(isFromFS && !isToLP){
					/*
					 * 1111|||||-----||||-----|||||-----||||||11111
	        		 *	        |===================|
					 */
					recordTempAssDss(tempAssDss, direction?toCur:fromCur, true);
				}
				else if(!isFromFS && isToLP){
					/*
					 * 1111|||||-----||||-----|||||-----||||||11111
	        		 *	            |==================|
					 */
					recordTempAssDss(tempAssDss, direction?toCur:fromCur, false);
				}
			} else {
				//Line to Box|Band
				/*
				 * 1111|||||-----||||-----|||||-----||||||11111
        		 *	            |=====================|
				 */
				if(from > fromEse.getFrom() + 1){
					recordTempAssDss(tempAssDss, direction?subEles.getPrevious(toCur):fromCur, false);
				}
				//Don't need to deal if from==fromEse.getFrom()
				//or from==fromEse.getFrom()+1
				/*
				 * 1111|||||-----||||-----|||||-----||||||11111
        		 *	        |=========================|
				 */
			}
		} else {
			if (isToTypeLine) {
				// Box|Band to Line
				/*
				 * 1111|||||-----||||-----|||||-----||||||11111
        		 *	     |======================|
				 */
				if(to < toEse.getTo() - 1){
					recordTempAssDss(tempAssDss, direction?toCur:subEles.getNext(fromCur), true);
				}
				//Don't need to deal if to==toEse.getTo()-1
				//or to==toEse.getTo()
				/*
				 * 1111|||||-----||||-----|||||-----||||||11111
        		 *	      |========================|
				 */
			} 
			// Don't need to deal Box|Band to Box|Band
			/*
			 * 1111|||||-----||||-----|||||-----||||||11111
    		 *	     |============================|
			 */
		}
	}

	/**
	 * Record an ASS or DSS.
	 * 
	 * @param tempAssDss
	 * @param cur
	 * @param isFS
	 *            If the variation effect the Line's first or second base,
	 *            <code>isFS=true</code>, false if the variation effect the
	 *            Line's last or penult base.
	 */
	private void recordTempAssDss(
			Map<Entry<EctypalSubElement>, String> tempAssDss,
			Entry<EctypalSubElement> cur, boolean isFS) {
		Entry<EctypalSubElement> e = (isFS ? subEles.getPrevious(cur) : subEles.getNext(cur));
		if (e != null) {
			boolean isBox = e.getElement().getType()
					.equals(Consts.SUBELEMENT_TYPE_BOX);
			String newType = "";
			if (isFS) {
				if (direction)
					newType = isBox ? Consts.SUBELEMENT_TYPE_EXTEND_BOX : Consts.SUBELEMENT_TYPE_EXTEND_BAND;
				else
					newType = isBox ? Consts.SUBELEMENT_TYPE_SKIP_BOX : Consts.SUBELEMENT_TYPE_SKIP_BAND;
			} else {
				if (direction)
					newType = isBox ? Consts.SUBELEMENT_TYPE_SKIP_BOX : Consts.SUBELEMENT_TYPE_SKIP_BAND;
				else
					newType = isBox ? Consts.SUBELEMENT_TYPE_EXTEND_BOX : Consts.SUBELEMENT_TYPE_EXTEND_BAND;
			}
			tempAssDss.put(e, newType);
		}
	}

	/**
	 * Whether INS effect BOX and INS.Letter.length > <code>BOX_LIMIT</code> *
	 * <code>initBoxLen</code>
	 *
	 * @return
	 */
	private boolean insEffectBox(Variant v, Entry<EctypalSubElement> cur) {
		EctypalSubElement ese = cur.getElement();
		if (ese.getType().equals(Consts.SUBELEMENT_TYPE_BOX)) {
			if (v.getLetter().length() > BOX_LIMIT * initBoxLen) {
				terminate(LARGE_VARIANTION, false);
				return true;
			}
		} else if (ese.getType().equals(Consts.SUBELEMENT_TYPE_LINE)) {
			Entry<EctypalSubElement> temp = direction ? subEles.getNext(cur)
					: subEles.getPrevious(cur);
			if (temp != null
					&& temp.getElement().getType().equals(Consts.SUBELEMENT_TYPE_BOX)
					&& ((direction && v.getFrom() == ese.getTo()) || (!direction && v
							.getTo() == ese.getFrom()))
					&& v.getLetter().length() > BOX_LIMIT * initBoxLen) {
				terminate(LARGE_VARIANTION, false);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method is used for move the current SubElement to another SubElement which contain
	 * the position: <code>from</code> from front to back in the previous deal stage.
	 * @return Return the <code>Entry&lt;EctypalSubElement&gt;</code> where <code>from</code>
	 *  &lt;= its to. Return null if <code>from</code> &gt; <code>this.to</code>.
	 */
	private Entry<EctypalSubElement> variantFrom(int from, Entry<EctypalSubElement> cur) {
		if (from > this.to)
			return null;
		while (from > cur.getElement().getTo()) {
			cur = subEles.getNext(cur);
		}
		return cur;
	}

	/**
	 * This method is used for move the current SubElement to another SubElement which contain
	 * the position: <code>to</code> from back to front in the previous deal stage.
	 * @return Return the <code>Entry&lt;EctypalSubElement&gt;</code> where <code>to</code> 
	 * &gt;= its from. Return null if <code>to</code> &lt; <code>this.from</code>.
	 */
	private Entry<EctypalSubElement> variantTo(int to, Entry<EctypalSubElement> cur) {
		if (to < this.from)
			return null;
		while (to < cur.getElement().getFrom()) {
			cur = subEles.getPrevious(cur);
		}
		return cur;
	}

	/**
	 * This method is used for move the current SubElement to another SubElement which contain
	 * the position: <code>from</code> from front to back in the deal stage. We should add the
	 * number of bases at the SubElement which type is Box. Remember that the upstream SubElement
	 * has an impact on the downstream SubElement.
	 * @return Return the <code>Entry&lt;EctypalSubElement&gt;</code> where <code>from</code> 
	 * &lt;= its to. Return null if <code>from</code> &gt; <code>this.to</code>.
	 */
	private Entry<EctypalSubElement> backToVariantFrom(int from, Entry<EctypalSubElement> cur){
		EctypalSubElement e = cur.getElement();
		String type = e.getType();
		while (from > e.getTo()) {
			if(!Consts.SUBELEMENT_TYPE_LINE.equals(type) && !Consts.SUBELEMENT_TYPE_BAND.equals(type))
				preSubEleNotLineNotBand = e;
			if(currentNeedToDeal.containsKey(type))
				boxBaseNumFromFirstBoxBase += e.getLength();
			cur = subEles.getNext(cur);
			if(cur == null) break;
			e = cur.getElement();
			type = e.getType();
			if(preSubEleNotLineNotBand != null){
				if(currentNeedToMap.containsKey(type)){
					e.setType(currentNeedToMap.get(type).get(preSubEleNotLineNotBand.getType()));
				}
			}
		}
	
		return cur;
	}

	/**
	 * This method is used for move the current SubElement to another SubElement which contain
	 * the position: <code>to</code> from back to front in the deal stage. We should add the
	 * number of bases at the SubElement which type is Box. Remember that the upstream SubElement
	 * has an impact on the downstream SubElement.
	 * @return Return the <code>Entry&lt;EctypalSubElement&gt;</code> where <code>to</code> 
	 * &gt;= its from. Return null if <code>to</code> &lt; <code>this.from</code>.
	 */
	private Entry<EctypalSubElement> moveToVariantFrom(int to, Entry<EctypalSubElement> cur){
		EctypalSubElement e = cur.getElement();
		String type = e.getType();
		while (to < e.getFrom()) {
			if(!Consts.SUBELEMENT_TYPE_LINE.equals(type) && !Consts.SUBELEMENT_TYPE_BAND.equals(type))
				preSubEleNotLineNotBand = e;
			if(currentNeedToDeal.containsKey(type))
				boxBaseNumFromFirstBoxBase += e.getLength();
			cur = subEles.getPrevious(cur);
			if(cur == null) break;
			e = cur.getElement();
			type = e.getType();
			if(preSubEleNotLineNotBand != null){
				if(currentNeedToMap.containsKey(type))
					e.setType(currentNeedToMap.get(type).get(preSubEleNotLineNotBand));
			}
		}
		
		return cur;
	}

	/**
	 * Get number of bases of DEL in Boxes.
	 * 
	 * @return Number of bases of DEL in Boxes
	 */
	private int getBasesInBoxOfDEL(Variant v, Entry<EctypalSubElement> cur) {
		int bases = 0;
		int from = v.getFrom();
		int to = v.getTo();
		Entry<EctypalSubElement> fromCur = null;
		Entry<EctypalSubElement> toCur = null;
		if (direction) {
			fromCur = cur;
			toCur = variantFrom(to, cur);
			if (toCur == null)
				toCur = subEles.getLast();
		} else {
			toCur = cur;
			fromCur = variantTo(from, cur);
			if (fromCur == null)
				fromCur = subEles.getFirst();
		}
		if (fromCur.getElement().getType().equals(Consts.SUBELEMENT_TYPE_BOX)) {
			bases += (to <= fromCur.getElement().getTo() ? to : fromCur.getElement().getTo()) - from + 1;
		}
		while (fromCur != toCur) {
			fromCur = subEles.getNext(fromCur);
			if (fromCur.getElement().getType().equals(Consts.SUBELEMENT_TYPE_BOX)) {
				bases += (to <= fromCur.getElement().getTo() ? to : fromCur.getElement().getTo())
						 - fromCur.getElement().getFrom() + 1;
			}
		}

		return bases;
	}
	
	private boolean variantContainElement(Variant v) {
		int _from = from - (direction ? 2000 : 500);
		if(_from < 1)
			_from = 1;
		int _to = to + (direction ? 500 : 2000);
		if(_to > chrLen)
			_to = chrLen;
		return contained(_from, _to, v.getFrom(), v.getTo());
	}

	/**
	 * @param cas	You must sure that <code>cas!=null</code> and <code>cas.size()&gt;0</code>.
	 */
	public void dealCtrlAreas(List<ControlArea> cas) {
		int _from = from - (direction ? 2000 : 500);
		int _to = to + (direction ? 500 : 2000);
		for(ControlArea ca : cas){
			if(ca.to < _from) continue;
			if(ca.from > _to) break;
			if (overlap(ca.from, ca.to, _from, _to)) {
				status.add(ca.id);
			}
		}
	}
	
	/**
	 * Extract suquence from fasta file, bases at <code>absolutePos</code>, and extract 
	 * abs(<code>relativeFrom</code>) number of bases in the "BOX" before the <code>absolutePos</code>
	 * and <code>relativeTo</code> number of bases in the "BOX" after the <code>absolutePos</code>. All
	 * bases we extract must be at the "BOX". Remember that we just need to extract three bases.
	 * <strong>Note: </strong>You must ensure that <code>relativeFrom</code> &lt;= 0 &lt;= 
	 * <code>relativeTo</code>, and <code>absolutePos</code> is at <code>cur</code>.
	 * 
	 * @param cur	
	 * 			  The current SubElement we deal.
	 * @param absolutePos
	 *            Absolute base position of the chromosome. You must ensure that <code>absolutePos</code>
	 *            is at <code>cur</code>.
	 * @param relativeFrom
	 *            The <code>relativeFrom</code> must &lt;= 0. It means we should
	 *            extract abs(<code>relativeFrom</code>) number of bases in the
	 *            "BOX" before the <code>absolutePos</code>.
	 * @param relativeTo
	 *            The <code>relativeTo</code> must &gt;= 0. It means we should
	 *            extract <code>relativeTo</code> number of bases in the
	 *            "BOX" after the <code>absolutePos</code>.
	 * @return The sequence (packaged in <code>Extract3Bases</code>) extract from fasta file of the given
	 * region in the "BOX". If less than abs(<code>relativeFrom</code>) bases from "BOX" before <code>absolutePos</code>
	 * or less than <code>relativeTo</code> bases from "BOX" after <code>absolutePos</code>, null will be returned.
	 * @throws IOException 
	 */
	private Extract3Bases extractFromFasta(Entry<EctypalSubElement> cur,
			int absolutePos, int relativeFrom, int relativeTo) throws IOException {
		//TODO
		/*
		 * 将该函数的实现改为一次取出一个位置的碱基。比如absolutePos=100，relativeFrom=-1和relativeTo=1并且是正链。
		 * 则先取出100和101（可能不是101，应该是100往后的第一个"BOX"上的碱基）位置的碱基。然后往前取出第一个位置的碱基。因为上游的
		 * 变异影响下游，所以往前取出的时候需要判断一些特殊的判断。这样就不需要函数dealBasesFromFastaFile了。
		 */
		Extract3Bases result = new Extract3Bases();
		if(relativeFrom < 0)
			extractForward(cur, absolutePos, -relativeFrom, result);
		if(result.sequence == null)
			return null;
		char absoluteChar = fr.extract_char(chr, absolutePos);
		result.sequence = result.sequence.concat(absoluteChar + "");
		if(relativeFrom == 0){
			result.first = cur;
			result.firstPos = absolutePos;
		}else if(relativeFrom == -1){
			result.second = cur;
			result.secondPos = absolutePos;
		}else if(relativeFrom == -2){
			result.third = cur;
			result.thirdPos = absolutePos;
		}
		if(relativeTo > 0)
			extractAfterward(cur, absolutePos, relativeTo, result);
		if(result.sequence == null)
			return null;
		return result;
	}
	
	private void extractForward(Entry<EctypalSubElement> cur, int absolutePos, int len, Extract3Bases result) throws IOException{
		String preResult = "";
		EctypalSubElement ese = cur.getElement();
		int from = 0;
		int to = 0;
		if(absolutePos > ese.getFrom()){
			to = absolutePos - 1;
		}else{
			// absolutePos == ese.getFrom()
			do{
				cur = subEles.getPrevious(cur);
				if(cur == null) break;
			}while(!currentNeedToDeal.containsKey(cur.getElement().getType()));
			if(cur == null){
				result.sequence = null;
				return ;
			}
			ese = cur.getElement();
			to = ese.getTo();
		}
		// Start to extract bases from fasta file before the absolutePos
		from = (to-len+1 >= ese.getFrom()) ? to-len+1 : ese.getFrom();
		preResult = fr.extract_seq(chr, from, to);
		// Obviously, len==1 or len==2
		if(len == 1){
			result.first = cur;
			result.firstPos = to;
		}else if(len == 2){
			result.second = cur;
			result.secondPos = to;
			if(preResult.length() == 2){
				result.first = cur;
				result.firstPos = from;
			}else if(preResult.length() == 1){
				do{
					cur = subEles.getPrevious(cur);
					if(cur == null) break;
				}while(!currentNeedToDeal.containsKey(cur.getElement().getType()));
				if(cur == null){
					result.sequence = null;
					return ;
				}
				ese = cur.getElement();
				preResult = fr.extract_seq(chr, ese.getTo(), ese.getTo()).concat(preResult);
				result.first = cur;
				result.firstPos = ese.getTo();
			}
		}
		result.sequence = preResult;
	}

	private void extractAfterward(Entry<EctypalSubElement> cur, int absolutePos, int len, Extract3Bases result) throws IOException{
		String afterResult = "";
		EctypalSubElement ese = cur.getElement();
		int from = 0;
		int to = 0;
		if(absolutePos < ese.getTo()){
			from = absolutePos + 1;
		}else{
			// absolutePos == ese.getTo()
			do{
				cur = subEles.getNext(cur);
				if(cur == null) break;
			}while(!currentNeedToDeal.containsKey(cur.getElement().getType()));
			if(cur == null){
				result.sequence = null;
				return ;
			}
			ese = cur.getElement();
			from = ese.getFrom();
		}
		// Start to extract bases from fasta file after the absolutePos
		to = (from+len-1 <= ese.getTo()) ? from+len-1 : ese.getTo();
		afterResult = fr.extract_seq(chr, from, to);
		// Obviously, len==1 or len==2
		if(len == 1){
			result.third = cur;
			result.thirdPos = from;
		}else if(len == 2){
			result.second = cur;
			result.secondPos = from;
			if(afterResult.length() == 2){
				result.third = cur;
				result.thirdPos = to;
			}else if(afterResult.length() == 1){
				do{
					cur = subEles.getNext(cur);
					if(cur == null) break;
				}while(!currentNeedToDeal.containsKey(cur.getElement().getType()));
				if(cur == null){
					result.sequence = null;
					return ;
				}
				ese = cur.getElement();
				afterResult = afterResult.concat(fr.extract_seq(chr, ese.getFrom(), ese.getFrom()));
				result.third = cur;
				result.thirdPos = ese.getFrom();
			}
		}
		result.sequence = result.sequence.concat(afterResult);
	}

	private Extract3Bases extractFromFasta2(Entry<EctypalSubElement> cur,
			int absolutePos, int relativeFrom, int relativeTo) throws IOException {
		//TODO 现在只处理SNV
		Extract3Bases result = new Extract3Bases();
		char[] resultChars = new char[3];
		char absoluteChar = fr.extract_char(chr, absolutePos);
		if(relativeFrom == 0){
			resultChars[0] = absoluteChar;
			result.first = cur;
			result.firstPos = absolutePos;
		}else if(relativeFrom == -1){
			resultChars[1] = absoluteChar;
			result.second = cur;
			result.secondPos = absolutePos;
		}else if(relativeFrom == -2){
			resultChars[2] = absoluteChar;
			result.third = cur;
			result.thirdPos = absolutePos;
		}
		if(direction){
			if(relativeTo > 0){
				
			}else{
				
			}
		}else{
			if(relativeFrom < 0){
				
			}else{
				
			}
		}
		return result;
	}
	Element write2XML(Document doc) {
		Element element = doc.createElement(Consts.XML_TAG_ELEMENT);
		if(id != null)
			element.setAttribute(Consts.XML_TAG_ID, id);
		if(type != null)
			element.setAttribute(Consts.XML_TAG_TYPE, type);
		XmlWriter.append_text_element(doc, element, Consts.XML_TAG_FROM, String.valueOf(from));
		XmlWriter.append_text_element(doc, element, Consts.XML_TAG_TO, String.valueOf(to));
		XmlWriter.append_text_element(doc, element, Consts.XML_TAG_DIRECTION, direction?"+":"-");
		if(description != null)
			XmlWriter.append_text_element(doc, element, Consts.XML_TAG_DESCRIPTION, description);
		if(color != null)
			XmlWriter.append_text_element(doc, element, Consts.XML_TAG_COLOR, color);
		StringBuilder builder = new StringBuilder();
		for(String s : status){
			builder.append(s);
			builder.append(';');
		}
		//TODO 在Consts里添加"status"
		if(builder.length() > 1){
			XmlWriter.append_text_element(doc, element, "status", builder.substring(0, builder.length()-1));
		}
		Entry<EctypalSubElement> next = subEles.getFirst();
		Element subEle = null;
		while (next != null) {
			subEle = next.getElement().write2XML(doc);
			element.appendChild(subEle);
			next = subEles.getNext(next);
		}
		return element;
	}

	/**
	 * Judge that whether [from1, to1] overlaps with [from2, to2].
	 * 
	 * Return true when:
	 * 1: <code>from1</code> &lt; <code>from2</code> and <code>to1</code> &gt;= <code>from2</code>;
	 * 2: <code>from1</code> &gt;= <code>from2</code> and <code>from1<code> &lt;= </code>to2</code>.
	 * @return
	 */
 	public static boolean overlap(int from1, int to1, int from2, int to2) {
		return ((from1 < from2 && to1 >= from2) || (from1 >= from2 && from1 <= to2));
	}

	/**
	 * Judge that whether [from1, to1] contained int [from2, to2].
	 * 
	 * Return true when:
	 * <code>from1</code> &gt;= <code>from2</code> and <code>to1</code> &lt;= <code>to2</code>.
	 * @return
	 */
	public static boolean contained(int from1, int to1, int from2, int to2) {
		return from1 >= from2 && to1 <= to2;
	}
    
	/**
	 * As we know, the upstream's variation has an impact on the downstream's variation,
	 * so we must record all the upstream's variations.
	 * For example, there is a Box from 500 to 550, and we has dealed a DEL variation from 522
	 * to 530 at it. Now there is another variation of SNV at 531, so we may need to extract
	 * bases at 520, 521 and 531, not 529 to 531. So we should mark each of the bases extracted
	 * from the fasta file which SubElement they come from. 
	 * @author Chengwu Yan
	 *
	 */
	private class Extract3Bases{
		String sequence = "";
		Entry<EctypalSubElement> first;
		int firstPos;
		Entry<EctypalSubElement> second;
		int secondPos;
		Entry<EctypalSubElement> third;
		int thirdPos;
		
		public String toString(){
			StringBuilder builder = new StringBuilder();
			builder.append("读取结果：" + sequence);
			builder.append("第一个位置是：" + firstPos);
			builder.append("第二个位置是：" + secondPos);
			builder.append("第三个位置是：" + thirdPos);
			return builder.toString();
		}
	}

	void printVariantsInSubElement(List<VariantMapToDBSNP> mergeVariants){
		Entry<EctypalSubElement> ese = subEles.getFirst();
		Variant v = null;
		if(ese != null){
			for(int i=0; i< mergeVariants.size(); i++){
				v = mergeVariants.get(i).variant;
				if(v.getTo() < ese.getElement().getFrom()) continue;
				if(v.getFrom() > ese.getElement().getTo()){
					ese = subEles.getNext(ese);
					if(ese == null) break;
					i--;
					continue;
				}
				System.out.println(this.id + "的" + ese.getElement().getFrom() + "-" + ese.getElement().getTo() + "(" + ese.getElement().getType() + "): " + v.toString());
			}
		}
	}
}
