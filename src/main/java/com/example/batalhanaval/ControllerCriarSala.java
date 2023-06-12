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

    @FXML
    private Label mensagem;
    @FXML
    private TextField porta;
    @FXML
    private TextField ip;
    @FXML
    private TextField nickname;
    int valorPorta;
    String nomeNickname, valorIp;
    ServerNetwork server = new ServerNetwork();

    public void criarSala(ActionEvent evento) {
        try {
            valorPorta = Integer.parseInt(porta.getText());
            valorIp = ip.getText();
            nomeNickname = nickname.getText();

            if (!(valorPorta >= 1 && valorPorta <= 65535)) {
                mensagem.setText("A porta digitada não é válida");
            } else {
                //chama o metodo de criar servidor, e se retornar true, troca de cena
                if(server.iniciarNetwork(valorIp, valorPorta)) {
                    //server.enviarMensagem(nomeNickname);
                    FXMLLoader load = new FXMLLoader(Main.class.getResource("BatalhaNaval.fxml"));
                    root = load.load();
                    ControllerBatalhaNaval controllerBatalhaNaval = load.getController();
                    server.setControllerBatalhaNaval(controllerBatalhaNaval, "oponente é: " + nomeNickname);

                    palco = (Stage) ((Node) evento.getSource()).getScene().getWindow();
                    cena = new Scene(root);
                    palco.setScene(cena);
                    palco.show();
                } else {
                    mensagem.setText("Algo deu errado");
                }
            }
        } catch (NumberFormatException e) {
            mensagem.setText("O valor da porta deve ser um número");
        } catch (Exception e) {
            System.out.println(e);
            mensagem.setText("Algo deu errado");
        }
    }

    public void trocaParaMenu(ActionEvent evento) throws IOException, InterruptedException {
        server.close();
        root = FXMLLoader.load(Main.class.getResource("view.fxml"));
        palco = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        cena = new Scene(root);
        palco.setScene(cena);
        palco.show();
    }

}