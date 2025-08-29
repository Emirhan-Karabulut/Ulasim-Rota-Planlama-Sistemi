package org.model.yolcu;


public interface Yolcu {

    double getIndirimOrani();
    double getYurumeHizi();

    default double indirimiUygula(double ucret) {
        return ucret * (1 - getIndirimOrani());
    }
}

