/**
 * Developer Kamel Assaf
 * Date modified : 27-May-2018
 * Date updated  : 27-Jul-2022
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine
{
    private ArrayList<String> rankedResults = new ArrayList<>();
    private ArrayList<Double> queryTFIDF = new ArrayList<>();
    private ArrayList<String> sortedRank = new ArrayList<>();
    private Set<String> sortedQueryTerms = new HashSet<>();
    private Set<String> sortedPostingTerms = new HashSet<>();
    private ArrayList<Integer> termFrequencyInQuery = new ArrayList<>();
    private int numOfDocs;

    public SearchEngine(ArrayList<String> stopwords)
        throws FileNotFoundException, IOException
    {
        numOfDocs = getNumberOfDocs();
        //loop here through the log base values.
        int numOfQueries = getNumberOfQueries();
        //double logBase = 0.1;
        DecimalFormat form = new DecimalFormat("#.#");
        //while (logBase < 100.0) {
            // logBase = logBase + 0.1;
            double logBase = 10.0;
            int queryNumber = 1;
//System.out.println("Engine Evaluation [Log]"+Double.valueOf(form.format(logBase)));
            startQuerying(stopwords, queryNumber, Double.valueOf(form.format(logBase)));
            EvaluationEngine ee = new EvaluationEngine(numOfQueries);
            EvaluationGraph eg = new EvaluationGraph(numOfQueries, Double.valueOf(form.format(logBase)));
        //}
    }

    private static int getNumberOfQueries()
        throws FileNotFoundException
    {
        Scanner scan = new Scanner(new FileReader("Collection/queries.txt"));
        int queryCount = 0;
        while (scan.hasNext()) {
            String line = scan.nextLine();
            if (!(line.equals(""))) {
                queryCount++;
            }
        }
        scan.close();
        //System.out.println("Number Of queries in collection : " + queryCount);
        return queryCount;
    }

    private void startQuerying(ArrayList<String> stopwords, 
        int queryNumber, double logBase)
            throws FileNotFoundException, IOException 
    {
        Scanner br = new Scanner(new FileReader("Collection/queries.txt"));
        while (br.hasNextLine()) {
            queryTFIDF = new ArrayList<>();
            Scanner readQueryStem = new Scanner(
                new FileReader("Query/readyQuery.sfx"));
            ArrayList<String> arrayOfQueryTerms = new ArrayList<>();
            String query = br.nextLine();
            if (!(query == null)) {
                adjustQuerying(query, stopwords);
                while (readQueryStem.hasNextLine())
                    arrayOfQueryTerms.add(readQueryStem.nextLine());
                queryExecute(arrayOfQueryTerms, numOfDocs, logBase, queryNumber);
                readQueryStem.close();
                queryNumber++;
            }
            queryTFIDF.clear();
        }
    }

    private void queryTFIDf(int numOfDocs, 
        ArrayList<String> arrayOfQueryTerms, double variant)
    {
        try{
            Scanner scan = new Scanner(
                new FileReader("Index_Version2.uncompressed"));
            while (scan.hasNextLine()) {
                String postingLine = scan.nextLine();
                String dictionaryToken = postingLine.
                        substring(0, postingLine.indexOf(","));
                String _df = postingLine.substring(postingLine.
                        indexOf(",") + 1, postingLine.indexOf("|"));
                int df = Integer.parseInt(_df);
                Iterator i = arrayOfQueryTerms.iterator();
                while (i.hasNext()) {
                    String termInQuery = i.next().toString();
                    String[] temp = termInQuery.split(":");
                    termInQuery = temp[0].trim();
                    if (termInQuery.equals(dictionaryToken)) {
                        int queryTF = Integer.parseInt(temp[1].trim());
                        double tfidf = calculateTFIDF(
                                queryTF, df, numOfDocs, variant);
                        queryTFIDF.add(tfidf);
                    }
                }
            }
        } catch (FileNotFoundException | NumberFormatException err) {
            System.out.println("" + err.getMessage());
        }
    }

    private double calculateTFIDF(int tf, int df, int N, double variant)
    {
        double idf = Math.log10((double) N / (double) df);
        idf = idf / Math.log10(variant);
        double result = tf * idf;
        String _result = result + "";
        if (_result.length() > 4) {
            _result = _result.substring(0, 4);
            result = Double.parseDouble(_result);
        }
        return result;
    }

    private int getNumberOfDocs() 
    {
        int num = 0;
        File folder = new File("TestCollection");
        File[] listOfFiles = folder.listFiles();
        for (File fileName : listOfFiles){
            if (fileName.isFile())
                num++;
        }
        return num;
    }

    private void adjustQuerying(String querySentence, 
        ArrayList<String> stopWords) throws FileNotFoundException
    {
        try {
            String[] sentence = querySentence.split("[\\s/(,='-]");
            createQueryTokens(sentence, stopWords);
            Stemmer.stemQuery();
            setTermFrequencyInQuery();
        } catch (IOException err) {
            System.out.println("[FUNC::AdjustQuerying]" + err.getMessage());
        }
    }

    public void createQueryTokens(String[] sentence, 
            ArrayList<String> stopWords)
        throws FileNotFoundException, UnsupportedEncodingException, IOException 
    {
        BufferedWriter writeQuerySTP = new BufferedWriter(
            new FileWriter("Query/query.stp"));
        for (String term : sentence) {
            // case folding
            term = term.toLowerCase().trim();
            // Remove hyphens, possessions, trim trailing and leading
            // special characters like comma, dot 
            // Eliminate one length terms and spaces
            if (!term.isEmpty() && !Pattern.matches("<*\\D+>", term) && 
                    term.length() > 1 && 
                    !Pattern.matches(".*[0-9].*", term)){
                term = term.replaceAll("[^a-zA-Z0-9]*$", "").
                    replaceAll("^[^a-zA-Z0-9]*", "").
                    replaceAll("\'s$", "");

                // i.e is appended to some words by typo, remove that
                Pattern ptrn = Pattern.compile("^\\D+(i\\.e)$");
                Matcher matcher = ptrn.matcher(term);
                if (matcher.matches())
                    term = term.replaceAll("(i\\.e)$", "");
                // abbreviations to tokens
                term = term.replaceAll("\\.", "");
                // Storing the terms and counts
                if (term.length() > 1 && !stopWords.contains(term))
                    writeQuerySTP.write(term + "\r\n");
            }
        }
        writeQuerySTP.close();
    }

    public void setTermFrequencyInQuery() {
        try {
            ArrayList<String> queryTerms = new ArrayList<>();
            populateQueryTerms(queryTerms);
            BufferedWriter writeQuery = new BufferedWriter(
                    new FileWriter("Query/readyQuery.sfx"));
            int tf = 0;
            Scanner reader = new Scanner(
                    new FileReader("Query/query.sfx"));
            while (reader.hasNext()) {
                String _term = reader.nextLine();
                for (int i = 0; i < queryTerms.size(); i++) {
                    String collectionTerm = queryTerms.get(i);
                    if (_term.equals(collectionTerm)) {
                        tf++;
                    }
                }
                writeQuery.write(_term + ":" + tf + "\r\n");
                String temp = _term + ":" + tf + "";
                sortedQueryTerms.add(temp);
                tf = 0;
            }
            reader.close();
            writeQuery.close();
        } catch (IOException err) {
            System.out.println("" + err.getMessage());
        }
    }

    public static void populateQueryTerms(ArrayList<String> queryTerms) 
        throws FileNotFoundException 
    {
        Scanner reader = new Scanner(new FileReader("Query/query.sfx"));
        while (reader.hasNext())
            queryTerms.add(reader.next());
        reader.close();
    }

    private void queryExecute(ArrayList<String> arrayOfQueryTerms, 
        int numOfDocs, double variant, int queryNumber) 
    {
        try{
            int querySize = arrayOfQueryTerms.size();
            double[][] vectorModel = new double[numOfDocs][querySize];
            String[] postings = null;

            queryTFIDf(numOfDocs, arrayOfQueryTerms, variant);
            int queryIndex = 0;

            for (int i = 0; i < arrayOfQueryTerms.size(); i++) {
                String termInQuery = arrayOfQueryTerms.get(i);
                String[] temp = termInQuery.split(":");
                termInQuery = temp[0].trim();
                Scanner scan = new Scanner(
                        new FileReader("Index_Version2.uncompressed"));
               // Scanner scan = new Scanner(new FileReader("output"));
                while (scan.hasNextLine()) {
                    String postingLine = scan.nextLine();
                    String dictionaryToken = postingLine.
                        substring(0, postingLine.indexOf(","));
                    if (termInQuery.equals(dictionaryToken)) {
                        int df = Integer.parseInt(postingLine.substring(
                            postingLine.indexOf(",") + 1, 
                            postingLine.indexOf("|"))
                        );
                        postingLine = postingLine.substring(
                                postingLine.indexOf("|") + 1);
                        postings = postingLine.split("->");
                        for (String post : postings) {
                            String[] docDetails = post.split(",");
                            int docId = Integer.parseInt(docDetails[0]);

                            int tf = Integer.parseInt(docDetails[2]);
                            double tfidf = calculateTFIDF(tf, df, numOfDocs, variant);
                            String node = docId + ":" + tfidf;
                            int _docId = docId - 1;
                            if (tfidf != 0 || tfidf != 0.0)
                                vectorModel[_docId][queryIndex] = tfidf;
                        }
                    }
                }
                scan.close();
                queryIndex++;
            }
            vectorModel(queryNumber, vectorModel);
        } catch (IOException | NumberFormatException err) {
            System.out.println("[Func::QueryExecute]" + err.getMessage());
        }
    }

    public void vectorModel(int queryNumber, double[][] vectorModel) 
        throws IOException 
    {
        double _numerator = 0;
        double sumOfDocTFIDF = 0;
        double sumOfQueryTFIDF = 0;
        int doc_index = 1;
        for (int i = 0; i < vectorModel.length; i++) {
            for (int j = 0; j < queryTFIDF.size(); j++) {
                double temp = queryTFIDF.get(j);
                double numerator;
                if (temp != 0 || temp != 0.0) {
                    numerator = vectorModel[i][j] * temp;
                } else {
                    numerator = 0;
                }
                _numerator += numerator;
                sumOfDocTFIDF += Math.pow(vectorModel[i][j], 2);
                sumOfQueryTFIDF += Math.pow(queryTFIDF.get(j), 2);
            }
            
            /*Using BigDecimal data type*/
            BigDecimal docsTfidf = new BigDecimal(sumOfDocTFIDF);

            BigDecimal queryTfidf = new BigDecimal(sumOfQueryTFIDF);
            BigDecimal _d = new BigDecimal(
                    docsTfidf.multiply(queryTfidf).doubleValue());
            BigDecimal _n = new BigDecimal(_numerator);
            BigDecimal denominator = new BigDecimal(
                    Math.sqrt(_d.doubleValue()));
            BigDecimal zero = new BigDecimal(0);
            BigDecimal zerozero = new BigDecimal(0.0);
            BigDecimal cosine;

            if (!(denominator.compareTo(zero) == 0)) {
                cosine = new BigDecimal(_n.divide(
                    denominator, 20, RoundingMode.HALF_UP).toPlainString());
                BigDecimal bg = new BigDecimal(1.0);
                int x = cosine.compareTo(bg);
                if (!(x == 1)) {//if not big than one then calculate.
                    if (cosine.compareTo(zero) == 0 || 
                        cosine.compareTo(zerozero) == 0) {
                        doc_index++;
                        sumOfDocTFIDF = 0;
                        sumOfQueryTFIDF = 0;
                        _numerator = 0;
                    } else {
                        String result = cosine + " - doc" + doc_index;
                        sortedRank.add(result);
                        doc_index++;
                        sumOfDocTFIDF = 0;
                        sumOfQueryTFIDF = 0;
                        _numerator = 0;
                    }
                }
            } else {
                doc_index++;
                sumOfDocTFIDF = 0;
                sumOfQueryTFIDF = 0;
                _numerator = 0;
            }
        }
        saveSortedDocumentRanking(queryNumber);
    }

    private void saveSortedDocumentRanking(int queryNumber) 
        throws IOException 
    {
        try {
            BufferedWriter bw = new BufferedWriter(
                new FileWriter("Evaluation/SortedResults["+queryNumber+"].txt"));
            Collections.sort(sortedRank);
            Collections.reverse(sortedRank);
            Iterator<String> iterator = sortedRank.iterator();
            int _counter = 0;
            while (iterator.hasNext()) {
                if (_counter == 10)
                    break;
                _counter++;
                String line = iterator.next();
                String[] value = line.split("-");
                String cosine = value[0].trim();
                String docNum = value[1].trim().substring(3);
                bw.write(docNum + " " + cosine + "\r\n");
            }
            sortedRank.clear();
            bw.close();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}
