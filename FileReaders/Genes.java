package FileReaders;

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
		File hgnc=new File(CfgReader.getBasicGenes("hg19").get_Path());
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
		Element genes = doc.createElement(Consts.XML_TAG_GENES);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(genes); 
		if(ChrMap.containsKey(chr)){
			int[] range=binarySearchOverlap(ChrMap.get(chr),start,end);
			if(range!=null){
				for(int i=range[0];i<=range[1];i++){
					Element gene=doc.createElement(Consts.XML_TAG_GENE);
					gene.setAttribute(Consts.XML_TAG_ID, Symbols[i]);
					XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_FROM, Integer.toString(Starts[i]+1));
					XmlWriter.append_text_element(doc, gene, Consts.XML_TAG_TO, Integer.toString(Ends[i]));
					genes.appendChild(gene);
				}
			}
		}
		return genes;
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