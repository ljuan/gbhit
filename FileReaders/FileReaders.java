package FileReaders;


public class FileReaders implements Consts{
	public FileReaders (){

		Instance i1=new Instance("hg19");
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.get_Assemblies());
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.get_Annotations());
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.update("chr1", 224754110, 224754430,1000));
		System.out.println(System.currentTimeMillis());
		String[] externals={"MCF7"};
		String[] externals_links={"http://219.217.238.167/gbfiles/MCF7_DNAseq.variants.snpRecalibrated.vcf.bgz"};
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
		System.out.println(i1.update("chr2", 224754110, 224855430,1000));
		System.out.println(System.currentTimeMillis());
	}
}