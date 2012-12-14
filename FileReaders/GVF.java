package FileReaders;

/**
 * @author Chengwu Yan
 */
public class GVF extends GFF implements Consts {
	String id;
	String type;
	String letter = "";

	GVF(String[] str) {
		super(str);

		String t = str[2];
		if (VARIANT_TYPE_SNV.equals(t)) {
			this.type = VARIANT_TYPE_SNV;
		} else if (VARIANT_TYPE_DELETION.equals(t)) {
			this.type = VARIANT_TYPE_DELETION;
		} else if (VARIANT_TYPE_INSERTION.equals(t)) {
			this.type = VARIANT_TYPE_INSERTION;
		} else if (VARIANT_TYPE_INVERSION.equals(t)) {
			this.type = VARIANT_TYPE_INVERSION;
		} else if (VARIANT_TYPE_MULTIPLE.equals(t)) {
			this.type = VARIANT_TYPE_MULTIPLE;
		} else if (VARIANT_TYPE_CNV.equals(t)) {
			this.type = VARIANT_TYPE_CNV;
		} else {
			this.type = VARIANT_TYPE_OTHERS;
		}

		String[] attributes = str[8].split(";");
		for (String s : attributes) {
			if (s.startsWith("ID=")) {
				this.id = s.split("=")[1];
				break;
			}
		}
		for (String s : attributes) {
			if (s.startsWith("Variant_seq=")) {
				String[] ss = s.split("=")[1].split(",");
				StringBuilder sb = new StringBuilder();
				for (String ssssss : ss)
					sb.append(ssssss);
				this.letter = sb.toString();
				break;
			}
		}
	}

	@Override
	public int compareTo(GFF o) {
		if (o.start != this.start)
			return (this.start > o.start) ? 1 : -1;
		// o.start == this.start
		if (o.end != this.end)
			return (this.end > o.end) ? 1 : -1;
		// o.start == this.start and o.end == this.end
		return 0;
	}
}
