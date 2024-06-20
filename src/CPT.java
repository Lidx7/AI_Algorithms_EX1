import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CPT {

    LinkedHashMap<ArrayList<String>,Float> tableRows; // the order of outcomes as key
    LinkedList<String> varsNames; // the name & order of the columns

    public CPT(Node n)
    {
        this.tableRows = new LinkedHashMap<ArrayList<String>,Float>();
        this.varsNames = new LinkedList<String>();
        fillCPT(n);
    }

    public CPT(LinkedHashMap<ArrayList<String>,Float> table, LinkedList<String> vars)
    {
        this.tableRows = table;
        this.varsNames = vars;
    }

    private void fillCPT(Node n)
    {
        this.varsNames.add(n.key);
        for (Node parent : n.parents)
            this.varsNames.add(parent.key);
        ArrayList<ArrayList<String>> allCombinations = cartesianProd(n);
        int index = 0;
        for (ArrayList<String> sub : allCombinations) {
            this.tableRows.put(sub, n.table.get(index));
            index++;
        }
    }

    private ArrayList<ArrayList<String>> cartesianProd(Node curr)
    {
        int cols = this.varsNames.size(); // num of cols = size of varsNames
        System.out.println("cols: " + cols);
        ArrayList<ArrayList<String>> myOutcomesSets = new ArrayList<ArrayList<String>>(cols);
        myOutcomesSets.add(curr.outcome);
        int size = curr.outcome.size(); // init num of combs to # outcome values of this node
        for (Node parent: curr.parents) {
            myOutcomesSets.add(parent.outcome);
            size *= parent.outcome.size();
        }
        ArrayList<ArrayList<String>> allKeys = new ArrayList<ArrayList<String>>();
        for(int i = 0; i < size; i++) {
            ArrayList<String> combination = new ArrayList<String>(cols);
            int j = 1;
            for(ArrayList<String> sub : myOutcomesSets) {
                combination.add(sub.get((i / j) % sub.size()));
                j *= sub.size();
            }
            allKeys.add(combination);
        }
        return allKeys;
    }


    public void eliminateEvidence(String evidence)
    {
        String[] evid = evidence.split("=");
        String variable = evid[0]; // the evidence variable
        int varCol = 0;
        for (String var : this.varsNames) {
            if (var.equals(variable)) // then we found the index of the wanted variable
                break;
            varCol++;
        }
        if (varCol >= this.varsNames.size())
            return; // because the evidence doesn't appear in this table
        LinkedHashMap<ArrayList<String>, Float> resTable = new LinkedHashMap<ArrayList<String>, Float>();
        for (ArrayList<String> key : this.tableRows.keySet()) {
            if (key.get(varCol).equals("T")) { // we will keep only rows of the evidence
                ArrayList<String> newKey = new ArrayList<String>(key); // copy of the key
                newKey.remove(varCol); // the key without the removed column
                resTable.put(newKey, this.tableRows.get(key)); // the old value in the new key
            }
        }
        this.varsNames.remove(varCol); // update the columns list
        this.tableRows = resTable; // update the rows of the new factor
    }


    public void eliminate(String toEliminate, AtomicInteger additionOper) {
        int eliminateCol = 0;
        for (String name : this.varsNames) {
            if (name.equals(toEliminate))
                break;
            eliminateCol++;
        }
        // merge rows that are the same expect of toEliminate
        LinkedHashMap<ArrayList<String>, Float> resTable = new LinkedHashMap<ArrayList<String>, Float>();
        resTable.putAll(this.tableRows);
        int i_index = 0;
        for (Map.Entry<ArrayList<String>, Float> i : this.tableRows.entrySet()) {
            ArrayList<String> key_i = new ArrayList<String>();
            key_i.addAll(i.getKey());
            key_i.remove(eliminateCol);
            int j_index = 0;
            float x = i.getValue();
            float y = 0;
            for (Map.Entry<ArrayList<String>, Float> j : this.tableRows.entrySet()) {
                ArrayList<String> key_j = new ArrayList<String>();
                key_j.addAll(j.getKey());
                key_j.remove(eliminateCol);

                if (key_i.equals(key_j) && i_index > j_index) { // the keys equal, indexes are not!
                    if (y == 0) {
                        y = j.getValue(); // because every time we have a new j = new addition oper
                        additionOper.addAndGet(1);
                    }
                    else {
                        y += j.getValue();
                    }
                    resTable.remove(j.getKey()); // eliminate the other row
                }
                j_index++;
            }
            resTable.put(i.getKey(), x + y); // add the values of the same key
            i_index++;
        }
        for (ArrayList<String> key : resTable.keySet())
            key.remove(eliminateCol);
        this.tableRows = resTable;
        this.varsNames.remove(eliminateCol);
    }


    public CPT join(CPT f, AtomicInteger mulOper, HashMap<String, Node> vars, Vector<CPT> factors) // this = the smaller, f = the bigger
    {
        LinkedHashMap<ArrayList<String>, Float> resTable = new LinkedHashMap<ArrayList<String>, Float>();
        LinkedList<String> resVars = new LinkedList<String>();
        int counter = 0;
        for (String name : this.varsNames)
            for (String name_ot : f.varsNames)
                if (name.equals(name_ot))
                    counter++;
        if (this.varsNames.size() == counter) {
            return joinWithEqual(f, mulOper, vars, factors);
        }
        return joinWithUnique(f, mulOper, vars, factors);
    }


    private CPT joinWithEqual(CPT f, AtomicInteger mulOper, HashMap<String,Node> vars, Vector<CPT> factors)
    {
        LinkedHashMap<ArrayList<String>, Float> resTable = new LinkedHashMap<ArrayList<String>, Float>();
        LinkedList<String> resVars = new LinkedList<String>();
        resVars.addAll(f.varsNames);
        float x = 0;
        float y = 0;
        for (Map.Entry<ArrayList<String>, Float> dst : f.tableRows.entrySet()) {
            x = 0;
            x += dst.getValue();
            y = findVal(dst.getKey(), this, f.varsNames);
            resTable.put(dst.getKey(), x * y);
            mulOper.addAndGet(1);
        }
        // remove the two above factors from the factors vector:
        factors.remove(f);
        factors.remove(this);
        return new CPT(resTable, resVars); // the merged factor
    }

    public CPT joinWithUnique(CPT f, AtomicInteger mulOper, HashMap<String,Node> vars, Vector<CPT> factors)
    {
        LinkedHashMap<ArrayList<String>,Float> resTable = new LinkedHashMap<ArrayList<String>,Float>();
        LinkedList<String> resVars = new LinkedList<String>();
        Node merged = new Node(f.varsNames.get(0));
        merged.outcome = vars.get(merged.key).outcome;
        for (String name : f.varsNames)
            if (!Objects.equals(name, merged.key))
                merged.parents.add(vars.get(name));
        for (String name : this.varsNames)
            if (!merged.parents.contains(vars.get(name)) && !Objects.equals(name, merged.key) )
                merged.parents.add(vars.get(name));
        resVars.add(merged.key);
        for (Node par : merged.parents)
            resVars.add(par.key);
        ArrayList<ArrayList<String>> allCombinations = cartesianProd(merged);
        for (ArrayList<String> sub : allCombinations)
            resTable.put(sub, findCurrectVal(sub,f,resVars,mulOper));
        factors.remove(f);
        factors.remove(this);
        return new CPT(resTable, resVars);
    }



    private float findCurrectVal(ArrayList<String> sub, CPT other, LinkedList<String> mergedVars, AtomicInteger mulOper)
    {
        float x = findVal(sub, other, mergedVars);
        float y = findVal(sub, this, mergedVars);
        mulOper.addAndGet(1);
        return x*y;
    }

    private float findVal(ArrayList<String> sub, CPT src, LinkedList<String> mergedVars)
    {
        ArrayList<String> key = new ArrayList<String>();
        int index = 0;
        for (String var : src.varsNames) {
            index = 0; // init the index to 0 every new src outcome
            for (String mergedVar : mergedVars) {
                if (var.equals(mergedVar)) {
                    key.add(sub.get(index));
                    break;
                }
                index++;
            }
        }
        for (Map.Entry<ArrayList<String>, Float> row : src.tableRows.entrySet()) {
            if (row.getKey().equals(key))
                return row.getValue();
        }
        return src.tableRows.get(key);
    }
}