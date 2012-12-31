package FileReaders;

public interface Consts{
	public static final String CONFIGURE="/home/ljuan/gbdata/config.xml";
	
	public static final String DEFAULT_ENCODE="ISO-8859-1";
	public static final String META_ROOT="MetaDataExchange";
	public static final String DATA_ROOT="DataExchange";
	
	public static final int LIMIT_SEQ=900;
	public static final int LIMIT_ELE=-1;
	public static final int LIMIT_VAR=1000000;
	
	public static final String VCF_HEADER_FILTER="filters";
	public static final String VCF_HEADER_INFO="info";
	public static final String VCF_HEADER_FORMAT="format";
	public static final String VCF_HEADER_SAMPLE="sample";
	public static final String VCF_QUAL_LIMIT="QUALLIMIT";
	public static final String VCF_CHROM_PREFIX="chromprefix";
	
	public static final String CYTOBAND_PREVIOUS_CHR="pChr";
	
	public static final String FORMAT_BED="BED";
	public static final String FORMAT_BIGBED="BB";
	public static final String FORMAT_BEDGRAPH="BG";
	public static final String FORMAT_VCF="VCF";
	public static final String FORMAT_WIG="WIG";
	public static final String FORMAT_BIGWIG="BW";
	public static final String FORMAT_BEDGZ="BEDGZ";
	public static final String FORMAT_FASTA="FASTA";
	public static final String FORMAT_REF="REF";
	public static final String FORMAT_BAM="BAM";
	public static final String FORMAT_GFF="GFF";
	public static final String FORMAT_GTF="GTF";
	public static final String FORMAT_GVF="GVF";
	public static final String FORMAT_GDF="GDF";
	public static final String FORMAT_GRF="GRF";
	public static final String FORMAT_CYTO="CYTO";
	public static final String FORMAT_ANNO="ANNO";
	public static final String FORMAT_FUNCTIONANNO="FANNO";
	
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
	public static final String XML_TAG_SUPERID="superid";
	public static final String XML_TAG_SUBID="subid";
	public static final String XML_TAG_TYPE="Type";
	public static final String XML_TAG_IFP="ifParam";
	public static final String XML_TAG_KEY="key";
	
	public static final String XML_TAG_VARIANT="Variant";
	public static final String XML_TAG_VARIANTS="Variants";
	public static final String XML_TAG_DIRECTION="Direction";
	public static final String XML_TAG_COLOR="Color";
	public static final String XML_TAG_FROM="From";
	public static final String XML_TAG_TO="To";
	public static final String XML_TAG_TOCHR="ToChr";
	public static final String XML_TAG_LETTER="Letter";
	public static final String XML_TAG_DESCRIPTION="Description";
	
	public static final String XML_TAG_CYTOBANDS="Cytobands";
	public static final String XML_TAG_CYTOBAND="Cytoband";
	public static final String XML_TAG_GIESTAIN="gieStain";
	
	public static final String XML_TAG_ELEMENT="Element";
	public static final String XML_TAG_ELEMENTS="Elements";
	public static final String XML_TAG_SUBELEMENT="SubElement";
	
	public static final String XML_TAG_PARAMETER="Parameter";
	public static final String XML_TAG_PARAMETERS="Parameters";
	public static final String XML_TAG_OPTIONS="Options";
	
	public static final String XML_TAG_MODE="Mode";
	public static final String XML_TAG_STEP="Step";
	public static final String XML_TAG_VALUE="Value";
	public static final String XML_TAG_VALUES="Values";
	
	public static final String XML_TAG_READS="Reads";
	public static final String XML_TAG_READ="Read";
	
	public static final String TEXT_TRUE="true";
	public static final String TEXT_FALSE="false";
	
	public static final String MODE_HIDE="hide";
	public static final String MODE_DENSE="dense";
	public static final String MODE_PACK="pack";
	public static final String MODE_FULL="full";
	public static final String MODE_DETAIL="detail";
	
	public static final String VARIANT_TYPE_SNV="SNV";
	public static final String VARIANT_TYPE_CNV="CNV";
	public static final String VARIANT_TYPE_BLS="BLS";
	public static final String VARIANT_TYPE_DUPLICATION="DUP";
	public static final String VARIANT_TYPE_INSERTION="INS";
	public static final String VARIANT_TYPE_INVERSION="INV";
	public static final String VARIANT_TYPE_DELETION="DEL";
	public static final String VARIANT_TYPE_MULTIPLE="MUL";
	public static final String VARIANT_TYPE_OTHERS="OTH";
	
	public static final String SUBELEMENT_TYPE_BOX="Box";
	public static final String SUBELEMENT_TYPE_LINE="Line";
	public static final String SUBELEMENT_TYPE_BAND="Band";
	public static final String SUBELEMENT_TYPE_LOSTBAND="lost_Band";
	public static final String SUBELEMENT_TYPE_LOSTBOX="lost_Box";
	public static final String SUBELEMENT_TYPE_RETAINEDLINE="retained_Line";
	public static final String SUBELEMENT_TYPE_SHIFTBOX="shift_Box";
	public static final String SUBELEMENT_TYPE_ENDLESSBAND="endless_Band";
	public static final String ELEMENT_TYPE_ENHANCE="Enhance";
	public static final String ELEMENT_TYPE_INHIBIT="Inhibit";
	public static final String ELEMENT_TYPE_SILENCE="Silence";
	public static final String ELEMENT_TYPE_CHANGED="Changed";
	
	
	public static final String PARAMETER_TYPE_CHECKBOX="CHECKBOX";
	public static final String PARAMETER_TYPE_SELECTION="SELECTION";
	public static final String PARAMETER_TYPE_STRING="STRING";
	public static final String PARAMETER_TYPE_VCFSAMPLE="VCFSAMPLE";
	public static final String PARAMETER_TYPE_INVISABLE="INVISABLE";
}