package com.example.batalhanaval;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


import java.io.File;


public class Menu {

    public void tocarMusica(File arquivo) {
        try {
            AudioInputStream audioinput = AudioSystem.getAudioInputStream(arquivo);
            Clip clip = AudioSystem.getClip();
            clip.open(audioinput);
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e){
            System.out.println("Não foi possível tocar a musica");
        }
    }
    public void exit(Stage palco) {
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
            System.out.println("Programa terminado com sucesso.");
            palco.close();
        }

    }

}
