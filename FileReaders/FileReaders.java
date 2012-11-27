package FileReaders;


public class FileReaders implements Consts{
	public FileReaders (){

		Instance i1=new Instance("hg19");
		System.out.println(i1.get_Annotations());
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.get_Assemblies());
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.get_Annotations());
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.update("chr21", 11114426, 38254436,1350));
		System.out.println(System.currentTimeMillis());
		String[] externals={"MCF7"};
		String[] externals_links={"http://127.0.0.1/gbfiles/MCF7_DNAseq.variants.snpRecalibrated.vcf.bgz"};
		String[] externals_types={"VCF"};
		String[] externals_modes={MODE_FULL};
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		externals[0]="RefSeq";
		i1.remove_Tracks(externals);
		System.out.println(System.currentTimeMillis());
		externals[0]="RefSeqOriginal";externals_links[0]="input/refGene.hg19.bed";externals_types[0]="BED";externals_modes[0]=MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		externals[0]="BamTest";externals_links[0]="http://127.0.0.1/gbfiles/MCF7_DNAseq.hg19.sorted.bam";externals_types[0]="BAM";externals_modes[0]=MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
/*		externals[0]="GffTest";externals_links[0]="http://127.0.0.1/gbfiles/refGene.hg19.sorted.gtf.gz";externals_types[0]="GFF";externals_modes[0]=MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		i1.remove_Tracks(externals);
		externals[0]="GtfTest";externals_links[0]="http://127.0.0.1/gbfiles/refGene.hg19.sorted.gtf.gz";externals_types[0]="GTF";externals_modes[0]=MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		externals[0]="GvfTest";externals_links[0]="http://127.0.0.1/gbfiles/Venter.sorted.gvf.gz";externals_types[0]="GVF";externals_modes[0]=MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		externals[0]="bbTest";externals_links[0]="http://127.0.0.1/gbfiles/ensGene.hg19.bb";externals_types[0]="BB";externals_modes[0]=MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
*/		externals[0]="bwTest";externals_links[0]="http://127.0.0.1/gbfiles/chr21.phyloP46way.placental.bigwig";externals_types[0]="BW";externals_modes[0]=MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		externals[0]="wigTest";externals_links[0]="/var/www/gbfiles/chr21.phyloP46way.placental.wigFix";externals_types[0]="WIG";externals_modes[0]=MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		externals[0]="bgTest";externals_links[0]="/var/www/gbfiles/chr21.phyloP46way.placental.bedGraph";externals_types[0]="BG";externals_modes[0]=MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
//		System.out.println(i1.update("chr2", 224754110, 224855430,1000));
//		System.out.println(System.currentTimeMillis());
	}
}