package org.model.arac;

import org.model.Durak;

public class Tramvay extends Arac {
    private double hiz;  // Otobüs için varsayılan hız

    public Tramvay() {
        super("tram");
        this.hiz = 0.0;  // Tramvay için varsayılan hız
    }

    @Override
    public double hesaplaMesafeUcreti(double mesafe) {
        // Tramvay için, JSON verilerini doğrudan SonrakiDurak içinde kullanıyoruz
        // Bu metot normal segmentler için çağrılmamalıdır
        return 0.0;
    }

    @Override
    public double hesaplaSure(double mesafe) {
        // Tramvay için, JSON verilerini doğrudan SonrakiDurak içinde kullanıyoruz
        // Bu, JSON'da bulunamazsa yedek bir hesaplama sağlar
        return 0.0;
    }

    @Override
    public double getHiz() {
        return hiz;
    }

    @Override
    public void setHiz(double hiz) {
        this.hiz =hiz;
    }

    @Override
    public boolean duraklaUyumluMu(Durak durak) {
        // Tramvay, "tram" tipi durakları desteklesin
        return durak.getType().equalsIgnoreCase("tram");
    }

    @Override
    public AracKategori getKategori() {
        return AracKategori.PUBLIC_TRANSPORT;
    }

    @Override
    public String getDisplayName() {
        return "🚋 Tramvay";
    }
}