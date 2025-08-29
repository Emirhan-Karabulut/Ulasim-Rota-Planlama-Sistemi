package org.denetleyici;

import org.model.Durak;
import org.model.Konum;
import org.model.SonrakiDurak;
import org.model.arac.Arac;
import org.model.arac.AracKategori;
import org.yardimci.RotaYardimcisi;
import org.model.arac.Yurume;
import org.model.odeme.OdemeYontemi;
import org.model.yolcu.Yolcu;

import java.util.List;

public class UcretHesaplayici {

    /**
     * Rota için toplam ücret, süre ve mesafeyi hesaplar.
     *
     * @param yol Rota üzerindeki duraklar
     * @param yolcu Yolcu bilgisi
     * @param odemeYontemi Ödeme yöntemi
     * @param kullanilanAraclar Kullanılan araçlar listesi
     * @return Toplam ücret, süre ve mesafe array olarak [ücret, süre, mesafe]
     */
    public double[] hesaplaRotaToplamDegerleri(List<Durak> yol, Yolcu yolcu, OdemeYontemi odemeYontemi, List<Arac> kullanilanAraclar) {
        double toplamMesafe = 0;
        double toplamSure = 0;
        double toplamUcret = 0;

        // Listede yeterli sayıda araç olup olmadığını kontrol et
        if (kullanilanAraclar.size() < yol.size() - 1) {
            System.out.println("Uyarı: Araç listesi, durak sayısı-1'den küçük. Araç listesi genişletiliyor.");
            // Kalan segmentler için son aracı kullan veya varsayılan bir araç ekle
            Arac defaultArac = kullanilanAraclar.isEmpty() ? null : kullanilanAraclar.get(kullanilanAraclar.size() - 1);
            while (kullanilanAraclar.size() < yol.size() - 1) {
                kullanilanAraclar.add(defaultArac);
            }
        }

        for (int i = 0; i < yol.size() - 1; i++) {
            Durak suanki = yol.get(i);
            Durak sonraki = yol.get(i + 1);
            Arac arac = kullanilanAraclar.get(i);

            double[] segmentDegerleri = hesaplaSegmentDegerleri(suanki, sonraki, arac, yolcu, odemeYontemi);

            toplamMesafe += segmentDegerleri[0];
            toplamSure += segmentDegerleri[1];
            toplamUcret += segmentDegerleri[2];
        }

        return new double[]{toplamUcret, toplamSure, toplamMesafe};
    }

