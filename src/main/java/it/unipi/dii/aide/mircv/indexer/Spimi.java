package it.unipi.dii.aide.mircv.indexer;
import it.unipi.dii.aide.mircv.models.*;
import it.unipi.dii.aide.mircv.text_processing.TextProcessing;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import static it.unipi.dii.aide.mircv.utils.FileUtils.docIndex_RAF;
import static it.unipi.dii.aide.mircv.utils.FileUtils.initBuffer;

public class Spimi {
    // block number
    protected int blockNum = 0;

    // docId counter
    protected int docid = 1;

    // vocabulary = hash map
    protected final HashMap<String, VocabularyElem> vocabulary = new HashMap<>();

    // list used to sort vocabulary elem
    public static ArrayList<String> termList = new ArrayList<>();

    // Posting list = hash map
    public static HashMap<String, PostingList> postingListElem = new HashMap<>();

    // threshold for memory
    private long MEMORYFree_THRESHOLD;

    // save last docId for CollectionStatistics
    public static int lastDocId = 0;
    // total length for CollectionStatistics
    public static int totalLentgh = 0;


    public int startIndexer() throws IOException {
        System.out.println("Start Spimi Alg ...");

        // read collection according to the compression flag
        BufferedReader bufferedReader = initBuffer(Configuration.isCompressionON(), Configuration.isTesting());

        // init docIndex_RAF
        FileUtils.initDocIndex_RAF();

        String line; // riga della collection
        String docno;
        String text; // body of the document
        int tab;
        int documnetLength;

        // leave 10% of memory free
        MEMORYFree_THRESHOLD = Runtime.getRuntime().totalMemory() *10 / 100;
        System.out.println("MEMORYFree_THRESHOLD : " + MEMORYFree_THRESHOLD);

        while ((line = bufferedReader.readLine()) != null) {

            // checking if the used memory has reached the threshold
            checkMemory();

            // split on tab
            tab = line.indexOf("\t");

            // check malformed line
            if (tab == -1) {  // tab not found
                continue;
            }

            // extract
            docno = line.substring(0, tab);
            text = line.substring(tab + 1);

            // check empty doc
            if (text.isEmpty())
                continue;

            // process text
            String[] tokens = TextProcessing.DocumentProcessing(text);

            // compute document length and total length
            documnetLength = tokens.length;
            totalLentgh += documnetLength;

            // new document index elem
            DocumentIndexElem doc = new DocumentIndexElem(docno, documnetLength);
            doc.writeToDisk(docIndex_RAF.getChannel());

            if (Configuration.isTesting()){
                System.out.println("\n DocNo: " + docno + " DocLen: " + documnetLength);
                System.out.println("Tokens: ");
                for (String token : tokens) {
                    System.out.print(token + "-");
                }
            }

            for (String token : tokens) {

                if (token.isEmpty())
                    continue;

                // compute term frequency
                int tf = Collections.frequency(Arrays.asList(tokens), token);

                // check if token is already in the vocabulary
                if (vocabulary.containsKey(token)) {

                    // termine è presente nel dizionario
                    // se è nello stesso docuemnto -> fine
                    // altrimenti dobbiamo :
                    // 1) aggiungere posting alla posting list
                    // 2) aggiornare vocabolary entry -> docFreq + colFreq

                    // get the vocabulary element
                    VocabularyElem vocElem = vocabulary.get(token);
                    // check if the term is in another document
                    if (vocElem.getLastDocIdInserted() != docid) {

                        // update the document frequency
                        vocElem.incDocFreq();
                        // update the collection frequency
                        vocElem.updateCollFreq(tf);
                        // update the last document id inserted
                        vocElem.setLastDocIdInserted(docid);

                        // add the posting list
                        postingListElem.get(token).addPosting(new Posting(docid, tf));
                    }

                } else {
                    // add new term in the vocabulary
                    VocabularyElem NewVocElem = new VocabularyElem(token, 1, tf);
                    NewVocElem.setLastDocIdInserted(docid);
                    vocabulary.put(token, NewVocElem);

                    // add new posting list
                    postingListElem.put(token, new PostingList(token, new Posting(docid, tf)));

                    // add new term in the list
                    termList.add(token);
                }
            }

            if (docid % 50000 == 0)
                System.out.println("< current docId: " + docid + " >");

            docid++;
        }

        // write the last block on the disk, which did not trigger checkMemory()
        if (!WriteBlockOnDisk(blockNum, termList, vocabulary, postingListElem)) {
            System.out.println("Couldn't write block "+ blockNum + " to disk.");
            rollback();
            return -1;
        }
        System.out.println("Block " + blockNum + " written on disk.");

        lastDocId = docid-1; // save last docId for CollectionStatistics

        // close the docIndex_RAF
        docIndex_RAF.close();

        // clear data structure
        clearDataStructure();

        return blockNum;
    }

