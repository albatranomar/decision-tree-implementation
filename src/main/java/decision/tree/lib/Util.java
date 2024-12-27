package decision.tree.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Util {
    public static HashMap<String, List<String>> readCSV(File file) throws IOException {
        HashMap<String, List<String>> data = new HashMap<>();

        try (Scanner scanner = new Scanner(file)) {
            if (!scanner.hasNextLine()) {
                throw new IOException("CSV file is empty.");
            }

            String headerLine = scanner.nextLine();
            String[] headers = headerLine.split(",");

            for (String header : headers) {
                data.put(header, new ArrayList<>());
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",");

                for (int i = 0; i < values.length; i++) {
                    String header = headers[i];
                    data.get(header).add(values[i].trim());
                }
            }
        }

        return data;
    }
}
