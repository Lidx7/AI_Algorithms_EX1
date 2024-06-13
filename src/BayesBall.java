import java.util.ArrayList;
public class BayesBall {
    String data;
    boolean conditionsFlag;
    ArrayList<Character> variables;
    ArrayList<Character> trueConditions;
    ArrayList<Character> falseConditions;

    public BayesBall(String data) {
        this.data = data;
        conditionsFlag = false;
        variables = new ArrayList<Character>();
        trueConditions = new ArrayList<Character>();
        falseConditions = new ArrayList<Character>();
        readVariables();
        System.out.println("Variables: " + variables);
        System.out.println("True Conditions: " + trueConditions);
        System.out.println("False Conditions: " + falseConditions);
    }

    /*TODO: this could be too messy. find a way to make it less junked / simpler and don't relay
        on the positions of the chars but rather their relations to the symbols ( | , = T F) */
    public void readVariables(){
        for(int i=0; i<data.length(); i++){
            if(data.charAt(i) == '|'){
                conditionsFlag = true;
            }
            if(!conditionsFlag && data.charAt(i) >= 'A' && data.charAt(i) <= 'Z'){
                variables.add(data.charAt(i));
            }
            if(conditionsFlag){
                char curr_condition = data.charAt(i);
                if(curr_condition >= 'A' && curr_condition <= 'Z') {
                    if (data.charAt(++i) == '=') {
                        if (data.charAt(++i) == 'T') {
                            trueConditions.add(curr_condition);
                        } else {
                            falseConditions.add(curr_condition);
                        }
                    }
                }
            }
        }
    }
}
