package decision.tree.lib;

import java.util.*;

public class DecisionTree {
    public static int[][] confusionMatrix(String targetClass, Node tree, HashMap<String, List<String>> testData) {
        List<String> possiblePredictions = getPossibleValues(targetClass, testData);
        HashMap<String, Integer> predictionMap = new HashMap<>();
        for (int i = 0; i < possiblePredictions.size(); i++) {
            predictionMap.put(possiblePredictions.get(i), i);
        }


        int[][] matrix = new int[possiblePredictions.size()][possiblePredictions.size()];

        int numberOfRows = 0;
        for (String feature: testData.keySet()) {
            numberOfRows = testData.get(feature).size();
            break;
        }

        for (int i = 0; i < numberOfRows; i++) {
            Node ptr = tree;
            String selectedBranch = testData.get(tree.feature).get(i);
            while (ptr.dataset != null) {
                for (String branch: ptr.children.keySet()) {
                    if (branch.equals(selectedBranch)) {
                        ptr = ptr.children.get(branch);
                        if (ptr.dataset != null) selectedBranch = testData.get(ptr.feature).get(i);
                    }
                }
            }

            String actual = testData.get(targetClass).get(i);
            String pre = ptr.feature;

            matrix[predictionMap.get(actual)][predictionMap.get(pre)]++;
        }

        return matrix;
    }

    public static Node constructTree(String targetClass, HashMap<String, List<String>> originalDataset, HashMap<String, List<String>> trainDataset) {
        return constructTreeHelper(targetClass, originalDataset, trainDataset);
    }

    private static Node constructTreeHelper(String targetClass, HashMap<String, List<String>> originalDataset, HashMap<String, List<String>> dataset) {
        Node root = null;
        double maxGainRatio = Double.MIN_VALUE;

        HashSet<String> set = new HashSet<>(dataset.get(targetClass));

        // if the dataset contain one value for the target class then its must be a leaf node
        if (set.size() == 1) {
            return new Node(set.iterator().next(), null);
        }

        // Calculate the maximum gain ratio of all features
        for (String feature: dataset.keySet()) {
            // skip the target class feature DUH.
            if (feature.equals(targetClass)) continue;

            Node node = new Node(feature, dataset);
            double gainRatio = GR(targetClass, node);

            if (gainRatio > maxGainRatio) {
                maxGainRatio = gainRatio;
                root = node;
            }
        }

        // When the dataset have only the target class left
        // Or the gain ratio turned out to be zero
        // Then there is no point in splitting and expand to more branches
        if ((dataset.size() == 1 && dataset.containsKey(targetClass)) || maxGainRatio == 0 || maxGainRatio == Double.MIN_VALUE) {
            // In the remaining dataset we need to know the values of the target class
            // To decide which one will the final decision
            List<String> possibleDecisions = getPossibleValues(targetClass, originalDataset);


            String finalDecision = null;
            int finalVote = 0;

            // Iterate over the possible decisions
            for (String decision: possibleDecisions) {

                // How many record vote for this to be the final decision
                int decisionVotes = 0;
                for (String vote: dataset.get(targetClass)) {
                    if (decision.equals(vote)) {
                        decisionVotes++;
                    }
                }

                if (decisionVotes >= finalVote) {
                    finalVote = decisionVotes;
                    finalDecision = decision;
                }
            }

            return new Node(finalDecision, null);
        }

        // Here we know that the node needs to be split
        // Based on the selected feature with the maximum Gain Ratio (root.feature)

        // Iterate over the possible branches for the node
        for (String label: getPossibleValues(root.feature, originalDataset)) {
            // For each branch we need to split the dataset that will be used by each child(branch)


            HashMap<String, List<String>> new_dataset = new HashMap<>();

            // We select all feature from the current dataset except for the feature that we are splitting on DUH
            for (String featureName: dataset.keySet()) {
                if (featureName.equals(root.feature)) continue;
                new_dataset.put(featureName, new ArrayList<>());
            }

            // Iterate over the rows to select appropriate rows for next node
            for (int i = 0; i < dataset.get(targetClass).size(); i++) {
                String rowLabel = dataset.get(root.feature).get(i);

                // If the row has the correct value of the branch we add it to the new dataset
                if (rowLabel.equals(label)) {

                    // Adding the row to the new dataset
                    for (String featureName: dataset.keySet()) {
                        if (featureName.equals(root.feature)) continue;
                        new_dataset.get(featureName).add(dataset.get(featureName).get(i));
                    }
                }
            }

            // Add the branch and recurse over the remaining dataset
            root.children.put(label, constructTreeHelper(targetClass, originalDataset, new_dataset));
        }

        return root;
    }