    // check if the used memory has reached the threshold and write on disk vocabulary and posting list
    private void checkMemory() {
        if (Runtime.getRuntime().freeMemory() < MEMORYFree_THRESHOLD) {
            System.out.println("Memory full..");
            System.out.println("Writing block " + blockNum + " on disk..");

            if (!WriteBlockOnDisk(blockNum, termList, vocabulary, postingListElem)) {
                System.out.println("Couldn't write block "+ blockNum + " to disk.");
                rollback();
                System.exit(-1);
            }

            System.out.println("Block " + blockNum + " written on disk.");

            clearDataStructure(); // here GC
            blockNum++;
            lastDocId = docid; // save last docId for CollectionStatistics

            System.out.println(Runtime.getRuntime().freeMemory() +  " <TH +" +MEMORYFree_THRESHOLD +"> Memory free again..");
        }
    }

    // clear data structure in memory and try to force garbage collection to free memory
    private void clearDataStructure() {
        // clear data structure in memory
        vocabulary.clear();
        termList.clear();
        postingListElem.clear();

        // and try to force garbage collection to free memory
        while (Runtime.getRuntime().freeMemory() < MEMORYFree_THRESHOLD * 2) {
            // wait for free memory
            System.gc();
        }

    }

    // remove data if error occcurs
    private void rollback() {
        FileUtils.clearDataFolder();
    }

    // write the block (partial vocabulary and posting list ) on the disk
    private boolean WriteBlockOnDisk(int blockNum, ArrayList<String> termList, HashMap<String, VocabularyElem> Pvocabulary, HashMap<String, PostingList> PpostingListElem) {

        // create RAF and temp file for the block
        FileUtils.createTempFile(blockNum);

        // sort the term list
        Collections.sort(termList);

        // write on the disk
        try {
            for (String term : termList) {

                // get the posting list of the term
                PostingList postList = PpostingListElem.get(term);

                // get the vocabulary element of the term
                VocabularyElem vocElem = Pvocabulary.get(term);

                // set the offset of the DocId posting list in the vocabulary element
                vocElem.setDocIdsOffset(FileUtils.skeleton_RAF.get(blockNum).get(1).getChannel().size());

                // set the offset of the TermFreq posting list in the vocabulary element
                vocElem.setTermFreqOffset(FileUtils.skeleton_RAF.get(blockNum).get(2).getChannel().size());

                // write the posting list on the disk
                postList.writeToDisk(FileUtils.skeleton_RAF.get(blockNum).get(1).getChannel(), FileUtils.skeleton_RAF.get(blockNum).get(2).getChannel());

                // update the vocabulary element
                vocElem.incFreqLen(postList.getPostingList().size()*4);
                vocElem.incDocLen(postList.getPostingList().size()*4);

                // write the vocabulary element on the disk
                Pvocabulary.put(term, vocElem); // update the vocabulary
                vocElem.writeToDisk(FileUtils.skeleton_RAF.get(blockNum).get(0).getChannel());

            }
            return true;
        }
        catch (IOException e) {
            System.out.println("I/O Error " + e);
            return false;
        }
    }

}
