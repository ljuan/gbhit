package edu.hit.mlg.individual;

import java.util.HashMap;
import java.util.Map;

public class GeneticCode {
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
	final static Map<String, Character> standardGeneticCode = new HashMap<String, Character>();
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
		standardGeneticCode.put("ATT", 'I');
		standardGeneticCode.put("ATC", 'I');
		standardGeneticCode.put("ATA", 'I');
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

	/**
	 * Get base's complementary base
	 */
	static char[] complementations = new char[] { 'T', 'G', '0', 'C',
			'0', '0', 'N', '0', '0', 'A' };
	
	/**
	 * translation genetic code
	 * @return
	 */
	static String bases2gene(String bases){
		StringBuilder builder = new StringBuilder();
		for(int i=0, len=bases.length(); i<len; i+=3){
			builder.append(standardGeneticCode.get(bases.substring(i, i+3)));
		}
		return builder.toString();
	}
	
	/**
	 * Inverse and complement the <code>sequence</code>
	 * @return
	 */
	static String InverseAndComplement(String sequence){
		StringBuilder builder = new StringBuilder();
		for(int i=sequence.length()-1; i>=0; i--){
			builder.append(complementations[((int) sequence.charAt(i) - 65) / 2]);
		}
		return builder.toString();
	}
	
	/**
	 * Inverse and complement the <code>sequence</code>
	 * @return
	 */
	static String InverseAndComplement(char[] sequence){
		StringBuilder builder = new StringBuilder();
		for(int i=sequence.length-1; i>=0; i--){
			builder.append(complementations[((int)sequence[i] - 65) / 2]);
		}
		return builder.toString();
	}
	
	static String replaceChar(String str, char c, int index, boolean direction){
		char[] cs = new char[3];
		cs[0] = str.charAt(0);
		cs[1] = str.charAt(1);
		cs[2] = str.charAt(2);
		cs[index] = direction ? c : complementations[((int) c - 65) / 2];
		return new String(cs);
	}
}
