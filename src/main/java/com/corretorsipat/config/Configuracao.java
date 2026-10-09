package com.corretorsipat.config;

/**
 * Armazena somente preferências locais de apresentação e da última pasta usada.
 */
public class Configuracao {
    private String ultimaPasta = "";
    private int x = -1;
    private int y = -1;
    private int largura = 1050;
    private int altura = 760;

    public String getUltimaPasta() {
        return ultimaPasta;
    }

    public void setUltimaPasta(String ultimaPasta) {
        this.ultimaPasta = ultimaPasta == null ? "" : ultimaPasta;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getLargura() {
        return largura;
    }

    public void setLargura(int largura) {
        this.largura = largura;
    }

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }
}
