Characterization of Twitter Profiles, with an application to offline influence detection
==============================
## Presentation
These scripts are meant to extract certain features from raw Twitter data describing Twitter users (tweets, profile info, as well as external data). Once the features are extracted, various forms of SVMs are trained, and logistic regressions are performed, to classify and rank the users. These operations are conducted on different subgroups of features. The details of the process are given in [CDL'15] and [CLD'15]. The scripts were applied to the classification/ranking of Twitter users in terms of *offline* influence, based on the [RepLab 2014 dataset](http://nlp.uned.es/replab2014/).

Please, cite [CLD'15] if you use our scripts. We would also be very interested to know your context of application and/or modification, so please, let us know.

Note the software may evolve depending on our future research work.

## Organization
The project is composed of the following scripts:
* `cosine_bot_xx.pl` scripts concern the Bag-of-Tweets based user classification. These scripts compute the probability of each tweet of a given user to be written by an influencer. They then associate to each user a score of being an influencer according 2 methods ('xx' can be replaced by Sum or Count).
* `plspm4influence.R` aims at finding relations between features categories and verify the efficiency of the proposed influence conceptual model.

Here are the third-party softwares used in this version:
* Part of SVMs-based experiments were made using [Multi-Class Support Vector Machine]((https://www.cs.cornell.edu/people/tj/svm_light/svm_multiclass.html)) from Thorsten Joachims.
* `plspm4influence.R` relies on the R package [`plspm`](https://cran.r-project.org/web/packages/plspm/index.html).

## Installation
* All perl scripts work perfectly on Windows (via Cygwin) and Unix systems with Perl 5, version 14, subversion 4, no additional module is required.
* To use `plspm4influence.R` just run: the following commands: 1) `install.packages("plspm")` to install the package and 2) `library("plspm")` at the begining of the script to use the library.
* To use *Multi-Class Support Vector Machine*, just download binaries at the following address: http://www.cs.cornell.edu/people/tj/svm_light/svm_multiclass.html

### Use and Input
* `cosine_bot_*.pl` scripts expect two text files (*training* and *test* set) as input, formatted as follows: `tweet_id`, `user_id`, `domain_id`, `language`, (3 unused fields), `tweet_content`, `reference_tag` (for influence), and an unused field. The script will load files in memory and build the model before cleaning the memory as it is running (6 GB RAM would be OK). The script only uses one core/thread and would be more or less fast depending on the CPU maximum frequency.
* `cosine_uad_*.pl` scripts expect two text files (*training* and *test* set) as input, formatted as follows: `user_id`, `reference_tag` (for influence), an unused field, `user_document` and the *number of tweets* in the domain/language selected (the domains are $Automotive$ and $Banking$, the languages are *English* and *Spanish*). The script will load files in memory and build the model before cleaning the memory as it is running. The script only uses one core/thread and would be more or less fast depending on the CPU maximum frequency. To complete the whole process you have to launch the script for each couple domain/language.
* `plspm4influence.R` expects as input format a text file, whose first field is the *user id*, the second one is the *reference tag* associated to each user (the field separator is a single tab), the next fields are then the *variable* you target for analysis. You can access each variable in the source code with its columun index. The first line (the header) contains *fields ID*.
* To use `plspm4influence.R` just run the source through R and query R on the variable you are interested in to get more details about it. The script produces by itself *internal* and *external* models figures. To select another domain, just change the data file name at the beginning of the script.

### Output
The program outputs...  **(à écrire)**

## Data
Raw data are available through the official RepLab page: http://nlp.uned.es/replab2014/

(direct link: http://nlp.uned.es/replab2014/replab2014-dataset.tar.gz).

RepLab 2014 uses Twitter data in English and Spanish. The balance between both languages depends on the availability of data for each of the profiles included in the dataset.

The training dataset consists of 7,000 Twitter profiles (all with at least 1,000 followers) related to the *Automotive* and *Banking* domains, evaluation is performed separately.
Each profile consists of (i) author name; (ii) profile URL and (iii) the last 600 tweets published by the author at crawling time, and have been manually labelled by reputation experts either as “opinion maker” (i.e. authors with reputational influence) or “non-opinion maker”. The objective is to find out which authors have more reputational influence (who the opinion makers are) and which profiles are less influential or have no influence at all. 

Since Twitter TOS do not allow redistribution of tweets, only tweets IDs and screen names are provided. RepLab organizers provide details about how to download the tweets.

The system outputs from our scripts for these data are freely available on Figshare: http://figshare.com/articles/ACTIA_png/1506785

## Replication
**Expliquer comment répliquer les traitement décrits dans les papiers.**

## Contact
*Work by:* Jean-Valère Cossu, Nicolas Dugué & Vincent Labatut.

*Corresponding author:* Jean-Valère Cossu <jean-valere.cossu@alumni.univ-avignon.fr>


## References
* **[CDL'15]** Cossu, J.-V.; Dugué, N. & Labatut, V. *Detecting Real-World Influence Through Twitter*, 2nd European Network Intelligence Conference (ENIC), Karlskrona, SE, 2015. http://arxiv.org/abs/1506.05903
* **[CLD'15]** Cossu, J.-V.; Labatut, V. & Dugué, N. *A Review of Features for the Discrimination of Twitter Users*, 2015, submitted to SNAM.
