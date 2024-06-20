import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Ex1 {
    public static void main(String[] args) throws IOException {
        String sourceFile = "";
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        File input_file = new File("input.txt");
        FileWriter output_file = new FileWriter("output.txt");
        HashMap<String, Node> variableMap;
        XMLFileReader xml = null;

        /*TODO: the following code just calls these classe to check if they work properly.
                should build some code that calls the xml reader independently and uses it.*/
        try{
            Scanner scanner  = new Scanner(input_file);
            while(scanner.hasNextLine()){
                String data = scanner.nextLine();
                if(data.charAt(0) == 'P'){
                    variableMap = xml.getMap();
                    System.out.println("Variable Elimination Method");
                    VariableElimination ve = new VariableElimination(data.substring(1), variableMap);
                    String ans = ve.getAlgorithm();
                    output_file.write(ans + "\n");
                    System.out.println(ans);

                }
                else if(data.contains("|")){
                    variableMap = xml.getMap();
                    System.out.println("Bayes Ball Method");
                    BayesBall bb = new BayesBall(data, variableMap);

                    if(!bb.getDependent()) {
                        output_file.write("yes\n");
                        System.out.println("yes\n");
                    }
                    else {
                        output_file.write("no\n");
                        System.out.println("no\n");
                    }
                }
                else if(data.contains(".xml")){
                    sourceFile = data;
//                    System.out.println("XML File Reader");
                    xml = new XMLFileReader(sourceFile);
//                    variableMap = xml.getXMLFile();


                }
            }
        }
        catch(FileNotFoundException e) {
            System.out.println("Error opening file.");
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        output_file.close();

    }
}