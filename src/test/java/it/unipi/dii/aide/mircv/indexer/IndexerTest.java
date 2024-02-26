package it.unipi.dii.aide.mircv.indexer;

import it.unipi.dii.aide.mircv.models.DocumentIndexElem;
import it.unipi.dii.aide.mircv.models.Posting;
import it.unipi.dii.aide.mircv.models.PostingList;
import it.unipi.dii.aide.mircv.models.VocabularyElem;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import static it.unipi.dii.aide.mircv.utils.FileUtils.Path_CollectionTest;
import static it.unipi.dii.aide.mircv.utils.FileUtils.docIndex_RAF;
import static org.junit.jupiter.api.Assertions.*;


class IndexerTest {

    /*
    Configuration needed for the test:
    "read_compressionON":false,
    "stemming_stopwordON":true,
    "index_compressionON":false,
    "testing_ON":true,
     */
    
    protected static final HashMap<String, VocabularyElem> vocabularyT = new HashMap<>();   // LinkedHashMap to preserve the order of insertion
    protected static final HashMap<String, VocabularyElem> vocabulary = new HashMap<>();    // LinkedHashMap to preserve the order of insertion

    // list of sorted term
    public static ArrayList<String> termList = new ArrayList<>();

    public static HashMap<String, PostingList> invIdexT = new HashMap<>();
    public static HashMap<String, PostingList> invIdex = new HashMap<>();

    static ArrayList<DocumentIndexElem> docIndex = new ArrayList<>();

    ArrayList<DocumentIndexElem> docIndexT = new ArrayList<>();

    private static BufferedReader bufferedReader;


    // docs
//    0	The presence of communication amid scientific minds was equally important to the success of the Manhattan Project as scientific intellect was. The only cloud hanging over the impressive achievement of the atomic researchers and engineers is what their success truly meant; hundreds of thousands of innocent lives obliterated.
//    1	The Manhattan Project and its atomic bomb helped bring an end to World War II. Its legacy of peaceful uses of atomic energy continues to have an impact on history and science.
//    2	Essay on The Manhattan Project - The Manhattan Project The Manhattan Project was to see if making an atomic bomb possible. The success of this project would forever change the world forever making it known that something this powerful can be manmade.

    // Manhattan, df = 3, cf = 5
    // Manhattan, (1, 1), (2, 1), (3, 3)

    // Project, df = 3, cf = 6
    // Project, (1, 1), (2, 1), (3, 4)

    // bomb, df = 2, cf = 2
    // bomb, (2, 1), (3, 1)



    @BeforeAll
    static void Indexer() throws IOException {
        Indexer indexer = new Indexer();
        Path_CollectionTest= "src/main/java/it/unipi/dii/aide/mircv/resources/collection_prova2.tsv";
        indexer.main(null);
        // indexer.PlotFinalStructure();
    }

    @Test
    void TestVocabulary() throws IOException {

        // array of test
        List<VocabularyElem> testVocabulary = Arrays.asList(
                new VocabularyElem("manhattan", 3, 5),
                new VocabularyElem("project", 3, 6),
                new VocabularyElem("bomb", 2, 2)
        );

        // retrive vocabulary
        for (VocabularyElem vocabularyElem : testVocabulary) {
            VocabularyElem vocabularyElem1 = Indexer.getTestVocabularyElem(vocabularyElem.getTerm());
            System.out.println("VocTest on term: " + vocabularyElem.getTerm());
            System.out.println("test 1");
            assertEquals(vocabularyElem.getTerm(), vocabularyElem1.getTerm());
            assertEquals(vocabularyElem.getDocFreq(), vocabularyElem1.getDocFreq());
            assertEquals(vocabularyElem.getCollFreq(), vocabularyElem1.getCollFreq());
        }
    }

