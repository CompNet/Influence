#!/usr/bin/perl
use strict; 
# use warnings;
# use utf8;
# Programme Perl pour une classification Cosinus
# Date : 22 02 2015
# Author: Jean-Valère Cossu
# email: jean-valere.cossu@alumni.univ-avignon.fr
if ($#ARGV < 0){
	print STDERR "Erreur cosine.pl : Missing Args\n\n";
	print STDERR "Usage : cosine.pl train test\n\n";
	print STDERR "Exemple : cosine.pl train.txt test.txt 0\n\n";
	exit 1;
}
# Ouverture des fichiers 
print "Cosinus ... $ARGV[0] $ARGV[1] \n";
# Variables
my %stoplist;			   # Stopword list
my %nbmots; 			   # Number of term in each class
my %s_wis_2; 			   # Class weight
my %WIM; 				   # Term weight in a document
my %buffer;				   # Output buffer writer
my %lambda;                # Expected weight of each term in each class
my %n_dial = ();           # Number of documents by class
my %class_df = ();         # Class document frequency
my %idf = ();              # Inverse document frequency (corpus)
my %gini = ();             # Purity: gini score
my %df = ();               # Train document frequency
my %df1 = ();              # Test document frequency
my %score_inf = ();        # Wrod influence score
my $term;                  # Current term
my $n_dialogs=0; 		   # Number of documents
my $coef_gini=1; 		   # Gini weight
my $coef_idf=1;  		   # IDF weight
my $coef_tf=1;     		   # TF weight
my $coef_tf_tweet=1;	   # TF in document weight
# Loading files in memory
open (CORPUS_TRAIN, $ARGV[0])or die "Erreur cosine.pl : Impossible d'ouvrir $ARGV[0]\n";
my @train=<CORPUS_TRAIN>;
chomp(@train);
close(CORPUS_TRAIN);
open (CORPUS_TEST, $ARGV[1])or die "Erreur cosine.pl : Impossible d'ouvrir $ARGV[1]\n";
my @test=<CORPUS_TEST>;
chomp(@test);
close(CORPUS_TEST);
# Files loaded 
# BEGIN Main programm
	&reading; # Reading files in memory
	&build_models; # Computing models
	&process; # Process each documents of test set
	&write; # Writing output
