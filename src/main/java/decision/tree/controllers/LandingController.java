package decision.tree.controllers;

import decision.tree.Main;
import decision.tree.lib.Alerter;
import decision.tree.lib.DecisionTree;
import decision.tree.lib.Node;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static decision.tree.lib.Util.readCSV;

public class LandingController {
    @FXML
    private ComboBox<String> cbClasses;

    @FXML
    private CheckBox cbShuffle;

    @FXML
    private TextArea taOutput;

    private File dataFile;
    private HashMap<String, List<String>> dataset;
    private Node tree;

    @FXML
    void initialize() {
        cbClasses.setDisable(true);
    }

    @FXML
    void onSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setTitle("Select a file that contains the dataset");
        File selectedFile = fileChooser.showOpenDialog(cbClasses.getScene().getWindow());
        if (selectedFile != null) {
            dataFile = selectedFile;

            try {
                dataset = readCSV(dataFile);

                cbClasses.getItems().clear();
                for (String f: dataset.keySet()) {
                    cbClasses.getItems().add(f);
                }

                cbClasses.setDisable(false);
            } catch (IOException e) {
                Alerter.error("Something went wrong!", e.getMessage()).show();
                cbClasses.setDisable(true);
            }

            return;
        }

        cbClasses.setDisable(true);
    }

    @FXML
    void onTargetClass() {
        String targetClass = cbClasses.getValue();
        if (targetClass == null || targetClass.isEmpty() || !dataset.containsKey(targetClass)) {
            Alerter.warn("You need to select a class!", "The class need to be on in the dataset").show();
            return;
        }

        var splited = DecisionTree.splitTestTrain(dataset, 0.3, cbShuffle.isSelected());
        var test_dataset = splited.get("test");
        var train_dataset = splited.get("train");

        Node root = DecisionTree.constructTree(targetClass, dataset, train_dataset);

        tree = root;

        int[][] m = DecisionTree.confusionMatrix(targetClass, root, dataset, test_dataset);

        StringBuilder builder = new StringBuilder();
        for (int[] r: m) {
            builder.append(Arrays.toString(r) + "\n");
            System.out.println(Arrays.toString(r));
        }

        double accuracy = DecisionTree.calculateAccuracy(m);
        builder.append("Accuracy: " + accuracy + "\n");
        System.out.println("Accuracy: " + accuracy);

        double[] precision = DecisionTree.calculatePrecision(m);
        double[] recall = DecisionTree.calculateRecall(m);
        double[] f1Scores = DecisionTree.calculateF1Score(precision, recall);

        for (int i = 0; i < precision.length; i++) {
            builder.append("Class " + i + ": Precision = " + precision[i] + ", Recall = " + recall[i] + ", F1 Score = " + f1Scores[i] + "\n");
            System.out.println("Class " + i + ": Precision = " + precision[i] + ", Recall = " + recall[i] + ", F1 Score = " + f1Scores[i]);
        }

        taOutput.setText(builder.toString());
    }

    @FXML
    void onShowTree() throws IOException {
        if (tree == null) {
            Alerter.warn("You need to select a target class first!").show();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Tree.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();

        stage.setTitle("Tree!");
        stage.setScene(scene);
        stage.show();
        stage.setMaximized(true);
        TreeController controller = fxmlLoader.getController();
        controller.inject(tree);
    }

}
