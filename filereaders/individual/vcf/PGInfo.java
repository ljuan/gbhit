package filereaders.individual.vcf;

public class PGInfo {
	/**
	 * Every element map to PGInfos_M. True represent INFO contains the key,
	 * false else.
	 */
	boolean[] pgInfos = new boolean[3];
	/**
	 * The variant end base
	 */
	int end = -1;
	/**
	 * Number of the variant bases
	 */
	int svlen = -1;
}