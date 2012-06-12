import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import FileReaders.*;

public class Interfaces extends HttpServlet{

	Instance ins=new Instance();

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		Map<String, String[]> param=req.getParameterMap();
		res.setContentType("application/xml");
		if (param.get("action")[0].equals("getAssemblies")){
			String a=ins.get_Assemblies();
			res.getWriter().print(a);
		}
		else if (param.get("action")[0].equals("getAnnotations")){
			String a=ins.get_Annotations();
			res.getWriter().print(a);
		}
		else if (param.get("action")[0].equals("setAssembly")){
			ins=new Instance(param.get("assembly")[0]);
		}
		else if (param.get("action")[0].equals("modiTracks")||param.get("action").equals("addTracks")){
			String a=ins.add_Tracks(param.get("tracks"), param.get("modes"));
			res.getWriter().print(a);
		}
		else if (param.get("action")[0].equals("removeTracks")){
			ins.remove_Tracks(param.get("track"));
		}
		else if (param.get("action")[0].equals("addExternals")){
			ins.add_Externals(param.get("tracks"),param.get("links"), param.get("types"),param.get("modes"));
			String a=ins.add_Tracks(param.get("tracks"), param.get("modes"));
			res.getWriter().print(a);
		}
		else if (param.get("action")[0].equals("removeExternals")){
			ins.remove_Externals(param.get("tracks"));
		}
		else if (param.get("action")[0].equals("update")){
			String chr=param.get("chr")[0];
			long start=Long.parseLong(param.get("start")[0]);
			long end=Long.parseLong(param.get("end")[0]);
			int window_width=Integer.parseInt(param.get("width")[0]);
			String a=ins.update(chr, start, end, window_width);
			res.getWriter().print(a);
		}
	}
	private <T extends Enum<T>> EnumSet<T> decode_annos(Class<T> annoSet, long elements){
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
	}
}