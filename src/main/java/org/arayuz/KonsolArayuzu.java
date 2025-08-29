package org.arayuz;

import org.model.Durak;
import org.model.Rota;
import org.model.SonrakiDurak;
import org.yardimci.JSONVeriYukleyici;

import java.util.List;

public class KonsolArayuzu {

    public void JSONYazdir(JSONVeriYukleyici veriYukleyici) {
        List<Durak> duraklar = veriYukleyici.getDuraklar();
        if (duraklar != null && !duraklar.isEmpty()) {
            for (Durak durak : duraklar) {
                System.out.println("Durak: " + durak.getName() + " (Tip: " + durak.getType() + ")");

                if (durak.getNextStops() != null && !durak.getNextStops().isEmpty()) {
                    for (SonrakiDurak nextStop : durak.getNextStops()) {
                        System.out.println("\t -> Sonraki Durak: " + nextStop.getStopId() +
                                " | Mesafe: " + nextStop.getMesafe() + " km" +
                                " | Süre: " + nextStop.getSure() + " dk" +
                                " | Ücret: " + nextStop.getUcret());
                    }
                }

                if (durak.getTransfer() != null) {
                    System.out.println("\t Transfer: " + durak.getTransfer().getTransferStopId() +
                            " | Transfer Süresi: " + durak.getTransfer().getTransferSure() + " dk" +
                            " | Transfer Ücreti: " + durak.getTransfer().getTransferUcret());
                }

                System.out.println("");
            }
        } else {
            System.out.println("Hiç durak verisi bulunamadı!");
        }
    }

    public void rotaYazdir(Rota rota, int rotaNumarasi) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================\n");
        sb.append(String.format("         ROTA #%d DETAYLARI\n", rotaNumarasi));
        sb.append("================================\n\n");

        // Kullanılan araçlar
        sb.append("Kullanılan Araçlar: ").append(rota.aracBilgisiGetir()).append("\n\n");

        // Rota: Başlangıç -> Duraklar (aktarmalarla) -> Hedef
        sb.append("Rota: ");
        if (rota.getBaslangicKonum() != null) {
            sb.append("Başlangıç: (").append(rota.getBaslangicKonum().getLat()).append(", ").append(rota.getBaslangicKonum().getLon()).append(")");
        } else {
            sb.append("Başlangıç: Belirtilmemiş");
        }

        List<Durak> yol = rota.getYol();
        for (int i = 0; i < yol.size(); i++) {
            sb.append(" -> ");
            sb.append(yol.get(i).getName());

            // Aktarma kontrolü: Bir sonraki durak transfer durağı mı?
            if (i < yol.size() - 1) {
                Durak current = yol.get(i);
                Durak next = yol.get(i + 1);
                if (current.getTransfer() != null && current.getTransfer().getTransferStopId().equals(next.getId())) {
                    sb.append(" (Aktarma: ").append(current.getTransfer().getTransferSure()).append(" dk, Ücret: ").append(current.getTransfer().getTransferUcret()).append(" TL)");
                }
            }
        }

        if (rota.getHedefKonum() != null) {
            sb.append(" -> Hedef: (").append(rota.getHedefKonum().getLat()).append(", ").append(rota.getHedefKonum().getLon()).append(")");
        } else {
            sb.append(" -> Hedef: Belirtilmemiş");
        }
        sb.append("\n\n");

        // Toplam mesafe, süre ve ücret
        sb.append(String.format("Toplam Mesafe : %.2f km\n", rota.getToplamMesafe()));
        sb.append(String.format("Toplam Süre   : %.2f dk\n", rota.getToplamSure()));
        sb.append(String.format("Toplam Ücret  : %.2f TL\n", rota.getToplamUcret()));
        sb.append("================================\n");

        System.out.println(sb.toString());
    }

    public void rotaListesiniYazdir(List<Rota> rotalar) {
        if (rotalar.isEmpty()) {
            System.out.println("Hiç rota bulunamadı!");
            return;
        }

        System.out.println("\n=== BULUNAN ROTALAR (" + rotalar.size() + " adet) ===\n");

        for (int i = 0; i < rotalar.size(); i++) {
            rotaYazdir(rotalar.get(i), i + 1);
        }
    }

}