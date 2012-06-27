package FileReaders;

import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
/*
 * BedReader implemented by Tabix
 * Require sorted and indexed bed file
 */
class BedReaderTabix implements Consts{
	TabixReader bed_tb;
	BedReaderTabix(String bed){
		try{
			bed_tb=new TabixReader(bed);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	Bed[] extract_bed(String chr,long start,long end){
		StringBuffer querystr=new StringBuffer();
		querystr.append(chr);
		querystr.append(':');
		querystr.append(start);
		querystr.append('-');
		querystr.append(end);
		TabixReader.Iterator Query=bed_tb.query(querystr.toString());
		ArrayList<Bed> bed_internal=new ArrayList<Bed>();
		String line;
		try{
			while((line=Query.next())!=null){
				bed_internal.add(new Bed(line));
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return bed_internal.toArray(new Bed[bed_internal.size()]);
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