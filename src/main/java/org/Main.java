package org;

import org.arayuz.grafikselarayuz.GrafikselArayuz;
import org.arayuz.KonsolArayuzu;
import org.denetleyici.RotaPlanlayici;
import org.model.Durak;
import org.model.Rota;
import org.model.RotaBilgileri;
import org.model.arac.*;
import org.model.odeme.*;
import org.model.yolcu.*;
import org.yardimci.JSONVeriYukleyici;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Main {
    private static List<Durak> duraklar;
    private static Map<String, Arac> araclar;
    private static RotaPlanlayici rotaPlanlayici;
    private static KonsolArayuzu konsolArayuzu;
    private static JSONVeriYukleyici veriYukleyici;
    private static Map<String, Supplier<Yolcu>> yolcuFactory;
    private static Map<String, Supplier<OdemeYontemi>> odemeFactory;

    public static void main(String[] args) throws Exception {
        // Veri yükleme işlemleri
        ObjectMapper mapper = new ObjectMapper();
        veriYukleyici = mapper.readValue(new File("src/main/resources/veriseti.json"), JSONVeriYukleyici.class);

        // Konsol arayüzü oluştur
        konsolArayuzu = new KonsolArayuzu();

        //HashMapleri oluştur
        yolcuFactory = new HashMap<>();
        odemeFactory = new HashMap<>();
        araclar = new HashMap<>();

        // HashMapleri hazırla
        yolcuFactory.put("Genel Yolcu", GenelYolcu::new);
        yolcuFactory.put("Öğrenci Yolcu", OgrenciYolcu::new);
        yolcuFactory.put("Yaşlı Yolcu", YasliYolcu::new);

        odemeFactory.put("KentKart", KentKart::new);
        odemeFactory.put("KrediKarti", KrediKarti::new);
        odemeFactory.put("Nakit", Nakit::new);

        araclar.put("taxi", veriYukleyici.getTaxi());
        araclar.put("tramvay", new Tramvay());
        araclar.put("otobus", new Otobus());
        araclar.put("yurume", new Yurume());
        araclar.put("aktarma", new AktarmaAraci());

        // Rota planlayıcıyı oluştur
        rotaPlanlayici = new RotaPlanlayici();

        // Durakları al
        duraklar = veriYukleyici.getDuraklar();

        // Grafiksel arayüzü oluştur ve göster
        SwingUtilities.invokeLater(() -> {
            GrafikselArayuz arayuz = new GrafikselArayuz(yolcuFactory, odemeFactory, araclar);
            arayuz.setDuraklar(duraklar);

            // Rota oluşturma işlemini ayrı bir metoda bağlama
            arayuz.setRotaOlusturListener(() -> {
                RotaBilgileri rotaData = arayuz.getRotaBilgileri();
                if (rotaData != null) {
                    // Rota alternatiflerini hesapla
                    List<Rota> rotalar = rotaPlanlayici.rotaAlternatifleriHesaplayici(
                            rotaData.getBaslangicKonum(),
                            rotaData.getVarisKonum(),
                            duraklar,
                            araclar,
                            rotaData.getYolcu(),
                            rotaData.getOdeme(),
                            rotaData.getizinVerilenOnDemandAracTipleri(),
                            rotaData.getizinVerilenPublicTransportAracTipleri(),
                            rotaData.getOptimizasyonTipi()
                    );
                    System.out.println(rotaData.getizinVerilenOnDemandAracTipleri());
                    System.out.println(rotaData.getizinVerilenPublicTransportAracTipleri());
                    // Rotaları arayüze gönder
                    arayuz.setRotalar(rotalar);

                    // Konsola rotaları yazdır
                    konsolArayuzu.rotaListesiniYazdir(rotalar);
                }
            });

            arayuz.setVisible(true);
        });

        System.out.println("\n\n================ TÜM DURAK BİLGİLERİ ================");
        konsolArayuzu.JSONYazdir(veriYukleyici);
    }
}