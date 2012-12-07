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
	public HashMap<String,Object> Parameter;
	public HashMap<String,String> CurrentSetting;
	/* null means no change for the parameter params[i] after initializing (do nothing)
	* "" means no change since the last effective parameter setting (do nothing)
	* "SOME CONTENT(STRING)" means the settings have been changed since the last effective parameter setting
	*/
	Annotations(String ID, String Path, String Type, String Mode){
		this.ID=ID;
		this.Path=Path;
		this.Type=Type;
		this.Mode=Mode;
		this.Parameter=new HashMap<String,Object>();
		this.CurrentSetting=new HashMap<String,String>();
	}
	Annotations(String ID, String[][] Paths, String Type, String Mode){
		this.ID=ID;
		this.Paths=new HashMap<String,String>();
		for(int i=0;i<Paths.length;i++)
			this.Paths.put(Paths[i][0], Paths[i][1]);
		this.Type=Type;
		this.Mode=Mode;
		this.Parameter=new HashMap<String,Object>();
		this.CurrentSetting=new HashMap<String,String>();
	}
	void set_Mode(String Mode){
		this.Mode=Mode;
	}
	String[] get_Parameters(){
		String[] ParametersList=new String[Parameter.size()];
		Parameter.keySet().toArray(ParametersList);
		return ParametersList;
	}
	Object get_Parameter(String Param){
		if(Parameter.containsKey(Param))
			return Parameter.get(Param);
		else
			return null;
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