package filereaders;

import filereaders.tools.StringSplit;

/**
 * BED Format Class Represents one BED record It seems BED file should be either
 * 3 column or 12 column, no other option. This class is more flexible in column
 * number. We hope this is compatible with CytoBand format.
 */

class Bed {
	String chrom;
	int chromStart;
	int chromEnd;
	String name;
	int score;
	String strand;
	int thickStart;
	int thickEnd;
	Rgb itemRgb = null;
	int blockCount;
	int[] blockSizes;
	int[] blockStarts;
	int fields;

	Bed(String[] temp, int fields) {
		this.fields = fields;
		chrom = temp[0];
		chromStart = Integer.parseInt(temp[1]);
		chromEnd = Integer.parseInt(temp[2]);
		if (fields > 3) {
			name = temp[3];
			if (fields > 4) {
				score = Integer.parseInt(temp[4]);
				if (fields > 5) {
					strand = temp[5];
					if (fields > 7) {
						thickStart = Integer.parseInt(temp[6]);
						thickEnd = Integer.parseInt(temp[7]);
						if (fields > 8) {
							itemRgb = new Rgb(temp[8]);
							if (fields > 11) {
								blockCount = Integer.parseInt(temp[9]);
								StringSplit startsSplit = new StringSplit(',',
										blockCount);
								StringSplit sizesSplit = new StringSplit(',',
										blockCount);
								startsSplit.split(temp[11]);
								sizesSplit.split(temp[10]);
								blockSizes = new int[blockCount];
								blockStarts = new int[blockCount];
								for (int i = 0; i < blockCount; i++) {
									blockSizes[i] = Integer.parseInt(sizesSplit
											.getResultByIndex(i));
									blockStarts[i] = Integer
											.parseInt(startsSplit
													.getResultByIndex(i));
								}
							}
						}
					}
				}
			}
		}
	}
}