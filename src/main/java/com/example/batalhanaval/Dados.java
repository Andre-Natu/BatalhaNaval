package com.example.batalhanaval;

import java.io.Serializable;

public class Dados implements Serializable{
    public int naviosParaColocarOponente;
    public boolean isVertical;
    public int x;
    public int y;

    public Dados(int naviosParaColocarOponente, boolean isVertical, int x, int y) {
        this.naviosParaColocarOponente = naviosParaColocarOponente;
        this.isVertical = isVertical;
        this.x = x;
        this.y = y;
    }
}
