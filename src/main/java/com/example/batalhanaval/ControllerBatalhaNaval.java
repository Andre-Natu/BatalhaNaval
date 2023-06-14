package com.example.batalhanaval;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerBatalhaNaval {
    Stage palco;
    private Stage palcoOponente;
    private Scene cena;
    private Parent root;

    private Menu menu;
    //protected boolean isServidor;
    private ServerNetwork servidor;
    private ClientNetwork cliente;

    @FXML
    private Label texto;

    @FXML
    private Button iniciar;

    public void atualizar(String nomeOponente) {
        Platform.runLater(() -> {
            texto.setText("O seu " + nomeOponente);
        });
    }

    public void receberServidor(ServerNetwork servidor) {
        this.servidor = servidor;
    }
    public void receberCliente(ClientNetwork cliente) {
        this.cliente = cliente;
    }

    public void setPalcoOponente(Stage palcoOponente) {
        this.palcoOponente = palcoOponente;
    }

    public void mostrarBotao(){
        Platform.runLater(() -> {
            iniciar.setDisable(false);
        });
    }

    public void trocaParaColocarNavios(ActionEvent evento) throws IOException, InterruptedException {
        iniciarTabuleiro();
        Dados dados = new Dados (2,"iniciarTabuleiro");
        servidor.enviarDados(dados);
    }

    public void iniciarTabuleiro() throws IOException, InterruptedException {

        palco = (Stage)texto.getScene().getWindow();

        if(servidor != null) {
            BatalhaNavalServidor batalhaNavalServidor = new BatalhaNavalServidor();
            cena = new Scene(batalhaNavalServidor.criarConteudo(palco));

            servidor.setBatalhaNavalServidor(batalhaNavalServidor);
            batalhaNavalServidor.receberServidor(servidor);

            System.out.println("é servidor");
        } else if(cliente != null) {
            BatalhaNavalCliente batalhaNavalCliente = new BatalhaNavalCliente();
            cena = new Scene(batalhaNavalCliente.criarConteudo());

            cliente.setBatalhaNavalCliente(batalhaNavalCliente);
            batalhaNavalCliente.receberCliente(cliente);

            System.out.println("é cliente");
        } else {
            System.out.println("Não existe nenhum servidor nem cliente.");
        }

        String css = this.getClass().getResource("estilo.css").toExternalForm();
        cena.getStylesheets().add(css);

        // obriga o programa a executar essa linha de código com a thread do JavaFx
        Platform.runLater(() -> {
            palco.setScene(cena);
            palco.show();
        });

        palco.setOnCloseRequest(event -> {
            event.consume();
            sair(palco);
        });
    }
    public void trocaParaMenu(ActionEvent evento) throws IOException, InterruptedException {
        if(servidor != null) {
            servidor.close();

        }else if(cliente != null) {
            cliente.close();
        }
        root = FXMLLoader.load(Main.class.getResource("view.fxml"));
        palco = (Stage)((Node)evento.getSource()).getScene().getWindow();
        cena = new Scene(root);
        palco.setScene(cena);
        palco.show();
    }

    public void sair(Stage palco) {
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
            if(servidor != null) {
                servidor.close();

            }else if(cliente != null) {
                cliente.close();
            }
            System.out.println("Programa terminado com sucesso.");
            palco.close();
        }

    }
}
