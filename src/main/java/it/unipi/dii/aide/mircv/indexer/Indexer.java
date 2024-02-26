package it.unipi.dii.aide.mircv.indexer;

import it.unipi.dii.aide.mircv.models.*;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import static it.unipi.dii.aide.mircv.compression.unary.readTermFreqCompressed;
import static it.unipi.dii.aide.mircv.compression.variableByte.readDocIdsCompressed;

// Inverted Index Creation: creating an inverted index structure from collection
public class Indexer {
    // numero di blocchi generati
    static Integer blockNumber = 0;
    // tempo di esecuzione
    static long startTime,stopTime, elapsedTimeSpimi, elapsedTimeMerger;

    public static void main(String[] args) throws IOException {
        System.out.println("> Start Indexing ...");

        // delete old index files
        FileUtils.clearDataFolder();

        // misura il tempo di esecuzione
        startTime = System.currentTimeMillis();

        // SPIMI
        Spimi spimi = new Spimi();
        blockNumber= spimi.startIndexer();

        System.out.println("Number of blocks generated: " + blockNumber);
        stopTime = System.currentTimeMillis();
        elapsedTimeSpimi = stopTime - startTime;

        blockNumber +=1;
        startTime = System.currentTimeMillis();

        // MERGER
        Merger merger = new Merger();
        merger.startMerger(blockNumber);

        stopTime = System.currentTimeMillis();
        elapsedTimeMerger = stopTime - startTime;

        // remove all partial file
        FileUtils.removePartialFiles();

        // salva su log file tutti i tempi di esecuzione, il numero di blocchi e la dimensione totale dei file finali
        FileUtils.saveLog(elapsedTimeSpimi, elapsedTimeMerger, blockNumber);

//        if(Configuration.isTesting()) {
//            // print the final structure
//            PlotFinalStructure();
//        }

    }


