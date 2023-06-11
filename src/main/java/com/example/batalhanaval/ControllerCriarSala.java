package com.example.batalhanaval;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerCriarSala {
    @FXML
    private Button exitButton;
    @FXML
    private AnchorPane painelCena;

    Stage palco;
    private Scene cena;
    private Parent root;

    public void trocaParaMenu(ActionEvent evento) throws IOException {
        root = FXMLLoader.load(Main.class.getResource("view.fxml"));
        palco = (Stage)((Node)evento.getSource()).getScene().getWindow();
        cena = new Scene(root);
        palco.setScene(cena);
        palco.show();
    }

}