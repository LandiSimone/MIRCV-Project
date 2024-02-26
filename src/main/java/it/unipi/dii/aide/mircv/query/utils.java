package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.models.*;
import it.unipi.dii.aide.mircv.utils.FileUtils;

import java.io.IOException;
import java.util.*;

import static it.unipi.dii.aide.mircv.query.queryHandler.VocTerms;
import static it.unipi.dii.aide.mircv.utils.FileUtils.collectionStatistics;

public class utils {

    // get the min docID from the posting list of the query
    public static int getMinDocID() {
        int minDocID = Integer.MAX_VALUE;
        int docIDcmp;

        for (PostingList postingList : queryHandler.postingListQuery) {
            docIDcmp = postingList.getPostingList().get(0).getDocID();
            if (docIDcmp < minDocID) {
                minDocID = docIDcmp;
            }
        }
        return minDocID;
    }

    // conjunctive query: all the terms must be present in the document
    public static PriorityQueue<scoreDoc> conjunctive(int k) throws IOException {

        // initialize the priority queue with score in descending order --> top k documents
        PriorityQueue<scoreDoc> scoreDocsDecreasing = new PriorityQueue<>(k, new scoreDocComparator());

        // initialize the priority queue with score in increasing order --> used to get the k-th score
        PriorityQueue<scoreDoc> scoreDocsIncreasing = new PriorityQueue<>(k, new scoreDocComparatorIncreasing());

        // initialize the score
        double score = 0;

        // next docID
        int nextDocID;

        // current docID
        int currentDocID;

        // get the min docID
        currentDocID = getMinDocID();

        // variable used to check if the docID is present in all the posting lists
        int present;

        // check all the docIDs in the posting lists
        while (true) {

            // reset the score
            score = 0;
            // update the next docID
            nextDocID = collectionStatistics.getDocCount();
            // update the presence
            present = 0;

            // iterate over the posting lists of the query
            for (PostingList postingList : queryHandler.postingListQuery) {

                // check if the posting list is empty
                if (postingList.getCurrentPostingList() == null) {
                    continue;
                }

                // check if the docID is the same
                if (postingList.getCurrentPostingList().getDocID() == currentDocID) {

                    // update the variable
                    present+=1;

                    // update the score
                    if (Configuration.isScoreON()) {
                        // BM25
                        score += BM25(postingList.getTerm(), postingList.getCurrentPostingList(), 1.2, 0.75);
                    } else {
                        // TF-IDF
                        score += TFIDF(postingList.getTerm(), postingList.getCurrentPostingList());
                    }

                    // get the next posting
                    postingList.nextPosting(postingList.getTerm());
                }

                // check if the posting list is empty
                if (postingList.getCurrentPostingList() == null) {
                    continue;
                }

                // update the next docID
                if (postingList.getCurrentPostingList().getDocID() < nextDocID) {
                    nextDocID = postingList.getCurrentPostingList().getDocID();
                }
            }

            // check if the docID is present in all the posting lists (present equal to the number of terms in the query)
            if (present == queryHandler.postingListQuery.size()) {

                // update the priority queue
                if (scoreDocsDecreasing.size() < k) {
                    // add the scoreDoc to the priority queue
                    scoreDocsDecreasing.add(new scoreDoc(currentDocID, score));
                    scoreDocsIncreasing.add(new scoreDoc(currentDocID, score));
                } else {
                    // check if the score is greater than the minimum score in the priority queue
                    if (scoreDocsIncreasing.peek().getScore() < score) {
                        // add the scoreDoc to the priority queue
                        scoreDocsDecreasing.add(new scoreDoc(currentDocID, score));
                        scoreDocsIncreasing.add(new scoreDoc(currentDocID, score));

                        // remove the minimum score
                        scoreDocsDecreasing.remove(scoreDocsIncreasing.poll());
                    }
                }
            }

            // check if no more docID to process
            if (currentDocID == nextDocID || nextDocID == collectionStatistics.getDocCount()){
                return scoreDocsDecreasing;
            }

            // update the current docID
            currentDocID = nextDocID;
        }

    }

