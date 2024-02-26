package it.unipi.dii.aide.mircv.models;
import it.unipi.dii.aide.mircv.compression.unary;
import it.unipi.dii.aide.mircv.compression.variableByte;
import it.unipi.dii.aide.mircv.utils.FileUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;

import static it.unipi.dii.aide.mircv.query.queryHandler.VocTerms;

public class PostingList {
    // term of the posting list
    private String term;

    // list of posting
    private final ArrayList<Posting> postingList;

    // current posting list element
    private Posting currentPostingList;

    // iterator of the posting list
    public Iterator<Posting> postingListIterator = null;

    // random access file for the docIds
    private RandomAccessFile docId_RAF;
    // random access file for the termFreqs
    private RandomAccessFile termFreq_RAF;

    public PostingList(String term) {
        this.term = term;
        this.postingList = new ArrayList<>();
    }

    public PostingList(String term, Posting posting) {
        this.term = term;
        this.postingList = new ArrayList<>();
        postingList.add(posting);
    }

    public PostingList(String term, ArrayList<Posting> postingList) {
        this.term = term;
        this.postingList = postingList;
    }

    public void addPosting(Posting posting) {
        postingList.add(posting);
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public ArrayList<Posting> getPostingList() {
        return postingList;
    }

    @Override
    public String toString() {
        return "PostingList{" +
                "term='" + term + '\'' +
                ", postingList=" + postingList +
                '}';
    }

    // write the posting list to disk
    public void writeToDisk(FileChannel channelDocID, FileChannel channelTermFreq) throws IOException {

        // initialize the buffers
        ByteBuffer docsByteBuffer;
        ByteBuffer freqsByteBuffer;

        // initialize the channels
        channelDocID.position(channelDocID.size());
        channelTermFreq.position(channelTermFreq.size());

        // allocate the buffers
        docsByteBuffer = ByteBuffer.allocate(this.postingList.size() * 4);
        freqsByteBuffer = ByteBuffer.allocate(this.postingList.size() * 4);

        // write docIds and termFreqs to the buffers
        for (Posting posting : this.postingList) {
            docsByteBuffer.putInt(posting.getDocID());
            freqsByteBuffer.putInt(posting.getTermFreq());
        }

        // reset the buffer position to 0
        docsByteBuffer = ByteBuffer.wrap(docsByteBuffer.array());
        freqsByteBuffer = ByteBuffer.wrap(freqsByteBuffer.array());

        // write buffers to the channels
        while (docsByteBuffer.hasRemaining())
            channelDocID.write(docsByteBuffer);

        while (freqsByteBuffer.hasRemaining())
            channelTermFreq.write(freqsByteBuffer);

    }

    // read the posting list from disk
    public void readFromDisk(FileChannel channelDocID, FileChannel channelTermFreq, long offsetDocId, long offsetTermFreq, int docIdsLen, int termFreqLen) throws IOException {
        try {
            // get the position of the docIds and termFreqs
            channelDocID.position(offsetDocId);
            channelTermFreq.position(offsetTermFreq);

            // creating ByteBuffer for reading docIds and termFreqs
            ByteBuffer bufferDocId = ByteBuffer.allocate(docIdsLen);
            ByteBuffer bufferTermFreq = ByteBuffer.allocate(termFreqLen);

            while (bufferDocId.hasRemaining())
                channelDocID.read(bufferDocId);

            while (bufferTermFreq.hasRemaining())
                channelTermFreq.read(bufferTermFreq);

            bufferDocId.rewind(); // reset the buffer position to 0
            bufferTermFreq.rewind(); // reset the buffer position to 0

            // reading docIds and termFreqs from buffer
            for (int i = 0; i < docIdsLen / 4; i++) {
                int docId = bufferDocId.getInt();
                int termFreq = bufferTermFreq.getInt();

                this.postingList.add(new Posting(docId, termFreq));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // add posting from disk to posting list (used in merger)
    public void addPostingFromDisk(FileChannel channelDocID, FileChannel channelTermFreq, long docIdsOffset, long termFreqOffset, int docIdsLen, int termFreqLen) throws IOException {
        try {
            // get the position of the docIds and termFreqs
            channelDocID.position(docIdsOffset);
            channelTermFreq.position(termFreqOffset);

            // creating ByteBuffer for reading docIds and termFreqs
            ByteBuffer bufferDocId = ByteBuffer.allocate(docIdsLen);
            ByteBuffer bufferTermFreq = ByteBuffer.allocate(termFreqLen);

            while (bufferDocId.hasRemaining())
                channelDocID.read(bufferDocId);

            while (bufferTermFreq.hasRemaining())
                channelTermFreq.read(bufferTermFreq);

            bufferDocId.rewind(); // reset the buffer position to 0
            bufferTermFreq.rewind(); // reset the buffer position to 0

            ArrayList<Posting> postingListApp = new ArrayList<>();
            // reading docIds and termFreqs from buffer
            for (int i = 0; i < docIdsLen / 4; i++) {
                int docId = bufferDocId.getInt();
                int termFreq = bufferTermFreq.getInt();
                // maintain old posting and add new posting
                this.postingList.add(new Posting(docId, termFreq));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // read the posting list from disk
    public void readPostingListFromDisk(FileChannel channelDocID, FileChannel channelTermFreq, long offsetDocId, long offsetTermFreq, int docIdsLen, int termFreqLen) throws IOException {

        // intialize one buffer for docIds and one for termFreqs
        ByteBuffer bufferDocId = ByteBuffer.allocate(docIdsLen);
        ByteBuffer bufferTermFreq = ByteBuffer.allocate(termFreqLen);

        // get the position of the docIds and termFreqs
        channelDocID.position(offsetDocId);
        channelTermFreq.position(offsetTermFreq);

        // read the docIds and termFreqs
        while (bufferDocId.hasRemaining())
            channelDocID.read(bufferDocId);

        while (bufferTermFreq.hasRemaining())
            channelTermFreq.read(bufferTermFreq);

        // reset the buffer position to 0
        bufferDocId.rewind();
        bufferTermFreq.rewind();

        // create the posting list
        for (int i = 0; i < docIdsLen / 4; i++) {
            int docId = bufferDocId.getInt();
            int termFreq = bufferTermFreq.getInt();

            this.postingList.add(new Posting(docId, termFreq));
        }

    }

    // read the posting list compressed from disk
    public void readCompressedPostingListFromDisk(FileChannel channelDocID, FileChannel channelTermFreq, long offsetDocId, long offsetTermFreq, int docIdsLen, int termFreqLen) throws IOException {

        // intialize one buffer for docIds and one for termFreqs
        ByteBuffer bufferDocId = ByteBuffer.allocate(docIdsLen);
        ByteBuffer bufferTermFreq = ByteBuffer.allocate(termFreqLen);

        // get the position of the docIds and termFreqs
        channelDocID.position(offsetDocId);
        channelTermFreq.position(offsetTermFreq);

        // read the docIds and termFreqs
        while (bufferDocId.hasRemaining())
            channelDocID.read(bufferDocId);

        while (bufferTermFreq.hasRemaining())
            channelTermFreq.read(bufferTermFreq);

        // reset the buffer position to 0
        bufferDocId.rewind();
        bufferTermFreq.rewind();

        // decompress
        ArrayList<Integer> docIds = variableByte.decompress(bufferDocId.array());

        ArrayList<Integer> termFreqs = unary.decompress(bufferTermFreq.array());

        // create the posting list
        for (int i = 0; i < docIds.size(); i++) {
            int docId = docIds.get(i);
            int termFreq = termFreqs.get(i);

            this.postingList.add(new Posting(docId, termFreq));
        }

    }

    // load the posting list given the term
    public void getPostingList(String term) throws IOException {

        // initialize the random access file
        docId_RAF = new RandomAccessFile(FileUtils.Path_FinalDocId, "r");
        termFreq_RAF = new RandomAccessFile(FileUtils.Path_FinalTermFreq, "r");

        // get the vocabulary element of the term
        VocabularyElem vocabularyElem = VocTerms.get(term);

        if (vocabularyElem == null) {
            System.out.println("Term " + term + " not found in the vocabulary");
            return;
        }

        if (Configuration.isIndex_compressionON()) {
            // compression index On
            readCompressedPostingListFromDisk(docId_RAF.getChannel(), termFreq_RAF.getChannel(), vocabularyElem.getDocIdsOffset(), vocabularyElem.getTermFreqOffset(), vocabularyElem.getDocIdsLen(), vocabularyElem.getTermFreqLen());
        } else {
            // compression index Off
            readPostingListFromDisk(docId_RAF.getChannel(), termFreq_RAF.getChannel(), vocabularyElem.getDocIdsOffset(), vocabularyElem.getTermFreqOffset(), vocabularyElem.getDocIdsLen(), vocabularyElem.getTermFreqLen());
        }

        // set posting list iterator
        postingListIterator = postingList.iterator();
        currentPostingList = postingListIterator.next();

    }

    public Posting getCurrentPostingList() {
        return currentPostingList;
    }

    // get the next posting of the posting list
    public void nextPosting(String term) throws IOException {
        // check if the posting list is empty
        if (postingListIterator.hasNext()) {
            // get the next posting
            currentPostingList = postingListIterator.next();
        } else {
            currentPostingList = null;
        }
    }
}