package it.unipi.dii.aide.mircv.compression;
import it.unipi.dii.aide.mircv.models.Posting;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class variableByte { // -> docid

    // compress an array of integer
    public static byte[] compress(ArrayList<Integer> uncompressed) {

        // allocate the array
        ArrayList<Integer> compressed = new ArrayList<>();

        // compress the numbers
        for(int uncompressedNumber : uncompressed) {

            // convert the number to binary
            String binary = Integer.toBinaryString(uncompressedNumber);

            // add 0s at the beginning to complete the byte
            while(binary.length() % 7 != 0) {
                binary = "0" + binary;
            }

            // split the binary string in 7-bit chunks removing the first bit
            for(int i = 0; i < binary.length(); i += 7) {
                // check if the chunk is the first one
                if(i == 0) {
                    // add a 0 at the beginning
                    compressed.add(Integer.parseInt("0" + binary.substring(i, i + 7), 2));
                } else {
                    // add a 1 at the beginning
                    compressed.add(Integer.parseInt("1" + binary.substring(i, i + 7), 2));
                }
            }
        }

        // convert the arraylist to a byte array
        byte[] compressedBytes = new byte[compressed.size()];
        for(int i = 0; i < compressed.size(); i++) {
            compressedBytes[i] = compressed.get(i).byteValue();
        }

        // return the compressed array
        return compressedBytes;

    }

    // compress an array of integer
    public static ArrayList<Integer> decompress(byte[] compressed) {
        // allocate the array
        ArrayList<Integer> decompressed = new ArrayList<>();

        // decompress
        int n = 0;
        // convert the bytes to an integer
        for (byte byteElem : compressed) {
            // convert the byte to an unsigned int
            int unsignedByte = byteElem & 0xff;
            // check if is the first chunk of the number
            if (unsignedByte < 128) {
                // check if it is not the first number
                if (n != 0) {
                    // add the previous number to the array
                    decompressed.add(n);
                    // reset the number
                    n = 0;
                }
                // update the number
                n = 128 * n + unsignedByte;
            } else {
                // not the first chunk of the number --> update the number
                n = 128 * n + (unsignedByte - 128);
            }
        }
        // add the last number to the array
        decompressed.add(n);

        return decompressed;
    }

    // compress an array of integer and write it to a file channel
    public static int writeDocIdCompressed(ArrayList<Posting> postingList, FileChannel channelDocId) throws IOException {

        ArrayList<Integer> docIdsUncompressed = new ArrayList<>();

        // get the docIds from the posting list
        for (Posting posting : postingList) {
            docIdsUncompressed.add(posting.getDocID());
        }

        // compress the list
        byte[] compressed = compress(docIdsUncompressed);

        // allocate ByteBuffer for writing docIds
        channelDocId.position(channelDocId.size());
        ByteBuffer DocIdByteBuffer = ByteBuffer.wrap(compressed);

        // write the DocIds to the buffers
        while (DocIdByteBuffer.hasRemaining())
            channelDocId.write(DocIdByteBuffer);

        // return the number of bytes written
        return compressed.length;
    }

    // read an array of integer from a file channel and decompress it
    public static ArrayList<Integer> readDocIdsCompressed(FileChannel channelDocId, long offsetDocId, int DocIdLen) throws IOException {
        try {
            // create a buffer
            ByteBuffer docsByteBuffer = ByteBuffer.allocate(DocIdLen);

            // set position
            channelDocId.position(offsetDocId);

            // read the docIds from the channel
            while (docsByteBuffer.hasRemaining())
                channelDocId.read(docsByteBuffer);

            // reset the buffer position to 0
            docsByteBuffer.rewind();

            // decompress the docIds
            ArrayList<Integer> docIdsDecompressed = decompress(docsByteBuffer.array());

            // return the decompressed docIds
            return docIdsDecompressed;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
