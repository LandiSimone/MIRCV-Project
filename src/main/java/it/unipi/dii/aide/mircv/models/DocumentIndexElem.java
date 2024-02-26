package it.unipi.dii.aide.mircv.models;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class DocumentIndexElem {
    private String docNo;   // from the collection
    private int docLen;  // length of the document

    public DocumentIndexElem(){
    }

    public DocumentIndexElem(String docno, int length) {
        int diffLength = 20 - docno.length();

        if (diffLength != 0)
            /* padding = concatenate " " to the fixed size of 20 characters */
            docno = docno + " ".repeat(Math.max(0, diffLength));

        this.docNo = docno;
        this.docLen = length;
    }


    public String getDocno() {
        return docNo;
    }

    public int getLength() {
        return docLen;
    }

    public void setDocno(String docno) {
        this.docNo = docno;
    }

    public void setLength(int length) {
        this.docLen = length;
    }

    @Override
    public String toString() {
        return "DocumentIndexElem{" +
                ", docno='" + docNo + '\'' +
                ", length=" + docLen +
                '}';
    }

    // write the object into the channel
    public void writeToDisk(FileChannel channel) {
        // 20 (doc_no)  + 4 (length) = 24 bytes
        try {
            // creating ByteBuffer
            ByteBuffer buffer = ByteBuffer.allocate(24);
            channel.position(channel.size());
            CharBuffer charBuffer = CharBuffer.allocate(20);    // 20 bytes for docno

            // writing into buffer
            for (int i = 0; i < this.docNo.length(); i++)
                charBuffer.put(i, this.docNo.charAt(i));

            buffer.put(StandardCharsets.UTF_8.encode(charBuffer));

            buffer.putInt(this.docLen); // [start at position 20] writing length into buffer

            buffer = ByteBuffer.wrap(buffer.array());

            // writing into channel
            while (buffer.hasRemaining())
                channel.write(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // read the object from the channel
    public void readFromDisk(FileChannel channel, int position) {
        // 20 (doc_no)  + 4 (length) = 24 bytes
        try {
            // creating ByteBuffer for reading DocNo
            ByteBuffer buffer = ByteBuffer.allocate(20);
            channel.position(position);
            while (buffer.hasRemaining())
                channel.read(buffer);

            this.setDocno(new String(buffer.array(), StandardCharsets.UTF_8).trim());

            // creating ByteBuffer for reading DocId and Length
            buffer = ByteBuffer.allocate(4);

            while (buffer.hasRemaining())
                channel.read(buffer);

            buffer.rewind(); // reset the buffer position to 0
            this.setLength(buffer.getInt());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
