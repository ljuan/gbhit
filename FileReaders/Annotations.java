package FileReaders;

import java.util.HashMap;

/*
 * Class for annotations stored in the configure file.
 */

class Annotations implements Consts{
	private String ID;
	private String Path=null;
	private String Type;
	private String Mode;
	private HashMap<String,String> Paths;
	Annotations(String ID, String Path, String Type, String Mode){
		this.ID=ID;
		this.Path=Path;
		this.Type=Type;
		this.Mode=Mode;
	}
	Annotations(String ID, String[][] Paths, String Type, String Mode){
		this.ID=ID;
		this.Paths=new HashMap<String,String>();
		for(int i=0;i<Paths.length;i++)
			this.Paths.put(Paths[i][0], Paths[i][1]);
		this.Type=Type;
		this.Mode=Mode;
	}
	void set_Mode(String Mode){
		this.Mode=Mode;
	}
	String get_ID(){
		return ID;
	}
	String get_Path(){
		return Path;
	}
	String get_Path(String key){
		if(Path != null)
			return Path;
		else
			return Paths.get(key);
	}
	String get_Type(){
		return Type;
	}
	String get_Mode(){
		return Mode;
	}
}