    @Test
    void TestPostingList() throws IOException {
        // array of test
        List<PostingList> testPostingList = Arrays.asList(
                new PostingList("manhattan", new ArrayList<Posting>(Arrays.asList(new Posting(1, 1), new Posting(2, 1), new Posting(3, 3)))),
                new PostingList("project", new ArrayList<Posting>(Arrays.asList(new Posting(1, 1), new Posting(2, 1), new Posting(3, 4)))),
                new PostingList("bomb", new ArrayList<Posting>(Arrays.asList(new Posting(2, 1), new Posting(3, 1)))
                )
        );

        // retrive posting list
        for (PostingList postingList : testPostingList) {
            PostingList postingList1 = Indexer.getTestPosting(postingList.getTerm());
            System.out.println("PostTest on term: " + postingList.getTerm());
            assertEquals(postingList.getTerm(), postingList1.getTerm());

            for (int i = 0; i < postingList.getPostingList().size(); i++) {
                assertEquals(postingList.getPostingList().get(i).getDocID(), postingList1.getPostingList().get(i).getDocID());
                assertEquals(postingList.getPostingList().get(i).getTermFreq(), postingList1.getPostingList().get(i).getTermFreq());
            }

        }

    }




    public void Vocabulary_Equal() throws IOException {
        // build vocabulary
        HashMap<String, VocabularyElem> vocabularyT = buildVocaulary(bufferedReader);

        // check if the two vocabulary have the same size
        assertEquals(vocabularyT.size(), vocabulary.size());

        // check if the two vocabulary have the same terms
        for (String term : vocabulary.keySet()) {
            assertTrue(vocabulary.containsKey(term));
        }

        // check if the two vocabulary have the same docFreq
        for (String term : vocabulary.keySet()) {
            assertEquals(vocabulary.get(term).getDocFreq(),vocabularyT.get(term).getDocFreq());
        }

        // check if the two vocabulary have the same collFreq
        for (String term : vocabulary.keySet()) {
            assertEquals(vocabulary.get(term).getDocFreq(),vocabularyT.get(term).getDocFreq());
        }
        System.out.println("okay");
    }


    public void PostingList_Equal() throws IOException {
        // build posting
        HashMap<String, PostingList> invIdexT = buildPosting(bufferedReader);

        // check if the two posting have the same size
        assertEquals(invIdexT.size(), invIdex.size());

        // check if the two posting have the same terms
        for (String term : invIdex.keySet()) {
            assertEquals(invIdexT.containsKey(term), invIdex.containsKey(term));
        }

        // for each key of the hashmap
        // check if the posting list is the same
        // check if the term is the same
        for (String term : invIdex.keySet()) {
            System.out.println("term: " + term);
            PostingList postList = invIdex.get(term);
            PostingList postListT = invIdexT.get(term);

            assertEquals(postList.getTerm(), postListT.getTerm());

            assertEquals(invIdexT.get(term).getTerm(), invIdex.get(term).getTerm());
        }
    }


    public void DocumentIndex_Equal() throws IOException {
        // build document index
        ArrayList<DocumentIndexElem> docIndexT = buildDocumentIndexElem(bufferedReader);

        // check if the two document index have the same size
        assertEquals(docIndex.size(), docIndexT.size());

        // check if the two document index have the same docno
        for (int i = 0; i < docIndex.size(); i++) {
            assertEquals(docIndex.get(i).getDocno(), docIndexT.get(i).getDocno());
        }

        // check if the two document index have the same documnetLength
        for (int i = 0; i < docIndex.size(); i++) {
            assertEquals(docIndex.get(i).getLength(), docIndexT.get(i).getLength());
        }
    }



    private ArrayList<DocumentIndexElem> buildDocumentIndexElem(BufferedReader bufferedReader) throws IOException {

        int docid = 1;
        String line;
        String docno;
        String text;
        int tab;
        int documnetLength;

        while ((line = bufferedReader.readLine()) != null) {

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
            String[] tokens = text.split(" ");

            documnetLength = tokens.length;

            // new document index elem
            DocumentIndexElem doc = new DocumentIndexElem( docno, documnetLength);
            docIndexT.add(doc);

            docid++;
        }
        return docIndexT;
    }



