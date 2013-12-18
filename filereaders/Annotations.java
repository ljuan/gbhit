package filereaders;

import java.io.Serializable;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Class for annotations stored in the configure file.
 */

class Annotations implements Serializable{
	private String ID;
	private String Path=null;
	private String Type;
	private String Mode;
	private String Group=null;
	private String Check=null;
	private HashMap<String,String> Paths;
	private HashMap<String,Object> Parameter;
	private HashMap<String,String> ParameterType;
	Annotations(String ID, String Path, String Type, String Mode, String Group){
		this.ID=ID;
		this.Path=Path;
		this.Type=Type;
		this.Mode=Mode;
		this.Group=Group;
		this.Parameter=new HashMap<String,Object>();
		this.ParameterType=new HashMap<String,String>();
	}
	Annotations(String ID, String[][] Paths, String Type, String Mode, String Group){
		this.ID=ID;
		this.Paths=new HashMap<String,String>();
		for(int i=0;i<Paths.length;i++)
			this.Paths.put(Paths[i][0], Paths[i][1]);
		this.Type=Type;
		this.Mode=Mode;
		this.Group=Group;
		this.Parameter=new HashMap<String,Object>();
		this.ParameterType=new HashMap<String,String>();
	}
	void set_Mode(String Mode){
		this.Mode=Mode;
		if(Mode.equals(Consts.MODE_HIDE))
			Parameter.clear();
	}
	void set_Check(String Check){
		this.Check=Check;
	}
	void set_Parameters(String params, String values){
		String[] ParameterList=params.split(";");
		String[] ValueList=values.split(";");
		for(int i=0;i<ParameterList.length;i++){
			if(Parameter.containsKey(ParameterList[i])){
				if(ParameterType.get(ParameterList[i]).equals(Consts.PARAMETER_TYPE_CHECKBOX)){
					String[] value_temp=ValueList[i].split(":");
					HashMap<String,Boolean> options_param=(HashMap<String,Boolean>)(Parameter.get(ParameterList[i]));
					String[] optionList=new String[options_param.size()];
					options_param.keySet().toArray(optionList);
					for(int j=0;j<optionList.length;j++)
						options_param.put(optionList[j], false);
					for(int j=0;j<value_temp.length;j++)
						if(options_param.containsKey(value_temp[j]))
							options_param.put(value_temp[j], true);
					Parameter.put(ParameterList[i], options_param);
				}
				else if(ParameterType.get(ParameterList[i]).equals(Consts.PARAMETER_TYPE_STRING))
					Parameter.put(ParameterList[i],ValueList[i]);
				else if(ParameterType.get(ParameterList[i]).equals(Consts.PARAMETER_TYPE_SELECTION))
					((String[])(Parameter.get(ParameterList[i])))[0]=ValueList[i];
				else if(ParameterType.get(ParameterList[i]).equals(Consts.PARAMETER_TYPE_VCFSAMPLE))
					((VcfSample)(Parameter.get(ParameterList[i]))).setSamples(ValueList[i]);
			}
		}
	}
	void set_Parameter(String param, Object value){
		if(Parameter.containsKey(param)&&ParameterType.get(param).equals(Consts.PARAMETER_TYPE_INVISABLE))
			Parameter.put(param, value);
	}
	void initialize_Parameter(String Param, Object Values, String Type){
		ParameterType.put(Param, Type);
		Parameter.put(Param, Values);
	}
	Element write_anno2parameter(Document doc){
		Element Parameters = doc.createElement(Consts.XML_TAG_PARAMETERS);
		Parameters.setAttribute(Consts.XML_TAG_ID, this.ID);
		String[] ParameterList=get_Parameters();
		for(int i=0;i<ParameterList.length;i++){
			if(!ParameterType.get(ParameterList[i]).equals(Consts.PARAMETER_TYPE_INVISABLE)){
				Element Param=doc.createElement(Consts.XML_TAG_PARAMETER);
				Param.setAttribute(Consts.XML_TAG_ID, ParameterList[i]);
				Param.setAttribute(Consts.XML_TAG_TYPE, ParameterType.get(ParameterList[i]));
				if(ParameterType.get(ParameterList[i]).equals(Consts.PARAMETER_TYPE_CHECKBOX)){
					HashMap<String,Boolean> options_param=(HashMap<String,Boolean>)(Parameter.get(ParameterList[i]));
					String[] optionList=new String[options_param.size()];
					options_param.keySet().toArray(optionList);
					StringBuffer options_temp=new StringBuffer();
					for(int j=0;j<optionList.length;j++){
						if(options_param.get(optionList[j]))
							options_temp.append(optionList[j]+":1");
						else
							options_temp.append(optionList[j]+":0");
						if(j<optionList.length-1)
							options_temp.append(";");
					}
					XmlWriter.append_text_element(doc, Param, Consts.XML_TAG_OPTIONS, options_temp.toString());
				}
				else if(ParameterType.get(ParameterList[i]).equals(Consts.PARAMETER_TYPE_SELECTION)){
					String[] optionList=(String[])(Parameter.get(ParameterList[i]));
					StringBuffer options_temp=new StringBuffer();
					for(int j=1;j<optionList.length;j++){
						options_temp.append(optionList[j]);
						if(j<optionList.length-1)
							options_temp.append(";");
					}
					XmlWriter.append_text_element(doc, Param, Consts.XML_TAG_OPTIONS, options_temp.toString());
					Param.appendChild(doc.createTextNode(optionList[0]));
				}
				else if(ParameterType.get(ParameterList[i]).equals(Consts.PARAMETER_TYPE_STRING))
					Param.appendChild(doc.createTextNode((String)Parameter.get(ParameterList[i])));
				else if(ParameterType.get(ParameterList[i]).equals(Consts.PARAMETER_TYPE_VCFSAMPLE))
					((VcfSample)(Parameter.get(ParameterList[i]))).appendXMLcontent(doc, Param);
				Parameters.appendChild(Param);
			}
			doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(Parameters);
		}
		return Parameters;
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
	boolean has_visable_Parameter(){
		if(ParameterType.containsValue(Consts.PARAMETER_TYPE_CHECKBOX)||ParameterType.containsValue(Consts.PARAMETER_TYPE_SELECTION)
		||ParameterType.containsValue(Consts.PARAMETER_TYPE_STRING)||ParameterType.containsValue(Consts.PARAMETER_TYPE_VCFSAMPLE))
			return true;
		else
			return false;
	}
	boolean has_Parameter(String param){
		return Parameter.containsKey(param);
	}
	String get_ID(){
		return ID;
	}
	String get_Group(){
		return Group;
	}
	String get_Path(){
		return Path;
	}
	String get_Path(String key){
		if(Path != null || Paths.isEmpty())
			return Path;
		else if(Paths.containsKey(key))
			return Paths.get(key);
		else 
			return Paths.values().iterator().next();
	}
	String get_Type(){
		return Type;
	}
	String get_Mode(){
		return Mode;
	}
	String get_Check(){
		return Check;
	}
}
