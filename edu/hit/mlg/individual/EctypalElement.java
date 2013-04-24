package edu.hit.mlg.individual;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hit.mlg.Tools.LinkedArrayList;
import edu.hit.mlg.Tools.LinkedArrayList.Entry;
import edu.hit.mlg.individual.DealedVariation.DelMap2SubElement;
import edu.hit.mlg.individual.VariantAnalysis.ControlArea;
import edu.hit.mlg.individual.vcf.Variant;

import FileReaders.FastaReader;
import FileReaders.XmlWriter;
import static FileReaders.Consts.*;
import static edu.hit.mlg.individual.GeneticCode.*;
import static edu.hit.mlg.individual.EctypalSubElement.*;
import static edu.hit.mlg.individual.vcf.Variant.*;

public class EctypalElement {
	/**
	 * The first index is direction of the Element: 0 for true and 1 for false;</br>
	 * The second index is type of variant: 0 for SNV and 1 for INS and 2 for DEL;</br>
	 * The third index is for the remain: form 0 to 2;</br>
	 * The fourth index: 0 for relativeFrom and 1 for relativeTo.
	 */
	private static final byte extractIndex[][][][] = new byte[][][][]{
			{ { {-2, 0}, {0, 2}, {-1, 1} }, { {0, 0}, {0, 2}, {-1, 1} }, { {-1, 0}, {0, 0}, {0, 1} } }, 
			{ { {0, 2}, {-2, 0}, {-1, 1} }, { {0, 0}, {-2, 0}, {-1, 1} }, { {0, 1}, {0, 0}, {-1, 0} } } 
		};
	/////////////////////////////////////////////////////////Field
	private static final float BOX_LIMIT = 1 / 10.0f;
	private Set<String> status = null;
	private String id = null;// Attribute
	private FastaReader fr = null;// Attribute
	private String variant = null;// Attribute
	private String symbol = null;// Attribute
	private String type = null;// Attribute
	private int from;// Tag
	private int to;// Tag
	/**
	 * True if the direction of this Element is "+"; false else.
	 */
	private boolean direction;// Tag
	private String description = null;// Tag
	private String color = null;// Tag
	private LinkedArrayList<EctypalSubElement> subEles = null;
	/**
	 * Whether this Element still need to do in the current stage.
	 */
	private boolean stillNeedToDeal = true;
	/**
	 * Total bases at all Boxes Initially
	 */
	private int initBoxLen = 0;
	/**
	 * Which chromosome this SubElement at.
	 */
	private String chr;
	/**
	 * The region of initiator and terminator.
	 * If the first Box's first three bases' position are 8,9,10. And the last Box's last three bases' position are 98,99,100.
	 * Now if direction=true, initiatorSmall=8 and initiatorLarge=10 and terminatorSmall=98 and terminatorLarge=100;
	 * If direction=false, initiatorSmall=100 and initiatorLarge=98 and terminatorSmall=10 and terminatorLarge=8.
	 * If initiator=0, there is no any box.
	 * When any INS or DEL variation appear in the initiator or terminator, we should change some of the four fields below.
	 */
	private int initiatorSmall = -1;
	private int initiatorLarge = -1;
	private int terminatorSmall = -1;
	private int terminatorLarge = -1;
	/**
	 * Length of chromosome <code>chr</code>
	 */
	private int chrLen;
	/**
	 * Number of bases at "BOX" current from the first base at "BOX".
	 * The "BOX" is one of <code>SUBELEMENT_TYPE_BOX</code>, <code>SUBELEMENT_TYPE_EXTENDBOX<code>, 
	 * <code>SUBELEMENT_TYPE_SHIFTBOX</code> or <code>SUBELEMENT_TYPE_SHIFTEXTENDBOX</code>.
	 */
	private int boxBaseNumFromFirstBoxBase = 0;
	/**
	 * Total number of bases of variations at "BOX" current we have dealed.
	 * If an INS variation has inserted 1 base, boxBaseNumDealed=boxBaseNumDealed+1;
	 * If a DEL variation has deleted 1 base, boxBaseNumDealed=boxBaseNumDealed-1;
	 */
	private int boxBaseNumDealed = 0;
	/**
	 * The previous SubElement of current SubElement which is not Line and not Band. We need to
	 * record this SubElement because in the third stage of processing, the upstream's SubElement
	 * have an impact on the downstream's Box.
	 * preSubEleNotLineNotBand=null for the initialization.
	 */
	private EctypalSubElement preSubEleNotLineNotBand = null;
	/**
	 * The upstream's variation has an impact on the downstream's variation, so we must record all
	 * the variations dealed in the upstream.
	 */
	private LinkedArrayList<DealedVariation> dealedVariations = null;
	/**
	 * Whether the upstream SubElement has an effect on the downstream SubElement.
	 */
	private boolean hasEffect;
	/////////////////////////////////////////////////////End of field
	
	/////////////////////////////////////////////////////////////Constructor
	/**
	 * Create an ectypal Object of <code>node</code>.
	 * 
	 * @param ele
	 * @param fr
	 * @param chr Which chromosome this SubElement at.
	 * @param hasEffect Whether the upstream SubElement has an effect on the downstream SubElement.
	 */
	EctypalElement(Element ele, FastaReader fr, String chr, boolean hasEffect) {
		subEles = new LinkedArrayList<EctypalSubElement>();
		status = new HashSet<String>();
		this.chr = chr;
		this.hasEffect = hasEffect;
		if(fr!=null)
			this.fr=fr;
		this.chrLen = (int) fr.getChromosomeLength(chr);
		this.id = ele.getAttribute(XML_TAG_ID);
		if ("".equals(this.id)) this.id = null;
		this.type = ele.getAttribute(XML_TAG_TYPE);
		if ("".equals(this.type)) this.type = null;
		this.symbol = ele.getAttribute(XML_TAG_SYMBOL);
		if ("".equals(this.symbol))	this.symbol = null;
		this.variant = ele.getAttribute(XML_TAG_VARIANT);
		if ("".equals(this.variant)) this.variant = null;
		this.direction = ele.getElementsByTagName(XML_TAG_DIRECTION).item(0).getTextContent().equals("+");
		NodeList nodes = ele.getChildNodes();// All Children
		// The first child must be "From"
		this.from = Integer.parseInt(nodes.item(0).getTextContent());
		// The second child must be "To"
		this.to = Integer.parseInt(nodes.item(1).getTextContent());
		retriveTags(nodes);
	}

	private void retriveTags(NodeList nodes) {
		Element e = null;
		String t = null;
		EctypalSubElement ese = null;
		boolean firstSubElement = true;
		boolean firstBox = true;
		boolean firstEqual = true;
		boolean lastEqual = true;
		for (int index = 2, len = nodes.getLength(); index < len; index++) {
			e = (Element) nodes.item(index);
			t = e.getTagName();
			if (t.equals(XML_TAG_SUBELEMENT)) {
				// SubElement
				ese = new EctypalSubElement(e);
				subEles.addLast(ese);
				if(firstSubElement){
					firstEqual = (this.from == ese.getFrom());
					firstSubElement = false;
				}
				lastEqual = this.to == ese.getTo();
				if (SUBELEMENT_TYPE_BOX.equals(ese.getType())) {
					initBoxLen += ese.getLength();
					if(direction){
						terminatorSmall = ese.getTo() - 2;
						terminatorLarge = ese.getTo();
					} else{
						initiatorSmall = ese.getTo() - 2;
						initiatorLarge = ese.getTo();
					}
					if(firstBox){
						if(direction){
							initiatorSmall = ese.getFrom();
							initiatorLarge = ese.getFrom() + 2;
						} else{
							terminatorSmall = ese.getFrom();
							terminatorLarge = ese.getFrom() + 2;
						}
						firstBox = false;
					}
				}
			} else if (t.equals(XML_TAG_DESCRIPTION)) {
				// Description
				this.description = e.getTextContent();
			} else if (t.equals(XML_TAG_COLOR)) {
				// Color
				this.color = e.getTextContent();
			}
		}
		
		if(!firstEqual){
			if(direction)
				initiatorSmall = initiatorLarge = -1;
			else
				terminatorSmall = terminatorLarge = -1;
		}
		if(!lastEqual){
			if(direction)
				terminatorSmall = terminatorLarge = Integer.MAX_VALUE;
			else
				initiatorSmall = initiatorLarge = Integer.MAX_VALUE;
		}
	}
	
