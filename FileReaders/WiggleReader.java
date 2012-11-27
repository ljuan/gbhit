package FileReaders;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import FileReaders.wiggle.DataValueList;
import FileReaders.wiggle.WiggleExtractor;

/**
 * Get dataValues from wiggle or bigwig.
 * 
 * <pre>
 * usage:
 * new WiggleReader(String filePath, boolean isBigWig).
 * 	 write_wiggle2Values(Document doc, String track, 
 * 						String chr, int start, int end, int windowSize, int step)
 * @author Chengwu Yan
 * 
 */
public class WiggleReader {
	/**
	 * If the file is a BigWig file, isBigWig=true, false else. If the file
	 * comes from remote server, isBigWig must be true.
	 */
	private boolean isBigWig = false;
	/**
	 * file path of the wiggle(BigWig) file in local, or uri of the BigWig file
	 * from remote.
	 */
	private String filePath = null;

	/**
	 * Constructor
	 * 
	 * @param filePath
	 *            file path of the wiggle(BigWig) file in local, or uri of the
	 *            BigWig file from remote.
	 * @param isBigWig
	 *            If the file is a BigWig file, isBigWig=true, false else. If
	 *            the file comes from remote server, isBigWig must be true.
	 */
	public WiggleReader(String filePath, boolean isBigWig) {
		this.filePath = filePath;
		this.isBigWig = isBigWig;
	}

	/**
	 * 
	 * @param doc
	 * @param track
	 * @param chr
	 * @param start
	 *            1-base
	 * @param end
	 *            1-base
	 * @param windowSize
	 *            size, in pixel. Make sure that size Can be divided by 2
	 * @param step
	 * @throws IOException
	 */
	public void write_wiggle2Values(Document doc, String track, String chr, int start,
			int end, int windowSize, int step) throws IOException {
		DataValueList values = new WiggleExtractor(filePath, chr, start,
				end, isBigWig, windowSize, step).extract();

		WiggleReader.writeDataValues2XML(doc, track, start, end, values.toString());
	}

	/**
	 * write dataValues to xml.
	 * @param doc
	 * @param track
	 * @param start
	 * @param end
	 * @param valueList
	 */
	static void writeDataValues2XML(Document doc, String track,
			int start, int end, String valueList) {
		Element ele = doc.createElement(Consts.XML_TAG_VALUES);
		ele.setAttribute(Consts.XML_TAG_ID, track);
		doc.getElementsByTagName(Consts.DATA_ROOT).item(0).appendChild(ele); // Values
		XmlWriter
				.append_text_element(doc, ele, Consts.XML_TAG_FROM, start + "");
		XmlWriter.append_text_element(doc, ele, Consts.XML_TAG_TO, end + "");
		XmlWriter.append_text_element(doc, ele, "ValueList", valueList);
	}
}
