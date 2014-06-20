package filereaders;

import static filereaders.Consts.DATA_ROOT;
import static filereaders.Consts.VCF_HEADER_SAMPLE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.apache.commons.lang3.SerializationUtils;

import filereaders.gff.*;
import filereaders.individual.GRFElementRegionComparison;
import filereaders.individual.GdfElementSelector;
import filereaders.individual.Individual;
import filereaders.individual.VariantAnalysis;


public class Instance {
	String Assembly;
	String Chr=null;
	long[] Coordinate;
	Hashtable<String, Annotations> Annos;
	Hashtable<String, Annotations> Externals;
	FastaReader rr;
	Annotations Ref;
	Annotations Cyto;
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
	IndividualStat is=null;
	String scoremeth = null;
	
	public Instance (){
		initialize("hg19");
	}
	public Instance (String Assembly){
		initialize(Assembly);
	}
	void initialize(String Assembly){
		this.Assembly=Assembly;
		this.Ref=CfgReader.getBasicRef(Assembly);
		this.Cyto=CfgReader.getBasicCyto(Assembly);
		try{
			rr=new FastaReader(Ref.get_Path());
		} catch(IOException e){
			e.printStackTrace();
		}
		Annotations[] Annos=CfgReader.getAnnotations(Assembly);
		this.Annos=new Hashtable<String, Annotations>(Annos.length,1);
		for(int i=0;i<Annos.length;i++){
			this.Annos.put(Annos[i].get_ID(), Annos[i]);
			if(Annos[i].get_Group().equals(Consts.GROUP_CLASS_PG))
				init_track(this.Annos.get(Annos[i].get_ID()));
		}
		Externals=new Hashtable<String, Annotations>();
		init_Pvar("1000genome_CEU","NA12716");
		bpp=1;
	}
	public String refresh(String chr,long start,long end,int window_width){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		int chrid=check_chromosome(chr);
		if(chrid>=0){
			Chr=chr;
			Coordinate=check_coordinate(chrid,start,end);
			this.window_width=(int) Math.round(window_width*((double)(Coordinate[1]-Coordinate[0])/(double)Math.abs(end-start)));
			bpp=(double)(Coordinate[1]-Coordinate[0])/(double)this.window_width;
			
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_CHROMOSOME, Chr);
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_START, String.valueOf(Coordinate[0]));
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_END, String.valueOf(Coordinate[1]));
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_LENGTH, String.valueOf(rr.fasta_index[chrid][0]));
			append_track(Ref,doc,Ref.get_Mode());
			append_track(Cyto,doc,Cyto.get_Mode());
		}
		else
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_ERROR,"Invalid Chromosome name");
		return XmlWriter.xml2string(doc);
	}
	public String update(String chr,long start,long end,int window_width){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		int chrid=check_chromosome(chr);
		if(chrid>=0){
			Chr=chr;
			Coordinate=check_coordinate(chrid,start,end);
			this.window_width=(int) Math.round(window_width*((double)(Coordinate[1]-Coordinate[0])/(double)Math.abs(end-start)));
			
			bpp=(double)(Coordinate[1]-Coordinate[0])/(double)this.window_width;
			
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_CHROMOSOME, Chr);
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_START, String.valueOf(Coordinate[0]));
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_END, String.valueOf(Coordinate[1]));
			XmlWriter.append_text_element(doc, doc.getElementsByTagName(Consts.DATA_ROOT).item(0), Consts.XML_TAG_LENGTH, String.valueOf(rr.fasta_index[chrid][0]));
			append_track(Ref,doc,Ref.get_Mode());
			append_track(Cyto,doc,Cyto.get_Mode());
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
		boolean validurl = true;
		String invalidlist= "";
		for(int i=0;i<tracks.length;i++)
			if(!Externals.containsKey(tracks[i])){
				if(links[i].indexOf(";")>0){
					String[] links_temp=links[i].split(";");
					String[][] links_table=new String[links_temp.length][2];
					for(int j=0;j<links_temp.length;j++){
						int colon=links_temp[j].indexOf(":");
						links_table[j][0]=links_temp[j].substring(0, colon);
						links_table[j][1]=links_temp[j].substring(colon+1);
						if(links_table[j][1].startsWith("http://") || links_table[j][1].startsWith("ftp://") || links_table[j][1].startsWith("https://")){
							if(!ifURLexists(links_table[j][1])){
								validurl = false;
								invalidlist+=links_table[j][1]+";";
							}
						}
					}
					Externals.put(tracks[i], new Annotations(tracks[i],links_table,types[i],modes[i],Consts.GROUP_CLASS_USR));
				}
				else{
					if(links[i].startsWith("http://") || links[i].startsWith("ftp://") || links[i].startsWith("https://")){
						if(!ifURLexists(links[i])){
							validurl = false;
							invalidlist+=links[i]+";";
						}
					}
					Externals.put(tracks[i], new Annotations(tracks[i],links[i],types[i],modes[i],Consts.GROUP_CLASS_USR));
				}
				if(types[i].equals(Consts.FORMAT_VCF))
					init_track(Externals.get(tracks[i])); 
				if(!validurl)
					((Annotations) Externals.get(tracks[i])).set_Check("The PGB cannot access the data file! The following file(s) may not be publicly accessible: "+invalidlist);
			}
	}
	public void add_ExIndividuals(String[] tracks,String[] links,String[] types,String[] modes){
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
					Externals.put(tracks[i], new Annotations(tracks[i],links_table,types[i],modes[i],Consts.GROUP_CLASS_EXI));
				}
				else{
					Externals.put(tracks[i], new Annotations(tracks[i],links[i],types[i],modes[i],Consts.GROUP_CLASS_EXI));
				}
				if(types[i].equals(Consts.FORMAT_VCF))
					init_track(Externals.get(tracks[i])); 
			}
	}
	public void remove_Externals(String[] tracks){
		for(int i=0;i<tracks.length;i++)
			if(Externals.containsKey(tracks[i])){
				String path = ((Annotations)Externals.get(tracks[i])).get_Path();
				if(path != null && !path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("ftp://")){
					File file = new File(path);
					if(file.exists())
						file.delete();
				}
				Externals.remove(tracks[i]);
			}
	}
	public void init_Pvar(String track,String PvarID){
		if(Annos.containsKey(track)){
			if(PvarID.equals(track)
					||(Annos.get(track).has_Parameter(Consts.VCF_HEADER_SAMPLE))){
				this.PvarID=PvarID;
				this.Pvar=SerializationUtils.clone(Annos.get(track));
				Pvar.set_Mode(Consts.MODE_PACK);
				if (!PvarID.equals(track))
					Pvar.set_Parameters(Consts.VCF_HEADER_SAMPLE, PvarID);
				if(Pfanno==null && Panno==null){
					if(Annos.containsKey("ensemblRegulation") && Annos.get("ensemblRegulation").get_Type().equals(Consts.FORMAT_GRF)){
						Pfanno=SerializationUtils.clone(Annos.get("ensemblRegulation"));
						Pfanno.set_Mode(Consts.MODE_PACK);
					}
					if(Annos.containsKey("refGene") && Annos.get("refGene").get_Type().equals(Consts.FORMAT_ANNO)){
						Panno=SerializationUtils.clone(Annos.get("refGene"));
						Panno.set_Mode(Consts.MODE_PACK);
					}
					if(Annos.containsKey("OMIM") && Annos.get("OMIM").get_Type().equals(Consts.FORMAT_GDF)){
						Pclns.put("OMIM",SerializationUtils.clone(Annos.get("OMIM")));
						Pclns.get("OMIM").set_Mode(Consts.MODE_DENSE);
					}
					if(Annos.containsKey("GwasCatalog") && Annos.get("GwasCatalog").get_Type().equals(Consts.FORMAT_GDF)){
						Pclns.put("GwasCatalog",SerializationUtils.clone(Annos.get("GwasCatalog")));
						Pclns.get("GwasCatalog").set_Mode(Consts.MODE_DENSE);
					}
				}
				init_IndividualStat();
			}
		}
	}
	private void init_IndividualStat(){
		if(Pvar!=null){
			if(Panno==null)
				is=new IndividualStat(new CytobandReader(Cyto.get_Path()).getCytobands(),get_BasicAnnos());
			else
				is=new IndividualStat(new CytobandReader(Cyto.get_Path()).getCytobands(),new Annotations[]{Panno});
			String isfp="";
			if (!PvarID.equals(Pvar.get_ID()))
				isfp=Pvar.get_ID()+"_"+PvarID+".stat.txt";
			else
				isfp=Pvar.get_ID()+".stat.txt";
			isfp=Pvar.get_Path("chr1").substring(0,Pvar.get_Path("chr1").lastIndexOf("/")+1)+isfp;
			File isf=new File(isfp);
			if(isf.exists()&&isf.isFile())
				is.load_Stat(isfp);
		}
	}
	public String add_Pvar(String track,String mode,String PvarID){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		String old_PvarID = this.PvarID;
		String old_Pvar = null;
		if(Pvar!=null)
			old_Pvar = Pvar.get_ID();
		if(Externals.containsKey(track)){
			if(PvarID.equals(track)
					||(Externals.get(track).has_Parameter(Consts.VCF_HEADER_SAMPLE)
							&&((VcfSample)(Externals.get(track).get_Parameter(Consts.VCF_HEADER_SAMPLE))).ifExists(PvarID))){
				this.PvarID=PvarID;
				this.Pvar=SerializationUtils.clone(Externals.get(track));
//				Pvar.set_Mode(mode);
				Pvar.set_Mode(Consts.MODE_PACK);
				if(Externals.get(track).has_Parameter(Consts.VCF_HEADER_SAMPLE)
							&&((VcfSample)(Externals.get(track).get_Parameter(Consts.VCF_HEADER_SAMPLE))).ifExists(PvarID))
					Pvar.set_Parameters(Consts.VCF_HEADER_SAMPLE, PvarID);
				else
					Pvar.set_Parameters(Consts.VCF_HEADER_SAMPLE, "");
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
				if(old_PvarID==null || old_Pvar==null || !old_Pvar.equals(track) || !old_PvarID.equals(PvarID))
					init_IndividualStat();
			}
		}
		else if(Annos.containsKey(track)){
			if(PvarID.equals(track)
					||(Annos.get(track).has_Parameter(Consts.VCF_HEADER_SAMPLE)
							&&((VcfSample)(Annos.get(track).get_Parameter(Consts.VCF_HEADER_SAMPLE))).ifExists(PvarID))){
				this.PvarID=PvarID;
				this.Pvar=SerializationUtils.clone(Annos.get(track));
//				Pvar.set_Mode(mode);
				Pvar.set_Mode(Consts.MODE_PACK);
				if(Annos.get(track).has_Parameter(Consts.VCF_HEADER_SAMPLE)
							&&((VcfSample)(Annos.get(track).get_Parameter(Consts.VCF_HEADER_SAMPLE))).ifExists(PvarID))
					Pvar.set_Parameters(Consts.VCF_HEADER_SAMPLE, PvarID);
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
				if(old_PvarID==null || old_Pvar==null || !old_Pvar.equals(track) || !old_PvarID.equals(PvarID))
					init_IndividualStat();
			}
		}
		return XmlWriter.xml2string(doc);
	}
	public void remove_Pvar(){
		Pvar=null;
		PvarID=null;
		Ele_var=null;
		is=null;
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
	public String get_Detail(String trackname_in, String id, int start,int end){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		Annotations track=null;
		boolean personal=false;
		if(trackname_in.startsWith("_")){
			trackname_in=trackname_in.substring(1);
			personal=true;
		}
		String trackname = "";
		String sample_id = null;
		if(trackname_in.indexOf("@")<0)
			trackname = trackname_in;
		else{
			String[] trackname_temp = trackname_in.split("@");
			sample_id = trackname_temp[0];
			trackname = trackname_temp[1];
		}

		if(personal && Pvar!=null && Pvar.get_ID().equals(trackname))
			track=Pvar;
		else if(Annos.containsKey(trackname))
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
				if(personal){
					VariantAnalysis ee = new VariantAnalysis(doc, rr, ele_temp, Ele_fanno, Ele_var, Chr);
					Element[] ele_anno_temp=null;
					try{
						ele_anno_temp=ee.deal();
					}catch(Exception e){
						e.printStackTrace();
					}
					doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(ele_anno_temp[0]);
					if(ele_anno_temp.length > 1)
						doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(ele_anno_temp[1]);
					doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(ele_temp);
				}
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
			else if(type_temp.equals(Consts.FORMAT_GRF)){
				GRFReader gr = new GRFReader(path_temp);
				ele_temp=gr.get_detail(doc, track.get_ID(),id, Chr,start,end);
				if(personal){
					GRFElementRegionComparison rc = new GRFElementRegionComparison(doc, Ele_var);
					rc.compareRegion(ele_temp);
				}
			}
			else if(type_temp.equals(Consts.FORMAT_GDF)){
				GDFReader gr = new GDFReader(path_temp);
				ele_temp=gr.get_detail(doc, track.get_ID(),id, Chr,start,end);
				if(personal){
					GdfElementSelector ges=new GdfElementSelector(doc,Ele_anno,Ele_var);
					doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(ges.select(ele_temp));
					doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(ele_temp);
				}
			}
			else if(type_temp.equals(Consts.FORMAT_GVF)){
				GVFReader gr = new GVFReader(path_temp);
				ele_temp=gr.get_detail(doc, track.get_ID(), id, Chr, start, end);
				if(personal){
					Element ele_var_temp=new Individual(ele_temp,true).mergeWithDBSNP(CfgReader.getBasicSnp(Assembly).get_Path(Chr), Chr, start, end, doc);
					doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(ele_var_temp);
					doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(ele_temp);
				}
			}
			else if (type_temp.equals(Consts.FORMAT_VCF)){
				VcfReader vr=new VcfReader(track,Chr);
				ele_temp=vr.get_detail(doc, track, sample_id, id, Chr, start, end);
				if(personal){
					Element ele_var_temp=new Individual(ele_temp,true).mergeWithDBSNP(CfgReader.getBasicSnp(Assembly).get_Path(Chr), Chr, start, end, doc);
					doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(ele_var_temp);
					doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(ele_temp);
				}
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
		Genes.overlap_Genes(doc, chr,start,end,is);
		return XmlWriter.xml2string(doc);
	}
	public String get_RankingGenes(int top_number){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		Genes.ranking_Genes(doc, is, top_number);
		return XmlWriter.xml2string(doc);
	}
	public String get_Assemblies(){
		Document doc=XmlWriter.init(Consts.META_ROOT);
		CfgReader.write_metalist(doc,CfgReader.getAssemblies(),"AssemblyList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Chromosomes(){
		String[] chrom_names=new String[rr.seq_name.size()];
		rr.seq_name.keySet().toArray(chrom_names);
		String[] chrom_lengths=new String[rr.seq_name.size()];
		for(int i=0;i<rr.seq_name.size();i++){
			int idx=check_chromosome(chrom_names[i]);
			chrom_lengths[idx]=chrom_names[i]+":"+rr.fasta_index[idx][0];
		}
		Document doc=XmlWriter.init(Consts.META_ROOT);
		CfgReader.write_metalist(doc,chrom_lengths, "ChromosomeList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Cyto(String chr){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		CytobandReader cbr=new CytobandReader(Cyto.get_Path());
		Element ele_temp=cbr.write_cytobands(doc, chr, is);
		return XmlWriter.xml2string(doc);
	}
	public String get_Cyto(){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		CytobandReader cbr=new CytobandReader(Cyto.get_Path());
		Element ele_temp=cbr.write_cytobands(doc, null, is);
		return XmlWriter.xml2string(doc);
	}
	public String get_SingleCytoScore(String chr, String id){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		CytobandReader cbr=new CytobandReader(Cyto.get_Path());
		Element ele_temp=cbr.write_cytoband(doc, chr, id, is, rr, Pvar,scoremeth);
		return XmlWriter.xml2string(doc);
	}
	public String get_Ped(String track){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		Annotations vt = null;
		if (Externals.containsKey(track))
			vt = Externals.get(track);
		else if(Annos.containsKey(track))
			vt = Annos.get(track);
		if(vt!=null&&vt.get_Type().equals(Consts.FORMAT_VCF)&&vt.has_Parameter(VCF_HEADER_SAMPLE)){
			Element ele_temp = ((VcfSample)vt.get_Parameter(VCF_HEADER_SAMPLE)).write_family2pedigree(doc, track);
		}
		return XmlWriter.xml2string(doc);
	}
	public String prioritize_Individuals(String track, String vcfs){
		Annotations vt = null;
		String[] indord = null;
		if (Externals.containsKey(track))
			vt = Externals.get(track);
		else if(Annos.containsKey(track))
			vt = Annos.get(track);
		if(vt!=null&&vt.get_Type().equals(Consts.FORMAT_VCF)){
			VcfReader vr = new VcfReader(vt, "chr1");
			indord = vr.write_individuals(track, vcfs);
		}
		Document doc=XmlWriter.init(Consts.META_ROOT);
		CfgReader.write_metalist(doc, indord, "IndividualOrder");
		return XmlWriter.xml2string(doc);
	}
	public String get_Intersection(String track, String set_a, String chr, long start, long end){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		int chrid=check_chromosome(chr);
		if(chrid>=0){
			Chr=chr;
			Coordinate=check_coordinate(chrid,start,end);
			Annotations vt = null;
			if (Externals.containsKey(track))
				vt = Externals.get(track);
			else if(Annos.containsKey(track))
				vt = Annos.get(track);
			if(vt!=null&&vt.get_Type().equals(Consts.FORMAT_VCF)&&vt.has_Parameter(VCF_HEADER_SAMPLE)){
				VcfReader vr = new VcfReader(vt, chr);
				Element ele_temp = vr.write_intersection(doc, track, set_a, chr, start, end);
			}
		}
		return XmlWriter.xml2string(doc);
	}
	public String get_Difference(String track, String set_a, String set_b, String chr, long start, long end){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		int chrid=check_chromosome(chr);
		if(chrid>=0){
			Chr=chr;
			Coordinate=check_coordinate(chrid,start,end);
			Annotations vt = null;
			if (Externals.containsKey(track))
				vt = Externals.get(track);
			else if(Annos.containsKey(track))
				vt = Annos.get(track);
			if(vt!=null&&vt.get_Type().equals(Consts.FORMAT_VCF)&&vt.has_Parameter(VCF_HEADER_SAMPLE)){
				VcfReader vr = new VcfReader(vt, chr);
				Element ele_temp = vr.write_difference(doc, track, set_a, set_b, chr, start, end);
			}
		}
		return XmlWriter.xml2string(doc);
	}
	public void remove_Ped(String track){
		Annotations vt = null;
		if (Externals.containsKey(track))
			vt = Externals.get(track);
		else if(Annos.containsKey(track))
			vt = Annos.get(track);
		if(vt!=null&&vt.get_Type().equals(Consts.FORMAT_VCF)&&vt.has_Parameter(VCF_HEADER_SAMPLE))
			((VcfSample)vt.get_Parameter(VCF_HEADER_SAMPLE)).removePedigree();
	}
	public void add_Ped(String track,String ped){
		Annotations vt = null;
		if (Externals.containsKey(track))
			vt = Externals.get(track);
		else if(Annos.containsKey(track))
			vt = Annos.get(track);
		if(vt!=null&&vt.get_Type().equals(Consts.FORMAT_VCF)&&vt.has_Parameter(VCF_HEADER_SAMPLE))
			((VcfSample)vt.get_Parameter(VCF_HEADER_SAMPLE)).initPedigree(ped);
	}
	public void load_Ped(String track,String filepath){
		Annotations vt = null;
		if (Externals.containsKey(track))
			vt = Externals.get(track);
		else if(Annos.containsKey(track))
			vt = Annos.get(track);
		if(vt!=null&&vt.get_Type().equals(Consts.FORMAT_VCF)&&vt.has_Parameter(VCF_HEADER_SAMPLE))
			((VcfSample)vt.get_Parameter(VCF_HEADER_SAMPLE)).loadPedigree(filepath);
	}
	public void load_Stat(String filepath){
		if(is!=null)
			is.load_Stat(filepath);
	}
	public String save_Stat(String session){
		if(is!=null&&Pvar!=null){
			is.save_Stat(session);
			if(PvarID==null||PvarID.equals("")||PvarID.equals(Pvar.get_ID()))
				return Pvar.get_ID();
			else
				return Pvar.get_ID()+"_"+PvarID;
		}
		return null;
	}
	public String set_ScoreMethod(String scoremeth){
		if(Annovar.ScoreMeth.containsKey(scoremeth))
			this.scoremeth=Annovar.ScoreMeth.get(scoremeth);
		else if(scoremeth.equals("Family"))
			this.scoremeth="Family";
		else
			this.scoremeth=null;
		if(Pvar!=null && PvarID != null && !scoremeth.equals("Family"))
			return add_Pvar(Pvar.get_ID(),Pvar.get_Mode(),PvarID);
		return null;
	}
	public String get_ScoreMethod(){
		Document doc=XmlWriter.init(Consts.META_ROOT);
		String[] scoremethlist=new String[1];
		if(this.scoremeth!=null)
			if(scoremeth.equals("ljb_sift"))
				scoremethlist[0]="SIFT";
			else if(scoremeth.equals("ljb_pp2"))
				scoremethlist[0]="PolyPhen2";
			else if(scoremeth.equals("Family"))
				scoremethlist[0]="Family";
			else
				scoremethlist[0]="PGB";
		else
			scoremethlist[0]="PGB";
		CfgReader.write_metalist(doc, scoremethlist ,"ScoreMethList");
		return XmlWriter.xml2string(doc);
	}
	public String get_ScoreMethods(){
		Document doc=XmlWriter.init(Consts.META_ROOT);
		String[] scoremethlist=new String[Annovar.ScoreMeth.keySet().size()];
		Annovar.ScoreMeth.keySet().toArray(scoremethlist);
		CfgReader.write_metalist(doc, scoremethlist ,"ScoreMethList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Annotations(){
		String[] anno_names_internal=new String[Annos.size()];
		int i=0;
		Enumeration<Annotations> annos_enum=Annos.elements();
		for(i=0;i<Annos.size();i++){
			Annotations temp=annos_enum.nextElement();
			anno_names_internal[i]=temp.get_Group()+":"+temp.get_ID()+":"+temp.get_Mode()+":"+temp.get_Type();
		}
		Arrays.sort(anno_names_internal);
		String[] anno_names=new String[Annos.size()+3+Pclns.size()];
		for(i=0;i<anno_names_internal.length;i++)
			anno_names[i]=anno_names_internal[i];
		if(Pvar!=null)
			anno_names[i++]="Pvar:_"+this.PvarID+"@"+Pvar.get_ID()+":"+Pvar.get_Mode()+":"+Pvar.get_Type();
		if(Pfanno!=null)
			anno_names[i++]="Pfanno:_"+Pfanno.get_ID()+":"+Pfanno.get_Mode()+":"+Pfanno.get_Type();
		if(Panno!=null)
			anno_names[i++]="Panno:_"+Panno.get_ID()+":"+Panno.get_Mode()+":"+Panno.get_Type();
		Enumeration<Annotations> pclns_enum=Pclns.elements();
		for(int j=0;j<Pclns.size();j++){
			Annotations temp=pclns_enum.nextElement();
			anno_names[i+j]="Pclns:_"+temp.get_ID()+":"+temp.get_Mode()+":"+temp.get_Type();
		}
		Document doc=XmlWriter.init(Consts.META_ROOT);
		CfgReader.write_metalist(doc,anno_names, "AnnotationList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Externals(){
		String[] externals_names=new String[Externals.size()];
		int i=0;
		Enumeration<Annotations> externals_enum=Externals.elements();
		for(i=0;i<Externals.size();i++){
			Annotations temp=externals_enum.nextElement();
			externals_names[i]=temp.get_Group()+":"+temp.get_ID()+":"+temp.get_Mode()+":"+temp.get_Type();
		}
		Arrays.sort(externals_names);
		Document doc=XmlWriter.init(Consts.META_ROOT);
		CfgReader.write_metalist(doc,externals_names, "AnnotationList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Check(String trackname){
		Annotations track=null;
		if(Annos.containsKey(trackname))
			track=Annos.get(trackname);
		else if(Externals.containsKey(trackname))
			track=Externals.get(trackname);
		else if(trackname.startsWith("_")&&Pvar!=null&&Pvar.get_ID().equals(trackname.substring(1)))
			track=Pvar;
		String[] check=new String[1];
		if(track!=null)
			check[0]=track.get_Check();
		else
			check[0]=null;
		Document doc=XmlWriter.init(Consts.META_ROOT);
		CfgReader.write_metalist(doc,check, "ErrorList");
		return XmlWriter.xml2string(doc);
	}
	public String get_CheckValue(String trackname){
		Annotations track=null;
		if(Annos.containsKey(trackname))
			track=Annos.get(trackname);
		else if(Externals.containsKey(trackname))
			track=Externals.get(trackname);
		else if(trackname.startsWith("_")&&Pvar!=null&&Pvar.get_ID().equals(trackname.substring(1)))
			track=Pvar;
		if(track!=null)
			return track.get_Check();
		else
			return null;
	}
	public String get_Individuals(){
		ArrayList<String> individuals = new ArrayList<String>();
		int i=0;
		Enumeration<Annotations> annos_enum=Annos.elements();
		for(i=0;i<Annos.size();i++){
			Annotations temp=annos_enum.nextElement();
			if(temp.get_Group().equals(Consts.GROUP_CLASS_PG)){
				StringBuffer individual=new StringBuffer();
				individual.append(temp.get_ID());
				individual.append(":");
				if(temp.get_Type().equals(Consts.FORMAT_VCF)&&temp.has_Parameter(Consts.VCF_HEADER_SAMPLE)){
					String[] samples=((VcfSample) temp.get_Parameter(Consts.VCF_HEADER_SAMPLE)).getSampleNames();
					for(int j=0;j<samples.length-1;j++){
						individual.append(samples[j]);
						individual.append(";");
					}
					if(samples.length>0)
						individual.append(samples[samples.length-1]);
				}
				else{
					individual.append(temp.get_ID());
				}
				individuals.add(individual.toString());
			}
		}
		String[] individuals_names = new String[individuals.size()];
		individuals.toArray(individuals_names);
		Arrays.sort(individuals_names);
		Document doc=XmlWriter.init(Consts.META_ROOT);
		CfgReader.write_metalist(doc,individuals_names, "IndividualList");
		return XmlWriter.xml2string(doc);
	}
	public String get_ExIndividuals(){
		ArrayList<String> individuals = new ArrayList<String>();
		int i=0;
		Enumeration<Annotations> externals_enum=Externals.elements();
		for(i=0;i<Externals.size();i++){
			Annotations temp=externals_enum.nextElement();
			if((temp.get_Type().equals(Consts.FORMAT_VCF)||temp.get_Type().equals(Consts.FORMAT_GVF))
				&& temp.get_Group().equals(Consts.GROUP_CLASS_EXI)){
				StringBuffer individual=new StringBuffer();
				individual.append(temp.get_ID());
				individual.append(":");
				if(temp.get_Type().equals(Consts.FORMAT_VCF)&&temp.has_Parameter(Consts.VCF_HEADER_SAMPLE)){
					String[] samples=((VcfSample) temp.get_Parameter(Consts.VCF_HEADER_SAMPLE)).getSampleNames();
					for(int j=0;j<samples.length-1;j++){
						individual.append(samples[j]);
						individual.append(";");
					}
					if(samples.length>0)
						individual.append(samples[samples.length-1]);
				}
				else{
					individual.append(temp.get_ID());
				}
				individuals.add(individual.toString());
			}
		}
		String[] individuals_names = new String[individuals.size()];
		individuals.toArray(individuals_names);
		Arrays.sort(individuals_names);
		Document doc=XmlWriter.init(Consts.META_ROOT);
		CfgReader.write_metalist(doc,individuals_names, "IndividualList");
		return XmlWriter.xml2string(doc);
	}
	public String get_Parameters(String[] tracks){
		Document doc=XmlWriter.init(Consts.DATA_ROOT);
		for(int i=0;i<tracks.length;i++)
			if(Annos.containsKey(tracks[i]))
				Annos.get(tracks[i]).write_anno2parameter(doc);
			else if(Externals.containsKey(tracks[i]))
				Externals.get(tracks[i]).write_anno2parameter(doc);
			else if(tracks[i].startsWith("_")&&tracks[i].substring(tracks[i].indexOf("@")+1).equals(Pvar.get_ID()))
				Pvar.write_anno2parameter(doc);
		return XmlWriter.xml2string(doc);
	}
	public void set_Params(String[] tracks,String[] params,String[] values){
		for(int i=0;i<tracks.length;i++){
			if(Annos.containsKey(tracks[i]))
				Annos.get(tracks[i]).set_Parameters(params[i], values[i]);
			else if(Externals.containsKey(tracks[i]))
				Externals.get(tracks[i]).set_Parameters(params[i], values[i]);
			else if(tracks[i].startsWith("_")&&tracks[i].substring(1).equals(Pvar.get_ID()))
				Pvar.set_Parameters(params[i], values[i]);
		}
	}
	
	public void save_Index(String trackname, String session){
		Annotations track = null;
		if (Pvar!=null && trackname.startsWith("_") && Pvar.get_ID().equals(trackname.substring(1)))
			track = Pvar;
		else if(Externals.containsKey(trackname))
			track = Externals.get(trackname);
		
		if (track!=null && track.get_Path()!=null && track.get_Type().equals(Consts.FORMAT_VCF) 
				&& (track.get_Path().startsWith("http://") || track.get_Path().startsWith("https://") || track.get_Path().startsWith("ftp://"))
				&& !track.has_Parameter(Consts.VCF_INDEX_LOCAL)){
			try {
				URL index = new URL(track.get_Path()+".tbi");
				ReadableByteChannel rbc = Channels.newChannel(index.openStream());
//				String filepath = System.getProperty("java.io.tmpdir")+"/"+session+"_"+trackname+".tbi";
				String filepath = System.getProperty("java.io.tmpdir")+"/"+md5(track.get_Path()+".tbi")+".tbi";
				File temp=new File(filepath);
				if(!temp.exists())
					temp.delete();
				FileOutputStream fos = new FileOutputStream(temp);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				
				track.initialize_Parameter(Consts.VCF_INDEX_LOCAL, filepath, Consts.PARAMETER_TYPE_INVISABLE);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		if (Coordinate[1]-Coordinate[0]>3000000)
			return;
		if(Pvar!=null&&track.get_ID().equals(Pvar.get_ID())&&type_temp.equals(Consts.FORMAT_VCF)&&Class==Consts.PTRACK_CLASS_VAR){
				VcfReader vr=new VcfReader(track,Chr);
				vr.changeBppLimit(Consts.LIMIT_BPP);
				Element ele_var=vr.write_vcf2variants(doc,"_"+track.get_ID(),mode,bpp,Chr,Coordinate[0],Coordinate[1]);
			//	Element ele_var=vr.write_vcf2variants(doc,"_"+track.get_ID(),Consts.MODE_PACK,bpp,Chr,Coordinate[0],Coordinate[1]);
				//Cancel Dense-mode-bandwidth saving. transfer all variants to client.
				add_att_ifParam(track,ele_var);
				if(PvarID!=null&&!track.get_ID().equals(PvarID))
					ele_var.setAttribute(Consts.XML_TAG_ID, "_"+PvarID);
				Ele_var = new Individual(ele_var).mergeWithDBSNP(CfgReader.getBasicSnp(Assembly).get_Path(Chr), Chr, Coordinate[0], Coordinate[1], doc);
				if(scoremeth!=null){
					Ele_var = Annovar.score_vars(doc, Ele_var, scoremeth, Chr, rr);
					add_att_ifParam(track,Ele_var);
				}
				doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Ele_var);
				doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(ele_var);
		}
		else if(Pvar!=null&&track.get_ID().equals(Pvar.get_ID())&&type_temp.equals(Consts.FORMAT_GVF)&&Class==Consts.PTRACK_CLASS_VAR){
			GVFReader gr = new GVFReader(Pvar.get_Path(Chr));
			Element ele_var=gr.write_gvf2variants(doc, "_"+track.get_ID(), Chr,Coordinate[0],Coordinate[1]);
			add_att_ifParam(track,ele_var);
			Ele_var = new Individual(ele_var).mergeWithDBSNP(CfgReader.getBasicSnp(Assembly).get_Path(Chr), Chr, Coordinate[0], Coordinate[1], doc);
			if(scoremeth!=null){
				Ele_var = Annovar.score_vars(doc, Ele_var, scoremeth, Chr, rr);
				add_att_ifParam(track,Ele_var);
			}
			doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Ele_var);
			doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(ele_var);
		}
		else if(Panno!=null&&track.get_ID().equals(Panno.get_ID())&&type_temp.equals(Consts.FORMAT_ANNO)&&Class==Consts.PTRACK_CLASS_ANNO){
			try{
				BasicAnnosReader bar=new BasicAnnosReader(Panno.get_Path(Chr));
				Element ele_anno=bar.write_ba2elements(doc, "_"+track.get_ID(), Chr, Coordinate[0], Coordinate[1], bpp);
				add_att_ifParam(track,ele_anno);
				VariantAnalysis ee = new VariantAnalysis(doc, rr, ele_anno, Ele_fanno, Ele_var, Chr);
				Ele_anno=ee.deal();
				if(scoremeth==null){
					if(Ele_var.getParentNode()==doc.getElementsByTagName(DATA_ROOT).item(0))
						doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(Ele_var);
					Ele_var=ee.variants2xml(doc, "_"+PvarID,"_"+Pvar.get_ID());
					doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Ele_var);
					add_att_ifParam(Pvar,Ele_var);
				}
				doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Ele_anno[0]);
				if(Ele_anno.length > 1){
					doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Ele_anno[1]);
				}
				doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(ele_anno);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else if(Pfanno!=null&&track.get_ID().equals(Pfanno.get_ID())&&type_temp.equals(Consts.FORMAT_GRF)&&Class==Consts.PTRACK_CLASS_FANNO){
			GRFReader gr2 = new GRFReader(Pfanno.get_Path(Chr));;
			try {
				Ele_fanno = gr2.write_grf2elements(doc, "_"+track.get_ID(), Chr,(int) Coordinate[0],(int) Coordinate[1]);
				GRFElementRegionComparison rc = new GRFElementRegionComparison(doc,Ele_var);
				rc.compareRegion(Ele_fanno);
				if(scoremeth==null){
					if(Ele_var.getParentNode()==doc.getElementsByTagName(DATA_ROOT).item(0))
						doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(Ele_var);
					Ele_var=rc.variants2xml(doc, "_"+PvarID,"_"+Pvar.get_ID());
					doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(Ele_var);
					add_att_ifParam(Pvar,Ele_var);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(Pclns.containsKey(track.get_ID())&&type_temp.equals(Consts.FORMAT_GDF)&&Class==Consts.PTRACK_CLASS_CLN){
			GDFReader gr3;
			try {
				gr3 = new GDFReader(Pclns.get(track.get_ID()).get_Path(Chr));
				Element ele_cln=gr3.write_gdf2elements(doc, "_"+track.get_ID(), Chr,(int) Coordinate[0],(int) Coordinate[1]);
				add_att_ifParam(track,ele_cln);
				GdfElementSelector ges=new GdfElementSelector(doc,Ele_anno,Ele_var);
				doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(ges.select(ele_cln));
				doc.getElementsByTagName(DATA_ROOT).item(0).removeChild(ele_cln);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else 
			append_track(track,doc,mode);
	}
	void init_track(Annotations track) {
		String path_temp=track.get_Path("chr1");
		String type_temp=track.get_Type();
		if(type_temp.equals(Consts.FORMAT_BEDGZ))
			new BedReaderTabix(path_temp);
		else if(type_temp.equals(Consts.FORMAT_ANNO))
			new BasicAnnosReader(path_temp);
		else if(type_temp.equals(Consts.FORMAT_BED))
			new BedReader(path_temp);
		else if(type_temp.equals(Consts.FORMAT_BIGBED))
			new BigBedReader(path_temp);
		else if(type_temp.equals(Consts.FORMAT_BEDGRAPH))
			new BedGraphReader(path_temp);
		else if(type_temp.equals(Consts.FORMAT_BIGWIG))
			new WiggleReader(path_temp,true);
		else if(type_temp.equals(Consts.FORMAT_WIG))
			new WiggleReader(path_temp,false);
		else if(type_temp.equals(Consts.FORMAT_GRF))
			new GRFReader(path_temp);
		else if(type_temp.equals(Consts.FORMAT_GDF))
			new GDFReader(path_temp);
		else if(type_temp.equals(Consts.FORMAT_GFF))
			new GFFReader(path_temp);
		else if(type_temp.equals(Consts.FORMAT_GTF))
			new GTFReader(path_temp);
		else if(type_temp.equals(Consts.FORMAT_GVF))
			new GVFReader(path_temp);
		else if (type_temp.equals(Consts.FORMAT_VCF))
			new VcfReader(track,"chr1");
		else if (type_temp.equals(Consts.FORMAT_BAM)){
			try {
				new BAMReader(path_temp);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		else if (type_temp.equals(Consts.FORMAT_FASTA)){
			try{
				new FastaReader(path_temp);
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		else if (type_temp.equals(Consts.FORMAT_CYTO))
			new CytobandReader(path_temp);
	}
	void append_track(Annotations track, Document doc,String mode) {
		String path_temp=track.get_Path(Chr);
		if(track.get_Check()!=null && !track.get_Check().equals(""))
			return;
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
				BigBedReader bbr=new BigBedReader(path_temp);
				ele_temp=bbr.write_bed2elements(doc, track.get_ID(), Chr, Coordinate[0], Coordinate[1], bpp);
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
				GDFReader gr;
				try{
					gr= new GDFReader(path_temp);
					ele_temp=gr.write_gdf2elements(doc, track.get_ID(), Chr,(int) Coordinate[0], (int) Coordinate[1]);
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			else if(type_temp.equals(Consts.FORMAT_GFF)){
				GFFReader gr = new GFFReader(path_temp);
				ele_temp=gr.write_gff2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1],"gene_id");
			}
			else if(type_temp.equals(Consts.FORMAT_GTF)){
				GTFReader gr = new GTFReader(path_temp);
				ele_temp=gr.write_gtf2elements(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1]);
			}
			else if (type_temp.equals(Consts.FORMAT_GVF)&&Coordinate[1]-Coordinate[0]<3000000){
				GVFReader gr = new GVFReader(path_temp);
				ele_temp=gr.write_gvf2variants(doc, track.get_ID(), Chr,Coordinate[0],Coordinate[1]);
			}
			else if (type_temp.equals(Consts.FORMAT_VCF)&&Coordinate[1]-Coordinate[0]<3000000){
				VcfReader vr=new VcfReader(track,Chr);
				ele_temp=vr.write_vcf2variants(doc,track.get_ID(),mode,bpp,Chr,Coordinate[0],Coordinate[1]);
			//	ele_temp=vr.write_vcf2variants(doc,track.get_ID(),Consts.MODE_PACK,bpp,Chr,Coordinate[0],Coordinate[1]);
			/* This is for automatically select THE sample when load single sample VCF file, 
			 * cooperate with the sentence in VcfReader initialization, 
			 * which is also annotated in this version.
			 * 
			 *  if(track.has_Parameter(Consts.VCF_HEADER_SAMPLE)
						&&((VcfSample) track.get_Parameter(Consts.VCF_HEADER_SAMPLE)).getSamplesNum()==1
						&&ele_temp.hasAttribute(Consts.XML_TAG_SUPERID)
						&&!ele_temp.getAttribute(Consts.XML_TAG_ID).equals(track.get_ID())){
					ele_temp.removeAttribute(Consts.XML_TAG_SUPERID);
					ele_temp.setAttribute(Consts.XML_TAG_ID,track.get_ID());
				}*/
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
			else if (type_temp.equals(Consts.FORMAT_FASTA)){
				try{
					FastaReader fr=new FastaReader(path_temp);
					ele_temp=fr.write_fasta2sequence(doc, Chr, Coordinate[0], Coordinate[1], track.get_ID(), bpp);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			else if (type_temp.equals(Consts.FORMAT_CYTO)){
				CytobandReader cbr=new CytobandReader(path_temp);
				ele_temp=cbr.write_cytobands(doc, Chr, is);
			}
			
			if(ele_temp!=null
					&&!ele_temp.getTagName().equals(Consts.XML_TAG_PARAMETERS)
					&&!type_temp.equals(Consts.FORMAT_CYTO)
					&&!type_temp.equals(Consts.FORMAT_REF)){
				if(!type_temp.equals(Consts.FORMAT_VCF)||!ele_temp.hasAttribute(Consts.XML_TAG_SUPERID))
					add_att_ifParam(track,ele_temp);
				else{
					Element DataEx=(Element) ele_temp.getParentNode();
					NodeList nl=DataEx.getChildNodes();
					for(int i=0;i<nl.getLength();i++){
						Element item_temp=(Element)(nl.item(i));
						if(item_temp.hasAttribute(Consts.XML_TAG_SUPERID)&&
								item_temp.getAttribute(Consts.XML_TAG_SUPERID).equals(
										ele_temp.getAttribute(Consts.XML_TAG_SUPERID)))
							add_att_ifParam(track,item_temp);
					}
				}
				
			}
		}
	}

	void add_att_ifParam(Annotations track, Element ele_temp){
		if(track.has_visable_Parameter())
			ele_temp.setAttribute(Consts.XML_TAG_IFP, Consts.TEXT_TRUE);
		else 
			ele_temp.setAttribute(Consts.XML_TAG_IFP, Consts.TEXT_FALSE);
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
		if (start>rr.fasta_index[chr_info][0])
			start=rr.fasta_index[chr_info][0];
		if (end<1)
			end=1;
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

	Annotations[] get_BasicAnnos(){
		ArrayList<Annotations> annolist=new ArrayList<Annotations>();
		Enumeration<Annotations> annos_enum=Annos.elements();
		for(int i=0;i<Annos.size();i++){
			Annotations anno_temp=annos_enum.nextElement();
			if(anno_temp.get_Type().equals(Consts.FORMAT_ANNO))
				annolist.add(anno_temp);
		}
		Enumeration<Annotations> externals_enum=Externals.elements();
		for(int i=0;i<Externals.size();i++){
			Annotations external_temp=externals_enum.nextElement();
			if(external_temp.get_Type().equals(Consts.FORMAT_ANNO))
				annolist.add(external_temp);
		}
		Annotations[] annos=new Annotations[annolist.size()];
		annolist.toArray(annos);
		return annos;
	}
	public static String md5 (String s){
		char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
		try{
			byte[] bytes = s.getBytes();
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			byte[] updateBytes = md.digest();
			int len = updateBytes.length;
			char myChar[] = new char[len*2];
			int k = 0;
			for(int i=0;i<len;i++){
				byte byte0 = updateBytes[i];
				myChar[k++] = hexDigits[byte0>>>4 & 0x0f];
				myChar[k++] = hexDigits[byte0 & 0x0f];
			}
			return new String(myChar);
		} catch (Exception e){
			return null;
		}
	}
	public static boolean ifURLexists(String URLName){
		try{
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		}
		catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
}
