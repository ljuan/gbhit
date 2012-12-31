package FileReaders.wiggle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Extract all WigItem from given wiggle file or bigwig file, given chromosome,
 * given region, given windowSize and given step. And deal them to ValueList.
 * 
 * @author Chengwu Yan
 * 
 */
public class WiggleExtractor {
	/**
	 * Separator of the declaration line
	 */
	private final char separator = ' ';
	/**
	 * file path of the BedGraph file
	 */
	private String filePath;
	/**
	 * If the file is a BigWig file, isBigWig=true, false else. If the file
	 * comes from remote server, isBigWig must be true.
	 */
	private boolean isBigWig = false;
	/**
	 * chromosome name
	 */
	private String chrom;
	/**
	 * 1-base
	 */
	private int start;
	/**
	 * 1-base
	 */
	private int end;

	private BufferedReader br;
	/**
	 * Total bases in [start, end].
	 */
	private int bases = 0;

	private DataValueList wiggles;

	/**
	 * 
	 * @param filePath
	 * @param chrom
	 * @param start
	 *            1-base
	 * @param end
	 *            1-base
	 * @param isBigWig
	 * @param windowSize
	 *            width of screen
	 * @param step
	 *            A step defines how many pixes show in a grid
	 */
	public WiggleExtractor(String filePath, String chrom, int start, int end,
			boolean isBigWig, int windowSize, int step) {
		this.filePath = filePath;
		this.chrom = chrom;
		this.start = start;
		this.end = end;
		this.isBigWig = isBigWig;
		bases = this.end - this.start + 1;
		wiggles = new DataValueList(start, end, windowSize, step);
	}

	/**
	 * Extract all WigItem from given wiggle file or bigwig file, given
	 * chromosome, given region, given windowSize and given step. And deal them
	 * to ValueList.
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public DataValueList extract() throws FileNotFoundException, IOException {
		if (isBigWig) {
			try {
				new BigWigReader(this.filePath).getBigWig(this.chrom,
						this.start, this.end, false, this.wiggles,
						this.wiggles.getWindowSize(), this.wiggles.getStep())
						.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			open();
			extract_wiggle();
			close();
		}

		return this.wiggles;
	}

	private void extract_wiggle() {
		String decLine = skipHead();
		while (decLine != null) {
			decLine = (decLine.charAt(0) == 'f') ? dealFixedStep(decLine)
					: dealVariableStep(decLine);
		}
	}

	/**
	 * Skip head and return the first Declaration line.
	 * 
	 * @return
	 * @throws IOException
	 */
	private String skipHead() {
		String line = null;
		while ((line = nextLine()) != null) {
			if (line.startsWith("fixedStep") || line.startsWith("variableStep"))
				break;
		}
		return line;
	}

	/**
	 * Deal a fixedStep line, and return next Declaration line. return null if
	 * eof occured.
	 * 
	 * @param fixedStep
	 * @return
	 */
	private String dealFixedStep(String fixedStep) {
		StepDeclaration fsd = resolvingFixedStep(fixedStep);
		if (fsd == null)
			return nextStepLine();

		String line = null;
		String chrom = fsd.chrom;
		int step = fsd.step;
		int span = fsd.span;

		while ((line = nextLine()) != null) {
			if (line.charAt(0) == 'f' || line.charAt(0) == 'v')
				break;

			int start = fsd.start;
			int end = start + span - 1;
			if (end < this.start) {
				fsd.start += step;
				continue;
			}
			if (start > this.end) {
				return nextStepLine();
			}
			wiggles.update(new DataValue(chrom, start - 1, end, Float
					.parseFloat(line)));
			if (end > this.end)
				end = this.end;
			if (start < this.start)
				start = this.start;
			bases -= end - start + 1;
			if (bases <= 0)
				return null;
			fsd.start += step;
		}

		return line;
	}

