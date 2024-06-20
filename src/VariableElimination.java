import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VariableElimination {
    String data;
    boolean conditionsFlag;
    boolean hiddenVarFlag;
    ArrayList<String> variables;
    ArrayList<String> trueConditions;
    ArrayList<String> falseConditions;
    ArrayList<String> hiddenVariables; //TODO: implement this!!!!
    HashMap<String, Node> variableMap;

    int addingSum;
    int multiSum;

    public VariableElimination(String data, HashMap<String, Node> varMap) {
        this.data = data;
        this.variableMap = varMap;
        variables = new ArrayList<String>();
        trueConditions = new ArrayList<String>();
        falseConditions = new ArrayList<String>();
        hiddenVariables = new ArrayList<String>();
        conditionsFlag = false;
        hiddenVarFlag = false;
        addingSum = 0;
        multiSum = 0;
        readVariables();
        System.out.println("Variables: " + variables);
        System.out.println("True Conditions: " + trueConditions);
        System.out.println("False Conditions: " + falseConditions);
        System.out.println("Hidden Variables: " + hiddenVariables);
    }

    public void readVariables(){
        for(int i = 0; i < data.length(); i++){
            if(data.charAt(i) == '|'){
                conditionsFlag = true;
            }
            if(!conditionsFlag && data.charAt(i) >= 'A' && data.charAt(i) <= 'Z'){
                char curr_condition = data.charAt(i);
                if(curr_condition >= 'A' && curr_condition <= 'Z') {
                    if (data.charAt(++i) == '=') {
                        if (data.charAt(++i) == 'T') {
                            variables.add(String.valueOf(curr_condition));
                        }
                    }
                }
            }
            if(conditionsFlag){
                char curr_condition = data.charAt(i);
                if(!hiddenVarFlag && (curr_condition >= 'A' && curr_condition <= 'Z')) {
                    if (data.charAt(++i) == '=') {
                        if (data.charAt(++i) == 'T') {
                            trueConditions.add(String.valueOf(curr_condition));
                        } else {
                            falseConditions.add(String.valueOf(curr_condition));
                        }
                    }
                }
                else if(curr_condition == ')') {
                    hiddenVarFlag = true;
                }
                else if(hiddenVarFlag && (curr_condition >= 'A' && curr_condition <= 'Z')){
                    hiddenVariables.add(String.valueOf(curr_condition));
                }
            }
        }
    }

    public String getAlgorithm(){
        return Algorithm();
    }



    public String Algorithm() {
        float ans = 0; // the final answer
        AtomicInteger mulOpers = new AtomicInteger(0);
        AtomicInteger addOpers = new AtomicInteger(0);

        String queryVar = variables.get(0);
        HashMap<String, Node> allVariables = new HashMap<String, Node>(variableMap);

        // Init Factors:
        Vector<CPT> factors = new Vector<CPT>();
        for (Node q : allVariables.values()) {
            factors.add(new CPT(allVariables.get(q.key)));
        }

        // Evidence Outcomes Update:

        for (CPT factor : factors)
            for (String evidence : trueConditions)
                if (factor.varsNames.contains(evidence))
                    factor.eliminateEvidence(evidence);

        // at this moment, the factors contain only query & hidden vars

        // Unnecessary Factors Elimination:

        ArrayList<String> querEvid = new ArrayList<String>();
        for (String name : variableMap.keySet()) {
            if (!hiddenVariables.contains(name) && !querEvid.contains(name)) {
                querEvid.add(name);
            }
        }

        // Quick Answer without Join/Elimination and Normalization case checking:

        if (querEvid.size() == 1)
            return quickAnswer(factors, querEvid, queryVar, addOpers, mulOpers);

        // Eliminate Factors of h Vars Which q\e Vars Aren't Ancestor of Them

        ArrayList<String> hiddenToRemove = new ArrayList<String>();
        for (int i = 0; i < hiddenVariables.size(); i++) {
            int counter = 0;
            for (String qeurOrEviVar : querEvid) {
                BayesBall.resetVars();
                if (variableMap.get(hiddenVariables.get(i)).bfs(qeurOrEviVar) == null)
                    counter++;
                if (counter == querEvid.size()) {
                    hiddenToRemove.add(hiddenVariables.get(i));
                    hiddenVariables.remove(hiddenVariables.get(i));
                    counter = 0;
                }
            }
        }

        // Eliminate Factors with h Vars Which Are Independent in Query:

        for (int i = 0; i < hiddenVariables.size(); i++) {
            BayesBall.resetVars();
            if (BayesBall.getDependent()) {
                hiddenToRemove.add(hiddenVariables.get(i));
                hiddenVariables.remove(hiddenVariables.get(i));
                i = 0;
            }
        }

        for (String hid : hiddenToRemove) {
            for (int i = 0; i < factors.size(); i++)
                if (factors.get(i).varsNames.contains(hid)) {
                    factors.remove(i);
                    i = 0;
                }
        }
        sortFactors(factors);

        // Hidden Vars while until no more Hidden vars in the factors:

        String toEliminate = "";
        while (!hiddenVariables.isEmpty()) {
            toEliminate = hiddenVariables.remove(0);
            // join on each hidden var - until only 1 factor contain it, then eliminate it from that factor:
            for (int i = 0; i < factors.size(); i++)
                if (factors.get(i).varsNames.contains(toEliminate))
                    for (int j = i + 1; j < factors.size(); j++)
                        if (factors.get(j).varsNames.contains(toEliminate) && factors.get(i).varsNames.contains(toEliminate)) {
                            factors.add(factors.get(i).join(factors.get(j), mulOpers, variableMap, factors));
                            sortFactors(factors);
                            i = 0;
                            j = 0;
                        }
            for (int i = 0; i < factors.size(); i++) // eliminate the curr hidden var from its factor:
                if (factors.get(i).varsNames.contains(toEliminate)) {
                    factors.get(i).eliminate(toEliminate, addOpers);
                    removeFactor(factors);
                    i = 0;
                }
        }
        // at this moment, we have only factor(s) with the query variable

        // Join on Query:

        while (factors.size() > 1)
            factors.add(factors.get(0).join(factors.get(1), mulOpers, variableMap, factors));

        // Normalization & Answer Returning:

        System.out.println("Factors: " + factors.get(0));
        System.out.println("Query Variable: " + queryVar);
        System.out.println(addOpers);
        ans = normalize(factors.get(0), queryVar, addOpers);
        //System.out.println("Additions: " + addOpers);
        //System.out.println("Multiplications: " + mulOpers);
        //System.out.println("Answer: " + ans);
        return String.format("%.5f", ans) + "," + addOpers + "," + mulOpers;
    }




    private static String quickAnswer(Vector<CPT> factors, ArrayList<String> querEvid, String queryVar, AtomicInteger addOpers, AtomicInteger mulOpers) {
        // the func returns the answer of the query when the answer is already given to us
        float ans = 0;
        for (CPT factor : factors)
            if (factor.varsNames.size() == 1 && factor.varsNames.contains(querEvid.get(0)))
                ans = ansWithoutNormalize(factor, queryVar);
        return String.format("%.5f", ans) + "," + addOpers + "," + mulOpers;
    }

    private static float normalize(CPT factor, String query, AtomicInteger addOpers) {
        String outcome = "T";
        ArrayList<String> key = new ArrayList<String>();
        key.add(outcome);
        float up = 0;
        float down = 0;
        for (Map.Entry<ArrayList<String>, Float> row : factor.tableRows.entrySet()) {
            if (down == 0)
                down = row.getValue(); // to avoid addition operation
            else {
                down += row.getValue();
                addOpers.addAndGet(1);
            }
            if (row.getKey().equals(key))
                up = row.getValue();
        }
        return up / down;
    }

    private static float ansWithoutNormalize(CPT factor, String query) { // answer calculation when normalization is no needed
        String outcome = "T";
        ArrayList<String> key = new ArrayList<String>();
        key.add(outcome);
        float res = 0;
        for (Map.Entry<ArrayList<String>, Float> row : factor.tableRows.entrySet())
            if (row.getKey().equals(key))
                res = row.getValue();
        return res;
    }

    private static void sortFactors(Vector<CPT> factors) { // by size of vars & outcomes in factor
        CPT temp = factors.get(0);
        LinkedHashMap<ArrayList<String>, Float> temp_table = factors.get(0).tableRows;
        for (int i = 0; i < factors.size(); i++) {
            for (int j = 1; j < factors.size() - i; j++) {
                if (factors.get(i).varsNames.size() < factors.get(j).varsNames.size()) {
                    temp = factors.get(i);
                    factors.set(i, factors.get(j));
                    factors.set(j, temp);
                } else if (factors.get(i).varsNames.size() == factors.get(j).varsNames.size() && factors.get(i).tableRows.size() < factors.get(j).tableRows.size()) {
                    temp_table = factors.get(i).tableRows;
                    LinkedList<String> temp_key = factors.get(i).varsNames;
                    factors.get(i).varsNames = factors.get(j).varsNames;
                    factors.get(i).tableRows = factors.get(j).tableRows;
                    factors.get(j).tableRows = temp_table;
                    factors.get(j).varsNames = temp_key;
                }
            }
        }
    }

    private static void removeFactor(Vector<CPT> factors) { // every factor with a size of 1 or less is being removed!
        factors.removeIf(factor -> factor.tableRows.size() < 2);
    }

}
