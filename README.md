


# MIRCV Search Engine 

In this project, a search engine was developed based on the MSMARCO Passages collection, which is available on https://microsoft.github.io/msmarco/TREC-Deep-Learning-2020. The project consists of three main parts:

* **Inverted Index Creation**: the first part involves creating an inverted index structure from a set of text documents.


* **Query Processing**: the second part focuses on processing queries over the inverted index through a terminal interface.


* **Search Engine Evaluation**: the final part involves evaluating the search engine's performance using standard evaluation tool TrecEval.

# Getting Started

###   Setup:
Before running, the collection path should be added to the Collection_Compressed_path or Collection_Uncompressed_path field of **Configuration JSON** file.

**Configuration JSON** file: `src/main/java/it/unipi/dii/aide/mircv/resources/configuration.json`



### Inverted Index Creation:

The executable file is inside `src/main/java/en/unipi/dii/aide/mircv/indexer/Indexer.java` and it autumatically generates the inverted index according to the options set in the Json File.
- read_compression",
- stemming_stopword",
- index_compression'


All the final structures will be saved in src/main/resources along with a **log.txt** showing the size and timing.

### Query Processing:
The executable file is inside `src/main/java/en/unipi/dii/aide/mircv/Main.java`
when launched through a terminal interface, it is possible to set the type of query to be executed (conjunctive or disjunctive), the scoring function to be used (BM25 or TFIDF) and the desired number of results.

- After configuration, the programme accepts queries iteratively
- config : if you wish to change the configuration
- exit : terminate the search

### Search Engine Evaluation:
The executable file is inside `src/main/java/en/unipi/dii/aide/mircv/utils/evaluation.java`
and when launched, tests can be run iteratively by changing configurations from the command line. 

The results are displayed both from the terminal and saved in `src/main/java/en/unipi/di/aide/mircv/resources/evaluation.txt`

The evaluation results were obtained using a standard collection TREC DL 2020 queries and TREC DL 2020 qrels.

 
