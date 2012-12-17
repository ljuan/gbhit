package FileReaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Liran Juan
 * @author Chengwu Yan
 * 
 */
class VcfSample implements Consts {
	private Map<String, BooleanIndex> Samples;
	private String[] SampleNames;
	private int[] selectedIndexes;
	private String[] selectedNames;

	/**
	 * Add all samples
	 * 
	 * @param SampleNames
	 */
	public VcfSample(String[] SampleNames) {
		this.SampleNames = SampleNames;
		if (SampleNames != null) {
			Samples = new HashMap<String, BooleanIndex>();
			for (int i = 0; i < SampleNames.length; i++)
				Samples.put(SampleNames[i], new BooleanIndex(false, i));
		}
	}

	public void setSamples(String selected_sample) {
		if (selected_sample == null)
			return;
		for (int i = 0; i < SampleNames.length; i++) {
			Samples.get(SampleNames[i]).selected = false;
		}
		String[] temp1_selectedNames = selected_sample.split(":");
		ArrayList<String> temp_selectedNames=new ArrayList<String>();
		for (int i = 0;i < temp1_selectedNames.length; i++)
			if (Samples.containsKey(temp1_selectedNames[i]))
				temp_selectedNames.add(temp1_selectedNames[i]);
		if(temp_selectedNames.size()==0){
			selectedNames=null;
			selectedIndexes=null;
			return;
		}
		selectedNames = new String[temp_selectedNames.size()]; 
		temp_selectedNames.toArray(selectedNames);
		selectedIndexes = new int[selectedNames.length];
		BooleanIndex bi = null;
		for (int i = 0; i < selectedNames.length; i++) {
				bi = Samples.get(selectedNames[i]);
				bi.selected = true;
				selectedIndexes[i] = bi.index;
		}
	}

	Element appendXMLcontent(Document doc, Element Param) {
		StringBuilder options_temp = new StringBuilder();
		String cur = null;
		for (int j = 0; j < SampleNames.length - 1; j++) {
			cur = SampleNames[j];
			options_temp.append(cur);
			options_temp.append(Samples.get(cur).selected ? ":1" : ":0");
			options_temp.append(';');
		}
		if (SampleNames.length > 0) {
			cur = SampleNames[SampleNames.length - 1];
			options_temp.append(cur);
			options_temp.append(Samples.get(cur).selected ? ":1" : ":0");
		}

		return XmlWriter.append_text_element(doc, Param, XML_TAG_OPTIONS,
				options_temp.toString());
	}

	public String[] getSampleNames() {
		return SampleNames;
	}

	public int[] getSelectedIndexes() {
		return selectedIndexes;
	}

	public String[] getSelectedNames() {
		return selectedNames;
	}

	public int getSamplesNum() {
		return SampleNames == null ? 0 : SampleNames.length;
	}

	private class BooleanIndex {
		boolean selected;
		int index;

		/**
		 * 
		 * @param selected
		 *            Whether the Sample is selected
		 * @param index
		 *            Index of the Sample in the track file head
		 */
		BooleanIndex(boolean selected, int index) {
			this.selected = selected;
			this.index = index;
		}
	}
}