#!/usr/bin/perl
# Programme Perl pour la mise au format TREC des sorties RepLab
# Date : 04 04 2015
# Author: Jean-Valère Cossu
# email: jean-valere.cossu@alumni.univ-avignon.fr
# Usage: perl format_trec.pl out.txt info.txt output.txt SYS
use strict; 
# use warnings;
# Ouverture des fichiers 
open (OUTPUT, $ARGV[0])or die "Erreur format_trec.pl : Impossible d'ouvrir $ARGV[0]\n";
my @OUTPUT=<OUTPUT>;
chomp(@OUTPUT);
close (OUTPUT);
open (INFO, $ARGV[1])or die "Erreur format_trec.pl : Impossible d'ouvrir $ARGV[1]\n";
my @INFO=<INFO>;
chomp(@INFO);
close (INFO);
# Variables
my %domain=();
my %score=();
my %compteur=();
# BEGIN
	&readf;
# END
# SUB
sub readf{
	my $read=0;
	foreach my $ligne (@INFO){
		chomp $ligne;
		$ligne=~s/\"//g;
		my @list = split('\t', $ligne);
		my $profil=$list[0];
		my $url=$list[1];
		my $domain=$list[2];
		$domain{$profil}=$domain;
		$read++;
	}	
	print "Documents read : $read\n";
	$read=0;
	undef @INFO;	
	foreach my $ligne (@OUTPUT){
		chomp $ligne;
		my @list = split('\t', $ligne);
		my $profil=$list[0];
		if($list[3] eq ""){
			$list[3]=0;
		}
		$score{$profil}=$list[3]/$list[6];
		$read++;
	}	
	print "Documents read : $read\n";
	my $file="> $ARGV[2]";
	open (SYS, $file)or die "Erreur format_trec.pl : Impossible d'ouvrir $file\n";	
	$read=0;
	undef @OUTPUT;
	foreach my $profil (sort { $score{$b} <=> $score{$a} } keys %score) {
		if($profil ne "" && $domain{$profil} ne "" && $domain{$profil} ne "RL2014D05"){
			$compteur{$domain{$profil}}++;
			print SYS "$domain{$profil}\t0\t$profil\t$compteur{$domain{$profil}}\t$score{$profil}\t$ARGV[3]\n";
			$read++;
		}
	}
	print "Documents read : $read\n";
	close (SYS);
}
