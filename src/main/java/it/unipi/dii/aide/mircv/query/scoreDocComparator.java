package it.unipi.dii.aide.mircv.query;

import java.util.Comparator;

public class scoreDocComparator implements Comparator<scoreDoc> {

    // Comparator for scoreDoc objects (used in the priority queue) in order to retrieve the top-k results
    @Override
    public int compare(scoreDoc o1, scoreDoc o2) {
        // descending order of score and ascending order of docID
        if (o1.score > o2.score) {
            return -1;
        } else if (o1.score < o2.score) {
            return 1;
        } else {
            if (o1.docID < o2.docID) {
                return -1;
            } else {
                return 1;
            }
        }
    }

}