	/**
	 * Resolve the fixedStep Declaration line into a FixedStepDeclaration object
	 * and return it. Null will be returned if the chromosome!=this.chrom.
	 * 
	 * @param fixedStep
	 * @return
	 */
	private FixedStepDeclaration resolvingFixedStep(String fixedStep) {
		int pos = 16;
		char[] char_fixedStep = fixedStep.toCharArray();
		String _chrom = this.chrom + separator;
		int length = _chrom.length();
		for (int i = 0; i < length; pos++, i++) {
			// Incorrect chromosome
			if (_chrom.charAt(i) != char_fixedStep[pos]) {
				return null;
			}
		}
		// skip String:"start="
		pos += 6;
		int start = 0;
		// default step=1
		int step = 0;
		// default span=1
		int span = 0;
		int _length = char_fixedStep.length;

		for (; pos < _length;) {
			if (char_fixedStep[pos] == separator) {
				pos++;
				break;
			}
			start = start * 10 + (char_fixedStep[pos++] - 48);
		}
		if (pos < _length) {
			pos++;
			int temp = 0;
			boolean isStep = (char_fixedStep[pos] == 't') ? true : false;
			pos += 4;
			for (; pos < _length;) {
				if (char_fixedStep[pos] == separator) {
					pos++;
					break;
				}
				temp = temp * 10 + (char_fixedStep[pos++] - 48);
			}
			if (!isStep)
				span = temp;
			else
				step = temp;
		}
		if (pos < _length) {
			// span
			pos += 5;
			for (; pos < _length;) {
				if (char_fixedStep[pos] == separator) {
					break;
				}
				span = span * 10 + (char_fixedStep[pos++] - 48);
			}
		}

		return new FixedStepDeclaration(this.chrom, start, (step == 0) ? 1
				: step, (span == 0) ? 1 : span);
	}

	class StepDeclaration {
		String chrom;
		/**
		 * 1-base
		 */
		int start = 0;
		int step = 1;
		int span = 1;

		StepDeclaration(String chrom, int start, int step, int span) {
			this.chrom = chrom;
			this.start = start;
			this.step = step;
			this.span = span;
		}
	}

	/**
	 * fixedStep is for data with regular intervals between new data values and
	 * is the more compact wiggle format. It begins with a declaration line and
	 * is followed by a single column of data values:
	 * 
	 * <pre>
	 * fixedStep chrom=chrN start=position [step=stepInterval] [span=windowSize]
	 * dataValue1 
	 * dataValue2 
	 * ... etc ...
	 * 
	 * For example:
	 * fixedStep chrom=chr3 start=400601 step=100 span=5
	 * 11
	 * 22
	 * 33
	 * </pre>
	 * 
	 * causes the values 11, 22, and 33 to be displayed as 5-base regions on
	 * chromosome 3 at positions 400601-400605, 400701-400705, and
	 * 400801-400805, respectively.</br> <b>Note</b>: the same span must be used
	 * throughout the dataset. If no span is specified, the default span of 1 is
	 * used. And the same step size must be used throughout the dataset. If no
	 * step size is specified, the default step size of 1 is used.
	 * 
	 * @author ChengWu Yan
	 * 
	 */
	private class FixedStepDeclaration extends StepDeclaration {
		FixedStepDeclaration(String chrom, int start, int step, int span) {
			super(chrom, start, step, span);
		}

		@Override
		public String toString() {
			return "fixedStep: chrom=" + this.chrom + ", start=" + this.start
					+ ", step=" + this.step + ", span=" + this.span;
		}
	}

	/**
	 * Deal a variableStep line, and return next Declaration line. return null
	 * if eof occured.
	 * 
	 * @param variableStep
	 * @return
	 */
	private String dealVariableStep(String variableStep) {
		VariableStepDeclaration vsd = resolvingVariableStep(variableStep);
		if (vsd == null)
			return nextStepLine();

		String line = null;
		int span = vsd.span;
		while ((line = nextLine()) != null) {
			if (line.charAt(0) == 'f' || line.charAt(0) == 'v')
				break;

			int regionStart = 0;
			int line_pos = 0;
			for (;;) {
				if (line.charAt(line_pos) == ' '
						|| line.charAt(line_pos) == '\t') {
					line_pos++;
					break;
				}
				regionStart = regionStart * 10 + (line.charAt(line_pos++) - 48);
			}

			int regionEnd = regionStart + span - 1;
			if (regionEnd < this.start) {
				continue;
			}
			if (regionStart > this.end) {
				return nextStepLine();
			}

			wiggles.update(new DataValue(this.chrom, regionStart - 1,
					regionEnd, Float.parseFloat(line.substring(line_pos))));

			if (regionEnd > this.end)
				regionEnd = this.end;
			if (regionStart < this.start)
				regionStart = this.start;
			bases -= regionEnd - regionStart + 1;
			if (bases <= 0)
				return null;
		}

		return line;
	}