	public String getId(){
		return this.id;
	}
	
	public int size(){
		return this.subEles.size();
	}
	///////////////////////////////////////////////////////////End of constructor

	//////////////////////////////////////////////////////////Predeal
	/**
	 * Deal structural variations and variations at ctrlAreas effect the Element
	 * and variations at ASS and ASS.
	 * 
	 * @param variants	All the variants need to be dealed.
	 * @throws IOException
	 */
	public void preDeal(List<Variant> variants) throws IOException {
		if(subEles == null || subEles.size() == 0){
			return ;
		}
		Map<Entry<EctypalSubElement>, String> tempAssDss = new HashMap<Entry<EctypalSubElement>, String>();
		Variant v = null;
		if (direction) {
			// Deal from the first variation
			Entry<EctypalSubElement> next = subEles.getFirst();
			for (int i = 0, num = variants.size(); i < num; i++) {
				v = variants.get(i);
				if(v.getTo() < next.getElement().getFrom())
					continue;
				next = dealAVariationPreDeal(v, tempAssDss,	next);
				if (next == null) break;
			}
		} else {
			// Deal from the last variation
			Entry<EctypalSubElement> pre = subEles.getLast();
			for (int i = variants.size() - 1; i >= 0; i--) {
				v = variants.get(i);
				if(v.getFrom() > pre.getElement().getTo())
					continue;
				pre = dealAVariationPreDeal(v, tempAssDss, pre);
				if (pre == null) break;
			}
		}
		if (!hasEffect || stillNeedToDeal){
			for (Entry<EctypalSubElement> ese : tempAssDss.keySet()) 
				ese.getElement().setType(tempAssDss.get(ese));
		}
	}
	
	/**
	 * Deal a variation in previous deal.
	 * @throws IOException
	 */
	private Entry<EctypalSubElement> dealAVariationPreDeal(Variant v, Map<Entry<EctypalSubElement>, String> tempAssDss,
			Entry<EctypalSubElement> cur) throws IOException {
		int _type = v.getType().hashCode();
		if(_type == hash_SNV || _type == hash_INS || _type == hash_DEL || _type == hash_CNV || _type == hash_DUP){
			if(direction && (v.getFrom() > cur.getElement().getTo()))
				cur = moveFromFrontToBack(v.getFrom(), cur, false);
			if(!direction && (v.getTo() < cur.getElement().getFrom()))
				cur = moveFromBackToFront(v.getTo(), cur, false);
			if(cur == null) return null;
		}
		if(_type == hash_SNV){
			if (cur.getElement().getType().equals(SUBELEMENT_TYPE_LINE))
				dealLineInPreDeal(v, 1, null, cur, tempAssDss);
			return cur;
		}
		if(_type == hash_INS){
			if (insEffectBox(v, cur)) return cur;
			if (cur.getElement().getType().equals(SUBELEMENT_TYPE_LINE))
				dealLineInPreDeal(v, 2, v.getLetter(), cur, tempAssDss);
			return cur;
		}
		if(_type == hash_BLS){
			if (v.getFrom() >= from && v.getFrom() <= to)
				recordStatus(v.getType(), false);
			return cur;
		} 
		// DEL, CNV, INV or DUP
		if(variantContainElement(v)){
			recordStatus(v.getType(), false);
			return cur;
		}
		if(_type == hash_DEL){
			DelMap2SubElement dms = map2SubEle(v, cur);
			if (dms != null && dms.boxBases > BOX_LIMIT * initBoxLen) {
				recordStatus(LARGE_VARIANTION, false);
				return cur;
			}
			dealLineInPreDeal(v, 3, null, 
					direction ? moveFromBackToFront(v.getFrom(), cur, false) : moveFromFrontToBack(v.getTo(), cur, false), tempAssDss);
			return cur;
		}
		if(_type == hash_INV){
			if (overlap(v.getFrom(), v.getTo(), from, to))
				recordStatus(LARGE_VARIANTION, false);
			return cur;
		}
		if(_type == hash_CNV || _type == hash_DUP){
			EctypalSubElement ese = cur.getElement();
			if (contained(v.getFrom(), v.getTo(), from, to) 
				&& !(ese.getType().equals(SUBELEMENT_TYPE_LINE) && contained(v.getFrom(), v.getTo(), ese.getFrom(), ese.getTo()))) {
				recordStatus(LARGE_VARIANTION, false);
				return cur;
			}
			return cur;
		}

		return cur;
	}
	
	/**
	 * If the variation type is "SNV" or "INS" or "DEL", and the variation is
	 * located at the SubElement which type is "Line", then we should deal it
	 * because it may cause ASS variation or DSS variation.
	 * 
	 * @param from
	 *            <code>from</code> = Variant.from
	 * @param to
	 *            <code>to</code> = Variant.to
	 * @param type
	 *            <code>type</code> = 1 when Variant.type="SNV" or
	 *            <code>type</code> = 2 when Variant.type="INS" or
	 *            <code>type</code> = 3 when Variant.type="DEL"
	 * @param basesOfINS
	 *            Bases of INS' Letter. Effective only when <code>type</code>=2.
	 * @param cur
	 * @param tempAssDss
	 * @throws IOException
	 */
	private void dealLineInPreDeal(Variant v, int type, String basesOfINS, Entry<EctypalSubElement> cur,
			Map<Entry<EctypalSubElement>, String> tempAssDss) throws IOException {
		int from = v.getFrom();
		int to = v.getTo();
		EctypalSubElement ese = cur.getElement();
		if(type == 1){
			if(!(from > ese.getFrom() + 1 && from < ese.getTo() - 1))
				recordTempAssDss(v, tempAssDss, cur, from <= ese.getFrom() + 1);
			return;
		}//End of SNV
		
		if(type == 2){
			if (from == ese.getFrom() && basesOfINS.charAt(0) != fr.extract_char(chr, to))
				recordTempAssDss(v, tempAssDss, cur, true);
			else if(to == ese.getTo() && basesOfINS.charAt(basesOfINS.length() - 1) != fr.extract_char(chr, from))
				recordTempAssDss(v, tempAssDss, cur, false);
			return;
		}//End of INS
		
		// Find the SubElement where from and to at respectively
		Entry<EctypalSubElement> fromCur = locate(from, cur);
		Entry<EctypalSubElement> toCur = locate(to, cur);
		if(fromCur == null){//from < this.from
			fromCur = subEles.getFirst();
			from = this.from;
		}
		if (toCur == null){//to > this.to
			toCur = subEles.getLast();
			to = this.to;
		}
		dealDELASSOrDSS(v, fromCur, toCur, tempAssDss);
	}
	
