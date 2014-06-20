package filereaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import filereaders.gff.GVFReader;
import filereaders.individual.VariantAnalysis;
import filereaders.individual.vcf.Variant;

public class IndividualStat {
	float[] CytoScores;
	float[] GeneScores;
	Integer[] Sorted_Order;
	String[][] Cytobands;
	public final static int A_LEVEL=100;
	public final static int B_LEVEL=10;
	public final static int C_LEVEL=10;
	public final static int D_LEVEL=1;
	public final static int E_LEVEL=0;
	Annotations[] annos;
	
	public IndividualStat(String[] cytobands, Annotations[] annos){
		Cytobands=new String[cytobands.length][];
		CytoScores=new float[cytobands.length];
		GeneScores=new float[Genes.geneNum()];
		Sorted_Order=new Integer[Genes.geneNum()];
		for(int i=0;i<cytobands.length;i++){
			Cytobands[i]=cytobands[i].split("\t");
			CytoScores[i]=-1;
		}
		for(int i=0;i<GeneScores.length;i++){
			GeneScores[i]=-1;
			Sorted_Order[i]=i;
		}
		this.annos=annos;
	}
	public void load_Stat(String filepath){
		BufferedReader in=null;
		try{
			if (filepath.startsWith("http://")||filepath.startsWith("https://")||filepath.startsWith("ftp://")){
				URL url=new URL(filepath);
				in=new BufferedReader(new InputStreamReader(url.openStream()));
			}
			else{
				in=new BufferedReader(new FileReader(filepath));
			}
			if(in!=null){
				String line;
				int i=0;
				int cytolen=CytoScores.length;
				while((line=in.readLine())!=null){
					String[] temp=line.split("\t");
					if(i<cytolen)
						CytoScores[i]=Float.parseFloat(temp[4]);
					else
						GeneScores[i-cytolen]=Float.parseFloat(temp[4]);
					i++;
				}
				in.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	void sort_Stat(){
		for(int i=0;i<Sorted_Order.length;i++)
			Sorted_Order[i]=i;
		Arrays.sort(Sorted_Order, new Comparator<Integer>(){
			@Override public int compare(Integer o1, Integer o2){
				return Float.compare(GeneScores[o1], GeneScores[o2]);
			}
		});
	}
	public Integer[] get_Order(){
		sort_Stat();
		return Sorted_Order;
	}
	public File save_Stat(String session){
		File ftemp=null;
		BufferedWriter out;
		try{
			ftemp=new File(System.getProperty("java.io.tmpdir")+"/"+session+".stat");
			out=new BufferedWriter(new FileWriter(ftemp));
			for(int i=0;i<Cytobands.length;i++)
				out.write(Cytobands[i][0]+"\t"+Cytobands[i][1]+"\t"+Cytobands[i][2]+"\t"+Cytobands[i][3]+"\t"+CytoScores[i]+"\n");
			for(int i=0;i<GeneScores.length;i++)
				out.write(Genes.get_Gene(i)+"\t"+GeneScores[i]+"\n");
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			ftemp.deleteOnExit();
		}
		return ftemp;
	}
	public float[] get_GeneScores(int up, int low){
		float[] scores=new float[up-low+1];
		for(int i=low;i<=up;i++){
			scores[i-low]=GeneScores[i];
		}
		return scores;
	}
	public float[] get_CytoScores(int up, int low){
		float[] scores=new float[up-low+1];
		for(int i=low;i<=up;i++){
			scores[i-low]=CytoScores[i];
		}
		return scores;
	}
	public void fill_Cyto(String chr, String id, FastaReader ref, Annotations pvar, String method){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		int start=0,end=0;
		int i=0;
		BasicAnnosReader[] bar=new BasicAnnosReader[annos.length];
		for(i=0;i<annos.length;i++)
			bar[i]=new BasicAnnosReader(annos[i].get_Path(chr));
		for(i=0;i<Cytobands.length;i++){
			if(Cytobands[i][0].equals(chr)&&Cytobands[i][3].equals(id)){
				start=Integer.parseInt(Cytobands[i][1])+1;
				end=Integer.parseInt(Cytobands[i][2]);
				break;
			}
		}
		int[] range=Genes.binarySearchOverlap(chr, start, end);
		CytoScores[i]=0;
		
		boolean ifTrioAvailable = false;
		VcfSample vcfSample = null;
		if(method!=null && method.equals("Family") && pvar.get_Type().equals(Consts.FORMAT_VCF) && pvar.has_Parameter(Consts.VCF_HEADER_SAMPLE)){
			vcfSample = (VcfSample)pvar.get_Parameter(Consts.VCF_HEADER_SAMPLE);
			ifTrioAvailable = vcfSample.ifTrioAvailable();
			if(ifTrioAvailable){
				String set_b = vcfSample.getSelectedNames()[0];
				String set_a = null;
				String[] ps = vcfSample.getParents();
				if(ps!=null)
					set_a = ps[0]+":"+ps[1];
				Element ele_var = new VcfReader(pvar,chr).write_difference(doc, pvar.get_ID(), set_a, set_b, chr, start, end);
				if (ele_var.getElementsByTagName(Consts.XML_TAG_VARIANT).getLength()>0){
					CytoScores[i]=(int)CytoScores[i]|1;
					CytoScores[i]=(int)CytoScores[i]|1<<4;
				}
			}
		}
		/* For method == Family
		 *-1 : Uninitialized
		 * 0 : Unknown
		 * 0x1  : has event |1
		 * 0x2  : allele 1 |1<<1
		 * 0x4  : allele 2 |1<<2
		 * 0x8  : has Compound heterozygous |1<<3
		 * 0x10 : has De novo mutation |1<<4
		 * 0x20 : Coding region |1<<5
		 * 0x40 : Non-synonymous |1<<6
		 * 0x80 : Rare |1<<7
		 * 0x100 : Reserved
		 * 0x200 : Reserved
		 * 0x400 : Reserved
		 */
		
		if(range!=null){
			VcfReader vr = null;
			GVFReader gr = null;
			if(pvar.get_Type().equals(Consts.FORMAT_VCF))
				vr = new VcfReader(pvar,chr);
			else if(pvar.get_Type().equals(Consts.FORMAT_GVF))
				gr = new GVFReader(pvar.get_Path(chr));
			for(int j=range[0];j<=range[1];j++){
				GeneScores[j]=0;
				int[] subrange=Genes.get_GeneRange(j);
				Element ele_var=null;
				if(method == null){
					if(pvar.get_Type().equals(Consts.FORMAT_GVF))
						ele_var=gr.write_gvf2variants(doc, pvar.get_ID(), chr, subrange[0]+1, subrange[1]);
					else if(pvar.get_Type().equals(Consts.FORMAT_VCF))
						ele_var=vr.write_vcf2variants(doc, pvar.get_ID(), Consts.MODE_PACK, 0.5, chr, subrange[0]+1, subrange[1]);
					Element[] ele_annos=new Element[annos.length];
					for(int k=0;k<annos.length;k++)
						ele_annos[k]=bar[k].write_ba2elements(doc, annos[k].get_ID(), chr, subrange[0]+1, subrange[1], 0.5);
					GeneScores[j]=Math.round(calc_Score(doc, ref, ele_annos, ele_var, chr, Genes.get_GeneSymbol(j))*10)/10;
				}
				else if(ifTrioAvailable){
					Element[] ele_annos=new Element[annos.length];
					for(int k=0;k<annos.length;k++)
						ele_annos[k]=bar[k].write_ba2elements(doc, annos[k].get_ID(), chr, subrange[0]+1, subrange[1], 0.5);
					
					Element[] ele_vars = vr.write_trio(doc, chr, subrange[0]+1, subrange[1]);
					GeneScores[j]=(int)GeneScores[j]|calc_Mut(doc,ref,ele_annos,ele_vars,chr,Genes.get_GeneSymbol(j));
				}
				else{
					if(pvar.get_Type().equals(Consts.FORMAT_GVF))
						ele_var=gr.write_gvf2variants(doc, pvar.get_ID(), chr, subrange[0]+1, subrange[1]);
					else if(pvar.get_Type().equals(Consts.FORMAT_VCF))
						ele_var=vr.write_vcf2variants(doc, pvar.get_ID(), Consts.MODE_PACK, 0.5, chr, subrange[0]+1, subrange[1]);
					GeneScores[j]=Math.round(Annovar.score_vars(ele_var, method, chr, ref)*10)/10;
				}
				
				if(ifTrioAvailable)
					CytoScores[i]=(int)CytoScores[i]|(int)GeneScores[j];
				else
					CytoScores[i]=CytoScores[i]>GeneScores[j]?CytoScores[i]:GeneScores[j];
			}
		}
	}
	int calc_Mut(Document doc, FastaReader rr, Element[] annos, Element[] pvars, String chr, String symbol){
		int score = 0;
		boolean maternal_comp = false;
		boolean paternal_comp = false;
		
		for(int vs=0;vs<3;vs++){ 
			// filter those 1|1 variants, which are highly improbable for both denovo mutation and compound heterozygous
			if(pvars[vs]==null)
				continue;
			NodeList var_temp = pvars[vs].getElementsByTagName(Consts.XML_TAG_VARIANT);
			for(int v=0;v<var_temp.getLength();v++)
				if(((Element)var_temp.item(v)).getAttribute(Consts.XML_TAG_HOMO).indexOf("0")<0)
					pvars[vs].removeChild(var_temp.item(v));
		}
		
		try{
			Element[] pannos=new Element[annos.length];
			
			for(int type=0;type<3;type++){
				if(pvars[type]==null)
					continue;
				for(int i=0;i<annos.length;i++){
					VariantAnalysis ee = new VariantAnalysis(doc, rr, annos[i], null, pvars[type], chr);
					pannos[i]=ee.easydeal();
					ArrayList<Integer> temp_score=new ArrayList<Integer>();
					for(int j=0;j<pannos[i].getChildNodes().getLength();j++){
						Element current_ele=(Element) pannos[i].getChildNodes().item(j);
						if(current_ele.getAttribute(Consts.XML_TAG_SYMBOL).equals(symbol)){
							if(current_ele.getElementsByTagName(Consts.XML_TAG_STATUS).getLength()>0)
								if(current_ele.getElementsByTagName(Consts.XML_TAG_STATUS).item(0).getTextContent().indexOf(Variant.LARGE_VARIANTION)>=0)
									if(type == 0){
										score = score|1;
										score = score|1<<4;
										score = score|1<<5;
										score = score|1<<6;
									}
									else if (type == 1)
										paternal_comp = true;
									else if (type == 2)
										maternal_comp = true;
							NodeList vs = current_ele.getElementsByTagName(Consts.XML_TAG_VARIANT);
							for(int k=0;k<vs.getLength();k++){
								String letter=((Element)vs.item(k)).getElementsByTagName(Consts.XML_TAG_LETTER).item(0).getTextContent();
								String[] letters=letter.split(":");
								if (letter.indexOf("^")>=0||letter.indexOf("$")>=0||letter.indexOf("#")>=0
										||letter.indexOf("(")>=0||letter.indexOf(")")>=0
										||letter.indexOf("_")>=0||letters[0].length()!=letters[1].length()
										||!letters[0].equals(letters[1])){
									if(type == 0){
										score = score|1;
										score = score|1<<4;
										score = score|1<<5;
										score = score|1<<6;
									}
									else if (type == 1)
										paternal_comp = true;
									else if (type == 2)
										maternal_comp = true;
								}
								else if(letters[0].equals(letters[1])){
									if(type == 0){
										score = score|1;
										score = score|1<<4;
										score = score|1<<5;
									}
								}
							}
						}
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		if(paternal_comp && maternal_comp){
			score = score|1;
			score = score|1<<3;
		}
		return score;
	}
	float calc_Score(Document doc, FastaReader rr, Element[] annos, Element pvar, String chr, String symbol){
		int score=0;
		int available=0;
		try{
			Element[] pannos=new Element[annos.length];
			for(int i=0;i<annos.length;i++){
				VariantAnalysis ee = new VariantAnalysis(doc, rr, annos[i], null, pvar, chr);
				pannos[i]=ee.easydeal();
				ArrayList<Integer> temp_score=new ArrayList<Integer>();
				for(int j=0;j<pannos[i].getChildNodes().getLength();j++){
					Element current_ele=(Element) pannos[i].getChildNodes().item(j);
					if(current_ele.getAttribute(Consts.XML_TAG_SYMBOL).equals(symbol)){
						int score_temp=0;
						if(current_ele.getElementsByTagName(Consts.XML_TAG_STATUS).getLength()>0)
							if(current_ele.getElementsByTagName(Consts.XML_TAG_STATUS).item(0).getTextContent().indexOf(Variant.LARGE_VARIANTION)>=0)
								score_temp+=A_LEVEL;
						NodeList vs = current_ele.getElementsByTagName(Consts.XML_TAG_VARIANT);
						for(int k=0;k<vs.getLength();k++){
							String letter=((Element)vs.item(k)).getElementsByTagName(Consts.XML_TAG_LETTER).item(0).getTextContent();
							String[] letters=letter.split(":");
							if (letter.indexOf("^")>=0||letter.indexOf("$")>=0||letter.indexOf("#")>=0||letter.indexOf("(")>=0||letter.indexOf(")")>=0){
								score_temp+=A_LEVEL;
							}
							else if(letter.indexOf("_")>=0||letters[0].length()!=letters[1].length()){
								score_temp+=B_LEVEL;
							}
							else if(!letters[0].equals(letters[1])){
								score_temp+=C_LEVEL;
							}
							else if(letters[0].equals(letters[1])){
								score_temp+=D_LEVEL;
							}
						}
						temp_score.add(score_temp);
					}
				}
				if(temp_score.size()>0){
					available++;
					for(int j=0;j<temp_score.size();j++)
						score=score>temp_score.get(j)?score:temp_score.get(j);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		if(available>0){
			return (float)score/(float)available;
		}
		return 0;
	}
}
