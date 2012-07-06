package FileReaders;

class Rgb{
	private int Red;
	private int Green;
	private int Blue;
	Rgb(){
		init(0,0,0);
	}
	Rgb(String rgb){
		if(rgb.matches("[0-9]+,[0-9]+,[0-9]+")){
			String[] temp=rgb.split(",");
			int r=Integer.parseInt(temp[0]);
			int g=Integer.parseInt(temp[1]);
			int b=Integer.parseInt(temp[2]);
			if(r>=0&&r<256&&g>=0&&g<256&&b>=0&&b<256)
				init(r,g,b);
			else
				init(0,0,0);
		}
		else
			init(0,0,0);
	}
	Rgb(int score){
		if(score<=166)
			init(200,200,200);
		else if(score>166&&score<=277)
			init(175,175,175);
		else if(score>277&&score<=388)
			init(150,150,150);
		else if(score>388&&score<=499)
			init(125,125,125);
		else if(score>499&&score<=611)
			init(100,100,100);
		else if(score>611&&score<=722)
			init(75,75,75);
		else if(score>722&&score<=833)
			init(50,50,50);
		else if(score>833&&score<=944)
			init(25,25,25);
		else
			init(0,0,0);
	}
	void init(int r,int g,int b){
		Red=r;
		Green=g;
		Blue=b;
	}
	String ToString(){
		StringBuffer rgb=new StringBuffer();
		rgb.append(Red);rgb.append(',');rgb.append(Green);rgb.append(',');rgb.append(Blue);
		return rgb.toString();
	}
}