package FileReaders;

import java.io.File;
import org.w3c.dom.*;

/*
 * Reading Configure file.
 * This class is extracted from XmlReader class for individual structure of configure file.
 * But many functions are deeply coupling with XmlReader.
 */

class CfgReader implements Consts{
	Document doc;
	String[] Assemblies;
	CfgReader(File cfg){
		doc=new XmlReader(cfg).getDoc();
		assem_list();
	}
	void assem_list(){
		NodeList assemlist = doc.getElementsByTagName(XML_TAG_ASSEMBLY);
		Assemblies=new String[assemlist.getLength()];
		for(int i=0;i<assemlist.getLength();i++){
			Assemblies[i]=assemlist.item(i).getAttributes().item(0).getTextContent();
		}
	}
	String[] getAssemblies(){
		return Assemblies;
	}
	Annotations[] getAnnotations(String assemblyid){
		
		NodeList assemlist = doc.getElementsByTagName(XML_TAG_ASSEMBLY);
		int id=0;
		while(id<assemlist.getLength()){
			if (assemlist.item(id).getAttributes().getNamedItem(XML_TAG_ID).getTextContent().equals(assemblyid))
				break;
			id++;
		}
		if(assemlist == null){
			System.out.println("Invalid Assembly");
			return null;
		}
		Element assembly=(Element)assemlist.item(id);
		NodeList annolist=assembly.getElementsByTagName(XML_TAG_ANNOTATION);
		Annotations[] Annos=new Annotations[annolist.getLength()];
		for(int i=0;i<annolist.getLength();i++){
			Element anno=(Element)annolist.item(i);
			String name=anno.getAttributes().getNamedItem(XML_TAG_ID).getTextContent();
			String path=anno.getElementsByTagName(XML_TAG_PATH).item(0).getTextContent();
			String format=anno.getElementsByTagName(XML_TAG_FORMAT).item(0).getTextContent();
			String mode=anno.getElementsByTagName(XML_TAG_DEFAULT).item(0).getTextContent();
			Annos[i]=new Annotations(name,path,format,mode);
		}
		return Annos;
	}
	Element write_metalist(Document doc,String[] metalist,String metatype){
		StringBuffer temp=new StringBuffer();
		for(int i=0;i<metalist.length;i++){
			temp.append(metalist[i]);
			if(i<metalist.length-1)
				temp.append(",");
		}
		return XmlWriter.append_text_element(doc,doc.getElementsByTagName(META_ROOT).item(0),metatype,temp.toString());
	}
}