	private void dealDELASSOrDSS(Variant v, Entry<EctypalSubElement> fromCur, Entry<EctypalSubElement> toCur,
			Map<Entry<EctypalSubElement>, String> tempAssDss) {
		boolean isFromTypeLine = fromCur.getElement().getType().equals(SUBELEMENT_TYPE_LINE);
		boolean isToTypeLine = toCur.getElement().getType().equals(SUBELEMENT_TYPE_LINE);
		int from = v.getFrom();
		int to = v.getTo();
		if (isFromTypeLine) {
			if (isToTypeLine) {
				//Line to Line
				boolean isFromFS = (from <= fromCur.getElement().getFrom() + 1);
				boolean isToLP = (to >= toCur.getElement().getTo() - 1);
				if(isFromFS && !isToLP){
					/*
					 * 1111|||||-----||||-----|||||-----||||||11111
	        		 *	        |===================|
					 */
					recordTempAssDss(v, tempAssDss, direction ? toCur : fromCur, true);
				}
				else if(!isFromFS && isToLP){
					/*
					 * 1111|||||-----||||-----|||||-----||||||11111
	        		 *	            |==================|
					 */
					recordTempAssDss(v, tempAssDss, direction ? toCur : fromCur, false);
				}
			} else {
				//Line to Box|Band
				/*
				 * 1111|||||-----||||-----|||||-----||||||11111
        		 *	            |=====================|
				 */
				if(from > fromCur.getElement().getFrom() + 1){
					recordTempAssDss(v, tempAssDss, direction ? subEles.getPrevious(toCur) : fromCur, false);
				}
				//Don't need to deal if from==fromEse.getFrom()
				//or from==fromEse.getFrom()+1
				/*
				 * 1111|||||-----||||-----|||||-----||||||11111
        		 *	        |=========================|
				 */
			}
		} else {
			if (isToTypeLine) {
				// Box|Band to Line
				/*
				 * 1111|||||-----||||-----|||||-----||||||11111
        		 *	     |======================|
				 */
				if(to < toCur.getElement().getTo() - 1){
					recordTempAssDss(v, tempAssDss, direction?toCur:subEles.getNext(fromCur), true);
				}
				//Don't need to deal if to==toEse.getTo()-1
				//or to==toEse.getTo()
				/*
				 * 1111|||||-----||||-----|||||-----||||||11111
        		 *	      |========================|
				 */
			} 
			// Don't need to deal Box|Band to Box|Band
			/*
			 * 1111|||||-----||||-----|||||-----||||||11111
    		 *	     |============================|
			 */
		}
	}

	/**
	 * Record an ASS or DSS.
	 * @param tempAssDss
	 * @param cur
	 * @param isFS	If the variation effect the Line's first or second base, <code>isFS=true</code>, 
	 *            	false if the variation effect the Line's last or penult base.
	 */
	private void recordTempAssDss(Variant v, Map<Entry<EctypalSubElement>, String> tempAssDss, Entry<EctypalSubElement> cur, boolean isFS) {
		Entry<EctypalSubElement> e = (isFS ? subEles.getPrevious(cur) : subEles.getNext(cur));
		if (e != null) {
			//we should add variant into the Box the variant affect.
			e.getElement().addMultiFromVariant(v.getId(), v.getType(), new int[]{ v.getFrom() }, new int[]{ v.getTo() }, 
					isFS == direction ? "(" : ")");
			if(hasEffect){
				//For hasEffect==true, we should change the type of the Box the variant affect.
				int small = (direction ? initiatorSmall : terminatorSmall);
				int large = (direction ? terminatorLarge : initiatorLarge);
				if((isFS && e.getElement().getTo() == large) || (!isFS && e.getElement().getFrom() == small))
					return;
				boolean isBox = e.getElement().getType().equals(SUBELEMENT_TYPE_BOX);
				String isUp = isBox ? SUBELEMENT_TYPE_EXTEND_BOX : SUBELEMENT_TYPE_EXTEND_BAND;
				String isDown = isBox ? SUBELEMENT_TYPE_SKIP_BOX : SUBELEMENT_TYPE_SKIP_BAND;
				tempAssDss.put(e, isFS ? (direction ? isUp : isDown) : (direction ? isDown : isUp));	
			}
		}
	}

	/**
	 * Whether INS effect BOX and INS.Letter.length > <code>BOX_LIMIT</code> * <code>initBoxLen</code>
	 * @return
	 */
	private boolean insEffectBox(Variant v, Entry<EctypalSubElement> cur) {
		EctypalSubElement ese = cur.getElement();
		if (ese.getType().equals(SUBELEMENT_TYPE_BOX) && v.getLetter().length() > BOX_LIMIT * initBoxLen) {
			return recordStatus(LARGE_VARIANTION, false);
		} 
		if (ese.getType().equals(SUBELEMENT_TYPE_LINE)) {
			Entry<EctypalSubElement> temp = direction ? subEles.getNext(cur) : subEles.getPrevious(cur);
			if (temp != null && temp.getElement().getType().equals(SUBELEMENT_TYPE_BOX)
					&& ((direction && v.getFrom() == ese.getTo()) || (!direction && v.getTo() == ese.getFrom()))
					&& v.getLetter().length() > BOX_LIMIT * initBoxLen) {
				return recordStatus(LARGE_VARIANTION, false);
			}
		}
		return false;
	}

	/**
	 * Record the <code>status</code> and <code>stillNeedToDeal</code>.
	 * @return 
	 */
	private boolean recordStatus(String status, boolean stillNeedToDeal) {
		this.status.add(status);
		this.stillNeedToDeal = stillNeedToDeal;
		return true;
	}
	
	/**
	 * Map deletion variant to SubElements.
	 * @return Number of bases of DEL in Boxes
	 */
	private DelMap2SubElement map2SubEle(Variant del, Entry<EctypalSubElement> cur) {
		Entry<EctypalSubElement> fromCur = locate(del.getFrom(), cur);
		Entry<EctypalSubElement> toCur = locate(del.getTo(), fromCur);
		if(fromCur == null) fromCur = subEles.getFirst();
		if(toCur == null) toCur = subEles.getLast();
		return new DelMap2SubElement(del, fromCur, toCur, subEles, hasEffect);
	}
	
	/*
	 * Locate the pos to an SubElement. Return the SubElement if we found it, null else.
	 */
	private Entry<EctypalSubElement> locate(int pos, Entry<EctypalSubElement> cur){
		if(pos < this.from || pos > this.to || cur == null)
			return null;
		if(pos < cur.getElement().getFrom())
			return moveFromBackToFront(pos, cur, false);
		if(pos > cur.getElement().getTo())
			return moveFromFrontToBack(pos, cur, false);
		return cur;
	}
	
	private boolean variantContainElement(Variant v) {
		int _from = from - (direction ? 2000 : 500);
		if(_from < 1) _from = 1;
		int _to = to + (direction ? 500 : 2000);
		if(_to > chrLen) _to = chrLen;
		return contained(_from, _to, v.getFrom(), v.getTo());
	}

	/**
	 * @param cas	You must sure that <code>cas!=null</code> and <code>cas.size()&gt;0</code>.
	 */
	public void dealCtrlAreas(List<ControlArea> cas) {
		int _from = from - (direction ? 2000 : 500);
		int _to = to + (direction ? 500 : 2000);
		for(ControlArea ca : cas){
			if(ca.to < _from) continue;
			if(ca.from > _to) break;
			status.add(ca.id);
		}
	}
	//////////////////////////////////////////////////////////End of predeal
	
