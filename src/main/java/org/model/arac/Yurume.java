package org.model.arac;

import org.model.Durak;
import org.yardimci.RotaYardimcisi;

public class Yurume extends Arac {
    // Ortalama yürüme hızı km/s cinsinden; örneğin 5 km/s
    private double yurumeHizi = 0.0;

    public Yurume() {
        super("yurume");
    }

    @Override
    public double hesaplaMesafeUcreti(double mesafe) {
        // Fallback hesaplama (API kullanılamadığında veya sadece mesafe bilgisi varsa)
        return 0.0;
    }

    @Override
    protected double hesaplaApiUcreti(double apiMesafeKm) {
        // API'den gelen mesafe bilgisini kullanarak özel hesaplama
        return 0.0;
    }

    @Override
    public double hesaplaSure(double mesafe) {
        // Fallback süre hesaplaması (API kullanılamadığında veya sadece mesafe bilgisi varsa)
        return (mesafe / yurumeHizi) * 60; // Dakika cinsinden
    }

    @Override
    public double getHiz() {
        return yurumeHizi;
    }

    @Override
    public void setHiz(double yurumeHizi) {
        this.yurumeHizi= yurumeHizi;
    }


    @Override
    public boolean duraklaUyumluMu(Durak durak) {
        return false;
    }

    @Override
    public AracKategori getKategori() {
        return AracKategori.ON_DEMAND;
    }

    @Override
    public String getDisplayName() {
        return "🚶 Yürüme";
    }
}