package edu.hit.mlg.Tools;

import java.util.StringTokenizer;

/**
 * Provide faster method to split String by just one char. You can use one
 * instance of StringSplit to split just one or many String, but each time,
 * getResultNum(), getResult() and getResultByIndex(int index) is relative to
 * the current String.
 * 
 * @author Chengwu Yan
 * 
 */
public class StringSplit {
	private char splitChar;
	/**
	 * Store the result of spliting String by splitChar.
	 */
	private String[] result;
	/**
	 * Number of the split result.
	 */
	private int resultNum;

	/**
	 * Initialize an instance, which will split String by splitChar.
	 * 
	 * @param splitChar
	 *            The split char
	 */
	public StringSplit(char splitChar) {
		this(splitChar, 16);
	}

	/**
	 * Initialize an instance, which will split String by splitChar. And the
	 * number of the split result may >= minNum.
	 * 
	 * @param splitChar
	 *            The split char
	 * @param minNum
	 *            The possible minimum number of the split result
	 */
	public StringSplit(char splitChar, int minNum) {
		this.splitChar = splitChar;
		result = new String[Math.max(minNum, 16)];
		resultNum = 0;
	}

	public StringSplit split(String str) {
		resultNum = 0;
		if ("".equals(str)) {
			result[0] = "";
			resultNum = 1;
			return this;
		}

		int len = str.length();
		int pos = 0;
		int rn = 0;
		int nextPos = 0;

		while (pos < len) {
			if (rn == result.length) {
				expandCapacity();
			}
			nextPos = str.indexOf(splitChar, pos);
			if (nextPos < 0) {
				nextPos = len;
			}
			result[rn++] = str.substring(pos, nextPos);
			if (nextPos > pos) {
				resultNum = rn;
			}
			pos = nextPos + 1;
		}

		return this;
	}

	/**
	 * Return the number of the result array spliting the current String by
	 * splitChar.
	 * 
	 * @return
	 */
	public int getResultNum() {
		return resultNum;
	}

	/**
	 * Return the split result array.
	 * 
	 * @return
	 */
	public String[] getResult() {
		String[] dest = new String[resultNum];
		System.arraycopy(result, 0, dest, 0, resultNum);
		return dest;
	}

	/**
	 * Return the designate element of the split result by index.
	 * 
	 * @param index
	 *            start from 0
	 * @return Designate element of the split result by index. Or throw
	 *         ArrayIndexOutOfBoundsException if index<0 or
	 *         index>=getResultNum()
	 * @throws ArrayIndexOutOfBoundsException
	 *             Exception will be throwed if index<0 or index>=getResultNum()
	 */
	public String getResultByIndex(int index)
			throws ArrayIndexOutOfBoundsException {
		if (index < 0 || index >= resultNum)
			throw new ArrayIndexOutOfBoundsException(index);
		return result[index];
	}

	private void expandCapacity() {
		String[] dest = new String[(result.length + 1) * 2];
		System.arraycopy(result, 0, dest, 0, resultNum);
		result = null;
		result = dest;
	}

	/**
	 * Compare three method to split String by char:<br>
	 * <strong>1: </strong>String.split(String splitChar); <br>
	 * <strong>2: </strong>StringTokenizer(String str, String splitStr);<br>
	 * <strong>3: </strong>new StringSplit(char splitChar, int minNum);<br>
	 * and access all elements from the result.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i <= 10000; i++) {
			builder.append(i);
			builder.append(";");
		}
		String testStr = builder.toString();

		long start = System.currentTimeMillis();
		String[] strs;
		for (int i = 0; i < 1000; i++) {
			strs = testStr.split(";");
			for (String str : strs) {

			}
		}
		System.out.println(System.currentTimeMillis() - start);
		start = System.currentTimeMillis();
		StringTokenizer tokenizer;
		String str;
		for (int i = 0; i < 1000; i++) {
			tokenizer = new StringTokenizer(testStr, ";");
			while (tokenizer.hasMoreTokens()) {
				str = tokenizer.nextToken();
			}
		}
		System.out.println(System.currentTimeMillis() - start);
		start = System.currentTimeMillis();
		StringSplit ss = new StringSplit(';', 10000);
		int resultNum = 0;
		for (int i = 0; i < 1000; i++) {
			ss.split(testStr);
			resultNum = ss.getResultNum();
			for (int index = 0; index < resultNum; index++) {
				str = ss.getResultByIndex(index);
			}
		}
		System.out.println(System.currentTimeMillis() - start);
	}
}