    /**
     * İki durak arasındaki segment için mesafe, süre ve ücreti hesaplar.
     *
     * @param suanki Başlangıç durağı
     * @param sonraki Varış durağı
     * @param arac Kullanılan araç
     * @param yolcu Yolcu bilgisi
     * @param odemeYontemi Ödeme yöntemi
     * @return [mesafe, süre, ücret] değerlerini içeren dizi
     */
    public double[] hesaplaSegmentDegerleri(Durak suanki, Durak sonraki, Arac arac, Yolcu yolcu, OdemeYontemi odemeYontemi) {
        double segmentMesafe = 0;
        double segmentSure = 0;
        double segmentUcret = 0;
        boolean segmentBulundu = false;

        // 1. Direkt bağlantı kontrolü
        if (suanki.getNextStops() != null) {
            for (SonrakiDurak snr : suanki.getNextStops()) {
                if (snr.getStopId().equals(sonraki.getId())) {
                    segmentMesafe = snr.getMesafe();
                    segmentSure = snr.getSure();
                    segmentUcret = snr.getUcret();
                    segmentBulundu = true;
                    break;
                }
            }
        }

        // 2. Transfer bağlantısı kontrolü
        if (!segmentBulundu && suanki.getTransfer() != null &&
                suanki.getTransfer().getTransferStopId().equals(sonraki.getId())) {
            segmentMesafe = 0; // Transfer için mesafe genelde 0 kabul edilir
            segmentSure = suanki.getTransfer().getTransferSure();
            segmentUcret = suanki.getTransfer().getTransferUcret();
            segmentBulundu = true;
        }

        // 3. Ters yön bağlantısı kontrolü
        if (!segmentBulundu && sonraki.getNextStops() != null) {
            for (SonrakiDurak snr : sonraki.getNextStops()) {
                if (snr.getStopId().equals(suanki.getId())) {
                    // Ters yön için aynı değerleri kullanıyoruz - veri tek yönlü olduğu için
                    segmentMesafe = snr.getMesafe();
                    segmentSure = snr.getSure();
                    segmentUcret = snr.getUcret();
                    segmentBulundu = true;
                    break;
                }
            }
        }

        // 4. Hiçbir bağlantı bulunamazsa, API veya haversine ile hesapla
        if (!segmentBulundu) {
            if (arac != null) {
                if (arac.getKategori() == AracKategori.ON_DEMAND) {
                    // ON_DEMAND kategorisindeki araçlar için API kullan
                    // Yeni metot imzasını kullan: (lat, lon koordinatları ile hesaplama)
                    segmentUcret = arac.hesaplaMesafeUcreti(
                            suanki.getLat(), suanki.getLon(),
                            sonraki.getLat(), sonraki.getLon());

                    segmentSure = arac.hesaplaSure(
                            suanki.getLat(), suanki.getLon(),
                            sonraki.getLat(), sonraki.getLon());

                    // API'den gelen mesafe değerini almak için RoutingHelper kullan
                    RotaYardimcisi.RotaBilgisi rotaBilgisi = RotaYardimcisi.getRotaBilgisi(
                            suanki.getLat(), suanki.getLon(),
                            sonraki.getLat(), sonraki.getLon());
                    segmentMesafe = rotaBilgisi.getMesafeKm();
                } else {
                    // PUBLIC_TRANSPORT kategorisi için haversine mesafesi kullan
                    segmentMesafe = org.yardimci.MesafeHesaplayici.haversine(
                            suanki.getLat(), suanki.getLon(),
                            sonraki.getLat(), sonraki.getLon());

                    // PUBLIC_TRANSPORT için mevcut hesaplama metodlarını kullan
                    segmentUcret = arac.hesaplaMesafeUcreti(segmentMesafe);
                    segmentSure = arac.hesaplaSure(segmentMesafe);
                }
            } else {
                // Araç null ise haversine ile hesapla
                segmentMesafe = org.yardimci.MesafeHesaplayici.haversine(
                        suanki.getLat(), suanki.getLon(),
                        sonraki.getLat(), sonraki.getLon());

                // Varsayılan hesaplama (arac null ise)
                segmentSure = (segmentMesafe / 30) * 60; // 30 km/s hız varsayımı
                segmentUcret = segmentMesafe * 2;  // km başına 2 TL varsayımı
            }
        }

        // Ücret üzerinde indirim ve ödeme yöntemi uygulaması
        // Artık aracın "yurume" olup olmadığını kontrol etmek için string karşılaştırması yerine instanceof kullanıyoruz.
        if (arac != null && !(arac instanceof Yurume)) {
            if (arac.getKategori() == AracKategori.PUBLIC_TRANSPORT) {
                // PUBLIC_TRANSPORT için indirim ve ödeme yöntemi uygula
                double indirimliFiyat = yolcu.indirimiUygula(segmentUcret);
                segmentUcret = odemeYontemi.odemeUcretiniHesapla(indirimliFiyat);
            } else {
                // ON_DEMAND için ödeme yöntemi uygulanmaz, ücret doğrudan kullanılır
            }
        } else {
            // Yürüme veya araç null ise ücret sıfırdır
        }

        return new double[]{segmentMesafe, segmentSure, segmentUcret};
    }

