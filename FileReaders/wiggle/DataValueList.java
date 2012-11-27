package FileReaders.wiggle;

import java.text.NumberFormat;

public class DataValueList {
	/**
	 * 1-base
	 */
	private int start;

	/**
	 * ValueList in array format
	 */
	private float[] values;

	/**
	 * size of values
	 */
	private int width = 0;

	/**
	 * How many bases an element of values contained.
	 */
	private float span = 1;

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
	public DataValueList(int start, int end, int windowSize, int step) {
		this.start = start;
		width = ((end - start + 1) < (windowSize / step)) ? (end - start + 1)
				: (windowSize / step);

		values = new float[width];

		for (int i = 0; i < width; i++)
			values[i] = 0;

		span = (float) ((end - start + 1.0) / width);
	}

	/**
	 * Every update will cause the ValueList changed.
	 * 
	 * @param dv
	 */
	public void update(DataValue dv) {
		int _start = dv.getStart() < (start - 1) ? (start - 1) : dv.getStart();
		int _end = dv.getEnd();
		float value = dv.getDataValue();
		float curPos = _start;

		for (int index = (int) ((_start - start + 1) / span); curPos < _end
				&& index < width; index++) {
			float curEnd = (index + 1) * span + start - 1;
			curEnd = (_end < curEnd) ? _end : curEnd;
			values[index] += (curEnd - curPos) * value;
			curPos = curEnd;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(3);

		for (double value : values) {
			builder.append(format.format(value / span));
			builder.append(";");
		}

		return builder.toString();
	}

	public static void main(String[] args) {
		DataValueList dvl1 = new DataValueList(1, 10, 16, 2);
		dvl1.update(new DataValue("chr1", 1, 2, 0.1f));
		dvl1.update(new DataValue("chr1", 3, 5, 0.2f));
		dvl1.update(new DataValue("chr1", 5, 6, 0.3f));
		dvl1.update(new DataValue("chr1", 7, 9, 0.4f));
		System.out.println(dvl1.toString());

		DataValueList dvl2 = new DataValueList(1, 10, 32, 2);
		dvl2.update(new DataValue("chr1", 1, 2, 0.1f));
		dvl2.update(new DataValue("chr1", 3, 5, 0.2f));
		dvl2.update(new DataValue("chr1", 5, 6, 0.3f));
		dvl2.update(new DataValue("chr1", 7, 9, 0.4f));
		System.out.println(dvl2.toString());
	}
}