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

		if (VARIANT_TYPE_SNV.equals(str[2])) {
			this.type = VARIANT_TYPE_SNV;
		} else if (VARIANT_TYPE_DELETION.equals(str[2])) {
			this.type = VARIANT_TYPE_DELETION;
		} else if (VARIANT_TYPE_INSERTION.equals(str[2])) {
			this.type = VARIANT_TYPE_INSERTION;
		} else if (VARIANT_TYPE_INVERSION.equals(str[2])) {
			this.type = VARIANT_TYPE_INVERSION;
		} else if (VARIANT_TYPE_MULTIPLE.equals(str[2])) {
			this.type = VARIANT_TYPE_MULTIPLE;
		} else if (VARIANT_TYPE_CNV.equals(str[2])) {
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
	public String toString() {
		return this.seqname + "\t" + this.source + "\t" + this.feature + "\t"
				+ this.start + "\t" + this.end + "\t" + this.score + "\t"
				+ this.strand + "\t" + this.frame + "\t" + this.id + "\t"
				+ this.type + "\t" + this.letter;
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
