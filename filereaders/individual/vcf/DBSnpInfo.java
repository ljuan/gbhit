package filereaders.individual.vcf;

import static filereaders.individual.vcf.Vcf.dbSnpInfos_M;

public class DBSnpInfo {
	/**
	 * Every element map to dbSnpInfos_M. True represent INFO contains the
	 * key, false else.
	 */
	public boolean[] dbSnpInfos = new boolean[17];
	/**
	 * Only when the track is Database in VCF and the INFO contains
	 * "GENEINFO", GENEINFO is effective.
	 */
	public String GENEINFO = null;
	/**
	 * Only when the track is Database VCF and the INFO contains "SCS", SCS
	 * is effective.
	 */
	public String SCS = null;
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		for(String key : dbSnpInfos_M.keySet()){
			if(dbSnpInfos[dbSnpInfos_M.get(key)]){
				builder.append(key);
				if(key.equals("GENEINFO")){
					builder.append('=');
					builder.append(GENEINFO);
				}else if(key.equals("SCS")){
					builder.append('=');
					builder.append(SCS);
				}
				builder.append(';');
			}
		}
		
		if(builder.length() >0){
			builder.deleteCharAt(builder.length() - 1);
			return builder.toString();
		}else{
			return null;
		}
	}
	
	public static DBSnpInfo copy(DBSnpInfo obj){
		if(obj == null) return null;
		DBSnpInfo newObj = new DBSnpInfo();
		boolean[] dbSnpInfos = new boolean[17];
		for(int i=0; i<17; i++){
			dbSnpInfos[i] = obj.dbSnpInfos[i];
		}
		newObj.dbSnpInfos = dbSnpInfos;
		newObj.GENEINFO = obj.GENEINFO;
		newObj.SCS = obj.SCS;
		return newObj;
	}
}