    /**
     * Başlangıç ve varış segmentleri için ek ücret, süre ve mesafe hesaplaması yapar.
     *
     * @param basMesafe Başlangıç mesafesi
     * @param sonMesafe Varış mesafesi
     * @param basArac Başlangıç aracı
     * @param varArac Varış aracı
     * @param yolcu Yolcu bilgisi
     * @param baslangicKonum Başlangıç konumu
     * @param baslangicDurak Başlangıç durağı
     * @param varisDurak Varış durağı
     * @param hedefKonum Hedef konum
     * @return [ek ücret, ek süre, ek mesafe] değerlerini içeren dizi
     */
    public double[] hesaplaEkSegmentDegerleri(
            double basMesafe, double sonMesafe,
            Arac basArac, Arac varArac,
            Yolcu yolcu,
            Konum baslangicKonum, Durak baslangicDurak,
            Durak varisDurak, Konum hedefKonum) {

        double ekUcret = 0;
        double ekSure = 0;
        double ekMesafe = 0;

        // Başlangıç segmenti hesaplaması
        if (basMesafe > 0 && basArac != null) {
            if (basArac.getKategori() == AracKategori.ON_DEMAND) {
                // Ücret hesaplaması aynı
                ekUcret += basArac.hesaplaMesafeUcreti(
                        baslangicKonum.getLat(), baslangicKonum.getLon(),
                        baslangicDurak.getLat(), baslangicDurak.getLon());

                // API'den mesafe bilgisini al ve km cinsinden kullan
                RotaYardimcisi.RotaBilgisi baslangicRotaBilgisi = RotaYardimcisi.getRotaBilgisi(
                        baslangicKonum.getLat(), baslangicKonum.getLon(),
                        baslangicDurak.getLat(), baslangicDurak.getLon());
                double segmentMesafeKm = baslangicRotaBilgisi.getMesafeKm();

                // Eğer araç yürüme aracı ise, setHiz çağrılır ve hesaplaSure metodu aracın fallback yöntemini kullanır.
                if (basArac instanceof Yurume) {
                    basArac.setHiz(yolcu.getYurumeHizi());
                    ekSure += basArac.hesaplaSure(segmentMesafeKm);
                } else {
                    ekSure += basArac.hesaplaSure(
                            baslangicKonum.getLat(), baslangicKonum.getLon(),
                            baslangicDurak.getLat(), baslangicDurak.getLon());
                }
                ekMesafe += segmentMesafeKm;
            } else {
                // PUBLIC_TRANSPORT için normal hesaplama
                ekUcret += basArac.hesaplaMesafeUcreti(basMesafe);
                ekSure += basArac.hesaplaSure(basMesafe);
                ekMesafe += basMesafe;
            }
        }

        // Varış segmenti hesaplaması
        if (sonMesafe > 0 && varArac != null) {
            if (varArac.getKategori() == AracKategori.ON_DEMAND) {
                ekUcret += varArac.hesaplaMesafeUcreti(
                        varisDurak.getLat(), varisDurak.getLon(),
                        hedefKonum.getLat(), hedefKonum.getLon());

                RotaYardimcisi.RotaBilgisi varisRotaBilgisi = RotaYardimcisi.getRotaBilgisi(
                        varisDurak.getLat(), varisDurak.getLon(),
                        hedefKonum.getLat(), hedefKonum.getLon());
                double segmentMesafeKm = varisRotaBilgisi.getMesafeKm();

                if (varArac instanceof Yurume) {
                    varArac.setHiz(yolcu.getYurumeHizi());
                    ekSure += varArac.hesaplaSure(segmentMesafeKm);
                } else {
                    ekSure += varArac.hesaplaSure(
                            varisDurak.getLat(), varisDurak.getLon(),
                            hedefKonum.getLat(), hedefKonum.getLon());
                }
                ekMesafe += segmentMesafeKm;
            } else {
                ekUcret += varArac.hesaplaMesafeUcreti(sonMesafe);
                ekSure += varArac.hesaplaSure(sonMesafe);
                ekMesafe += sonMesafe;
            }
        }

        return new double[]{ekUcret, ekSure, ekMesafe};
    }