    private HashMap<String, PostingList> buildPosting(BufferedReader bufferedReader) throws IOException {

        // init docIndex_RAF
        FileUtils.initDocIndex_RAF();

        int docid = 1;
        String line;
        String docno;
        String text;
        int tab;
        int documnetLength;

        while ((line = bufferedReader.readLine()) != null) {

            // split on tab
            tab = line.indexOf("\t");

            // extract
            docno = line.substring(0, tab);
            text = line.substring(tab + 1);

            String[] tokens = text.split(" ");

            documnetLength = tokens.length;

            for (String token : tokens) {

                // compute term frequency in the document
                int tf = Collections.frequency(java.util.Arrays.asList(tokens), token);

                // check if token is already in the vocabulary
                if (vocabulary.containsKey(token)) {

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
                        invIdexT.get(token).addPosting(new Posting(docid, tf));
                    }

                } else {
                    // add new term in the vocabulary
                    VocabularyElem NewVocElem = new VocabularyElem(token, 1, tf);
                    NewVocElem.setLastDocIdInserted(docid);
                    vocabulary.put(token, NewVocElem);

                    // add new posting list
                    invIdexT.put(token, new PostingList(token, new Posting(docid, tf)));

                    // add new term in the list
                    termList.add(token);
                }
            }

            if (docid % 50000 == 0)
                System.out.println("< current docId: " + docid +" >");

            docid++;
        }

        return invIdexT;
    }

    private HashMap<String, VocabularyElem> buildVocaulary(BufferedReader bufferedReader) throws IOException {

        // init docIndex_RAF
        FileUtils.initDocIndex_RAF();
        int docid = 1;
        String line;
        String docno;
        String text;
        int tab;
        int documnetLength;

        while ((line = bufferedReader.readLine()) != null) {

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

            String[] tokens = text.split(" ");

            documnetLength = tokens.length;

            // new document index elem
            DocumentIndexElem doc = new DocumentIndexElem( docno, documnetLength);
            doc.writeToDisk(docIndex_RAF.getChannel());

            for (String token : tokens) {

                // compute term frequency in the document
                int tf = Collections.frequency(java.util.Arrays.asList(tokens), token);

                // check if token is already in the vocabulary
                if (vocabulary.containsKey(token)) {

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

                    }

                } else {
                    // add new term in the vocabulary
                    VocabularyElem NewVocElem = new VocabularyElem(token, 1, tf);
                    NewVocElem.setLastDocIdInserted(docid);
                    vocabulary.put(token, NewVocElem);

                    // add new term in the list
                    termList.add(token);
                }
            }

            docid++;
        }
        // sort the vocabulary
        return vocabulary;
    }


    /*
    // tree ,ocean, sunshine, whisper, tree, harmony
        // Sunshine, ball, house

        DocumentIndexElem doc1 = new DocumentIndexElem( "1", 6);
        DocumentIndexElem doc2 = new DocumentIndexElem( "2", 3);
        docIndex.add(doc1);
        docIndex.add(doc2);

        bufferedReader = new BufferedReader(new StringReader("0\ttree ocean sunshine whisper tree harmony\n" + "1\tsunshine ball house"));

        VocabularyElem VocElem1 = new VocabularyElem("tree", 1, 2);
        vocabularyT.put("tree", VocElem1);
        VocabularyElem VocElem2 = new VocabularyElem("ocean", 1, 1);
        vocabularyT.put("ocean", VocElem2);
        VocabularyElem VocElem3 = new VocabularyElem("sunshine", 2, 2);
        vocabularyT.put("sunshine", VocElem3);
        VocabularyElem VocElem4 = new VocabularyElem("whisper", 1, 1);
        vocabularyT.put("whisper", VocElem4);
        VocabularyElem VocElem5 = new VocabularyElem("harmony", 1, 1);
        vocabularyT.put("harmony", VocElem5);
        VocabularyElem VocElem6 = new VocabularyElem("ball", 1, 1);
        vocabularyT.put("ball", VocElem6);
        VocabularyElem VocElem7 = new VocabularyElem("house", 1, 1);
        vocabularyT.put("house", VocElem7);

        invIdex.put("harmony", new PostingList("harmony", new Posting(1, 1)));
        invIdex.put("house", new PostingList("house", new Posting(2, 1)));
        invIdex.put("ball", new PostingList("ball", new Posting(2, 1)));
        invIdex.put("ocean", new PostingList("ocean", new Posting(1, 1)));
        invIdex.put("sunshine", new PostingList("sunshine", new Posting(1, 1)));
        invIdex.get("sunshine").addPosting(new Posting(2, 1));
        invIdex.put("tree", new PostingList("tree", new Posting(1, 2)));
        invIdex.put("whisper", new PostingList("whisper", new Posting(1, 1)));

        System.out.println("okay");
     */
}