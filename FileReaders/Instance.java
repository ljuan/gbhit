package FileReaders;

import java.io.File;
import java.util.*;




public class Instance implements Consts{
	String Assembly;
	String Chr;
	long[] Coordinate;
	Hashtable<String, Annotations> Annos;
	Hashtable<String, Annotations> Externals;
	FastaReader fr;
	CfgReader Config;
	double bpp;
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
			if(Annos[i].get_ID().equals("Reference"))
				fr=new FastaReader(Annos[i].get_Path());
			this.Annos.put(Annos[i].get_ID(), Annos[i]);
		}
		Externals=new Hashtable<String, Annotations>();
		bpp=1;
	}
	public void refresh(String chr,long start,long end){
		
	}
	public String update(String chr,long start,long end,int window_width){
		XmlWriter xw=new XmlWriter();
		int chrid=check_chromosome(chr);
		if(chrid>=0){
			Chr=chr;
			Coordinate=check_coordinate(chrid,start,end);
			bpp=(double)(Coordinate[1]-Coordinate[0])/(double)window_width;
			Enumeration<Annotations> annos_enum=Annos.elements();
			xw.write_basic(Chr, XML_TAG_CHROMOSOME);
			xw.write_basic(String.valueOf(Coordinate[0]), XML_TAG_START);
			xw.write_basic(String.valueOf(Coordinate[1]), XML_TAG_END);
			xw.write_basic(String.valueOf(fr.fasta_index[chrid][0]), XML_TAG_LENGTH);
			for(int i=0;i<Annos.size();i++){
				Annotations anno_temp=annos_enum.nextElement();
				append_track(anno_temp,xw,anno_temp.get_Mode(),bpp);
			}
			Enumeration<Annotations> externals_enum=Externals.elements();
			for(int i=0;i<Externals.size();i++){
				Annotations external_temp=externals_enum.nextElement();
				append_track(external_temp,xw,external_temp.get_Mode(),bpp);
			}	
		}
		else
			xw.write_basic("Invalid Chromosome name", XML_TAG_ERROR);
		return xw.xml2string();
	}
	public String add_Tracks(String[] tracks,String[] modes){
		XmlWriter xw=new XmlWriter();
		for(int i=0;i<tracks.length;i++){
			set_mode(tracks[i],modes[i]);
			if(Annos.containsKey(tracks[i]))
				append_track(Annos.get(tracks[i]),xw,Annos.get(tracks[i]).get_Mode(),bpp);
			else if(Externals.containsKey(tracks[i]))
				append_track(Externals.get(tracks[i]),xw,Externals.get(tracks[i]).get_Mode(),bpp);
		}
		return xw.xml2string();
	}
	public void remove_Tracks(String[] tracks){
		for(int i=0;i<tracks.length;i++)
			if(Annos.containsKey(tracks[i]))
				Annos.get(tracks[i]).set_Mode(MODE_HIDE);
			else if(Externals.containsKey(tracks[i]))
				Externals.get(tracks[i]).set_Mode(MODE_HIDE);
	}
	public void add_Externals(String[] tracks,String[] links,String[] types,String[] modes){
		for(int i=0;i<tracks.length;i++)
			if(!Externals.containsKey(tracks[i]))
				Externals.put(tracks[i], new Annotations(tracks[i],links[i],types[i],modes[i]));
	}
	public void remove_Externals(String[] tracks){
		for(int i=0;i<tracks.length;i++)
			if(Externals.containsKey(tracks[i]))
				Externals.remove(tracks[i]);
	}
	public String get_Assemblies(){
		XmlWriter xw=new XmlWriter(META_ROOT);
		xw.write_metalist(Config.getAssemblies(),"AssemblyList");
		return xw.xml2string();
	}
	public String get_Annotations(){
		String[] anno_names=new String[Annos.size()];
		Enumeration<Annotations> annos_enum=Annos.elements();
		for(int i=0;i<Annos.size();i++)
			anno_names[i]=annos_enum.nextElement().get_ID();
		XmlWriter xw=new XmlWriter(META_ROOT);
		xw.write_metalist(anno_names, "AnnotationList");
		return xw.xml2string();
	}
	
	void set_mode(String track,String mode){
		if(Annos.containsKey(track))
			Annos.get(track).set_Mode(mode);
		else if(Externals.containsKey(track))
			Externals.get(track).set_Mode(mode);
	}
	void append_track(Annotations track, XmlWriter xw,String mode,double bpp){
		if(!mode.equals(MODE_HIDE)){
			if(track.get_Type().equals(FORMAT_BEDGZ)){
				BedReaderTabix brt=new BedReaderTabix(track.get_Path());
				Bed[] temp=brt.extract_bed(Chr, Coordinate[0], Coordinate[1]);
				xw.write_bed2elements(temp, track.get_ID(),Coordinate[0],Coordinate[1]);
			}
			else if(track.get_Type().equals(FORMAT_BED)){
				BedReader br=new BedReader(track.get_Path());
				Bed[] temp=br.extract_bed(Chr, Coordinate[0], Coordinate[1]);
				xw.write_bed2elements(temp, track.get_ID(),Coordinate[0],Coordinate[1]);
			}
			else if (track.get_Type().equals(FORMAT_VCF)&&Coordinate[1]-Coordinate[0]<1000000){
				VcfReader vr=new VcfReader(track.get_Path());
				Vcf[] temp=vr.extract_vcf(Chr, Coordinate[0], Coordinate[1]);
				xw.write_vcf2variants(temp, track.get_ID(),mode,bpp);
			}
			else if (track.get_Type().equals(FORMAT_FASTA)&&bpp<0.5){
				String seq=fr.extract_seq(Chr, Coordinate[0], Coordinate[1]);
				xw.write_sequence(seq, track.get_ID());
			}
		}
	}

	int check_chromosome(String chr){
		if(fr.seq_name.containsKey(chr))
			return fr.seq_name.get(chr);
		else
			return -1;
	}
	long[] check_coordinate(int chr_info, long start, long end){
		long[] coordinate=new long[2];
		if (start<1)
			start=1;
		if (end>fr.fasta_index[chr_info][0])
			end=fr.fasta_index[chr_info][0];
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