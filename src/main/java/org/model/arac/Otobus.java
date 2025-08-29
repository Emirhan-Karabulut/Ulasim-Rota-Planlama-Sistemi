package org.model.arac;

import org.model.Durak;

public class Otobus extends Arac {
    private double hiz;  // Hız, km/s cinsinden

    public Otobus() {
        super("bus");
        this.hiz = 0.0;  // Otobüs için varsayılan hız
    }

    @Override
    public double hesaplaMesafeUcreti(double mesafe) {
        // Otobüs için, JSON verilerini doğrudan SonrakiDurak içinde kullanıyoruz
        // Bu metot normal segmentler için çağrılmamalıdır
        return 0.0;
    }

    @Override
    public double hesaplaSure(double mesafe) {
        // Otobüs için, JSON verilerini doğrudan SonrakiDurak içinde kullanıyoruz
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
        // Otobüs, "bus" tipi durakları desteklesin
        return durak.getType().equalsIgnoreCase("bus");
    }

    @Override
    public AracKategori getKategori() {
        return AracKategori.PUBLIC_TRANSPORT;
    }

    @Override
    public String getDisplayName() {
        return "🚌 Otobüs";
    }

}
