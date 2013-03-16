package FileReaders;


public class FileReaders {
	public FileReaders (){

		Instance i1=new Instance("hg19");
		System.out.println(i1.get_Annotations());
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.get_Assemblies());
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.get_Annotations());
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.update("chr21",33043478,33044749,1350));
		System.out.println(System.currentTimeMillis());
		String[] externals={"1000g"};
		String[] externals_links={"Chr21:http://202.118.228.68/gbfiles/ALL.chr21.phase1_release_v3.20101123.snps_indels_svs.genotypes.vcf.gz;"};
		String[] externals_types={"VCF"};
		String[] externals_modes={Consts.MODE_PACK};
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.add_Pvar("1000g", Consts.MODE_PACK, "1000g"));
		externals[0]="Regulation";externals_links[0]="http://127.0.0.1/gbfiles/ensemblRegulation.hg19.grf.gz";externals_types[0]=Consts.FORMAT_GRF;externals_modes[0]=Consts.MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Pfanno("Regulation", Consts.MODE_PACK));
		System.out.println(i1.add_Panno("RefSeq", Consts.MODE_PACK));
/*		i1.remove_Tracks(externals);
/*		System.out.println(System.currentTimeMillis());
		externals[0]="cytoband";externals_links[0]="input/cytoBand.hg19.txt";externals_types[0]=Consts.FORMAT_CYTO;externals_modes[0]=Consts.MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		externals[0]="BamTest";externals_links[0]="http://202.118.228.68/gbfiles/MCF7_DNAseq.hg19.sorted.bam";externals_types[0]="BAM";externals_modes[0]=Consts.MODE_FULL;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
/*		externals[0]="GffTest";externals_links[0]="http://127.0.0.1/gbfiles/refGene.hg19.sorted.gtf.gz";externals_types[0]=Consts.FORMAT_GFF;externals_modes[0]=Consts.MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		i1.remove_Tracks(externals);
/*		externals[0]="dbsnp135";externals_links[0]="http://202.118.228.68/gbfiles/dbsnp135.hg19.vcf.bgz";externals_types[0]=Consts.FORMAT_VCF;externals_modes[0]=Consts.MODE_PACK;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
/*		externals[0]="GvfTest";externals_links[0]="http://127.0.0.1/gbfiles/Venter.sorted.gvf.gz";externals_types[0]="GVF";externals_modes[0]=Consts.MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		externals[0]="bbTest";externals_links[0]="http://127.0.0.1/gbfiles/ensGene.hg19.bb";externals_types[0]="BB";externals_modes[0]=Consts.MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		System.out.println(i1.get_Detail("bbTest", "ENST00000480336", "chr1", 32930658, 33066393));
/*		externals[0]="bwTest";externals_links[0]="http://127.0.0.1/gbfiles/chr21.phyloP46way.placental.bigwig";externals_types[0]="BW";externals_modes[0]=Consts.MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
/*		externals[0]="wigTest";externals_links[0]="/var/www/gbfiles/chr21.phyloP46way.placental.wigFix";externals_types[0]="WIG";externals_modes[0]=Consts.MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
		externals[0]="bgTest";externals_links[0]="/var/www/gbfiles/chr21.phyloP46way.placental.bedGraph";externals_types[0]="BG";externals_modes[0]=Consts.MODE_DENSE;
		i1.add_Externals(externals,externals_links,externals_types,externals_modes);
		System.out.println(i1.add_Tracks(externals,externals_modes));
		System.out.println(System.currentTimeMillis());
/*		System.out.println(i1.update("chr2", 24754110, 24855430,1000));
		System.out.println(System.currentTimeMillis());
*/	}
}