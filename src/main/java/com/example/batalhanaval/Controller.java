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

public class Controller {
    @FXML
    private Button exitButton;
    @FXML
    private AnchorPane painelCena;

    Stage palco;
    private Scene cena;
    private Parent root;
    public void trocaParaEntrarJogo(ActionEvent evento) throws IOException {
        root = FXMLLoader.load(Main.class.getResource("EntrarJogo.fxml"));
        palco = (Stage)((Node)evento.getSource()).getScene().getWindow();
        cena = new Scene(root);
        palco.setScene(cena);
        palco.show();
    }

    public void trocaParaCriarSala(ActionEvent evento) throws IOException {
        root = FXMLLoader.load(Main.class.getResource("CriarSala.fxml"));
        palco = (Stage)((Node)evento.getSource()).getScene().getWindow();
        cena = new Scene(root);
        palco.setScene(cena);
        palco.show();
    }

    public void exit(ActionEvent evento) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Sair do programa");
        alerta.setHeaderText("Tem certeza que deseja sair?");

        // customização da janela de saída
        DialogPane dialogPane = alerta.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("estilo.css").toExternalForm());
        dialogPane.getStyleClass().add("myDialog");

        // adicionar ícone na janela de saída
        Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
        Image icone = new Image("icone.png");
        stage.getIcons().add(icone);

        if(alerta.showAndWait().get() == ButtonType.OK){
            palco = (Stage) painelCena.getScene().getWindow();
            System.out.println("Programa terminado com sucesso.");
            palco.close();
        }

    }

}