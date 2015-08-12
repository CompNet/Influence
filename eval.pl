#!/usr/bin/perl
# Programme Perl pour l'évaluation classification
# Date : 13 07 2015
# Author: Jean-Valère Cossu
# email: jean-valere.cossu@alumni.univ-avignon.fr
use strict; 
# use utf8;
# use warnings;
if ($#ARGV < 0){
	print STDERR "Erreur eval.pl : Missing Args\n\n";
	print STDERR "Usage : eval.pl references sysout domain\n\n";
	exit 1;
}
# Ouverture des fichiers 
open (GOLD, $ARGV[0])or die "Erreur eval.pl : Impossible d'ouvrir le fichier references : $ARGV[0]\n";
open (SYS_OUT, $ARGV[1])or die "Erreur eval.pl : Impossible d'ouvrir le fichier a mesurer : $ARGV[1]\n";
# Variables
my %cls; 				# Classes list
my %correctes; 			# Number of corrects documents in each class
my %origine; 			# Number of documents in each class in the reference 
my %predites; 			# Number of documents predicted in each class 
my %gold; 				# Document Label (reference)
my %hyp; 				# Document Label (hypothesis)
my %precision; 			# Precision for each class
my %rappel; 			# Reccal for each class
my %fm;					# FScore for each class
my $total_correctes=0; 	# Number of correct documents 
my $total_origine=0; 	# Number of documents in the gold
my $total_predite=0; 	# Number of documents predicted
# Reading gold
while(my $ligne=<GOLD>){
	chomp $ligne;
	my @list = split('\t', $ligne);
	my $dom = $list[0]; 
	my $tweet_id = $list[2]; 
	my $classe = $list[3];
	if($list[0] eq $ARGV[2]){ # Filtre sur le domain
		$gold{$tweet_id}=$classe; 
		$cls{$classe}=1; 
		$origine{$classe}++; 
		$total_origine++;
	}
}
print "Reading references done: $total_origine documents read\n";
# Reading gold done
# Reading system
while(my $ligne = <SYS_OUT>){
	chomp $ligne;
	my @list = split('\t', $ligne);
	my $dom = $list[0]; 
	my $tweet_id = $list[2]; 
	my $hypothese = $list[4]*2;
	if($hypothese>0.5){ # Seuil si la proba d'etre influent est supérieur à 0.5 ... 
		$hypothese=1
	}
	else{
		$hypothese=0
	}
	if(exists $gold{$tweet_id}){ # On ne fait l'évaluation que sur les documents présents dans la référence
		$total_predite++; 
		$predites{$hypothese}++;
		if($hypothese eq $gold{$tweet_id}){
			$correctes{$hypothese}++;
			$total_correctes++; 
		}
	}
}
print "Reading sysoutput done: $total_predite documents read\n";
# Reading system done
my $numberofclasses=(keys %cls);
my $sumF=0; # Sum of FScore
my $sumR=0; # Sum of Reccal
my $sumP=0; # Sum of Precision
# Computing scores
print "Classe\t Precision\tRappel\tF-Score\n";
foreach my $classe (sort keys (%cls)){ 
	my $marker=0;
	if($predites{$classe}==0){ # Protect by 0 division if no document predicted for one class
		$marker=1;
		$predites{$classe}=1; 
	}
	$rappel{$classe}=int($correctes{$classe}/$origine{$classe}*10000)/100;
	$precision{$classe}=int($correctes{$classe}/$predites{$classe}*10000)/100;
	if($marker==1){ # End of protection
		$predites{$classe}=0; 
	}	
	# Computing F for each class
	if($rappel{$classe}==0 && $precision{$classe}==0){ # Protect by 0 division if no document correct for one class 
		$fm{$classe}=0;
	}
	else{
		$fm{$classe}=2*($rappel{$classe}*$precision{$classe})/($rappel{$classe}+$precision{$classe});
	}
	$fm{$classe}=int($fm{$classe}*100)/100;
	print "$classe\t$precision{$classe}\t$rappel{$classe}\t$fm{$classe}\n";
	$sumF+=$fm{$classe}; # On somme le Fscore de chaque classe 
	$sumR+=$rappel{$classe}; # On somme le Rappel de chaque classe 
	$sumP+=$precision{$classe}; # On somme la Precision de chaque classe 
}
my $avgF=int($sumF/$numberofclasses*100)/100; # Average F-Score (Macro F-Score)
my $avgR=int($sumR/$numberofclasses*100)/100; # Average Reccal
my $avgP=int($sumP/$numberofclasses*100)/100; # Average Precision
my $accuracy= int ($total_correctes/$total_origine*10000)/100; # Accuracy
my $f = int (2*($avgR*$avgP)/($avgR+$avgP)*100)/100; # F-Score micro 
print "$ARGV[1]\tCorrects $total_correctes / $total_predite\n";
print "$ARGV[1]\tavgP\t$avgP\tavgR\t$avgR\tavgF\t$avgF\n";
print "$ARGV[1]\taccuracy\t$accuracy\tmicroF\t$f\n";
# Fermeture des fichiers
close (CORPUS_EVAL);
close (GOLD);
