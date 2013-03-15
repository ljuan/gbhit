package edu.hit.mlg.individual;

import java.io.IOException;

import FileReaders.FastaReader;

public class TestFastaReader {
	public static void main(String[] args) throws IOException{
		FastaReader fr = new FastaReader("F:\\基因组\\fasta\\hg19.fa");
		
		System.out.println(fr.extract_seq("chr1", 20009533, 20009937));
		System.out.println(fr.extract_seq("chr1", 20020927, 20021044));
		System.out.println(fr.extract_seq("chr1", 20027261, 20027378));
		System.out.println(fr.extract_seq("chr1", 20063865, 20063949));
		System.out.println(fr.extract_seq("chr1", 20066317, 20066453));
		System.out.println(fr.extract_seq("chr1", 20067270, 20067434));
		System.out.println(fr.extract_seq("chr1", 20072025, 20072144));
		System.out.println(fr.extract_seq("chr1", 20072949, 20073092));
		System.out.println(fr.extract_seq("chr1", 20073656, 20073753));
		System.out.println(fr.extract_seq("chr1", 20082127, 20082259));
		System.out.println(fr.extract_seq("chr1", 20097035, 20097062));
		System.out.println(fr.extract_seq("chr1", 20097801, 20097975));
		System.out.println(fr.extract_seq("chr1", 20107073, 20107251));
		fr.close();
	}
}
