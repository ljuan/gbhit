package filereaders.individual.vcf;

/**
 * An instance of Samples store only SAMPLEs we need of a VCF record.
 * 
 * @author Chengwu Yan
 * 
 */
public class VCFRecordSamples {
	private String format;
	private int[][] sampleVariants;
	private String[] homos;
	private String[] samples;
	private boolean containGT;

	/**
	 * 
	 * @param samples 	VCF line split by '\t'
	 */
	public VCFRecordSamples(String[] samples) {
		int len = samples.length - 9;
		this.format = samples[8];
		containGT = format.length() == 2 ? format.equals("GT") : format.startsWith("GT:");
		this.sampleVariants = new int[len][];
		this.homos = new String[len];
		for (int i = 0; i < len; i++) {
			this.homos[i] = "";
		}
		this.samples = new String[len];
		
		for (int index = 0; index < len; index++) {
			this.samples[index] = samples[index + 9];
		}
		if (containGT) {
			char[] cs = new char[100];
			for (int index = 0; index < len; index++) {
				this.sampleVariants[index] = getIntValues(getGT(samples[index + 9], cs, index), index);
			}
		}
	}

	/**
	 * Get "GT" field of sample. If no "GT" field, return null.
	 * 
	 * @param sample
	 * @param cs
	 * @param index
	 * @return
	 */
	private char[] getGT(String sample, char[] cs, int index) {
		int count = 0;
		for (int i = 0; i < sample.length(); i++) {
			if (sample.charAt(i) == ':')
				break;
			cs[count++] = sample.charAt(i);
		}

		char[] dest = new char[count];
		System.arraycopy(cs, 0, dest, 0, count);
		this.homos[index] = new String(dest);

		return dest;
	}

	/**
	 * Retrieve number from GT. The numbers may separate by '|' or '/'.
	 * 
	 * <pre>
	 * They are five examples:
	 * "0", result: null
	 * "0|1", result: {1}
	 * "1|1", result: {1}
	 * "0/1/2/3/5", result: {1, 2, 3, 5}
	 * "0/3/2/3/4/5", result: {2, 3, 4, 5}
	 * @param GT
	 * @param index index of homos
	 * @return
	 */
	private int[] getIntValues(char[] GT, int index) {
		int[] intValues = new int[10];
		int[] position = new int[11];
		int curNum = -1;
		int count = 0;
		int num = 0;

		for (char c : GT) {
			if (c >= '0' && c <= '9') {
				num = num * 10 + (int) c - 48;
			} else {
				if (num != curNum) {
					curNum = num;
				} else {
					curNum = -1;
				}
				if (num > 0 && position[num] == 0) {
					intValues[count++] = num;
					position[num] = 1;
				}
				num = 0;
			}
		}
		if (num != curNum) {
			curNum = num;
		} else {
			curNum = -1;
		}
		if (num > 0 && position[num] == 0)
			intValues[count++] = num;
		// In most cases
		if (count == 0)
			return null;
		if (count == 1)
			return new int[] { intValues[0] };
		if (count == 2)
			return new int[] { intValues[0], intValues[1] };
		// count > 2
		int[] dest = new int[count];
		System.arraycopy(intValues, 0, dest, 0, count);

		return dest;
	}

	public boolean containGT() {
		return containGT;
	}

	public int[] getVariantIndexes(int index) {
		return sampleVariants[index];
	}

	public String getHome(int index) {
		return homos[index];
	}

	public String getFormat() {
		return format;
	}
	public String getSample(int SelectedIndex) {
		return samples[SelectedIndex];
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
