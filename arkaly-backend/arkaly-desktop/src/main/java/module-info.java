module com.arkaly.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    opens com.arkaly.desktop to javafx.fxml;
    opens com.arkaly.desktop.controllers to javafx.fxml;
    opens com.arkaly.desktop.utils to javafx.fxml;

    exports com.arkaly.desktop;
}