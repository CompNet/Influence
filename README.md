TwitterInfluence
==============================
*Characterization of Twitter Profiles, with an application to offline influence detection*

* Copyright 2014-15 Jean-Valère Cossu, Nicolas Dugué & Vincent Labatut

`TwitterInfluence` is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation. For source availability and license information see `licence.txt`

* **Lab site:** http://lia.univ-avignon.fr
* **GitHub repo:** https://github.com/CompNet/Influence
* **Contact:** Jean-Valère Cossu <jean-valere.cossu@alumni.univ-avignon.fr>
 
-------------------------------------------------------------------------

## Description
These scripts are meant to extract certain features from raw Twitter data describing Twitter users (tweets, profile info, as well as external data). Once the features are extracted, various forms of SVMs are trained, and logistic regressions are performed, to classify and rank the users. These operations are conducted on different subgroups of features. The details of the process are given in [[CDL'15](#references)] and [[CLD'16](#references)]. The scripts were applied to the classification/ranking of Twitter users in terms of *offline* influence, based on the [RepLab 2014 dataset](http://nlp.uned.es/replab2014/).

Please, cite [[CLD'16](#references)] if you use our scripts. We would also be very interested to know your context of application and/or modification, so please, let us know.
```bibtex
@Article{Cossu2016,
  author        = {Cossu, Jean-Valère and Labatut, Vincent and Dugué, Nicolas},
  title         = {A Review of Features for the Discrimination of {Twitter} Users: Application to the Prediction of Offline Influence},
  journal       = {Social Network Analysis and Mining},
  year          = {2016},
  volume        = {6},
  pages         = {25},
  doi           = {10.1007/s13278-016-0329-x},
}
```

Note that the software may evolve depending on our future research work.


## Organization
The project is composed of the following folders:
* `preprocessing`: programs related to the preparation of the data.
  * `retrieval`: Java classes used to retrieve additional Twitter and Klout data
    * `Main.java`: allows to obtain additional features related to a Twitter profile.
    * `MainGetTweets.java`: allows to obtain tweets posted by users
  * `cooccurrence`: programs aiming at extracting cooccurrence networks and the related features.
    * `net-extraction.R`: extracts the cooccurrence networks based on a collection of documents, each one corresponding to all the tweets published by a user (*User-as-Document* approach). The script also computes the vector features based on topological centrality measures, through the `igraph` library.
    * `net-distance.R`: processes the distance between each pair of cooccurrence network (each network corresponds to a user).
    * `DistanceProcessor.java`: same as above, but in Java instead of R, and much faster. The program is multithreaded, by comparison `SerialDistanceProcessor.java` performs the same processing in a non-parallel way.
* `processing`: programs implementing the classification and ranking tasks, as well as their evaluation.
  * `cosine_bot_xxx.pl` scripts concern the Bag-of-Tweets based user classification. These scripts compute the probability of each tweet of a given user to be written by an influencer. They then associate to each user a score of being an influencer according 2 methods `xx` can be replaced by `Sum` or `Count`).
* `postprocessing`: programs used to analyze and convert the processing results
  * `regression`: PLS-PM scripts, used to study more thoroughly the relation between certain features and the class.
    * `plspm4influence.R` : this script aims at finding relations between features categories and verify the efficiency of the proposed influence conceptual model.
  * `format`: scripts related to the format of the input data.
    * `format_trec_xxx(.multi).pl`: these scripts convert the RepLab data to the TREC format.

Here are the third-party softwares used in this version:
* Part of SVMs-based experiments were made using [Multi-Class Support Vector Machine](https://www.cs.cornell.edu/people/tj/svm_light/svm_multiclass.html) by Thorsten Joachims.
* `plspm4influence.R` relies on the [`plspm`](https://cran.r-project.org/web/packages/plspm/index.html). R package 
* `net-extraction.R` relies on the [`igraph`](https://cran.r-project.org/web/packages/igraph/index.html) R package.
* `net-distance.R` and `net-extraction.R` use the [`foreach`](https://cran.r-project.org/web/packages/foreach/index.html) and [`doParallel`](https://cran.r-project.org/web/packages/doParallel/index.html) R packages.


## Installation
* The Java project may be imported into all IDE (Eclipse, IntelliJ or Netbeans) and then built. It can also be built by running: `javac MainGetTweets.java` and `javac Main.java` 
* All perl scripts work perfectly on Windows (via Cygwin) and Unix systems with Perl 5, version 14, subversion 4, no additional module is required.
* To use *Multi-Class Support Vector Machine*, just download binaries at the following address: http://www.cs.cornell.edu/people/tj/svm_light/svm_multiclass.html
* To use the R scripts, just install regularly the appropriate packages, using the command `install.packages("xxxxx")` for package `xxxx`.
* Logistic regression, Random Forest, PCA and SVM can also be runned by using Python Script with the [Sci-kit Lean Lib]((http://scikit-learn.org/stable/))


## Use and Input


### Folder `retrieval`
To get Twitter profile data, run `java Main fileWithToken fileWithAccounts fileToWriteAccounts` with: 
* `fileWithToken` a file containing Twitter tokens, 
* `fileWithAccounts` a file containing the Twitter accounts to crawl,
* `fileToWriteAccounts` the file name where to write the results.

To get tweets from specific Twitter accounts, run `java MainGetTweets fileWithToken fileWithAccounts fileToWriteAccounts` with:
* `fileWithToken` a file containing Twitter tokens, 
* `fileWithAccounts` a file containing the Twitter accounts to crawl,
* 'fileToWriteAccounts' the file name where to write the results.

The `fileWithToken` file has to be written as follows : 
```
twitterAccountName	1346266278-3orvewl5mfCO1xfEEt1gN064uWnjyNyGRDzHO6c	1r65PNGNh62dUg28M8eyUJNkXekomzWNyguSXXqW6Q
```
with one token per line. The `fileWithAccounts` file has to contain one user id per line.
 

### Folder `cooccurrence`
If you want to use the cooccurrence-based features, you need first to apply the `net-extraction.R` script. But before, you must edit it to set up the variables `in.folder` (input folder containing a collection of text documents, each one corresponding to the concatenation of the all the tweets published by a given user), `out.folder` (output folder, in which all the produced data files will be placed) and `log.file` at the beginning. The produced files include the list of terms for the whole collection (`terms.txt`), and for each user: the list of terms used by the user (`localterms.txt`), the user term cooccurrence matrix (`cooccurrences.txt`), the cooccurrence networks with all the nodal centrality measures at the Graphml format (`wordnetwork.graphml`), the same centrality vectors separately in a text file (`local.features.txt`) and their average values for the whole network as well as other global network measures such as the density `global.features.txt`). 

The distance between cooccurrence networks can be processed using either the `net-distance.R` R script or the `SerialDistanceProcessor.java` class (faster). For the R script, like before you must set up the variables at the beginning. For the Java class, the same modifications must be performed in the `main` method. 


### Folder `processing`
The `cosine_bot_xxx.pl` scripts expect two text files (*training* and *test* set) as input, formatted as follows: `tweet_id`, `user_id`, `domain_id`, `language`, (3 unused fields), `tweet_content`, `reference_tag` (for influence), and an unused field. The script will load files in memory and build the model before cleaning the memory as it is running (6 GB RAM would be OK). The script only uses one core/thread and would be more or less fast depending on the CPU maximum frequency.

The `cosine_uad_xxx.pl` scripts expect two text files (*training* and *test* set) as input, formatted as follows: `user_id`, `reference_tag` (for influence), an unused field, `user_document` and the *number of tweets* in the domain/language selected (the domains are $Automotive$ and $Banking$, the languages are *English* and *Spanish*). The script will load files in memory and build the model before cleaning the memory as it is running. The script only uses one core/thread and would be more or less fast depending on the CPU maximum frequency. To complete the whole process you have to launch the script for each couple domain/language.


### Folder `postprocessing`
The `plspm4influence.R` script expects as input format a text file, whose first field is the *user id*, the second one is the *reference tag* associated to each user (the field separator is a single tab), the next fields are then the *variable* you target for analysis. You can access each variable in the source code with its columun index. The first line (the header) contains *fields ID*. To use `plspm4influence.R` just run the source through R and query R on the variable you are interested in to get more details about it. The script produces by itself *internal* and *external* models figures. To select another domain, just change the data file name at the beginning of the script.

Outputs from `cosine_xxx.pl` scripts are translated to the TREC-EVAL tool format using the corresponding `format_trec_xxx.pl` script. The TREC-EVAL format is the following: `domain_id`, `unused_field`, `user_id`, `user_rank`, `score`, `system_name`.


## Data
Raw data are available through the official RepLab page: http://nlp.uned.es/replab2014/ (direct link: http://nlp.uned.es/replab2014/replab2014-dataset.tar.gz).`

RepLab 2014 uses Twitter data in English and Spanish. The balance between both languages depends on the availability of data for each of the profiles included in the dataset.

The training dataset consists of 7,000 Twitter profiles (all with at least 1,000 followers) related to the *Automotive* and *Banking* domains, evaluation is performed separately.
Each profile consists of (i) author name; (ii) profile URL and (iii) the last 600 tweets published by the author at crawling time, and have been manually labelled by reputation experts either as “opinion maker” (i.e. authors with reputational influence) or “non-opinion maker”. The objective is to find out which authors have more reputational influence (who the opinion makers are) and which profiles are less influential or have no influence at all. 

Since Twitter TOS do not allow redistribution of tweets, only tweets IDs and screen names are provided. RepLab organizers provide details about how to download the tweets.

The system outputs from our scripts for these data are freely available on [Zenodo](https://doi.org/10.5281/zenodo.6815449).


## References
 * **[CLD'16]** J.-V. Cossu, N. Dugué & V. Labatut. *A Review of Features for the Discrimination of Twitter Users: Application to the Prediction of Offline Influence*, Social Network Analysis and Mining, 6(1):25, 2016. DOI: [10.1007/s13278-016-0329-x](https://doi.org/10.1007/s13278-016-0329-x) - [⟨hal-01203171⟩](https://hal.archives-ouvertes.fr/hal-01203171)
 * **[CDL'15]** J.-V. Cossu, N. Dugué & V. Labatut. *Detecting Real-World Influence Through Twitter*, 2nd European Network Intelligence Conference (ENIC), 2015, 228-233. DOI: [10.1109/ENIC.2015.20](https://doi.org/10.1109/ENIC.2015.20) - [⟨hal-01164453⟩](https://hal.archives-ouvertes.fr/hal-01164453)
