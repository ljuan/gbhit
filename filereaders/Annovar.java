package filereaders;


import static filereaders.Consts.XML_TAG_IFP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import filereaders.individual.vcf.Variant;

class Annovar{
	public final static Map<String, String> ScoreMeth = new HashMap<String, String>();
	static String link;
	static{
		Annotations annovar = CfgReader.getBasic(Consts.CURRENT_ASSEMBLY,Consts.FORMAT_ANNOVAR);
		link = annovar.get_Path();
		ScoreMeth.put("PGB", null);
		ScoreMeth.put("SIFT", "ljb_sift");
		ScoreMeth.put("PolyPhen2", "ljb_pp2");
	}
	Annovar(){
	}
	
	public static Element score_vars(Document doc, Element variants, String method, String chr, FastaReader fr){
		List<Variant> variantlist=xml2variants(variants);
		try {
			File tempin = File.createTempFile("Annovar", ".txt");
			variants2file(variantlist,tempin,chr,fr);
			String[] cmd = {"/usr/bin/perl", link+"/annotate_variation.pl", "-filter", "-dbtype", method, "-buildver", Consts.CURRENT_ASSEMBLY,
					"-outfile", tempin.getAbsolutePath(), tempin.getAbsolutePath(), link+"/humandb/"};
			int exitValue = 0;
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			exitValue = proc.exitValue();
			if(exitValue == 0){
				File tempout1=new File(tempin.getAbsolutePath()+"."+Consts.CURRENT_ASSEMBLY+"_"+method+"_dropped");
				File tempout2=new File(tempin.getAbsolutePath()+"."+Consts.CURRENT_ASSEMBLY+"_"+method+"_filtered");
				File tempout3=new File(tempin.getAbsolutePath()+".invalid_input");
				File tempout4=new File(tempin.getAbsolutePath()+".log");
				if(tempout1.exists()&&tempout1.isFile()){
					file2variants(variantlist,tempout1);
					tempout1.delete();
				}
				if(tempout2.exists()&&tempout2.isFile())
					tempout2.delete();
				if(tempout3.exists()&&tempout3.isFile())
					tempout3.delete();
				if(tempout4.exists()&&tempout4.isFile())
					tempout4.delete();
			}
			tempin.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return variants2xml(variantlist,doc,variants.getAttribute(Consts.XML_TAG_ID),variants.getAttribute(Consts.XML_TAG_SUPERID));
	}
	public static float score_vars(Element variants, String method, String chr, FastaReader fr){
		List<Variant> variantlist=xml2variants(variants);
		float score = 0;
		try {
			File tempin = File.createTempFile("Annovar", ".txt");
			variants2file(variantlist,tempin,chr,fr);
			String[] cmd = {"/usr/bin/perl", link+"/annotate_variation.pl", "-filter", "-dbtype", method, "-buildver", Consts.CURRENT_ASSEMBLY,
					"-outfile", tempin.getAbsolutePath(), tempin.getAbsolutePath(), link+"/humandb/"};
			int exitValue = 0;
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			exitValue = proc.exitValue();
			if(exitValue == 0){
				File tempout1=new File(tempin.getAbsolutePath()+"."+Consts.CURRENT_ASSEMBLY+"_"+method+"_dropped");
				File tempout2=new File(tempin.getAbsolutePath()+"."+Consts.CURRENT_ASSEMBLY+"_"+method+"_filtered");
				File tempout3=new File(tempin.getAbsolutePath()+".invalid_input");
				File tempout4=new File(tempin.getAbsolutePath()+".log");
				if(tempout1.exists()&&tempout1.isFile()){
					BufferedReader br = new BufferedReader(new FileReader(tempout1));
					String line;
					while((line = br.readLine())!=null){
						float score1=Float.parseFloat(line.split("\t")[1]);
						if(score1>=0&&score1<1)
							score+=-10*Math.log10(1-score1);
						else if (score1 == 1)
							score+=100;
					}
					br.close();
					tempout1.delete();
				}
				if(tempout2.exists()&&tempout2.isFile())
					tempout2.delete();
				if(tempout3.exists()&&tempout3.isFile())
					tempout3.delete();
				if(tempout4.exists()&&tempout4.isFile())
					tempout4.delete();
			}
			tempin.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return score;
	}
	public static Element variants2xml(List<Variant> variantlist, Document doc, String id, String superid){
		Element variants = doc.createElement(Consts.XML_TAG_VARIANTS);
		variants.setAttribute(Consts.XML_TAG_ID, id);
		variants.setAttribute(Consts.XML_TAG_SUPERID, superid);
		for (Variant v : variantlist)
			v.write2xml(doc, variants);
		return variants;
	}
	public static List<Variant> xml2variants(Element variants){
		List<Variant> list = new ArrayList<Variant>();
		NodeList nodeList = variants.getChildNodes();
		for (int i = 0, num = nodeList.getLength(); i < num; i++)
			list.add(Variant.convertElement2Variant((Element)nodeList.item(i)));
		return list;
	}
	public static void variants2file(List<Variant> variantlist, File file, String chr, FastaReader fr) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		String chr_simple = chr.startsWith("chr")?chr.substring(3):chr;
		if("M".equals(chr_simple))
			chr_simple="MT";
		for(Variant v : variantlist){
			if(v.getType().equals(Consts.VARIANT_TYPE_SNV))
				bw.write(chr_simple+"\t"+v.getFrom()+"\t"+v.getTo()+"\t"+fr.extract_char(chr, v.getFrom())+"\t"+v.getLetter()+"\n");
			else if(v.getType().equals(Consts.VARIANT_TYPE_INSERTION))
				if(v.getLetter()!=null && v.getLetter().length()<50)
					bw.write(chr_simple+"\t"+v.getFrom()+"\t"+v.getFrom()+"\t-\t"+v.getLetter()+"\n");
			else if(v.getType().equals(Consts.VARIANT_TYPE_DELETION))
				if(v.getTo()-v.getFrom()<50)
					bw.write(chr_simple+"\t"+v.getFrom()+"\t"+v.getTo()+"\t"+fr.extract_seq(chr, (long)v.getFrom(), (long)v.getTo())+"\t-\n");
				else
					bw.write(chr_simple+"\t"+v.getFrom()+"\t"+v.getTo()+"\t0\t-\n");
			else
				continue;
		}
		bw.close();
	}
	public static void file2variants(List<Variant> variantlist, File file) throws NumberFormatException, IOException{
		String line;
		Map<String, Float> dropped = new HashMap<String, Float>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		while((line = br.readLine())!=null){
			String[] linetemp=line.split("\t");
			dropped.put(linetemp[3]+"_"+linetemp[4]+"_"+linetemp[6], Float.parseFloat(linetemp[1]));
		}
		br.close();
		
		String typetemp;
		String fromtemp;
		String totemp;
		String lettertemp;
		for(Variant v : variantlist){
			typetemp=v.getType();
			fromtemp=String.valueOf(v.getFrom());
			totemp=String.valueOf(v.getTo());
			lettertemp=v.getLetter();
			if(typetemp.equals(Consts.VARIANT_TYPE_DELETION) && dropped.containsKey(fromtemp+"_"+totemp+"_-")){
				float score = dropped.get(fromtemp+"_"+totemp+"_-");
				if(score>=0&&score<1)
					v.setEffect((int)Math.round(-10*Math.log10(1-score)));
				else if (score == 1)
					v.setEffect(100);
			}
			else if(lettertemp==null || lettertemp.length() > 50)
				continue;
			else if(typetemp.equals(Consts.VARIANT_TYPE_SNV) && dropped.containsKey(fromtemp+"_"+totemp+"_"+lettertemp)){
				float score = dropped.get(fromtemp+"_"+totemp+"_"+lettertemp);
				if(score>=0&&score<1)
					v.setEffect((int)Math.round(-10*Math.log10(1-score)));
				else if (score == 1)
					v.setEffect(100);
			}
			else if(typetemp.equals(Consts.VARIANT_TYPE_INSERTION) && dropped.containsKey(fromtemp+"_"+totemp+"_"+lettertemp)){
				float score = dropped.get(fromtemp+"_"+fromtemp+"_"+lettertemp);
				if(score>=0&&score<1)
					v.setEffect((int)Math.round(-10*Math.log10(1-score)));
				else if (score == 1)
					v.setEffect(100);
			}
			else
				continue;
		}
	}
}