    /**
     * Calculates the Gain Ratio of some node 'parent'
     * @param targetClass
     * @param parent
     * @return Gain Ratio
     */
    private static double GR(String targetClass, Node parent) {
        if (parent.dataset.get(targetClass).isEmpty()) {
            return Double.MIN_VALUE;
        }

        List<String> targetValues = getPossibleValues(targetClass, parent.dataset);

        HashMap<String, Integer[]> entropy = new HashMap<>();

        // Calculate the entropy list of each feature
        // entropy list -> in case of binary classification [numberOfPositiveRecords, numberOfNegativeRecords]
        for (int i = 0; i < parent.dataset.get(parent.feature).size(); i++) {
            String label = parent.dataset.get(parent.feature).get(i);

            // For each label (possible value) in the feature will create an entropy list
            if (!entropy.containsKey(label)) {
                Integer[] numbers = new Integer[targetValues.size()];
                Arrays.fill(numbers, 0);

                entropy.put(label, numbers);
            }

            for (int j = 0; j < targetValues.size(); j++) {
                String targetValue = targetValues.get(j);
                if (parent.dataset.get(targetClass).get(i).equals(targetValue)) {
                    entropy.get(label)[j]++;
                }
            }
        }

        // Entropy list of the parent
        Integer[] parentEntropy = new Integer[targetValues.size()];
        Arrays.fill(parentEntropy, 0);

        int parentSum = 0;
        for (String e: entropy.keySet()) {
            for (int i = 0; i < entropy.get(e).length; i++) {
                parentEntropy[i] += entropy.get(e)[i];
                parentSum += entropy.get(e)[i];
            }
        }

        double parentE = E(Arrays.asList(parentEntropy));

        double labelsE = 0;
        double splitInfo = 0;

        for (String label: entropy.keySet()) {
            double sum = 0;
            for (Integer n: entropy.get(label)) {
                sum += n;
            }

            double ee = E(Arrays.asList(entropy.get(label)));
            splitInfo += ee;
            labelsE += (sum/parentSum) * ee;
        }

        if (splitInfo == 0) splitInfo = 1;

        return (parentE - labelsE) / splitInfo;
    }

    /**
     * Calculates the entropy of given classes
     * @param p
     * @return Entropy
     */
    private static double E(List<Integer> p) {
        double sum = 0;
        for (Integer n: p) {
            sum += n;
        }

        double entropy = 0.0;

        for (Integer n: p) {
            if (n > 0) {
                entropy += (n/sum) * Math.log(n/sum) / Math.log(2);
            }
        }

        return -entropy;
    }

    /**
     * Given a column in a dataset return the possible values that can be in that given column in the dataset
     * @param feature target column
     * @param dataset search dataset
     * @return list of possible values that can be in that column/feature
     */
    private static List<String> getPossibleValues(String feature, HashMap<String, List<String>> dataset) {
        List<String> values = new ArrayList<>();

        for (String label: dataset.get(feature)) {
            if (!values.contains(label)) {
                values.add(label);
            }
        }

        return values;
    }

    public static HashMap<String, HashMap<String, List<String>>> splitTestTrain(HashMap<String, List<String>> data, double testRatio, boolean shuffle) {
        HashMap<String, List<String>> test = new HashMap<>();
        HashMap<String, List<String>> train = new HashMap<>();

        for (String column : data.keySet()) {
            List<String> values = new ArrayList<>(data.get(column));
            if (shuffle) Collections.shuffle(values);

            int splitIndex = (int) (values.size() * testRatio);

            test.put(column, values.subList(0, splitIndex));
            train.put(column, values.subList(splitIndex, values.size()));
        }

        HashMap<String, HashMap<String, List<String>>> result = new HashMap<>();
        result.put("test", test);
        result.put("train", train);

        return result;
    }

    public static double calculateAccuracy(int[][] confusionMatrix) {
        int correctPredictions = 0;
        int totalPredictions = 0;
        for (int i = 0; i < confusionMatrix.length; i++) {
            correctPredictions += confusionMatrix[i][i];
            totalPredictions += sum(confusionMatrix[i]);
        }
        return (double) correctPredictions / totalPredictions;
    }

    public static double[] calculatePrecision(int[][] confusionMatrix) {
        int numClasses = confusionMatrix.length;
        double[] precision = new double[numClasses];
        for (int i = 0; i < numClasses; i++) {
            int TP = confusionMatrix[i][i];
            int FP = 0;
            for (int j = 0; j < numClasses; j++) {
                if (i != j) {
                    FP += confusionMatrix[j][i];
                }
            }
            precision[i] = (TP + FP == 0) ? 0 : (double) TP / (TP + FP);
        }
        return precision;
    }

    public static double[] calculateRecall(int[][] confusionMatrix) {
        int numClasses = confusionMatrix.length;
        double[] recall = new double[numClasses];
        for (int i = 0; i < numClasses; i++) {
            int TP = confusionMatrix[i][i];
            int FN = 0;
            for (int j = 0; j < numClasses; j++) {
                if (i != j) {
                    FN += confusionMatrix[i][j];
                }
            }
            recall[i] = (TP + FN == 0) ? 0 : (double) TP / (TP + FN);
        }
        return recall;
    }

    public static double[] calculateF1Score(double[] precision, double[] recall) {
        int numClasses = precision.length;
        double[] f1Scores = new double[numClasses];
        for (int i = 0; i < numClasses; i++) {
            if (precision[i] + recall[i] == 0) {
                f1Scores[i] = 0;
            } else {
                f1Scores[i] = 2 * (precision[i] * recall[i]) / (precision[i] + recall[i]);
            }
        }
        return f1Scores;
    }

    private static int sum(int[] array) {
        int sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }
}
