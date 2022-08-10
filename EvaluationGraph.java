/**
 * Developer Kamel Assaf
 * Date modified : 27-May-2018
 * Date updated  : 27-Jul-2022
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/*
 * 
This class is executed after the engine search for all the queries in the 
collection, therefore you will get by this class the 11 highest precision values

*/
public class EvaluationGraph 
{
    private Set<Double> setOfAPValues = new HashSet<>();
    private BufferedWriter bw;
    public EvaluationGraph(int numOfQueries, double logBase)throws IOException{
        startEngineEvaluation(numOfQueries,logBase);
        File file = new File("Evaluation");
        deleteAllFiles(file);
    }
    
    public void deleteAllFiles(File folderName)
    {
        for(File file: folderName.listFiles()) 
            if (!file.isDirectory()) file.delete();
    }
    
    private void startEngineEvaluation(int numOfqueries, double logBase) 
            throws IOException 
    {
        /*You can set manually here the number of queries you have in your Test Collection.*/
        for (int i = 1; i <= numOfqueries; i++)
            MAP(i);
        bw = new BufferedWriter(new FileWriter("GRAPH/11AP[Log"+logBase+"].txt"));
       
        List list = new ArrayList(setOfAPValues);
        Collections.sort(list);
        Collections.reverse(list);
       
        Iterator iterator = list.iterator();
        int i = 1;
        while(iterator.hasNext()){
            if(i==12){
                bw.close();
                break;
            }
            i++;
            bw.write(iterator.next().toString()+"\r\n");
        }
        bw.close();
    }
    
    private void MAP(int queryNumber)
    {
        try {
            double _map = 0;
            int counter = 0;
            System.out.println("Precision Values for Query => "+queryNumber);
            Scanner reader = new Scanner(
                new FileReader("Evaluation/ap["+queryNumber+"].txt")
            );
            while(reader.hasNextLine()){  
                double precision = Double.parseDouble(reader.nextLine().trim());
                if(precision !=0){
                    setOfAPValues.add(precision);
                    _map+=precision;
                    counter++;
                }
            }
        } catch (NumberFormatException | IOException err) {
            System.out.println("[Func::MAP] " + err.getMessage());
        }
    }
}