package FileReaders;

/**
 * @author Chengwu Yan
 */
public class GTF extends GFF {
	/**
	 * The group field has been expanded into a list of attributes. Each
	 * attribute consists of a type/value pair. Attributes must end in a
	 * semi-colon, and be separated from any following attribute by exactly one
	 * space.
	 * 
	 * The attribute list must begin with the two mandatory attributes: gene_id
	 * value - A globally unique identifier for the genomic source of the
	 * sequence. transcript_id value - A globally unique identifier for the
	 * predicted transcript.
	 */
	String group;

	String t_id;

	GTF(String[] str, boolean isSorted) {
		super(str);
		this.group = str[8];

		for (String s : str[8].split("; ")) {
			if (s.startsWith("transcript_id ")) {
				t_id = s.split(" ")[1];
				if (t_id.contains("\""))
					t_id = t_id.replace("\"", "");
				break;
			}
		}
	}

	public int compareTo(GTF o) {
		if (!this.t_id.equals(o.t_id))
			return this.t_id.compareTo(o.t_id);
		if (!"exon".equals(this.feature))
			return -1;
		// this.feature == "exon"
		if (!"exon".equals(o.feature))
			return 1;
		// this.feature == "exon" and o.feature == "exon"
		if (o.start != this.start)
			return (this.start > o.start) ? 1 : -1;
		// o.start == this.start
		if (o.end != this.end)
			return (this.end > o.end) ? 1 : -1;
		// o.start == this.start and o.end == this.end
		return 0;
	}
}
