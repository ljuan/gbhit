package FileReaders;

import java.util.*;
import java.io.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CytobandReader implements Consts {
	String cytobands[];
	String cytoPath;

	CytobandReader(String cyto) {
		File cyto_file = new File(cyto);
		ByteBufferChannel bbc = new ByteBufferChannel(cyto_file, 0,	cyto_file.length());
		String temp = bbc.ToString(DEFAULT_ENCODE);
		cytobands = temp.split("\n");
	}

	Element write_cytobands(Document doc, String chr, Annotations track){
		if (!track.has_Parameter("pChr")||!((String)track.get_Parameter("pChr")).equals(chr)){
			Element Cytobands = doc.createElement(XML_TAG_CYTOBANDS);
			for (int i = 0; i < cytobands.length; i++) 
				if(cytobands[i].startsWith(chr+"\t")){
					String[] cytoband_temp=cytobands[i].split("\t");
					Element Cytoband = doc.createElement(XML_TAG_CYTOBAND);
					Cytoband.setAttribute(XML_TAG_ID, cytoband_temp[3]);
					Cytoband.setAttribute(XML_TAG_GIESTAIN, cytoband_temp[4]);
					XmlWriter.append_text_element(doc, Cytoband, XML_TAG_FROM, String.valueOf(Long.parseLong(cytoband_temp[1])+1));
					XmlWriter.append_text_element(doc, Cytoband, XML_TAG_TO, String.valueOf(Long.parseLong(cytoband_temp[2])));
					Cytobands.appendChild(Cytoband);
				}
			doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Cytobands);
			track.set_Parameters("pChr", chr);
			return Cytobands;
		}
		else
			return null;
	}
}