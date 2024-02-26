package it.unipi.dii.aide.mircv.compression;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class variableByteTest {

    @Test
    void compress() {
        // Test input array: [67822]
        // Expected compressed binary: 00000100 10010001 11101110
        ArrayList<Integer> input1 = new ArrayList<>();
        input1.add(67822);

        // Test input array: [1, 2, 4]
        // Expected compressed binary: 00000001 00000010 00000100
        ArrayList<Integer> input2 = new ArrayList<>();
        input2.add(1);
        input2.add(2);
        input2.add(4);

        // Test input array: [1, 2, 67822, 4]
        // Expected compressed binary: 00000001 00000010 00000100 10010001 11101110 00000100
        ArrayList<Integer> input3 = new ArrayList<>();
        input3.add(1);
        input3.add(2);
        input3.add(67822);
        input3.add(4);

        // Expected compressed bytes
        byte[] expectedCompressed1 = {(byte) 0b00000100, (byte) 0b10010001, (byte) 0b11101110};
        byte[] expectedCompressed2 = {(byte) 0b00000001, (byte) 0b00000010, (byte) 0b00000100};
        byte[] expectedCompressed3 = {(byte) 0b00000001, (byte) 0b00000010, (byte) 0b00000100, (byte) 0b10010001, (byte) 0b11101110, (byte) 0b00000100};

        // Compress the input
        byte[] compressed1 = variableByte.compress(input1);
        byte[] compressed2 = variableByte.compress(input2);
        byte[] compressed3 = variableByte.compress(input3);

        // Assert that the compressed array is equal to the expected compressed array
        //assertArrayEquals(expectedCompressed1, compressed1);
        //assertArrayEquals(expectedCompressed2, compressed2);
        assertArrayEquals(expectedCompressed3, compressed3);

    }

    @Test
    void decompress() {
        // Test input array: [67822]
        // Expected compressed binary: 00000100 10010001 11101110
        ArrayList<Integer> expectedDecompressed1 = new ArrayList<>();
        expectedDecompressed1.add(67822);

        // Test input array: [1, 2, 4]
        // Expected compressed binary: 00000001 00000010 00000100
        ArrayList<Integer> expectedDecompressed2 = new ArrayList<>();
        expectedDecompressed2.add(1);
        expectedDecompressed2.add(2);
        expectedDecompressed2.add(4);

        // Test input array: [1, 2, 67822, 4]
        // Expected compressed binary: 00000001 00000010 00000100 10010001 11101110 00000100
        ArrayList<Integer> expectedDecompressed3 = new ArrayList<>();
        expectedDecompressed3.add(1);
        expectedDecompressed3.add(2);
        expectedDecompressed3.add(67822);
        expectedDecompressed3.add(4);

        // Compress the input
        byte[] compressed1 = {(byte) 0b00000100, (byte) 0b10010001, (byte) 0b11101110};
        byte[] compressed2 = {(byte) 0b00000001, (byte) 0b00000010, (byte) 0b00000100};
        byte[] compressed3 = {(byte) 0b00000001, (byte) 0b00000010, (byte) 0b00000100, (byte) 0b10010001, (byte) 0b11101110, (byte) 0b00000100};


        // Decompress the compressed array
        ArrayList<Integer> decompressed1 = variableByte.decompress(compressed1);
        ArrayList<Integer> decompressed2 = variableByte.decompress(compressed2);
        ArrayList<Integer> decompressed3 = variableByte.decompress(compressed3);

        // Assert that the decompressed array is equal to the expected decompressed array
        //assertEquals(expectedDecompressed1, decompressed1);
        //assertEquals(expectedDecompressed2, decompressed2);
        assertEquals(expectedDecompressed3, decompressed3);
    }

}