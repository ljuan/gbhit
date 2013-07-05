package filereaders.bam;

/**
 * 
 * @author Chengwu Yan
 * 
 */
public class BAMValueList {
	/**
	 * How many bases a region contained.
	 */
	private float span = 1;

	/**
	 * Number of regions
	 */
	int divideWindowSize;

	/**
	 * Start position of each region
	 */
	int[] starts;

	/**
	 * result in double
	 */
	double[] regions;

	/**
	 * 
	 * @param start
	 *            1-base start base of the region
	 * @param end
	 *            1-base end base of the region
	 * @param windowSize
	 *            width of screen
	 * @param step
	 *            A step defines how many pixes show a grid.
	 */
	public BAMValueList(int start, int end, int windowSize, int step) {
		divideWindowSize = windowSize / step;
		span = (float) ((end - start + 1) / (windowSize / ((float) step)));

		starts = new int[(divideWindowSize) + 1];
		for (int i = 0; i <= divideWindowSize; i++)
			starts[i] = (int) Math.round(i * span);
		starts[divideWindowSize] = end - start + 1;

		regions = new double[divideWindowSize];
		for (int i = 0; i < divideWindowSize; i++)
			regions[i] = 0;
	}

	/**
	 * Every update will cause the result changed.
	 * 
	 * @param relativeStart
	 *            Start position relative to alignment start
	 * @param relativeEnd
	 *            End position relative to alignment start
	 */
	public void update(int relativeStart, int relativeEnd) {
		int startRegion = relativeStart <= 0 ? 0
				: ((int) (relativeStart / span));
		int endRegion = (int) (relativeEnd / span);
		if (endRegion >= divideWindowSize)
			endRegion = divideWindowSize - 1;
		float divide = (float) (1.0 / (endRegion - startRegion + 1));
		for (int i = startRegion; i <= endRegion; i++) {
			regions[i] += divide;
		}
	}

	public double[] getResults() {
		return regions;
	}

	/**
	 * Explicit Converse every element of double array to integer, and link them
	 * with ";"
	 * 
	 * <pre>
	 * For example, array={1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.7, 9.9, 10.0},
	 * return "1;2;3;4;5;6;7;8;9;10".
	 * @param array
	 * @return
	 */
	public static String doubleArray2IntString(double[] array) {
		if (array == null)
			return null;
		StringBuilder builder = new StringBuilder();
		int len = array.length;
		for (int i = 0; i < len - 1; i++) {
			builder.append((int) array[i]);
			builder.append(';');
		}
		if (len > 0)
			builder.append((int) array[len - 1]);
		return builder.toString();
	}

	/**
	 * Link every element of integer array with ";"
	 * 
	 * <pre>
	 * For example, array={1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
	 * return "1;2;3;4;5;6;7;8;9;10".
	 * @param array
	 * @return
	 */
	public static String intArray2IntString(int[] array) {
		return intArray2IntString(array, 0, array.length - 1);
	}
	
	public static String intArray2IntString(int[] array, char c) {
		return intArray2IntString(array, 0, array.length - 1, c);
	}
	
	/**
	 * Link every element of integer array of the certain region with ";"
	 * 
	 * <pre>
	 * For example, array={1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, from=1, to=8
	 * return "2;3;4;5;6;7;8;9".
	 * @param array
	 * @param from from index, inclusive.
	 * @param to to index, inclusive.
	 * @return
	 */
	public static String intArray2IntString(int[] array, int from, int to){
		return intArray2IntString(array, from, to, ';');
	}
	
	public static String intArray2IntString(int[] array, int from, int to, char c){
		if(array == null || from < 0 || from >= array.length || to < 0 || to >= array.length || from > to)
			return null;
		StringBuilder builder = new StringBuilder();
		for (int i = from; i <= to; i++) {
			builder.append(array[i]);
			builder.append(c);
		}
		return builder.toString().substring(0, builder.length() - 1);
	}
}
