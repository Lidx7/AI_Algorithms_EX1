import java.util.*;



public class Node {
    final static int UNCOLORED = 0, COLORED = 1;
    final static int UNVISITED = 0, VISIT_FROM_CHILD = 1, VISIT_FROM_PARENT = 2;
    String key;
    int color;
    int visit;
    ArrayList<Node> parents;
    ArrayList<Node> children;
    ArrayList<String> outcome;
    ArrayList<Float> table;
    CPT cpt;

    public Node(String key) // constructor
    {
        this.key = key;
        this.color = UNCOLORED; // for evidence
        this.visit = UNVISITED; // for search
        this.parents = new ArrayList<Node>();
        this.children = new ArrayList<Node>();
        this.outcome = new ArrayList<String>();
        this.table = new ArrayList<Float>();
        this.cpt = new CPT(this);
    }

    public Node bfs(String toFind)
    {
        Queue<Node> Q = new LinkedList<Node>();
        Node v = null;
        this.color = COLORED;
        Q.add(this);
        while (!Q.isEmpty()) {
            v = Q.remove();
            if (Objects.equals(v.key, toFind))
                return v;
            if (v.hasChild())
                for (Node target : v.children) {
                    if (target.color == UNCOLORED) {
                        target.color = COLORED;
                        Q.add(target);
                    }
                }
        }
        return null;
    }

    boolean hasParent(){
        return !this.parents.isEmpty();
    }

    boolean hasChild(){
        return !this.children.isEmpty();
    }
}