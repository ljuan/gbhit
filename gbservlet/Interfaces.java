package gbservlet;

import java.io.*;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
		if (action.equals("getAssemblies")){
			String a=ins.get_Assemblies();
			res.getWriter().print(a);
		}
		if (action.equals("getSession")){
			res.getWriter().print(session.getId());
		}
		else if (action.equals("getAnnotations")){
			String a=ins.get_Annotations();
			res.getWriter().print(a);
		}
		else if (action.equals("getChromosomes")){
			String a=ins.get_Chromosomes();
			res.getWriter().print(a);
		}
		else if (action.equals("getCytobands")){
			String a=ins.get_Cyto(req.getParameter("chr"));
			res.getWriter().print(a);
		}
		else if (action.equals("getCytoband")){
			String a=ins.get_SingleCytoScore(req.getParameter("chr"),req.getParameter("id"));
			res.getWriter().print(a);
		}
		else if (action.equals("setAssembly")){
			ins=new Instance(req.getParameter("assembly"));
			session.setAttribute("Instance", ins);
		}
		else if (action.equals("modiTracks")||action.equals("addTracks")){
			String a=ins.add_Tracks(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
			res.getWriter().print(a);
		}
		else if (action.equals("removeTracks")){
			ins.remove_Tracks(req.getParameter("tracks").split(","));
		}
		else if (action.equals("modiPvar")||action.equals("addPvar")){
			String a=ins.add_Pvar(req.getParameter("tracks"),req.getParameter("modes"),req.getParameter("id"));
			res.getWriter().print(a);
		}
		else if (action.equals("initPvar")){
			ins.init_Pvar(req.getParameter("tracks"),req.getParameter("id"));
		}
		else if (action.equals("removePvar")){
			ins.remove_Pvar();
		}
		else if (action.equals("modiPanno")||action.equals("addPanno")){
			String a=ins.add_Panno(req.getParameter("tracks"),req.getParameter("modes"));
			res.getWriter().print(a);
		}
		else if (action.equals("removePanno")){
			ins.remove_Panno();
		}
		else if (action.equals("modiPfanno")||action.equals("addPfanno")){
			String a=ins.add_Pfanno(req.getParameter("tracks"),req.getParameter("modes"));
			res.getWriter().print(a);
		}
		else if (action.equals("removePfanno")){
			String a=ins.remove_Pfanno();
			res.getWriter().print(a);
		}
		else if (action.equals("modiPclns")||action.equals("addPclns")){
			String a=ins.add_Pclns(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
			res.getWriter().print(a);
		}
		else if (action.equals("removePclns")){
			ins.remove_Pclns(req.getParameter("tracks").split(","));
		}
		else if (action.equals("setParams")){
			ins.set_Params(req.getParameter("tracks").split(","), req.getParameter("params").split(","), req.getParameter("values").split(","));
			String a=ins.add_Tracks(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
			res.getWriter().print(a);
		}
		else if (action.equals("setScoreMethod")){
			String a=ins.set_ScoreMethod(req.getParameter("scoremeth"));
			res.getWriter().print(a);
		}
		else if (action.equals("getScoreMethods")){
			String a=ins.get_ScoreMethods();
			res.getWriter().print(a);
		}
		else if (action.equals("getDetail")){
			String a=ins.get_Detail(req.getParameter("tracks"), req.getParameter("id"), Integer.parseInt(req.getParameter("start")), Integer.parseInt(req.getParameter("end")));
			res.getWriter().print(a);
		}
		else if (action.equals("findGene")){
			String a=ins.find_Gene(req.getParameter("prefix"));
			res.getWriter().print(a);
		}
		else if (action.equals("getGene")){
			String a=ins.get_Geneinfo(req.getParameter("gene"));
			res.getWriter().print(a);
		}
		else if (action.equals("overlapGene")){
			String a=ins.get_OverlapGenes(req.getParameter("chr"), Integer.parseInt(req.getParameter("start")), Integer.parseInt(req.getParameter("end")));
			res.getWriter().print(a);
		}
		else if (action.equals("getParams")){
			String a=ins.get_Parameters(req.getParameter("tracks").split(","));
			res.getWriter().print(a);
		}
		else if (action.equals("addExternals")){
			ins.add_Externals(req.getParameter("tracks").split(","),req.getParameter("links").split(","), req.getParameter("types").split(","),req.getParameter("modes").split(","));
			String a=ins.add_Tracks(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
			res.getWriter().print(a);
		}
		else if (action.equals("removeExternals")){
			ins.remove_Externals(req.getParameter("tracks").split(","));
		}
		else if (action.equals("update")){
			String chr=req.getParameter("chr");
			long start=Long.parseLong(req.getParameter("start"));
			long end=Long.parseLong(req.getParameter("end"));
			int window_width=Integer.parseInt(req.getParameter("width"));
			String a=ins.update(chr, start, end, window_width);
			res.getWriter().print(a);
		}
		else if (action.equals("refresh")){
			String chr=req.getParameter("chr");
			long start=Long.parseLong(req.getParameter("start"));
			long end=Long.parseLong(req.getParameter("end"));
			int window_width=Integer.parseInt(req.getParameter("width"));
			String a=ins.refresh(chr, start, end, window_width);
			res.getWriter().print(a);
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
