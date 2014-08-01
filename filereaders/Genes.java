package filereaders;

import java.io.*;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/* 
 * This class is for reading HGNC gene annotation.
 * Including HGNC ID, RefSeq ID, UCSC known gene ID, Ensembl ID, Entrez ID,
 * Approved Symbol and Gene Name, as well as Gene location.
 * 
 */

public class Genes{
	private static String[] Symbols;
	private static String[] Symbols_sorted;
	private static String[] HGNCs;
	private static String[] Entrezs;
	private static String[] RefSeqs;
	private static String[] UCSCs;
	private static String[] Ensembls;
	private static String[] Names;
	private static int[] Chrs;
	private static int[] Starts;
	private static int[] Ends;
	private static HashMap<String,Integer> ChrMap;
	private static HashMap<String,Integer> SymbolMap;
	private static String[] ChrList;
	private static Integer[] ChrStarts;
	private static Integer[] ChrEnds;
	static {
		int current_chr=-1;
		File hgnc=new File(CfgReader.getBasic(Consts.CURRENT_ASSEMBLY,Consts.FORMAT_HGNC).get_Path());
		ByteBufferChannel bbc=new ByteBufferChannel(hgnc,0,hgnc.length());
		String[] temp=bbc.ToString(Consts.DEFAULT_ENCODE).split("\n");
		ArrayList<String> ChrList_temp=new ArrayList<String>();
		ArrayList<Integer> ChrStart_temp=new ArrayList<Integer>();
		ArrayList<Integer> ChrEnd_temp=new ArrayList<Integer>();
		ChrMap=new HashMap<String,Integer>();
		SymbolMap=new HashMap<String,Integer>();
		Symbols=new String[temp.length];
		Symbols_sorted=new String[temp.length];
		HGNCs=new String[temp.length];
		Entrezs=new String[temp.length];
		RefSeqs=new String[temp.length];
		UCSCs=new String[temp.length];
		Ensembls=new String[temp.length];
		Names=new String[temp.length];
		Chrs=new int[temp.length];
		Starts=new int[temp.length];
		Ends=new int[temp.length];
		for(int i=0;i<temp.length;i++){
			String[] line_temp=temp[i].split("\t");
			if(!ChrMap.containsKey(line_temp[0])){
				if(i>0)
					ChrEnd_temp.add(i-1);
				ChrMap.put(line_temp[0], ++current_chr);
				ChrList_temp.add(line_temp[0]);
				ChrStart_temp.add(i);
			}
			Chrs[i]=current_chr;
			Starts[i]=Integer.parseInt(line_temp[1]);
			Ends[i]=Integer.parseInt(line_temp[2]);
			Symbols[i]=line_temp[3];
			Symbols_sorted[i]=line_temp[3];
			SymbolMap.put(line_temp[3], i);
			RefSeqs[i]=line_temp[4];
			UCSCs[i]=line_temp[5];
			Ensembls[i]=line_temp[6];
			Entrezs[i]=line_temp[7];
			HGNCs[i]=line_temp[8];
			Names[i]=line_temp[9];
		}
		ChrEnd_temp.add(temp.length-1);
		Arrays.sort(Symbols_sorted);
		ChrList=new String[ChrList_temp.size()];
		ChrStarts=new Integer[ChrList.length];
		ChrEnds=new Integer[ChrList.length];
		ChrList_temp.toArray(ChrList);
		ChrStart_temp.toArray(ChrStarts);
		ChrEnd_temp.toArray(ChrEnds);
	}
	Genes(){
	}
	public static Element gene_Info(Document doc,String symbol){
		Element genes = doc.createElement(Consts.XML_TAG_GENES);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(genes); 
		if(SymbolMap.containsKey(symbol)){
			int index=SymbolMap.get(symbol);
			Element gene = doc.createElement(Consts.XML_TAG_GENE);
			gene.setAttribute(Consts.XML_TAG_ID, Symbols[index]);
			genes.appendChild(gene);
			XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_CHROMOSOME, ChrList[Chrs[index]]);
			XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_FROM, Integer.toString(Starts[index]+1));
			XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_TO, Integer.toString(Ends[index]));
			if(!RefSeqs[index].equals(""))
				XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_REFSEQ, RefSeqs[index]);
			if(!UCSCs[index].equals(""))
				XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_UCSC, UCSCs[index]);
			if(!Ensembls[index].equals(""))
				XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_ENSEMBL, Ensembls[index]);
			if(!Entrezs[index].equals(""))
				XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_ENTREZ, Entrezs[index]);
			XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_HGNC, HGNCs[index]);
			XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_NAME, Names[index]);
		}
		return genes;
	}
	public static Element find_Gene(Document doc, String prefix){
		prefix=prefix.toUpperCase();
		if(prefix.matches("C[XY0-9]+ORF")){
			prefix=prefix.replaceFirst("ORF", "orf");
		}
		else if(prefix.matches("C[XY0-9]+OR")){
			prefix=prefix.replaceFirst("OR", "or");
		}
		else if(prefix.matches("^C[XY0-9]+O")){
			prefix=prefix.replaceFirst("O", "o");
		}
		Element genes = doc.createElement(Consts.XML_TAG_GENES);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(genes); 
		int match=binarySearchPrefix(prefix);
		if(match>=0){
			for(int i=match;i<match+5;i++){
				if(i>=Symbols_sorted.length || !Symbols_sorted[i].startsWith(prefix))
					return genes;
				else{
					Element gene=doc.createElement(Consts.XML_TAG_GENE);
					gene.setAttribute(Consts.XML_TAG_ID, Symbols_sorted[i]);
					int index=SymbolMap.get(Symbols_sorted[i]);
					XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_CHROMOSOME, ChrList[Chrs[index]]);
					XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_FROM, Integer.toString(Starts[index]+1));
					XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_TO, Integer.toString(Ends[index]));
					genes.appendChild(gene);
				}
			}
		}
		return genes;
	}
	public static Element overlap_Genes(Document doc, String chr, int start, int end){
		return overlap_Genes(doc, chr, start, end, null);
	}
	public static Element overlap_Genes(Document doc, String chr, int start, int end, IndividualStat is){
		Element genes = doc.createElement(Consts.XML_TAG_GENES);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(genes); 
		if(ChrMap.containsKey(chr)){
			int[] range=binarySearchOverlap(ChrMap.get(chr),start,end);
			if(range!=null){
				float[] scores=null;
				if(is!=null)
					scores=is.get_GeneScores(range[1], range[0]);
				for(int i=range[0];i<=range[1];i++){
					Element gene=doc.createElement(Consts.XML_TAG_GENE);
					gene.setAttribute(Consts.XML_TAG_ID, Symbols[i]);
					XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_FROM, Integer.toString(Starts[i]+1));
					XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_TO, Integer.toString(Ends[i]));
					if(is!=null)
						XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_SCORE, Float.toString(Math.round(scores[i-range[0]]*10)/10));
					genes.appendChild(gene);
				}
			}
		}
		return genes;
	}
	public static Element ranking_Genes(Document doc, IndividualStat is, int number){
		Element genes = doc.createElement(Consts.XML_TAG_GENES);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(genes); 
		float[] scores=null;
		Integer[] index;
		if(number > Symbols.length)
			number = Symbols.length;
		if(is!=null){
			index=is.get_Order();
			scores=is.get_GeneScores(Symbols.length-1,0);
			for(int i=Symbols.length-1;i>=Symbols.length-number;i--){
				Element gene=doc.createElement(Consts.XML_TAG_GENE);
				gene.setAttribute(Consts.XML_TAG_ID, Symbols[index[i]]);
				XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_CHROMOSOME, ChrList[Chrs[index[i]]]);
				XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_FROM, Integer.toString(Starts[index[i]]+1));
				XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_TO, Integer.toString(Ends[index[i]]));
				XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_SCORE, Float.toString(Math.round(scores[index[i]]*10)/10));
				genes.appendChild(gene);
			}
		}
		return genes;
	}
	public static String get_Gene(int idx){
		return ChrList[Chrs[idx]]+"\t"+Starts[idx]+"\t"+Ends[idx]+"\t"+Symbols[idx];
	}
	public static int geneNum(){
		return Symbols.length;
	}
	public static int[] get_GeneRange(int idx){
		return new int[] {Starts[idx],Ends[idx]};
	}
	public static String get_GeneSymbol(int idx){
		return Symbols[idx];
	}
	public static int[] binarySearchOverlap(String chr,int start,int end){
		if(ChrMap.containsKey(chr))
			return binarySearchOverlap(ChrMap.get(chr),start,end);
		return null;
	}
	static int[] binarySearchOverlap(int chr,int start,int end){
		int low=ChrStarts[chr];
		int up=ChrEnds[chr];
		int mid=low;
		while(low<=up){
			mid=(up+low)/2;
			if(Starts[mid]<end && Ends[mid]>=start){
				int[] range=new int[2];
				range[0]=mid;
				while(range[0]>=ChrStarts[chr] && Starts[range[0]]<end && Ends[range[0]]>=start)
					range[0]--;
				range[0]++;
				range[1]=mid;
				while(range[1]<=ChrEnds[chr] && Starts[range[1]]<end && Ends[range[1]]>=start)
					range[1]++;
				range[1]--;
				return range;
			}
			else if(Starts[mid]>=end)
				up=mid-1;
			else
				low=mid+1;
		}
		return null;
	}
	static int binarySearchPrefix(String prefix){
		int low=0;
		int up=Symbols_sorted.length-1;
		int mid=low;
		while(low<=up){
			mid=(up+low)/2;
			if(Symbols_sorted[mid].startsWith(prefix)){
				for(int i=mid;i>=0;i--)
					if(!Symbols_sorted[i].startsWith(prefix))
						return i+1; 
				return 0;
			}
			else if(Symbols_sorted[mid].compareTo(prefix)<0)
				low=mid+1;
			else
				up=mid-1;
		}
		return -1;
	}
}