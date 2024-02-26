package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.models.CollectionStatistics;
import it.unipi.dii.aide.mircv.models.Configuration;
import it.unipi.dii.aide.mircv.models.PostingList;
import it.unipi.dii.aide.mircv.models.VocabularyElem;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static it.unipi.dii.aide.mircv.text_processing.TextProcessing.DocumentProcessing;
import static it.unipi.dii.aide.mircv.utils.FileUtils.documentIndex;

// receive a query and return the top k  results
public class queryHandler {

    // posting list with the tokens in the query
    public static ArrayList<PostingList> postingListQuery = new ArrayList<>();

    // vocabulary entries of the tokens in the query
    public static HashMap<String, VocabularyElem> VocTerms = new HashMap<>();

    // LRU cache used to store query results
    public static final Cache<ArrayList<String>, PriorityQueue<scoreDoc>> queryCache = CacheBuilder.newBuilder().maximumSize(1000).initialCapacity(1000).build();

    // flag used to enable/disable the cache
    public static boolean cacheFlag = true;

    // receive a query and return the top k (10 or 20) results
    public static void executeQuery(ArrayList<String> tokens, int k) throws IOException {

        // ArrayList of tokens removing duplicates
        ArrayList<String> tokensNoDuplicates = new ArrayList<>();
        for (String token : tokens) {
            if (!tokensNoDuplicates.contains(token)) {
                tokensNoDuplicates.add(token);
            }
        }

        // check if the query is in the cache
        if (cacheFlag == true) {
            PriorityQueue<scoreDoc> documentScores;
            // check if result are in cache and return them
            if ((documentScores = queryCache.getIfPresent(tokensNoDuplicates)) != null) {
                PriorityQueue<scoreDoc> documentScores2 = new PriorityQueue<>(documentScores);
                // print the results
                printResults(documentScores2, k);
                //queryCache.invalidate(tokensNoDuplicates);
                return;
            }
        }

        // retrieve vocabulary entries
        for (String token : tokensNoDuplicates) {
            VocabularyElem Velem;
            Velem = binarySearch(token);

            VocTerms.put(token, Velem);
        }

        // process each token of the query
        for (String token : tokensNoDuplicates) {

            // posting list initialization
            PostingList postingList = new PostingList(token);

            // obtain the posting list for the token
            postingList.getPostingList(token);

            // check if the posting list is empty (the token is not in the vocabulary)
            if (postingList.getPostingList().size() == 0) {
                System.out.println("The token " + token + " is not in the vocabulary");
                if (Configuration.isConjunctiveON()) {
                    // print the results
                    System.out.println("Results for query: ");
                    System.out.println("--------------------------------------------------");
                    System.out.println("No results found");
                    System.out.println("--------------------------------------------------");
                    resetDataStructures();
                    return;
                }
                continue;
            }

            // add the posting list to the posting list with the tokens in the query
            postingListQuery.add(postingList);

        }

        // initialize a priority queue of scoreDoc objects
        PriorityQueue<scoreDoc> priorityQueue;

        // check if the posting list with the tokens in the query is empty
        if (postingListQuery.size() == 0) {
            System.out.println("All the tokens are not in the vocabulary");
            resetDataStructures();
            return;
        }

        // check the type of configuration
        if (Configuration.isConjunctiveON()) {
            // conjunctive
            priorityQueue = utils.conjunctive(k);
        } else {
            // disjunctive
            priorityQueue = utils.disjunctive(k);
        }

        // check if the cache is enabled
        if (cacheFlag == true) {
            // copy priorityQueue into a new priorityQueue
            PriorityQueue<scoreDoc> priorityQueueCopy = new PriorityQueue<>(priorityQueue);
            // save the results in cache
            queryCache.put(tokensNoDuplicates, priorityQueueCopy);
        }

        // print the results
        printResults(priorityQueue, k);

        // reset the data structures
        resetDataStructures();

        priorityQueue.clear();
        VocTerms.clear();
    }

