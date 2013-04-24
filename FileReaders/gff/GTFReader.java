package FileReaders.gff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.hit.mlg.Tools.StringSplit;

import FileReaders.Consts;
import FileReaders.TabixReader;

/**
 * <pre>
 * usage:
 * Firstly, new an object of class GTFReader: new GTFReader(filePath or url);
 * Secondly, call write_gtf2elements(Document doc, String track, String chr, 
 * 		long regionstart, long regionend)
 * </pre>
 * 
 * @author Chengwu Yan
 * 
 */
public class GTFReader {
	private TabixReader tb;

	/**
	 * 
	 * @param filePath
	 *            file path of GTF file
	 * @throws IOException
	 */
	public GTFReader(String filePath) throws IOException {
		this.tb = new TabixReader(filePath);
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
	 * @return
	 */
	public Element write_gtf2elements(Document doc, String track, String chr,
			long regionstart, long regionend) {
		List<GTF> gtfs = new ArrayList<GTF>();

		Element elements = doc.createElement(Consts.XML_TAG_ELEMENTS);
		elements.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0)
				.appendChild(elements); // Elements
		try {
			String querystr = (tb.hasChromPrefix() ? chr : chr.substring(3))
					+ ":" + regionstart + "-" + regionend;
			String line;
			TabixReader.Iterator Query = tb.query(querystr);
			StringSplit ss = new StringSplit('\t');
			if (Query != null) {
				while ((line = Query.next()) != null) {
					gtfs.add(new GTF(ss.split(line).getResult()));
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

		String transcript_id = null;
		List<GFF> gs = new ArrayList<GFF>();
		for (int i = 0, sizeMinus1 = gtfs.size() - 1; i <= sizeMinus1; i++) {
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
			if (i == sizeMinus1)
				new _Element(transcript_id, gs, true).addToElements(doc,
						elements);
		}

		return elements;
	}
}