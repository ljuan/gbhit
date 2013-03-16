package FileReaders;

import static FileReaders.Consts.DATA_ROOT;
import FileReaders.gff.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.lang3.SerializationUtils;

import edu.hit.mlg.individual.VariantAnalysis;

public class Instance {
	String Assembly;
	String Chr=null;
	long[] Coordinate;
	Hashtable<String, Annotations> Annos;
	Hashtable<String, Annotations> Externals;
	FastaReader rr;
	CfgReader Config;
	double bpp;
	int window_width;
	
	String PvarID=null;
	Annotations Pvar=null;
	Element Ele_var=null;
	Annotations Panno=null;
	Element[] Ele_anno=null;
	Annotations Pfanno=null;
	Element Ele_fanno=null;
	Hashtable<String,Annotations> Pclns=new Hashtable<String,Annotations>();
	
	public Instance (){
		initialize("hg19");
	}
	public Instance (String Assembly){
		initialize(Assembly);
	}
	void initialize(String Assembly){
		Config=new CfgReader(new File(Consts.CONFIGURE));
		this.Assembly=Assembly;
		Annotations[] Annos=Config.getAnnotations(Assembly);
		this.Annos=new Hashtable<String, Annotations>(Annos.length,1);
		for(int i=0;i<Annos.length;i++){
			if(Annos[i].get_Type().equals(Consts.FORMAT_REF)){
				try{
					rr=new FastaReader(Annos[i].get_Path());
				} catch(IOException e){
					e.printStackTrace();
				}
				
			}
			this.Annos.put(Annos[i].get_ID(), Annos[i]);
		}
		Externals=new Hashtable<String, Annotations>();
		bpp=1;
	}
	public void refresh(String chr,long start,long end){
		
	}
	public String update(String chr,long start,long end,int window_width){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		int chrid=check_chromosome(chr);
		if(chrid>=0){
			if(Chr!=null && Chr.equals(chr) && Coordinate[0]==start && Coordinate[1]==end)
				if(Annos.get("cytoBand").has_Parameter(Consts.CYTOBAND_PREVIOUS_CHR))
					Annos.get("cytoBand").set_Parameter(Consts.CYTOBAND_PREVIOUS_CHR, "chr0");
			Chr=chr;
			Coordinate=check_coordinate(chrid,start,end);
			this.window_width=window_width;
			bpp=(double)(Coordinate[1]-Coordinate[0])/(double)window_width;
			
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_CHROMOSOME, Chr);
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_START, String.valueOf(Coordinate[0]));
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_END, String.valueOf(Coordinate[1]));
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_LENGTH, String.valueOf(rr.fasta_index[chrid][0]));

			Enumeration<Annotations> annos_enum=Annos.elements();
			for(int i=0;i<Annos.size();i++){
				Annotations anno_temp=annos_enum.nextElement();
				append_track(anno_temp,doc,anno_temp.get_Mode());
			}
			Enumeration<Annotations> externals_enum=Externals.elements();
			for(int i=0;i<Externals.size();i++){
				Annotations external_temp=externals_enum.nextElement();
				append_track(external_temp,doc,external_temp.get_Mode());
			}	
			if(Pvar!=null){
				append_Ptrack(Pvar,doc,Pvar.get_Mode(),Consts.PTRACK_CLASS_VAR);
				if(Pfanno!=null)
					append_Ptrack(Pfanno,doc,Pfanno.get_Mode(),Consts.PTRACK_CLASS_FANNO);
				if(Panno!=null){
					append_Ptrack(Panno,doc,Panno.get_Mode(),Consts.PTRACK_CLASS_ANNO);
					Enumeration<Annotations> pclns_enum=Pclns.elements();
					for(int i=0;i<Pclns.size();i++){
						Annotations pclns_temp=pclns_enum.nextElement();
						append_Ptrack(pclns_temp,doc,pclns_temp.get_Mode(),Consts.PTRACK_CLASS_CLN);
					}
				}
			}
		}
		else
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_ERROR,"Invalid Chromosome name");
		return XmlWriter.xml2string(doc);
	}
	public String add_Tracks(String[] tracks,String[] modes){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		for(int i=0;i<tracks.length;i++){
			set_mode(tracks[i],modes[i]);
			if(Annos.containsKey(tracks[i]))
				append_track(Annos.get(tracks[i]),doc,Annos.get(tracks[i]).get_Mode());
			else if(Externals.containsKey(tracks[i]))
				append_track(Externals.get(tracks[i]),doc,Externals.get(tracks[i]).get_Mode());
		}
		return XmlWriter.xml2string(doc);
	}
	public void remove_Tracks(String[] tracks){
		for(int i=0;i<tracks.length;i++)
			set_mode(tracks[i],Consts.MODE_HIDE);
	}
	public void add_Externals(String[] tracks,String[] links,String[] types,String[] modes){
		for(int i=0;i<tracks.length;i++)
			if(!Externals.containsKey(tracks[i])){
				if(links[i].indexOf(";")>0){
					String[] links_temp=links[i].split(";");
					String[][] links_table=new String[links_temp.length][2];
					for(int j=0;j<links_temp.length;j++){
						int colon=links_temp[j].indexOf(":");
						links_table[j][0]=links_temp[j].substring(0, colon);
						links_table[j][1]=links_temp[j].substring(colon+1);
					}
					Externals.put(tracks[i], new Annotations(tracks[i],links_table,types[i],modes[i],Consts.GROUP_CLASS_USR));
				}
				else{
					Externals.put(tracks[i], new Annotations(tracks[i],links[i],types[i],modes[i],Consts.GROUP_CLASS_USR));
				}
			}
	}
	public void remove_Externals(String[] tracks){
		for(int i=0;i<tracks.length;i++)
			if(Externals.containsKey(tracks[i]))
				Externals.remove(tracks[i]);
	}
	public String add_Pvar(String track,String mode,String PvarID){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		if(Annos.containsKey(track)){
			if(PvarID.equals(track)
					||(Annos.get(track).has_Parameter(Consts.VCF_HEADER_SAMPLE)
							&&((VcfSample)(Annos.get(track).get_Parameter(Consts.VCF_HEADER_SAMPLE))).ifSelected(PvarID))){
				this.PvarID=PvarID;
				this.Pvar=SerializationUtils.clone(Annos.get(track));
				Pvar.set_Mode(mode);
				if (!PvarID.equals(track))
					Pvar.set_Parameters(Consts.VCF_HEADER_SAMPLE, PvarID);
				append_Ptrack(Pvar,doc,Pvar.get_Mode(),Consts.PTRACK_CLASS_VAR);
				if(Panno!=null){
					append_Ptrack(Panno,doc,Panno.get_Mode(),Consts.PTRACK_CLASS_ANNO);
					Enumeration<Annotations> pclns_enum=Pclns.elements();
					for(int i=0;i<Pclns.size();i++){
						Annotations pclns_temp=pclns_enum.nextElement();
						append_Ptrack(pclns_temp,doc,pclns_temp.get_Mode(),Consts.PTRACK_CLASS_CLN);
					}
				}
			}
		}
		else if(Externals.containsKey(track)){
			if(PvarID.equals(track)
					||(Externals.get(track).has_Parameter(Consts.VCF_HEADER_SAMPLE)
							&&((VcfSample)(Externals.get(track).get_Parameter(Consts.VCF_HEADER_SAMPLE))).ifSelected(PvarID))){
				this.PvarID=PvarID;
				this.Pvar=SerializationUtils.clone(Externals.get(track));
				Pvar.set_Mode(mode);
				if (!PvarID.equals(track))
					Pvar.set_Parameters(Consts.VCF_HEADER_SAMPLE, PvarID);
				append_Ptrack(Pvar,doc,Pvar.get_Mode(),Consts.PTRACK_CLASS_VAR);
				if(Panno!=null){
					append_Ptrack(Panno,doc,Panno.get_Mode(),Consts.PTRACK_CLASS_ANNO);
					Enumeration<Annotations> pclns_enum=Pclns.elements();
					for(int i=0;i<Pclns.size();i++){
						Annotations pclns_temp=pclns_enum.nextElement();
						append_Ptrack(pclns_temp,doc,pclns_temp.get_Mode(),Consts.PTRACK_CLASS_CLN);
					}
				}
			}
		}
		return XmlWriter.xml2string(doc);
	}
	public void remove_Pvar(){
		Pvar=null;
		PvarID=null;
		Ele_var=null;
	}
	public String add_Panno(String track,String mode){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		if(Annos.containsKey(track)&&Annos.get(track).get_Type().equals(Consts.FORMAT_ANNO)){
			Panno=SerializationUtils.clone(Annos.get(track));
			Panno.set_Mode(mode);
			append_Ptrack(Panno,doc,Panno.get_Mode(),Consts.PTRACK_CLASS_ANNO);
			Enumeration<Annotations> pclns_enum=Pclns.elements();
			for(int i=0;i<Pclns.size();i++){
				Annotations pclns_temp=pclns_enum.nextElement();
				append_Ptrack(pclns_temp,doc,pclns_temp.get_Mode(),Consts.PTRACK_CLASS_CLN);
			}
		}
		else if(Externals.containsKey(track)&&Externals.get(track).get_Type().equals(Consts.FORMAT_ANNO)){
			Panno=SerializationUtils.clone(Externals.get(track));
			Panno.set_Mode(mode);
			append_Ptrack(Panno,doc,Panno.get_Mode(),Consts.PTRACK_CLASS_ANNO);
			Enumeration<Annotations> pclns_enum=Pclns.elements();
			for(int i=0;i<Pclns.size();i++){
				Annotations pclns_temp=pclns_enum.nextElement();
				append_Ptrack(pclns_temp,doc,pclns_temp.get_Mode(),Consts.PTRACK_CLASS_CLN);
			}
		}
		return XmlWriter.xml2string(doc);
	}
	public void remove_Panno(){
		Panno=null;
		Ele_anno=null;
	}
	public String add_Pfanno(String track,String mode){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		if(Annos.containsKey(track)&&Annos.get(track).get_Type().equals(Consts.FORMAT_GRF)){
			Pfanno=SerializationUtils.clone(Annos.get(track));
			Pfanno.set_Mode(mode);
			append_Ptrack(Pfanno,doc,Pfanno.get_Mode(),Consts.PTRACK_CLASS_FANNO);
			if(Panno!=null&&Pvar!=null){
				append_Ptrack(Panno,doc,Panno.get_Mode(),Consts.PTRACK_CLASS_ANNO);
				Enumeration<Annotations> pclns_enum=Pclns.elements();
				for(int i=0;i<Pclns.size();i++){
					Annotations pclns_temp=pclns_enum.nextElement();
					append_Ptrack(pclns_temp,doc,pclns_temp.get_Mode(),Consts.PTRACK_CLASS_CLN);
				}
			}
		}
		else if(Externals.containsKey(track)&&Externals.get(track).get_Type().equals(Consts.FORMAT_GRF)){
			Pfanno=SerializationUtils.clone(Externals.get(track));
			Pfanno.set_Mode(mode);
			append_Ptrack(Pfanno,doc,Pfanno.get_Mode(),Consts.PTRACK_CLASS_FANNO);
			if(Panno!=null&&Pvar!=null){
				append_Ptrack(Panno,doc,Panno.get_Mode(),Consts.PTRACK_CLASS_ANNO);
				Enumeration<Annotations> pclns_enum=Pclns.elements();
				for(int i=0;i<Pclns.size();i++){
					Annotations pclns_temp=pclns_enum.nextElement();
					append_Ptrack(pclns_temp,doc,pclns_temp.get_Mode(),Consts.PTRACK_CLASS_CLN);
				}
			}
		}
		return XmlWriter.xml2string(doc);
	}
	public String remove_Pfanno(){
		Pfanno=null;
		Ele_fanno=null;
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		if(Panno!=null&&Pvar!=null){
			append_Ptrack(Panno,doc,Panno.get_Mode(),Consts.PTRACK_CLASS_ANNO);
			Enumeration<Annotations> pclns_enum=Pclns.elements();
			for(int i=0;i<Pclns.size();i++){
				Annotations pclns_temp=pclns_enum.nextElement();
				append_Ptrack(pclns_temp,doc,pclns_temp.get_Mode(),Consts.PTRACK_CLASS_CLN);
			}
		}
		return XmlWriter.xml2string(doc);
	}
	public String add_Pclns(String[] tracks,String[] modes){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		for(int i=0;i<tracks.length;i++)
			if(Annos.containsKey(tracks[i])&&Annos.get(tracks[i]).get_Type().equals(Consts.FORMAT_GDF)){
				Pclns.put(tracks[i],SerializationUtils.clone(Annos.get(tracks[i])));
				Pclns.get(tracks[i]).set_Mode(modes[i]);
				append_Ptrack(Pclns.get(tracks[i]),doc,Pclns.get(tracks[i]).get_Mode(),Consts.PTRACK_CLASS_CLN);
			}
			else if(Externals.containsKey(tracks[i])&&Externals.get(tracks[i]).get_Type().equals(Consts.FORMAT_GDF)){
				Pclns.put(tracks[i],SerializationUtils.clone(Externals.get(tracks[i])));
				Pclns.get(tracks[i]).set_Mode(modes[i]);
				append_Ptrack(Pclns.get(tracks[i]),doc,Pclns.get(tracks[i]).get_Mode(),Consts.PTRACK_CLASS_CLN);
			}
		return XmlWriter.xml2string(doc);
	}
	public void remove_Pclns(String[] tracks){
		for(int i=0;i<tracks.length;i++)
			if(Pclns.containsKey(tracks[i]))
				Pclns.remove(tracks[i]);
	}
	public String get_Detail(String trackname, String id,int start,int end){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		Annotations track=null;
		if(Annos.containsKey(trackname))
			track=Annos.get(trackname);
		else if(Externals.containsKey(trackname))
			track=Externals.get(trackname);
		if(track!=null){
			Element ele_temp=null;
			String type_temp=track.get_Type();
			String path_temp=track.get_Path(Chr);
			if(type_temp.equals(Consts.FORMAT_BEDGZ)){
				BedReaderTabix brt=new BedReaderTabix(path_temp);
				ele_temp=brt.get_detail(doc, track.get_ID(), id, Chr, (long)start, (long)end);
			}
			else if(type_temp.equals(Consts.FORMAT_ANNO)){
				BasicAnnosReader bar=new BasicAnnosReader(path_temp);
				ele_temp=bar.get_detail(doc, track.get_ID(), id, Chr, (long)start, (long)end);
			}
			else if(type_temp.equals(Consts.FORMAT_BED)){
				BedReader br=new BedReader(path_temp);
				try {
					ele_temp=br.get_detail(doc, track.get_ID(), id, Chr, start, end);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_BIGBED)){
				BigBedReader bbr;
				try{
					bbr=new BigBedReader(path_temp);
					ele_temp=bbr.get_detail(doc, track.get_ID(), id, Chr, start, end);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
/*			else if(type_temp.equals(Consts.FORMAT_GRF)){
				GRFReader gr;
				try {
					gr = new GRFReader(path_temp);
					ele_temp=gr.write_grf2elements(doc, track.get_ID(), Chr,(int)Coordinate[0],(int)Coordinate[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
/*			else if(type_temp.equals(Consts.FORMAT_GDF)){
				GFFReader gr;
				try {
					gr = new GFFReader(path_temp);
					ele_temp=gr.write_gff2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],"gene_id");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
*/			else if(type_temp.equals(Consts.FORMAT_GVF)){
				GVFReader gr;
				try {
					gr = new GVFReader(path_temp);
					ele_temp=gr.get_detail(doc, track.get_ID(), id, Chr, start, end);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if (type_temp.equals(Consts.FORMAT_VCF)){
				VcfReader vr=new VcfReader(track,Chr);
				ele_temp=vr.get_detail(doc, track.get_ID(), id, Chr, start, end);
			}
			else if (type_temp.equals(Consts.FORMAT_BAM)){
				try {
					BAMReader br2=new BAMReader(path_temp);
					ele_temp=br2.get_detail(doc, track.get_ID(), id, Chr, start, end);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		return XmlWriter.xml2string(doc);
	}
	public String get_Geneinfo(String gene){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		Genes.gene_Info(doc, gene);
		return XmlWriter.xml2string(doc);
	}
	public String find_Gene(String prefix){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		Genes.find_Gene(doc, prefix);
		return XmlWriter.xml2string(doc);
	}
	public String get_OverlapGenes(String chr,int start,int end){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		Genes.overlap_Genes(doc, chr,start,end);
		return XmlWriter.xml2string(doc);
	}
	public String get_Assemblies(){
		Document doc=XmlWriter.init(Consts.META_ROOT);
		Config.write_metalist(doc,Config.getAssemblies(),"AssemblyList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Annotations(){
		String[] anno_names=new String[Annos.size()];
		Enumeration<Annotations> annos_enum=Annos.elements();
		for(int i=0;i<Annos.size();i++){
			Annotations temp=annos_enum.nextElement();
			anno_names[i]=temp.get_Group()+":"+temp.get_ID()+":"+temp.get_Mode()+":"+temp.get_Type();
		}
		Document doc=XmlWriter.init(Consts.META_ROOT);
		Config.write_metalist(doc,anno_names, "AnnotationList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Parameters(String[] tracks){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		for(int i=0;i<tracks.length;i++)
			if(Annos.containsKey(tracks[i]))
				Annos.get(tracks[i]).write_anno2parameter(doc);
			else if(Externals.containsKey(tracks[i]))
				Externals.get(tracks[i]).write_anno2parameter(doc);
		return XmlWriter.xml2string(doc);
	}
	public void set_Params(String[] tracks,String[] params,String[] values){
		for(int i=0;i<tracks.length;i++){
			if(Annos.containsKey(tracks[i]))
				Annos.get(tracks[i]).set_Parameters(params[i], values[i]);
			else if(Externals.containsKey(tracks[i]))
				Externals.get(tracks[i]).set_Parameters(params[i], values[i]);
		}
	}
	void set_mode(String track,String mode){
		if(Annos.containsKey(track))
			Annos.get(track).set_Mode(mode);
		else if(Externals.containsKey(track))
			Externals.get(track).set_Mode(mode);
	}
	void append_Ptrack(Annotations track,Document doc,String mode,int Class) {
		String type_temp=track.get_Type();
		if (Coordinate[1]-Coordinate[0]>1000000)
			return;
		if(Pvar!=null&&track.get_ID().equals(Pvar.get_ID())&&type_temp.equals(Consts.FORMAT_VCF)&&Class==Consts.PTRACK_CLASS_VAR){
			if(track.get_ID().equals(PvarID)){
				VcfReader vr=new VcfReader(track,Chr);
				vr.changeBppLimit(Consts.LIMIT_BPP);
				Ele_var=vr.write_vcf2variants(doc,"_"+track.get_ID(),mode,bpp,Chr,Coordinate[0],Coordinate[1]);
			}
			else {
				VcfReader vr=new VcfReader(track,Chr);
				vr.changeBppLimit(Consts.LIMIT_BPP);
				Ele_var=vr.write_vcf2variants(doc,"_"+track.get_ID(),mode,bpp,Chr,Coordinate[0],Coordinate[1]);
			}
		}
		else if(Pvar!=null&&track.get_ID().equals(Pvar.get_ID())&&type_temp.equals(Consts.FORMAT_GVF)&&Class==Consts.PTRACK_CLASS_VAR){
			GVFReader gr;
			try {
				gr = new GVFReader(Pvar.get_Path(Chr));
				Ele_var=gr.write_gvf2variants(doc, "_"+track.get_ID(), Chr,Coordinate[0],Coordinate[1]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(Panno!=null&&track.get_ID().equals(Panno.get_ID())&&type_temp.equals(Consts.FORMAT_ANNO)&&Class==Consts.PTRACK_CLASS_ANNO){
			try{
				BasicAnnosReader bar=new BasicAnnosReader(Panno.get_Path(Chr));
				Element ele_anno=bar.write_ba2elements(doc, "_"+track.get_ID(), Chr, Coordinate[0], Coordinate[1], bpp);
				VariantAnalysis ee = new VariantAnalysis(doc, rr, ele_anno,Ele_fanno, Ele_var, Consts.DBSNP_DATA, Chr, (int)Coordinate[0], (int)Coordinate[1]);
				Ele_anno=ee.deal();
				doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Ele_anno[0]);
				if(Ele_anno.length > 1)
					doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Ele_anno[1]);
				doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(ele_anno);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else if(Pfanno!=null&&track.get_ID().equals(Pfanno.get_ID())&&type_temp.equals(Consts.FORMAT_GRF)&&Class==Consts.PTRACK_CLASS_FANNO){
			GRFReader gr2;
			try {
				gr2 = new GRFReader(Pfanno.get_Path(Chr));
				Ele_fanno=gr2.write_grf2elements(doc, "_"+track.get_ID(), Chr,(int) Coordinate[0],(int) Coordinate[1]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(Pclns.containsKey(track.get_ID())&&type_temp.equals(Consts.FORMAT_GDF)&&Class==Consts.PTRACK_CLASS_CLN)
			append_track(track,doc,mode);
		else 
			append_track(track,doc,mode);
	}
	void append_track(Annotations track, Document doc,String mode) {
		String path_temp=track.get_Path(Chr);
		if(!mode.equals(Consts.MODE_HIDE) && path_temp!=null){
			Element ele_temp=null;
			String type_temp=track.get_Type();
			if(type_temp.equals(Consts.FORMAT_BEDGZ)){
				BedReaderTabix brt=new BedReaderTabix(path_temp);
				ele_temp=brt.write_bed2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],bpp);
			}
			else if(type_temp.equals(Consts.FORMAT_ANNO)){
				BasicAnnosReader bar=new BasicAnnosReader(path_temp);
				ele_temp=bar.write_ba2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],bpp);
			}
			else if(type_temp.equals(Consts.FORMAT_BED)){
				BedReader br=new BedReader(path_temp);
				try {
					ele_temp=br.write_bed2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],bpp);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_BIGBED)){
				BigBedReader bbr;
				try{
					bbr=new BigBedReader(path_temp);
					ele_temp=bbr.write_bed2elements(doc, track.get_ID(), Chr, Coordinate[0], Coordinate[1], bpp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_BEDGRAPH)){
				BedGraphReader bgr;
				try{
					bgr=new BedGraphReader(path_temp);
					ele_temp=bgr.write_bedGraph2Values(doc, track.get_ID(), Chr, (int) Coordinate[0], (int) Coordinate[1], window_width, 2);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_BIGWIG)){
				WiggleReader wr;
				try{
					wr=new WiggleReader(path_temp,true);
					ele_temp=wr.write_wiggle2Values(doc, track.get_ID(), Chr, (int) Coordinate[0], (int) Coordinate[1], window_width, 2);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_WIG)){
				WiggleReader wr2;
				try{
					wr2=new WiggleReader(path_temp,false);
					ele_temp=wr2.write_wiggle2Values(doc, track.get_ID(), Chr, (int) Coordinate[0], (int) Coordinate[1], window_width, 2);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_GRF)){
				GRFReader gr2;
				try {
					gr2 = new GRFReader(path_temp);
					ele_temp=gr2.write_grf2elements(doc, track.get_ID(), Chr,(int) Coordinate[0],(int) Coordinate[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_GDF)){
				GFFReader gr;
				try {
					gr = new GFFReader(path_temp);
					ele_temp=gr.write_gff2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],"gene_id");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_GFF)){
				GFFReader gr;
				try {
					gr = new GFFReader(path_temp);
					ele_temp=gr.write_gff2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],"gene_id");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_GTF)){
				GTFReader gr;
				try {
					gr = new GTFReader(path_temp);
					ele_temp=gr.write_gtf2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_GVF)){
				GVFReader gr;
				try {
					gr = new GVFReader(path_temp);
					ele_temp=gr.write_gvf2variants(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if (type_temp.equals(Consts.FORMAT_VCF)&&Coordinate[1]-Coordinate[0]<10000000){
				VcfReader vr=new VcfReader(track,Chr);
				ele_temp=vr.write_vcf2variants(doc,track.get_ID(),mode,bpp,Chr,Coordinate[0],Coordinate[1]);
			}
			else if (type_temp.equals(Consts.FORMAT_BAM)){
				try {
					BAMReader br2=new BAMReader(path_temp);
					ele_temp=br2.readBAM(doc,Chr,(int)Coordinate[0],(int)Coordinate[1],window_width,2, mode,track.get_ID());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			else if (type_temp.equals(Consts.FORMAT_REF)&&bpp<0.5){
				try{
					rr.write_sequence(doc, Chr, Coordinate[0], Coordinate[1], track.get_ID());
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			else if (type_temp.equals(Consts.FORMAT_FASTA)&&bpp<0.5){
				try{
					FastaReader fr=new FastaReader(path_temp);
					ele_temp=fr.write_sequence(doc, Chr, Coordinate[0], Coordinate[1], track.get_ID());
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			else if (type_temp.equals(Consts.FORMAT_CYTO)){
				CytobandReader cbr=new CytobandReader(path_temp);
				ele_temp=cbr.write_cytobands(doc, Chr, track);
			}
			
			if(ele_temp!=null
					&&!ele_temp.getTagName().equals(Consts.XML_TAG_PARAMETERS)
					&&!type_temp.equals(Consts.FORMAT_CYTO)
					&&!type_temp.equals(Consts.FORMAT_REF))
				if(track.has_visable_Parameter())
					ele_temp.setAttribute(Consts.XML_TAG_IFP, Consts.TEXT_TRUE);
				else 
					ele_temp.setAttribute(Consts.XML_TAG_IFP, Consts.TEXT_FALSE);
		}
	}

	int check_chromosome(String chr){
		if(rr.seq_name.containsKey(chr))
			return rr.seq_name.get(chr);
		else
			return -1;
	}
	long[] check_coordinate(int chr_info, long start, long end){
		long[] coordinate=new long[2];
		if (start<1)
			start=1;
		if (end>rr.fasta_index[chr_info][0])
			end=rr.fasta_index[chr_info][0];
		if(start<=end){
			coordinate[0]=start;
			coordinate[1]=end;
		}
		else{
			coordinate[1]=start;
			coordinate[0]=end;
		}
		return coordinate;
	}

}