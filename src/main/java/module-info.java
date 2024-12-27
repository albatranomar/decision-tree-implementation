module decision.tree {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens decision.tree to javafx.fxml;
    exports decision.tree;
    exports decision.tree.lib;
    exports decision.tree.controllers;
    opens decision.tree.controllers to javafx.fxml;
}