package FileReaders.vcf;

import java.util.Arrays;

/**
 * An instance of Samples store only SAMPLEs we need of a VCF record.
 * 
 * @author Chengwu Yan
 * 
 */
public class VCFRecordSamples {
	private String format;
	private int[][] sampleVariants;
	private String[] samples;
	private boolean containGT;

	/**
	 * 
	 * @param samples
	 *            VCF line split by '\t'
	 */
	public VCFRecordSamples(String[] samples) {
		int len = samples.length - 9;
		this.format = samples[8];
		containGT = format.length() > 2 && format.charAt(0) == 'G'
				&& format.charAt(1) == 'T' && format.charAt(2) == ':';
		this.sampleVariants = new int[len][];
		this.samples = new String[len];
		for (int index = 0; index < len; index++) {
			this.samples[index] = samples[index + 9];
		}
		if (containGT) {
			char[] cs = new char[100];
			for (int index = 0; index < len; index++) {
				this.sampleVariants[index] = getIntValues(getGT(
						samples[index + 9], cs));
			}
		}
	}

	/**
	 * Get "GT" field of sample. If no "GT" field, return null.
	 * 
	 * @param sample
	 * @param cs
	 * @return
	 */
	private char[] getGT(String sample, char[] cs) {
		int count = 0;
		for (int i = 0; i < sample.length(); i++) {
			if (sample.charAt(i) == ':')
				break;
			cs[count++] = sample.charAt(i);
		}

		char[] dest = new char[count];
		System.arraycopy(cs, 0, dest, 0, count);

		return dest;
	}

	/**
	 * Retrieve number from GT. The numbers may separate by '|' or '/'. If
	 * 
	 * <pre>
	 * They are five examples:
	 * "0", result: null
	 * "0|1", result: {1}
	 * "1|1", result: {1}
	 * "0/1/2/3/5", result: {1, 2, 3, 5}
	 * "0/3/2/3/4/5", result: {2, 3, 4, 5}
	 * @param GT
	 * @return
	 */
	private static int[] getIntValues(char[] GT) {
		int[] intValues = new int[10];
		int[] position = new int[11];
		int count = 0;
		int num = 0;

		for (char c : GT) {
			if (c >= '0' && c <= '9') {
				num = num * 10 + (int) c - 48;
			} else {
				if (num > 0 && position[num] == 0) {
					intValues[count++] = num;
					position[num] = 1;
				}
				num = 0;
			}
		}
		if (num > 0 && position[num] == 0)
			intValues[count++] = num;
		// In most cases
		if (count == 0)
			return null;
		// In most cases
		if (count == 1)
			return new int[] { intValues[0] };
		// In most cases
		if (count == 2)
			return new int[] { intValues[0], intValues[1] };

		int[] dest = new int[count];
		System.arraycopy(intValues, 0, dest, 0, count);
		Arrays.sort(dest);

		return dest;
	}

	public boolean containGT() {
		return containGT;
	}

	public int[] getVariantIndexes(int index) {
		return sampleVariants[index];
	}

	public String getFormat() {
		return format;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		int len = this.samples.length;
		for (int i = 0; i < len - 1; i++) {
			builder.append(this.samples[i]);
			builder.append(',');
		}
		if (len > 0) {
			builder.append(this.samples[len - 1]);
		}

		return builder.toString();
	}
}
