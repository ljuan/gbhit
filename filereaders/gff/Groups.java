package filereaders.gff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chengwu Yan
 */
public class Groups {
	private Map<String, ArrayList<GFF>> hasAttr = new HashMap<String, ArrayList<GFF>>();
	private List<GFF> noAttr = new ArrayList<GFF>();
	private List<GFF> gffs;
	private String attr;
	Groups(List<GFF> gffs, String attr) {
		this.gffs = gffs;
		this.attr = attr;
	}
	
	void grouping(){
		int index = 0;
		for (int i = 0; i < gffs.size(); i++) {
			GFF gff = gffs.get(i);
			String[] attribute = gff.getAttribute();

			boolean deassign = true;
			if (index < attribute.length
					&& attribute[index].startsWith(attr + "=")) {
				addHasAttr(attribute[index], gff);
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
					addHasAttr(attribute[j], gff);
					index = j;
				} else {
					addNoAttr(gff);
					index = 0;
				}
			}
		}
	}

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

	Map<String, ArrayList<GFF>> getHasAttr() {
		return hasAttr;
	}

	List<GFF> getNoAttr() {
		return noAttr;
	}
}