package gbservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import FileReaders.*;

public class Interfaces extends HttpServlet{
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		res.setContentType("application/xml");
		HttpSession session=req.getSession();
		if(session.getAttribute("Instance")==null)
			session.setAttribute("Instance", new Instance());
		Instance ins=(Instance) session.getAttribute("Instance");
		String action=req.getParameter("action");
		if (action.equals("getAssemblies")){
			String a=ins.get_Assemblies();
			res.getWriter().print(a);
		}
		else if (action.equals("getAnnotations")){
			String a=ins.get_Annotations();
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
		else if (action.equals("setParams")){
			ins.set_Params(req.getParameter("tracks").split(","), req.getParameter("params").split(","), req.getParameter("values").split(","));
			String a=ins.add_Tracks(req.getParameter("tracks").split(","), req.getParameter("modes").split(","));
			res.getWriter().print(a);
		}
		else if (action.equals("getParams")){
			String a=ins.get_Parameters(req.getParameter("tracks").split(","));
			res.getWriter().print(a);
		}
		else if (action.equals("removeTracks")){
			ins.remove_Tracks(req.getParameter("tracks").split(","));
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