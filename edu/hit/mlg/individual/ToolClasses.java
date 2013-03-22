package edu.hit.mlg.individual;

import static edu.hit.mlg.individual.EctypalSubElement.*;

import java.io.IOException;

import FileReaders.FastaReader;
import edu.hit.mlg.Tools.LinkedArrayList;
import edu.hit.mlg.Tools.LinkedArrayList.Entry;
import edu.hit.mlg.individual.vcf.Variant;

/**
 * As we know, the upstream's variation has an impact on the downstream's variation,
 * so we must record all the upstream's variations.
 * For example, there is a Box from 500 to 550, and we has dealed a DEL variation from 522
 * to 530 at it. Now there is another variation of SNV at 531, so we may need to extract
 * bases at 520, 521 and 531, not 529 to 531. So we should mark each of the bases extracted
 * from the fasta file which SubElement they come from. 
 * @author Chengwu Yan
 *
 */
class Extract3Bases{
	String sequence = "";
	Position2SubElement[] pss;
	
	Extract3Bases(int length){
		pss = new Position2SubElement[length];
	}
	
	/**
	 * Just used for DEL.
	 * @return
	 */
	public static Extract3Bases merge(Extract3Bases e3b1, Extract3Bases e3b2, boolean direction){
		if(e3b1 == null || e3b2 == null) return null;
		Extract3Bases e3b = new Extract3Bases(3);
		e3b.sequence = direction ? e3b1.sequence.concat(e3b2.sequence) : e3b2.sequence.concat(e3b1.sequence);
		e3b.pss[0] = e3b1.pss[0];
		if(e3b1.pss.length > 1)
			e3b.pss[1] = e3b1.pss[1];
		int len2 = e3b2.pss.length;
		e3b.pss[2] = e3b2.pss[len2 - 1];
		if(len2 > 1)
			e3b.pss[1] = e3b2.pss[0];
		
		return e3b;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		for(Position2SubElement ps : pss)
			builder.append(ps.toString() + ";");
		builder.append("sequence=" + sequence);
		return builder.toString();
	}
}

class Position2SubElement{
	int position;
	Entry<EctypalSubElement> subEle;
	
	Position2SubElement(int position, Entry<EctypalSubElement> subEle){
		this.position = position;
		this.subEle = subEle;
	}
	
	static boolean isContinuous(Position2SubElement prePs, Position2SubElement afterPs){
		return prePs.subEle == afterPs.subEle && (prePs.position == afterPs.position || prePs.position + 1 == afterPs.position);
	}
	
	public String toString(){
		return subEle.getElement().toString() + "(" + position + ")";
	}
}

class DealedVariation{
	String type;
	int from;
	int to;
	String letter;
	
	DealedVariation(Variant variant){
		this.type = variant.getType();
		this.from = variant.getFrom();
		this.to = variant.getTo();
		this.letter = variant.getLetter();
	}
	
	/*
	 * Deletion variant map to SubElement
	 */
	static class DelMap2SubElement{
		Variant deletion;//The deletion variant
		int boxBases = 0;//Number of bases of the deletion in the "BOX"
		private LinkedArrayList<EctypalSubElement> subEles;
		/*
		 * firstSubEle and lastSubEle is the first and last SubElement the deletion variant at respectively;
		 */
		Entry<EctypalSubElement> firstSubEle;
		Entry<EctypalSubElement> lastSubEle;
		int realFrom;
		int realTo;
		private boolean hasEffect;
		
		/**
		 * @param deletion The DEL variant
		 * @param firstSubEle	Must be where the <code>deletion.getFrom()</code> at, except <code>deletion.getFrom()</code> &lt; Element.from 
		 * @param lastSubEle	Must be where the <code>deletion.getTo()</code> at, except <code>deletion.getTo()</code> &gt; Element.to
		 * @param subEles
		 * @param hasEffect
		 */
		public DelMap2SubElement(Variant deletion, Entry<EctypalSubElement> firstSubEle, Entry<EctypalSubElement> lastSubEle,
				LinkedArrayList<EctypalSubElement> subEles, boolean hasEffect) {
			this.deletion =deletion;
			this.firstSubEle = firstSubEle;
			this.lastSubEle = lastSubEle;
			this.subEles = subEles;
			this.hasEffect = hasEffect;
			
			while(!shouldAddBoxBases(this.firstSubEle.getElement().getType(), hasEffect)){
				this.firstSubEle = subEles.getNext(this.firstSubEle);
				if(this.firstSubEle == null || this.firstSubEle.getElement().getFrom() > this.lastSubEle.getElement().getFrom()){
					this.firstSubEle = null;
					break;
				}
			}
			if(this.firstSubEle != null){
				while(!shouldAddBoxBases(this.lastSubEle.getElement().getType(), hasEffect)){
					this.lastSubEle = subEles.getPrevious(this.lastSubEle);
				}
				realFrom = deletion.getFrom() > this.firstSubEle.getElement().getFrom() ? deletion.getFrom() : this.firstSubEle.getElement().getFrom();  
				realTo = deletion.getTo() < this.lastSubEle.getElement().getTo() ? deletion.getTo() : this.lastSubEle.getElement().getTo();
				countBoxBases(subEles, hasEffect);
			}
		}
		
