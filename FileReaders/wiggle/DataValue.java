package FileReaders.wiggle;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/*
 * 问题：如果最后返回的DataValue字符串和位置无关，那中间有一片连续的空白无DataValue区域是否对显示有影响。
 * 例如要显示[1, 1000],但是区域[300, 500]无DataValue，而其它子区域有DataValue.
 */
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
	/**
	 * dataValue is a float data, but for convenience, we define it as String.
	 */
	private String dataValue = "";

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
	public DataValue(String chr, int start, int end, String dataValue) {
		this.chr = chr;
		this.start = start;
		this.end = end;
		this.dataValue = dataValue;
	}

	public DataValue(String[] str) {
		this(str[0], Integer.valueOf(str[1]), Integer.valueOf(str[2]), str[3]);
	}

	public DataValue(DataValue dv) {
		this(dv.getChr(), dv.getStart(), dv.getEnd(), dv.getDataValue());
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

	public String getDataValue() {
		return dataValue;
	}

	public void setDataValue(String dataValue) {
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

	/**
	 * Join the neighbouring DataValue. We can ensure that all elements in the
	 * values have the same chromosome.
	 * 
	 * <pre>
	 * For example, values has five elements:
	 * chr=chr1 start=0 end=10 dataValue=0.17
	 * chr=chr1 start=10 end=20 dataValue=0.17
	 * chr=chr1 start=20 end=30 dataValue=0.18
	 * chr=chr1 start=30 end=40 dataValue=0.19
	 * chr=chr1 start=40 end=50 dataValue=0.20 
	 * and after deal, it will return a List contains four elements:
	 * chr=chr1 start=0 end=20 dataValue=0.17
	 * chr=chr1 start=20 end=30 dataValue=0.18
	 * chr=chr1 start=30 end=40 dataValue=0.19
	 * chr=chr1 start=40 end=50 dataValue=0.20 
	 * @param values
	 * @return
	 */
	public static List<DataValue> joinDateValues(List<DataValue> values) {
		List<DataValue> newValues = new ArrayList<DataValue>();

		if (values.size() == 0)
			return newValues;

		DataValue dv1 = values.get(0);

		for (int i = 1; i < values.size(); i++) {
			DataValue dv2 = values.get(i);
			if (dv1.getEnd() == dv2.getStart()
					&& dv1.getDataValue().equals(dv2.getDataValue())) {
				dv1 = new DataValue(dv1.getChr(), dv1.getStart(), dv2.getEnd(),
						dv1.getDataValue());
			} else {
				newValues.add(new DataValue(dv1));
				dv1 = new DataValue(dv2);
			}
		}
		newValues.add(new DataValue(dv1));

		return newValues;
	}

	/**
	 * Filling DataValues with value=0 of each base which has no DataValue in
	 * [start, end]. And cutting DataValue into DataValues with end-start=1
	 * which end-start>1.
	 * 
	 * <pre>
	 * For example, start=1, end=10, and values contains:
	 * start=1, end=2, value=0.1
	 * start=2, end=4, value=0.2
	 * start=6, end=7, value=0.3
	 * start=7, end=9, value=0.4
	 * after deal, it will return a List contains:
	 * start=0, end=1, value=0
	 * start=1, end=2, value=0.1
	 * start=2, end=3, value=0.2
	 * start=3, end=4, value=0.2
	 * start=4, end=5, value=0
	 * start=5, end=6, value=0
	 * start=6, end=7, value=0.3
	 * start=7, end=8, value=0.4
	 * start=8, end=9, value=0.4
	 * start=9, end=10, value=0
	 * @param values
	 * @param start
	 *            1-base
	 * @param end
	 *            1-base
	 * @param chr
	 * @return
	 */
	public static List<DataValue> fillingAndCutting(List<DataValue> values,
			int start, int end, String chr) {
		List<DataValue> newValues = new LinkedList<DataValue>();
		int first = end;
		int last = end;
		int expStart = 0;
		if (values.size() > 0) {
			first = values.get(0).getStart();
			last = values.get(values.size() - 1).getEnd();
			expStart = first;
		}
		for (int i = start - 1; i < first; i++) {
			newValues.add(new DataValue(chr, i, i + 1, "0"));
		}

		for (int i = 0; i < values.size(); i++) {
			DataValue dv = values.get(i);
			int _start = dv.getStart();
			int _end = dv.getEnd();
			String value = dv.getDataValue();
			if (expStart < _start) {
				for (int j = expStart; j < _start; j++) {
					newValues.add(new DataValue(chr, j, j + 1, "0"));
				}
			}
			for (int j = _start; j < _end; j++) {
				newValues.add(new DataValue(chr, j, j + 1, value));
			}
			expStart = _end;
		}

		for (int i = last; i < end; i++) {
			newValues.add(new DataValue(chr, i, i + 1, "0"));
		}

		values = null;

		return newValues;
	}

	/**
	 * For example, if locale = Locale.CHINA, 0.10 will be expressed as 0.1.
	 * 
	 * @param values
	 * @param locale
	 */
	public static void changeLocale(List<DataValue> values, Locale locale) {
		DataValue dv = null;
		NumberFormat snf = NumberFormat.getInstance(locale);
		for (int i = 0; i < values.size(); i++) {
			dv = values.get(i);
			dv.setDataValue(snf.format(Double.valueOf(dv.getDataValue())));
		}
	}

	/**
	 * 
	 * @param windowSize
	 *            Please ensure that windowSize%2==0
	 * @param dvs
	 * @param start
	 *            1-base
	 * @param end
	 *            1-base
	 * @param chr
	 * @return
	 */
	public static String distributeDVsInWindowSize(int windowSize,
			List<DataValue> dvs, int start, int end, String chr) {
		if (windowSize <= 0 || dvs == null)
			return "";

		dvs = DataValue.fillingAndCutting(dvs, start, end, chr);

		if (dvs.size() <= windowSize / 2) {
			StringBuilder builder = new StringBuilder();
			for (DataValue dv : dvs) {
				builder.append(dv.getDataValue());
				builder.append(";");
			}
			return builder.toString();
		}

		double ratio = (end - start + 1) / (windowSize / 2.0);

		return distributeDVsInWindowSize(windowSize, dvs, start, ratio);
	}

	/**
	 * 
	 * @param windowSize
	 * @param dvs
	 *            all regions are 0-base
	 * @param start
	 *            1-base
	 * @param ratio
	 * @return
	 */
	private static String distributeDVsInWindowSize(int windowSize,
			List<DataValue> dvs, int start, double ratio) {
		StringBuilder builder = new StringBuilder();
		NumberFormat nf = NumberFormat.getInstance(Locale.CHINA);
		nf.setMaximumFractionDigits(3);

		double curPos = start - 1;
		double curDes = start - 1 + ratio;
		double value = 0;
		int expNum = windowSize / 2;
		int size = dvs.size();

		for (int i = 0, j = 0; i < expNum && j < size; j++) {
			DataValue dv = dvs.get(j);
			if (dv.getEnd() < curDes) {
				value += Double.parseDouble(dv.getDataValue())
						* (dv.getEnd() - curPos);
				curPos = dv.getEnd();
			} else {
				value += Double.parseDouble(dv.getDataValue())
						* (curDes - curPos);
				curPos = curDes;
				curDes += ratio;
				builder.append(nf.format(value/ratio));
				builder.append(";");
				value = 0;
				i++;
				j--;
			}
		}

		return builder.toString();
	}

	public static void main(String[] args) {
		List<DataValue> values1 = new LinkedList<DataValue>();
		values1.add(new DataValue("chr1", 1, 2, "0.1"));
		values1.add(new DataValue("chr1", 2, 4, "0.2"));
		values1.add(new DataValue("chr1", 6, 7, "0.3"));
		values1.add(new DataValue("chr1", 7, 9, "0.4"));

		for (DataValue dv : DataValue.fillingAndCutting(values1, 1, 10, "chr1"))
			System.out.println(dv);

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();

		List<DataValue> values2 = new LinkedList<DataValue>();
		for (DataValue dv : DataValue.fillingAndCutting(values2, 1, 10, "chr1"))
			System.out.println(dv);

		List<DataValue> values3 = new LinkedList<DataValue>();
		values3.add(new DataValue("chr1", 0, 1, "0.1"));
		values3.add(new DataValue("chr1", 1, 2, "0.2"));
		values3.add(new DataValue("chr1", 2, 3, "0.3"));
		values3.add(new DataValue("chr1", 3, 4, "0.4"));
		values3.add(new DataValue("chr1", 4, 5, "0.5"));
		values3.add(new DataValue("chr1", 5, 6, "0.6"));

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println(DataValue.distributeDVsInWindowSize(8, values3, 1,
				6, "chr1"));
	}
}
