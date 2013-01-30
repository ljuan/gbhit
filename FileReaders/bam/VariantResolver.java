package FileReaders.bam;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMRecord;
import FileReaders.Consts;
import edu.hit.mlg.individual.vcf.Variant;

/**
 * 
 * @author Chengwu Yan
 * 
 */
public class VariantResolver {
	private Variant[] variants = null;
	private int len;
	private int count;

	public VariantResolver(SAMRecord record) {
		variants = new Variant[10];
		len = 10;
		count = 0;
		// MD:(short)(tag.charAt(1) << '\b' | tag.charAt(0))
		Cigar merge = new CigarMD(record.getCigar(),
				(String) record.getAttribute((short) ('D' << '\b' | 'M')))
				.getMergedCigar();
		extractVariants(record, merge);
	}

	private void extractVariants(SAMRecord record, Cigar cigar) {
		String read = record.getReadString();
		int pos = 1;
		int from = 0;
		Variant v = null;
		for (CigarElement ce : cigar.getCigarElements()) {
			if (ce.getOperator() == CigarOperator.M
					|| ce.getOperator() == CigarOperator.S) {
				pos += ce.getLength();
			} else if (ce.getOperator() == CigarOperator.X) {
				// Mean SNV
				v = new Variant();
				v.setType(Consts.VARIANT_TYPE_SNV);
				from = record.getReferencePositionAtReadPosition(pos);
				v.setFrom(from);
				v.setTo(from);
				v.setLetter(read.substring(pos - 1, pos));
				ensureCapacity();
				variants[count++] = v;
				pos += ce.getLength();
			} else if (ce.getOperator() == CigarOperator.D) {
				v = new Variant();
				v.setType(Consts.VARIANT_TYPE_DELETION);
				from = record.getReferencePositionAtReadPosition(pos - 1);
				v.setFrom(from + 1);
				v.setTo(from + ce.getLength());
				ensureCapacity();
				variants[count++] = v;
			} else if (ce.getOperator() == CigarOperator.I) {
				v = new Variant();
				v.setType(Consts.VARIANT_TYPE_INSERTION);
				from = record.getReferencePositionAtReadPosition(pos - 1);
				v.setFrom(from);
				v.setTo(from + 1);
				v.setLetter(read.substring(pos - 1, pos + ce.getLength() - 1));
				ensureCapacity();
				variants[count++] = v;
				pos += ce.getLength();
			}
		}
	}

	private void ensureCapacity() {
		if (count == len) {
			Variant[] vs = new Variant[len + len];
			System.arraycopy(variants, 0, vs, 0, len);
			len += len;
		}
	}

	public Variant[] getVariants() {
		Variant[] vs = new Variant[count];
		System.arraycopy(variants, 0, vs, 0, count);

		return vs;
	}
}
