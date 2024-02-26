package it.unipi.dii.aide.mircv.models;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CollectionStatistics {

    // # of documents = size of documnetIndex
    private static int docCount;
    // total number of terms
    private static long totalLength;

    // default constructor
    public CollectionStatistics() {
        this.docCount = 0;
        this.totalLength = 0;
    }

    // constructor
    public CollectionStatistics(int docCount, long totalLength) {
        this.docCount = docCount;
        this.totalLength = totalLength;
    }

    public static int getDocCount() {
        return docCount;
    }

    public static double getAvgDocLen() {
        return (double) totalLength / docCount;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    // increment the number of documents
    public void setDocCount(int lastDocId) {
        this.docCount = lastDocId +1;
    }

    @Override
    public String toString() {
        return "CollectionStatistics{" +
                "docCount=" + docCount +
                ", totalLength=" + totalLength +
                '}';
    }

    // write the object into the channel
    public void writeToDisk(FileChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4+8);

        channel.position(channel.size());

        buffer.putInt(docCount);
        buffer.putLong(totalLength);

        buffer = ByteBuffer.wrap(buffer.array());

        // writing into channel
        while (buffer.hasRemaining())
            channel.write(buffer);

    }

    // read the object from the channel
    public void readFromDisk(FileChannel channel, long offset) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4+8);

        channel.position(offset);

        while (buffer.hasRemaining())
            channel.read(buffer);

        buffer.rewind(); // reset the buffer position to 0
        this.docCount = buffer.getInt();                        // reading docCount from buffer
        this.totalLength = buffer.getLong();                     // reading totalLength from buffer
    }
}
