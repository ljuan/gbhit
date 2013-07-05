package filereaders.gff;

import java.io.*;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import filereaders.Consts;
import filereaders.TabixReader;
import filereaders.tools.StringSplit;



/**
 * <pre>
 * usage:
 * Firstly, new an object of class GFFReader: new GFFReader(filePath or url);
 * Secondly, call write_gff2elements(Document doc, String track, String chr, 
 * 		long regionstart, long regionend, String attributes)
 * </pre>
 * 
 * @author Chengwu Yan
 * 
 */
public class GFFReader {
	private String filePath;

	/**
	 * 
	 * @param filePath
	 *            file path of GFF file
	 * @throws IOException
	 */
	public GFFReader(String filePath) {
		this.filePath=filePath;
	}

	/**
	 * Extract necessary GFF datas and write them into Elements
	 * 
	 * @param doc
	 * @param track
	 * @param chr
	 *            chromosome name
	 * @param regionstart
	 * @param regionend
	 * @param attributes
	 *            use attributes to group
	 * @return
	 */
	public Element write_gff2elements(Document doc, String track, String chr,
			long regionstart, long regionend, String attributes) {
		List<GFF> gffs = new ArrayList<GFF>();

		Element elements = doc.createElement(Consts.XML_TAG_ELEMENTS);
		elements.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0)
				.appendChild(elements); // Elements

		TabixReader tb=null;
		try {
			tb = new TabixReader(filePath);
			String querystr = (tb.hasChromPrefix() ? chr : chr.substring(3))
					+ ":" + regionstart + "-" + regionend;
			String line;
			TabixReader.Iterator Query = tb.query(querystr);
			StringSplit ss = new StringSplit('\t');
			if (Query != null) {
				while ((line = Query.next()) != null) {
					gffs.add(new GFF(ss.split(line).getResult()));
				}
			}
			tb.TabixReaderClose();

			Groups gs = new Groups(gffs, attributes);
			gs.grouping();

			// extract all the groups has id
			Map<String, ArrayList<GFF>> hasAttr = gs.getHasAttr();
			for (String key : hasAttr.keySet()) {
				new _Element(key, hasAttr.get(key), false).addToElements(doc,
						elements);
			}
			// extract all the groups has no id
			for (GFF gff : gs.getNoAttr()) {
				List<GFF> list = new ArrayList<GFF>();
				list.add(gff);
				new _Element("", list, false).addToElements(doc, elements);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally{
			if(tb != null){
				try {
					tb.TabixReaderClose();
				} catch (IOException e) {
				}
			}
		}

		return elements;
	}
}