package FileReaders.wiggle;

import java.text.NumberFormat;

/**
 * Deal all WigItem from given wiggle file or bigwig file, given chromosome,
 * given region, given windowSize and given step to ValueList.
 * 
 * @author Chengwu Yan
 * 
 */
public class DataValueList {
	/**
	 * start base of the region. 0-base
	 */
	private int start;
	/**
	 * end base of the region. 0-base
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
	 * width / total bases
	 */
	private float wPerBase;
	/**
	 * total bases / width
	 */
	private float bPerWidth;
	/**
	 * Number of zoom level bases
	 */
	private int zoomLevelBases = 1;
	/**
	 * width of screen
	 */
	private int windowSize = 0;
	/**
	 * A step defines how many pixes show a grid
	 */
	private int step = 0;

	/**
	 * Use default zoomLevelBases = 1
	 * 
	 * @param start
	 *            start base of the region. 1-base
	 * @param end
	 *            end base of the region. 1-base
	 * @param windowSize
	 *            width of screen
	 * @param step
	 *            A step defines how many pixes show in a grid
	 */
	public DataValueList(int start, int end, int windowSize, int step) {
		this(start, end, windowSize, step, 1);
	}

	/**
	 * 
	 * @param start
	 *            start base of the region. 1-base
	 * @param end
	 *            end base of the region. 1-base
	 * @param windowSize
	 *            width of screen
	 * @param step
	 *            A step defines how many pixes show in a grid
	 * @param zoomLevelBases
	 *            Number of zoom level bases
	 */
	public DataValueList(int start, int end, int windowSize, int step,
			int zoomLevelBases) {
		this.start = start - 1;
		this.end = end;
		this.zoomLevelBases = zoomLevelBases;
		this.windowSize = windowSize;
		this.step = step;
		width = ((end - this.start) < (windowSize / step)) ? (end - this.start)
				: (windowSize / step);
		values = new float[width];
		for (int i = 0; i < width; i++)
			values[i] = 0;
		this.wPerBase = (float) width / (end - this.start);
		this.bPerWidth = (end - this.start) / (float) width;
	}

	/**
	 * Change the value of zoomLevelBases
	 * 
	 * @param zoomLevelBases
	 *            Number of zoom level bases
	 */
	public void setZoomLevelBases(int zoomLevelBases) {
		this.zoomLevelBases = zoomLevelBases;
	}

	/**
	 * Get width of screen
	 * 
	 * @return
	 */
	public int getWindowSize() {
		return windowSize;
	}

	/**
	 * Get how many pixes show in a grid
	 * 
	 * @return
	 */
	public int getStep() {
		return step;
	}

	/**
	 * Every update will cause the ValueList changed. When reading a new
	 * DataValue from wiggle file or bigwig file, you should call this method!
	 * 
	 * @param dv
	 */
	public void update(DataValue dv) {
		DataValue[] dvs = null;
		if (dv.getEnd() - dv.getStart() <= zoomLevelBases) {
			dvs = new DataValue[] { dv };
		} else {
			dvs = distribute(dv);
		}

		if (zoomLevelBases == 1) {
			updateWithHighestZoom(dvs);
		} else {
			updateWithLowZoom(dvs);
		}
	}

	/**
	 * With ZoomLevelBases==1
	 * 
	 * @param dvs
	 */
	private void updateWithHighestZoom(DataValue[] dvs) {
		int pos = 0;
		int firstPos = 0;
		for (DataValue d : dvs) {
			firstPos = d.getStart();
			if (firstPos >= end)
				break;
			pos = (int) ((firstPos - start) * wPerBase);
			if (pos < 0)
				continue;
			if (pos >= width) {
				pos = width - 1;
			}
			values[pos] += d.getDataValue();
		}
	}

	/**
	 * With ZoomLevelBases>1
	 * 
	 * @param dvs
	 */
	private void updateWithLowZoom(DataValue[] dvs) {
		int thisStart = 0;
		int thisEnd = 0;
		int startPos = 0;
		int endPos = 0;
		for (DataValue d : dvs) {
			thisStart = d.getStart();
			if (thisStart >= end)
				break;
			thisStart -= start;
			thisEnd = d.getEnd() - start;
			if (thisEnd < 0)
				continue;
			startPos = (int) (thisStart * wPerBase);
			endPos = (int) (thisEnd * wPerBase);
			if (startPos < 0)
				startPos = 0;
			if (endPos >= width)
				endPos = width - 1;
			if (startPos == endPos) {
				values[startPos] += d.getDataValue() * (thisEnd - thisStart);
			} else {
				// Calculate the seperate position, use Math.floor(double d)
				int sepPos = (int) Math.floor(((startPos + 1) * bPerWidth));
				values[startPos] += d.getDataValue() * (sepPos - thisStart);
				values[endPos] += d.getDataValue() * (thisEnd - sepPos);
			}
		}
	}

	/**
	 * <pre>
	 * For example, dv={[1, 17): 0.5}, zoomLevelBases=5, return
	 * [{[1, 6): 0.5}, {[6, 11): 0.5}, {[11, 16): 0.5}, {[16, 21): 0.5}]
	 * @param dv
	 * @return
	 */
	private DataValue[] distribute(DataValue dv) {
		String chr = dv.getChr();
		int start = dv.getStart();
		int end = dv.getEnd();
		float value = dv.getDataValue();
		DataValue[] dvs = new DataValue[(int) Math
				.ceil(((end - start) / (double) zoomLevelBases))];
		for (int i = start, index = 0; i < end; i += zoomLevelBases, index++) {
			dvs[index] = new DataValue(chr, i, i + zoomLevelBases, value);
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
			formatNum = format.format(values[i] / bPerWidth);
			builder.append(formatNum);
			builder.append(";");
		}
		if (values.length > 0) {
			formatNum = format.format(values[values.length - 1] / bPerWidth);
			builder.append(formatNum);
		}

		return builder.toString();
	}
}