package com.example.batalhanaval;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tabuleiro extends Parent {
    private VBox linhas = new VBox();
    private boolean oponente = false;
    public int quantidadeNavios = 5;

    // construtor do tabuleiro, cria um tabuleiro com 10x10 Celulas
    public Tabuleiro(boolean oponente, EventHandler<? super MouseEvent> handler) {
        this.oponente = oponente;
        for (int y = 0; y < 10; y++) {
            HBox linha = new HBox();
            for (int x = 0; x < 10; x++) {
                Celula c = new Celula(x, y, this);
                c.setOnMouseClicked(handler);
                linha.getChildren().add(c);
            }

            linhas.getChildren().add(linha);
        }

        getChildren().add(linhas);
    }

    public boolean colocarNavio(Navio navio, int x, int y) {

        if (podeColocarNavio(navio, x, y)) {
            int tamanhoNavio = navio.tamanhoNavio;
            boolean isVertical = navio.isVertical;

            if (isVertical) {
                for (int i = y; i < y + tamanhoNavio; i++) {

                    // pego a celula da posição x,i e armazeno o navio na celula
                    Celula celula = getCelula(x, i);
                    celula.navio = navio;

                    // se a celula não for do inimigo, irei marcar ela no tabuleiro
                    // para saber que existe um navio alí.
                    if (!oponente) {
                        celula.setFill(Color.WHITE);
                        celula.setStroke(Color.GREEN);
                    }
                }
            }
            else {
                // caso o navio seja colocado na horizontal
                for (int i = x; i < x + tamanhoNavio; i++) {

                    Celula cell = getCelula(i, y);
                    cell.navio = navio;

                    if (!oponente) {
                        cell.setFill(Color.WHITE);
                        cell.setStroke(Color.GREEN);
                    }
                }
            }
            // significa que o navio pode ser colocado
            return true;
        }
        // significa que o navio não pode ser colocado
        return false;
    }

    public Celula getCelula(int x, int y) {
        // primeiro eu pego o valor da minha coluna,  através do getCHilden.get(y)
        // depois eu transformo esse valor em HBox e com isso consigo pegar o valor
        // do x, que seria a celula que eu quero. E por último eu transformo no tipo celula
        return (Celula)((HBox)linhas.getChildren().get(y)).getChildren().get(x);
    }

    private Celula[] getVizinhos(int x, int y) {
        // cria um array com os pontos ao lado do ponto em questão
        Point2D[] points = new Point2D[] {
                new Point2D(x - 1, y),
                new Point2D(x + 1, y),
                new Point2D(x, y - 1),
                new Point2D(x, y + 1)
        };

        List<Celula> vizinhos = new ArrayList<Celula>();

        // verifica cada ponto do array para ver se são válidos e os adicona a lista
        for (Point2D p : points) {
            if (verificarPontoValido(p)) {
                vizinhos.add(getCelula((int)p.getX(), (int)p.getY()));
            }
        }

        return vizinhos.toArray(new Celula[0]);
    }

    private boolean podeColocarNavio(Navio navio, int x, int y) {
        int tamanhoNavio = navio.tamanhoNavio;


        if (navio.isVertical) {
            // caso o navio seja colocado na vertical
            for (int i = y; i < y + tamanhoNavio; i++) {

                // se nao for valido, retorna falso
                if (!verificarPontoValido(x, i)){
                    return false;
                }
                // pego a celula correspondente a este ponto.
                Celula celula = getCelula(x, i);
                if (celula.navio != null)
                    return false;

                // verifica se já não existe um navio em uma celula vizinha
                for (Celula vizinha : getVizinhos(x, i)) {
                    if (!verificarPontoValido(x, i))
                        return false;

                    if (vizinha.navio != null)
                        return false;
                }
            }
        }
        else {
            // caso o navio seja colocado na horizontal
            for (int i = x; i < x + tamanhoNavio; i++) {

                // se nao for valido, retorna falso
                if (!verificarPontoValido(i, y)) {
                    return false;
                }

                // verifica se já não existe um navio na celula
                Celula celula = getCelula(i, y);
                if (celula.navio != null)
                    return false;

                // verifiaca se já não existe um navio em uma celula vizinha
                for (Celula vizinha : getVizinhos(i, y)) {
                    if (!verificarPontoValido(i, y))
                        return false;

                    if (vizinha.navio != null)
                        return false;
                }
            }
        }

        return true;
    }

    private boolean verificarPontoValido(Point2D point) {return verificarPontoValido(point.getX(), point.getY());
    }

    private boolean verificarPontoValido(double x, double y) {
        // verifica se o ponto não está fora do tabuleiro., ex na posição 11.
        return (x >= 0 && x < 10) && (y >= 0 && y < 10);
    }

    public class Celula extends Rectangle {
        public int x, y;
        public Navio navio = null;
        public boolean tomouTiro = false;
        private Tabuleiro tabuleiro;

        // construtor da celula
        public Celula(int x, int y, Tabuleiro tabuleiro) {
            // tamanho das celulas
            super(35, 35);
            this.x = x;
            this.y = y;
            this.tabuleiro = tabuleiro;
            setFill(Color.LIGHTGRAY);
            setStroke(Color.BLACK);
        }

        public boolean atirarNaCelula() {
            // quando o jogador clica numa celula, ela é pintada de preto
            tomouTiro = true;
            setFill(Color.BLACK);

            // se tiver um navio dentro da celula, ela é pintada de vermelho
            // a vida no navio é diminuida e retorna verdadeiro.
            if (navio != null) {
                navio.acerto();
                setFill(Color.RED);
                if (!navio.estaVivo()) {
                    tabuleiro.quantidadeNavios--;
                }
                return true;
            }

            return false;
        }
    }
}

