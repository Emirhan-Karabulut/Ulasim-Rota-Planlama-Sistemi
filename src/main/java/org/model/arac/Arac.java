package org.model.arac;

import org.model.Durak;
import org.yardimci.RotaYardimcisi;

public abstract class Arac {
    private String type;
    private double hiz; // Hız değişkeni eklendi

    protected Arac(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public abstract double hesaplaMesafeUcreti(double mesafe);

    public double hesaplaMesafeUcreti(double baslangicLat, double baslangicLon, double bitisLat, double bitisLon) {
        if (getKategori() == AracKategori.ON_DEMAND) {
            RotaYardimcisi.RotaBilgisi rotaBilgisi = getRotaVerisi(baslangicLat, baslangicLon, bitisLat, bitisLon);
            return hesaplaApiUcreti(rotaBilgisi.getMesafeKm());
        } else {
            double mesafe = org.yardimci.MesafeHesaplayici.haversine(
                    baslangicLat, baslangicLon, bitisLat, bitisLon);
            return hesaplaMesafeUcreti(mesafe);
        }
    }

    protected double hesaplaApiUcreti(double apiMesafeKm) {
        return hesaplaMesafeUcreti(apiMesafeKm);
    }

    public abstract double hesaplaSure(double mesafe);


    public double hesaplaSure(double baslangicLat, double baslangicLon, double bitisLat, double bitisLon) {
        if (getKategori() == AracKategori.ON_DEMAND) {
            // Önce API'den sadece mesafeyi al, süreyi alma
            RotaYardimcisi.RotaBilgisi rotaBilgisi = getRotaVerisi(baslangicLat, baslangicLon, bitisLat, bitisLon);
            double mesafe = rotaBilgisi.getMesafeKm();

            // Mesafeyi kullanarak kendi süre hesaplama fonksiyonunu çağır
            return hesaplaSure(mesafe);
        } else {
            // PUBLIC_TRANSPORT için mevcut implementasyon
            double mesafe = org.yardimci.MesafeHesaplayici.haversine(
                    baslangicLat, baslangicLon, bitisLat, bitisLon);
            return hesaplaSure(mesafe);
        }
    }

    public void setHiz(double yeniHiz) {
            this.hiz = yeniHiz;
    }

    protected RotaYardimcisi.RotaBilgisi getRotaVerisi(double baslangicLat, double baslangicLon, double bitisLat, double bitisLon) {
        return RotaYardimcisi.getRotaBilgisi(
                baslangicLat, baslangicLon, bitisLat, bitisLon);
    }

    public abstract AracKategori getKategori();

    public abstract double getHiz();

    public abstract boolean duraklaUyumluMu(Durak durak);

    public abstract String getDisplayName();
}