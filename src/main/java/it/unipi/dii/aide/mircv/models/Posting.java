package it.unipi.dii.aide.mircv.models;

import java.nio.channels.FileChannel;

public class Posting {

    // document ID
    private final int docID;

    // term frequency in the document
    private final int termFreq;

    public Posting(int docID, int termFreq) {
        this.docID = docID;
        this.termFreq = termFreq;
    }

    public int getDocID() {
        return docID;
    }

    public int getTermFreq() {
        return termFreq;
    }

    @Override
    public String toString() {
        return "Posting{" +
                "docID=" + docID +
                ", termFreq=" + termFreq +
                '}';
    }

}
