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

public class ControllerEntrarJogo {
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
    String nomeNickname,valorIp;
    ClientNetwork client = new ClientNetwork();

    public void entrarSala(ActionEvent evento) {
        try {
            valorPorta = Integer.parseInt(porta.getText());
            valorIp = ip.getText();
            nomeNickname = nickname.getText();

            if (!(valorPorta >= 1 && valorPorta <= 65535)){
                mensagem.setText("A porta digitada não é válida");
            } else {
                if (client.iniciarNetwork(valorIp, valorPorta)) {
                    FXMLLoader load = new FXMLLoader(Main.class.getResource("BatalhaNaval.fxml"));
                    root = load.load();
                    ControllerBatalhaNaval controllerBatalhaNaval = load.getController();
                    client.setControllerBatalhaNaval(controllerBatalhaNaval);

                    // Enviar mensagem para a classe ClientNetwork
                    client.enviarMensagem("oponente é: " + nomeNickname);

                    // Atualizar a label na classe ControllerBatalhaNaval
                    //controllerBatalhaNaval.atualizar(nomeNickname);

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

    public void trocaParaMenu(ActionEvent evento) throws IOException {
        client.close();

        root = FXMLLoader.load(Main.class.getResource("view.fxml"));
        palco = (Stage)((Node)evento.getSource()).getScene().getWindow();
        cena = new Scene(root);
        palco.setScene(cena);
        palco.show();
    }



}