    // process the query, do text processing, check if the # of token > 0 and return the tokens
    public static ArrayList<String> QueryPreProcessing(String query) throws IOException {

        if (query == "") {
            System.out.println("The query is empty! \n");
            return null;
        }

        // do text processing on query
        String[] tokens = DocumentProcessing(query);

        // Transform tokens into an ArrayList<String>
        return new ArrayList<>(Arrays.asList(tokens));
    }

    // print the results of the query
    private static void printResults(PriorityQueue<scoreDoc> priorityQueue, int k) {

        // print the results
        System.out.println("Results for query: ");
        System.out.println("--------------------------------------------------");

        // check if the priority queue is empty
        if (priorityQueue.isEmpty()) {
            System.out.println("No results found");
            return;
        }

        // print the results
        while (!priorityQueue.isEmpty() && k > 0) {
            scoreDoc scoreDoc = priorityQueue.poll();
            k--;
            String pid = documentIndex.get(scoreDoc.getDocID()).getDocno();
            System.out.println("DocNo: " + pid + " Score: " + scoreDoc.getScore());

        }

        System.out.println("--------------------------------------------------");
    }



    // reset the data structures
    private static void resetDataStructures() {

        if (postingListQuery != null)
            postingListQuery.clear();

    }

    // use for testing purposes only: return the top dociD of the query
    public static int returnTopDoc(ArrayList<String> tokens, int k) throws IOException {

        // ArrayList of tokens removing duplicates
        ArrayList<String> tokensNoDuplicates = new ArrayList<>();
        for (String token : tokens) {
            if (!tokensNoDuplicates.contains(token)) {
                tokensNoDuplicates.add(token);
            }
        }

        // retrieve vocabulary entries
        for (String token : tokensNoDuplicates) {
            VocabularyElem Velem;
            Velem = binarySearch(token);

            VocTerms.put(token, Velem);
        }

        // process each token of the query
        for (String token : tokensNoDuplicates) {

            // posting list initialization
            PostingList postingList = new PostingList(token);

            // obtain the posting list for the token
            postingList.getPostingList(token);

            // check if the posting list is empty (the token is not in the vocabulary)
            if (postingList.getPostingList().size() == 0) {
                if (Configuration.isConjunctiveON()) {
                    resetDataStructures();
                    return 0; // no results
                }
                continue;
            }

            // add the posting list to the posting list with the tokens in the query
            postingListQuery.add(postingList);

        }

        // initialize a priority queue of scoreDoc objects
        PriorityQueue<scoreDoc> priorityQueue;

        // check if the posting list with the tokens in the query is empty
        if (postingListQuery.size() == 0) {
            resetDataStructures();
            return 0; // no results
        }

        // check the type of configuration
        if (Configuration.isConjunctiveON()) {
            // conjunctive configuration
            priorityQueue = utils.conjunctive(k);
        } else {
            // disjunctive configuration
            priorityQueue = utils.disjunctive(k);
        }

        // reset the data structures
        resetDataStructures();

        scoreDoc scoreDoc = priorityQueue.poll();
        if (scoreDoc == null)
            return 0; // no results
        else {
            priorityQueue.clear();
            VocTerms.clear();
            return scoreDoc.getDocID();
        }
    }

    // return PriorityQueue<scoreDoc> used for evaluating purposes only
    public static PriorityQueue<scoreDoc> returnPriorityQueue(ArrayList<String> tokens, int k) throws IOException {

        // ArrayList of tokens removing duplicates
        ArrayList<String> tokensNoDuplicates = new ArrayList<>();
        for (String token : tokens) {
            if (!tokensNoDuplicates.contains(token)) {
                tokensNoDuplicates.add(token);
            }
        }

        // retrive vocabulary
        for (String token : tokensNoDuplicates) {
            VocabularyElem Velem;
            Velem = binarySearch(token);

            VocTerms.put(token, Velem);
        }

        // process each token of the query
        for (String token : tokensNoDuplicates) {

            // posting list initialization
            PostingList postingList = new PostingList(token);

            // obtain the posting list for the token
            postingList.getPostingList(token);

            // check if the posting list is empty (the token is not in the vocabulary)
            if (postingList.getPostingList().size() == 0) {
                if (Configuration.isConjunctiveON()) {
                    resetDataStructures();
                    return null; // no results
                }
                continue;
            }

            // add the posting list to the posting list with the tokens in the query
            postingListQuery.add(postingList);

        }

        // initialize a priority queue of scoreDoc objects
        PriorityQueue<scoreDoc> priorityQueue;

        // check if the posting list with the tokens in the query is empty
        if (postingListQuery.size() == 0) {
            resetDataStructures();
            return null; // no results
        }

        // check the type of configuration
        if (Configuration.isConjunctiveON()) {
            // conjunctive configuration
            priorityQueue = utils.conjunctive(k);
        } else {
            // disjunctive configuration
            priorityQueue = utils.disjunctive(k);
        }

        // reset the data structures
        resetDataStructures();

        return priorityQueue;
    }

