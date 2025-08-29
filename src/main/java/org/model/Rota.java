package org.model;

import org.model.arac.Arac;

import java.util.List;

public class Rota {
    private List<Durak> yol;
    private double toplamMesafe;
    private double toplamSure;
    private double toplamUcret;
    private List<Arac> kullanilanAraclar; // Hangi araçların kullanıldığını tutar
    private Konum baslangicKonum; // Başlangıç konumu
    private Konum hedefKonum;     // Hedef konumu

    public Rota(List<Durak> yol, double toplamMesafe, double toplamSure, double toplamUcret) {
        this.yol = yol;
        this.toplamMesafe = toplamMesafe;
        this.toplamSure = toplamSure;
        this.toplamUcret = toplamUcret;
    }

    public List<Durak> getYol() {
        return yol;
    }

    public void setYol(List<Durak> yol) {
        this.yol = yol;
    }

    public double getToplamMesafe() {
        return toplamMesafe;
    }

    public void setToplamMesafe(double toplamMesafe) {
        this.toplamMesafe = toplamMesafe;
    }

    public double getToplamSure() {
        return toplamSure;
    }

    public void setToplamSure(double toplamSure) {
        this.toplamSure = toplamSure;
    }

    public double getToplamUcret() {
        return toplamUcret;
    }

    public void setToplamUcret(double toplamUcret) {
        this.toplamUcret = toplamUcret;
    }

    public List<Arac> getKullanilanAraclar() {
        return kullanilanAraclar;
    }

    public void setKullanilanAraclar(List<Arac> kullanilanAraclar) {
        this.kullanilanAraclar = kullanilanAraclar;
    }

    public Konum getBaslangicKonum() {
        return baslangicKonum;
    }

    public void setBaslangicKonum(Konum baslangicKonum) {
        this.baslangicKonum = baslangicKonum;
    }

    public Konum getHedefKonum() {
        return hedefKonum;
    }

    public void setHedefKonum(Konum hedefKonum) {
        this.hedefKonum = hedefKonum;
    }

    public String aracBilgisiGetir() {
        StringBuilder sb = new StringBuilder();
        for (Arac arac : kullanilanAraclar) {
            if (arac != null) {
                sb.append(arac.getDisplayName()).append(", ");
            } else {
                sb.append("Tanımsız, ");
            }
        }
        // Eğer aktarma yapıldıysa, aktarma bilgisini ekleyelim
        if (!sb.toString().contains("Aktarma") && kullanilanAraclar.size() > 1) {
            sb.append("Aktarma, ");
        }
        // Son virgülü temizle
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }


    /**
     * Rota bilgilerini okunabilir bir formatta döndürür.
     * Başlangıç ve hedef konumları ile durakları içerir.
     *
     * @return Rota detaylarını içeren String
     */
    @Override
    public String toString() {
        StringBuilder routeNames = new StringBuilder();
        if (baslangicKonum != null) {
            routeNames.append("Başlangıç: (").append(baslangicKonum.getLat()).append(", ").append(baslangicKonum.getLon()).append(")");
        } else {
            routeNames.append("Başlangıç: Belirtilmemiş");
        }

        if (!yol.isEmpty()) {
            routeNames.append(" -> ");
            for (int i = 0; i < yol.size(); i++) {
                routeNames.append(yol.get(i).getName());
                if (i < yol.size() - 1) {
                    routeNames.append(" -> ");
                }
            }
        }

        if (hedefKonum != null) {
            routeNames.append(" -> Hedef: (").append(hedefKonum.getLat()).append(", ").append(hedefKonum.getLon()).append(")");
        } else {
            routeNames.append(" -> Hedef: Belirtilmemiş");
        }

        return "ROTA DETAYLARI\n" +
                "Kullanılan Araçlar: " + aracBilgisiGetir() + "\n" +
                "Rota: " + routeNames.toString() + "\n" +
                "Toplam Mesafe: " + toplamMesafe + " km\n" +
                "Toplam Süre: " + toplamSure + " dk\n" +
                "Toplam Ücret: " + toplamUcret + " TL\n";
    }
}