    // used for tesing, read according to the compression flag the final structure : vocabulary, posting lists, collection statistics
    public static void PlotFinalStructure() throws IOException {
        System.out.println("> Plotting final Structure ...");
        // offset corrente
        long currentOffset = 0;
        long vocabularySize = 60;

        // take channel
        FileChannel channelVocabulary = FileUtils.GetCorrectChannel(-1, 0);
        FileChannel channelDocID = FileUtils.GetCorrectChannel(-1, 1);
        FileChannel channelTermFreq = FileUtils.GetCorrectChannel(-1, 2);

        // take the size of the file
        while (true) {
            try {
                if (!((currentOffset + vocabularySize) < channelVocabulary.size())) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // read the vocabulary element
            VocabularyElem vocabularyElem = new VocabularyElem();
            vocabularyElem.readFromDisk(channelVocabulary, currentOffset);

            // print the vocabulary element
            System.out.println(vocabularyElem);

            // update the offset
            currentOffset += vocabularySize;

            // read the posting list
            PostingList postingList = new PostingList(vocabularyElem.getTerm());

            if(Configuration.isIndex_compressionON()){
                // read + decompress the posting list : docIds [Vbyte] and termFreqs [Unary]

                // unary
                ArrayList<Integer> termFreqs = readTermFreqCompressed(channelTermFreq, vocabularyElem.getTermFreqOffset(), vocabularyElem.getTermFreqLen());

                // vbyte
                ArrayList<Integer> docIds = readDocIdsCompressed(channelDocID, vocabularyElem.getDocIdsOffset(), vocabularyElem.getDocIdsLen());

                // assemble the posting list
                for(int i = 0; i < docIds.size(); i++){
                    Posting p = new Posting(docIds.get(i), termFreqs.get(i));
                    postingList.addPosting(p);
                }
            }
            else
                postingList.readFromDisk(channelDocID, channelTermFreq, vocabularyElem.getDocIdsOffset(), vocabularyElem.getTermFreqOffset(), vocabularyElem.getDocIdsLen(), vocabularyElem.getTermFreqLen());

            // print the posting list
            System.out.println(postingList );

            // aspetta input tastira ogni 10000 elementi
            if (currentOffset % 600000 == 0) {
                System.out.println("Press Enter to continue...");
                try {
                    System.in.read();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        // print the collection statistics
        CollectionStatistics collectionStatistics = new CollectionStatistics();
        collectionStatistics.readFromDisk(FileUtils.GetCorrectChannel(-1, 3), 0);
        System.out.println(collectionStatistics);
    }

    // used for testing, print the document index
    public static void printDocumentIndex() throws IOException {
        int position = 0;
        DocumentIndexElem doc_elem = new DocumentIndexElem();

        FileChannel DocIndexFC = new RandomAccessFile(FileUtils.Path_DocumentIndex, "rw").getChannel();

        while (position < (int)(DocIndexFC.size())) {
            System.out.println("Position: " + position);
            doc_elem = new DocumentIndexElem();
            doc_elem.readFromDisk(DocIndexFC, position);
            System.out.println(doc_elem.toString());
            System.out.printf(" DocNo: %s, Lenght: %d\n", doc_elem.getDocno(), doc_elem.getLength());

            position += 28;
        }
    }

    // used for testing, given term , print the posting list and the vocabulary element
    public static void printPostingList(String term) throws IOException {
        // take channel
        FileChannel channelVocabulary = FileUtils.GetCorrectChannel(-1, 0);
        FileChannel channelDocID = FileUtils.GetCorrectChannel(-1, 1);
        FileChannel channelTermFreq = FileUtils.GetCorrectChannel(-1, 2);

        // offset corrente
        long currentOffset = 0;
        long vocabularySize= 60;

        // take the size of the file
        while (true) {
            try {
                if (!((currentOffset + vocabularySize) < channelVocabulary.size())) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // read the vocabulary element
            VocabularyElem vocabularyElem = new VocabularyElem();
            vocabularyElem.readFromDisk(channelVocabulary, currentOffset);

            // update the offset
            currentOffset += vocabularySize;

            // check if the term is the one we are looking for
            if (vocabularyElem.getTerm().equals(term)) {
                // print the vocabulary element
                System.out.println(vocabularyElem);

                // read the posting list
                PostingList postingList = new PostingList(vocabularyElem.getTerm());
                postingList.readFromDisk(channelDocID, channelTermFreq, vocabularyElem.getDocIdsOffset(), vocabularyElem.getTermFreqOffset(), vocabularyElem.getDocIdsLen(), vocabularyElem.getTermFreqLen());

                // print the posting list
                System.out.println(postingList + "\n");
            }
        }
    }

    // used for testing, given term , retrive the PostingList
    public static PostingList getTestPosting(String term) throws IOException {
        // take channel
        FileChannel channelVocabulary = FileUtils.GetCorrectChannel(-1, 0);
        FileChannel channelDocID = FileUtils.GetCorrectChannel(-1, 1);
        FileChannel channelTermFreq = FileUtils.GetCorrectChannel(-1, 2);

        // offset corrente
        long currentOffset = 0;
        long vocabularySize= 60;

        // take the size of the file
        while (true) {
            try {
                if (!((currentOffset + vocabularySize) < channelVocabulary.size())) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // read the vocabulary element
            VocabularyElem vocabularyElem = new VocabularyElem();
            vocabularyElem.readFromDisk(channelVocabulary, currentOffset);

            // update the offset
            currentOffset += vocabularySize;

            // check if the term is the one we are looking for
            if (vocabularyElem.getTerm().equals(term)) {
                // read the posting list
                PostingList postingList = new PostingList(vocabularyElem.getTerm());
                postingList.readFromDisk(channelDocID, channelTermFreq, vocabularyElem.getDocIdsOffset(), vocabularyElem.getTermFreqOffset(), vocabularyElem.getDocIdsLen(), vocabularyElem.getTermFreqLen());

                return postingList;
            }
        }
        return null;
    }

    // used for testing, given term , retrive the VocabularyElem
    public static VocabularyElem getTestVocabularyElem(String term) throws IOException {
        // take channel
        FileChannel channelVocabulary = FileUtils.GetCorrectChannel(-1, 0);

        // offset corrente
        long currentOffset = 0;
        long vocabularySize= 60;

        // take the size of the file
        while (true) {
            try {
                if (!((currentOffset + 60) < channelVocabulary.size())) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // read the vocabulary element
            VocabularyElem vocabularyElem = new VocabularyElem();
            vocabularyElem.readFromDisk(channelVocabulary, currentOffset);

            // update the offset
            currentOffset += 60;

            // check if the term is the one we are looking for
            if (vocabularyElem.getTerm().equals(term)) {
                // print the vocabulary element
                System.out.println(vocabularyElem);
                return vocabularyElem;
            }
        }
        return null;
    }

}