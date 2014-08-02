package filereaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import filereaders.tools.StringSplit;

/*
 * LdReader implemented by Tabix
 * Require sorted and indexed ld file
 */
class LdReader{
	String ldPath;

	LdReader(String ld) {
		this.ldPath = ld;
	}

	List<String[]> write_ld2matrix(String chr, long regionstart, long regionend) {
		
		List<String[]> matrix = new ArrayList<String[]>();
		TabixReader ld_tb = null;
		try {
			String line;
			ld_tb = new TabixReader(ldPath);
			TabixReader.Iterator Query = ld_tb.query(chr + ":" + regionstart + "-" + regionend);
			StringSplit split = new StringSplit('\t');
			if(Query!=null){
				while ((line = Query.next()) != null) {
					split.split(line);
					matrix.add(split.getResult());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(ld_tb != null){
				try {
					ld_tb.TabixReaderClose();
				} catch (IOException e) {
				}
			}
		}
		return matrix;
	}
}