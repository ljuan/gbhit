package FileReaders.wiggle;

/**
 * 
 * <b>Note</b>:Region of DataValue is 0-base
 * 
 * @author Chengwu Yan
 * 
 */
public class DataValue implements Comparable<DataValue> {
	private String chr = "";
	private int start = 0;
	private int end = 0;
	private float dataValue = 0;

	/**
	 * <b>Note</b>:Region of DataValue is 0-base
	 * 
	 * @param chr
	 * @param start
	 *            0-base. inclusive
	 * @param end
	 *            0-base. exclusive
	 * @param dataValue
	 */
	public DataValue(String chr, int start, int end, float dataValue) {
		this.chr = chr;
		this.start = start;
		this.end = end;
		this.dataValue = dataValue;
	}

	public DataValue(DataValue dv) {
		this(dv.getChr(), dv.getStart(), dv.getEnd(), dv.getDataValue());
	}

	public DataValue(String[] values) {
		this(values[0], Integer.parseInt(values[1]), Integer
				.parseInt(values[2]), Float.parseFloat(values[3]));
	}

	public String getChr() {
		return chr;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public float getDataValue() {
		return dataValue;
	}

	public void setDataValue(float dataValue) {
		this.dataValue = dataValue;
	}

	@Override
	public String toString() {
		return "chr:" + this.chr + "\t[" + this.start + ", " + this.end
				+ ")\tdataValue:" + this.dataValue;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof DataValue))
			return false;

		if (this == o)
			return true;

		DataValue dv = (DataValue) o;

		return this.chr.equals(dv.getChr()) && this.start == dv.getStart()
				&& this.end == dv.getEnd();
	}

	@Override
	public int compareTo(DataValue o) {
		return this.start - o.start;
	}
}
