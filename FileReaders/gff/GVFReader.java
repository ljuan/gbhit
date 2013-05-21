package FileReaders.gff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.hit.mlg.Tools.StringSplit;

import FileReaders.Consts;
import FileReaders.TabixReader;
import FileReaders.XmlWriter;

/**
 * <pre>
 * usage:
 * Firstly, new an object of class GVFReader: new GVFReader(filePath or url);
 * Secondly, call write_gvf2variants(Document doc, String track, String chr, 
 * 		long regionstart, long regionend)
 * </pre>
 * 
 * @author Chengwu Yan
 * 
 */
public class GVFReader {
	private String filePath;

	/**
	 * 
	 * @param filePath
	 *            file path of GVF file
	 * @throws IOException
	 */
	public GVFReader(String filePath){
		this.filePath = filePath;
	}

	public Element get_detail(Document doc, String track, String id,
			String chr, long regionstart, long regionend) {
		Element variants = doc.createElement(Consts.XML_TAG_VARIANTS);
		variants.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(variants); // Variants
		GVF gvf = null;
		TabixReader tb=null;
		try {
			tb=new TabixReader(filePath);
			String querystr = (tb.hasChromPrefix() ? chr : chr.substring(3)) + ":" + regionstart + "-" + regionend;
			String line;
			TabixReader.Iterator Query = tb.query(querystr);
			StringSplit ss = new StringSplit('\t');
			if (Query != null) {
				while ((line = Query.next()) != null) {
					GVF gvf_temp = new GVF(ss.split(line).getResult());
					if (gvf_temp.id.equals(id) && gvf_temp.start == regionstart && gvf_temp.end == regionend) {
						gvf = new GVF(ss.split(line).getResult());
						break;
					}
				}
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
		if (gvf == null) {
			return variants;
		}
		String[][] vs;
		Element variant = null;
		vs = gvf.getVariants();
		for (String[] strs : vs) {
			variant = doc.createElement(Consts.XML_TAG_VARIANT);
			variant.setAttribute(Consts.XML_TAG_ID, gvf.id);
			if (gvf.homo != null && !gvf.homo.equals("")) {
				variant.setAttribute("homo", gvf.homo);
			}
			variant.setAttribute(Consts.XML_TAG_TYPE, strs[0]);
			XmlWriter.append_text_element(doc, variant, Consts.XML_TAG_FROM, gvf.start + "");
			XmlWriter.append_text_element(doc, variant, Consts.XML_TAG_TO, gvf.end + "");
			if (strs[1] != null) {
				XmlWriter.append_text_element(doc, variant, Consts.XML_TAG_LETTER, strs[1]);
			}
			XmlWriter.append_text_element(doc, variant, Consts.XML_TAG_DESCRIPTION, 
					"Source:" + gvf.source + ";Feature:" + gvf.feature + ";Score:" + gvf.score + 
					";Attributes:" + gvf.getAttributesInString());
			variants.appendChild(variant);
		}
		return variants;
	}

	public Element write_gvf2variants(Document doc, String track, String chr,
			long regionstart, long regionend) {
		List<GVF> gvfs = new ArrayList<GVF>();

		Element variants = doc.createElement(Consts.XML_TAG_VARIANTS);
		variants.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(variants); // Variants

		boolean equals = true;
		TabixReader tb=null;
		try {
			tb=new TabixReader(filePath);
			String querystr = (tb.hasChromPrefix() ? chr : chr.substring(3))
					+ ":" + regionstart + "-" + regionend;
			String line;
			String cur = null;
			GVF gvf = null;
			TabixReader.Iterator Query = tb.query(querystr);
			StringSplit ss = new StringSplit('\t');
			if (Query != null) {
				while ((line = Query.next()) != null) {
					gvf = new GVF(ss.split(line).getResult());
					gvfs.add(gvf);
					if (equals) {
						if (gvf.phased == null) {
							equals = false;
						} else {
							if (cur != null) {
								equals = cur.equals(gvf.phased);
							}
							cur = gvf.phased;
						}
					}
				}
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

		String[][] vs;
		Element variant = null;
		for (GVF gvf : gvfs) {
			vs = gvf.getVariants();
			if (!"".equals(gvf.homo)) {
				gvf.homo.replace(':', equals ? '|' : '/');
			}
			for (String[] strs : vs) {
				variant = doc.createElement(Consts.XML_TAG_VARIANT);
				variant.setAttribute(Consts.XML_TAG_ID, gvf.id);
				if (!"".equals(gvf.homo)) {
					variant.setAttribute("homo", gvf.homo);
				}
				variant.setAttribute(Consts.XML_TAG_TYPE, strs[0]);
				XmlWriter.append_text_element(doc, variant, Consts.XML_TAG_FROM, gvf.start + "");
				XmlWriter.append_text_element(doc, variant, Consts.XML_TAG_TO, gvf.end + "");
				if (strs[1] != null) {
					XmlWriter.append_text_element(doc, variant, Consts.XML_TAG_LETTER, strs[1]);
				}
				variants.appendChild(variant);
			}
		}

		return variants;
	}
}