package gbservlet;

import java.io.*;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.util.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.math.util.MultidimensionalCounter.Iterator;

import javax.servlet.*;
import javax.servlet.http.*;

import filereaders.*;


public class Interfaces extends HttpServlet{
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		res.setContentType("application/xml");
		HttpSession session=req.getSession();
		if(session.getAttribute("Instance")==null)
			session.setAttribute("Instance", new Instance());
		if(res!=null){
			res.setHeader("Cache-Control", "no-cache,must-revalidate");
			res.setHeader("Pragma", "no-cache");
			res.setHeader("Expires", "-1");
		}
		Instance ins=(Instance) session.getAttribute("Instance");
		String action=req.getParameter("action");
		String a = null;
		if (action.equals("getAssemblies")){
			a=ins.get_Assemblies();
		}
		if (action.equals("getSession")){
			a = session.getId();
		}
		else if (action.equals("getAnnotations")){
			a=ins.get_Annotations();
		}
		else if (action.equals("getExternals")){
			a=ins.get_Externals();
		}
		else if (action.equals("getChromosomes")){
			a=ins.get_Chromosomes();
		}
		else if (action.equals("getAllCytobands")){
			a=ins.get_Cyto();
		}
		else if (action.equals("getIndividuals")){
			a=ins.get_Individuals();
		}
		else if (action.equals("getExIndividuals")){
			a=ins.get_ExIndividuals();
		}
		else if (action.equals("getCytobands")){
			a=ins.get_Cyto(req.getParameter("chr"));
		}
		else if (action.equals("getCytoband")){
			a=ins.get_SingleCytoScore(req.getParameter("chr"),req.getParameter("id"));
		}
		else if (action.equals("getPedigree")){
			a=ins.get_Ped(req.getParameter("tracks"));
		}
		else if (action.equals("removePedigree")){
			ins.remove_Ped(req.getParameter("tracks"));
		}
		else if (action.equals("setAssembly")){
			ins=new Instance(req.getParameter("assembly"));
			session.setAttribute("Instance", ins);
		}
		else if (action.equals("modiTracks")||action.equals("addTracks")){
			a=ins.add_Tracks(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
		}
		else if (action.equals("removeTracks")){
			ins.remove_Tracks(req.getParameter("tracks").split(","));
		}
		else if (action.equals("modiPvar")||action.equals("addPvar")){
			a=ins.add_Pvar(req.getParameter("tracks"),req.getParameter("modes"),req.getParameter("id"));
		}
		else if (action.equals("initPvar")){
			ins.init_Pvar(req.getParameter("tracks"),req.getParameter("id"));
		}
		else if (action.equals("removePvar")){
			ins.remove_Pvar();
		}
		else if (action.equals("modiPanno")||action.equals("addPanno")){
			a=ins.add_Panno(req.getParameter("tracks"),req.getParameter("modes"));
		}
		else if (action.equals("removePanno")){
			ins.remove_Panno();
		}
		else if (action.equals("modiPfanno")||action.equals("addPfanno")){
			a=ins.add_Pfanno(req.getParameter("tracks"),req.getParameter("modes"));
		}
		else if (action.equals("removePfanno")){
			a=ins.remove_Pfanno();
		}
		else if (action.equals("modiPclns")||action.equals("addPclns")){
			a=ins.add_Pclns(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
		}
		else if (action.equals("removePclns")){
			ins.remove_Pclns(req.getParameter("tracks").split(","));
		}
		else if (action.equals("setParams")){
			ins.set_Params(req.getParameter("tracks").split(","), req.getParameter("params").split(","), req.getParameter("values").split(","));
			a=ins.add_Tracks(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
		}
		else if (action.equals("downloadIndex")){
			ins.save_Index(req.getParameter("tracks"),session.getId());
		}
		else if (action.equals("setScoreMethod")){
			a=ins.set_ScoreMethod(req.getParameter("scoremeth"));
		}
		else if (action.equals("getScoreMethod")){
			a=ins.get_ScoreMethod();
		}
		else if (action.equals("getScoreMethods")){
			a=ins.get_ScoreMethods();
		}
		else if (action.equals("getDetail")){
			a=ins.get_Detail(req.getParameter("tracks"), req.getParameter("id"), Integer.parseInt(req.getParameter("start")), Integer.parseInt(req.getParameter("end")));
		}
		else if (action.equals("findGene")){
			a=ins.find_Gene(req.getParameter("prefix"));
		}
		else if (action.equals("getGene")){
			a=ins.get_Geneinfo(req.getParameter("gene"));
		}
		else if (action.equals("overlapGene")){
			a=ins.get_OverlapGenes(req.getParameter("chr"), Integer.parseInt(req.getParameter("start")), Integer.parseInt(req.getParameter("end")));
		}
		else if (action.equals("rankGene")){
			a=ins.get_RankingGenes(Integer.parseInt(req.getParameter("topnumber")));
		}
		else if (action.equals("getParams")){
			a=ins.get_Parameters(req.getParameter("tracks").split(","));
		}
		else if (action.equals("getCheck")){
			a=ins.get_Check(req.getParameter("tracks"));
		}
		else if (action.equals("addExternals")){
			ins.add_Externals(req.getParameter("tracks").split(","),req.getParameter("links").split(","), req.getParameter("types").split(","),req.getParameter("modes").split(","));
			a=ins.add_Tracks(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
		}
		else if (action.equals("addExIndividuals")){
			ins.add_Externals(req.getParameter("tracks").split(","),req.getParameter("links").split(","), req.getParameter("types").split(","),req.getParameter("modes").split(","));
		}
		else if (action.equals("removeExternals")){
			ins.remove_Externals(req.getParameter("tracks").split(","));
		}
		else if (action.equals("update")){
			String chr=req.getParameter("chr");
			long start=Long.parseLong(req.getParameter("start"));
			long end=Long.parseLong(req.getParameter("end"));
			int window_width=Integer.parseInt(req.getParameter("width"));
			a=ins.update(chr, start, end, window_width);
		}
		else if (action.equals("refresh")){
			String chr=req.getParameter("chr");
			long start=Long.parseLong(req.getParameter("start"));
			long end=Long.parseLong(req.getParameter("end"));
			int window_width=Integer.parseInt(req.getParameter("width"));
			a=ins.refresh(chr, start, end, window_width);
		}
		else if (action.equals("getStat")){
			String filename=ins.save_Stat(session.getId());
			if(filename!=null){
				File temp=new File(System.getProperty("java.io.tmpdir")+"/"+session.getId()+".stat");
				if(temp.exists()&&temp.isFile()){
					InputStream fis=null;
					OutputStream os=null;
					try{
						fis = new BufferedInputStream(new FileInputStream(temp));
						byte[] buffer = new byte[fis.available()];
						fis.read(buffer);
						fis.close();
						res.reset();
						res.setContentType("application/force-download");
						res.setHeader("Cache-Control", "no-cache,must-revalidate");
						res.setHeader("Pragma", "no-cache");
						res.setHeader("Expires", "-1");
						res.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename+".stat.txt", Consts.DEFAULT_ENCODE));
						res.addHeader("Content-Length", "" + temp.length());
						os = new BufferedOutputStream(res.getOutputStream());
				//		res.setContentType("application/octet-stream");
						os.write(buffer);
						os.flush();
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if(fis!=null){
							fis.close();
							fis=null;
						}
						if(os!=null){
							os.close();
							os=null;
						}
					}
				}
			}
		}
		PrintWriter out;
		if(a!=null){
			if (GzipUtils.isGzipSupported(req) && !GzipUtils.isGzipDisabled(req) && a.length()>1024*16){
				out = GzipUtils.getGzipWriter(res);
				res.setHeader("Content-Encoding", "gzip");
			}
			else
				out = res.getWriter();
			out.print(a);
			out.close();
		}
	}
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		//res.setContentType("application/xml");
		HttpSession session=req.getSession();
		if(session.getAttribute("Instance")==null)
			session.setAttribute("Instance", new Instance());
		if(res!=null){
			res.setHeader("Cache-Control", "no-cache,must-revalidate");
			res.setHeader("Pragma", "no-cache");
			res.setHeader("Expires", "-1");
		}
		Instance ins=(Instance) session.getAttribute("Instance");
		String action=req.getParameter("action");
		String a = null;
		if (action.equals("upStat")){
			File tmpdir=new File(System.getProperty("java.io.tmpdir"));
			File ftemp=null;
			String filepath=null;
			if(tmpdir.isDirectory()){
				int maxFileSize = 20*1024*1024;
				int maxMemSize = 2000*1024;
				String contentType=req.getContentType();
				if(contentType.indexOf("multipart/form-data")>=0){
					filepath=System.getProperty("java.io.tmpdir")+"/"+session.getId()+".stat";
					ftemp=new File(filepath);
					DiskFileItemFactory factory = new DiskFileItemFactory();
					factory.setSizeThreshold(maxMemSize);
					factory.setRepository(tmpdir);
					ServletFileUpload upload = new ServletFileUpload(factory);
					upload.setSizeMax(maxFileSize);
					try{
						List fileItems = upload.parseRequest(req);
						java.util.Iterator i = fileItems.iterator();
						while(i.hasNext()){
							FileItem fi = (FileItem)i.next();
							if(!fi.isFormField()){
								fi.write(ftemp);
								res.setStatus(200);
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}finally{
					}
				}
			}
			if(ftemp!=null){
				ins.load_Stat(filepath);
			}
		}
		else if (action.equals("addPedigree")){
			String track = req.getParameter("tracks");
			String contentType=req.getContentType();
			if(contentType.indexOf("multipart/form-data")>=0){
				DiskFileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				try{
					List fileItems = upload.parseRequest(req);
					java.util.Iterator i = fileItems.iterator();
					while(i.hasNext()){
						FileItem fi = (FileItem)i.next();
						if(fi.isFormField()&&fi.getFieldName().equals("pedigree")){
							ins.add_Ped(track, fi.getString());
							res.setStatus(200);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
				}
			}
		}
		else if (action.equals("searchIndividual")){
			String track = req.getParameter("tracks");
			String contentType=req.getContentType();
			if(contentType.indexOf("multipart/form-data")>=0){
				DiskFileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				try{
					List fileItems = upload.parseRequest(req);
					java.util.Iterator i = fileItems.iterator();
					while(i.hasNext()){
						FileItem fi = (FileItem)i.next();
						if(fi.isFormField()&&fi.getFieldName().equals("variants")){
							a = ins.prioritize_Individuals(track, fi.getString());
							res.setStatus(200);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
				}
			}
		}
		else if (action.equals("getIntersection")){
			String track = req.getParameter("tracks");
			String chr=req.getParameter("chr");
			long start=Long.parseLong(req.getParameter("start"));
			long end=Long.parseLong(req.getParameter("end"));

			String contentType=req.getContentType();
			if(contentType.indexOf("multipart/form-data")>=0){
				DiskFileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				try{
					List fileItems = upload.parseRequest(req);
					java.util.Iterator i = fileItems.iterator();
					while(i.hasNext()){
						FileItem fi = (FileItem)i.next();
						if(fi.isFormField()&&fi.getFieldName().equals("sets")){
							a=ins.get_Intersection(track, fi.getString(), chr, start, end);
							res.setStatus(200);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
				}
			}
		}
		else if (action.equals("getDifference")){
			String track = req.getParameter("tracks");
			String chr=req.getParameter("chr");
			long start=Long.parseLong(req.getParameter("start"));
			long end=Long.parseLong(req.getParameter("end"));

			String contentType=req.getContentType();
			if(contentType.indexOf("multipart/form-data")>=0){
				DiskFileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				try{
					List fileItems = upload.parseRequest(req);
					java.util.Iterator i = fileItems.iterator();
					while(i.hasNext()){
						FileItem fi = (FileItem)i.next();
						if(fi.isFormField()&&fi.getFieldName().equals("sets")){
							String[] sets = fi.getString().split(",");
							a=ins.get_Difference(track, sets[0], sets[1], chr, start, end);
							res.setStatus(200);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
				}
			}
		}
		else if (action.equals("upPedigree")){
			File tmpdir=new File(System.getProperty("java.io.tmpdir"));
			File ftemp=null;
			String track = req.getParameter("tracks");
			String filepath=null;
			if(tmpdir.isDirectory()){
				int maxFileSize = 20*1024*1024;
				int maxMemSize = 2000*1024;
				String contentType=req.getContentType();
				if(contentType.indexOf("multipart/form-data")>=0){
					filepath=System.getProperty("java.io.tmpdir")+"/"+session.getId()+"_"+track+".ped";
					ftemp=new File(filepath);
					DiskFileItemFactory factory = new DiskFileItemFactory();
					factory.setSizeThreshold(maxMemSize);
					factory.setRepository(tmpdir);
					ServletFileUpload upload = new ServletFileUpload(factory);
					upload.setSizeMax(maxFileSize);
					try{
						List fileItems = upload.parseRequest(req);
						java.util.Iterator i = fileItems.iterator();
						while(i.hasNext()){
							FileItem fi = (FileItem)i.next();
							if(!fi.isFormField()){
								fi.write(ftemp);
								res.setStatus(200);
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}finally{
					}
				}
			}
			if(ftemp!=null){
				ins.load_Ped(track, filepath);
			}
		}
		else if (action.equals("upExternal")){
			File tmpdir=new File(System.getProperty("java.io.tmpdir"));
			File ftemp1=null;
			File ftemp2=null;
			String filepath=null;
			String[] tracks = req.getParameter("tracks").split(",");
			String[] links = new String[tracks.length];
			String[] types = req.getParameter("types").split(",");
			String[] modes = req.getParameter("modes").split(",");
			if(tmpdir.isDirectory()){
				int maxFileSize = 20*1024*1024;
				int maxMemSize = 2000*1024;
				String contentType=req.getContentType();
				if(contentType.indexOf("multipart/form-data")>=0){
//					filepath=System.getProperty("java.io.tmpdir")+"/"+session.getId()+"_"+tracks[0]+".vcf.gz";
					filepath=System.getProperty("java.io.tmpdir")+"/"+Instance.md5(session.getId()+"_"+tracks[0])+".vcf.gz";
					ftemp1=new File(filepath);
					ftemp2=new File(filepath+".tbi");
					DiskFileItemFactory factory = new DiskFileItemFactory();
					factory.setSizeThreshold(maxMemSize);
					factory.setRepository(tmpdir);
					ServletFileUpload upload = new ServletFileUpload(factory);
					upload.setSizeMax(maxFileSize);
					try{
						List fileItems = upload.parseRequest(req);
						java.util.Iterator i = fileItems.iterator();
						int ii = 0;
						while(i.hasNext()){
							FileItem fi = (FileItem)i.next();
							if(!fi.isFormField() && ii == 0){
								fi.write(ftemp1);
								res.setStatus(200);
							}
							else if(!fi.isFormField() && ii == 1){
								fi.write(ftemp2);
								res.setStatus(200);
							}
							ii++;
						}
					}catch(Exception e){
						e.printStackTrace();
					}finally{
					}
				}
			}
			if(ftemp1!=null && ftemp2!=null){
				links[0]=filepath;
				ins.add_Externals(tracks,links,types,modes);
			}
		}
		PrintWriter out;
		if(a!=null){
			if (GzipUtils.isGzipSupported(req) && !GzipUtils.isGzipDisabled(req) && a.length()>1024*16){
				out = GzipUtils.getGzipWriter(res);
				res.setHeader("Content-Encoding", "gzip");
			}
			else
				out = res.getWriter();
			out.print(a);
			out.close();
		}
	}
/*	private <T extends Enum<T>> EnumSet<T> decode_annos(Class<T> annoSet, long elements){
		EnumSet<T> result=EnumSet.allOf(annoSet);
		for(T element : result){
			if((elements &(1L<<element.ordinal()))==0)
				result.remove(element);
		}
		return result;
	}
	private <T extends Enum<T>> long encode_annos(EnumSet<T> annoSet){
		if(annoSet==null)
			return 0;
		long elements=0;
		for(T element:annoSet){
			elements |= (1L<<element.ordinal());
		}
		return elements;
	}*/
}

class GzipUtils {
	public static boolean isGzipSupported(HttpServletRequest request) {
		String encodings = request.getHeader("Accept-Encoding");
		return ((encodings != null) && (encodings.indexOf("gzip") != -1));
	}
	public static boolean isGzipDisabled(HttpServletRequest request) {
        String flag = request.getParameter("disableGzip");
        return ((flag != null) && (!flag.equalsIgnoreCase("false")));
    }
	public static PrintWriter getGzipWriter(HttpServletResponse response){
        try {
            return (new PrintWriter(new GZIPOutputStream(response.getOutputStream())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
