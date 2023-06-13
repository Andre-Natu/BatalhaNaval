package com.example.batalhanaval;

import javafx.scene.Parent;

public class Navio extends Parent {
    public int tamanhoNavio;
    public boolean isVertical = true; // verifica se o navio é horizontal ou vertical

    private int vida;

    // construtor do navio.
    public Navio(int tamanhoNavio, boolean vertical) {
        this.tamanhoNavio = tamanhoNavio;
        this.isVertical = vertical;
        vida = tamanhoNavio;
    }

    // sinaliza que o navio tomou um tiro e diminui uma vida dele.
    public void acerto() {
        vida--;
    }

    // verifica se o navio continua vivo
    public boolean estaVivo() {
        return vida > 0;
    }
}