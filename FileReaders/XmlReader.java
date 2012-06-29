package FileReaders;

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/*
 * Read and operate Xml File.
 * This class initially is for a particular purpose: configure file reader.
 * But We may have more data store in XML format in the future.
 * More well-packaged methods need to be developed 
 * to instead the raw/directly strategy in class CfgReader.
 */

class XmlReader implements Consts{
	Document doc;
	XmlReader(File xml){
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		FileInputStream xmlis=null;
		try {
			DocumentBuilder db=dbf.newDocumentBuilder();
			xmlis=new FileInputStream(xml);
			doc=db.parse(xmlis);
			doc.normalize();
			xmlis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	Document getDoc(){
		return doc;
	}
}