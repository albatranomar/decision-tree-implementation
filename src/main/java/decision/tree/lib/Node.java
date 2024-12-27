package decision.tree.lib;

import java.util.HashMap;
import java.util.List;

public class Node {
    public String feature;
    public HashMap<String, List<String>> dataset;
    public HashMap<String, Node> children;

    public Node(String value, HashMap<String, List<String>> dataset) {
        this.feature = value;
        this.children = new HashMap<>();
        this.dataset = dataset;
    }
}