		/**
		 * Count the number of bases of the deletion in the "BOX"
		 */
		private void countBoxBases(LinkedArrayList<EctypalSubElement> subEles, boolean hasEffect){
			Entry<EctypalSubElement> cur = firstSubEle;
			while(cur.getElement().getFrom() <= lastSubEle.getElement().getFrom()){
				if(shouldAddBoxBases(cur.getElement().getType(), hasEffect))
					boxBases += cur.getElement().getLength();
				cur = subEles.getNext(cur);
			}
			boxBases -= lastSubEle.getElement().getTo() - realTo;
			boxBases -= realFrom - firstSubEle.getElement().getFrom();
		}
		
		/**
		 * Count number of bases from <code>firstSubEle.from</code> to <code>realFrom</code> when <code>direction==true</code> 
		 * or from <code>realTo</code> to <code>lastSubEle.to</code> when <code>direction==false</code>
		 * @return
		 */
		int countDistanceToTheEdge(boolean direction){
			return direction ? (realFrom - firstSubEle.getElement().getFrom() + 1) : (lastSubEle.getElement().getTo() - realTo + 1);
		}
		
		/**
		 * If boxBases % 3 != 0, we should change <code>firstSubEle</code> and <code>realFrom</code> and <code>lastSubEle</code> and 
		 * <code>realTo</code> if need.
		 * @return For <code>direcion==true</code>, if <code>lastSubEle</code> changed, return the foregone 
		 * <code>lastSubEle.getElement().getLength()</code>, return 0 if <code>lastSubEle</code> not changed.
		 * For <code>direcion==true</code>, if <code>lastSubEle</code> changed, return the foregone 
		 * <code>lastSubEle.getElement().getLength()</code>. return 0 if <code>firstSubEle</code> not changed.
		 * Return -1 if the <code>firstSubEle</code> or <code>lastSubEle</code> should be null.
		 */
		int changePos(boolean direction, int remain){
			if(boxBases % 3 == 0) return 0;
			int _temp = (3 - ((remain + boxBases - 1) % 3)) % 3;
			if(remain == 0) remain = 3;
			int fromMinus = direction ? (remain - 1) : _temp;
			int toAdd = direction ? _temp : (remain - 1);
			
			if(fromMinus > 0){
				Entry<EctypalSubElement> _first = firstSubEle;
				int fromPos = realFrom;
				fromPos = (fromPos - fromMinus < _first.getElement().getFrom()) ? _first.getElement().getFrom() : (fromPos - fromMinus);
				fromMinus -= realFrom - fromPos;
				if(fromMinus > 0){
					_first = getPreviousBox(subEles, _first, hasEffect);
					if(_first == null) return -1;
					fromPos = _first.getElement().getTo() - fromMinus + 1;
				}
				this.firstSubEle = _first;
				this.realFrom = fromPos;
			}
			
			if(toAdd > 0){
				Entry<EctypalSubElement> _last = lastSubEle;
				int toPos = realTo;
				toPos = (toPos + toAdd > _last.getElement().getTo()) ? _last.getElement().getTo() : (toPos + toAdd);
				toAdd -= toPos - realTo;
				if(toAdd > 0){
					_last = getNextBox(subEles, _last, hasEffect);
					if(_last == null) return -1;
					toPos = _last.getElement().getFrom() + toAdd - 1;
				}
				this.lastSubEle = _last;
				this.realTo = toPos;
			}
			return 0;
		}
		
		/**
		 * Get the sequence of the bases in the "Box" the deletion deleted.
		 * @param fr FastaReader instance
		 * @param chr chromosome name
		 * @return
		 */
		String delBases(FastaReader fr, String chr){
			try {
				StringBuilder bases = new StringBuilder();
				Entry<EctypalSubElement> cur = firstSubEle;
				int fromPos = realFrom;
				int lastPos = lastSubEle.getElement().getTo();
				do{
					if(shouldAddBoxBases(cur.getElement().getType(), hasEffect)){
						bases.append(fr.extract_seq(chr, fromPos, realTo < cur.getElement().getTo() ? realTo : cur.getElement().getTo()));
					}
					cur = subEles.getNext(cur);
					if(cur == null) break;
					fromPos = cur.getElement().getFrom();
				}while(cur.getElement().getTo() <= lastPos);
				return bases.toString();
			} catch (IOException e) {
				return null;
			}
		}
		
		/**
		 * Get <code>firstSubEle</code> if <code>direction==true</code>, <code>lastSubEle</code> else. 
		 * @param direction
		 * @return
		 */
		Entry<EctypalSubElement> getUpstreamSubEle(boolean direction){
			return direction ? firstSubEle : lastSubEle;
		}
		
		/**
		 * Get <code>lastSubEle</code> if <code>direction==true</code>, <code>firstSubEle</code> else. 
		 * @param direction
		 * @return
		 */
		Entry<EctypalSubElement> getDownstreamSubEle(boolean direction){
			return direction ? lastSubEle : firstSubEle;
		}
		
		/**
		 * Get <code>realFrom</code> if <code>direction==true</code>, <code>realTo</code> else. 
		 * @param direction
		 * @return
		 */
		int getUpstreamPos(boolean direction){
			return direction ? realFrom : realTo;
		}
		
		/**
		 * Get <code>realTo</code> if <code>direction==true</code>, <code>realFrom</code> else. 
		 * @param direction
		 * @return
		 */
		int getDownstreamPos(boolean direction){
			return direction ? realTo : realFrom;
		}
		
		public String toString(){
			return deletion + "Map to：[" + firstSubEle.getElement().toString() + "(" + this.realFrom + "), "
					 + lastSubEle.getElement().toString() + "(" + this.realTo + ")], boxNum:" + this.boxBases;
		}
	}
}