package FileReaders;

/* 
 * BED Format Class
 * Represents one BED record
 * It seems BED file should be either 3 column or 12 column, no other option.
 * This class is more flexible in column number.
 * We hope this is compatible with CytoBand format.
 */

class Bed {
	String chrom;
	long chromStart;
	long chromEnd;
	String name;
	int score;
	String strand;
	long thickStart;
	long thickEnd;
	Rgb itemRgb=null;
	int blockCount;
	long[] blockSizes;
	long[] blockStarts;
	int fields;

	Bed(String line){
		String[] temp=line.split("\t");
		fields=temp.length;
		chrom=temp[0];
		chromStart=Long.parseLong(temp[1]);
		chromEnd=Long.parseLong(temp[2]);
		if(fields>3){
			name=temp[3];
			if(fields>4){
				score=Integer.parseInt(temp[4]);
				if(fields>5){
					strand=temp[5];
					if(fields>7){
						thickStart=Long.parseLong(temp[6]);
						thickEnd=Long.parseLong(temp[7]);
						if(fields>8){
							itemRgb=new Rgb(temp[8]);
							if(fields>11){
								blockCount=Integer.parseInt(temp[9]);
								String[] starts=temp[11].split(",");
								String[] sizes=temp[10].split(",");
								blockSizes=new long[blockCount];
								blockStarts=new long[blockCount];
								for(int i=0;i<blockCount;i++){
									blockSizes[i]=Long.parseLong(sizes[i]);
									blockStarts[i]=Long.parseLong(starts[i]);
								}
							}
						}
					}
				}
			}
		}
	}
}