package filereaders;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.*;

/*
 * Reading Configure file.
 * This class is extracted from XmlReader class for individual structure of configure file.
 * But many functions are deeply coupling with XmlReader.
 */

class CfgReader{
	static Document doc;
	static String[] Assemblies;
	static{
		doc=new XmlReader(Consts.CONFIGURE).getDoc();
		assem_list();
	}
	CfgReader(File cfg){
		
	}
	static void assem_list(){
		NodeList assemlist = doc.getElementsByTagName(Consts.XML_TAG_ASSEMBLY);
		Assemblies=new String[assemlist.getLength()];
		for(int i=0;i<assemlist.getLength();i++){
			Assemblies[i]=assemlist.item(i).getAttributes().item(0).getTextContent();
		}
	}
	static String[] getAssemblies(){
		return Assemblies;
	}
	public static Annotations getBasicRef(String assemblyid){
		NodeList assemlist = doc.getElementsByTagName(Consts.XML_TAG_ASSEMBLY);
		int id=0;
		while(id<assemlist.getLength()){
			if (assemlist.item(id).getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent().equals(assemblyid))
				break;
			id++;
		}
		if(assemlist == null)
			return null;
		Element assembly=(Element)assemlist.item(id);
		NodeList annolist=assembly.getElementsByTagName(Consts.XML_TAG_ANNOTATION);
		for(int i=0;i<annolist.getLength();i++){
			Element anno=(Element)annolist.item(i);
			String group=anno.getAttributes().getNamedItem(Consts.XML_TAG_GROUP).getTextContent();
			String format=anno.getElementsByTagName(Consts.XML_TAG_FORMAT).item(0).getTextContent();
			if(format.equals(Consts.FORMAT_REF)&&group.equals(Consts.GROUP_CLASS_BASIC)){
				String name=anno.getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent();
				String mode=anno.getElementsByTagName(Consts.XML_TAG_DEFAULT).item(0).getTextContent();
				NodeList pathlist=anno.getElementsByTagName(Consts.XML_TAG_PATH);
				if(pathlist.getLength() == 1){
					String path=pathlist.item(0).getTextContent();
					return new Annotations(name,path,format,mode,group);
				}
				else if(pathlist.getLength() > 1){
					String[][] paths=new String[pathlist.getLength()][2];
					for(int j=0;j<pathlist.getLength();j++){
						paths[j][0]=pathlist.item(j).getAttributes().getNamedItem(Consts.XML_TAG_KEY).getTextContent();
						paths[j][1]=pathlist.item(j).getTextContent();
					}
					return new Annotations(name,paths,format,mode,group);
				}
			}
			else
				continue;
		}
		return null;
	}
	public static Annotations getBasicGenes(String assemblyid){
		NodeList assemlist = doc.getElementsByTagName(Consts.XML_TAG_ASSEMBLY);
		int id=0;
		while(id<assemlist.getLength()){
			if (assemlist.item(id).getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent().equals(assemblyid))
				break;
			id++;
		}
		if(assemlist == null)
			return null;
		Element assembly=(Element)assemlist.item(id);
		NodeList annolist=assembly.getElementsByTagName(Consts.XML_TAG_ANNOTATION);
		for(int i=0;i<annolist.getLength();i++){
			Element anno=(Element)annolist.item(i);
			String group=anno.getAttributes().getNamedItem(Consts.XML_TAG_GROUP).getTextContent();
			String format=anno.getElementsByTagName(Consts.XML_TAG_FORMAT).item(0).getTextContent();
			if(format.equals(Consts.FORMAT_HGNC)&&group.equals(Consts.GROUP_CLASS_BASIC)){
				String name=anno.getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent();
				String mode=anno.getElementsByTagName(Consts.XML_TAG_DEFAULT).item(0).getTextContent();
				NodeList pathlist=anno.getElementsByTagName(Consts.XML_TAG_PATH);
				if(pathlist.getLength() == 1){
					String path=pathlist.item(0).getTextContent();
					return new Annotations(name,path,format,mode,group);
				}
				else if(pathlist.getLength() > 1){
					String[][] paths=new String[pathlist.getLength()][2];
					for(int j=0;j<pathlist.getLength();j++){
						paths[j][0]=pathlist.item(j).getAttributes().getNamedItem(Consts.XML_TAG_KEY).getTextContent();
						paths[j][1]=pathlist.item(j).getTextContent();
					}
					return new Annotations(name,paths,format,mode,group);
				}
			}
			else
				continue;
		}
		return null;
		
	}
	public static Annotations getBasicCyto(String assemblyid){
		NodeList assemlist = doc.getElementsByTagName(Consts.XML_TAG_ASSEMBLY);
		int id=0;
		while(id<assemlist.getLength()){
			if (assemlist.item(id).getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent().equals(assemblyid))
				break;
			id++;
		}
		if(assemlist == null)
			return null;
		Element assembly=(Element)assemlist.item(id);
		NodeList annolist=assembly.getElementsByTagName(Consts.XML_TAG_ANNOTATION);
		for(int i=0;i<annolist.getLength();i++){
			Element anno=(Element)annolist.item(i);
			String group=anno.getAttributes().getNamedItem(Consts.XML_TAG_GROUP).getTextContent();
			String format=anno.getElementsByTagName(Consts.XML_TAG_FORMAT).item(0).getTextContent();
			if(format.equals(Consts.FORMAT_CYTO)&&group.equals(Consts.GROUP_CLASS_BASIC)){
				String name=anno.getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent();
				String mode=anno.getElementsByTagName(Consts.XML_TAG_DEFAULT).item(0).getTextContent();
				NodeList pathlist=anno.getElementsByTagName(Consts.XML_TAG_PATH);
				if(pathlist.getLength() == 1){
					String path=pathlist.item(0).getTextContent();
					return new Annotations(name,path,format,mode,group);
				}
				else if(pathlist.getLength() > 1){
					String[][] paths=new String[pathlist.getLength()][2];
					for(int j=0;j<pathlist.getLength();j++){
						paths[j][0]=pathlist.item(j).getAttributes().getNamedItem(Consts.XML_TAG_KEY).getTextContent();
						paths[j][1]=pathlist.item(j).getTextContent();
					}
					return new Annotations(name,paths,format,mode,group);
				}
			}
			else
				continue;
		}
		return null;
	}
	public static Annotations getBasicSnp(String assemblyid){
		NodeList assemlist = doc.getElementsByTagName(Consts.XML_TAG_ASSEMBLY);
		int id=0;
		while(id<assemlist.getLength()){
			if (assemlist.item(id).getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent().equals(assemblyid))
				break;
			id++;
		}
		if(assemlist == null)
			return null;
		Element assembly=(Element)assemlist.item(id);
		NodeList annolist=assembly.getElementsByTagName(Consts.XML_TAG_ANNOTATION);
		for(int i=0;i<annolist.getLength();i++){
			Element anno=(Element)annolist.item(i);
			String group=anno.getAttributes().getNamedItem(Consts.XML_TAG_GROUP).getTextContent();
			String format=anno.getElementsByTagName(Consts.XML_TAG_FORMAT).item(0).getTextContent();
			if(format.equals(Consts.FORMAT_SNP)&&group.equals(Consts.GROUP_CLASS_BASIC)){
				String name=anno.getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent();
				String mode=anno.getElementsByTagName(Consts.XML_TAG_DEFAULT).item(0).getTextContent();
				NodeList pathlist=anno.getElementsByTagName(Consts.XML_TAG_PATH);
				if(pathlist.getLength() == 1){
					String path=pathlist.item(0).getTextContent();
					return new Annotations(name,path,format,mode,group);
				}
				else if(pathlist.getLength() > 1){
					String[][] paths=new String[pathlist.getLength()][2];
					for(int j=0;j<pathlist.getLength();j++){
						paths[j][0]=pathlist.item(j).getAttributes().getNamedItem(Consts.XML_TAG_KEY).getTextContent();
						paths[j][1]=pathlist.item(j).getTextContent();
					}
					return new Annotations(name,paths,format,mode,group);
				}
			}
			else
				continue;
		}
		return null;
	}
	public static Annotations getBasicAnnovar(String assemblyid){
		NodeList assemlist = doc.getElementsByTagName(Consts.XML_TAG_ASSEMBLY);
		int id=0;
		while(id<assemlist.getLength()){
			if (assemlist.item(id).getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent().equals(assemblyid))
				break;
			id++;
		}
		if(assemlist == null)
			return null;
		Element assembly=(Element)assemlist.item(id);
		NodeList annolist=assembly.getElementsByTagName(Consts.XML_TAG_ANNOTATION);
		for(int i=0;i<annolist.getLength();i++){
			Element anno=(Element)annolist.item(i);
			String group=anno.getAttributes().getNamedItem(Consts.XML_TAG_GROUP).getTextContent();
			String format=anno.getElementsByTagName(Consts.XML_TAG_FORMAT).item(0).getTextContent();
			if(format.equals(Consts.FORMAT_ANNOVAR)&&group.equals(Consts.GROUP_CLASS_BASIC)){
				String name=anno.getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent();
				String mode=anno.getElementsByTagName(Consts.XML_TAG_DEFAULT).item(0).getTextContent();
				NodeList pathlist=anno.getElementsByTagName(Consts.XML_TAG_PATH);
				if(pathlist.getLength() == 1){
					String path=pathlist.item(0).getTextContent();
					return new Annotations(name,path,format,mode,group);
				}
				else if(pathlist.getLength() > 1){
					String[][] paths=new String[pathlist.getLength()][2];
					for(int j=0;j<pathlist.getLength();j++){
						paths[j][0]=pathlist.item(j).getAttributes().getNamedItem(Consts.XML_TAG_KEY).getTextContent();
						paths[j][1]=pathlist.item(j).getTextContent();
					}
					return new Annotations(name,paths,format,mode,group);
				}
			}
			else
				continue;
		}
		return null;
	}
	static Annotations[] getAnnotations(String assemblyid){
		NodeList assemlist = doc.getElementsByTagName(Consts.XML_TAG_ASSEMBLY);
		int id=0;
		while(id<assemlist.getLength()){
			if (assemlist.item(id).getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent().equals(assemblyid))
				break;
			id++;
		}
		if(assemlist == null)
			return null;
		Element assembly=(Element)assemlist.item(id);
		NodeList annolist=assembly.getElementsByTagName(Consts.XML_TAG_ANNOTATION);
		ArrayList<Annotations> annoarray=new ArrayList<Annotations>();
		
		for(int i=0;i<annolist.getLength();i++){
			Element anno=(Element)annolist.item(i);
			String group=anno.getAttributes().getNamedItem(Consts.XML_TAG_GROUP).getTextContent();
			if(group.equals(Consts.GROUP_CLASS_BASIC))
				continue;
			String name=anno.getAttributes().getNamedItem(Consts.XML_TAG_ID).getTextContent();
			String format=anno.getElementsByTagName(Consts.XML_TAG_FORMAT).item(0).getTextContent();
			String mode=anno.getElementsByTagName(Consts.XML_TAG_DEFAULT).item(0).getTextContent();
			NodeList pathlist=anno.getElementsByTagName(Consts.XML_TAG_PATH);
			if(pathlist.getLength() == 1){
				String path=pathlist.item(0).getTextContent();
				annoarray.add(new Annotations(name,path,format,mode,group));
			}
			else if(pathlist.getLength() > 1){
				String[][] paths=new String[pathlist.getLength()][2];
				for(int j=0;j<pathlist.getLength();j++){
					paths[j][0]=pathlist.item(j).getAttributes().getNamedItem(Consts.XML_TAG_KEY).getTextContent();
					paths[j][1]=pathlist.item(j).getTextContent();
				}
				annoarray.add(new Annotations(name,paths,format,mode,group));
			}
		}
		Annotations[] Annos=new Annotations[annoarray.size()];
		annoarray.toArray(Annos);
		return Annos;
	}
	static Element write_metalist(Document doc,String[] metalist,String metatype){
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