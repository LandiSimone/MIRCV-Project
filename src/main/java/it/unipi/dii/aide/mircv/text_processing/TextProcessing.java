package it.unipi.dii.aide.mircv.text_processing;
import ca.rmen.porterstemmer.PorterStemmer;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import it.unipi.dii.aide.mircv.models.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class TextProcessing {

    // Regex for removing html tags
    private static final String HTML_REGEX = "<[^>]+>";

    // Regex for removing punctuation and strange characters
    private static final String CharREGEX = "[^a-zA-Z0-9\\s]";

    // Regex for split string into tokens
    private static final String SPLIT_REGEX = "\\s+";

    // stemmer
    private static final PorterStemmer stemmer = new PorterStemmer();

    // stopword in hashset O(1) lookup
    private static final HashSet<String> stopWords;

    static {
        try {
            stopWords = new HashSet<>(Files.readAllLines(Paths.get(FileUtils.Path_StopWords)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Process a document
    public static String[] DocumentProcessing(String doc) throws IOException{
        // 1) preprocessing document
        doc = cleanText(doc);

        // 2) tokenization
        String[] tokens = tokenize(doc);

        // 3) stemming + stopword removal
        if (Configuration.isStemming_stopwordON()) {
            tokens = removeStopWords(tokens);
            tokens = stemming(tokens);
        }
        return tokens;
    }

    // clean a document
    public static String cleanText(String doc) {
        // remove html tags
        doc = doc.replaceAll(HTML_REGEX, "");
        // remove punctuation and strange characters
        doc = doc.replaceAll(CharREGEX, " ");
        // remove multiple spaces
        doc = doc.replaceAll("\\s+", " ");
        // lowercase
        doc = doc.toLowerCase();

        return doc;
    }

    // split a document into tokens
    public static String[] tokenize(String Doc) {
        String[] tokens = Doc.split(SPLIT_REGEX);
        for(int i = 0; i < tokens.length; i++) {
            tokens[i] = trouncateToken(tokens[i]);
        }
        return tokens;
    }

    // trouncate a token if it is too long (MAX_TERM_LENGTH)
    public static String trouncateToken(String token) {
        if (token.length() > FileUtils.MAX_TERM_LENGTH) {
            return token.substring(0,FileUtils.MAX_TERM_LENGTH );
        }
        return token;
    }

    // stem a list of tokens
    public static String[] stemming(String[] tokens) {
        //replace each word with its stem
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = stemmer.stemWord(tokens[i]);
        }
        return tokens;
    }

    // remove stop words from a list of tokens
    public static String[] removeStopWords(String[] tokens) {
        //remove stop words
        ArrayList<String> tokensList = new ArrayList<>(Arrays.asList(tokens));
        tokensList.removeIf(stopWords::contains);
        return tokensList.toArray(new String[0]);
    }
}
