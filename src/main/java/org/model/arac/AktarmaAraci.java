package org.model.arac;

import org.model.Durak;

public class AktarmaAraci extends Arac {
    private String fromStopId;
    private String toStopId;
    private int transferSure;
    private double transferUcret;


    public AktarmaAraci() {
        super("aktarma");
    }
    public AktarmaAraci(String fromStopId, String toStopId, int transferSure, double transferUcret) {
        super("aktarma");
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
        this.transferSure = transferSure;
        this.transferUcret = transferUcret;
    }

    @Override
    public double hesaplaMesafeUcreti(double mesafe) {
        return transferUcret; // Aktarma ücretini döndür
    }

    @Override
    public double hesaplaSure(double mesafe) {
        return transferSure; // Aktarma süresini döndür
    }

    @Override
    public AracKategori getKategori() {
        return AracKategori.PUBLIC_TRANSPORT;
    }

    @Override
    public double getHiz() {
        // Aktarmalar için yürüme hızı (gerekirse)
        return 0.0;
    }

    @Override
    public boolean duraklaUyumluMu(Durak durak) {
        // Aktarmalar her durakla uyumludur
        return true;
    }

    @Override
    public String getDisplayName() {
        return "🔁 Aktarma";
    }
}