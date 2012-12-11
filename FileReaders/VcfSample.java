package FileReaders;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class VcfSample implements Consts{
	HashMap<String,Boolean> Samples;
	String[] SampleNames;
	VcfSample(String[] SampleNames){
		this.SampleNames=SampleNames;
		HashMap<String,Boolean> Samples=new HashMap<String,Boolean>();
		if(SampleNames!=null)
			for(int i=0;i<SampleNames.length;i++)
				Samples.put(SampleNames[i], false);
	}
	void setSamples(String selected_sample){
		for(int i=0;i<SampleNames.length;i++)
			Samples.put(SampleNames[i], false);
		String[] selected_samples=selected_sample.split(":");
		for(int i=0;i<selected_samples.length;i++)
			if(Samples.containsKey(selected_samples[i]))
				Samples.put(selected_samples[i], true);
	}
	Element appendXMLcontent(Document doc, Element Param){
		StringBuffer options_temp=new StringBuffer();
		for(int j=0;j<SampleNames.length;j++){
			if(Samples.get(SampleNames[j]))
				options_temp.append(SampleNames[j]+":1");
			else
				options_temp.append(SampleNames[j]+":0");
			if(j<SampleNames.length-1)
				options_temp.append(";");
		}
		return XmlWriter.append_text_element(doc, Param, XML_TAG_OPTIONS, options_temp.toString());
	}
}