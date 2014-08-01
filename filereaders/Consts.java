package filereaders;

public interface Consts{
	public static final String CONFIGURE=Consts.class.getResource("/")+"../../"+"config.xml";
//	public static final String GENE_DATA="/home/ljuan/gbdata/hg19/hgnc.hg19.sorted.txt";
//	public static final String DBSNP_DATA="/home/ljuan/gbdata/hg19/dbsnp135.hg19.vcf.bgz";
//	public static final String REFERNCE_DATA="/home/ljuan/gbdata/hg19/hg19.fa";
//	public static final String CYTO_DATA="/home/ljuan/gbdata/hg19/hg19.fa";
	
	public static final String DEFAULT_ENCODE="ISO-8859-1";
	public static final String CURRENT_ASSEMBLY="hg19";
	public static final String META_ROOT="MetaDataExchange";
	public static final String DATA_ROOT="DataExchange";
	
	public static final int LIMIT_SEQ=900;
	public static final int LIMIT_ELE=-1;
	public static final int LIMIT_VAR=1000000;
	public static final double LIMIT_BPP=1.5;
	
	public static final String VCF_HEADER_FILTER="Filters";
	public static final String VCF_HEADER_INFO="INFO";
	public static final String VCF_HEADER_FORMAT="Format";
	public static final String VCF_HEADER_SAMPLE="Samples";
	public static final String VCF_QUAL_LIMIT="QualityThreshold";
	public static final String VCF_CHROM_PREFIX="chromprefix";
	public static final String VCF_INDEX_LOCAL="indexpath";
	
	public static final String GDF_SNPID="SNPid";
	public static final String GDF_MUTID="MUTID";
	public static final String GDF_GENEID="GeneSymbol";
	
	public static final int PTRACK_CLASS_VAR=0;
	public static final int PTRACK_CLASS_FANNO=1;
	public static final int PTRACK_CLASS_ANNO=2;
	public static final int PTRACK_CLASS_CLN=3;
	public static final int PTRACK_CLASS_OTH=4;
	
	public static final String FORMAT_BED="BED";
	public static final String FORMAT_BIGBED="BB";
	public static final String FORMAT_BEDGRAPH="BG";
	public static final String FORMAT_VCF="VCF";
	public static final String FORMAT_WIG="WIG";
	public static final String FORMAT_BIGWIG="BW";
	public static final String FORMAT_BEDGZ="BEDGZ";
	public static final String FORMAT_FASTA="FASTA";
	public static final String FORMAT_BAM="BAM";
	public static final String FORMAT_GFF="GFF";
	public static final String FORMAT_GTF="GTF";
	public static final String FORMAT_GVF="GVF";
	public static final String FORMAT_GDF="GDF";
	public static final String FORMAT_GRF="GRF";
	public static final String FORMAT_CYTO="CYTO";
	public static final String FORMAT_REF="REF";
	public static final String FORMAT_SNP="SNP";
	public static final String FORMAT_ANNOVAR="ANNOVAR";
	public static final String FORMAT_LD="LD";
	public static final String FORMAT_HGNC="HGNC";
	public static final String FORMAT_ANNO="ANNO";
	public static final String FORMAT_FUNCTIONANNO="FANNO";
	
	public static final String XML_TAG_GROUP="group";
	public static final String GROUP_CLASS_SEQ="Sequence";
	public static final String GROUP_CLASS_USR="User";
	public static final String GROUP_CLASS_EXI="Ex";
	public static final String GROUP_CLASS_REP="Repeat";
	public static final String GROUP_CLASS_GCC="GC";
	public static final String GROUP_CLASS_GENE="Gene";
	public static final String GROUP_CLASS_DBSNP="dbSNP";
	public static final String GROUP_CLASS_KG="1000g";
	public static final String GROUP_CLASS_PG="Personal Genome";
	public static final String GROUP_CLASS_RNA="RNA";
	public static final String GROUP_CLASS_DATA="data";
	public static final String GROUP_CLASS_BASIC="Basic";
	
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
	public static final String XML_TAG_SEQ="Seq";
	public static final String XML_TAG_ERROR="Error";
	
	public static final String XML_TAG_ID="id";
	public static final String XML_TAG_SUPERID="superid";
	public static final String XML_TAG_DBSNPID="dd";
	public static final String XML_TAG_SUBID="subid";
	public static final String XML_TAG_TYPE="Y";
	public static final String XML_TAG_IFP="ifParam";
	public static final String XML_TAG_KEY="key";
	