# END
# SUB
sub process {
	foreach my $ligne (@test){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $id=$list[0];
		my $dim = $list[1];
		my $text =&nettoyage($list[3]);
		my $id1=$id;
		my $hypothese="";	
		my $total=0; 
		my $max_methode=0; 
		my $new_s_WIM_2=0;	
		my %tf_term_tweet=();	   # Frequence du terme dans le document
		my %cos_d_classe; 		   # Poids du document dans la classe
		my %numerateur_classe; 	   # Somme des WIM x Wi pour chaque classe
		my %denominateur_classe;   # Somme des WIM x Wi pour chaque classe
		foreach my $label (keys (%n_dial)){ 
			$numerateur_classe{$label}=0;
			$denominateur_classe{$label}=0;
			$cos_d_classe{$label}=0;
		}
		my @zero = split(' ',$text);
		my $indice=0;
		while($indice <= ($#zero)){
			my $word=$zero[$indice];
			$tf_term_tweet{$word}++;
			$indice++;
		}
		foreach my $mot (@zero){ 
			if(exists $WIM{$mot}){
				$new_s_WIM_2+=(($WIM{$mot}*($tf_term_tweet{$mot}**$coef_tf_tweet))**2);
				foreach my $label (keys (%n_dial)){ 
					if(exists $lambda{$mot}{$label}){ 
						$numerateur_classe{$label}+=($lambda{$mot}{$label}*($WIM{$mot}*($tf_term_tweet{$mot}**$coef_tf_tweet)));
					}
				}
			}
		} 
		foreach my $label (keys (%n_dial)){ 
			$denominateur_classe{$label}=sqrt($s_wis_2{$label}*$new_s_WIM_2);
			if($denominateur_classe{$label} !=0){
				$cos_d_classe{$label}=$numerateur_classe{$label}/$denominateur_classe{$label}; 
			}
			$total+=$cos_d_classe{$label};
		}
		
		if($total!=0){ 
			$buffer{$id1}="$list[0]";
			foreach my $label (keys (%n_dial)){ 
				$cos_d_classe{$label}/=$total;
				$cos_d_classe{$label}=int($cos_d_classe{$label}*10000)/10000;
				if($cos_d_classe{$label} != 0){
					$buffer{$id1}.="\t$label\t$cos_d_classe{$label}";
				}
			}
		}
		else{
			delete $buffer{$id1};
		}
	}	
	undef @test;
}
sub write {
	my $z=0; # Compteur du nombre de documents traités
	my @list = split('\.', $ARGV[1]);
	my $file1=$list[0];		
	my $file="> $file1.cos";
	open (SYS, $file)or die "Erreur cosine.pl : Impossible d'ouvrir $file\n";
	foreach my $id (keys %buffer){
		if($id ne ""){
			print SYS "$buffer{$id}\n";
			$z++;
		}
	}
	close (SYS);
	print "... Complete, $z documents read\n";
}
sub nettoyage {
	my $text = $_[0];
	my $temp="";
	my $marker=0;
	my @zero = split(' ',$text);
	foreach my $word (@zero){ 
		if((!exists $stoplist{$word}) && !($word=~ "http") && !($word=~ "pic.twitter.com") && length($word)>2){ 
			$word=lc($word);
			$word=~s/[ \-\_,\)\\(\\"\\&\;\...\«\»\.\!\?\*+:]/ /g;
			$word=~s/[']/ /g;	
			$word=~s/[ÊÉÈËéèëê]/e/g;
			$word=~s/[ÂÄÀàâä]/a/g;
			$word=~s/[ÖÔôö]/o/g;
			$word=~s/[ÛÜÙùûü]/u/g;
			$word=~s/[ÎÏïî]/i/g;
			$word=~s/[Çç]/c/g;
			$word=~s/[ ]+//g;	
			if((!exists $stoplist{$word}) && !(length($word)==1) && !(length($word)==2)){ 
				$temp.="$word ";
			}
		}
	}
	$text=$temp;
	$text=~s/[ ]+/ /g;	
	return ($text);	
}
sub reading {
	foreach my $ligne (@train){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $id=$list[0];
		my $dim = $list[1];
		if($dim ne "Undecidable"){
			my $text =&nettoyage($list[3]);
			my @zero = split(' ',$text);
			foreach my $word (@zero){ 
				$class_df{$word}{$dim}++;
				$df{$word}++;
			}
			$n_dial{$dim}++; 
			$s_wis_2{$dim}=0;
			$n_dialogs++;
		}
	}
	foreach my $ligne (@test){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $id=$list[0];


		my $text =&nettoyage($list[3]);	
		my @zero = split(' ',$text);
		foreach my $word (@zero){ 
			$df1{$word}++;
		}
		$n_dialogs++;
	}
	undef @train;
	foreach my $lab (keys (%n_dial)){
		if($lab eq ""){
			delete $n_dial{$lab};
		}
	}		
	print "reading done\n";
}
sub build_models {
	foreach my $term (keys (%df)){
		if(exists $df1{$term}){
			$idf{$term}=log($n_dialogs/($df{$term}+$df1{$term})); 
		}
		else{
			$idf{$term}=log($n_dialogs/$df{$term}); 
		}
		if($idf{$term}>0){
			foreach my $lab (keys %{$class_df{$term}}){
				$gini{$term} += (($class_df{$term}{$lab})/($df{$term}))**2;
			}
		}
		else{
			delete $df{$term};
			delete $idf{$term};
			foreach my $lab (keys %{$class_df{$term}}){
				delete $class_df{$term}{$lab};
			}
		}
		foreach my $lab (keys %{$class_df{$term}}) {
			$lambda{$term}{$lab}=($class_df{$term}{$lab}**$coef_tf)*($idf{$term}**$coef_idf)*($gini{$term}**$coef_gini);
			if($lab ne "non_opinion_maker"){
				$score_inf{$term}=$lambda{$term}{$lab};
			}
			$s_wis_2{$lab}+=($lambda{$term}{$lab}**2);
		}
		$WIM{$term}=(($idf{$term}**$coef_idf)*($gini{$term}**$coef_gini))/$n_dialogs;
	}
	# Sortie vocabulaire
	# my $cpt=0;
	# foreach my $term (sort { $score_inf{$b} <=> $score_inf{$a} } keys %score_inf) {
		# if($cpt<100){
			# print "$term\t$score_inf{$term}\t$gini{$term}\n";
		# }
		# $cpt++;
	# }	
	undef %idf;
	undef %gini;
	undef %class_df;
	undef %df;
	undef %df1;
	print "Models DONE\n";
}