	//////////////////////////////////////////////////////////Deal
	public void deal(List<Variant> variants) throws IOException{
		if(initBoxLen == 0) return;
		Variant v = null;
		int typeHash = 0;
		Integer curNeedToDealType = null;
		dealedVariations = new LinkedArrayList<DealedVariation>();
		if (direction) {
			// direction=true, deal from the first variation
			Entry<EctypalSubElement> next = subEles.getFirst();
			for (int i = 0, num = variants.size(); i < num; i++) {
				if(next == null) break;
				v = variants.get(i);
				if(v.getTo() < next.getElement().getFrom()) 
					continue;
				if(v.getFrom() > next.getElement().getTo()){
					next = moveFromFrontToBack(v.getFrom(), next, true);
					if(next == null) break;
				}
				////////////Now next is the SubElement where v.getFrom() at
				typeHash = v.getType().hashCode();
				curNeedToDealType = hasEffect ? currentNeedToDeal.get(next.getElement().getType()) 
						: (notLineNotBand(next.getElement().getType()) ? currentNeedToDeal.get(SUBELEMENT_TYPE_BOX) : null);
				if(typeHash == hash_SNV && curNeedToDealType != null){
					if(v.getLetter() == null)
						continue;
					if(curNeedToDealType <= 2){
						next = dealASNV(next, v);
					}
				}else if(typeHash == hash_INS){
					if(v.getLetter() == null)
						continue;
					if(null == curNeedToDealType){
						if(v.getFrom() == next.getElement().getTo()){
							//The from of the variant equals the to of the current SubElement
							next = moveFromFrontToBack(v.getTo(), next, true);
							if(next == null) break;
							curNeedToDealType = hasEffect ? currentNeedToDeal.get(next.getElement().getType()) 
									: (notLineNotBand(next.getElement().getType()) ? currentNeedToDeal.get(SUBELEMENT_TYPE_BOX) : null);
							if(curNeedToDealType == null)
								continue;
						}else{
							continue;
						}
					}
					next = dealAINS(next, v, curNeedToDealType);
				}else if(typeHash == hash_DEL){
					DelMap2SubElement dms = map2SubEle(v, next);
					if(dms == null || dms.boxBases == 0)
						continue;
					next = dealADEL(dms);
				}
			}
			
			if(next != null)
				next = moveFromFrontToBack(this.to + 1, next, true);
			return;
		}/////////////////////////////////////////////////////////////End of direction=true
		// direction=false, deal from the last variation
		Entry<EctypalSubElement> pre = subEles.getLast();
		for (int i = variants.size() - 1; i >= 0; i--) {
			if(pre == null) break;
			v = variants.get(i);
			if(v.getFrom() > pre.getElement().getTo())
				continue;
			if(v.getTo() < pre.getElement().getFrom()){
				pre = moveFromBackToFront(v.getTo(), pre, true);
				if(pre == null) break;
			}
			////////////Now pre is the SubElement where v.getTo() at
			typeHash = v.getType().hashCode();
			curNeedToDealType = hasEffect ? currentNeedToDeal.get(pre.getElement().getType()) 
					: (notLineNotBand(pre.getElement().getType()) ? currentNeedToDeal.get(SUBELEMENT_TYPE_BOX) : null);
			if(typeHash == hash_SNV && curNeedToDealType != null){
				if(v.getLetter() == null)
					continue;
				if(curNeedToDealType <= 2){
					pre = dealASNV(pre, v);
				}
			}else if(typeHash == hash_INS){
				if(v.getLetter() == null)
					continue;
				if(null == curNeedToDealType){
					if(v.getTo() == pre.getElement().getFrom()){
						//The to of the variant equals the from of the current SubElement
						pre = moveFromBackToFront(v.getFrom(), pre, true);
						if(pre == null) break;
						curNeedToDealType = hasEffect ? currentNeedToDeal.get(pre.getElement().getType()) 
								: (notLineNotBand(pre.getElement().getType()) ? currentNeedToDeal.get(SUBELEMENT_TYPE_BOX) : null);
						if(curNeedToDealType == null)
							continue;
					}else{
						continue;
					}
				}
				pre = dealAINS(pre, v, curNeedToDealType);
			}else if(typeHash == hash_DEL){
				DelMap2SubElement dms = map2SubEle(v, pre);
				if(dms == null || dms.boxBases == 0)
					continue;
				pre = dealADEL(dms);
			}
		}
		
		if(pre != null)
			moveFromBackToFront(this.from - 1, pre, true);
	}
	
	/*
	 * Deal a SNV variation in the deal stage. 
	 * For direction=true, the cur is the SubElement where v.getFrom() at; for direction=false, the cur is the SubElement where v.getTo() at. 
	 * If hasEffect=true, the type of cur must be "Box" or "Extend_Box", but for hasEffect=false, the type of cur may be one of the type 
	 * which is not "Line" and "Band". 
	 */
	private Entry<EctypalSubElement> dealASNV(Entry<EctypalSubElement> cur, Variant v) throws IOException{
		int vFromAtThisBox = direction ? (v.getFrom() - cur.getElement().getFrom() + 1) : (cur.getElement().getTo() - v.getTo() + 1);
		int remain = (boxBaseNumFromFirstBoxBase + vFromAtThisBox) % 3;
		
		Extract3Bases result = extractFromFasta(cur, v.getFrom(), extractIndex[direction?0:1][0][remain][0], extractIndex[direction?0:1][0][remain][1]);   
		if (result == null) return cur;
		char oldTranscription = standardGeneticCode.get(result.sequence);
		result.sequence = replaceChar(result.sequence, v.getLetter().charAt(0), remain==0 ? 2 : (remain==1 ? 0 : 1), direction);
		// Now we have get the sequence from the fasta file.
		char transcription = standardGeneticCode.get(result.sequence);
		//Record this variation in SubElement
		recordVariant(result, v, oldTranscription + ":" + transcription, cur.getElement());
		
		if(!hasEffect)
			return cur;
		
		dealedVariations.addLast(new DealedVariation(v));

		if(v.getFrom() >= initiatorSmall && v.getTo() <= initiatorLarge){//The variant appeared in the initiator and must change the initiator
			String changeType = cur.getElement().getType().equals(SUBELEMENT_TYPE_BOX) ? SUBELEMENT_TYPE_LOST_BOX : SUBELEMENT_TYPE_EXTEND_BAND;
			cur.getElement().setType(changeType);
			changeInitiator();
			return cur;
		}
		
		if(v.getFrom() >= terminatorSmall && v.getTo() <= terminatorLarge && transcription != '$'){
			//The variant appeared in the terminator and change the terminator
			cur.getElement().setType(SUBELEMENT_TYPE_EXTEND_BOX);
			return direction ? subEles.getNext(cur) : subEles.getPrevious(cur);
		}
		
		if(transcription == '$'){//The variant appeared not in the initiator and the terminator and terminator has appeared
			int resultIndex = direction ? 2 : 0;
			String changeType = SUBELEMENT_TYPE_BOX.equals(result.pss[resultIndex].subEle.getElement().getType()) 
														? SUBELEMENT_TYPE_LOST_BOX : SUBELEMENT_TYPE_EXTEND_BAND;
			return cutWhenAffect(result.pss[resultIndex].subEle, result.pss[resultIndex].position, SUBELEMENT_TYPE_BOX, changeType);
		}
		//ordinary variant, not in the initiator, not change the terminator, not cause the terminator
		return cur;
	}
	