	public static final String XML_TAG_VARIANT="V";
	public static final String XML_TAG_VARIANTS="Vs";
	public static final String XML_TAG_DIRECTION="s";
	public static final String XML_TAG_COLOR="Color";
	public static final String XML_TAG_FROM="F";
	public static final String XML_TAG_TO="T";
	public static final String XML_TAG_TOCHR="Mate";
	public static final String XML_TAG_LETTER="B";
	public static final String XML_TAG_DESCRIPTION="Des";
	public static final String XML_TAG_SAMPLEINFO="SampleInfo";
	public static final String XML_TAG_HOMO="h";
	public static final String XML_TAG_DBSNP="Dbsnp";
	public static final String XML_TAG_SOURCE="Source";
	public static final String XML_TAG_FRAME="Frame";
	public static final String XML_TAG_SCORE="Score";
	public static final String XML_TAG_EFFECT="e";
	public static final String XML_TAG_MAXAF="MaxAF";
	
	public static final String XML_TAG_CYTOBANDS="Cbs";
	public static final String XML_TAG_CYTOBAND="Cb";
	public static final String XML_TAG_GIESTAIN="gS";
	
	public static final String XML_TAG_ELEMENT="E";
	public static final String XML_TAG_ELEMENTS="Es";
	public static final String XML_TAG_SUBELEMENT="S";
	public static final String XML_TAG_STATUS="status";
	
	public static final String XML_TAG_PARAMETER="Parameter";
	public static final String XML_TAG_PARAMETERS="Parameters";
	public static final String XML_TAG_OPTIONS="Options";
	
	public static final String XML_TAG_PEDIGREE="Pedigree";
	public static final String XML_TAG_MEMBER="Member";
	public static final String XML_TAG_FAMILY="Family";
	public static final String XML_TAG_FATHER="Father";
	public static final String XML_TAG_MOTHER="Mother";
	public static final String XML_TAG_SEX="Sex";
	public static final String XML_TAG_AFFECTED="Affected";
	public static final String XML_TAG_IFS="ifSample";
	
	public static final String XML_TAG_MODE="Mode";
	public static final String XML_TAG_STEP="Step";
	public static final String XML_TAG_VALUE="Value";
	public static final String XML_TAG_VALUES="Values";
	public static final String XML_TAG_VALUE_LIST="ValueList";
	
	public static final String XML_TAG_READS="Rs";
	public static final String XML_TAG_READ="R";
	
	public static final String XML_TAG_GENES="Genes";
	public static final String XML_TAG_GENE="Gene";
	public static final String XML_TAG_HGNC="HGNC";
	public static final String XML_TAG_REFSEQ="RefSeq";
	public static final String XML_TAG_UCSC="UCSC";
	public static final String XML_TAG_SYMBOL="Symbol";
	public static final String XML_TAG_NAME="Name";
	public static final String XML_TAG_ENTREZ="Entrez";
	public static final String XML_TAG_ENSEMBL="Ensembl";
	
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
	
	public static final String SUBELEMENT_TYPE_LINE="L";
	public static final String SUBELEMENT_TYPE_SKIP_BAND="sBand";
	public static final String SUBELEMENT_TYPE_SKIP_BOX="skBox";
	
	public static final String SUBELEMENT_TYPE_BAND="D";
	public static final String SUBELEMENT_TYPE_EXTEND_BAND="eBand";
	public static final String SUBELEMENT_TYPE_LOST_BOX="lBox";
	
	public static final String SUBELEMENT_TYPE_BOX="X";
	public static final String SUBELEMENT_TYPE_EXTEND_BOX="eBox";
	public static final String SUBELEMENT_TYPE_SHIFT_BOX="shBox";
	public static final String SUBELEMENT_TYPE_SHIFT_EXTEND_BOX="seBox";
	
	public static final String SUBELEMENT_TYPE_POSSIBLE_SHIFT_BOX="psBox";
	public static final String SUBELEMENT_TYPE_POSSIBLE_SHIFT_EXTEND_BOX="pseBox";
	
	public static final String PARAMETER_TYPE_CHECKBOX="CHECKBOX";
	public static final String PARAMETER_TYPE_SELECTION="SELECTION";
	public static final String PARAMETER_TYPE_STRING="STRING";
	public static final String PARAMETER_TYPE_VCFSAMPLE="VCFSAMPLE";
	public static final String PARAMETER_TYPE_INVISABLE="INVISABLE";
	
}
