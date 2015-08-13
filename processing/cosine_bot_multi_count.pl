#!/usr/bin/perl
# Programme Perl pour une classification cosinus sur la tache OM
# Date : 14 05 2014
# Author: Jean-Valère Cossu
# email: jean-valere.cossu@alumni.univ-avignon.fr
# Usage : perl cosine_om_om.pl train test ouput
use strict; 
# use warnings;
# use utf8;
if ($#ARGV < 0){
	print STDERR "Erreur cosine_om.pl : Argument manquant\n\n";
	print STDERR "Usage : perl cosine_om.pl train test ouput\n\n";
	exit 1;
}
# Ouverture des fichiers 
print "Cosinus ... \n";
print "Classification pour la tache OM\n";
open (CORPUS_TRAIN, $ARGV[0])or die "Erreur cosine_om.pl : Impossible d'ouvrir train input $ARGV[0]\n";
open (CORPUS_TEST, $ARGV[1])or die "Erreur cosine_om.pl : Impossible d'ouvrir test intput $ARGV[1]\n";
my $file="> $ARGV[2]";
open (SYS, $file)or die "Erreur cosine_om.pl : Impossible d'ouvrir ouput $file\n";
# Variables
my @train=();
@train = <CORPUS_TRAIN>;
chomp(@train);
close(CORPUS_TRAIN);
my @test=();
@test = <CORPUS_TEST>;
chomp(@test);
close(CORPUS_TEST);
my %s_wis_2; 			   # Somme des Wi classe1²
my %out_sys; 			   # Somme des Wi classe1²
my %out_gold; 			   # Somme des Wi classe1²
my %cos_d_classe; 		   # poids dans la classe
my %numerateur_classe; 	   # Somme des WIM x Wi pour chaque classe
my %denominateur_classe;   # Somme des WIM x Wi pour chaque classe
my %WIM; 				   # WIM
my %lambda;                # expected weight of each term in each class - Poisson pmf parameter
my %n_dial = ();           # number of dialogs by class
my %lprior = ();           # estimated log probability of each class (prior)
my %class_df = ();         # class document frequency
my %idf = ();              # inverse document frequency (corpus)
my %gini = ();             # purity: gini score
my %df = ();               # corpus document frequency
my %df1 = ();              # corpus document frequency
my %seen = ();              
my %nbd_user = (); 
my %hyp_user = ();         
my $term;                  # curren term
my $lab;                   # current dialog label
my $n_labs;                # number of current labels
my $n_dialogs=0; 		   # Nombre de documents
my $min=0; 
my $bool=0; 
my $coef_gini=1; 
my $coef_idf=1; 
my $coef_tf_tweet=1;
my $coef_tf=1;
my $z=0; # Compteur du nombre de documents traités
my %stoplist;
&reading;
&build_models;
undef %seen;
foreach my $ligne (@test){
	chomp $ligne;
	my @list = split('\t', $ligne);
	my $user=$list[1];
	$nbd_user{$user}++;
	my $text =&nettoyage($list[7]);
	# if(!exists $seen{$text}){
		$seen{$text}=1;	
		my $hypothese="";
		my $total=0; 
		my $max_methode=0; 
		my $new_s_WIM_2=0;	
		my %tf_term_tweet=();
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
			$new_s_WIM_2+=(($WIM{$mot}*($tf_term_tweet{$mot}**$coef_tf_tweet))**2);
			foreach my $label (%n_dial){
				if(exists $lambda{$mot}{$label}){ 
					$numerateur_classe{$label}+=($lambda{$mot}{$label}*($WIM{$mot}*($tf_term_tweet{$mot}**$coef_tf_tweet)));
				}
			}
		} 
		foreach my $label (%n_dial){
			$denominateur_classe{$label}=sqrt($s_wis_2{$label}*$new_s_WIM_2);
			if($denominateur_classe{$label} !=0){
				$cos_d_classe{$label}=$numerateur_classe{$label}/$denominateur_classe{$label}; 
			}
			if($total<$cos_d_classe{$label}){
				$total=$cos_d_classe{$label};
			}
		}
		foreach my $label (%n_dial){
			if($total!=0 && $cos_d_classe{$label}==$total){ 
				$hyp_user{$user}{$label}++;
			}
		}
		$z++;
	# }
}
print "... Complete, $z documents read\n";
print "Voting ...\n";
foreach my $user (sort keys (%nbd_user)){
	if($nbd_user{$user}>0 && $user ne "1"){
		print SYS "$user\t\tOM\t".$hyp_user{$user}{"opinion_maker"}."\tNOM\t".$hyp_user{$user}{"non_opinion_maker"}."\t".$nbd_user{$user}."\n";
	}
}
print "Done\n";
# Fermeture des fichiers 
close (SYS);
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
			$s_wis_2{$lab}+=($lambda{$term}{$lab}**2);
		}
		$WIM{$term}=(($idf{$term}**$coef_idf)*($gini{$term}**$coef_gini))/$n_dialogs;
	}
	undef %idf;
	undef %gini;
	undef %class_df;
	undef %df;
	undef %df1;
	print "Models DONE\n";
}
sub reading {
	foreach my $ligne (@train){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $user=$list[1];
		my $label = $list[8];
		my $text =&nettoyage($list[7]);
		# if(!exists $seen{$text}){
			# $seen{$text}=1;
			my @zero = split(' ',$text);
			foreach my $word (@zero){ 
				$class_df{$word}{$label}++;
				$df{$word}++;
			}
			$n_dial{$label}++; 
			$s_wis_2{$label}=0;
			$n_dialogs++;
		# }
	}
	undef @train;
	foreach my $ligne (@test){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $text =&nettoyage($list[7]);
		# if(!exists $seen{$text}){
			# $seen{$text}=1;		
			my @zero = split(' ',$text);
			foreach my $word (@zero){
				$df1{$word}++;
			}
			$n_dialogs++;
		# }
	}
	foreach my $lab (keys (%n_dial)){
		if($lab eq ""){
			delete $n_dial{$lab};
		}
	}		
	print "reading done\n";
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
