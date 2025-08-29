package org.model.odeme;


public interface OdemeYontemi {
    /**
     * Ödeme yöntemine bağlı ek ücret veya indirim oranını döndürür.
     * Ek ücret varsa pozitif, indirim varsa negatif değer döndürür.
     * Örneğin: %5 ek ücret için 0.05, %10 indirim için -0.10.
     */
    double getEkUcretOrani();

    /**
     * Temel ücrete, ödeme yöntemine bağlı ek ücret/indirim uygular.
     */
    default double odemeUcretiniHesapla(double ucret) {
        return ucret * (1 + getEkUcretOrani());
    }
}
