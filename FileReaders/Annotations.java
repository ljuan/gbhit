package FileReaders;

/*
 * Class for annotations stored in the configure file.
 */

class Annotations implements Consts{
	private String ID;
	private String Path;
	private String Type;
	private boolean Request;
	private String Mode;
	Annotations(String ID, String Path, String Type, String Mode){
		this.ID=ID;
		this.Path=Path;
		this.Type=Type;
		this.Mode=Mode;
		if(Mode.equals(MODE_HIDE))
			Request=false;
		else
			Request=true;
	}
	void set_Mode(String Mode){
		this.Mode=Mode;
		if(Mode.equals(MODE_HIDE))
			Request=false;
		else
			Request=true;
	}
	boolean get_request(){
		return Request;
	}
	String get_ID(){
		return ID;
	}
	String get_Path(){
		return Path;
	}
	String get_Type(){
		return Type;
	}
	String get_Mode(){
		return Mode;
	}
}