	/*
	 * deal a INS variant in the deal stage. 
	 * If hasEffect=true, the type of cur must be "Box" or "Extend_Box" or "Shift_Box" or "Shift_Extend_Box", but for hasEffect=false, 
	 * the type of cur may be one of the type which is not "Line" and "Band".
	 * For direction=true, the cur is the SubElement where v.getFrom() at or where v.getTo() at when v.getTo()==cur.getFrom(); 
	 * for direction=false, the cur is the SubElement where v.getTo() at or where v.getFrom() at when v.getFrom()==cur.getTo().
	 */
	private Entry<EctypalSubElement> dealAINS(Entry<EctypalSubElement> cur, Variant v, Integer curNeedToDealType) throws IOException{
		int vFromAtThisBox = direction ? (v.getFrom() - cur.getElement().getFrom() + 1) : (cur.getElement().getTo() - v.getTo() + 1);
		int remain = (boxBaseNumFromFirstBoxBase + vFromAtThisBox) % 3;
		if(remain == 0) remain = 3;
		Extract3Bases e3b = null;
		String trans = null;

		if(hasEffect)
			boxBaseNumDealed += v.getLetter().length();
		if(remain != 3){
			int _index = direction ? 0 : 1;
			e3b = extractFromFasta(cur, direction ? v.getFrom() : v.getTo(), extractIndex[_index][1][remain][0], extractIndex[_index][1][remain][1]);
			if(e3b == null) return cur;
		}
		//record the variant
		if(v.getLetter().length() % 3 == 0){
			if(!direction)//If direction="-", we should inverse and complement the letter first.
				v.setLetter(InverseAndComplement(v.getLetter()));
			if(remain != 3)
				v.setLetter(e3b.sequence.substring(0, remain).concat(v.getLetter()).concat(e3b.sequence.substring(remain)));
			String after = bases2gene(v.getLetter());
			trans = remain != 3 ? bases2gene(e3b.sequence) : "_";
			if(remain != 3)
				recordVariant(e3b, v, trans + ":" + after, cur.getElement());
			else
				cur.getElement().addMultiFromVariant(v.getId(), v.getType(), new int[]{v.getFrom()}, new int[]{v.getTo()}, trans + ":" + after);
		}else{
			cur.getElement().addMultiFromVariant(v.getId(), v.getType(), new int[]{v.getFrom()}, new int[]{v.getTo()}, "#");
		}
		
		if(!hasEffect)
			return cur;

		dealedVariations.addLast(new DealedVariation(v));
		
		if(curNeedToDealType <= 2){//The current SubElement is Box or Extend_Box
			if(v.getLetter().length() % 3 == 0){//The length of insert bases is a multiple of 3
				// now we have record the variant
				if((direction && v.getTo() <= initiatorLarge) || (!direction && v.getFrom() >= initiatorSmall)){
					//The variant appeared in the initiator
					boolean initiatorLost = false;
					if(trans.indexOf("M") != 0){//The initiator has lost
						String changeType = curNeedToDealType == 1 ? SUBELEMENT_TYPE_LOST_BOX : SUBELEMENT_TYPE_EXTEND_BAND;
						cur.getElement().setType(changeType);
						initiatorLost = true;
					}
					if(!initiatorLost){
						//The initiator hasn't lost
						if(trans.indexOf("$") > 0){//The terminator appeared
							String changeType = curNeedToDealType == 1 ? SUBELEMENT_TYPE_LOST_BOX : SUBELEMENT_TYPE_EXTEND_BAND;
							if((direction && v.getTo() == initiatorSmall) || (!direction && v.getFrom() == initiatorLarge)){
								cur.getElement().setType(changeType);
							}else{
								cur = cutWhenAffect(cur, direction?initiatorLarge:initiatorSmall, SUBELEMENT_TYPE_BOX, changeType);
							}
						}
					}
					changeInitiator();
					return cur;
				}
				//End of v.getFrom() >= initiatorSmall && v.getTo() <= initiatorLarge
				if(v.getFrom() >= terminatorSmall && v.getTo() <= terminatorLarge){//The variant appeared in the terminator
					if(!trans.contains("$")){//The terminator has lost
						cur.getElement().setType(SUBELEMENT_TYPE_EXTEND_BOX);
						return direction ? subEles.getNext(cur) : subEles.getPrevious(cur);
					}
					return cur;
				}
				//End of (direction && v.getTo() <= initiatorLarge) || (!direction && v.getFrom() >= initiatorSmall)
				//Now the variant appeared not in the initiator and the terminator
				if(trans.contains("$")){//The terminator appeared
					int resultIndex = direction ? 2 : 0;
					String str = e3b!=null ? e3b.pss[resultIndex].subEle.getElement().getType() : cur.getElement().getType();
					String changeType = SUBELEMENT_TYPE_BOX.equals(str) ? SUBELEMENT_TYPE_LOST_BOX : SUBELEMENT_TYPE_EXTEND_BAND;
					return (e3b == null) ? cutWhenAffect(cur, direction ? v.getFrom() : v.getTo(), SUBELEMENT_TYPE_BOX, changeType)
							: cutWhenAffect(e3b.pss[resultIndex].subEle, e3b.pss[resultIndex].position, SUBELEMENT_TYPE_BOX, changeType);
				}
				//Not in the initiator, not in the terminator, no terminator appeared
				return cur;
				// End of letters.length() % 3 == 0
			}
			
			//The length of insert bases is not a multiple of 3
			if((direction && v.getTo() <= initiatorLarge) || (!direction && v.getFrom() >= initiatorSmall)){//The variant appeared in the initiator
				//The initiator has lost
				String changeType = curNeedToDealType == 1 ? SUBELEMENT_TYPE_LOST_BOX : SUBELEMENT_TYPE_EXTEND_BAND;
				cur.getElement().setType(changeType);
				return cur;
			}
			//End of v.getFrom() >= initiatorSmall && v.getTo() <= initiatorLarge
			if(v.getFrom() >= terminatorSmall && v.getTo() <= terminatorLarge){//The variant appeared in the terminator
				//The terminator has lost
				cur.getElement().setType(SUBELEMENT_TYPE_EXTEND_BOX);
				return direction ? subEles.getNext(cur) : subEles.getPrevious(cur);
			}
			//End of v.getFrom() >= terminatorSmall && v.getTo() <= terminatorLarge
			//Now the variant appeared not in the initiator and the terminator
			int resultIndex = direction ? 2 : 0;
			String str = e3b!=null ? e3b.pss[resultIndex].subEle.getElement().getType() : cur.getElement().getType();
			String changeType = SUBELEMENT_TYPE_BOX.equals(str) ? SUBELEMENT_TYPE_SHIFT_BOX : SUBELEMENT_TYPE_SHIFT_EXTEND_BOX;
			return e3b == null ? cutWhenAffect(cur, direction ? v.getFrom() : v.getTo(), SUBELEMENT_TYPE_BOX, changeType)
					: cutWhenAffect(e3b.pss[resultIndex].subEle, e3b.pss[resultIndex].position, SUBELEMENT_TYPE_BOX, changeType);
		}
		//End of curNeedToDealType <= 2, now the current SubElement is Shift_Box or Shift_Extend_Box
		if(boxBaseNumDealed % 3 == 0){
			int resultIndex = direction ? 2 : 0;
			String str = e3b!=null ? e3b.pss[resultIndex].subEle.getElement().getType() : cur.getElement().getType();
			String changeType = SUBELEMENT_TYPE_SHIFT_BOX.equals(str) ? SUBELEMENT_TYPE_BOX : SUBELEMENT_TYPE_EXTEND_BOX;
			return e3b == null ? cutWhenAffect(cur, direction ? v.getFrom() : v.getTo(), cur.getElement().getType(), changeType)
					: cutWhenAffect(e3b.pss[resultIndex].subEle, e3b.pss[resultIndex].position, e3b.pss[resultIndex].subEle.getElement().getType(), changeType);
		}
		return cur;
	}

