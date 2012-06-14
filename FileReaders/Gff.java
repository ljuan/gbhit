package FileReaders;

class Gff{
	String name;
	String source;
	String feature;
	long start;
	long end;
	int score;
	String strand;
	String frame;
	String group;
	
	Gff(String line){
		String[] temp=line.split("\t");
		name=temp[0];
		source=temp[1];
		feature=temp[2];
		start=Long.parseLong(temp[3]);
		end=Long.parseLong(temp[4]);
		score=Integer.parseInt(temp[5]);
		strand=temp[6];
		frame=temp[7];
		group=temp[8];
	}
}