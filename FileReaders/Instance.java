package FileReaders;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Instance implements Consts{
	String Assembly;
	String Chr=null;
	long[] Coordinate;
	Hashtable<String, Annotations> Annos;
	Hashtable<String, Annotations> Externals;
	FastaReader rr;
	CfgReader Config;
	double bpp;
	int window_width;
	public Instance (){
		initialize("hg19");
	}
	public Instance (String Assembly){
		initialize(Assembly);
	}
	void initialize(String Assembly){
		Config=new CfgReader(new File(CONFIGURE));
		this.Assembly=Assembly;
		Annotations[] Annos=Config.getAnnotations(Assembly);
		this.Annos=new Hashtable<String, Annotations>(Annos.length,1);
		for(int i=0;i<Annos.length;i++){
			if(Annos[i].get_Type().equals(FORMAT_REF))
				rr=new FastaReader(Annos[i].get_Path());
			this.Annos.put(Annos[i].get_ID(), Annos[i]);
		}
		Externals=new Hashtable<String, Annotations>();
		bpp=1;
	}
	public void refresh(String chr,long start,long end){
		
	}
	public String update(String chr,long start,long end,int window_width){
		Document doc=XmlWriter.init(DATA_ROOT);
		int chrid=check_chromosome(chr);
		if(chrid>=0){
			Chr=chr;
			Coordinate=check_coordinate(chrid,start,end);
			this.window_width=window_width;
			bpp=(double)(Coordinate[1]-Coordinate[0])/(double)window_width;
			Enumeration<Annotations> annos_enum=Annos.elements();
			
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(DATA_ROOT).item(0), XML_TAG_CHROMOSOME, Chr);
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(DATA_ROOT).item(0), XML_TAG_START, String.valueOf(Coordinate[0]));
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(DATA_ROOT).item(0), XML_TAG_END, String.valueOf(Coordinate[1]));
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(DATA_ROOT).item(0), XML_TAG_LENGTH, String.valueOf(rr.fasta_index[chrid][0]));

			for(int i=0;i<Annos.size();i++){
				Annotations anno_temp=annos_enum.nextElement();
				append_track(anno_temp,doc,anno_temp.get_Mode());
			}
			Enumeration<Annotations> externals_enum=Externals.elements();
			for(int i=0;i<Externals.size();i++){
				Annotations external_temp=externals_enum.nextElement();
				append_track(external_temp,doc,external_temp.get_Mode());
			}	
		}
		else
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(DATA_ROOT).item(0), XML_TAG_ERROR,"Invalid Chromosome name");
		return XmlWriter.xml2string(doc);
	}
	public String add_Tracks(String[] tracks,String[] modes){
		Document doc=XmlWriter.init(DATA_ROOT);
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
			set_mode(tracks[i],MODE_HIDE);
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
					Externals.put(tracks[i], new Annotations(tracks[i],links_table,types[i],modes[i]));
				}
				else{
					Externals.put(tracks[i], new Annotations(tracks[i],links[i],types[i],modes[i]));
				}
			}
	}
	public void remove_Externals(String[] tracks){
		for(int i=0;i<tracks.length;i++)
			if(Externals.containsKey(tracks[i]))
				Externals.remove(tracks[i]);
	}
	public String get_Assemblies(){
		Document doc=XmlWriter.init(META_ROOT);
		Config.write_metalist(doc,Config.getAssemblies(),"AssemblyList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Annotations(){
		String[] anno_names=new String[Annos.size()];
		Enumeration<Annotations> annos_enum=Annos.elements();
		for(int i=0;i<Annos.size();i++){
			Annotations temp=annos_enum.nextElement();
			anno_names[i]=temp.get_ID()+":"+temp.get_Mode()+":"+temp.get_Type();
		}
		Document doc=XmlWriter.init(META_ROOT);
		Config.write_metalist(doc,anno_names, "AnnotationList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Parameters(String[] tracks){
		Document doc=XmlWriter.init(DATA_ROOT);
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
	void append_track(Annotations track, Document doc,String mode) {
		if(!mode.equals(MODE_HIDE)){
			Element ele_temp=null;
			String type_temp=track.get_Type();
			if(type_temp.equals(FORMAT_BEDGZ)){
				BedReaderTabix brt=new BedReaderTabix(track.get_Path(Chr));
				ele_temp=brt.write_bed2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],bpp);
			}
			else if(type_temp.equals(FORMAT_BED)){
				BedReader br=new BedReader(track.get_Path(Chr));
				ele_temp=br.write_bed2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],bpp);
			}
			else if(type_temp.equals(FORMAT_BIGBED)){
				BigBedReader bbr;
				try{
					bbr=new BigBedReader(track.get_Path(Chr));
					ele_temp=bbr.write_bed2elements(doc, track.get_ID(), Chr, Coordinate[0], Coordinate[1], bpp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(FORMAT_BEDGRAPH)){
				BedGraphReader bgr;
				try{
					bgr=new BedGraphReader(track.get_Path(Chr));
					ele_temp=bgr.write_bedGraph2Values(doc, track.get_ID(), Chr, (int) Coordinate[0], (int) Coordinate[1], window_width, 2);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(FORMAT_BIGWIG)){
				WiggleReader wr;
				try{
					wr=new WiggleReader(track.get_Path(Chr),true);
					ele_temp=wr.write_wiggle2Values(doc, track.get_ID(), Chr, (int) Coordinate[0], (int) Coordinate[1], window_width, 2);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(FORMAT_WIG)){
				WiggleReader wr2;
				try{
					wr2=new WiggleReader(track.get_Path(Chr),false);
					ele_temp=wr2.write_wiggle2Values(doc, track.get_ID(), Chr, (int) Coordinate[0], (int) Coordinate[1], window_width, 2);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(FORMAT_GFF)){
				GFFReader gr;
				try {
					gr = new GFFReader(track.get_Path(Chr));
					ele_temp=gr.write_gff2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],"gene_id");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(FORMAT_GTF)){
				GTFReader gr;
				try {
					gr = new GTFReader(track.get_Path(Chr));
					ele_temp=gr.write_gtf2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(FORMAT_GVF)){
				GVFReader gr;
				try {
					gr = new GVFReader(track.get_Path(Chr));
					ele_temp=gr.write_gvf2variants(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if (type_temp.equals(FORMAT_VCF)&&Coordinate[1]-Coordinate[0]<10000000){
				VcfReader vr=new VcfReader(track,Chr);
				ele_temp=vr.write_vcf2variants(doc,track.get_ID(),mode,bpp,Chr,Coordinate[0],Coordinate[1]);
			}
			else if (type_temp.equals(FORMAT_BAM)){
				try {
					BAMReader br2=new BAMReader(track.get_Path(Chr));
					ele_temp=br2.readBAM(doc,Chr,(int)Coordinate[0],(int)Coordinate[1],window_width,2, mode,track.get_ID());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			else if (type_temp.equals(FORMAT_REF)&&bpp<0.5){
				rr.write_sequence(doc, Chr, Coordinate[0], Coordinate[1], track.get_ID());
			}
			else if (type_temp.equals(FORMAT_FASTA)&&bpp<0.5){
				FastaReader fr=new FastaReader(track.get_Path());
				ele_temp=fr.write_sequence(doc, Chr, Coordinate[0], Coordinate[1], track.get_ID());
			}
			else if (type_temp.equals(FORMAT_CYTO)){
				CytobandReader cbr=new CytobandReader(track.get_Path(Chr));
				ele_temp=cbr.write_cytobands(doc, Chr, track);
			}
			
			if(ele_temp!=null
					&&!ele_temp.getTagName().equals(XML_TAG_PARAMETERS)
					&&!type_temp.equals(FORMAT_CYTO)
					&&!type_temp.equals(FORMAT_REF))
				if(track.has_visable_Parameter())
					ele_temp.setAttribute(XML_TAG_IFP, TEXT_TRUE);
				else 
					ele_temp.setAttribute(XML_TAG_IFP, TEXT_FALSE);
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