    // return the vocabulary entry of a term
    public static VocabularyElem binarySearch(String targetTerm) throws IOException {
        // Get the total number of terms in the vocabulary
        FileChannel vocabularyChannel = FileUtils.GetCorrectChannel(-1, 0);
        long totalTerms = (vocabularyChannel.size() / 60);

        // Binary search
        long lowerBound = 0;
        long upperBound = totalTerms;
        long previousMiddlePoint = -1;

        // Binary search on the vocabulary file
        while (true) {
            // Calculate the middle point of the window
            long middlePoint = (upperBound - lowerBound) / 2 + lowerBound;

            // Check if the window is empty
            if (previousMiddlePoint == middlePoint)
                break;

            previousMiddlePoint = middlePoint;

            String middleTermString = getOnlyTerm(middlePoint);
            VocabularyElem middleTerm = null;

            // Compare the target term with the middle term
            int comparisonResult = middleTermString.compareTo(targetTerm);

            if (comparisonResult == 0) {
                // Found
                if (middleTerm == null) {
                    middleTerm = getTermFromDisk(middlePoint);
                }
                return middleTerm;
            } else if (comparisonResult > 0) {
                // Target term is in the left half
                upperBound = middlePoint;
            } else {
                // Target term is in the right half
                lowerBound = middlePoint;
            }
        }

        return null;
    }


    // return the vocabulary entry of a term (used in the binary search)
    private static VocabularyElem getTermFromDisk(long from) throws IOException {
        from= from * 60;
        FileChannel channel = FileUtils.GetCorrectChannel(-1, 0);
        // creating ByteBuffer for reading term
        ByteBuffer buffer = ByteBuffer.allocate(20);
        channel.position(from);

        while (buffer.hasRemaining())
            channel.read(buffer);

        String term = new String(buffer.array(), StandardCharsets.UTF_8).trim();

        buffer = ByteBuffer.allocate(4 + 4 + 8 + 8 + 4 + 4  + 8 );

        while (buffer.hasRemaining())
            channel.read(buffer);

        buffer.rewind(); // reset the buffer position to 0
        int DocFreq = buffer.getInt();
        int CollFreq = buffer.getInt();
        long docIdsOffset = buffer.getLong();
        long termFreqOffset = buffer.getLong();
        int docIdsLen = buffer.getInt();
        int termFreqLen =  buffer.getInt();
        double idf = buffer.getDouble();
        VocabularyElem v = new VocabularyElem(term, DocFreq, CollFreq, docIdsOffset, termFreqOffset, docIdsLen, termFreqLen);
        v.setIdf(idf);
        //System.out.println("VocabularyElem read from disk: " + v);
        return v;

    }

    // return the term (used in the binary search)
    private static String getOnlyTerm(long from) throws IOException {
        from= from * 60;
        FileChannel channel = FileUtils.GetCorrectChannel(-1, 0);

        // creating ByteBuffer for reading term
        ByteBuffer buffer = ByteBuffer.allocate(20);
        channel.position(from);

        while (buffer.hasRemaining())
            channel.read(buffer);

        String term = new String(buffer.array(), StandardCharsets.UTF_8).trim();

        return term;

    }
}