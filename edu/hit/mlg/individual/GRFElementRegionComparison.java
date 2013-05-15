package edu.hit.mlg.individual;

import static FileReaders.Consts.DATA_ROOT;
import static FileReaders.Consts.XML_TAG_FROM;
import static FileReaders.Consts.XML_TAG_TO;
import static FileReaders.Consts.XML_TAG_VARIANT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import FileReaders.gff.GRFReader;
import edu.hit.mlg.individual.vcf.Variant;

public class GRFElementRegionComparison {
	/**
	 * Just record each from and start of the variants
	 */
	private List<Variant> variantRange;
	
	/**
	 * 
	 * @param variants	VCF variants
	 */
	public GRFElementRegionComparison(Element variants){
		getRangesFromVariant(variants);
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
		
		System.out.println(variantSize);
		System.out.println(grfSize);
		
		int index = 0;//current index of variant
		Variant cur = variantRange.get(index);
		Element ele = null;
		Element fromEle = null;
		Element toEle = null;
		int from = 0;
		int to = 0;
		
		for(int i=0; i<grfSize; i++) {
			ele = (Element)grfs.item(i);
			fromEle = (Element)(ele.getElementsByTagName(XML_TAG_FROM).item(0));
			toEle = (Element)(ele.getElementsByTagName(XML_TAG_TO).item(0));
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
