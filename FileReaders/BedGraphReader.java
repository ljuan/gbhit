package FileReaders;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.w3c.dom.Document;

import FileReaders.wiggle.DataValue;
import FileReaders.wiggle.DataValueList;

/**
 * Get ValueList from BedGraph.
 * 
 * <pre>
 * Usage:
 * new BedGraphReader(String uri).write_bedGraph2Values
 * 		(Document doc, String track, String chr, int start, int end, int windowSize, int step)
 * @author Chengwu Yan
 * 
 */
public class BedGraphReader {
	/**
	 * file path of the BedGraph file
	 */
	private String filePath = null;

	/**
	 * 
	 * @param filePath
	 *            file path of the BedGraph file
	 */
	BedGraphReader(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * 
	 * @param doc
	 * @param track
	 * @param chr
	 * @param start
	 *            1-base
	 * @param end
	 *            1-base
	 * @param windowSize
	 *            size, in pixel. Make sure that size Can be divided by 2
	 * @param step
	 *            A step defines how many pixes show a grid.
	 * @throws IOException
	 */
	public void write_bedGraph2Values(Document doc, String track, String chr,
			int start, int end, int windowSize, int step) throws IOException {
		DataValueList values = new DataValueList(start, end, windowSize, step);
		BedGraph.BedGraphLineIterator itor = new BedGraph(filePath, chr, start,
				end).iterator();
		if (itor != null) {
			while (itor.hasNext()) {
				values.update(itor.next());
			}
			itor.close();
		}

		WiggleReader.writeDataValues2XML(doc, track, start, end,
				values.toString());
	}
}

class BedGraph implements Iterable<DataValue> {
	/**
	 * file path of the BedGraph file
	 */
	private String filePath;
	/**
	 * chromosome name
	 */
	private String chrom;
	/**
	 * 0-base, inclusive.
	 */
	private int start;
	/**
	 * 0-base, exclusive.
	 */
	private int end;

	private BufferedReader br;

	BedGraph(String filePath, String chrom, int start, int end) {
		this.filePath = filePath;
		this.chrom = chrom;
		this.start = start;
		this.end = end;
	}

	@Override
	public BedGraphLineIterator iterator() {
		if (this.chrom == null)
			return null;

		try {
			br = new BufferedReader(new FileReader(this.filePath), 1024 * 1024);
		} catch (FileNotFoundException e) {
			return null;
		}
		if (br == null)
			return null;

		String line = null;
		while ((line = nextLine()) != null) {
			// ////////////////////////////////START
			/**
			 * <pre>
			 * Instead of:
			 * if (!line.startsWith(this.chrom))
			 * 	continue;
			 * To increase efficiency
			 * </pre>
			 */
			int ii = 0;
			for (ii = 0; ii < this.chrom.length(); ii++)
				if (this.chrom.charAt(ii) != line.charAt(ii))
					break;
			// Haven't found chromosome==chrom
			if (ii < this.chrom.length())
				continue;
			// ///////////////////////////////END

			// /////////////////////////////////////////////////START
			/**
			 * <pre>
			 * Instead of:
			 * line.split("\t");
			 * To increase efficiency
			 * </pre>
			 */
			int pos = 0;
			for (pos = line.length() - 1; pos >= 0; pos--) {
				if (line.charAt(pos) == '\t') {
					pos--;
					break;
				}
			}

			int endRegion = 0;
			for (int i = 1; pos >= 0; pos--, i *= 10) {
				if (line.charAt(pos) == '\t') {
					pos--;
					break;
				}
				endRegion += i * (line.charAt(pos) - 48);
			}
			// Haven't found dataValue from the given region
			if (endRegion <= this.start)
				continue;

			int startRegion = 0;
			for (int i = 1; pos >= 0; pos--, i *= 10) {
				if (line.charAt(pos) == '\t')
					break;
				startRegion += i * (line.charAt(pos) - 48);
			}
			// Over the given region
			if (startRegion >= this.end) {
				line = null;
				break;
			}
			// Found the first datavalue from the given region of the chrom
			break;
			// ///////////////////////////////////////////////////////////END
		}
		// not found the chromosome or dataValues from the given region
		if (line == null)
			return null;
		return new BedGraphLineIterator(line);
	}

	/**
	 * return a single line from file.
	 * 
	 * @return
	 */
	private String nextLine() {
		try {
			return br.readLine();
		} catch (IOException e) {
			return null;
		}
	}

	class BedGraphLineIterator implements Iterator<DataValue> {
		private boolean hasNext = true;
		private String[] values = null;

		BedGraphLineIterator(String line) {
			this.values = line.split("\t");
			this.hasNext = true;
		}

		@Override
		public boolean hasNext() {
			return this.hasNext;
		}

		@Override
		public DataValue next() {
			DataValue dv = (this.values == null) ? null : new DataValue(
					this.values);

			String line = nextLine();
			if (line == null) {
				this.hasNext = false;
				this.values = null;
				return dv;
			}

			int ii = 0;
			boolean startsCorrect = true;
			for (ii = 0; ii < BedGraph.this.chrom.length(); ii++)
				if (BedGraph.this.chrom.charAt(ii) != line.charAt(ii))
					break;
			// Haven't found chromosome==chrom
			if (ii < BedGraph.this.chrom.length())
				startsCorrect = false;

			if (startsCorrect) {
				int pos = BedGraph.this.chrom.length() + 1;
				int startRegion = 0;
				for (;;) {
					if (line.charAt(pos) == '\t') {
						pos++;
						break;
					}
					startRegion = startRegion * 10 + (line.charAt(pos++) - 48);
				}

				if (startRegion < BedGraph.this.end) {
					this.values = line.split("\t");
				} else {
					this.hasNext = false;
					this.values = null;
				}
			} else {
				this.hasNext = false;
				this.values = null;
			}

			return dv;
		}

		@Override
		public void remove() {
			try {
				throw new Exception("Unsupported mothod");
			} catch (Exception e) {
			}
		}

		void close() {
			try {
				BedGraph.this.br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}