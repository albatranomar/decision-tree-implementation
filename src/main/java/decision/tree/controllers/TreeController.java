package decision.tree.controllers;

import decision.tree.lib.Node;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class TreeController {
    @FXML
    private Pane pane;

    public void inject(Node root) {
        drawTree(pane, root, 1500, 30, 1024);
    }

    private double calculateSubtreeWidth(Node node) {
        if (node.dataset == null || node.children.isEmpty()) {
            return 100;
        }
        double width = 0;
        for (Node child : node.children.values()) {
            width += calculateSubtreeWidth(child);
        }
        return Math.max(width, 100);
    }

    private void drawTree(Pane pane, Node node, double x, double y, double width) {
        Text nodeText = new Text(x, y, node.feature);
        pane.getChildren().add(nodeText);

        if (node.dataset == null) {
            return;
        }

        double childY = y + 100;
        double totalWidth = calculateSubtreeWidth(node);
        double startX = x - totalWidth / 2;

        for (String edgeLabel : node.children.keySet()) {
            Node child = node.children.get(edgeLabel);
            double childWidth = calculateSubtreeWidth(child);
            double childX = startX + childWidth / 2;

            Line line = new Line(x, y + 10, childX, childY - 10);
            pane.getChildren().add(line);

            Text edgeText = new Text((x + childX) / 2, (y + childY) / 2 - 10, edgeLabel);
            pane.getChildren().add(edgeText);

            drawTree(pane, child, childX, childY, childWidth);

            startX += childWidth;
        }
    }
}