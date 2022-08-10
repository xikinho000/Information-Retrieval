/**
 * Developer Kamel Assaf
 * Date modified : 27-May-2018
 * Date updated  : 27-Jul-2022
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenize 
{
    private ArrayList<String> stopWords;
    private ArrayList<String> tokenisedWords;

    public Tokenize(){}

    public void startTokenize() throws FileNotFoundException, 
                                       UnsupportedEncodingException, 
                                       IOException
    {
        int docId = 0;
        //Removal of stop words
        Scanner stopWordFile = new Scanner(new File("Stoplist/stoplist.txt"));
        stopWords = new ArrayList<>();
        tokenisedWords = new ArrayList<>();

        while (stopWordFile.hasNext()) {
            stopWords.add(stopWordFile.next());
        }
        String[] idAndNames = null;
        File folder = new File("TestCollection/");
        File[] files = null;
        if (folder.exists() && folder.isDirectory())
            files = folder.listFiles();
        //   int i =1;
        for(File inputFile : files){
            if (inputFile.isFile()){
                idAndNames = inputFile.getName().split("(?=\\d)(?<!\\d)");
                docId = Integer.parseInt(idAndNames[1]);
                //docId = Integer.parseInt(""+ i );
                createTokens(inputFile, docId);
                // createTokens(inputFile, i);    
                // i++;
            }
        }
        stopWordFile.close();
        Stemmer stem = new Stemmer();
        stem.stemmerCollection(tokenisedWords);
        SearchEngine se = new SearchEngine(stopWords);
    }
    //Create tokens after removing unwanted characters
    private void createTokens(File inputFile, int docId)
            throws FileNotFoundException, UnsupportedEncodingException 
    {
        //, PrintWriter writer
        String[] sentence;
        Scanner srcFile1 = new Scanner(inputFile);
        int docLen = 0;
        while (srcFile1.hasNextLine()) {
            sentence = srcFile1.nextLine().split("[\\s/(,='-]");
            docLen += sentence.length;
        }
        srcFile1.close();

        Scanner srcFile = new Scanner(inputFile);
        while (srcFile.hasNextLine()) {
            // splitting the tokens
            sentence = srcFile.nextLine().split("[\\s/(,='-]");

            for (String term : sentence){
                // case folding
                term = term.toLowerCase().trim();
                // Remove hyphens, possessions, trim trailing and leading
                // special characters like comma, dot 
                // Eliminate one length terms and spaces
                if (!term.isEmpty() && 
                    !Pattern.matches("<*\\D+>", term) &&
                    term.length() > 1 &&
                    !Pattern.matches(".*[0-9].*", term)
                ){
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
                        tokenisedWords.add(
                            term + " " + docId + " " + docLen);
                }
            }
        }
        srcFile.close();
    }

    //sort by value for the max tf
    public static <K, V extends Comparable<? super V>> Map<K, V> 
                    sortByValue(Map<K, V> map
    ){
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());
        return result;
    }
}