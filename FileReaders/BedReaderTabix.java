package FileReaders;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.hit.mlg.Tools.StringSplit;
import static FileReaders.BedReader.append2Element;
import static FileReaders.BedReader.deal_thick;
import static FileReaders.BedReader.append_subele;

/*
 * BedReader implemented by Tabix
 * Require sorted and indexed bed file
 */
class BedReaderTabix{
	String bedPath;

	BedReaderTabix(String bed) {
		this.bedPath = bed;
	}

	Element get_detail(Document doc, String track, String id,String chr,long regionstart, long regionend) {
		Element Elements = doc.createElement(Consts.XML_TAG_ELEMENTS);
		Elements.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Elements);
		// node
		TabixReader bed_tb = null;
		try {
			Element Ele = null;
			String line;
			bed_tb = new TabixReader(bedPath);
			TabixReader.Iterator Query = bed_tb.query(chr + ":" + regionstart
					+ "-" + regionend);
			StringSplit split = new StringSplit('\t');
			while (Query!=null&&(line = Query.next()) != null) {
				split.split(line);
				if (regionstart==Integer.parseInt(split.getResultByIndex(1))+1&&regionend==Integer.parseInt(split.getResultByIndex(2))&&split.getResultByIndex(3).equals(id)) {
					Ele = doc.createElement(Consts.XML_TAG_ELEMENT);
					Bed bed=new Bed(split.getResult(), split.getResultNum());
					XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_FROM,Integer.toString(bed.chromStart + 1, 10));
					XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_TO,Integer.toString(bed.chromEnd, 10));
					if (bed.fields > 3) 
						Ele.setAttribute(Consts.XML_TAG_ID, bed.name);
					if (bed.fields > 4) 
						if (bed.itemRgb != null	&& !bed.itemRgb.ToString().equals("0,0,0"))
							XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_COLOR, bed.itemRgb.ToString());
						else if (bed.score > 0)
							XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_COLOR, new Rgb(bed.score).ToString());
					if (bed.fields > 5) 
						XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_DIRECTION, bed.strand);
					if (bed.fields > 11) {
						for (int j = 0; j < bed.blockCount; j++) {
							long substart = bed.blockStarts[j] + bed.chromStart;
							long subend = bed.blockStarts[j] + bed.chromStart + bed.blockSizes[j];
							deal_thick(doc, Ele, substart, subend, bed.thickStart, bed.thickEnd);
							if (j < bed.blockCount - 1) 
								append_subele(doc, Ele,	Long.toString(subend + 1, 10), Long.toString(bed.blockStarts[j + 1]	+ bed.chromStart, 10), Consts.SUBELEMENT_TYPE_LINE);
						}
					} 
					else if (bed.fields > 7) 
						deal_thick(doc, Ele, bed.chromStart, bed.chromEnd, bed.thickStart, bed.thickEnd);
					Elements.appendChild(Ele);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(bed_tb != null){
				try {
					bed_tb.TabixReaderClose();
				} catch (IOException e) {
				}
			}
		}

		return Elements;
	}
	Element write_bed2elements(Document doc, String track, String chr,
			long regionstart, long regionend, double bpp) {
		Element Elements = doc.createElement(Consts.XML_TAG_ELEMENTS);
		Elements.setAttribute(Consts.XML_TAG_ID, track);
		// node
		TabixReader bed_tb = null;
		try {
			Element Ele = null;
			String line;
			bed_tb = new TabixReader(bedPath);
			TabixReader.Iterator Query = bed_tb.query(chr + ":" + regionstart
					+ "-" + regionend);
			StringSplit split = new StringSplit('\t');
			while (Query!=null&&(line = Query.next()) != null) {
				Ele = doc.createElement(Consts.XML_TAG_ELEMENT);
				split.split(line);
				append2Element(doc, regionstart, regionend, bpp, Ele, new Bed(
						split.getResult(), split.getResultNum()));
				Elements.appendChild(Ele);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(bed_tb != null){
				try {
					bed_tb.TabixReaderClose();
				} catch (IOException e) {
				}
			}
		}

		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Elements);
		return Elements;
	}
}