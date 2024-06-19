import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String sourceFile = "";
        File input_file = new File("input.txt");
        HashMap<String, Node> variableMap;
        /*TODO: the following code just calls these classe to check if they work properly.
                should build some code that calls the xml reader independently and uses it.*/
        try{
            Scanner scanner  = new Scanner(input_file);
            while(scanner.hasNextLine()){
                String data = scanner.nextLine();
                if(data.charAt(0) == 'P'){
                    XMLFileReader xml = new XMLFileReader(sourceFile);
                    variableMap = xml.getMap();
                    System.out.println("Variable Elimination Method");
                    VariableElimination ve = new VariableElimination(data.substring(1), variableMap);
                }
                else if(data.contains("|")){
                    XMLFileReader xml = new XMLFileReader(sourceFile);
                    variableMap = xml.getMap();
                    System.out.println("Bayes Ball Method");
                    BayesBall bb = new BayesBall(data, variableMap);

                    if(BayesBall.getDependent())
                        System.out.println("yes\n");
                    else
                        System.out.println("no\n");
                }
                else{
                    sourceFile = data;
//                    System.out.println("XML File Reader");
//                    XMLFileReader xml = new XMLFileReader(sourceFile);
//                    variableMap = xml.getXMLFile();


                }
            }
        }
        catch(FileNotFoundException e) {
            System.out.println("Error opening file.");
        }catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}