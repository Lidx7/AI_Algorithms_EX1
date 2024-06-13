import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javax.xml.parsers.*;

public class Main {
    public static void main(String[] args) {
        String sourceFile = "";
        File input_file = new File("input.txt");
        try{
            Scanner scanner  = new Scanner(input_file);
            while(scanner.hasNextLine()){
                String data = scanner.nextLine();
                if(data.charAt(0) == 'P'){
                    System.out.println("Variable Elimination Method");
                    VariableElimination ve = new VariableElimination(data);
                }
                else if(data.contains("|")){
                    System.out.println("Bayes Ball Method");
                    BayesBall bb = new BayesBall(data);
                }
                else{
                    sourceFile = data;
                }
            }
        }
        catch(FileNotFoundException e) {
            System.out.println("Error opening file.");
        }
    }
}