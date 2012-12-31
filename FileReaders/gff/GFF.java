package FileReaders.gff;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import edu.hit.mlg.Tools.StringSplit;

/**
 * @author Chengwu Yan
 */
class GFF implements Comparable<GFF> {
	private static DecimalFormat numberForm = (DecimalFormat) NumberFormat
			.getNumberInstance(Locale.CHINA);
	/**
	 * The name of the sequence. Must be a chromosome or scaffold.
	 */
	String seqname;
	/**
	 * The program that generated this feature.
	 */
	String source;
	/**
	 * The name of this type of feature. Some examples of standard feature types
	 * are "CDS", "start_codon", "stop_codon", and "exon".
	 */
	String feature;
	/**
	 * The starting position of the feature in the sequence. The first base is
	 * numbered 1.
	 */
	int start;
	/**
	 * The ending position of the feature (inclusive).
	 */
	int end;
	/**
	 * A score between 0 and 1000. If the track line useScore attribute is set
	 * to 1 for this annotation data set, the score value will determine the
	 * level of gray in which this feature is displayed (higher numbers = darker
	 * gray). If there is no score value, enter ".".
	 */
	String score;
	/**
	 * Valid entries include '+', '-', or '.' (for don't know/don't care).
	 */
	String strand;
	/**
	 * If the feature is a coding exon, frame should be a number between 0-2
	 * that represents the reading frame of the first base. If the feature is
	 * not a coding exon, the value should be '.'.
	 */
	String frame;
	/**
	 * All lines with the same group are linked together into a single item
	 */
	private String attributes;
	/**
	 * split attributes with ";"
	 */
	private String[] attribute;

	GFF(String[] str) {
		this.seqname = str[0];
		this.source = str[1];
		this.feature = str[2];
		this.start = Integer.parseInt(str[3]);
		this.end = Integer.parseInt(str[4]);
		this.score = str[5];
		if (!this.score.equals(".")) {
			this.score = numberForm.format(new Double(this.score)).toString();
		}
		this.strand = str[6];
		this.frame = str[7];
		this.attributes = str[8];
		this.attribute = new StringSplit(';').split(str[8]).getResult();
	}

	String[] getAttribute() {
		return this.attribute;
	}

	@Override
	public int compareTo(GFF o) {
		return this.attributes.compareTo(o.attributes);
	}
}
