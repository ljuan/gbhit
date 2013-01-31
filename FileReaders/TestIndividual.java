package FileReaders;

import static FileReaders.Consts.DATA_ROOT;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import FileReaders.gff.GRFReader;

import edu.hit.mlg.individual.EctypalElements;


public class TestIndividual {
	public static void main(String[] args) throws Exception {
		FastaReader fr = new FastaReader("input/hg19.fa");//这个是fasta文件的位置
		String bedPath = "";//这个是BED文件的位置
		String grfPath = "F:\\基因组\\GRF\\ensemblRegulation.hg19.grf.gz";//这个是GRF文件的位置
		//下面这个是个人基因组的vcf文件的位置
		String vcfPath = "F:\\基因组\\VCF\\ALL.chr1.phase1_release_v3.20101123.snps_indels_svs.genotypes.vcf.gz";
		String dbsnpPath = "F:\\基因组\\VCF\\dbsnp135.hg19.vcf.bgz";//下面这个是dbsnp的vcf文件的位置
		String chr = "chr1";//这个是需要测试的染色体的位置
		int start = 212975000;//我是用这个范围测试的
		int end = 212990000;
		/**
		 * 找了一号染色体的所有范围，只有下面这两个范围有status或ASS、DSS上的变异。
		 * 179500000-180000000
		 * 212500000-213000000
		 * 
		 */
		Element elements = null;
		Element ctrlArea = null;
		Element variants = null;
		long l = System.currentTimeMillis();
		long ll = System.currentTimeMillis();

		Document doc = XMLOperations.createDocument(DATA_ROOT);
		elements = new BedReader(bedPath).write_bed2elements(doc, "Bed", chr,
				start, end, 0.49);
		System.out.println("Read Bed time:" + (System.currentTimeMillis() - ll));
		ll = System.currentTimeMillis();

		ctrlArea = new GRFReader(grfPath).write_grf2elements(doc, "regulation",chr, start - 2000, end + 2000);
		System.out.println("Read ctrlArea time:" + (System.currentTimeMillis() - ll));
		ll = System.currentTimeMillis();

		Annotations anno = new Annotations("VCF", vcfPath, "Type", Consts.MODE_FULL,Consts.GROUP_CLASS_PG);
		System.out.println("Construct Annotations time:" + (System.currentTimeMillis() - ll));
		ll = System.currentTimeMillis();

		VcfReader vcfReader = new VcfReader(anno, vcfPath);
		variants = vcfReader.write_vcf2variants(doc, "VCF", Consts.MODE_FULL,
				0.49, chr, start, end);
		System.out.println(variants.getChildNodes().getLength() + "个变异");
		System.out.println("Read VCF time:" + (System.currentTimeMillis() - ll));
		ll = System.currentTimeMillis();

		EctypalElements ee = new EctypalElements(doc, fr, elements, ctrlArea,
				variants, true, dbsnpPath, chr, start, end);
		Element ectypalEle = ee.deal();
		doc.getElementsByTagName(DATA_ROOT).item(0).appendChild(ectypalEle);
		XMLOperations.write(doc, "GenerateBed.xml");
		System.out.println("Deal EctypalElements time:" + (System.currentTimeMillis() - ll));
		System.out.println("Total time:" + (System.currentTimeMillis() - l));
		doc = null;
		fr.close();
	}
}

class XMLOperations {
	/**
	 * 创建一个XML Document，并以<code>root</code> 作为根元素
	 * 
	 * @param root
	 *            根元素名
	 * @return
	 * @throws Exception
	 */
	public static Document createDocument(String root) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();

		Element rootEle = doc.createElement("DataExchange");
		doc.appendChild(rootEle);

		return doc;
	}

	/**
	 * 将<code>doc</code> 写入到文件<code>path</code>中
	 * 
	 * @param doc
	 * @param path
	 * @throws Exception
	 */
	public static void write(Document doc, String path) throws Exception {
		File file = new File(path);
		// File file = new File("bigbed.xml");

		 TransformerFactory tf = TransformerFactory.newInstance();  
         Transformer transformer = tf.newTransformer();  
         FileOutputStream out = new FileOutputStream(file);  
         StreamResult xmlResult = new StreamResult(out);  
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");  
         transformer.transform(new DOMSource(doc), xmlResult);  
	}
}
