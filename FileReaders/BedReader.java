package FileReaders;

import java.util.*;
import java.io.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * This is for reading BED files.
 * We don't interpret the specific content in this level,
 * because a lot of genomic data can be stored in BED formats.
 * BED itself is display-oriented.
 * BED is also a typical tab-delimited file format, 
 * which can be compressed, indexed and read by Tabix API.
 * But we consider BED as a light-weight up-to-2-level genomic region representation format here,
 * and believe it is convenient if we keep BED as Text-based format, not compressed binary format,
 * and thus we can tolerant unsorted or unindexed files to display.
 * For this purpose, we read the whole file into the memory and traverse all of the records, to find 
 * the ones should be displayed in the current window when a query is executed.
 */

class BedReader implements Consts{
	String beds[];
	BedReader(String bed){
		File bed_file=new File(bed);
		ByteBufferChannel bbc=new ByteBufferChannel(bed_file,0,bed_file.length());
		String temp=bbc.ToString(DEFAULT_ENCODE);
		beds=temp.split("\n");
	}

	Bed[] extract_bed(String chr, long start, long end){
		ArrayList<Bed> beds_internal=new ArrayList<Bed>();
		for(int i=0;i<beds.length;i++){
			Bed bed_temp=new Bed(beds[i]);
			if (bed_temp.chrom.equals(chr)&&bed_temp.chromStart<end&&bed_temp.chromEnd>=start){
				beds_internal.add(bed_temp);
			}
		}
		return beds_internal.toArray(new Bed[beds_internal.size()]);
	}
	Element write_bed2elements(Document doc,String track,String chr, long regionstart,long regionend){
		Bed[] bed=extract_bed(chr,regionstart,regionend);
		Element Elements = doc.createElement(XML_TAG_ELEMENTS);
		Elements.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Elements); //Elements node
		
		for(int i=0;i<bed.length;i++){
			Element Ele=doc.createElement(XML_TAG_ELEMENT);
			XmlWriter.append_text_element(doc,Ele,XML_TAG_FROM,String.valueOf(bed[i].chromStart+1));
			XmlWriter.append_text_element(doc,Ele,XML_TAG_TO,String.valueOf(bed[i].chromEnd));
			
			if(bed[i].name != null){
				Ele.setAttribute(XML_TAG_ID, bed[i].name);
				XmlWriter.append_text_element(doc,Ele,XML_TAG_DIRECTION,bed[i].strand);
				
				if(!bed[i].itemRgb.ToString().equals("0,0,0"))
					XmlWriter.append_text_element(doc,Ele,XML_TAG_COLOR,bed[i].itemRgb.ToString());
				else if(bed[i].score>0)
					XmlWriter.append_text_element(doc,Ele,XML_TAG_COLOR,new Rgb(bed[i].score).ToString());
				
				for(int j=0;j<bed[i].blockCount;j++){
					long substart=bed[i].blockStarts[j]+bed[i].chromStart;
					long subend=bed[i].blockStarts[j]+bed[i].chromStart+bed[i].blockSizes[j];
					if (substart<regionend && subend>=regionstart){
						if(subend<=bed[i].thickStart || substart>=bed[i].thickEnd){
							Element subele=doc.createElement(XML_TAG_SUBELEMENT);
							XmlWriter.append_text_element(doc,subele,XML_TAG_FROM,String.valueOf(substart+1));
							XmlWriter.append_text_element(doc,subele,XML_TAG_TO,String.valueOf(subend));
							subele.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele);
						}
						else if(substart>=bed[i].thickStart && subend<=bed[i].thickEnd){
							Element subele=doc.createElement(XML_TAG_SUBELEMENT);
							XmlWriter.append_text_element(doc,subele,XML_TAG_FROM,String.valueOf(substart+1));
							XmlWriter.append_text_element(doc,subele,XML_TAG_TO,String.valueOf(subend));
							subele.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BOX);
							Ele.appendChild(subele);
						}
						else if(substart<bed[i].thickStart && subend>bed[i].thickEnd){
							Element subele1=doc.createElement(XML_TAG_SUBELEMENT);
							XmlWriter.append_text_element(doc,subele1,XML_TAG_FROM,String.valueOf(substart+1));
							XmlWriter.append_text_element(doc,subele1,XML_TAG_TO,String.valueOf(bed[i].thickStart));
							subele1.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele1);
							Element subele2=doc.createElement(XML_TAG_SUBELEMENT);
							XmlWriter.append_text_element(doc,subele2,XML_TAG_FROM,String.valueOf(bed[i].thickStart+1));
							XmlWriter.append_text_element(doc,subele2,XML_TAG_TO,String.valueOf(bed[i].thickEnd));
							subele2.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BOX);
							Ele.appendChild(subele2);
							Element subele3=doc.createElement(XML_TAG_SUBELEMENT);
							XmlWriter.append_text_element(doc,subele3,XML_TAG_FROM,String.valueOf(bed[i].thickEnd+1));
							XmlWriter.append_text_element(doc,subele3,XML_TAG_TO,String.valueOf(subend));
							subele3.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele3);
						}
						else if(substart<bed[i].thickStart && subend>bed[i].thickStart){
							Element subele1=doc.createElement(XML_TAG_SUBELEMENT);
							XmlWriter.append_text_element(doc,subele1,XML_TAG_FROM,String.valueOf(substart+1));
							XmlWriter.append_text_element(doc,subele1,XML_TAG_TO,String.valueOf(bed[i].thickStart));
							subele1.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele1);
							Element subele2=doc.createElement(XML_TAG_SUBELEMENT);
							XmlWriter.append_text_element(doc,subele2,XML_TAG_FROM,String.valueOf(bed[i].thickStart+1));
							XmlWriter.append_text_element(doc,subele2,XML_TAG_TO,String.valueOf(subend));
							subele2.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BOX);
							Ele.appendChild(subele2);
						}
						else if(substart<bed[i].thickEnd && subend>bed[i].thickEnd){
							Element subele2=doc.createElement(XML_TAG_SUBELEMENT);
							XmlWriter.append_text_element(doc,subele2,XML_TAG_FROM,String.valueOf(substart+1));
							XmlWriter.append_text_element(doc,subele2,XML_TAG_TO,String.valueOf(bed[i].thickEnd));
							subele2.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BOX);
							Ele.appendChild(subele2);
							Element subele3=doc.createElement(XML_TAG_SUBELEMENT);
							XmlWriter.append_text_element(doc,subele3,XML_TAG_FROM,String.valueOf(bed[i].thickEnd+1));
							XmlWriter.append_text_element(doc,subele3,XML_TAG_TO,String.valueOf(subend));
							subele3.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_BAND);
							Ele.appendChild(subele3);
						}
					}
					if(j<bed[i].blockCount-1 && subend<regionend && (bed[i].blockStarts[j+1]+bed[i].chromStart)>=regionstart){
						Element subele=doc.createElement(XML_TAG_SUBELEMENT);
						XmlWriter.append_text_element(doc,subele,XML_TAG_FROM,String.valueOf(subend+1));
						XmlWriter.append_text_element(doc,subele,XML_TAG_TO,String.valueOf(bed[i].blockStarts[j+1]+bed[i].chromStart));
						subele.setAttribute(XML_TAG_TYPE, SUBELEMENT_TYPE_LINE);
						Ele.appendChild(subele);
					}
				}
			}
			Elements.appendChild(Ele);
		}
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Elements);
		return Elements;
	}
}