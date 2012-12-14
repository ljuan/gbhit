package FileReaders.bam;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;

public class CigarMD {
	private CigarElement[] mds;
	private Cigar merge = null;
	private int len;
	private int count;

	public CigarMD(Cigar cigar, String MD) {
		mds = new CigarElement[20];
		len = 20;
		count = 0;
		merge(cigar, MD);
	}

	/**
	 * Merge Cigar and MD
	 */
	private void merge(Cigar cigar, String MD) {
		if (MD == null) {
			merge = cigar;
			return;
		}
		merge = new Cigar();
		resolveMD(MD);
		mergeCigarMD(cigar);
	}

	private void resolveMD(String MD) {
		String next = null;
		char first;
		CigarElement ce = null;

		Pattern pattern = Pattern.compile("\\d+|[ATCGN]+|\\^[ATCGN]+");
		Matcher matcher = pattern.matcher(MD);
		while (matcher.find()) {
			next = matcher.group();
			first = next.charAt(0);
			if (first >= '0' && first <= '9') {
				// MATCH
				ce = new CigarElement(getIntValue(next), CigarOperator.M);
			} else if (first == '^') {
				// DEL
				ce = new CigarElement(next.length() - 1, CigarOperator.D);
			} else {
				// Mean SNV
				ce = new CigarElement(1, CigarOperator.X);
			}
			ensureCapacity();
			mds[count++] = ce;
		}
	}

	private void mergeCigarMD(Cigar cigar) {
		List<CigarElement> ces = cigar.getCigarElements();
		int ceNum = ces.size();
		CigarElement md = null;
		CigarElement ce = null;
		int mdRemain = 0;
		int ceRemain = 0;
		int remain = 0;
		int mdPos = 0;
		int cePos = 0;

		while (true) {
			if (ceRemain <= 0) {
				if (cePos >= ceNum)
					break;
				ce = ces.get(cePos++);
				if (ce.getOperator() == CigarOperator.I
						|| ce.getOperator() == CigarOperator.N
						|| ce.getOperator() == CigarOperator.S) {
					merge.add(ce);
					continue;
				}
				if (ce.getOperator() != CigarOperator.M
						&& ce.getOperator() != CigarOperator.D) {
					continue;
				}
				ceRemain = ce.getLength();
			}
			if (mdRemain <= 0) {
				if (mdPos >= count)
					break;
				md = mds[mdPos++];
				if (md.getOperator().equals(CigarOperator.X)) {
					// Mean SNV
					merge.add(md);
					ceRemain -= 1;
					continue;
				}
				mdRemain = md.getLength();
			}
			remain = Math.min(ceRemain, mdRemain);
			merge.add(new CigarElement(remain, ce.getOperator()));
			ceRemain -= remain;
			mdRemain -= remain;
		}
	}

	/**
	 * Get integer value from String. We must affirm that strValue is an integer
	 * in String format before we call this function.
	 * 
	 * @param strValue
	 * @return
	 */
	public static int getIntValue(String strValue) {
		int num = 0;
		char[] cs = strValue.toCharArray();
		for (char c : cs) {
			num = num * 10 + (int) c - 48;
		}

		return num;
	}

	private void ensureCapacity() {
		if (count == len) {
			CigarElement[] ces = new CigarElement[len + len];
			System.arraycopy(mds, 0, ces, 0, len);
			mds = null;
			mds = ces;
			len += len;
		}
	}

	public Cigar getMergedCigar() {
		return this.merge;
	}
}