    /**
     * Doğrudan rota için ücret ve süre hesaplaması yapar
     *
     * @param mesafe Rota mesafesi (km cinsinden)
     * @param arac Kullanılan araç
     * @param yolcu Yolcu bilgisi
     * @param odemeYontemi Ödeme yöntemi
     * @param baslangicKonum Başlangıç konumu (null değilse konum bazlı hesaplama yapılır)
     * @param varisKonum Varış konumu (null değilse konum bazlı hesaplama yapılır)
     * @return [ücret, süre, mesafe] değerlerini içeren dizi
     */
    public double[] hesaplaDogruRotaDegerleri(
            double mesafe,
            Arac arac,
            Yolcu yolcu,
            OdemeYontemi odemeYontemi,
            Konum baslangicKonum,
            Konum varisKonum) {

        double ucret = 0;
        double sure = 0;
        double hesaplananMesafe = mesafe;

        if (arac == null) {
            // Varsayılan hesaplama
            sure = (mesafe / 30) * 60; // 30 km/s hız varsayımı
            ucret = mesafe * 2;  // km başına 2 TL varsayımı
        } else {
            // Araç kategorisine göre hesaplama
            if (arac.getKategori() == AracKategori.ON_DEMAND && baslangicKonum != null && varisKonum != null) {
                // On-demand araçlar için konum bazlı hesaplama
                if (arac instanceof Yurume) {
                    // Yürüme aracı için hız ayarı
                    arac.setHiz(yolcu.getYurumeHizi());

                    // API'den mesafe bilgisini al
                    RotaYardimcisi.RotaBilgisi rotaBilgisi = RotaYardimcisi.getRotaBilgisi(
                            baslangicKonum.getLat(), baslangicKonum.getLon(),
                            varisKonum.getLat(), varisKonum.getLon());
                    hesaplananMesafe = rotaBilgisi.getMesafeKm();

                    // Ücret ve süre hesaplama
                    ucret = arac.hesaplaMesafeUcreti(hesaplananMesafe); // Genellikle 0
                    sure = arac.hesaplaSure(hesaplananMesafe);
                } else {
                    // Diğer on-demand araçlar için konum bazlı hesaplama
                    ucret = arac.hesaplaMesafeUcreti(
                            baslangicKonum.getLat(), baslangicKonum.getLon(),
                            varisKonum.getLat(), varisKonum.getLon());

                    // API'den mesafe bilgisini al
                    RotaYardimcisi.RotaBilgisi rotaBilgisi = RotaYardimcisi.getRotaBilgisi(
                            baslangicKonum.getLat(), baslangicKonum.getLon(),
                            varisKonum.getLat(), varisKonum.getLon());
                    hesaplananMesafe = rotaBilgisi.getMesafeKm();

                    sure = arac.hesaplaSure(
                            baslangicKonum.getLat(), baslangicKonum.getLon(),
                            varisKonum.getLat(), varisKonum.getLon());
                }
            } else {
                // Toplu taşıma veya konum bilgisi olmayan durum için mesafe bazlı hesaplama
                ucret = arac.hesaplaMesafeUcreti(mesafe);
                sure = arac.hesaplaSure(mesafe);
            }

            // İndirim ve ödeme yöntemi uygulaması
            if (arac != null && arac.getKategori() == AracKategori.PUBLIC_TRANSPORT) {
                double indirimliFiyat = yolcu.indirimiUygula(ucret);
                ucret = odemeYontemi.odemeUcretiniHesapla(indirimliFiyat);
            } // ON_DEMAND veya araç null ise ödeme yöntemi uygulanmaz
        }

        return new double[]{ucret, sure, hesaplananMesafe};
    }

}
