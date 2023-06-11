package com.example.batalhanaval;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {


    public static void main(String[] args) {

        launch();
    }

    @Override
    public void start(Stage palco) throws Exception {
        String titulo = "Batalha Naval 2000";

         Parent raiz = FXMLLoader.load(Main.class.getResource("view.fxml"));
         Scene cena = new Scene(raiz,Color.rgb(84,121,128));
         //raiz.getNamespace().put("titulo", titulo);

        Menu menu = new Menu();

        File musicpath = new File(this.getClass().getResource("musica1.wav").toURI());
        menu.tocarMusica(musicpath);

        String css = this.getClass().getResource("estilo.css").toExternalForm();
        cena.getStylesheets().add(css);

        // configurações da cena
        Image icone = new Image("icone.png");
        palco.getIcons().add(icone);
        palco.setTitle(titulo);
        palco.setResizable(false);

        palco.setScene(cena);
        palco.show();
        
        // função pra fechar o programa
        palco.setOnCloseRequest(event -> {
            event.consume();
            menu.exit(palco);
        });
    }



}