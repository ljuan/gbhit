package filereaders.individual;

import static filereaders.Consts.XML_TAG_VARIANT;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import filereaders.individual.vcf.DBSnpInfo;
import filereaders.individual.vcf.Variant;
import filereaders.tools.StringSplit;



public class VariantMapToDBSNP implements Comparable<VariantMapToDBSNP> {
	Variant variant;
	DBSnpInfo dbsnp;
	String dbsnpInfo;
	String dbsnpId;

	public VariantMapToDBSNP(Variant variant, DBSnpInfo dbsnp, String dbsnpId, String dbsnpInfo) {
		this.variant = variant;
		this.dbsnp = dbsnp;
		this.dbsnpId = dbsnpId;
		this.dbsnpInfo = dbsnpInfo;
	}
	
	Element write2xml(Document doc, Element parent){
		return this.write2xml(doc, parent, false);
	}
	
	/**
	 * Write this object to XML Node.
	 * @param doc
	 * @param parent This object's parent node.
	 * @param isDetail whether this object comes from get_detail();
	 * @return
	 */
	Element write2xml(Document doc, Element parent, boolean isDetail){
		Element ele = doc.createElement(XML_TAG_VARIANT);
		if(dbsnpId != null){
			variant.setDbsnpid(dbsnpId);
			if(isDetail){
//				variant.setDbsnpInfo(dbsnp.toString());//Temporarily replaced by simple all fields of dbsnp info.
				variant.setDbsnpInfo(dbsnpInfo);
			}
		}
		variant.write2xml(doc, parent);
		
		return ele;
	}
	
	/**
	 * Divide all Variants into two groups only when all Variants.getHomo() is like: number1|number2. 
	 * Or return <code>variants</code>.<br />
	 * if Variant.getHomo() is like: number1|number2, then the first group will contain this Variant 
	 * Object if number!=0, and the second group will also contain this Variant Object if number2!=0. 
	 * 
	 * @param variants
	 * @return
	 */
	static Object[] divide(List<Variant> variants){
		ArrayList<Variant> result1 = new ArrayList<Variant>();
		ArrayList<Variant> result2 = new ArrayList<Variant>();
		String homo;
		boolean divide = true;
		StringSplit split = new StringSplit('|');
		
		for(Variant variant : variants){
			homo = variant.getHomo();
			if("".equals(homo)){
				divide = false;
				break;
			}
			split.split(homo);
			if(split.getResultNum() != 2){
				divide = false;
				break;
			}
			if('0' != split.getResultByIndex(0).charAt(0)){
				result1.add(Variant.copy(variant));
			}
			if('0' != split.getResultByIndex(1).charAt(0)){
				result2.add(Variant.copy(variant));
			}
		}
		
		return divide ? new Object[]{ result1, result2 } : new Object[]{ variants };
	}
	
	
	@Override
	public int compareTo(VariantMapToDBSNP o) {
		return this.variant.compareTo(o.variant);
	}
	
	public static VariantMapToDBSNP copy(VariantMapToDBSNP obj){
		Variant variant = null;
		DBSnpInfo dbsnp = null;
		String dbsnpId = null;
		String dbsnpInfo = null;
		
		variant = Variant.copy(obj.variant);
		dbsnp = DBSnpInfo.copy(obj.dbsnp);
		dbsnpId = obj.dbsnpId;
		dbsnpInfo = obj.dbsnpInfo;
		
		return new VariantMapToDBSNP(variant, dbsnp, dbsnpId, dbsnpInfo);
	}
}