	/*
	 * deal a DEL variant when direction=true.
	 */
	private Entry<EctypalSubElement> dealADEL(DelMap2SubElement dms) throws IOException{
		int remain = (boxBaseNumFromFirstBoxBase + dms.countDistanceToTheEdge(direction)) % 3;
		Extract3Bases e3b = null;
		int resultIndex = direction ? 2 : 0;
		Variant del = dms.deletion;
		
		if(hasEffect){
			boxBaseNumDealed -= dms.boxBases;
			if(dms.boxBases % 3 != 0){
				int add = dms.changePos(direction, remain);
				if(add < 0)
					return dms.getUpstreamSubEle(direction);
				boxBaseNumFromFirstBoxBase += add;
				remain = 1;
			}
		}
		if(remain != 1){
			e3b = Extract3Bases.merge(
					extractFromFasta(dms.firstSubEle, dms.realFrom-1, extractIndex[direction?0:1][2][remain][0], 0), 
					extractFromFasta(dms.lastSubEle, dms.realTo+1, 0, extractIndex[direction?0:1][2][remain][1]), 
					direction);
			if(e3b == null) return dms.getUpstreamSubEle(direction);
		}
		
		Entry<EctypalSubElement> upSubEle = (e3b==null) ? dms.getUpstreamSubEle(direction) : (e3b.pss[direction ? 0 : 2].subEle);
		Entry<EctypalSubElement> downSubEle = (e3b==null) ? dms.getDownstreamSubEle(direction) : (e3b.pss[resultIndex].subEle);
		int downPos = (e3b==null) ? (dms.getDownstreamPos(direction)) : (e3b.pss[resultIndex].position);

		//record the variant
		if(dms.boxBases % 3 == 0){
			String delBases = direction ? dms.delBases(fr,chr) : InverseAndComplement(dms.delBases(fr,chr));
			if(remain != 1)
				delBases = e3b.sequence.substring(0, (remain + 2) % 3).concat(delBases).concat(e3b.sequence.substring((remain + 2) % 3));
			String pre = bases2gene(delBases);
			String after = remain != 1 ? bases2gene(e3b.sequence) : "_";
			if(remain != 1)
				recordVariant(e3b, del, pre + ":" + after, upSubEle.getElement());
			else
				upSubEle.getElement().addMultiFromVariant(del.getId(), del.getType(), new int[]{del.getFrom()}, new int[]{del.getTo()}, pre + ":" + after);
		}else{
			upSubEle.getElement().addMultiFromVariant(del.getId(), del.getType(), new int[]{del.getFrom()}, new int[]{del.getTo()}, "#");
		}
		
		if(!hasEffect)
			return upSubEle;
		
		dealedVariations.addLast(new DealedVariation(dms.deletion));
		if(currentNeedToDeal.get(dms.firstSubEle.getElement().getType()) <= 2){
			//The upstream SubElement is BOX or EXTEND_BOX
			if(overlap(initiatorSmall, initiatorLarge, dms.realFrom, dms.realTo)){
				//The DEL variant overlaps with the initiator, so the initiator has lost
				String changeType = SUBELEMENT_TYPE_BOX.equals(downSubEle.getElement().getType()) ? SUBELEMENT_TYPE_LOST_BOX : SUBELEMENT_TYPE_EXTEND_BAND;
				upSubEle.getElement().setType(changeType);
				changeInitiator();
				return upSubEle;
			}
			
			if(overlap(terminatorSmall, terminatorLarge, dms.realFrom, dms.realTo)){//The DEL variant overlaps with the terminator
				if(dms.boxBases %3 != 0 || e3b == null || !bases2gene(e3b.sequence).contains("$")){//The initiator has lost
					downSubEle.getElement().setType(SUBELEMENT_TYPE_EXTEND_BOX);
					return direction ? subEles.getNext(downSubEle) : subEles.getPrevious(downSubEle);
				}
				return upSubEle;
			}
			//Now the DEL variant not overlaps with the initiator and terminator
			if(dms.boxBases % 3 == 0 && e3b != null && bases2gene(e3b.sequence).contains("$")){
				//The length of delete bases is a multiple of 3 and terminator appeared
				String changeType = SUBELEMENT_TYPE_BOX.equals(downSubEle.getElement().getType()) ? SUBELEMENT_TYPE_LOST_BOX : SUBELEMENT_TYPE_EXTEND_BAND;
				return cutWhenAffect(downSubEle, downPos, SUBELEMENT_TYPE_BOX, changeType);
			}
			
			if(dms.boxBases % 3 != 0){
				String changeType = SUBELEMENT_TYPE_BOX.equals(downSubEle.getElement().getType()) ? SUBELEMENT_TYPE_SHIFT_BOX : SUBELEMENT_TYPE_SHIFT_EXTEND_BOX;
				return cutWhenAffect(downSubEle, downPos, SUBELEMENT_TYPE_BOX, changeType);
			}
			
			return upSubEle;
			//End of firstType <= 2
		}
		//Now the upstream SubElement is SHIFT_BOX or SHIFT_EXTEND_BOX
		if(boxBaseNumDealed % 3 == 0){
			String changeType = SUBELEMENT_TYPE_SHIFT_BOX.equals(downSubEle.getElement().getType()) ? SUBELEMENT_TYPE_BOX : SUBELEMENT_TYPE_EXTEND_BOX;
			return cutWhenAffect(downSubEle, downPos, downSubEle.getElement().getType(), changeType);
		}
		
		return upSubEle;
	}
	
	private void changeInitiator(){
		if(direction)
			initiatorLarge = Integer.MIN_VALUE;
		else
			initiatorSmall = Integer.MAX_VALUE;
	}
	
	private void recordVariant(Extract3Bases e3b, Variant v, String transcription, EctypalSubElement cur){
		boolean fstSndContinuous = Position2SubElement.isContinuous(e3b.pss[0], e3b.pss[1]);
		boolean sndRdContinuous = Position2SubElement.isContinuous(e3b.pss[1], e3b.pss[2]);
		int[] from = null;
		int[] to = null;
		if(fstSndContinuous){
			if(sndRdContinuous){
				// the three bases are all continuous
				from = new int[]{e3b.pss[0].position};
				to = new int[]{e3b.pss[2].position};
			}else{
				// the first and the second bases are continuous
				from = new int[]{e3b.pss[0].position, e3b.pss[2].position};
				to = new int[]{e3b.pss[1].position, e3b.pss[2].position};
			}
		}else{
			if(sndRdContinuous){
				// the second and the third bases are continuous
				from = new int[]{e3b.pss[0].position, e3b.pss[1].position};
				to = new int[]{e3b.pss[0].position, e3b.pss[2].position};
			}else{
				// the three bases are all not continuous
				from = new int[]{e3b.pss[0].position, e3b.pss[1].position, e3b.pss[2].position};
				to = new int[]{e3b.pss[0].position, e3b.pss[1].position, e3b.pss[2].position};
			}
		}
		cur.addMultiFromVariant(v.getId(), v.getType(), from, to, transcription);
	}
	
