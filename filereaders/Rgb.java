package filereaders;

import java.util.HashMap;

public class Rgb{
	private int Red;
	private int Green;
	private int Blue;
	private static HashMap<String,int[]> ColorScheme;
	public static final String COLOR_LIGHTBLUE="Lightblue";
	public static final String COLOR_STEELBLUE="Steelblue";
	public static final String COLOR_BLACK="Black";
	public static final String COLOR_LIGHTGREEN="Lightgreen";
	public static final String COLOR_GREEN="Green";
	public static final String COLOR_PINK="Pink";
	public static final String COLOR_RED="Red";
	public static final String COLOR_TAN="Tan";
	public static final String COLOR_ORANGE="Orange";
	public static final String COLOR_VIOLET="Violet";
	public static final String COLOR_PURPLE="Purple";
	public static final String COLOR_GRAY="Gray";
	public static final String COLOR_LIGHTYELLOW="Lightyellow";
	public static final String[] COLOR_LIST={COLOR_RED,COLOR_STEELBLUE,COLOR_GREEN,COLOR_PURPLE,COLOR_ORANGE,COLOR_PINK,COLOR_LIGHTBLUE,COLOR_LIGHTGREEN,
											COLOR_VIOLET,COLOR_TAN,COLOR_LIGHTYELLOW};
	static {
		ColorScheme=new HashMap<String,int[]>();
		ColorScheme.put(COLOR_LIGHTBLUE, new int[] {166,206,227});
		ColorScheme.put(COLOR_STEELBLUE, new int[] {31,120,180});
		ColorScheme.put(COLOR_LIGHTGREEN, new int[] {178,223,138});
		ColorScheme.put(COLOR_GREEN, new int[] {51,160,44});
		ColorScheme.put(COLOR_PINK, new int[] {251,154,153});
		ColorScheme.put(COLOR_RED, new int[] {227,26,28});
		ColorScheme.put(COLOR_TAN, new int[] {253,191,111});
		ColorScheme.put(COLOR_ORANGE, new int[] {255,127,0});
		ColorScheme.put(COLOR_VIOLET, new int[] {202,178,214});
		ColorScheme.put(COLOR_PURPLE, new int[] {106,61,154});
		ColorScheme.put(COLOR_LIGHTYELLOW, new int[] {255,255,153});
		ColorScheme.put(COLOR_BLACK, new int[] {0,0,0});
		ColorScheme.put(COLOR_GRAY, new int[] {128,128,128});
	}
	public Rgb(){
		init(0,0,0);
	}
	public Rgb(String rgb){
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
		else if(ColorScheme.containsKey(rgb)){
			int[] scheme=ColorScheme.get("rgb");
			init(scheme[0],scheme[1],scheme[2]);
		}
		else
			init(0,0,0);
	}
	public Rgb(int score){
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
	static String[] getColorList(){
		String[] colorlist=new String[ColorScheme.keySet().size()];
		ColorScheme.keySet().toArray(colorlist);
		return colorlist;
	}
	public static String getColor(int num){
		return COLOR_LIST[num%COLOR_LIST.length];
	}
	public String ToString(){
		StringBuffer rgb=new StringBuffer();
		rgb.append(Red);rgb.append(',');rgb.append(Green);rgb.append(',');rgb.append(Blue);
		return rgb.toString();
	}
}