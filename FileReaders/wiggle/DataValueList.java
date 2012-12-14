package FileReaders.wiggle;

import java.text.NumberFormat;

/**
 * @author Chengwu Yan
 * 
 */
public class DataValueList {
	/**
	 * start base of the region. 0-base
	 */
	private int start;
	/**
	 * ValueList in array format
	 */
	private float[] values;
	/**
	 * Number of DataValue of each grid.
	 */
	private int[] numOfGrids;

	/**
	 * size of values
	 */
	private int width = 0;
	/**
	 * width / total bases
	 */
	private float wPerBase;

	/**
	 * 
	 * @param start
	 *            start base of the region. 1-base
	 * @param end
	 *            end base of the region. 1-base
	 * @param windowSize
	 *            width of screen
	 * @param step
	 *            A step defines how many pixes show a grid.
	 */
	public DataValueList(int start, int end, int windowSize, int step) {
		this.start = start - 1;
		width = ((end - this.start) < (windowSize / step)) ? (end - this.start)
				: (windowSize / step);
		values = new float[width];
		numOfGrids = new int[width];
		for (int i = 0; i < width; i++)
			values[i] = 0;
		this.wPerBase = (float) width / (end - this.start);
	}

	/**
	 * Every update will cause the ValueList changed.
	 * 
	 * @param dv
	 */
	public void update(DataValue dv) {
		DataValue[] dvs = distribute(dv);
		int pos = 0;
		
		for (DataValue d : dvs) {
			pos = (int) ((d.getStart() - start) * wPerBase);
			if (pos < 0)
				pos = 0;
			values[pos] += d.getDataValue();
			numOfGrids[pos]++;
		}
	}

	/**
	 * <pre>
	 * For example, dv={[1, 5): 0.5}, return
	 * [{[1, 2): 0.5}, {[2, 3): 0.5}, {[3, 4): 0.5}, {[4, 5): 0.5}]
	 * @param dv
	 * @return
	 */
	private DataValue[] distribute(DataValue dv) {
		String chr = dv.getChr();
		int start = dv.getStart();
		int end = dv.getEnd();
		float value = dv.getDataValue();
		DataValue[] dvs = new DataValue[end - start];
		for (int i = start; i < end; i++) {
			dvs[i - start] = new DataValue(chr, i, i + 1, value);
		}
		return dvs;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(3);
		String formatNum = null;
		for (int i = 0; i < values.length - 1; i++) {
			formatNum = format.format(numOfGrids[i] == 0 ? 0
					: (values[i] / numOfGrids[i]));
			builder.append(formatNum);
			builder.append(";");
		}
		if (values.length > 0) {
			formatNum = format
					.format(numOfGrids[values.length - 1] == 0 ? 0
							: (values[values.length - 1] / numOfGrids[values.length - 1]));
			builder.append(formatNum);
		}

		return builder.toString();
	}

}