package FileReaders.gff;

import static FileReaders.Consts.*;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.hit.mlg.Tools.StringSplit;

import FileReaders.TabixReader;
import FileReaders.XmlWriter;

public class GDFReader {
	private String path = null;

	public GDFReader(String path) {
		this.path = path;
	}

	public Element get_detail(Document doc,String track, String id, String chr, int start, int end){
		Element elements = doc.createElement(XML_TAG_ELEMENTS);
		try{
			elements.setAttribute(XML_TAG_ID, track);
			doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(elements); // Elements
			TabixReader tabix = new TabixReader(this.path);
			String chrom = tabix.hasChromPrefix() ? chr : chr.substring(3);
			if ("M".equalsIgnoreCase(chrom)) {
				chrom = "MT";
			}
			TabixReader.Iterator Query = tabix.query(chrom + ":" + start + "-" + end);
			String line = null;
			StringSplit split = new StringSplit('\t');
			StringSplit DNameSplit = new StringSplit(';');
			StringSplit equalSignSplit = new StringSplit('=');
			String eight = null;
			String DName = null;
			Element element = null;
			if (Query != null) 
				while ((line = Query.next()) != null) {
					split.split(line);
					eight = split.getResultByIndex(8);
					DNameSplit.split(eight);
					DName = equalSignSplit.split(DNameSplit.getResultByIndex(0)).getResultByIndex(1);
					if(DName.equals(id)){
						element = doc.createElement(XML_TAG_ELEMENT);
						element.setAttribute(XML_TAG_ID, DName);
						element.setAttribute(XML_TAG_TYPE, split.getResultByIndex(2));
						for(int i=0;i<DNameSplit.getResultNum();i++){
							equalSignSplit.split(DNameSplit.getResultByIndex(i));
							if (equalSignSplit.getResultByIndex(0).equals(GDF_GENEID))
								element.setAttribute(XML_TAG_SYMBOL, equalSignSplit.getResultByIndex(1));
							else if (equalSignSplit.getResultByIndex(0).equals(GDF_SNPID))
								element.setAttribute(XML_TAG_VARIANT, equalSignSplit.getResultByIndex(1));
						}
						StringBuilder builder = new StringBuilder();
						builder.append(XML_TAG_CHROMOSOME);
						builder.append("=");
						builder.append(split.getResultByIndex(0));
						builder.append(";");
						builder.append(XML_TAG_SOURCE);
						builder.append("=");
						builder.append(split.getResultByIndex(1));
						builder.append(";");
						builder.append(XML_TAG_SCORE);
						builder.append("=");
						builder.append(split.getResultByIndex(5));
						builder.append(";");
						builder.append(XML_TAG_DIRECTION);
						builder.append("=");
						builder.append(split.getResultByIndex(6));
						builder.append(";");
						builder.append(XML_TAG_FRAME);
						builder.append("=");
						builder.append(split.getResultByIndex(7));
						builder.append(";");
						builder.append(split.getResultByIndex(8));
						XmlWriter.append_text_element(doc, element, XML_TAG_DESCRIPTION, builder.toString());
						elements.appendChild(element);
					}
				}
		} catch(Exception e){
			e.printStackTrace();
		}
		return elements;
	}
	public Element write_gdf2elements(Document doc, String track, String chr, int start, int end) throws IOException {
		Element elements = doc.createElement(XML_TAG_ELEMENTS);
		elements.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(elements); // Elements
		TabixReader tabix = new TabixReader(this.path);
		String chrom = tabix.hasChromPrefix() ? chr : chr.substring(3);
		if ("M".equalsIgnoreCase(chrom)) {
			chrom = "MT";
		}
		TabixReader.Iterator Query = tabix.query(chrom + ":" + start + "-" + end);
		String line = null;
		StringSplit split = new StringSplit('\t');
		StringSplit DNameSplit = new StringSplit(';');
		StringSplit equalSignSplit = new StringSplit('=');
		Element element = null;
		if (Query != null) {
			while ((line = Query.next()) != null) {
				split.split(line);
				element = doc.createElement(XML_TAG_ELEMENT);
				String DName = DNameSplit.split(split.getResultByIndex(8)).getResultByIndex(0);
				element.setAttribute(XML_TAG_ID, equalSignSplit.split(DName).getResultByIndex(1));
				element.setAttribute(XML_TAG_TYPE, split.getResultByIndex(2));
				XmlWriter.append_text_element(doc, element, XML_TAG_FROM, split.getResultByIndex(3));
				XmlWriter.append_text_element(doc, element, XML_TAG_TO, split.getResultByIndex(4));
				String[] attributes=DNameSplit.split(split.getResultByIndex(8)).getResult();
				for(int i=0;i<attributes.length;i++){
					String[] attribute=equalSignSplit.split(attributes[i]).getResult();
					if (attribute[0].equals(GDF_GENEID))
						element.setAttribute(XML_TAG_SYMBOL, attribute[1]);
					else if (attribute[0].equals(GDF_SNPID))
						element.setAttribute(XML_TAG_VARIANT, attribute[1]);
				}
				elements.appendChild(element);
			}
		}
		return elements;
	}
}
