package FileReaders;

import java.io.File;
import org.w3c.dom.*;

/*
 * Reading Configure file.
 * This class is extracted from XmlReader class for individual structure of configure file.
 * But many functions are deeply coupling with XmlReader.
 */

class CfgReader{
	Document doc;
	String[] Assemblies;
	CfgReader(File cfg){
		doc=new XmlReader(cfg).getDoc();
		assem_list();
	}
	void assem_list(){
		NodeList assemlist = doc.getElementsByTagName(Consts.XML_TAG_ASSEMBLY);
		Assemblies=new String[assemlist.getLength()];
		for(int i=0;i<assemlist.getLength();i++){
			Assemblies[i]=assemlist.item(i).getAttributes().item(0).getTextContent();
		}
	}
	String[] getAssemblies(){
		return Assemblies;
	}

	Annotations[] getAnnotations(String assemblyid){
		
		NodeList assemlist = doc.getElementsByTagName(Consts.XML_TAG_ASSEMBLY);
		int id=0;
		while(id<assemlist.getLength()){
			if (assemlist.item(id).getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent().equals(assemblyid))
				break;
			id++;
		}
		if(assemlist == null){
			System.out.println("Invalid Assembly");
			return null;
		}
		Element assembly=(Element)assemlist.item(id);
		NodeList annolist=assembly.getElementsByTagName(Consts.XML_TAG_ANNOTATION);
		Annotations[] Annos=new Annotations[annolist.getLength()];
		for(int i=0;i<annolist.getLength();i++){
			Element anno=(Element)annolist.item(i);
			String name=anno.getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent();
			String group=anno.getAttributes().getNamedItem(Consts.XML_TAG_GROUP).getTextContent();
			String format=anno.getElementsByTagName(Consts.XML_TAG_FORMAT).item(0).getTextContent();
			String mode=anno.getElementsByTagName(Consts.XML_TAG_DEFAULT).item(0).getTextContent();
			NodeList pathlist=anno.getElementsByTagName(Consts.XML_TAG_PATH);
			if(pathlist.getLength() == 1){
				String path=pathlist.item(0).getTextContent();
				Annos[i]=new Annotations(name,path,format,mode,group);
			}
			else if(pathlist.getLength() > 1){
				String[][] paths=new String[pathlist.getLength()][2];
				for(int j=0;j<pathlist.getLength();j++){
					paths[j][0]=pathlist.item(j).getAttributes().getNamedItem(Consts.XML_TAG_KEY).getTextContent();
					paths[j][1]=pathlist.item(j).getTextContent();
				}
				Annos[i]=new Annotations(name,paths,format,mode,group);
			}
		}
		return Annos;
	}
	Element write_metalist(Document doc,String[] metalist,String metatype){
		StringBuffer temp=new StringBuffer();
		for(int i=0;i<metalist.length;i++){
			if(metalist[i]==null)
				continue;
			if(i>0)
				temp.append(",");
			temp.append(metalist[i]);
		}
		return XmlWriter.append_text_element(doc,doc.getElementsByTagName(Consts.META_ROOT).item(0),metatype,temp.toString());
	}
}