package filereaders;

import java.io.*;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class CytobandReader {
	String[] cytobands;
	String cytoPath;

	public CytobandReader(String cyto) {
		File cyto_file = new File(cyto);
		ByteBufferChannel bbc = new ByteBufferChannel(cyto_file, 0,	cyto_file.length());
		String temp = bbc.ToString(Consts.DEFAULT_ENCODE);
		cytobands = temp.split("\n");
	}

	public Element write_cytobands(Document doc, String chr){
		return write_cytobands(doc,chr,null);
	}
	public Element write_cytobands(Document doc, IndividualStat is){
		return write_cytobands(doc,null,is);
	}
	public Element write_cytobands(Document doc){
		return write_cytobands(doc,null,null);
	}
	public Element write_cytobands(Document doc, String chr, IndividualStat is){
		Element Cytobands = doc.createElement(Consts.XML_TAG_CYTOBANDS);
		for (int i = 0; i < cytobands.length; i++) 
			if(chr==null || cytobands[i].startsWith(chr+"\t")){
				String[] cytoband_temp=cytobands[i].split("\t");
				Element Cytoband = doc.createElement(Consts.XML_TAG_CYTOBAND);
				Cytoband.setAttribute(Consts.XML_TAG_ID, cytoband_temp[3]);
				Cytoband.setAttribute(Consts.XML_TAG_GIESTAIN, cytoband_temp[4]);
				if(chr==null)
					XmlWriter.append_text_element(doc, Cytoband, Consts.XML_TAG_CHROMOSOME, cytoband_temp[0]);
				XmlWriter.append_text_element(doc, Cytoband, Consts.XML_TAG_FROM, String.valueOf(Long.parseLong(cytoband_temp[1])+1));
				XmlWriter.append_text_element(doc, Cytoband, Consts.XML_TAG_TO, String.valueOf(Long.parseLong(cytoband_temp[2])));
				if(is!=null && is.get_CytoScores(i, i)[0]>=0)
					XmlWriter.append_text_element(doc, Cytoband, Consts.XML_TAG_SCORE, String.valueOf(Math.round(is.get_CytoScores(i, i)[0]*10)/10));
				Cytobands.appendChild(Cytoband);
			}
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Cytobands);
		return Cytobands;
	}
	public Element write_cytoband(Document doc, String chr, String id, IndividualStat is, FastaReader ref, Annotations pvar, String method){
		Element Cytobands = doc.createElement(Consts.XML_TAG_CYTOBANDS);
		for (int i = 0; i < cytobands.length; i++) 
			if(cytobands[i].startsWith(chr+"\t")&&cytobands[i].indexOf("\t"+id+"\t")>=0){
				String[] cytoband_temp=cytobands[i].split("\t");
				Element Cytoband = doc.createElement(Consts.XML_TAG_CYTOBAND);
				Cytoband.setAttribute(Consts.XML_TAG_ID, cytoband_temp[3]);
				Cytoband.setAttribute(Consts.XML_TAG_GIESTAIN, cytoband_temp[4]);
				XmlWriter.append_text_element(doc, Cytoband, Consts.XML_TAG_FROM, String.valueOf(Long.parseLong(cytoband_temp[1])+1));
				XmlWriter.append_text_element(doc, Cytoband, Consts.XML_TAG_TO, String.valueOf(Long.parseLong(cytoband_temp[2])));
				if(is!=null&&pvar!=null){	
					if(is.get_CytoScores(i, i)[0]<0)
						is.fill_Cyto(chr, id, ref, pvar, method);
					XmlWriter.append_text_element(doc, Cytoband, Consts.XML_TAG_SCORE, String.valueOf(Math.round(is.get_CytoScores(i, i)[0]*10)/10));
				}
				Cytobands.appendChild(Cytoband);
			}
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Cytobands);
		return Cytobands;
	}
	public String[] getCytobands(){
		return cytobands;
	}
}