package FileReaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
public class GVFReader extends GFFReader {
	public GVFReader(String filePath) throws IOException {
		super(filePath);
	}

	public Element write_gvf2variants(Document doc, String track, String chr,
			long regionstart, long regionend) {
		List<GVF> gvfs = new ArrayList<GVF>();

		Element variants = doc.createElement(XML_TAG_VARIANTS);
		variants.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(variants); // Variants

		try {
			for (String[] strs : extract(chr, regionstart, regionend)) {
				gvfs.add(new GVF(strs));
			}
			Collections.sort(gvfs);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		for (GVF gvf : gvfs) {
			Element variant = doc.createElement(XML_TAG_VARIANT);
			variant.setAttribute(XML_TAG_ID, gvf.id);
			variant.setAttribute(XML_TAG_TYPE, gvf.type);
			XmlWriter.append_text_element(doc, variant, XML_TAG_FROM, gvf.start
					+ "");
			XmlWriter.append_text_element(doc, variant, XML_TAG_TO, gvf.end
					+ "");
			XmlWriter.append_text_element(doc, variant, XML_TAG_LETTER,
					gvf.letter);

			variants.appendChild(variant);
		}

		return variants;
	}

	@Override
	public Element write_gff2elements(Document doc, String track, String chr,
			long regionstart, long regionend, String attributes) {
		try {
			throw new Exception("Unsupport method!");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
