Characterization of Twitter Profiles, with an application to offline influence detection
==============================
## Presentation
These scripts are meant to extract certain features from raw Twitter data describing Twitter users (tweets, profile info, as well as external data). Once the features are extracted, various forms of SVMs are trained, and logistic regressions are performed, to classify and rank the users. These operations are conducted on different subgroups of features. The details of the process are given in [CDL'15] and [CLD'15].

Please, cite [CLD'15] if you use our scripts. We would also be very interested to know your context of application and/or modification, so please, let us know.

Note the software may evolve depending on our future research work.

## Organization
The project is composed of the following scripts:
* xxxxx **(une phrase par script ou groupe de scripts pour expliquer ce que ça fait)**
* yyyyy
* plspm4influence.R aims at finding relations between features categories and verify the efficiency of the proposed influence conceptual model

Here are the third-party softwares included in this version:
* Part of SVMs-based experiments were made using Multi-Class Support Vector Machine from Thorsten Joachims  (see: https://www.cs.cornell.edu/people/tj/svm_light/svm_multiclass.html)

## Installation
* all perl scripts work perfectly on Windows (cygwin) and UNIX systems with Perl 5, version 14, subversion 4, no additional module is required 
* plspm4influence.R relies on R package 'plspm' (see: https://cran.r-project.org/web/packages/plspm/index.html)

## Use
The whole process is launched by... **(comemnt lancer le traitement, différents types de traitment possibles, etc.)**

### Input
The input expected by the programs must take the form... **(format, fichiers, tout ça)**

### Output
The program outputs...  **(même chose)**

## Data

Raw data are available through the official RepLab page: http://nlp.uned.es/replab2014/ (follow: http://nlp.uned.es/replab2014/replab2014-dataset.tar.gz)

RepLab 2014 uses Twitter data in English and Spanish. The balance between both languages depends on the availability of data for each of the profiles included in the dataset.

The training dataset consists of 7,000 Twitter profiles (all with at least 1,000 followers) related to the automotive and banking domains, evaluation is performed separately.
Each profile consists of (i) author name; (ii) profile URL and (iii) the last 600 tweets published by the author at crawling time and have been manually labelled by reputation experts either as “opinion maker” (i.e. authors with reputational influence) or “non-opinion maker”.
The objective is to find out which authors have more reputational influence (who the opinion makers are) and which profiles are less influential or have no influence at all. 

Since Twitter TOS do not allow redistribution of tweets, only tweets ids and screen names are provided. Replab organisers provide details about how to download the tweets.


System outputs are available at: http://figshare.com/articles/ACTIA_png/1506785

## Replication
**Expliquer comment répliquer les traitement décrits dans les papiers.**

## Contact
*Work by:* Jean-Valère Cossu, Nicolas Dugué & Vincent Labatut.

*Corresponding author:* Jean-Valère Cossu <jean-valere.cossu@alumni.univ-avignon.fr>


## References
* **[CDL'15]** Cossu, J.-V.; Dugué, N. & Labatut, V. *Detecting Real-World Influence Through Twitter*, 2nd European Network Intelligence Conference (ENIC), Karlskrona, SE, 2015. http://arxiv.org/abs/1506.05903
* **[CLD'15]** Cossu, J.-V.; Labatut, V. & Dugué, N. *A Review of Features for the Discrimination of Twitter Users*, 2015, submitted to SNAM.
