package edu.hit.mlg.individual;

import static FileReaders.Consts.XML_TAG_VARIANT;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.hit.mlg.Tools.StringSplit;
import edu.hit.mlg.individual.vcf.DBSnpInfo;
import edu.hit.mlg.individual.vcf.Variant;

public class VariantMapToDBSNP implements Comparable<VariantMapToDBSNP> {
	Variant variant;
	DBSnpInfo dbsnp;
	String dbsnpId;

	public VariantMapToDBSNP(Variant variant, DBSnpInfo dbsnp, String dbsnpId) {
		this.variant = variant;
		this.dbsnp = dbsnp;
		this.dbsnpId = dbsnpId;
	}
	
	Element write2xml(Document doc, Element parent){
		Element ele = doc.createElement(XML_TAG_VARIANT);
		if(dbsnp != null){
			variant.setId(dbsnpId);
			String info = dbsnp.toString();
			String letter = variant.getLetter();
			if(letter == null){
				variant.setLetter(info);
			}else{
				variant.setLetter((info != null ? info : "").concat(";").concat(letter));
			}
		}
		variant.write2xml(doc, parent);
		
		return ele;
	}
	
	/**
	 * Divide all VariantMapToDBSNP into two groups only when all VariantMapToDBSNP.variant.getHomo() is
	 * like: number1|number2. Or return <code>mergeVariants</code>.<br />
	 * if VariantMapToDBSNP.variant.getHomo() is like: number1|number2, then the first group will contain this
	 * VariantMapToDBSNP Object if number!=0, and the second group will also contain this VariantMapToDBSNP 
	 * Object if number2!=0. 
	 * 
	 * @param mergeVariants
	 * @return
	 */
	static Object[] divide(List<VariantMapToDBSNP> mergeVariants){
		ArrayList<VariantMapToDBSNP> result1 = new ArrayList<VariantMapToDBSNP>();
		ArrayList<VariantMapToDBSNP> result2 = new ArrayList<VariantMapToDBSNP>();
		String homo;
		boolean divide = true;
		StringSplit split = new StringSplit('|');
		
		for(VariantMapToDBSNP mergeVariant : mergeVariants){
			homo = mergeVariant.variant.getHomo();
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
				result1.add(mergeVariant);
			}
			if('0' != split.getResultByIndex(1).charAt(0)){
				result2.add(copy(mergeVariant));
			}
		}
		
		return divide ? new Object[]{ result1, result2 } : new Object[]{ mergeVariants };
	}

	@Override
	public int compareTo(VariantMapToDBSNP o) {
		return this.variant.compareTo(o.variant);
	}
	
	public static VariantMapToDBSNP copy(VariantMapToDBSNP obj){
		Variant variant = null;
		DBSnpInfo dbsnp = null;
		String dbsnpId = null;
		
		variant = Variant.copy(obj.variant);
		dbsnp = DBSnpInfo.copy(obj.dbsnp);
		dbsnpId = obj.dbsnpId;
		
		return new VariantMapToDBSNP(variant, dbsnp, dbsnpId);
	}
}
