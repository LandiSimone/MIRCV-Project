package it.unipi.dii.aide.mircv.compression;

import it.unipi.dii.aide.mircv.models.Posting;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class unary { // -> termfreq

    // compress an array of integer
    public static byte[] compress(ArrayList<Integer> uncompressed) {

        // count the number of bits needed to store the compressed array
        int bits = 0;
        for (int i = 0; i < uncompressed.size(); i++) {
            // check if 0 or negative
            if (uncompressed.get(i) <= 0) {
                System.out.println("skipped element because <= 0");
                continue;
            }
            bits += uncompressed.get(i);
        }

        // allocate the byte array for the compressed array (round up to the next byte)
        byte[] compressed = new byte[(int) Math.ceil(bits / 8.0)];

        // compress each number n with n-1 1s and a 0 at the end
        int index = 0;
        int count = 0;
        for (int i = 0; i < uncompressed.size(); i++) {
            // check if 1
            if (uncompressed.get(i) == 1) {
                // set to 0 the bit at position count
                compressed[index] |= 0 << (7 - count);
                count++;
                // check if the byte is full
                if (count == 8) {
                    // reset count and move to the next byte
                    index++;
                    count = 0;
                }
                continue;
            }
            // add n-1 1s
            for (int j = 0; j < uncompressed.get(i) - 1; j++) {
                // set to 1 the bit at position count
                compressed[index] |= 1 << (7 - count);
                count++;
                // check if the byte is full
                if (count == 8) {
                    // reset count and move to the next byte
                    index++;
                    count = 0;
                }
            }
            // add a 0 at the end of the sequence
            compressed[index] |= 0 << (7 - count);
            count++;
            // check if the byte is full
            if (count == 8) {
                // reset count and move to the next byte
                index++;
                count = 0;
            }
        }

        // check if the last byte is full
        if (count != 0) {
            // fill the last bits with 1s
            for (int i = count; i < 8; i++) {
                compressed[index] |= 1 << (7 - i);
            }
        }
        return compressed;
    }

    // decompress an array of bytes
    public static ArrayList<Integer> decompress(byte[] compressed) {

        // allocate the array for the decompressed array
        ArrayList<Integer> decompressed = new ArrayList<>();

        // decompress each byte
        int count = 0;
        for (int i = 0; i < compressed.length; i++) {
            for (int j = 0; j < 8; j++) {
                // check if the bit at position j is 1
                if ((compressed[i] & (1 << (7 - j))) != 0) {
                    // increment the counter
                    count++;
                } else {
                    // add the number to the array (number of 1s + 1)
                    decompressed.add(count + 1);
                    // reset the counter
                    count = 0;
                }
            }
        }

        return decompressed;
    }

    // compress an array of integer and write it to a file channel
    public static int writeTermFreqCompressed(ArrayList<Posting> postingList, FileChannel channelTermFreq) throws IOException {

        ArrayList<Integer> TFlistUncompressed = new ArrayList<>();

        // get the termFreqs from the posting list
        for (Posting posting : postingList) {
            TFlistUncompressed.add(posting.getTermFreq());
        }

        // compress the list
        byte[] compressed = compress(TFlistUncompressed);

        // allocate ByteBuffer for writing termFreqs
        channelTermFreq.position(channelTermFreq.size());
        ByteBuffer freqsByteBuffer = ByteBuffer.wrap(compressed);

        // write termFreqs to the buffers
        while (freqsByteBuffer.hasRemaining())
            channelTermFreq.write(freqsByteBuffer);

        // return the number of bytes written
        return compressed.length;
    }

    // read an array of integer from a file channel and decompress it
    public static ArrayList<Integer> readTermFreqCompressed( FileChannel channelTermFreq , long offsetTermFreq, int termFreqLen) throws IOException {
        try {
            // array for decompressed termFreqs
            ArrayList<Integer> TFlistDecompressed = new ArrayList<>();
            // set position
            channelTermFreq.position(offsetTermFreq);

            // creating ByteBuffer for reading termFreqs
            ByteBuffer bufferTermFreq = ByteBuffer.allocate(termFreqLen );

            // reading termFreqs from channel
            while (bufferTermFreq.hasRemaining())
                channelTermFreq.read(bufferTermFreq);

            bufferTermFreq.rewind(); // reset the buffer position to 0

            // reading termFreqs from buffer
            TFlistDecompressed = decompress(bufferTermFreq.array());

            // return the decompressed termFreqs
            return TFlistDecompressed;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
