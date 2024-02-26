package it.unipi.dii.aide.mircv.compression;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class unaryTest {

    @Test
    void compress() {
        // Test input array: [1, 2, 3, 1, 4, 1]
        // Expected compressed binary: 01011001 11001111
        ArrayList<Integer> input1 = new ArrayList<>();
        input1.add(1);
        input1.add(2);
        input1.add(3);
        input1.add(1);
        input1.add(4);
        input1.add(1);

        // Test input array: [45]
        // Expected compressed binary: 11111111 11111111 11111111 11111111 11111111 11110111
        ArrayList<Integer> input2 = new ArrayList<>();
        input2.add(45);

        // Test input array: [1, 2, 3, 45, 1, 4, 1]
        // Expected compressed binary: 01011011 11111111 11111111 11111111 11111111 11111111 11001110 01111111
        ArrayList<Integer> input3 = new ArrayList<>();
        input3.add(1);
        input3.add(2);
        input3.add(3);
        input3.add(45);
        input3.add(1);
        input3.add(4);
        input3.add(1);

        // Expected compressed bytes
        byte[] expectedCompressed1 = {(byte) 0b01011001, (byte) 0b11001111};
        byte[] expectedCompressed2 = {(byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11110111};
        byte[] expectedCompressed3 = {(byte) 0b01011011, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11001110, (byte) 0b01111111};

        // Compress the input
        byte[] compressed1 = unary.compress(input1);
        byte[] compressed2 = unary.compress(input2);
        byte[] compressed3 = unary.compress(input3);

        // Assert that the compressed array is equal to the expected compressed array
        //assertArrayEquals(expectedCompressed1, compressed1);
        //assertArrayEquals(expectedCompressed2, compressed2);
        assertArrayEquals(expectedCompressed3, compressed3);

    }

    @Test
    void decompress() {

        // Test input array: [1, 2, 3, 1, 4, 1]
        // Expected compressed binary: 01011001 11001111
        ArrayList<Integer> expectedDecompressed1 = new ArrayList<>();
        expectedDecompressed1.add(1);
        expectedDecompressed1.add(2);
        expectedDecompressed1.add(3);
        expectedDecompressed1.add(1);
        expectedDecompressed1.add(4);
        expectedDecompressed1.add(1);

        // Test input array: [45]
        // Expected compressed binary: 11111111 11111111 11111111 11111111 11111111 11110111
        ArrayList<Integer> expectedDecompressed2 = new ArrayList<>();
        expectedDecompressed2.add(45);

        // Test input array: [1, 2, 3, 45, 1, 4, 1]
        // Expected compressed binary: 01011011 11111111 11111111 11111111 11111111 11111111 11001110 01111111
        ArrayList<Integer> expectedDecompressed3 = new ArrayList<>();
        expectedDecompressed3.add(1);
        expectedDecompressed3.add(2);
        expectedDecompressed3.add(3);
        expectedDecompressed3.add(45);
        expectedDecompressed3.add(1);
        expectedDecompressed3.add(4);
        expectedDecompressed3.add(1);

        // Compress the input
        byte[] compressed1 = {(byte) 0b01011001, (byte) 0b11001111};
        byte[] compressed2 = {(byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11110111};
        byte[] compressed3 = {(byte) 0b01011011, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11001110, (byte) 0b01111111};

        // Decompress the compressed array
        ArrayList<Integer> decompressed1 = unary.decompress(compressed1);
        ArrayList<Integer> decompressed2 = unary.decompress(compressed2);
        ArrayList<Integer> decompressed3 = unary.decompress(compressed3);

        // Assert that the decompressed array is equal to the expected decompressed array
        //assertEquals(expectedDecompressed1, decompressed1);
        //assertEquals(expectedDecompressed2, decompressed2);
        assertEquals(expectedDecompressed3, decompressed3);
    }
}