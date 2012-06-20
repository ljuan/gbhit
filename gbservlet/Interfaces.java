package gbservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import FileReaders.*;

public class Interfaces extends HttpServlet{
	
	Instance ins=new Instance();
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		res.setContentType("application/xml");
		
		res.getWriter().println(req.getQueryString());
		if (req.getParameter("action").equals("getAssemblies")){
			String a=ins.get_Assemblies();
			res.getWriter().print(a);
		}
		else if (req.getParameter("action").equals("getAnnotations")){
			String a=ins.get_Annotations();
			res.getWriter().print(a);
		}
		else if (req.getParameter("action").equals("setAssembly")){
			ins=new Instance(req.getParameter("assembly"));
		}
		else if (req.getParameter("action").equals("modiTracks")||req.getParameter("action").equals("addTracks")){
			String a=ins.add_Tracks(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
			res.getWriter().print(a);
		}
		else if (req.getParameter("action").equals("removeTracks")){
			ins.remove_Tracks(req.getParameter("tracks").split(","));
		}
		else if (req.getParameter("action").equals("addExternals")){
			ins.add_Externals(req.getParameter("tracks").split(","),req.getParameter("links").split(","), req.getParameter("types").split(","),req.getParameter("modes").split(","));
			String a=ins.add_Tracks(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
			res.getWriter().print(a);
		}
		else if (req.getParameter("action").equals("removeExternals")){
			ins.remove_Externals(req.getParameter("tracks").split(","));
		}
		else if (req.getParameter("action").equals("update")){
			String chr=req.getParameter("chr");
			long start=Long.parseLong(req.getParameter("start"));
			long end=Long.parseLong(req.getParameter("end"));
			int window_width=Integer.parseInt(req.getParameter("width"));
			String a=ins.update(chr, start, end, window_width);
			res.getWriter().print(a);
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