    // disjunctive query: at least one term must be present in the document
    public static PriorityQueue<scoreDoc> disjunctive(int k) throws IOException {

        // initialize the priority queue with score in descending order --> top k documents
        PriorityQueue<scoreDoc> scoreDocsDecreasing = new PriorityQueue<>(k, new scoreDocComparator());

        // initialize the priority queue with score in increasing order --> used to get the k-th score
        PriorityQueue<scoreDoc> scoreDocsIncreasing = new PriorityQueue<>(k, new scoreDocComparatorIncreasing());

        // initialize the score
        double score = 0;

        // next docID
        int nextDocID;

        // current docID
        int currentDocID;

        // get the min docID
        currentDocID = getMinDocID();

        // check all the docIDs in the posting lists
        while (true) {
            // reset the score
            score = 0;
            // update the next docID
            nextDocID = collectionStatistics.getDocCount();

            // iterate over the posting lists of the query
            for (PostingList postingList : queryHandler.postingListQuery) {

                // check if the posting list is empty
                if (postingList.getCurrentPostingList() == null) {
                    continue;
                }

                // check if the docID is the same
                if (postingList.getCurrentPostingList().getDocID() == currentDocID) {

                    // update the score
                    if (Configuration.isScoreON()) {
                        // BM25
                        score += BM25(postingList.getTerm(), postingList.getCurrentPostingList(), 1.2, 0.75);
                    } else {
                        // TF-IDF
                        score += TFIDF(postingList.getTerm(), postingList.getCurrentPostingList());
                    }

                    // next posting
                    postingList.nextPosting(postingList.getTerm());

                }

                // check if the posting list is empty
                if (postingList.getCurrentPostingList() == null) {
                    continue;
                }

                // update the next docID
                if (postingList.getCurrentPostingList().getDocID() < nextDocID) {
                    nextDocID = postingList.getCurrentPostingList().getDocID();
                }

            }

            // update the priority queue
            if (scoreDocsDecreasing.size() < k) {
                // add the scoreDoc to the priority queue
                scoreDocsDecreasing.add(new scoreDoc(currentDocID, score));
                scoreDocsIncreasing.add(new scoreDoc(currentDocID, score));
            } else {
                // check if the score is greater than the minimum score in the priority queue
                if (scoreDocsIncreasing.peek().getScore() < score) {
                    // add the scoreDoc to the priority queue
                    scoreDocsDecreasing.add(new scoreDoc(currentDocID, score));
                    scoreDocsIncreasing.add(new scoreDoc(currentDocID, score));

                    // remove the minimum score
                    scoreDocsDecreasing.remove(scoreDocsIncreasing.poll());
                }
            }

            // check if no more docID to process
            if (currentDocID == nextDocID || nextDocID == collectionStatistics.getDocCount()) {
                return scoreDocsDecreasing;
            }

            // update the current docID
            currentDocID = nextDocID;

        }

    }

    // TF-IDF score
    private static double TFIDF(String term, Posting currentPostingList) {
        // term frequency weight
        double tf = currentPostingList.getTermFreq();
        double tfWeight = 1 + Math.log10(tf);

        // inverse document frequency
        double idf = VocTerms.get(term).getIdf();

        double TFIDF = tfWeight * idf;

        return TFIDF;
    }

    // BM25 score
    private static double BM25(String term, Posting currentPostingList, double k, double b) {
        // get term frequency of the posting list
        int tf = currentPostingList.getTermFreq();

        // inverse document frequency
        double idf = VocTerms.get(term).getIdf();

        // average document length
        double avgDocLen = CollectionStatistics.getAvgDocLen();

        double BM25 = idf * (tf / (k * ((1 - b) + (b * (FileUtils.documentIndex.get(currentPostingList.getDocID()).getLength() / avgDocLen))) + tf));

        return BM25;
    }

}
