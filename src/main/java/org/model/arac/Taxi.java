package org.model.arac;

import org.model.Durak;

public class Taxi extends Arac {
    private double openingFee;
    private double costPerKm;
    private double speed;

    public Taxi() {
        super("taxi");
        this.openingFee = 10.0;
        this.costPerKm = 4.0;
        this.speed = 80.0;
    }

    public Taxi(double openingFee, double costPerKm, double speed) {
        super("taxi");
        this.openingFee = openingFee;
        this.costPerKm = costPerKm;
        this.speed = speed;
    }

    @Override
    public double hesaplaMesafeUcreti(double mesafe) {
        // Fallback hesaplama (API kullanılamadığında veya sadece mesafe bilgisi varsa)
        return (mesafe * costPerKm) + openingFee;
    }

    @Override
    protected double hesaplaApiUcreti(double apiMesafeKm) {
        // API'den gelen mesafe bilgisini kullanarak özel hesaplama
        return (apiMesafeKm * costPerKm) + openingFee;
    }

    @Override
    public double hesaplaSure(double mesafe) {
        // Fallback süre hesaplaması (API kullanılamadığında veya sadece mesafe bilgisi varsa)
        return (mesafe / speed) * 60; // Dakika cinsinden
    }

    @Override
    public double getHiz() {
        return speed;
    }

    @Override
    public void setHiz(double hiz) {
        this.speed =hiz;
    }

    @Override
    public AracKategori getKategori() {
        return AracKategori.ON_DEMAND;
    }

    // Getters for properties
    public double getOpeningFee() {
        return openingFee;
    }

    public double getCostPerKm() {
        return costPerKm;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public boolean duraklaUyumluMu(Durak durak) {
        return false; // Taksiler herhangi bir durakla uyumlu değil
    }

    @Override
    public String getDisplayName() {
        return "🚕 Taksi";
    }
}