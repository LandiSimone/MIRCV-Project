package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.models.Configuration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static it.unipi.dii.aide.mircv.Main.configureSearchEngine;
import static it.unipi.dii.aide.mircv.utils.FileUtils.loadFinalStructure;
import static it.unipi.dii.aide.mircv.utils.FileUtils.takeFinalRAF;

public class queryPerformance {

    static int k = 10; // # doc to retrieve

    public static void main(String[] arg) throws IOException {

        takeFinalRAF();
        loadFinalStructure();

        System.out.println("Welcome to the MIRCV search engine!");

        while (true){

            // list used to store the tokens of the query
            ArrayList<String> tokens;
            long startTime, stopTime;
            Scanner scanner = new Scanner(System.in);
            String query;

            configureSearchEngine(scanner);

            // take the query from the file msmarco-test2020-queries.tsv
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/java/it/unipi/dii/aide/mircv/resources/msmarco-test2020-queries.tsv"), StandardCharsets.UTF_8));
            query = reader.readLine();

            ArrayList<Long> queryTime = new ArrayList<>();

            // for each query
            while (query != null){

                // take the query id and the query
                String[] queryTokens = query.split("\t");

                // print query
                System.out.println("Query ID: " + queryTokens[0]);
                System.out.println("Query: " + queryTokens[1]);

                // remove the query id from the query
                List<String> queryList = new ArrayList<>(Arrays.asList(queryTokens));
                queryList.remove(0);

                // convert to document without id
                String finalQuery = String.join(",", queryList);

                // start timer
                startTime = System.currentTimeMillis();

                // query preprocessing
                tokens = queryHandler.QueryPreProcessing(finalQuery);

                // query execution
                queryHandler.executeQuery(tokens, k);

                // stop timer
                stopTime = System.currentTimeMillis() - startTime;

                // print time
                System.out.println("Query time: " + stopTime + " ms");

                // add time to list
                queryTime.add(stopTime);

                // take the next query
                query = reader.readLine();
                tokens.clear();
            }

            // average time in seconds
            long sum = 0;
            for (long time : queryTime){
                sum += time;
            }

            System.out.println("--------------------------------------------------");
            System.out.println("Average time: " + sum/queryTime.size() + " ms");
        }
    }

}
