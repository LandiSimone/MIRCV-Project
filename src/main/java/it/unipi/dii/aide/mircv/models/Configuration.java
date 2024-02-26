package it.unipi.dii.aide.mircv.models;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.IOException;

public class Configuration {

    // compression reading
    private static boolean compressionON;

    // stemming and stopword removal
    private static boolean stemming_stopwordON;

    // compression index
    private static boolean index_compressionON;

    // testing
    private static boolean testing_ON ;

    // score function : true = BM25 - false= TFIDF
    private static boolean scoreON;

    // query retrieval : true = conjunctive - false= disjunctive
    private static boolean conjunctiveON;

    public static String Collection_Compressed_path;
    public static String Collection_Uncompressed_path;

    static {

        try {
            System.out.println("----------------- CONFIGURATION  -----------------");
            // read json configuration file
            JsonObject jsonObject = JsonParser.parseReader(new FileReader("src/main/java/it/unipi/dii/aide/mircv/resources/configuration.json")).getAsJsonObject();
            compressionON = jsonObject.get("read_compressionON").getAsBoolean();
            stemming_stopwordON = jsonObject.get("stemming_stopwordON").getAsBoolean();
            index_compressionON = jsonObject.get("index_compressionON").getAsBoolean();
            testing_ON = jsonObject.get("testing_ON").getAsBoolean();
            Collection_Compressed_path = jsonObject.get("Collection_Compressed_path").getAsString();
            Collection_Uncompressed_path = jsonObject.get("Collection_Uncompressed_path").getAsString();
            // print configuration
            System.out.println("Compressed Reading = " + compressionON + "\n" + "Stemming = " + stemming_stopwordON + "\n" + "Stopword = "+ stemming_stopwordON +  "\n" + "Index Compression = " + index_compressionON + "\n" + "Testing = " + testing_ON);
            System.out.println("--------------------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configuration() {
    }

    public Configuration(boolean compressionON, boolean stemming_stopwordON, boolean index_compressionON, boolean testing_ON) {
        this.compressionON = compressionON;
        this.stemming_stopwordON = stemming_stopwordON;
        this.index_compressionON = index_compressionON;
        this.testing_ON = testing_ON;
    }

    public static boolean isCompressionON() {
        return compressionON;
    }

    public static boolean isStemming_stopwordON() {
        return stemming_stopwordON;
    }

    public static boolean isIndex_compressionON() {
        return index_compressionON;
    }

    public static boolean isTesting() {
        return testing_ON;
    }
    public static boolean isScoreON() {
        return scoreON;
    }

    public static boolean isConjunctiveON() {
        return conjunctiveON;
    }

    public void setConjunctiveON(boolean compressionON) {
        this.conjunctiveON = compressionON;
    }

    public void setScoreON(boolean scoreON) {
        this.scoreON = scoreON;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "read_compressionON=" + compressionON +
                ", stemming_stopwordON=" + stemming_stopwordON +
                ", index_compressionON=" + index_compressionON +
                ", testing_ON=" + testing_ON +
                ", scoreON=" + scoreON +
                ", conjunctiveON=" + conjunctiveON +
                '}';
    }
}
