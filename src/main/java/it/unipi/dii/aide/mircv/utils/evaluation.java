package it.unipi.dii.aide.mircv.utils;

import com.google.gson.FieldAttributes;
import it.unipi.dii.aide.mircv.models.Configuration;
import it.unipi.dii.aide.mircv.query.queryHandler;
import it.unipi.dii.aide.mircv.query.scoreDoc;

import javax.swing.plaf.ProgressBarUI;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static it.unipi.dii.aide.mircv.Main.configureSearchEngine;
import static it.unipi.dii.aide.mircv.utils.FileUtils.*;

public class evaluation {
    static int k;

    // iterative process to evaluate the system performance by changing the configuration
    // create a Result TracEval file for all query in msmarco-test-queries.tsv file
    // and run trec_eval tool to evaluate the system performance
    public static void main(String[] args) throws IOException {
        takeFinalRAF();
        loadFinalStructure();

        while (true){
            ArrayList<String> tokens;
            long startTime, stopTime;
            Scanner scanner = new Scanner(System.in);
            String query;
            configureSearchEngine(scanner);

            // set k parameter
            System.out.println("Set k:");
            while (true) {
                try {
                    k = scanner.nextInt();
                    break;
                } catch (java.util.InputMismatchException e) {
                    System.out.println("Invalid input.");
                    scanner.nextLine();
                }
            }
            scanner.nextLine();

            // read query from file
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(TestQueryPath), StandardCharsets.UTF_8));
            query = reader.readLine();

            // array to store the query time
            ArrayList<Long> queryTime = new ArrayList<>();

            while (query != null){

                // split query in id and text
                String[] queryTokens = query.split("\t");

                // split query text in tokens
                List<String> queryList = new ArrayList<>(Arrays.asList(queryTokens));
                // remove id
                queryList.remove(0);

                // convert to document without id
                String finalQuery = String.join(",", queryList);

                // start timer
                startTime = System.currentTimeMillis();

                // preprocess query
                tokens = queryHandler.QueryPreProcessing(finalQuery);

                // execute query
                PriorityQueue<scoreDoc> result = queryHandler.returnPriorityQueue(tokens, k);

                // stop timer
                stopTime = System.currentTimeMillis();

                // create a qrel file
                writeResultOnFile(queryTokens[0], result);

                // read next query
                query = reader.readLine();
                tokens.clear();

                // add query time
                queryTime.add(stopTime - startTime);
            }

            System.out.println("Query result written on file ");

            System.out.println("Run trec_eval command");
            // run trec_eval
            runTrecEval(ResultQueryPath, QrelPath);

            // clean result file for the loop evaluation
            PrintWriter writer = new PrintWriter(ResultQueryPath);
            writer.print("");
            writer.close();

            // calculate average query time
            long total = 0;
            for (long time : queryTime) {
                total += time;
            }

            System.out.println("Average query time: " + total / queryTime.size() + " ms");

        }
    }

    // create a Result TracEval file for all query in msmarco-test-queries.tsv file
    // according to the format : <query_id Q0 doc_id rank score STANDARD>
    private static void writeResultOnFile(String queryId, PriorityQueue<scoreDoc> returnPriorityQueue) throws IOException {

        // write on file the result of the query
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ResultQueryPath, true)))
        {
            // rank of the document
            int rank = 1;
            // number of document to write
            int g = k;

            // <query_id Q0 doc_id rank score STANDARD>
            while (!returnPriorityQueue.isEmpty() && g > 0){
                // get the document
                scoreDoc doc = returnPriorityQueue.poll();
                // get the pid of the document
                String pid = documentIndex.get(doc.getDocID()).getDocno();
                // write the result on file according to the format <query_id Q0 doc_id rank score STANDARD>
                String resultLine = queryId + " Q0 " + pid + " " + rank + " " + doc.getScore() + " STANDARD\n";
                writer.write(resultLine);
                // update rank and number of document to write
                rank++;
                g--;

            }

        }
    }

    // compare ResultQuery of our system file with Qrel file
    // using trec_eval tool to evaluate the system performance
    // command : trec_eval -m all_trec qrels.dev.tsv resultTrecQeury.txt
    public static void runTrecEval(String resultsFile, String qrelsFile) {
        try {
            // get absolute path of the file
            File fileQrels = new File(qrelsFile);
            String AbsolutePathfileQrels = fileQrels.getAbsolutePath();
            File fileResult = new File(resultsFile);
            String AbsolutePathfileResult = fileResult.getAbsolutePath();

            // Command to run trec_eval
            String command = "/usr/local/bin/trec_eval -m all_trec " + AbsolutePathfileQrels + " " + AbsolutePathfileResult;
            //String command = "/usr/local/bin/trec_eval -m ndcg_cut -m map -m recip_rank -m recall.10,100,1000 -M1000  " + AbsolutePathfileQrels + " " + AbsolutePathfileResult;

            // run the command
            Process process = Runtime.getRuntime().exec(command);

            // write the evaluation result on file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/it/unipi/dii/aide/mircv/resources/evaluation.txt", true))) {

                // write the configuration
                writer.write("--------------------------------------------------\n");
                if (Configuration.isConjunctiveON())
                    writer.write("Conjunctive  \n");
                else
                    writer.write("Disjunctive  \n");

                if (Configuration.isScoreON())
                    writer.write("BM25  \n");
                else
                    writer.write("TFIDF \n");

                // write the evaluation result on file
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);

                        writer.write(line + "\n");
                    }
                }

                // check if there is an error
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
