package filereaders;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author Liran Juan
 * @author Chengwu Yan
 * 
 */
class VcfSample implements Serializable{
	private Map<String, BooleanIndex> Samples;
	private String[] SampleNames;
	private int[] selectedIndexes;
	private String[] selectedNames=null;
	private Family Pedigree=null;

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
		initPedigree(null);
	}

	public void setSamples(String selected_sample) {
		if (selected_sample == null)
			return;
		for (int i = 0; i < SampleNames.length; i++) {
			Samples.get(SampleNames[i]).selected = false;
		}
		String[] temp1_selectedNames = selected_sample.split(":");
		ArrayList<String> temp_selectedNames = new ArrayList<String>();
		for (int i = 0; i < temp1_selectedNames.length; i++)
			if (Samples.containsKey(temp1_selectedNames[i]))
				temp_selectedNames.add(temp1_selectedNames[i]);
		if (temp_selectedNames.size() == 0) {
			selectedNames = null;
			selectedIndexes = null;
			return;
		}
		selectedIndexes = new int[temp_selectedNames.size()];
		BooleanIndex bi = null;
		int i = 0;
		while(i<temp_selectedNames.size()){
			bi = Samples.get(temp_selectedNames.get(i));
			bi.selected = true;
			selectedIndexes[i++] = bi.index;
		}
		Arrays.sort(selectedIndexes);
		selectedNames = new String[selectedIndexes.length];
		for(i=0;i<selectedIndexes.length;i++){
			selectedNames[i] = SampleNames[selectedIndexes[i]];
		}
	}
	public void loadPedigree(String filepath){
		File ped = new File(filepath);
		ByteBufferChannel bbc = new ByteBufferChannel(ped, 0, ped.length());
		initPedigree(bbc.ToString(Consts.DEFAULT_ENCODE));
	}
	public void initPedigree(String ped){
		this.Pedigree = new Family(ped);
	}
	public void removePedigree(){
		this.Pedigree = null;
	}
	public Element write_family2pedigree(Document doc, String track){
		if(Pedigree!=null)
			return Pedigree.write_family2pedigree(doc, track);
		else
			return null;
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

		return XmlWriter.append_text_element(doc, Param, Consts.XML_TAG_OPTIONS,
				options_temp.toString());
	}

	public String[] getSampleNames() {
		return SampleNames;
	}

	public int[] getSelectedIndexes() {
		return selectedIndexes;
	}
	public boolean ifSelected(String SampleName){
		if(selectedNames!=null)
			for(int i=0;i<selectedNames.length;i++)
				if(SampleName.equals(selectedNames[i]))
					return true;
		return false;
	}
	public boolean ifExists(String SampleName){
		return Samples.containsKey(SampleName);
	}
	public boolean ifTrioAvailable(){
		if(Pedigree != null && selectedNames!=null && selectedNames.length == 1
				&& Pedigree.Members.containsKey(selectedNames[0]) 
				&& Pedigree.Members.get(selectedNames[0]).ifSample()
				&& Pedigree.Members.containsKey(Pedigree.Members.get(selectedNames[0]).Fid) 
				&& Pedigree.Members.get(Pedigree.Members.get(selectedNames[0]).Fid).ifSample()
				&& Pedigree.Members.containsKey(Pedigree.Members.get(selectedNames[0]).Mid) 
				&& Pedigree.Members.get(Pedigree.Members.get(selectedNames[0]).Mid).ifSample())
			return true;
		else
			return false;
	}

	public String[] getSelectedNames() {
		return selectedNames;
	}
	public String[] getParents() {
		if(ifTrioAvailable()){
			String[] parents = {Pedigree.Members.get(selectedNames[0]).Fid, Pedigree.Members.get(selectedNames[0]).Mid};
			return parents;
		}
		else
			return null;
	}

	/**
	 * Index of selectedName in selected names.
	 * 
	 * @param selectedName
	 * @return
	 */
	public int getSelectedIndex(String selectedName) {
		if (selectedName == null || selectedNames == null)
			return -1;

		int len = selectedNames.length;
		for (int index = 0; index < len; index++) {
			if (selectedName.equals(selectedNames[index])) {
				return index;
			}
		}
		return -1;
	}
	public int getIndex(String SampleName){
		if(ifExists(SampleName))
			return Samples.get(SampleName).index;
		return -1;
	}

	public int getSamplesNum() {
		return SampleNames == null ? 0 : SampleNames.length;
	}

	private class BooleanIndex implements Serializable {
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
	private class Family implements Serializable{
//		Map<String, FamilyMember> Roots;
		Map<String, FamilyMember> Members;
		Family(String ped){
//			this.Roots = new HashMap<String, FamilyMember>();
			this.Members = new HashMap<String, FamilyMember>();
			if(ped!=null){
				String[] temp = ped.split("[\n\r]+");
				for(int i=0;i<temp.length;i++)
					addFamilyMember(temp[i]);
			}
			for(int i=0;i<SampleNames.length;i++)
				if(!Members.containsKey(SampleNames[i]))
					addFamilyMember(SampleNames[i],0);
		}
		void addFamilyMember(String ped){
			String[] temp = ped.split("\\s+|,|;");
			FamilyMember fm = new FamilyMember(temp);
			Members.put(fm.getID(), fm);
		}
		void addFamilyMember(String id,int sex){
			FamilyMember fm = new FamilyMember("--",id,sex);
			Members.put(fm.getID(), fm);
		}
		Element write_family2pedigree(Document doc,String track){
			Element ped=doc.createElement(Consts.XML_TAG_PEDIGREE);
			ped.setAttribute(Consts.XML_TAG_ID, track);
			Iterator member = Members.values().iterator();
			while(member.hasNext())
				ped.appendChild(((FamilyMember)member.next()).toXml(doc));
			doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(ped);
			return ped;
		}
		
	}
	private class FamilyMember implements Serializable{
		String Family;
		String id;
		String Fid;
		String Mid;
		int Sex;
		int Affected;
		String Info = null;
		boolean IfSample = false;
		
//		String LeftMostOffspring;
//		String Spouse;
//		String RightBrother;
		FamilyMember(String[] ped){
			this.Family = ped[0];
			this.id = ped[1];
			this.Fid = ped[2];
			this.Mid = ped[3];
			if(ped[4].equals("1"))
				this.Sex = 1;
			else if(ped[4].equals("2"))
				this.Sex = 2;
			else
				this.Sex = 0;
			if(ped[5].equals("1"))
				this.Affected = 1;
			else if (ped[5].equals("2"))
				this.Affected = 2;
			else
				this.Affected = 0;
			if(ped.length>6){
				this.Info = "";
				for(int i=6;i<ped.length;i++)
					Info = Info+ped[i]+",";
			}
			if(Samples.containsKey(id))
				IfSample = true;
		}
		FamilyMember(String family, String id, int sex){
			this.Family = family;
			this.id = id;
			this.Fid = "0";
			this.Mid = "0";
			this.Sex = sex;
			this.Affected = 0;
			if(Samples.containsKey(id))
				IfSample = true;
		}
		
		public String getFamily(){
			return Family;
		}
		public int getSex(){
			return Sex;
		}
		public String getFather(){
			return Fid;
		}
		public String getMother(){
			return Mid;
		}
		public String getID(){
			return id;
		}
		public boolean ifSample(){
			return IfSample;
		}
		
		public Element toXml(Document doc){
			Element member = doc.createElement(Consts.XML_TAG_MEMBER);
			member.setAttribute(Consts.XML_TAG_ID, id);
			member.setAttribute(Consts.XML_TAG_IFS, String.valueOf(IfSample));
			XmlWriter.append_text_element(doc, member, Consts.XML_TAG_FAMILY, Family);
			XmlWriter.append_text_element(doc, member, Consts.XML_TAG_FATHER, Fid);
			XmlWriter.append_text_element(doc, member, Consts.XML_TAG_MOTHER, Mid);
			XmlWriter.append_text_element(doc, member, Consts.XML_TAG_SEX, String.valueOf(Sex));
			XmlWriter.append_text_element(doc, member, Consts.XML_TAG_AFFECTED, String.valueOf(Affected));
			if(Info!=null)
				XmlWriter.append_text_element(doc, member, Consts.XML_TAG_DESCRIPTION, Info);
			return member;
		}
	}
}
