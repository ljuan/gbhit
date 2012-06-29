package FileReaders;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

class XmlWriter implements Consts{
	Document doc;
	XmlWriter(){
		init(DATA_ROOT);
	}
	XmlWriter(String roottag){
		init(roottag);
	}
	static Document init(String roottag){
		Document doc;
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db=dbf.newDocumentBuilder();
			doc=db.newDocument();
			Element root=doc.createElement(roottag);
			doc.appendChild(root);
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	static Element append_text_element(Document doc,Node parent,String tag,String text){
		Element offspring=doc.createElement(tag);
		offspring.appendChild(doc.createTextNode(text));
		parent.appendChild(offspring);
		return offspring;
	}
	static String xml2string(Document doc){
		String xml="";
		try{
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			xml= writer.getBuffer().toString().replaceAll("\n|\r", "");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return xml;
	}
}
/*
 * We have moved functional writeXML method to corresponding classes:
 * 1 write_bed2elements to BedReader/BedReaderTabix,
 * 2 write_vcf2variants to VcfReader,
 * 3 write_metalist to CfgReader,
 * 4 write_sequence to FastaReader
 * We will further deploy:
 * 5 write_bam2reads in BamReader,
 * 6 write_gff2elements in GffReader,
 * 7 write_wig2values in WigReader,
 * 8 write_bigwig2values in BigWigReader
 * 9 write_bb2values in bbReader, etc.
 * We keep some common method: xml2string, init, append_text_element in this class and make them static.
 */
/*	Element write_basic(Document doc,String text, String tag){
		return XmlWriter.append_text_element(doc,doc.getElementsByTagName(DATA_ROOT).item(0),tag,text);
	}
	void write_metalist(String[] metalist,String metatype){
		StringBuffer temp=new StringBuffer();
		for(int i=0;i<metalist.length;i++){
			temp.append(metalist[i]);
			if(i<metalist.length-1)
				temp.append(",");
		}
		append_text_element(doc.getElementsByTagName(META_ROOT).item(0),metatype,temp.toString());
	}

	void write_sequence(String seq, String id){
		Element sequence=append_text_element(doc.getElementsByTagName(DATA_ROOT).item(0),XML_TAG_SEQUENCE,seq);
		sequence.setAttribute(XML_TAG_ID, id);
	}
	void write_vcf2variants(Vcf[] vcf, String track, String mode, double bpp){//bpp:bases per pixel
		Element Variants=doc.createElement(XML_TAG_VARIANTS);
		Variants.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Variants);
		long lastpos=0;
		for(int i=0;i<vcf.length;i++){
			String[] alt_temp=vcf[i].Alt.split(",");
			int range=vcf[i].Ref.length()-alt_temp[0].length();
			if(range<0)
				range=0;
			if(mode.equals(MODE_DENSE)&&vcf[i].Pos+range-lastpos<bpp)
				continue;
			Element Variant=doc.createElement(XML_TAG_VARIANT);
			Variant.setAttribute(XML_TAG_ID, vcf[i].ID);
			if(alt_temp.length>1)
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_MULTIPLE);
			else if(alt_temp[0].length()<vcf[i].Ref.length())
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_DELETION);
			else if(alt_temp[0].length()>vcf[i].Ref.length())
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_INSERTION);
			else if(alt_temp[0].length()==vcf[i].Ref.length())
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_SNV);
			else
				Variant.setAttribute(XML_TAG_TYPE, VARIANT_TYPE_OTHERS);
			append_text_element(Variant,XML_TAG_FROM,String.valueOf(vcf[i].Pos));
			append_text_element(Variant,XML_TAG_TO,String.valueOf(vcf[i].Pos+range));
			lastpos=vcf[i].Pos+range;
			if(!mode.equals(MODE_DENSE)&&bpp<0.5)
				append_text_element(Variant,XML_TAG_LETTER,vcf[i].Alt);
			if(mode.equals(MODE_DETAIL)){
				StringBuffer Description=new StringBuffer();
				Description.append("QUAL:");
				Description.append(vcf[i].Qual);		
				Description.append(";FILTER:");
				Description.append(vcf[i].Filter);		
				Description.append(";INFO:");
				Description.append(vcf[i].Info);		
				if(vcf[i].Samples.length>0){
					Description.append(";FORMAT:");
					Description.append(vcf[i].Format);
					Description.append(";SAMPLES:");
					for(int j=0;j<vcf[i].Samples.length;j++){
						Description.append(vcf[i].Samples[j]);
						if(j<vcf[i].Samples.length-1)
							Description.append(",");
					}
				}
				append_text_element(Variant,XML_TAG_DESCRIPTION,Description.toString());
			}
			Variants.appendChild(Variant);
		}
	}
	void write_bed2elements(Bed[] bed,String track,long regionstart,long regionend){
		Element Elements = doc.createElement(XML_TAG_ELEMENTS);
		Elements.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Elements); //Elements node
		
		for(int i=0;i<bed.length;i++){
			Element Ele=doc.createElement(XML_TAG_ELEMENT);
			append_text_element(Ele,XML_TAG_FROM,String.valueOf(bed[i].chromStart+1));
			append_text_element(Ele,XML_TAG_TO,String.valueOf(bed[i].chromEnd));
			
			if(bed[i].name != null){
				Ele.setAttribute(XML_TAG_ID, bed[i].name);
				append_text_element(Ele,XML_TAG_DIRECTION,bed[i].strand);
				
				if(!bed[i].itemRgb.ToString().equals("0,0,0"))
					append_text_element(Ele,XML_TAG_COLOR,bed[i].itemRgb.ToString());
				else if(bed[i].score>0)
					append_text_element(Ele,XML_TAG_COLOR,new Rgb(bed[i].score).ToString());
				
				for(int j=0;j<bed[i].blockCount;j++){
					long substart=bed[i].blockStarts[j]+bed[i].chromStart;
					long subend=bed[i].blockStarts[j]+bed[i].chromStart+bed[i].blockSizes[j];
					if (substart<regionend && subend>=regionstart){
						if(subend<=bed[i].thickStart || substart>=bed[i].thickEnd){
							Element subele=doc.createElement(XML_TAG_SUBELEMENT);
							append_text_element(subele,XML_TAG_FROM,String.valueOf(substart+1));
							append_text_element(subele,XML_TAG_TO,String.valueOf(subend));
							subele.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele);
						}
						else if(substart>=bed[i].thickStart && subend<=bed[i].thickEnd){
							Element subele=doc.createElement(XML_TAG_SUBELEMENT);
							append_text_element(subele,XML_TAG_FROM,String.valueOf(substart+1));
							append_text_element(subele,XML_TAG_TO,String.valueOf(subend));
							subele.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BOX);
							Ele.appendChild(subele);
						}
						else if(substart<bed[i].thickStart && subend>bed[i].thickEnd){
							Element subele1=doc.createElement(XML_TAG_SUBELEMENT);
							append_text_element(subele1,XML_TAG_FROM,String.valueOf(substart+1));
							append_text_element(subele1,XML_TAG_TO,String.valueOf(bed[i].thickStart));
							subele1.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele1);
							Element subele2=doc.createElement(XML_TAG_SUBELEMENT);
							append_text_element(subele2,XML_TAG_FROM,String.valueOf(bed[i].thickStart+1));
							append_text_element(subele2,XML_TAG_TO,String.valueOf(bed[i].thickEnd));
							subele2.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BOX);
							Ele.appendChild(subele2);
							Element subele3=doc.createElement(XML_TAG_SUBELEMENT);
							append_text_element(subele3,XML_TAG_FROM,String.valueOf(bed[i].thickEnd+1));
							append_text_element(subele3,XML_TAG_TO,String.valueOf(subend));
							subele3.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele3);
						}
						else if(substart<bed[i].thickStart && subend>bed[i].thickStart){
							Element subele1=doc.createElement(XML_TAG_SUBELEMENT);
							append_text_element(subele1,XML_TAG_FROM,String.valueOf(substart+1));
							append_text_element(subele1,XML_TAG_TO,String.valueOf(bed[i].thickStart));
							subele1.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele1);
							Element subele2=doc.createElement(XML_TAG_SUBELEMENT);
							append_text_element(subele2,XML_TAG_FROM,String.valueOf(bed[i].thickStart+1));
							append_text_element(subele2,XML_TAG_TO,String.valueOf(subend));
							subele2.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BOX);
							Ele.appendChild(subele2);
						}
						else if(substart<bed[i].thickEnd && subend>bed[i].thickEnd){
							Element subele2=doc.createElement(XML_TAG_SUBELEMENT);
							append_text_element(subele2,XML_TAG_FROM,String.valueOf(substart+1));
							append_text_element(subele2,XML_TAG_TO,String.valueOf(bed[i].thickEnd));
							subele2.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BOX);
							Ele.appendChild(subele2);
							Element subele3=doc.createElement(XML_TAG_SUBELEMENT);
							append_text_element(subele3,XML_TAG_FROM,String.valueOf(bed[i].thickEnd+1));
							append_text_element(subele3,XML_TAG_TO,String.valueOf(subend));
							subele3.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele3);
						}
					}
					if(j<bed[i].blockCount-1 && subend<regionend && (bed[i].blockStarts[j+1]+bed[i].chromStart)>=regionstart){
						Element subele=doc.createElement(XML_TAG_SUBELEMENT);
						append_text_element(subele,XML_TAG_FROM,String.valueOf(subend+1));
						append_text_element(subele,XML_TAG_TO,String.valueOf(bed[i].blockStarts[j+1]+bed[i].chromStart));
						subele.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_LINE);
						Ele.appendChild(subele);
					}
				}
			}
			Elements.appendChild(Ele);
		}
	}*/