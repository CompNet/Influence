#!/usr/bin/perl
# Programme Perl pour la mise au format TREC des sorties RepLab
# Date : 04 04 2015
# Author: Jean-Valère Cossu
# email: jean-valere.cossu@alumni.univ-avignon.fr
# Usage: perl format_trec.pl test_bank_en.txt test_bank_es.txt test_bank_en.cos test_bank_es.cos COS RL2014D02 
# Usage: perl format_trec.pl test_auto_en.txt test_auto_es.txt test_auto_en.cos test_auto_es.cos COS RL2014D01 
use strict; 
# use warnings;
# Ouverture des fichiers 
open (TRAIN_EN, $ARGV[0])or die "Erreur format_trec.pl : Impossible d'ouvrir $ARGV[0]\n";
my @TRAIN_EN=<TRAIN_EN>;
chomp(@TRAIN_EN);
close (TRAIN_EN);
open (TRAIN_ES, $ARGV[1])or die "Erreur format_trec.pl : Impossible d'ouvrir $ARGV[1]\n";
my @TRAIN_ES=<TRAIN_ES>;
chomp(@TRAIN_ES);
close (TRAIN_ES);
open (FILE_EN, $ARGV[2])or die "Erreur format_trec.pl : Impossible d'ouvrir $ARGV[2]\n";
my @FILE_EN=<FILE_EN>;
chomp(@FILE_EN);
close (FILE_EN);
open (FILE_ES, $ARGV[3])or die "Erreur format_trec.pl : Impossible d'ouvrir $ARGV[3]\n";
my @FILE_ES=<FILE_ES>;
chomp(@FILE_ES);
close (FILE_ES);
# Variables
my %domain=();
my %scores=();
my %score=();
my %compteur=();
my %vote=();
# BEGIN
	&readf;
# END
# SUB
sub readf{
	my $read=0;	
	foreach my $ligne (@TRAIN_ES){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $profil=$list[1];
		$domain{$profil}=$ARGV[5];
		$read++;
	}	
	print "Documents read : $read\n";
	$read=0;
	undef @TRAIN_ES;
	foreach my $ligne (@TRAIN_EN){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $profil=$list[1];
		$vote{$profil}{"en"}=$list[10];
		$domain{$profil}=$ARGV[5];
		$read++;
	}	
	print "Documents read : $read\n";
	$read=0;
	undef @TRAIN_EN;	
	# NewYorkCP		OM	24.5859001805225	NOM	35.4140998194775	60
	foreach my $ligne (@FILE_ES){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $profil=$list[0];
		if($list[3] eq ""){
			$list[3]=0;
		}		
		$scores{$profil}{"es"}=$list[3];
		$vote{$profil}{"es"}=$list[6];
		$read++;
	}	
	print "Documents read : $read\n";
	$read=0;
	undef @FILE_ES;	
	foreach my $ligne (@FILE_EN){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $profil=$list[0];
		if($list[3] eq ""){
			$list[3]=0;
		}		
		$scores{$profil}{"en"}=$list[3];
		$vote{$profil}{"en"}=$list[6];
		$read++;
	}	
	print "Documents read : $read\n";
	$read=0;
	undef @FILE_EN;
	my $file=">> out_$ARGV[4].txt";
	open (SYS, $file)or die "Erreur format_trec.pl : Impossible d'ouvrir $file\n";	
	foreach my $profil (keys %domain) {
		my $score1=$scores{$profil}{"en"}/($vote{$profil}{"en"}+$vote{$profil}{"es"});
		my $score2=$scores{$profil}{"es"}/($vote{$profil}{"en"}+$vote{$profil}{"es"});
		$score{$profil}=$score1+$score2;
	}
	foreach my $profil (sort { $score{$b} <=> $score{$a} } keys %score) {
		if($profil ne "" && $domain{$profil} ne "" && $domain{$profil} ne "RL2014D05"){
			$compteur{$domain{$profil}}++;
			print SYS "$domain{$profil}\t0\t$profil\t$compteur{$domain{$profil}}\t$score{$profil}\t$ARGV[1]\n";
			$read++;
		}
	}
	print "Documents read : $read\n";
	close (SYS);
}