	/**
	 * Extract suquence from fasta file, bases at <code>absolutePos</code>, and extract 
	 * abs(<code>relativeFrom</code>) number of bases in the "BOX" before the <code>absolutePos</code>
	 * and <code>relativeTo</code> number of bases in the "BOX" after the <code>absolutePos</code>. All
	 * bases we extract must be at the "BOX". Remember that we just need to extract three bases.
	 * <strong>Note: </strong>You must ensure that <code>relativeFrom</code> &lt;= 0 &lt;= 
	 * <code>relativeTo</code>, and <code>absolutePos</code> is at <code>cur</code>.
	 * And because the upstream's variation has an impact on the downstream's variation, we must take the
	 * upstream's variations into account.
	 * @param cur	
	 * 			  The current SubElement we deal.
	 * @param absolutePos
	 *            Absolute base position of the chromosome. You must ensure that <code>absolutePos</code>
	 *            is at <code>cur</code>.
	 * @param relativeFrom
	 *            The <code>relativeFrom</code> must &lt;= 0. It means we should
	 *            extract abs(<code>relativeFrom</code>) number of bases in the
	 *            "BOX" before the <code>absolutePos</code>.
	 * @param relativeTo
	 *            The <code>relativeTo</code> must &gt;= 0. It means we should
	 *            extract <code>relativeTo</code> number of bases in the
	 *            "BOX" after the <code>absolutePos</code>.
	 * @return The sequence (packaged in <code>Extract3Bases</code>) extract from fasta file of the given
	 * region in the "BOX". If less than abs(<code>relativeFrom</code>) bases from "BOX" before <code>absolutePos</code>
	 * or less than <code>relativeTo</code> bases from "BOX" after <code>absolutePos</code>, null will be returned.
	 * And if <code>direction==false</code>, the sequence will be inversed and complemented before it is returned.
	 * @throws IOException 
	 */
	private Extract3Bases extractFromFasta(Entry<EctypalSubElement> cur, int absolutePos, int relativeFrom, int relativeTo) 
			throws IOException {
		int extractNum = relativeTo - relativeFrom + 1;
		Extract3Bases result = new Extract3Bases(extractNum);
		char[] resultChars = new char[extractNum];
		Entry<DealedVariation> dvEntry = dealedVariations.getLast();
		int curPos = absolutePos;
		int needToDealNumPre = -relativeFrom + (direction ? 1 : 0);
		int needToDealNumAfter = relativeTo + (direction ? 0 : 1);
		Entry<EctypalSubElement> copy = cur;
		boolean effected = false;
		
		if(direction){
			while(needToDealNumPre > 0){
				if(curPos < copy.getElement().getFrom()){
					copy = getPreviousBox(subEles, copy, hasEffect);
					if(copy == null) return null;
					curPos = copy.getElement().getTo();
				}
				if(hasEffect && dvEntry != null){
					String dvType = dvEntry.getElement().type;
					if(dvType.equals(VARIANT_TYPE_SNV) && dvEntry.getElement().to == curPos){
						resultChars[needToDealNumPre - 1] = dvEntry.getElement().letter.charAt(0);
						result.pss[needToDealNumPre - 1] = new Position2SubElement(curPos, copy);
						needToDealNumPre--;
						curPos--;
						effected = true;
					}else if(dvType.equals(VARIANT_TYPE_INSERTION) && curPos < dvEntry.getElement().to){
						String letter = dvEntry.getElement().letter;
						int len = 0;
						while(needToDealNumPre > 0 && letter.length() > len){
							resultChars[needToDealNumPre - 1] = letter.charAt(letter.length() - len - 1);
							result.pss[needToDealNumPre - 1] = new Position2SubElement(dvEntry.getElement().to, copy);
							needToDealNumPre--;
							len++;
						}
						effected = true;
					} else if(dvType.equals(VARIANT_TYPE_DELETION) && curPos <= dvEntry.getElement().to){
						curPos = dvEntry.getElement().from - 1;
						effected = true;
					}
				}
				if(effected){
					dvEntry = dealedVariations.getPrevious(dvEntry);
					effected = false;
				}else{
					resultChars[needToDealNumPre - 1] = fr.extract_char(chr, curPos);
					result.pss[needToDealNumPre - 1] = new Position2SubElement(curPos, copy);
					needToDealNumPre--;
					curPos--;
				}
			}
			//End of needToDealNumPre > 0
			curPos = absolutePos + 1;
			while(needToDealNumAfter > 0){
				if(curPos > cur.getElement().getTo()){
					cur = getNextBox(subEles, cur, hasEffect);
					if(cur == null) return null;
					curPos = cur.getElement().getFrom();
				}
				resultChars[extractNum - needToDealNumAfter] = fr.extract_char(chr, curPos);
				result.pss[extractNum - needToDealNumAfter] = new Position2SubElement(curPos, cur);
				needToDealNumAfter--;
				curPos++;
			}
			
			result.sequence= new String(resultChars);
			return result;
		}
		
		//End of direction=true
		//direction = false
		while(needToDealNumAfter > 0){
			if(curPos > copy.getElement().getTo()){
				copy = getNextBox(subEles, copy, hasEffect);
				if(copy == null) return null;
				curPos = copy.getElement().getFrom();
			}
			if(hasEffect && dvEntry != null){
				String dvType = dvEntry.getElement().type;
				if(dvType.equals(VARIANT_TYPE_SNV) && dvEntry.getElement().from == curPos){
					resultChars[extractNum - needToDealNumAfter] = dvEntry.getElement().letter.charAt(0);
					result.pss[extractNum - needToDealNumAfter] = new Position2SubElement(curPos, copy);
					needToDealNumAfter--;
					curPos++;
					effected = true;
				}else if(dvType.equals(VARIANT_TYPE_INSERTION) && curPos > dvEntry.getElement().from){
					String letter = dvEntry.getElement().letter;
					int len = 0;
					while(needToDealNumAfter > 0 && letter.length() > len){
						resultChars[extractNum - needToDealNumAfter] = letter.charAt(len);
						result.pss[extractNum - needToDealNumAfter] = new Position2SubElement(dvEntry.getElement().from, copy);
						needToDealNumAfter--;
						len++;
					}
					effected = true;
				} else if(dvType.equals(VARIANT_TYPE_DELETION) && curPos >= dvEntry.getElement().from){
					curPos = dvEntry.getElement().to + 1;
					effected = true;
				}
			}
			if(effected){
				dvEntry = dealedVariations.getPrevious(dvEntry);
				effected = false;
			}else{
				resultChars[extractNum - needToDealNumAfter] = fr.extract_char(chr, curPos);
				result.pss[extractNum - needToDealNumAfter] = new Position2SubElement(curPos, copy);
				needToDealNumAfter--;
				curPos++;
			}
		}
		curPos = absolutePos - 1;
		while(needToDealNumPre > 0){
			if(curPos < cur.getElement().getFrom()){
				cur = getPreviousBox(subEles, cur, hasEffect);
				if(cur == null) return null;
				curPos = cur.getElement().getTo();
			}
			resultChars[needToDealNumPre - 1] = fr.extract_char(chr, curPos);
			result.pss[needToDealNumPre - 1] = new Position2SubElement(curPos, cur);
			needToDealNumPre--;
			curPos--;
		}
		
		result.sequence = InverseAndComplement(resultChars);
		return result;
	}
	//////////////////////////////////////////////////////////End of deal
	
	////////////////////////////////////////////////////////////Other functions
	/**
	 * This method is used for move the current SubElement to another SubElement which contain
	 * the position: <code>pos</code> from front to back.
	 * @param pos
	 * @param cur
	 * @param dealStage True if now is in the deal stage, false if now is in the predeal stage.
	 * 					if <code>dealStage==true</code>, We should add the number of bases at the 
	 * 					SubElement which type is Box. And if the upstream SubElement variant has 
	 * 					an impact on the downstream SubElement, we should change the downstream 
	 * 					variant's type.
	 * @return Return the <code>Entry&lt;EctypalSubElement&gt;</code> which contain the position: <code>pos</code>.
	 * 			Return null if not this <code>Entry&lt;EctypalSubElement&gt;</code> exists.
	 */
	private Entry<EctypalSubElement> moveFromFrontToBack(int pos, Entry<EctypalSubElement> cur, boolean dealStage){
		while (pos > cur.getElement().getTo()) {
			if(dealStage){
				if(hasEffect && notLineNotBand(cur.getElement().getType()))
					preSubEleNotLineNotBand = cur.getElement();
				if((hasEffect && currentNeedToDeal.containsKey(cur.getElement().getType())) || (!hasEffect && notLineNotBand(cur.getElement().getType())))
					addBoxBaseNumFromFirstBoxBases(cur.getElement(), 1);
			}
			cur = subEles.getNext(cur);
			if(cur == null) break;
			if(dealStage && hasEffect && preSubEleNotLineNotBand != null && notLineNotBand(cur.getElement().getType())){
				cur.getElement().setType(currentNeedToMap.get(cur.getElement().getType()).get(preSubEleNotLineNotBand.getType()));
			}
		}
	
		return cur;
	}
	
