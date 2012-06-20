package FileReaders;

import java.io.*;
public interface Consts{
	public static final String CONFIGURE="input/config.xml";
	
	public static final String DEFAULT_ENCODE="ISO-8859-1";
	public static final String META_ROOT="MetaDataExchange";
	public static final String DATA_ROOT="DataExchange";
	
	public static final int LIMIT_SEQ=400;
	public static final int LIMIT_ELE=-1;
	public static final int LIMIT_VAR=1000000;
	
	public static final String FORMAT_BED="BED";
	public static final String FORMAT_VCF="VCF";
	public static final String FORMAT_WIG="WIG";
	public static final String FORMAT_BEDGZ="BEDGZ";
	public static final String FORMAT_FASTA="FASTA";
	
	public static final String XML_TAG_ASSEMBLY="Assembly";
	public static final String XML_TAG_ANNOTATION="Anno";
	public static final String XML_TAG_FORMAT="Format";
	public static final String XML_TAG_PATH="Path";
	public static final String XML_TAG_DEFAULT="Default";
	
	public static final String XML_TAG_CHROMOSOME="Chromosome";
	public static final String XML_TAG_START="Start";
	public static final String XML_TAG_END="End";
	public static final String XML_TAG_LENGTH="Length";
	public static final String XML_TAG_SEQUENCE="Sequence";
	public static final String XML_TAG_ERROR="Error";
	
	public static final String XML_TAG_ID="id";
	public static final String XML_TAG_TYPE="Type";
	
	public static final String XML_TAG_VARIANT="Variant";
	public static final String XML_TAG_VARIANTS="Variants";
	public static final String XML_TAG_ELEMENT="Element";
	public static final String XML_TAG_ELEMENTS="Elements";
	public static final String XML_TAG_SUBELEMENT="SubElement";
	public static final String XML_TAG_DIRECTION="Direction";
	public static final String XML_TAG_COLOR="Color";
	public static final String XML_TAG_FROM="From";
	public static final String XML_TAG_TO="To";
	public static final String XML_TAG_LETTER="Letter";
	public static final String XML_TAG_DESCRIPTION="Description";
	
	public static final String XML_TAG_MODE="Mode";
	public static final String XML_TAG_STEP="Step";
	public static final String XML_TAG_VALUE="Value";
	
	public static final String TEXT_TRUE="true";
	public static final String TEXT_FALSE="false";
	
	public static final String MODE_HIDE="hide";
	public static final String MODE_DENSE="dense";
	public static final String MODE_PACK="pack";
	public static final String MODE_FULL="full";
	public static final String MODE_DETAIL="detail";
	
	public static final String VARIANT_TYPE_SNV="SNV";
	public static final String VARIANT_TYPE_CNV="CNV";
	public static final String VARIANT_TYPE_INSERTION="INS";
	public static final String VARIANT_TYPE_INVERSION="INV";
	public static final String VARIANT_TYPE_DELETION="DEL";
	public static final String VARIANT_TYPE_MULTIPLE="MUL";
	public static final String VARIANT_TYPE_OTHERS="OTH";
	
	public static final String SUBELEMENT_TYPE_BOX="Box";
	public static final String SUBELEMENT_TYPE_LINE="Line";
	public static final String SUBELEMENT_TYPE_BAND="Band";
	
}