package FileReaders;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.hit.mlg.Tools.StringSplit;
import static FileReaders.BedReader.deal_thick;
import static FileReaders.BedReader.append_subele;

/*
 * BedReader implemented by Tabix
 * Require sorted and indexed bed file
 */
class BasicAnnosReader{
	String baPath;

	BasicAnnosReader(String ba) {
		this.baPath = ba;
	}
	
	Element get_detail(Document doc, String track, String id, String chr,
			long regionstart, long regionend) {
		Element Elements = doc.createElement(Consts.XML_TAG_ELEMENTS);
		Elements.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Elements); // Elements

		// node
		try {
			Element Ele = null;
			String line;
			TabixReader ba_tb = new TabixReader(baPath);
			TabixReader.Iterator Query = ba_tb.query(chr + ":" + regionstart
					+ "-" + regionend);
			StringSplit split = new StringSplit('\t');
			while (Query!=null&&(line = Query.next()) != null) {
				split.split(line);
				if (regionstart==Integer.parseInt(split.getResultByIndex(1))+1&&regionend==Integer.parseInt(split.getResultByIndex(2))&&split.getResultByIndex(3).equals(id)) {
					Ele = doc.createElement(Consts.XML_TAG_ELEMENT);
					BasicAnnos ba=new BasicAnnos(split.getResult(), split.getResultNum());
					XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_FROM, Integer.toString(ba.txStart + 1, 10));
					XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_TO, Integer.toString(ba.txEnd, 10));
					Ele.setAttribute(Consts.XML_TAG_ID, ba.name);
					if(!ba.symbol.equals(""))
						Ele.setAttribute(Consts.XML_TAG_SYMBOL, ba.symbol);
					XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_DIRECTION, ba.strand);
					for (int j = 0; j < ba.exonCount; j++) {
						long substart = ba.exonStarts[j] + ba.txStart;
						long subend = ba.exonStarts[j] + ba.txStart + ba.exonSizes[j];
						deal_thick(doc, Ele, substart, subend, ba.cdsStart, ba.cdsEnd);
						if (j < ba.exonCount - 1) 
							append_subele(doc, Ele,	Long.toString(subend + 1, 10), Long.toString(ba.exonStarts[j + 1]	+ ba.txStart, 10), Consts.SUBELEMENT_TYPE_LINE);
					}
					Elements.appendChild(Ele);
					break;
				}

			}
			ba_tb.TabixReaderClose();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Elements;
	}
	Element write_ba2elements(Document doc, String track, String chr,
			long regionstart, long regionend, double bpp) {
		Element Elements = doc.createElement(Consts.XML_TAG_ELEMENTS);
		Elements.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Elements); // Elements

		// node
		try {
			Element Ele = null;
			String line;
			TabixReader ba_tb = new TabixReader(baPath);
			TabixReader.Iterator Query = ba_tb.query(chr + ":" + regionstart
					+ "-" + regionend);
			StringSplit split = new StringSplit('\t');
			while (Query!=null&&(line = Query.next()) != null) {
				Ele = doc.createElement(Consts.XML_TAG_ELEMENT);
				split.split(line);
				append2Element(doc, regionstart, regionend, bpp, Ele, new BasicAnnos(
						split.getResult(), split.getResultNum()));
				Elements.appendChild(Ele);
			}
			ba_tb.TabixReaderClose();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Elements;
	}
	static void append2Element(Document doc, long regionstart, long regionend, double bpp, Element Ele, BasicAnnos ba) {
		XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_FROM, Integer.toString(ba.txStart + 1, 10));
		XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_TO, Integer.toString(ba.txEnd, 10));
		Ele.setAttribute(Consts.XML_TAG_ID, ba.name);
		if(!ba.symbol.equals(""))
			Ele.setAttribute(Consts.XML_TAG_SYMBOL, ba.symbol);
		XmlWriter.append_text_element(doc, Ele, Consts.XML_TAG_DIRECTION, ba.strand);
		if ((ba.txEnd - ba.txStart) / bpp > ba.exonCount * 2) {
			for (int j = 0; j < ba.exonCount; j++) {
				long substart = ba.exonStarts[j] + ba.txStart;
				long subend = ba.exonStarts[j] + ba.txStart + ba.exonSizes[j];
				if (substart < regionend && subend >= regionstart) 
					deal_thick(doc, Ele, substart, subend, ba.cdsStart, ba.cdsEnd);
				if (j < ba.exonCount - 1 && subend < regionend && (ba.exonStarts[j + 1] + ba.txStart) >= regionstart) 
					append_subele(doc, Ele,	Long.toString(subend + 1, 10), Long.toString(ba.exonStarts[j + 1]	+ ba.txStart, 10), Consts.SUBELEMENT_TYPE_LINE);
			}
		} 
	}
}