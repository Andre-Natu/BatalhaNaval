package com.example.batalhanaval;

import java.io.Serializable;

public class Dados implements Serializable{
    public int naviosParaColocarOponente;
    public boolean isVertical;
    public int x;
    public int y;
    public int operacao;
    public String mensagem;

    public Dados(int operacao ,int naviosParaColocarOponente, boolean isVertical, int x, int y) {
        this.operacao = operacao;
        this.naviosParaColocarOponente = naviosParaColocarOponente;
        this.isVertical = isVertical;
        this.x = x;
        this.y = y;
    }

    public Dados (int operacao, int x, int y){
        this.operacao = operacao;
        this.x = x;
        this.y = y;
    }

    public Dados(int operacao, String mensagem) {
        this.operacao = operacao;
        this.mensagem = mensagem;
    }
}