	/**
	 * This method is used for move the current SubElement to another SubElement which contain
	 * the position: <code>pos</code> from back to front.
	 * @param pos
	 * @param cur
	 * @param dealStage True if now is in the deal stage, false if now is in the predeal stage.
	 * 					if <code>dealStage==true</code>, We should add the number of bases at the 
	 * 					SubElement which type is Box. And if the upstream SubElement variant has 
	 * 					an impact on the downstream SubElement, we should change the downstream 
	 * 					variant's type.
	 * @return Return the <code>Entry&lt;EctypalSubElement&gt;</code> which contain the position: <code>pos</code>.
	 * 			Return null if not this <code>Entry&lt;EctypalSubElement&gt;</code> exists.
	 */
	private Entry<EctypalSubElement> moveFromBackToFront(int pos, Entry<EctypalSubElement> cur, boolean dealStage){
		while (pos < cur.getElement().getFrom()) {
			if(dealStage){
				if(hasEffect && notLineNotBand(cur.getElement().getType()))
					preSubEleNotLineNotBand = cur.getElement();
				if(shouldAddBoxBases(cur.getElement().getType(), hasEffect))
					addBoxBaseNumFromFirstBoxBases(cur.getElement(), 2);
			}
			cur = subEles.getPrevious(cur);
			if(cur == null) break;
			if(dealStage && hasEffect && preSubEleNotLineNotBand != null && notLineNotBand(cur.getElement().getType())){
				cur.getElement().setType(currentNeedToMap.get(cur.getElement().getType()).get(preSubEleNotLineNotBand.getType()));
			}
		}
	
		return cur;
	}
	
	Element write2XML(Document doc) {
		Element element = doc.createElement(XML_TAG_ELEMENT);
		if(id != null)
			element.setAttribute(XML_TAG_ID, id);
		if(type != null)
			element.setAttribute(XML_TAG_TYPE, type);
		if (symbol != null)	
			element.setAttribute(XML_TAG_SYMBOL, symbol);
		if (variant != null)	
			element.setAttribute(XML_TAG_VARIANT, variant);
		XmlWriter.append_text_element(doc, element, XML_TAG_FROM, String.valueOf(from));
		XmlWriter.append_text_element(doc, element, XML_TAG_TO, String.valueOf(to));
		XmlWriter.append_text_element(doc, element, XML_TAG_DIRECTION, direction?"+":"-");
		if(description != null)
			XmlWriter.append_text_element(doc, element, XML_TAG_DESCRIPTION, description);
		if(color != null)
			XmlWriter.append_text_element(doc, element, XML_TAG_COLOR, color);
		StringBuilder builder = new StringBuilder();
		for(String s : status){
			builder.append(s);
			builder.append(';');
		}
		if(builder.length() > 1){
			XmlWriter.append_text_element(doc, element, "status", builder.substring(0, builder.length()-1));
		}
		Entry<EctypalSubElement> next = subEles.getFirst();
		while (next != null) {
			element.appendChild(next.getElement().write2XML(doc));
			next = subEles.getNext(next);
		}
		return element;
	}
	
	/**
	 * @return Whether this Element still need to deal.
	 */
	public boolean stillNeedToDeal() {
		return !hasEffect || stillNeedToDeal;
	}
	
	/////////////////////////////////////////////Cut
	/**
	 * Cut the <code>cur</code>. If <code>belongFirst</code>==true, <code>pos</code> &lt; <code>cur.getTo()</code>;
	 * else if <code>belongFirst</code>==false, <code>pos</code> &gt; <code>cur.getFrom()</code>.</br>
	 * If <code>direction</code>==true, return the second SubElement, else return the first SubElement.
	 * @return
	 */
	private Entry<EctypalSubElement> cut(Entry<EctypalSubElement> cur, int pos, String firstType, String secondType, boolean belongFirst){
		EctypalSubElement[] divideResultSubEles = divideInto2SubElements(cur.getElement(), pos, firstType, secondType, belongFirst, direction);
		if(direction){
			/*
			 *-----||||||||||||||--------
			 *        | return
			 *       pos 
			 */
			cur = subEles.removeAndReturnPrevious(cur);
			cur = cur != null ? subEles.addAfter(divideResultSubEles[0], cur) : subEles.addFirst(divideResultSubEles[0]); 
			if(shouldAddBoxBases(firstType, hasEffect))
				addBoxBaseNumFromFirstBoxBases(cur.getElement(), 3);
			return subEles.addAfter(divideResultSubEles[1], cur);
		}else{
			/*
			 *-----|||||||||||||||--------
			 *      return |  
			 *            pos 
			 */
			cur = subEles.removeAndReturnNext(cur);
			cur = cur != null ? subEles.addBefore(divideResultSubEles[1], cur) : subEles.addLast(divideResultSubEles[1]); 
			if(shouldAddBoxBases(secondType, hasEffect))
				addBoxBaseNumFromFirstBoxBases(cur.getElement(), 4);
			return subEles.addBefore(divideResultSubEles[0], cur);
		}
	}
	
	private Entry<EctypalSubElement> cutWhenAffect(Entry<EctypalSubElement> cur, int pos, String notEffectSubEleType, String effectSubEleType){
		return direction ? cutWhenAffectWhenPositive(cur, pos, notEffectSubEleType, effectSubEleType)
				: cutWhenAffectWhenNegative(cur, pos, effectSubEleType, notEffectSubEleType);
	}
	
	private Entry<EctypalSubElement> cutWhenAffectWhenPositive(Entry<EctypalSubElement> cur, int pos, String firstType, String secondType){
		int minus = pos + 1 - cur.getElement().getTo();
		if(minus <= 0) {
			/*
			 *        cur                       cur                      cur
			 * ---||||||||||-----	or	------|||||||||----	or	------|||||||||----
			 *    |	                            |                            |
			 *   pos                           pos                          pos
			 */
			return cut(cur, pos + 1, firstType, secondType, false);
		}
		//minus > 0
		/*
		 *        cur
		 * ---||||||||||-----
		 *             |
		 *            pos 
		 */
		cur.getElement().setType(firstType);
		if(shouldAddBoxBases(firstType, hasEffect))
			addBoxBaseNumFromFirstBoxBases(cur.getElement(), 5);
		cur = getNextBox(subEles, cur, hasEffect);
		if(cur != null)
			cur.getElement().setType(secondType);
		return cur;
	}
	
	private Entry<EctypalSubElement> cutWhenAffectWhenNegative(Entry<EctypalSubElement> cur, int pos, String firstType, String secondType){
		int minus = pos - 1 - cur.getElement().getFrom();
		if(minus >= 0) {
			/*
			 *        cur                       cur                      cur
			 * ---||||||||||-----	or	------|||||||||----	or	------|||||||||----
			 *     |	                          |                           |
			 *    pos                            pos                         pos
			 */
			return cut(cur, pos - 1, firstType, secondType, true);
		}
		//minus < 0
		/*
		 *        cur
		 * ---||||||||||-----
		 *    |
		 *   pos 
		 */
		cur.getElement().setType(secondType);
		if(shouldAddBoxBases(secondType, hasEffect))
			addBoxBaseNumFromFirstBoxBases(cur.getElement(), 6);
		cur = getPreviousBox(subEles, cur, hasEffect);
		if(cur != null)
			cur.getElement().setType(firstType);
		return cur;
	}
	/////////////////////////////////////////////End of cut
	
	private void addBoxBaseNumFromFirstBoxBases(EctypalSubElement cur, int test){
		boxBaseNumFromFirstBoxBase += cur.getLength();
	}
}
