package filereaders;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import filereaders.individual.vcf.Variant;

class Annovar{
	public final static int SIFT = 1;
	public final static int POLYPHEN = 2;
	public final static int POLYPHEN2 = 3;
	public final static int PHYLOP = 4;
	public final static int SIPHY = 5;
	static String link;
	static{
		Annotations annovar = CfgReader.getBasicAnnovar(Consts.CURRENT_ASSEMBLY);
		link = annovar.get_Path();
	}
	Annovar(){
	}
	
	public static Element score_vars(Document doc, Element variants, int method, String chr, FastaReader fr, String anno){
		List<Variant> variantlist=xml2variants(variants);
		try {
			File tempin = File.createTempFile("Annovar", "in.txt");
			File tempout = File.createTempFile("Annovar", "out.txt");
			variants2file(variantlist,tempin,chr,fr);
			String genetype=null;
			if(anno.equals("knownGene"))
				genetype="knowngene";
			else if(anno.equals("ensemblGene"))
				genetype="ensgene";
			String cmdString = "perl "+link+"/summarize_annovar.pl -verdbsnp 137 -veresp 6500si -remove -checkfile OFF -buildver "+Consts.CURRENT_ASSEMBLY;
			if(genetype!=null)
				cmdString+=" -genetype "+genetype;
			cmdString+=" -outfile "+tempout.getAbsolutePath()+" "+tempin.getAbsolutePath()+" "+link+"/humandb/";
			Runtime.getRuntime().exec(cmdString);
			file2variants(variantlist,tempout,method);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return variants2xml(variantlist,doc,variants.getAttribute(Consts.XML_TAG_ID),variants.getAttribute(Consts.XML_TAG_SUPERID));
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
	public static void file2variants(List<Variant> variantlist, File file, int method) throws NumberFormatException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		String typetemp;
		String line;
		for(Variant v : variantlist){
			typetemp=v.getType();
			if(typetemp.equals(Consts.VARIANT_TYPE_SNV)||typetemp.equals(Consts.VARIANT_TYPE_INSERTION)||typetemp.equals(Consts.VARIANT_TYPE_DELETION)){
				if((line = br.readLine())!=null)
					v.setEffect(Integer.parseInt(line.split("\t")[method]));
				else
					break;
			}
			else
				continue;
		}
		br.close();
	}
}