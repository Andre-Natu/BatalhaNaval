package com.example.batalhanaval;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.io.IOException;

public class ControllerBatalhaNaval {
    Stage palco;
    private Scene cena;
    private Parent root;

    @FXML
    private Label texto;

    public void atualizar(String nomeOponente) {
        Platform.runLater(() -> {
            texto.setText("O seu " + nomeOponente);
        });
    }

    public void trocaParaMenu(ActionEvent evento) throws IOException, InterruptedException {
        // network.close();
        root = FXMLLoader.load(Main.class.getResource("view.fxml"));
        palco = (Stage)((Node)evento.getSource()).getScene().getWindow();
        cena = new Scene(root);
        palco.setScene(cena);
        palco.show();
    }
}
