package FileReaders;

import java.io.IOException;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <pre>
 * usage:
 * Firstly, new an object of class GTFReader: new GTFReader(filePath or url);
 * Secondly, call write_gtf2elements(Document doc, String track, String chr, 
 * 		long regionstart, long regionend)
 * </pre>
 * 
 * @author cwyan
 * 
 */
public class GTFReader extends GFFReader {
	public GTFReader(String filePath) throws IOException {
		super(filePath);
	}

	/**
	 * Extract necessary GTF datas and write them into Elements
	 * 
	 * @param doc
	 * @param track
	 * @param chr
	 *            chromosome name
	 * @param regionstart
	 * @param regionend
	 */
	public void write_gtf2elements(Document doc, String track, String chr,
			long regionstart, long regionend) {
		List<GTF> gtfs = new ArrayList<GTF>();

		Element elements = doc.createElement(XML_TAG_ELEMENTS);
		elements.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(elements); // Elements

		try {
			for (String[] strs : extract(chr, regionstart, regionend)) {
				gtfs.add(new GTF(strs, this.isIsSorted()));
			}
			Collections.sort(gtfs);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		String transcript_id = null;
		List<GFF> gs = new ArrayList<GFF>();
		for (int i = 0; i < gtfs.size(); i++) {
			GTF gtf = gtfs.get(i);
			if (!gtf.t_id.equals(transcript_id)) {
				if (transcript_id != null) {
					new _Element(transcript_id, gs, true).addToElements(doc,
							elements);
					gs.clear();
				}
				transcript_id = gtf.t_id;
			}
			gs.add(gtf);
			if (i == gtfs.size() - 1)
				new _Element(transcript_id, gs, true).addToElements(doc,
						elements);
		}
	}

	@Override
	public void write_gff2elements(Document doc, String track, String chr,
			long regionstart, long regionend, String attributes) {
		try {
			throw new Exception("Unsupport method!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}