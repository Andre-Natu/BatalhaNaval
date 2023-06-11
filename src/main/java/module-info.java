module com.example.batalhanaval {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;


    opens com.example.batalhanaval to javafx.fxml;
    exports com.example.batalhanaval;
}