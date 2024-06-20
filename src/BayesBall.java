import java.util.*;

public class BayesBall {
    String data;
    boolean conditionsFlag;
    static ArrayList<String> variables;
    static ArrayList<String> trueConditions;
    ArrayList<String> falseConditions;
    static HashMap<String, Node> variableMap;

    public BayesBall(String data, HashMap<String, Node> variableMap) {
        this.data = data;
        this.variableMap = variableMap;
        conditionsFlag = false;
        variables = new ArrayList<String>();
        trueConditions = new ArrayList<String>();
        falseConditions = new ArrayList<String>();
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
                variables.add(String.valueOf(data.charAt(i)));
            }
            if(conditionsFlag){
                char curr_condition = data.charAt(i);
                if(curr_condition >= 'A' && curr_condition <= 'Z') {
                    if (data.charAt(++i) == '=') {
                        if (data.charAt(++i) == 'T') {
                            trueConditions.add(String.valueOf(curr_condition));
                        } else {
                            falseConditions.add(String.valueOf(curr_condition));
                        }
                    }
                }
            }
        }

    }

    public static boolean getDependent(){
        markConditions();
        return Algorithm(variableMap.get(variables.get(0)), variables.get(1), variableMap) != null;
    }

    public static void resetVars()
    { // a simple function that restart the color & visit params of the network
        for (Map.Entry<String, Node> val : variableMap.entrySet()) {
            val.getValue().color = Node.UNCOLORED;
            val.getValue().visit = Node.UNVISITED;
        }
    }

    public static void markConditions() {
        //TODO: if this disturbs the map (conversion from char to string) then "trueConditions" should be changed to string
        for (int i = 0; i < trueConditions.size(); i++) {
            if (variableMap.get(trueConditions.get(i)) != null) {
                variableMap.get(trueConditions.get(i)).color = Node.COLORED;
            }
        }
    }

    public static Node Algorithm(Node source, String target, HashMap<String,Node> vars) {
        Queue<Node> toVisit = new LinkedList<Node>();
        Node curr = source;
        source.visit = Node.VISIT_FROM_CHILD;
        toVisit.add(curr);
        while (!toVisit.isEmpty()) {
            curr = toVisit.remove();
            if (Objects.equals(curr.key, target))
                return vars.get(target);
            if (curr.color == Node.UNCOLORED && curr.visit == Node.VISIT_FROM_CHILD) { // case 1
                if (curr.hasChild())
                    for (Node child : curr.children)
                        if (child.visit == Node.UNVISITED) {
                            child.visit = Node.VISIT_FROM_PARENT;
                            toVisit.add(child);
                        }
                if (curr.hasParent())
                    for (Node parent : curr.parents)
                        if (parent.visit == Node.UNVISITED) {
                            parent.visit = Node.VISIT_FROM_CHILD;
                            toVisit.add(parent);
                        }
            } else if (curr.color == Node.UNCOLORED && curr.visit == Node.VISIT_FROM_PARENT) { // case 2
                if (curr.hasChild())
                    for (Node child : curr.children)
                        if (child.visit != Node.VISIT_FROM_PARENT) {
                            child.visit = Node.VISIT_FROM_PARENT;
                            toVisit.add(child);
                        }
            } else if (curr.color == Node.COLORED && curr.visit == Node.VISIT_FROM_PARENT) { // case 3
                if (curr.hasParent())
                    for (Node parent : curr.parents)
                        if (parent.visit != Node.VISIT_FROM_CHILD) {
                            parent.visit = Node.VISIT_FROM_CHILD;
                            toVisit.add(parent);
                        }
            }
            else if(curr.color == Node.COLORED && curr.visit == Node.VISIT_FROM_CHILD){
                //nothing
            }
        }
        return null;
    }
}
