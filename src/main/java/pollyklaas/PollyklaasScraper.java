package pollyklaas;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by nehaojha on 11/04/18.
 */
public class PollyklaasScraper {

    private static final String URL = "http://www.pollyklaas.org/missing/index.html?state=&year=&search_name=&page=";
    private static final Logger LOGGER = Logger.getLogger(PollyklaasScraper.class);
    private static String OUTPUT_DIR = "/tmp/htmls";
    private static String INPUT_DIR = "/tmp/htmls";

    static {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8118");
    }

    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
            OUTPUT_DIR = args[0];
        }
        if (args.length > 1) {
            INPUT_DIR = args[1];
        }
        //make sure in place
        File file = new File(OUTPUT_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }

        freshScrap();
//        readFromScrapedFile();

    }

    private static void freshScrap() {
        LOGGER.info("Name, Missing From, Date Missing");
        for (int i = 1; i < 16; i++) {
            Document doc = getDoc(URL + i);
            try {
                saveDoc(doc, "page" + i);
            } catch (IOException e) {
                e.printStackTrace();
            }
            printRequiredFields(doc);
        }
    }

    private static void readFromScrapedFile() throws IOException {
        File file = new File(INPUT_DIR);
        LOGGER.info("Name|Date Missing|Missing From|DOB|Age at Disappearance|Race|Height|Weight|Eyes|Hair|Other");
        File[] files = file.listFiles();
        int pageCount = 1;
        for (File htmlDoc : files) {
            getMissingInformation(Jsoup.parse(htmlDoc, "UTF-8", "http://example.com/"), pageCount++);
        }
    }

    private static void getMissingInformation(Document doc, int pageCount) throws IOException {
        int size = doc.select("#template-list-pkf-836040149 > table").size();
        for (int i = 1; i < size; i++) {
            String link = doc.select("#template-list-pkf-836040149 > table:nth-child(" + (i + 2) + ") > tbody > tr > td:nth-child(2) > p").select("a").attr("href");
            Document document = getDoc(link);
            saveDoc(document, "" + pageCount + "_" + i + ".html");
            printAllInfo(document);
        }
    }

    private static void printAllInfo(Document document) {
        String info = document.select("#twelve > span > p").text();
        String name = document.select("#twenty > span").text();
        LOGGER.info(name + "|" + info.replace("Date Missing:", "").replace("Missing From:", "|").replace("DOB:", "|")
                .replace("Age at Disappearance:", "|").replace("Race:", "|").replace("Height:", "|")
                .replace("Weight:", "|").replace("Eyes:", "|").replace("Hair:", "|").replace("Other:", ""));
    }

    private static void printRequiredFields(Document doc) {
        int tables = doc.select("#template-list-pkf-836040149 > table").size();
        for (int i = 1; i < tables; i++) {
            String info = doc.select("#template-list-pkf-836040149 > table:nth-child(" + (i + 2) + ") > tbody > tr > td:nth-child(2) > p").text();
            LOGGER.info(info.replace("Name: ", "|").replace(" Missing From: ", "|").replace("Date Missing: ", "|"));
        }
    }

    private static void saveDoc(Document doc, String docName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "/" + docName + ".html"))) {
            writer.write(doc.toString());
        }
    }

    private static Document getDoc(String url) {
        try {
            return Jsoup.connect(url)
                    .timeout(60000)
                    .get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
