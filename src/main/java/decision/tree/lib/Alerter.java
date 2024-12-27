package decision.tree.lib;

import javafx.scene.control.Alert;

// Manages the alert system
public class Alerter {
    private static Alert create(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        if (title != null) alert.setHeaderText(title);
        if (content != null) alert.setContentText(content);

        return alert;
    }

    public static Alert info(String content) {
        return info(null, content);
    }

    public static Alert info(String title, String content) {
        return create(Alert.AlertType.INFORMATION, title, content);
    }

    public static Alert warn(String content) {
        return warn(null, content);
    }

    public static Alert warn(String title, String content) {
        return create(Alert.AlertType.WARNING, title, content);
    }

    public static Alert error(String content) {
        return error(null, content);
    }

    public static Alert error(String title, String content) {
        return create(Alert.AlertType.ERROR, title, content);
    }

    public static Alert confirm(String content) {
        return confirm(null, content);
    }

    public static Alert confirm(String title, String content) {
        return create(Alert.AlertType.CONFIRMATION, title, content);
    }
}
