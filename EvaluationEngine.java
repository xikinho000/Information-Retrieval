/**
 * Developer Kamel Assaf
 * Date modified : 27-May-2018
 * Date updated  : 27-Jul-2022
 */
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;

public class EvaluationEngine 
{
    private int countRelevantRetrieved = 0;
    private Scanner resultsFile, reader;
    private ArrayList<String> sorted = new ArrayList<>();
  
    public EvaluationEngine(int numOfQueries) throws IOException 
    {
        // int numberOfqueries = getNumberOfQueries();
        for (int i = 1; i <= numOfQueries; i++) {
            int indexRelevant = 0;
            int indexPrecision = 0;
            int indexRecall = 0;
            TreeMap<Integer, String> relevantResults = new TreeMap<>();
            TreeMap<Integer, Double> precisionValues = new TreeMap<>();
            TreeMap<Integer, Double> recallValues = new TreeMap<>();
            setResultsRelevance(i, indexRelevant, relevantResults);
            setPrecision(i, indexPrecision, relevantResults, precisionValues);
            setRecall(i, indexRecall, relevantResults, recallValues);
            calculateAP(i, recallValues, precisionValues);
        }
    }

    public void setResultsRelevance(int queryNumber, int index, 
            TreeMap<Integer, String> relevantResults
    ){
        try {
            resultsFile = new Scanner(new FileReader(
                "Evaluation/SortedResults[" + queryNumber + "].txt"));
            double count = 0;
            int rr = 0;
            int _counter = 0;
            while (resultsFile.hasNextLine()) {
                String line = resultsFile.nextLine();
                String result[] = line.split(" ");
                String right = result[1];
                String docNum = result[0];
                if (!right.equals("0.0")) {
                    checkRelevanceInCollection(count, rr, index, 
                            docNum, queryNumber, relevantResults
                    );
                    index++;
                }
                _counter++;
                if (_counter > 10)
                    break;
            }
            resultsFile.close();
        } catch (FileNotFoundException err) {
            System.out.println(err.getMessage());
        }
    }

    private void checkRelevanceInCollection(double count, int rr, 
            int index, String docNumber, int queryNumber, 
            TreeMap<Integer, String> relevantResults
    ){
        try{
            reader = new Scanner(new FileReader("Collection/RelevanceList.txt"));
            while (reader.hasNextLine()) {
                String line[] = reader.nextLine().split(" ");
                String queryNumberInCollection = line[0];
                int queryIndex = Integer.parseInt(queryNumberInCollection.trim());
                String docNumberInCollection = line[2];
                if (docNumber.equals( docNumberInCollection) && 
                    (queryIndex == queryNumber)) {
                    countRelevantRetrieved++;
                    relevantResults.put(index, docNumber + ":R");
                    return;
                }
            }
            relevantResults.put(index, docNumber + ":N");
            reader.close();
        } catch (NumberFormatException | IOException err) {
            System.out.println(err.getMessage());
        }
    }

    private TreeMap<Integer, Double> setPrecision(int queryNumber, 
            int index, TreeMap<Integer, String> relevantResults, 
            TreeMap<Integer, Double> precisionValues) 
    {
        System.out.println("Calculating Precision...");
        double rr = 0;
        double count = 0;

        for (Entry<Integer, String> entry : relevantResults.entrySet()) {
            int id = entry.getKey();
            String str = entry.getValue();
            if (str.substring(str.indexOf(":")).contains("R")) {
                //so its relevant.
                rr++;
            }
            count++;
            if (!(rr == 0) && !(count == 0)){
                BigDecimal _rr = new BigDecimal(rr);
                BigDecimal _count = new BigDecimal(count);
                BigDecimal _calc = new BigDecimal(
                        _rr.divide(_count, 20, 
                        RoundingMode.HALF_UP).toPlainString()
                );
                String calc = "" + _calc;
                if (calc.length() > 4) {
                    calc = calc.substring(0, 4);
                }
                precisionValues.put(index, Double.parseDouble(calc));
                index++;
            }
        }
        return precisionValues;
    }

    private TreeMap<Integer, Double> setRecall(int queryNumber, 
        int index, TreeMap<Integer, String> relevantResults, 
        TreeMap<Integer, Double> recallValues) 
    {
        double rr = 0;
        double calculation = 0;
        for (Entry<Integer, String> entry : relevantResults.entrySet()) {
            int id = entry.getKey();
            String str = entry.getValue();
            if (str.substring(str.indexOf(":")).contains("R"))
                rr++;
            if (!(rr == 0) && !(countRelevantRetrieved == 0)) {
                calculation = (double) rr / (double) countRelevantRetrieved;
                String calc = "" + calculation;
                if (calc.length() > 4) {
                    calc = calc.substring(0, 4);
                }
                recallValues.put(index, Double.parseDouble(calc));
                index++;
            }
        }
        countRelevantRetrieved = 0;
        return recallValues;
    }

    public void calculateAP(int queryNumber, TreeMap<Integer, Double> recallList, 
        TreeMap<Integer, Double> precisionList) 
    {
        try {
            Set<Double> unredant_recall = new HashSet<>();
            TreeMap<Integer, Double> unredanceRecalls = new TreeMap<>();
            LinkedList<Double> avg = new LinkedList<>();
            TreeMap<Integer, Double> recall_values = new TreeMap<>();
            TreeMap<Integer, Double> precision_values = new TreeMap<>();
            BufferedWriter average_file = new BufferedWriter(
                new FileWriter("Evaluation/ap[" + queryNumber + "].txt")
            );
            /*Recall*/
            int tempIndex = 0;
            for (Entry<Integer, Double> entry : recallList.entrySet()){
                double _recall = entry.getValue();
                String temp = _recall + "";
                if (temp.length() > 4) {
                    temp = temp.substring(0, 4);
                }
                double recall_value = Double.parseDouble(temp);
                recall_values.put(tempIndex, recall_value);
                tempIndex++;
                if (recall_value != 0.0) 
                    unredant_recall.add(recall_value);
            }
            tempIndex = 0;
            int count = 0;
            Object[] uniqueRecall = unredant_recall.toArray();
            for (int i = 0; i < uniqueRecall.length; i++) {
                float value = Float.parseFloat(uniqueRecall[i].toString());
                // unique_recall_file.write(value+"\r\n");
                double average = 0.0;
                for (int j = 0; j < recall_values.size(); j++) {
                    if (value == Float.parseFloat(recall_values.get(j).toString())) {
                        average += Double.parseDouble(precisionList.get(j).toString());
                        count++;
                    }
                }
                average = average / count;
                avg.add(average);
                average_file.write(average + "\r\n");
                count = 0;
            }
            average_file.close();
        } catch (IOException | NumberFormatException err) {
            System.out.println(err.getMessage());
        }
    }
}