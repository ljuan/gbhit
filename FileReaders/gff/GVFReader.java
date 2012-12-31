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
	private TabixReader tb;

	/**
	 * 
	 * @param filePath
	 *            file path of GVF file
	 * @throws IOException
	 */
	public GVFReader(String filePath) throws IOException {
		this.tb = new TabixReader(filePath);
	}

	public Element write_gvf2variants(Document doc, String track, String chr,
			long regionstart, long regionend) {
		List<GVF> gvfs = new ArrayList<GVF>();

		Element variants = doc.createElement(Consts.XML_TAG_VARIANTS);
		variants.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0)
				.appendChild(variants); // Variants

		try {
			String querystr = (tb.hasChromPrefix() ? chr : chr.substring(3))
					+ ":" + regionstart + "-" + regionend;
			String line;
			TabixReader.Iterator Query = tb.query(querystr);
			StringSplit ss = new StringSplit('\t');
			if (Query != null) {
				while ((line = Query.next()) != null) {
					gvfs.add(new GVF(ss.split(line).getResult()));
				}
			}
			tb.TabixReaderClose();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String[][] vs;
		Element variant = null;
		for (GVF gvf : gvfs) {
			vs = gvf.getVariants();
			for (String[] strs : vs) {
				variant = doc.createElement(Consts.XML_TAG_VARIANT);
				variant.setAttribute(Consts.XML_TAG_ID, gvf.id);
				variant.setAttribute(Consts.XML_TAG_TYPE, strs[0]);
				XmlWriter.append_text_element(doc, variant,
						Consts.XML_TAG_FROM, gvf.start + "");
				XmlWriter.append_text_element(doc, variant, Consts.XML_TAG_TO,
						gvf.end + "");
				if (strs[1] != null) {
					XmlWriter.append_text_element(doc, variant,
							Consts.XML_TAG_LETTER, strs[1]);
				}
				variants.appendChild(variant);
			}
		}

		return variants;
	}
}
