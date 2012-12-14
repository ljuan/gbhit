package FileReaders;

import java.io.*;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import FileReaders.TabixReader;

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
class GFFReader implements Consts {
	private BufferedReader br;
	private TabixReader tb;
	private boolean isSorted;

	public GFFReader() {
	}

	/**
	 * 
	 * @param filePath
	 *            file path of local file or url of remote file
	 * @throws IOException
	 */
	public GFFReader(String filePath) throws IOException {
		if (filePath.startsWith("http://") || filePath.startsWith("ftp://")
				|| filePath.startsWith("https://")
				|| new File(filePath + ".tbi").exists()) {
			this.tb = new TabixReader(filePath);
			this.isSorted = true;
		} else {
			this.br = new BufferedReader(new FileReader(filePath));
			this.isSorted = false;
		}
	}

	boolean isIsSorted() {
		return isSorted;
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

		Element elements = doc.createElement(XML_TAG_ELEMENTS);
		elements.setAttribute(XML_TAG_ID, track);
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(elements); // Elements

		try {
			for (String[] s : extract(chr, regionstart, regionend))
				gffs.add(new GFF(s));

			Collections.sort(gffs);

			Groups gs = grouping(gffs, attributes);

			// extract all the groups has id
			for (String key : gs.hasAttr.keySet()) {
				new _Element(key, gs.hasAttr.get(key), false).addToElements(
						doc, elements);
			}
			// extract all the groups has no id
			for (GFF gff : gs.noAttr) {
				List<GFF> list = new ArrayList<GFF>();
				list.add(gff);
				new _Element("", list, false).addToElements(doc, elements);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return elements;
	}

	List<String[]> extract(String chr, long start, long end) throws IOException {
		if (isSorted)
			return extract_binary(chr, start, end);
		else
			return extract_text(chr, start, end);
	}

	private List<String[]> extract_binary(String chr, long start, long end)
			throws IOException {
		List<String[]> gffs = new ArrayList<String[]>();

		String querystr = chr + ":" + start + "-" + end;
		String line;
		try {
			TabixReader.Iterator Query = tb.query(querystr);
			if (Query == null)
				return gffs;
			while ((line = Query.next()) != null) {
				gffs.add(line.split("\t"));
			}
			tb.TabixReaderClose();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return gffs;
	}

	private List<String[]> extract_text(String chr, long start, long end)
			throws IOException {
		List<String[]> gffs = new ArrayList<String[]>();

		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith(chr + "\t")) {
				String[] str = line.split("\t");
				if (overlaps(start, end, Long.parseLong(str[3]),
						Long.parseLong(str[4]))) {
					gffs.add(str);
				}
			}
		}

		return gffs;
	}

	public boolean overlaps(long region_start, long region_end, long chr_start,
			long chr_end) {
		if (chr_start >= region_start && chr_start <= region_end)
			return true;
		if (chr_end >= region_start && chr_end <= region_end)
			return true;
		if (chr_start <= region_start && chr_end >= region_end)
			return true;

		return false;
	}

	private Groups grouping(List<GFF> gffs, String attr) {
		Groups gs = new Groups();
		int index = 0;

		for (int i = 0; i < gffs.size(); i++) {
			GFF gff = gffs.get(i);
			String[] attribute = gff.getAttribute();

			boolean deassign = true;
			if (index < attribute.length
					&& attribute[index].startsWith(attr + "=")) {
				gs.addHasAttr(attribute[index], gff);
				deassign = false;
			}
			if (deassign) {
				int j = 0;
				for (j = 0; j < attribute.length; j++) {
					if (attribute[j].startsWith(attr + "=")) {
						break;
					}
				}
				if (j < attribute.length) {
					gs.addHasAttr(attribute[j], gff);
					index = j;
				} else {
					gs.addNoAttr(gff);
					index = 0;
				}
			}
		}

		return gs;
	}

	/**
	 * @author cwyan
	 */
	private class Groups {
		Map<String, ArrayList<GFF>> hasAttr = new HashMap<String, ArrayList<GFF>>();
		List<GFF> noAttr = new ArrayList<GFF>();

		void addHasAttr(String keys, GFF gff) {
			String str = keys.substring(keys.indexOf("=") + 1, keys.length());
			String[] key = str.split(",");
			for (String k : key) {
				if (!hasAttr.containsKey(k))
					hasAttr.put(k, new ArrayList<GFF>());
				hasAttr.get(k).add(gff);
			}
		}

		void addNoAttr(GFF gff) {
			noAttr.add(gff);
		}
	}
}
