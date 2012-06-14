import net.sf.samtools.*;

public class BAMReader {
	public BAMReader(){
	}
	/**
	 * 给定染色体和基因组序列范围
	 * @param Chromosome 染色体名
	 * @param begin 序列起始位置
	 * @param end 序列终止位置
	 */
	public BAMReader(String Chromosome, int begin, int end){
		this.Chromosome = Chromosome;
		this.begin = begin;
		this.end = end;
	}
	
	/**
	 * 获得序列范围所在的最小的linear index
	 * @return linear index
	 */
	public int getLeftmostLinearIndex(){
		return begin >> 14;
	}
	/**
	 * 染色体名
	 */
	private String  Chromosome = "";
	/**
	 * 给定范围的起始位置
	 */
	private int begin = 0;
	/**
	 * 给定范围的终止位置
	 */
	private int end = 0;
}
