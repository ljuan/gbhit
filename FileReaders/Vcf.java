package FileReaders;

import java.util.*;

/*
 * Variant Call Format (VCF) Format Class
 * Represent one VCF record.
 */


class Vcf{
	String Chr;
	long Pos;
	String ID;
	String Ref;
	String Alt;
	String Qual;
	String Filter;

	String Info;
	String Format;
	String[] Samples;
	Vcf(String vcf){
		String[] temp=vcf.split("\t");
		if(!temp[0].startsWith("chr"))
			Chr="chr"+temp[0];
		else
			Chr=temp[0];
		Pos=Long.parseLong(temp[1]);
		ID=temp[2];
		Ref=temp[3];
		Alt=temp[4];
		Qual=temp[5];
		Filter=temp[6];
		Info=temp[7];
		Format="";
		Samples=new String[0];
	}
	Vcf(String vcf,int samplenum){
		String[] temp=vcf.split("\t");
		if(!temp[0].startsWith("chr"))
			Chr="chr"+temp[0];
		else
			Chr=temp[0];
		Pos=Long.parseLong(temp[1]);
		ID=temp[2];
		Ref=temp[3];
		Alt=temp[4];
		Qual=temp[5];
		Filter=temp[6];
		Info=temp[7];
		Format=temp[8];
		Samples=Arrays.copyOfRange(temp, 9, 9+samplenum);
	}
}
	/* 
	 * We don't analyze Info and Format fields at this stage.
	 * These code need to be rewrite in OO manner. 
	 * For the same logic process of different fields, 
	 * and for the same processing method of different data type(Integer, Float, String...)
	 * We also need to consider to put this function up to higher level(VcfReader)
	 * 
	Hashtable<String, Object> Info;
	Hashtable<String, Object> Format;
	
	Vcf(String vcf, Hashtable<String, String[]> keys_info, Hashtable<String, String[]> keys_format){
		String[] temp=vcf.split("\t");
		if(!temp[0].startsWith("chr"))
			Chr="chr"+temp[0];
		else
			Chr=temp[0];
		Pos=Long.parseLong(temp[1]);
		ID=temp[2];
		Ref=temp[3];
		Alt=temp[4];
		Qual=Integer.parseInt(temp[5]);
		Filter=temp[6];
		String[] info_temp=temp[7].split(";");
		for(int i=0;i<info_temp.length;i++){
			if(info_temp[i].indexOf("=")>0){
				String[] sub_info_temp=info_temp[i].split("=");
				int num=-1;
				if(keys_info.get(sub_info_temp[0])[0].matches("[0-9]+"))
					num=Integer.parseInt(keys_info.get(sub_info_temp[0])[0]);
				else
					num=sub_info_temp[1].split(",").length;
				if(keys_info.get(sub_info_temp[0])[1].equals("Integer")){
					if(num<2)
						Info.put(sub_info_temp[0], Integer.parseInt(sub_info_temp[1]));
					else{
						String[] content_temp=sub_info_temp[1].split(",");
						int[] integer=new int[content_temp.length];
						for(int j=0;j<content_temp.length;j++)
							integer[j]=Integer.parseInt(content_temp[j]);
						Info.put(sub_info_temp[0], integer);
					}
				}
				else if(keys_info.get(sub_info_temp[0])[1].equals("Float")){
					if(num<2)
						Info.put(sub_info_temp[0], Float.parseFloat(sub_info_temp[1]));
					else{
						String[] content_temp=sub_info_temp[1].split(",");
						float[] floats=new float[content_temp.length];
						for(int j=0;j<content_temp.length;j++)
							floats[j]=Float.parseFloat(content_temp[j]);
						Info.put(sub_info_temp[0], floats);
					}
				}
				else {
					if(num<2)
						Info.put(sub_info_temp[0], sub_info_temp[1]);
					else
						Info.put(sub_info_temp[0], sub_info_temp[1].split(","));
				}
			}
			else{
				Info.put(info_temp[i], true);
			}
		}
		String[] format_temp=temp[8].split(";");
		for(int i=0;i<format_temp.length;i++){
			if(format_temp[i].indexOf("=")>0){
				String[] sub_format_temp=format_temp[i].split("=");
				int num=-1;
				if(keys_format.get(sub_format_temp[0])[0].matches("[0-9]+"))
					num=Integer.parseInt(keys_format.get(sub_format_temp[0])[0]);
				else
					num=sub_format_temp[1].split(",").length;
				if(keys_format.get(sub_format_temp[0])[1].equals("Integer")){
					if(num<2)
						Format.put(sub_format_temp[0], Integer.parseInt(sub_format_temp[1]));
					else{
						String[] content_temp=sub_format_temp[1].split(",");
						int[] integer=new int[content_temp.length];
						for(int j=0;j<content_temp.length;j++)
							integer[j]=Integer.parseInt(content_temp[j]);
						Format.put(sub_format_temp[0], integer);
					}
				}
				else if(keys_format.get(sub_format_temp[0])[1].equals("Float")){
					if(num<2)
						Format.put(sub_format_temp[0], Float.parseFloat(sub_format_temp[1]));
					else{
						String[] content_temp=sub_format_temp[1].split(",");
						float[] floats=new float[content_temp.length];
						for(int j=0;j<content_temp.length;j++)
							floats[j]=Float.parseFloat(content_temp[j]);
						Format.put(sub_format_temp[0], floats);
					}
				}
				else {
					if(num<2)
						Format.put(sub_format_temp[0], sub_format_temp[1]);
					else
						Format.put(sub_format_temp[0], sub_format_temp[1].split(","));
				}
			}
			else{
				Format.put(format_temp[i], true);
			}
		}
	}
*/
	
