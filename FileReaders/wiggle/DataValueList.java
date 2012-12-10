package FileReaders.wiggle;

import java.text.NumberFormat;

/**
 * 
 * @author Chengwu Yan
 * 
 */
public class DataValueList {
	/**
	 * 0-base. inclusive
	 */
	private int start;
	/**
	 * 0-base. exclusive
	 */
	private int end;

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
		this.start = start - 1;
		this.end = end;
		width = ((this.end - this.start) < (windowSize / step)) ? (this.end - this.start)
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
		int _start = dv.getStart() < start ? start : dv.getStart();
		int _end = dv.getEnd();
		float value = dv.getDataValue();

		int startIndex = (int) ((_start - start) / span);
		int endIndex = (int) ((_end - start) / span);
		double thisStart = 0;
		double thisEnd = 0;
		float thisSpan = 0;
		for (int index = startIndex; index <= endIndex && index < width; index++) {
			thisStart = ((index == startIndex) ? (_start - start)
					: (span * index));
			thisEnd = (index == endIndex) ? (_end - start)
					: (span * (index + 1));
			thisSpan = (float) (thisEnd - thisStart);
			values[index] += thisSpan * value;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(3);

		for (int i = 0; i < values.length - 1; i++) {
			builder.append(format.format(values[i] / span));
			builder.append(";");
		}
		if (values.length > 0) {
			builder.append(format.format(values[values.length - 1] / span));
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