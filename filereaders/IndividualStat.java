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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import filereaders.gff.GVFReader;
import filereaders.individual.VariantAnalysis;
import filereaders.individual.vcf.Variant;

public class IndividualStat {
	float[] CytoScores;
	float[] GeneScores;
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
		for(int i=0;i<cytobands.length;i++){
			Cytobands[i]=cytobands[i].split("\t");
			CytoScores[i]=-1;
		}
		for(int i=0;i<GeneScores.length;i++){
			GeneScores[i]=-1;
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
		if(range!=null)
			for(int j=range[0];j<=range[1];j++){
				int[] subrange=Genes.get_GeneRange(j);
				Element ele_var=null;
				if(pvar.get_Type().equals(Consts.FORMAT_GVF))
					ele_var=new GVFReader(pvar.get_Path(chr)).write_gvf2variants(doc, pvar.get_ID(), chr, subrange[0]+1, subrange[1]);
				else if(pvar.get_Type().equals(Consts.FORMAT_VCF))
					ele_var=new VcfReader(pvar,chr).write_vcf2variants(doc, pvar.get_ID(), Consts.MODE_PACK, 0.5, chr, subrange[0]+1, subrange[1]);
				if(method == null){
					Element[] ele_annos=new Element[annos.length];
					for(int k=0;k<annos.length;k++)
						ele_annos[k]=bar[k].write_ba2elements(doc, annos[k].get_ID(), chr, subrange[0]+1, subrange[1], 0.5);
					GeneScores[j]=Math.round(calc_Score(doc, ref, ele_annos, ele_var, chr, Genes.get_GeneSymbol(j))*10)/10;
				}
				else
					GeneScores[j]=Math.round(Annovar.score_vars(ele_var, method, chr, ref)*10)/10;
				CytoScores[i]=CytoScores[i]>GeneScores[j]?CytoScores[i]:GeneScores[j];
			}
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