	/**
	 * Resolve the variableStep Declaration line into a VariableStepDeclaration
	 * object and return it. Null will be returned if the
	 * chromosome!=this.chrom.
	 * 
	 * @param fixedStep
	 * @return
	 */
	private VariableStepDeclaration resolvingVariableStep(String variableStep) {
		int pos = 19;
		StringBuilder _chrom = new StringBuilder();
		char[] char_variableStep = variableStep.toCharArray();
		int _length = variableStep.length();

		for (; pos < _length;) {
			if (char_variableStep[pos] == separator)
				break;
			_chrom.append(char_variableStep[pos++]);
		}
		String chrom = _chrom.toString();

		if (!this.chrom.equals(chrom)) {
			return null;
		}

		int span = 0;
		if (pos < _length) {
			// skip String:" span="
			pos += 6;
			for (; pos < _length;) {
				span = span * 10 + (char_variableStep[pos++] - 48);
			}
		}
		if (span == 0)
			span = 1;

		return new VariableStepDeclaration(chrom, span);
	}

	/**
	 * variableStep is for data with irregular intervals between new data points
	 * and is the more commonly used wiggle format. It begins with a declaration
	 * line and is followed by two columns containing chromosome positions and
	 * data values:
	 * 
	 * <pre>
	 * variableStep  chrom=chrN  [span=windowSize]
	 * chromStartA  dataValueA
	 * chromStartB  dataValueB
	 * ... etc ...  ... etc ...
	 * </pre>
	 * 
	 * The declaration line starts with the word variableStep and is followed by
	 * a specification for a chromosome. The optional span parameter (default:
	 * span=1) allows data composed of contiguous runs of bases with the same
	 * data value to be specified more succinctly. The span begins at each
	 * chromosome position specified and indicates the number of bases that data
	 * value should cover.
	 * 
	 * <pre>
	 * For example, this variableStep specification:
	 * variableStep chrom=chr2
	 * 300701 12.5
	 * 300702 12.5
	 * 300703 12.5
	 * 300704 12.5
	 * 300705 12.5
	 * 
	 * is equivalent to:
	 * 
	 * variableStep chrom=chr2 span=5
	 * 300701 12.5
	 * </pre>
	 * 
	 * Both versions display a value of 12.5 at position 300701-300705 on
	 * chromosome 2. </br><b>Note</b>: the same span must be used throughout the
	 * dataset. If no span is specified, the default span of 1 is used.
	 * 
	 * @author ChengWu Yan
	 * 
	 */
	private class VariableStepDeclaration extends StepDeclaration {
		VariableStepDeclaration(String chrom, int span) {
			super(chrom, -1, -1, span);
		}

		@Override
		public String toString() {
			return "fixedStep: chrom=" + this.chrom + ", span=" + this.span;
		}
	}

	/**
	 * return a single line from file. This line would't be a line feed.
	 * 
	 * @return
	 */
	private final String nextLine() {
		try {
			String line = null;
			while ((line = br.readLine()) == "\n") {
				;
			}
			return line;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Return next fixedStep Declaration line or VariableStep Declaration line.
	 * 
	 * @return
	 */
	private final String nextStepLine() {
		String line = null;
		while ((line = nextLine()) != null) {
			if (line.charAt(0) == 'f' || line.charAt(0) == 'v')
				break;
		}
		return line;
	}

	private void open() {
		try {
			br = new BufferedReader(new FileReader(this.filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void close() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}