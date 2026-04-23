module me.abollo.javachat {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens me.abollo.javachat to javafx.fxml;
    exports me.abollo.javachat;
}