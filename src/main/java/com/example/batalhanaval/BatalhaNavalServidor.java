package com.example.batalhanaval;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.example.batalhanaval.Tabuleiro.Celula;

public class BatalhaNavalServidor extends Application {

    private boolean terminouColocarNavios = false;
    private Tabuleiro tabuleiroOponente, tabuleiroJogador;

    private int naviosParaColocar = 5;

    private boolean oponentePronto = false;
    private boolean jogadorPronto = false;
    private boolean turnoInimigo = false;
    private String mensagem = "Coloque os seus navios no tabuleiro";
    private ServerNetwork servidor;
    private Dados dados;

    private String tituloAlertaVitoria;

    private String textoAlertaVitoria;

    private Label texto;

    // modificar essa classe para funcionar multiplayer
    protected Parent criarConteudo(Stage palco) {
        BorderPane raiz = new BorderPane();
        raiz.setPrefSize(972, 648);


        // crio um tabuleiro do oponente que ao receber o click do mouse, fará algo.
        tabuleiroOponente = new Tabuleiro(true, event -> {

            // verifica se a partida começou, de forma que só é possível clicar no
            // tabuleiro inimigo depois que a partida tiver começado.
            if (!terminouColocarNavios) {
                return;
            }

            if (turnoInimigo) {
                return;
            }

            // verifico se a celula já foi atirada, se for retorno, fazendo com que nada aconteça.
            Celula celula = (Celula) event.getSource();
            if (celula.tomouTiro)
                return;

            // atiro na celula e passo o turno pro inimigo.
            celula.atirarNaCelula();
            enviarTiro(celula.x,celula.y);

            // passo o turno para o inimigo
            turnoInimigo = true;
            texto.setText("É o turno do oponente.");

            if (tabuleiroOponente.quantidadeNavios == 0) {
                tituloAlertaVitoria = "Vitoria";
                textoAlertaVitoria = "Parabéns, você ganhou!";
                enviarDerrota();
                servidor.close();
                Platform.runLater(() -> {
                    jogadorGanhou();
                });
            }

        });

        tabuleiroJogador = new Tabuleiro(false, event -> {

            // verifica se a partida começou, se ela começou retorna
            // assim não é possível modificar o tabuleiro após a partida começar
            if (terminouColocarNavios)
                return;

            // função para colocar os navios no tabuleiro.
            Celula celula = (Celula) event.getSource();
            if (tabuleiroJogador.colocarNavio(new Navio(naviosParaColocar
                    , event.getButton() == MouseButton.PRIMARY), celula.x, celula.y)) {

                enviarNavio(naviosParaColocar,event.getButton() == MouseButton.PRIMARY, celula.x, celula.y );

                if (--naviosParaColocar == 0) {
                    jogadorPronto = true;
                    inicarJogo();
                }
            }
        });



        // configurações de onde o tabuleiro será colocado.
        tabuleiroOponente.setId("tabuleiro");
        tabuleiroJogador.setId("tabuleiro");
        HBox hboxTabuleiro = new HBox(100, tabuleiroOponente, tabuleiroJogador);
        hboxTabuleiro.setAlignment(Pos.CENTER);
        hboxTabuleiro.setPadding(new Insets(-50,0,0,0));


        // configurações do texto
        texto = new Label(mensagem);
        texto.setPrefWidth(972);
        texto.setMaxWidth(972);
        texto.setAlignment(Pos.CENTER);
        texto.setPadding(new Insets(50,0,20,0));
        texto.setEffect( new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.3), 10, 0.6, 0.0, 0.0));

        Label textoOponente = new Label("Tabuleiro do Oponente");
        textoOponente.setId("textoOponente");
        textoOponente.setPrefWidth(486);
        textoOponente.setAlignment(Pos.CENTER);

        Label textoJogador = new Label("Seu tabuleiro");
        textoJogador.setId("textoJogador");
        textoJogador.setPrefWidth(486);
        textoJogador.setAlignment(Pos.CENTER);

        // configurações dos botões e colocando tudo em um Hbox e Vbox
        HBox hBoxTextoTabuleiro = new HBox(textoOponente,textoJogador);
        hBoxTextoTabuleiro.setPadding(new Insets(30,0,0,0));

        Button voltarMenu = new Button("voltar para o menu");
        Button tutorial = new Button("tutorial");

        HBox hBoxBotao = new HBox(10,tutorial,voltarMenu);
        hBoxBotao.setAlignment(Pos.CENTER);
        hBoxTextoTabuleiro.setPadding(new Insets(-50,0,0,0));

        VBox vBox = new VBox(50,texto,hBoxTextoTabuleiro,hboxTabuleiro,hBoxBotao);
        raiz.setCenter(vBox);

        //ActionEvents para quando o usuário clicar nos botões de baixo:
        tutorial.setOnAction(actionEvent -> {
            comoJogar();
        });

        voltarMenu.setOnAction(actionEvent -> {

            try {
                trocaParaMenu(actionEvent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });

        return raiz;
    }

    // função que envia as coordenadas do navio do jogador para o oponente.
    public synchronized void enviarNavio(int naviosParaColocarOponente,boolean isVertical, int x, int y) {

        Dados dado = new Dados(3, naviosParaColocarOponente, isVertical, x, y);
        servidor.enviarDados(dado);
        System.out.println("navio enviado com+ sucesso!");
    }

    public synchronized void receberNavioOponente(int naviosParaColocarOponente,boolean isVertical, int x, int y) {
        System.out.println("navio recebido com sucesso!");

        int finalNaviosParaColocarOponente = naviosParaColocarOponente;

        Platform.runLater(() -> {
            Celula celula = tabuleiroOponente.getCelula(x,y);
                tabuleiroOponente.colocarNavio(new Navio(finalNaviosParaColocarOponente, isVertical), celula.x, celula.y);
                });

            if (--naviosParaColocarOponente == 0) {
                oponentePronto = true;
                inicarJogo();
            System.out.println("Navio colocado com sucesso.");
        }

    }

    public synchronized void enviarTiro(int x, int y) {
        Dados dado = new Dados(4, x, y);
        servidor.enviarDados(dado);
        System.out.println("tiro enviado com sucesso!");
    }

    public synchronized void receberTiroDoOponente(int x, int y) {
        System.out.println("Tiro recebido com sucesso");

        Platform.runLater(() -> {
            Celula celula = tabuleiroJogador.getCelula(x,y);
            celula.atirarNaCelula();
            texto.setText("É o seu turno");
        });
        turnoInimigo = false;
    }

    public synchronized void enviarDerrota() {
        Dados dado = new Dados(5,"você perdeu!");
        servidor.enviarDados(dado);
        System.out.println("Derrota enviada com sucesso!");
    }

    public synchronized void receberDerrota() {
        tituloAlertaVitoria = "Derrota";
        textoAlertaVitoria = "Você perdeu!";
        Platform.runLater(() -> {
            jogadorGanhou();
        });
    }

    public synchronized void receberServidor(ServerNetwork servidor) {
        this.servidor = servidor;
    }

    public void comoJogar() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Tutorial");
        alerta.setHeaderText("Como jogar batalha naval:");
        alerta.setContentText(" Primeiramente começe a partida colocando seus navios,"
                + " clique no seu tabuleiro para coloca-los. Botão esquerdo coloca o navio na vertical"
                +" e o botão direito coloca o navio na horizontal."
                +"Você e o seu oponente ambos começam com 5 navios e perde quem tiver todos os navios afundados primeiro."
                +"\n Quando a partida começar, você deverá escolher um quadrado do tabuleiro inimigo para atirar."
                +"Se houver um navio naquele o, ele ficará vermelho, caso não exista nenhum navio, ele ficará preto."
                +"Após o seu movimento, será o turno do oponente e você deverá esperar ele realizar o turno dele para proseguir.");


        // customização da janela de saída
        DialogPane dialogPane = alerta.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("estilo.css").toExternalForm());
        dialogPane.getStyleClass().add("myDialog");

        // adicionar ícone na janela de saída
        Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
        Image icone = new Image("icone.png");
        stage.getIcons().add(icone);

        // caso o usuário aperte ok no alerta.
        if(alerta.showAndWait().get() == ButtonType.OK){

        }
    }

    public void trocaParaMenu(ActionEvent evento) throws IOException, InterruptedException {
        servidor.close();
        Parent root = FXMLLoader.load(Main.class.getResource("view.fxml"));
        Stage palco = (Stage)((Node)evento.getSource()).getScene().getWindow();
        Scene cena = new Scene(root);
        palco.setScene(cena);
        palco.show();
    }

    private void inicarJogo() {

        if (jogadorPronto && oponentePronto){
            System.out.println("O jogo começou!");
            mensagem = "A partida começou!";

            texto.setText(mensagem);
            terminouColocarNavios = true;
        } else if(jogadorPronto) {
            mensagem = "Espere seu oponente terminar de colar os navios!";
            turnoInimigo = true;
            texto.setText(mensagem);
        }

    }

    public void jogadorGanhou() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(tituloAlertaVitoria);
        alerta.setHeaderText(textoAlertaVitoria);

        // customização da janela de saída
        DialogPane dialogPane = alerta.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("estilo.css").toExternalForm());
        dialogPane.getStyleClass().add("myDialog");

        // adicionar ícone na janela de saída
        Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
        Image icone = new Image("icone.png");
        stage.getIcons().add(icone);

        // caso o usuário aperte ok no alerta, o programa será fechado junto com o servidor/cliente.
        if(alerta.showAndWait().get() == ButtonType.OK || alerta.showAndWait().get() == ButtonType.CANCEL){

            Stage palco = (Stage)texto.getScene().getWindow();
            System.out.println("Programa terminado com sucesso.");
            palco.close();
        }
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

        // caso o usuário aperte ok no alerta, o programa será fechado junto com o servidor/cliente.
        if(alerta.showAndWait().get() == ButtonType.OK){

            servidor.close();
            System.out.println("Programa terminado com sucesso.");
            palco.close();
        }

    }
    @Override
    public void start(Stage palco) throws Exception {
        Scene scene = new Scene(criarConteudo(palco));
        palco.setTitle("Battleship");
        palco.setScene(scene);
        palco.setResizable(false);

        palco.show();

        // função pra fechar o programa e a thread
        palco.setOnCloseRequest(event -> {
            event.consume();
            sair(palco);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
