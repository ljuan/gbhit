import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class createXML {
	private Document document;

	public void init() {
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			this.document = builder.newDocument();
		}
		catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        }
	}
	public void create(String Filename){ 
		
		Element DataExchange = this.document.createElement("DataExchange");
		this.document.appendChild(DataExchange);
		

			
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			DOMSource source = new DOMSource(document);
			transformer.setOutputProperty(OutputKeys.ENCODING, "GB2312");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			PrintWriter pw = new PrintWriter(new FileOutputStream(Filename));
			StreamResult result = new StreamResult(pw);
			transformer.transform(source, result);
		} catch (TransformerException mye) {
			   mye.printStackTrace();
		} catch (IOException exp) {
			   exp.printStackTrace();
		}
		
	}


	void Assembly(String assembly){
		Element Assembly = this.document.createElement("Assembly");
		Assembly.appendChild(this.document.createTextNode(assembly)); 
	}
	void Chromosome(String chromosome){
		Element Chromosome = this.document.createElement("Chromosome");
		Chromosome.appendChild(this.document.createTextNode(chromosome));
	}
	void Start(long start){
		Element Start = this.document.createElement("Start");
		Start.appendChild(this.document.createTextNode(String.valueOf(start)));
	}
	void End(long end){
		Element End = this.document.createElement("End");
		End.appendChild(this.document.createTextNode(String.valueOf(end)));
	}
	public void Reference(String seqlimit){
		if(seqlimit.length()<400){
			Element Reference = this.document.createElement("Reference");
			Reference.appendChild(this.document.createTextNode(seqlimit));
		}
		else{
			System.out.println("The length of seq is larger than 400!");
		}
	}
	
	
}


class Bed {
	String chrom;
	long chromStart;
	long chromEnd;
	String name;
	int score;
	String strand;
	long thickStart;
	long thickEnd;
	String itemRgb;
	int blockCount;
	long[] blockSizes;
	long[] blockStarts;

	Bed(String line){
		String[] temp=line.split("\t");
		chrom=temp[0];
		chromStart=Long.parseLong(temp[1]);
		chromEnd=Long.parseLong(temp[2]);
		if(temp.length>3){
			name=temp[3];
			score=Integer.parseInt(temp[4]);
			strand=temp[5];
			thickStart=Long.parseLong(temp[6]);
			thickEnd=Long.parseLong(temp[7]);
			itemRgb=temp[8];
			blockCount=Integer.parseInt(temp[9]);
			String[] starts=temp[11].split(",");
			String[] sizes=temp[10].split(",");
			blockSizes=new long[blockCount];
			blockStarts=new long[blockCount];
			for(int i=0;i<blockCount;i++){
				blockSizes[i]=Long.parseLong(sizes[i]);
				blockStarts[i]=Long.parseLong(starts[i]);
			}
		}
	}
}