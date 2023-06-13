package com.example.batalhanaval;

import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import com.example.batalhanaval.Tabuleiro.Celula;

public class BatalhaNavalCliente extends Application {
    private boolean terminouColocarNavios = false;
    private Tabuleiro tabuleiroOponente, tabuleiroJogador;

    private int naviosParaColocar = 5;

    private boolean turnoInimigo = false;
    private String mensagem = "Coloque os seus navios no tabuleiro";
    private ClientNetwork cliente;
    private Random random = new Random();

    protected Parent criarConteudo() {
        BorderPane raiz = new BorderPane();
        raiz.setPrefSize(972, 648);

        raiz.setTop(new Label(mensagem));

        // crio um tabuleiro do oponente que ao receber o click do mouse, fará algo.
        tabuleiroOponente = new Tabuleiro(true, event -> {

            // verifica se a partida começou, de forma que só é possível clicar no
            // tabuleiro inimigo depois que a partida tiver começado.
            if (!terminouColocarNavios) {
                return;
            }

            // verifico se a celula já foi atirada, se for retorno, fazendo com que nada aconteça.
            Celula celula = (Celula) event.getSource();
            if (celula.tomouTiro)
                return;

            // atiro na celula e passo o turno pro inimigo.
            celula.atirarNaCelula();
            turnoInimigo = true;

            if (tabuleiroOponente.quantidadeNavios == 0) {
                System.out.println("Você ganhou");
                System.exit(0);
            }

            if (turnoInimigo)
                movimentoDoOponente();
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
                    inicarJogo();
                }
            }
        });

        // configurações de onde o tabuleiro será colocado.
        HBox hbox = new HBox(100, tabuleiroOponente, tabuleiroJogador);
        hbox.setAlignment(Pos.CENTER);

        raiz.setCenter(hbox);

        return raiz;
    }

    // atualmente é utilizada para o computador realizar sua jogada.
    private void movimentoDoOponente() {
        while (turnoInimigo) {

            // o computador escolhe uma celula aleatória
            int x = random.nextInt(10);
            int y = random.nextInt(10);

            // pega a celula escolhida e verifica se ela já foi atirada
            Celula celula = tabuleiroJogador.getCelula(x, y);
            if (celula.tomouTiro)
                continue;

            celula.atirarNaCelula();

            // verifica os tabuleiros do inimigo
            if (tabuleiroJogador.quantidadeNavios == 0) {
                System.out.println("Você Perdeu!");
                System.exit(0);
            }

            turnoInimigo = false;
        }
    }

    // função que envia as coordenadas do navio do jogador para o oponente.
    public void enviarNavio(int naviosParaColocarOponente,boolean isVertical, int x, int y) {

        Dados dado = new Dados(naviosParaColocarOponente, isVertical, x, y);
        cliente.enviarNavio(dado);
    }

    // função que recebe as coordenadas do navio do oponente e coloca no tabuleiro dele.
    public void receberNavioOponente(int naviosParaColocarOponente,boolean isVertical, int x, int y) {
        Celula celula = tabuleiroOponente.getCelula(x,y);
        if (tabuleiroOponente.colocarNavio(new Navio(naviosParaColocarOponente, isVertical), celula.x, celula.y)) {
            if (--naviosParaColocarOponente == 0) {
                inicarJogo();
            }
            System.out.println("Navio colocado com sucesso.");
        }
    }

    public void receberCliente(ClientNetwork cliente) {
        this.cliente = cliente;
    }

    private void inicarJogo() {
        int quantidadeNavios = 5; // é a quantidade de navios que falta para o tabuleiro inimigo.

        // Computador coloca os navios no seu tabuleiro de forma aleatoria
        while (quantidadeNavios > 0) {
            int x = random.nextInt(10);
            int y = random.nextInt(10);

            // verifica se a posição aleatoria que o computador criou é válida.
            if (tabuleiroOponente.colocarNavio(new Navio(quantidadeNavios, Math.random() < 0.5), x, y)) {

                quantidadeNavios--;
            }
        }

        System.out.println("O jogo começou!");
        mensagem = "A partida começou!";
        terminouColocarNavios = true;
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

            cliente.close();
            System.out.println("Programa terminado com sucesso.");
            palco.close();
        }

    }

    @Override
    public void start(Stage palco) throws Exception {
        Scene scene = new Scene(criarConteudo());
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
