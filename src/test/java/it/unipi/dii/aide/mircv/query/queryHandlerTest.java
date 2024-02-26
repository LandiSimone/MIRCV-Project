package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.models.Configuration;
import it.unipi.dii.aide.mircv.models.Posting;
import it.unipi.dii.aide.mircv.models.PostingList;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static it.unipi.dii.aide.mircv.query.queryHandler.executeQuery;
import static it.unipi.dii.aide.mircv.utils.FileUtils.loadFinalStructure;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class queryHandlerTest {

    @Test
    void executeQuery() throws IOException {

        FileUtils.takeFinalRAF();
        loadFinalStructure();

        // create a String as a query
        String query1 = "manhattan discussions"; // doc 1-10 with manhattan, doc 11 with discussions
        String query2 = "discussions"; // only doc 11
        String query3 = "army component"; // doc 4-9 with army, doc 8 with component and army

        ArrayList<String> tokens1 = queryHandler.QueryPreProcessing(query1);
        ArrayList<String> tokens2 = queryHandler.QueryPreProcessing(query2);
        ArrayList<String> tokens3 = queryHandler.QueryPreProcessing(query3);

        System.out.println("Query tokens: " + tokens1);
        System.out.println("Query tokens: " + tokens2);
        System.out.println("Query tokens: " + tokens3);

        // BM25 + Conjunctive
        Configuration configuration = new Configuration();
        configuration.setScoreON(true); // BM25
        configuration.setConjunctiveON(true); // Conjunctive

        // expected 0: not found
        //assertEquals(0, queryHandler.returnTopDoc(tokens1, 1));
        // expected 11
        //assertEquals(11, queryHandler.returnTopDoc(tokens2, 1));
        // expected 8
        //assertEquals(8, queryHandler.returnTopDoc(tokens3, 1));

        // TFIDF + Conjunctive
        configuration.setScoreON(false); // TFIDF
        configuration.setConjunctiveON(true); // Conjunctive

        // expected 0: not found
        //assertEquals(0, queryHandler.returnTopDoc(tokens1, 1));
        // expected 11
        //assertEquals(11, queryHandler.returnTopDoc(tokens2, 1));
        // expected 8
        //assertEquals(8, queryHandler.returnTopDoc(tokens3, 1));

        // BM25 + Disjunctive
        configuration.setScoreON(true); // BM25
        configuration.setConjunctiveON(false); // Disjunctive

        // expected 3
        //assertEquals(3, queryHandler.returnTopDoc(tokens1, 1));
        // expected 11
        //assertEquals(11, queryHandler.returnTopDoc(tokens2, 1));
        // expected 8
        //assertEquals(8, queryHandler.returnTopDoc(tokens3, 1));

        // TFIDF + Disjunctive
        configuration.setScoreON(false); // TFIDF
        configuration.setConjunctiveON(false); // Disjunctive

        // expected 3
        //assertEquals(3, queryHandler.returnTopDoc(tokens1, 1));
        // expected 11
        //assertEquals(11, queryHandler.returnTopDoc(tokens2, 1));
        // expected 8
        assertEquals(8, queryHandler.returnTopDoc(tokens3, 1));
    }

}