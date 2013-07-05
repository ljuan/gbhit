package filereaders;

import filereaders.tools.StringSplit;

/**
 * Basic Annotations for RefSeq Gene, UCSC Known Gene and Ensembl Gene 
 * Basic Annotations is a very small variation from Bed format.
 * Bed.Color -> Gene Symbol
 * Bed.Score -> Gene Name2 
 */

class BasicAnnos {
	String chrom;
	int txStart;
	int txEnd;
	String name;
	String name2;
	String strand;
	int cdsStart;
	int cdsEnd;
	String symbol;
	int exonCount;
	int[] exonSizes;
	int[] exonStarts;

	BasicAnnos(String[] temp, int fields) {
		chrom = temp[0];
		txStart = Integer.parseInt(temp[1]);
		txEnd = Integer.parseInt(temp[2]);
		name = temp[3];
		name2 = temp[4];
		strand = temp[5];
		cdsStart = Integer.parseInt(temp[6]);
		cdsEnd = Integer.parseInt(temp[7]);
		symbol = temp[8];
		exonCount = Integer.parseInt(temp[9]);
		StringSplit startsSplit = new StringSplit(',',exonCount);
		StringSplit sizesSplit = new StringSplit(',',exonCount);
		startsSplit.split(temp[11]);
		sizesSplit.split(temp[10]);
		exonSizes = new int[exonCount];
		exonStarts = new int[exonCount];
		for (int i = 0; i < exonCount; i++) {
			exonSizes[i] = Integer.parseInt(sizesSplit.getResultByIndex(i));
			exonStarts[i] = Integer.parseInt(startsSplit.getResultByIndex(i));
		}
	}
}