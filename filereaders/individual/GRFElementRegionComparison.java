package filereaders.individual;

import static filereaders.Consts.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import filereaders.Rgb;
import filereaders.XmlWriter;
import filereaders.gff.GRFReader;
import filereaders.individual.vcf.Variant;


public class GRFElementRegionComparison {
	/**
	 * Just record each from and start of the variants
	 */
	private List<Variant> variantRange;
	private Document doc;
	
	/**
	 * 
	 * @param variants	VCF variants
	 */
	public GRFElementRegionComparison(Document doc, Element variants){
		getRangesFromVariant(variants);
		this.doc=doc;
	}
	
	
	/**
	 * If some of the grf overlap with one of the variant, we should add an attribute
	 * to the grf: variant values true
	 * 
	 * @param grf	GRF elements
	 */
	public void compareRegion(Element grf){
		int variantSize = variantRange.size();
		if(variantSize == 0)
			return;
		NodeList grfs = grf.getChildNodes();
		int grfSize = grfs.getLength();
		if(grfSize == 0)
			return;
		
		int index = 0;//current index of variant
		Variant cur = variantRange.get(index);
		Element ele = null;
		Element fromEle = null;
		Element toEle = null;
		int from = 0;
		int to = 0;
		int colornum=0;
		HashMap<String,String> colormap=new HashMap<String,String>();
		for(int i=0; i<grfSize; i++) {
			ele = (Element)grfs.item(i);
			fromEle = (Element)(ele.getElementsByTagName(XML_TAG_FROM).item(0));
			toEle = (Element)(ele.getElementsByTagName(XML_TAG_TO).item(0));
			String source=((Element)(ele.getElementsByTagName(XML_TAG_SOURCE).item(0))).getTextContent();
			if(!colormap.containsKey(source))
				colormap.put(source, Rgb.getColor(colornum++));
			from = Integer.parseInt(fromEle.getTextContent());
			to = Integer.parseInt(toEle.getTextContent());
			if(to < cur.getFrom())
				continue;
			if(from > cur.getTo()) {
				i--;
				index++;
				if(index >= variantSize)
					break;
				cur = variantRange.get(index);
				continue;
			}
			ele.setAttribute(XML_TAG_VARIANT, "true");
			if(ele.getElementsByTagName(XML_TAG_COLOR).getLength()==0)
				XmlWriter.append_text_element(doc, ele, XML_TAG_COLOR, new Rgb((String)colormap.get(source)).ToString());
		}
	}
	
	/**
	 * Extract all range from Variant.
	 * @param variants
	 */
	private void getRangesFromVariant(Element variants){
		variantRange = new ArrayList<Variant>();
		NodeList nodes = variants.getChildNodes();
		for(int i=0, num=nodes.getLength(); i<num; i++)
			variantRange.add(Variant.convertElement2Variant((Element)nodes.item(i)));
		Collections.sort(variantRange);
	}
}
