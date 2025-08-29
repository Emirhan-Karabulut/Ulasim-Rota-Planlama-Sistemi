package org.denetleyici;

import org.model.arac.*;
import org.yardimci.RotaYardimcisi;
import org.model.Durak;
import org.model.Konum;
import org.model.Rota;
import org.model.SonrakiDurak;
import org.model.odeme.OdemeYontemi;
import org.model.yolcu.Yolcu;
import org.yardimci.MesafeHesaplayici;

import java.util.*;
import java.util.stream.Collectors;

public class RotaPlanlayici {

    // 3 km eşik değeri, bu mesafenin üzerindeyse başlangıç/varış segmentinde diğer on_demand tercih edilecek
    private static final double YURUME_ESIK_MESAFE = 3.0;

    // UcretHesaplayici örneğini ekleyelim
    private final UcretHesaplayici ucretHesaplayici;

    public RotaPlanlayici() {
        this.ucretHesaplayici = new UcretHesaplayici();
    }

    // UcretHesaplayici'yı dışarıdan enjekte etmek için constructor
    public RotaPlanlayici(UcretHesaplayici ucretHesaplayici) {
        this.ucretHesaplayici = ucretHesaplayici;
    }

    /**
     * Rota alternatiflerini hesaplar.
     * <p>
     * - DFS ile temel rota (durak dizisi ve kullanılan araç listesi) oluşturulur.
     * - Başlangıç ve varış segmentlerinin ek hesaplamaları, merkezi bir metot ile uygulanır.
     * - Eğer istenilen araç tipi tanımlı ve uygunsa, sadece o aracın kullanılmasına göre ek segmentler eklenir.
     * - Aksi durumda, başlangıç/varış segmentleri için uygun on-demand araç kombinasyonları üretilir.
     * - Son olarak, oluşturulan rota alternatifleri final aşamada yinelenenlerden filtrelenir.
     */
    public List<Rota> rotaAlternatifleriHesaplayici(
            Konum baslangicKonum,
            Konum varisKonum,
            List<Durak> duraklar,
            Map<String, Arac> araclar,
            Yolcu yolcu,
            OdemeYontemi odemeYontemi,
            List<String> izinVerilenondemandAracTipleri,
            List<String> izinVerilenPublicTransportTipleri,
            RotaOptimizasyonTipi optimizasyonTipi) {

        Map<String, Durak> durakHaritasi = new HashMap<>();
        for (Durak d : duraklar) {
            durakHaritasi.put(d.getId(), d);
        }

        Durak baslangicDuragi = enYakinDurak(baslangicKonum, duraklar, izinVerilenPublicTransportTipleri);
        Durak varisDuragi = enYakinDurak(varisKonum, duraklar, izinVerilenPublicTransportTipleri);

        // Başlangıç ve varış durakları aynı mı kontrolü - bu durumda sadece direkt rotaları kullan
        if (baslangicDuragi != null && varisDuragi != null && baslangicDuragi.getId().equals(varisDuragi.getId())) {
            System.out.println("Başlangıç ve varış için en yakın durak aynı: " + baslangicDuragi.getId() +
                    ". Sadece doğrudan rotalar hesaplanacak.");
            List<Rota> dogruRotalar = dogruRotaHesapla(baslangicKonum, varisKonum, araclar, yolcu, odemeYontemi, izinVerilenondemandAracTipleri);

            // Doğrudan rotalara başlangıç ve hedef konumlarını ekle
            for (Rota rota : dogruRotalar) {
                rota.setBaslangicKonum(baslangicKonum);
                rota.setHedefKonum(varisKonum);
            }
            return dogruRotalar;
        }

        // Uygun durak bulunamadı, doğrudan rotalar hesaplanacak
        if (baslangicDuragi == null || varisDuragi == null) {
            System.out.println("Başlangıç veya varış için uygun durak bulunamadı.");
            List<Rota> dogruRotalar = dogruRotaHesapla(baslangicKonum, varisKonum, araclar, yolcu, odemeYontemi, izinVerilenondemandAracTipleri);

            // Doğrudan rotalara başlangıç ve hedef konumlarını ekle
            for (Rota rota : dogruRotalar) {
                rota.setBaslangicKonum(baslangicKonum);
                rota.setHedefKonum(varisKonum);
            }
            return dogruRotalar;
        }
        RotaYardimcisi.RotaBilgisi rotaBaslangic = RotaYardimcisi.getRotaBilgisi(
                baslangicKonum.getLat(), baslangicKonum.getLon(),
                baslangicDuragi.getLat(), baslangicDuragi.getLon());
        // API'den dönen mesafe (metre cinsinden) veya km cinsinden: rotaBaslangic.getMesafe() / rotaBaslangic.getMesafeKm()
        double baslangictanIlkDuragaMesafe = rotaBaslangic.getMesafeKm();

        RotaYardimcisi.RotaBilgisi rotaVaris = RotaYardimcisi.getRotaBilgisi(varisDuragi.getLat(), varisDuragi.getLon(),varisKonum.getLat(), varisKonum.getLon());

        double varistanSonDuragaMesafe = rotaVaris.getMesafeKm();


        List<Rota> temelRotalar = new ArrayList<>();
        derinAramaIleRotalariBul(
                baslangicDuragi,
                varisDuragi,
                durakHaritasi,
                new ArrayList<>(),
                temelRotalar,
                new HashSet<>(),
                araclar,
                yolcu,
                odemeYontemi,
                new ArrayList<>(),
                izinVerilenPublicTransportTipleri
        );

        // Temel rotalara başlangıç ve hedef konumlarını ekle
        for (Rota rota : temelRotalar) {
            rota.setBaslangicKonum(baslangicKonum);
            rota.setHedefKonum(varisKonum);
        }

        List<Rota> finalRotalar = new ArrayList<>();

        for (Rota rota : temelRotalar) {
            List<Arac> baslangicAraclari = uygunOnDemandAracSec(
                    araclar,
                    baslangictanIlkDuragaMesafe,
                    izinVerilenondemandAracTipleri
            );

            List<Arac> varisAraclari = uygunOnDemandAracSec(
                    araclar,
                    varistanSonDuragaMesafe,
                    izinVerilenondemandAracTipleri
            );

            if (baslangicAraclari.isEmpty()) {
                baslangicAraclari = Collections.singletonList(null);
            }
            if (varisAraclari.isEmpty()) {
                varisAraclari = Collections.singletonList(null);
            }

            for (Arac basArac : baslangicAraclari) {
                for (Arac varArac : varisAraclari) {
                    Rota yeniRota = new Rota(
                            new ArrayList<>(rota.getYol()),
                            rota.getToplamMesafe(),
                            rota.getToplamSure(),
                            rota.getToplamUcret()
                    );
                    yeniRota.setKullanilanAraclar(new ArrayList<>(rota.getKullanilanAraclar()));
                    // Yeni rotaya da başlangıç ve hedef ekle
                    yeniRota.setBaslangicKonum(baslangicKonum);
                    yeniRota.setHedefKonum(varisKonum);

                    ekSegmentleriUygula(
                            yeniRota,
                            baslangictanIlkDuragaMesafe,
                            varistanSonDuragaMesafe,
                            basArac,
                            varArac,
                            yolcu,
                            baslangicKonum,  // Başlangıç konumu
                            baslangicDuragi, // Başlangıç durağı
                            varisDuragi,     // Varış durağı
                            varisKonum       // Hedef konum
                    );

                    finalRotalar.add(yeniRota);
                }
            }
        }

        List<Rota> dogruRotalar = dogruRotaHesapla(
                baslangicKonum,
                varisKonum,
                araclar,
                yolcu,
                odemeYontemi,
                izinVerilenondemandAracTipleri
        );

        // Doğrudan rotalara başlangıç ve hedef konumlarını ekle
        for (Rota rota : dogruRotalar) {
            rota.setBaslangicKonum(baslangicKonum);
            rota.setHedefKonum(varisKonum);
        }

        finalRotalar.addAll(dogruRotalar);

        return filtreleAyniRotalar(finalRotalar, optimizasyonTipi);
    }

    private List<Rota> dogruRotaHesapla(
            Konum baslangicKonum,
            Konum varisKonum,
            Map<String, Arac> araclar,
            Yolcu yolcu,
            OdemeYontemi odemeYontemi,
            List<String> izinVerilenondemandAracTipleri) {

        // OSRM API'den rota bilgisini alarak mesafe hesaplaması
        RotaYardimcisi.RotaBilgisi rotaBilgisi = RotaYardimcisi.getRotaBilgisi(
                baslangicKonum.getLat(), baslangicKonum.getLon(),
                varisKonum.getLat(), varisKonum.getLon());
        double mesafe = rotaBilgisi.getMesafe();

        List<Arac> onDemandAraclar = araclar.values().stream()
                .filter(arac -> arac.getKategori() == AracKategori.ON_DEMAND)
                .filter(arac -> izinVerilenondemandAracTipleri.contains(arac.getType()))
                .collect(Collectors.toList());

        List<Rota> dogruRotalar = new ArrayList<>();

        Optional<Arac> yurume = onDemandAraclar.stream()
                .filter(arac -> arac instanceof Yurume)
                .findFirst();

        List<Arac> yurumeDisindakiler = onDemandAraclar.stream()
                .filter(arac -> !(arac instanceof Yurume))
                .collect(Collectors.toList());

        if (mesafe > YURUME_ESIK_MESAFE) {
            if (!yurumeDisindakiler.isEmpty()) {
                for (Arac arac : yurumeDisindakiler) {
                    dogruRotalar.add(dogruRotaOlustur(baslangicKonum, varisKonum, arac, yolcu, odemeYontemi));
                }
            } else if (yurume.isPresent()) {
                dogruRotalar.add(dogruRotaOlustur(baslangicKonum, varisKonum, yurume.get(), yolcu, odemeYontemi));
            }
        } else {
            if (yurume.isPresent()) {
                dogruRotalar.add(dogruRotaOlustur(baslangicKonum, varisKonum, yurume.get(), yolcu, odemeYontemi));
            } else {
                for (Arac arac : yurumeDisindakiler) {
                    dogruRotalar.add(dogruRotaOlustur(baslangicKonum, varisKonum, arac, yolcu, odemeYontemi));
                }
            }
        }

        return dogruRotalar;
    }

    private Rota dogruRotaOlustur(
            Konum baslangicKonum,
            Konum varisKonum,
            Arac arac,
            Yolcu yolcu,
            OdemeYontemi odemeYontemi) {

        // OSRM API'den rota bilgisini alarak mesafe hesaplaması
        RotaYardimcisi.RotaBilgisi rotaBilgisi = RotaYardimcisi.getRotaBilgisi(
                baslangicKonum.getLat(), baslangicKonum.getLon(),
                varisKonum.getLat(), varisKonum.getLon());
        double mesafe = rotaBilgisi.getMesafeKm();

        // UcretHesaplayici kullanılarak ücret ve süre hesaplaması - konum parametrelerini de geçiyoruz
        double[] sonuclar = ucretHesaplayici.hesaplaDogruRotaDegerleri(
                mesafe,
                arac,
                yolcu,
                odemeYontemi,
                baslangicKonum,
                varisKonum);

        // Dönüş değerlerini alıyoruz
        double ucret = sonuclar[0];
        double sure = sonuclar[1];
        // Eğer hesaplanan mesafe değişmişse, güncel mesafeyi alıyoruz
        double hesaplananMesafe = sonuclar[2];

        List<Durak> bosYol = new ArrayList<>();
        Rota rota = new Rota(bosYol, hesaplananMesafe, sure, ucret);

        List<Arac> kullanilanAraclar = new ArrayList<>();
        kullanilanAraclar.add(arac);
        rota.setKullanilanAraclar(kullanilanAraclar);

        return rota;
    }


    private void ekSegmentleriUygula(Rota rota, double basMesafe, double sonMesafe,
                                    Arac basArac, Arac varArac, Yolcu yolcu,
                                    Konum baslangicKonum, Durak baslangicDurak,
                                    Durak varisDurak, Konum hedefKonum) {
        // UcretHesaplayici sınıfını kullanarak ek segment hesaplaması
        double[] ekDegerler = ucretHesaplayici.hesaplaEkSegmentDegerleri(
                basMesafe, sonMesafe, basArac, varArac, yolcu,
                baslangicKonum, baslangicDurak, varisDurak, hedefKonum);
        double ekUcret = ekDegerler[0];
        double ekSure = ekDegerler[1];
        double ekMesafe = ekDegerler[2];

        List<Arac> aracListesi = rota.getKullanilanAraclar();

        if (basMesafe > 0 && basArac != null) {
            // Başlangıç segmenti, rota listesinin başına ekleniyor
            aracListesi.add(0, basArac);
            System.out.println("Başlangıç segmenti için " + basArac.getClass().getSimpleName() + " eklendi. Mesafe: " + basMesafe);
        }

        if (sonMesafe > 0 && varArac != null) {
            // Varış segmenti, rota listesinin sonuna ekleniyor
            aracListesi.add(varArac);
            System.out.println("Varış segmenti için " + varArac.getClass().getSimpleName() + " eklendi. Mesafe: " + sonMesafe);
        }

        rota.setToplamUcret(rota.getToplamUcret() + ekUcret);
        rota.setToplamSure(rota.getToplamSure() + ekSure);
        rota.setToplamMesafe(rota.getToplamMesafe() + ekMesafe);
    }


    private List<Rota> filtreleAyniRotalar(List<Rota> rotalar, RotaOptimizasyonTipi optimizasyonTipi) {
        Set<String> uniqueSignatures = new HashSet<>();
        List<Rota> filtrelenmis = new ArrayList<>();

        for (Rota rota : rotalar) {
            String signature = rotaImzasiOlustur(rota);
            if (!uniqueSignatures.contains(signature)) {
                uniqueSignatures.add(signature);
                filtrelenmis.add(rota);
            }
        }

        // Optimizasyon türüne göre sıralama
        switch (optimizasyonTipi) {
            case MESAFE:
                filtrelenmis.sort(Comparator.comparingDouble(Rota::getToplamMesafe));
                break;
            case SURE:
                filtrelenmis.sort(Comparator.comparingDouble(Rota::getToplamSure));
                break;
            case UCRET:
                filtrelenmis.sort(Comparator.comparingDouble(Rota::getToplamUcret));
                break;
        }

        return filtrelenmis;
    }

    private String rotaImzasiOlustur(Rota rota) {
        StringBuilder sb = new StringBuilder();
        for (Durak d : rota.getYol()) {
            sb.append(d.getId()).append("-");
        }
        for (Arac a : rota.getKullanilanAraclar()) {
            if (a != null) {
                // Araç sınıfının adını kullanarak imza oluştur (OCP uyumlu)
                sb.append(a.getClass().getSimpleName()).append("-");
            }
        }
        return sb.toString();
    }

    private List<Arac> uygunOnDemandAracSec(
            Map<String, Arac> araclar,
            double mesafe,
            List<String> izinVerilenondemandAracTipleri) {

        List<Arac> onDemandAraclar = new ArrayList<>();

        System.out.println("Mesafe: " + mesafe + " km");

        // İzin verilen on-demand araçları filtrele
        List<Arac> filtrelenmisAraclar = araclar.values().stream()
                .filter(arac -> arac.getKategori() == AracKategori.ON_DEMAND)
                .filter(arac -> izinVerilenondemandAracTipleri.contains(arac.getType()))
                .collect(Collectors.toList());

        // Yürüme aracını instance of ile bul
        Optional<Arac> yurume = filtrelenmisAraclar.stream()
                .filter(arac -> arac instanceof Yurume)
                .findFirst();

        // Yürüme dışındaki araçları bul
        List<Arac> yurumeDisindakiler = filtrelenmisAraclar.stream()
                .filter(arac -> !(arac instanceof Yurume))
                .collect(Collectors.toList());

        // Mesafe kontrolü
        if (mesafe <= YURUME_ESIK_MESAFE) {
            // 3 km'den az mesafe için
            if (yurume.isPresent()) {
                // Yürüme varsa, SADECE yürüme aracını ekle
                onDemandAraclar.add(yurume.get());
                System.out.println("3km altı, YÜRÜME seçildi");
            } else {
                // Yürüme yoksa, diğer araçları ekle
                onDemandAraclar.addAll(yurumeDisindakiler);
                System.out.println("3km altı, yürüme yok, DİĞER ARAÇLAR seçildi");
            }
        } else {
            // 3 km'den fazla mesafe için
            if (!yurumeDisindakiler.isEmpty()) {
                // Yürüme dışında araçlar varsa, SADECE onları ekle
                onDemandAraclar.addAll(yurumeDisindakiler);
                System.out.println("3km üstü, DİĞER ARAÇLAR seçildi");
            } else if (yurume.isPresent()) {
                // Başka araç yoksa, yürümeyi ekle
                onDemandAraclar.add(yurume.get());
                System.out.println("3km üstü, başka araç yok, YÜRÜME seçildi");
            }
        }

        // Hiç araç seçilmediyse, varsayılan olarak taksi ekle (eğer izin verilmişse)
        if (onDemandAraclar.isEmpty()) {
            Optional<Arac> taxi = filtrelenmisAraclar.stream()
                    .filter(arac -> arac instanceof Taxi)
                    .findFirst();

            taxi.ifPresent(arac -> {
                onDemandAraclar.add(arac);
                System.out.println("Hiçbir uygun araç bulunamadı, varsayılan olarak TAKSİ eklendi");
            });
        }

        return onDemandAraclar;
    }


    private void derinAramaIleRotalariBul(
            Durak suankiDurak,
            Durak hedefDurak,
            Map<String, Durak> durakHaritasi,
            List<Durak> suankiYol,
            List<Rota> rotaAlternatifleri,
            Set<String> ziyaretEdilenler,
            Map<String, Arac> araclar,
            Yolcu yolcu,
            OdemeYontemi odemeYontemi,
            List<Arac> kullanilanAraclar,
            List<String> izinVerilenPublicTransportTipleri) {

        // DFS başlangıç logu
        System.out.println("DFS: Mevcut durak: " + suankiDurak.getId() + ", Hedef: " + hedefDurak.getId() + ", Yol uzunluğu: " + suankiYol.size());

        // Null kontrolleri: suankiYol ve kullanilanAraclar boş değilse devam
        if (suankiYol == null) {
            suankiYol = new ArrayList<>();
        }
        if (kullanilanAraclar == null) {
            kullanilanAraclar = new ArrayList<>();
        }

        suankiYol.add(suankiDurak);
        // Araç bilgisini kontrol ederken null kontrolü
        String ziyaretKontrol = suankiDurak.getId() +
                ( (kullanilanAraclar.isEmpty() || kullanilanAraclar.get(kullanilanAraclar.size() - 1) == null)
                        ? "" : kullanilanAraclar.get(kullanilanAraclar.size() - 1).getClass().getSimpleName());
        if (ziyaretEdilenler.contains(ziyaretKontrol)) {
            suankiYol.remove(suankiYol.size() - 1);
            return;
        }
        ziyaretEdilenler.add(ziyaretKontrol);

        // Eğer hedef durağa ulaşıldıysa, rota oluşturuluyor
        if (suankiDurak.getId().equals(hedefDurak.getId())) {
            Rota rota = rotaOlustur(suankiYol, yolcu, odemeYontemi, kullanilanAraclar);
            rotaAlternatifleri.add(rota);
        } else {
            // İleri yön: suankiDurak'ın nextStops listesindeki duraklar
            if (suankiDurak.getNextStops() != null) {
                System.out.println("Durak " + suankiDurak.getId() + " için " + suankiDurak.getNextStops().size() + " sonraki durak var");
                for (SonrakiDurak sonraki : suankiDurak.getNextStops()) {
                    Durak komsuDurak = durakHaritasi.get(sonraki.getStopId());
                    // Eğer bu durak zaten mevcut yolda varsa (tekrarlı ziyaret), geçiyoruz.
                    if (komsuDurak != null && suankiYol.stream().anyMatch(d -> d.getId().equals(komsuDurak.getId()))) {
                        continue;
                    }
                    // Bir önceki durak kontrolü: A->B sonrası B'den A'ya dönüş engelleniyor.
                    if (suankiYol.size() > 1 && komsuDurak.getId().equals(suankiYol.get(suankiYol.size() - 2).getId())) {
                        continue;
                    }
                    if (komsuDurak != null && izinVerilenPublicTransportTipleri.contains(komsuDurak.getType())) {
                        Arac sonrakiArac = belirleSonrakiArac(suankiDurak, komsuDurak, araclar);
                        // Eğer belirlenen araç null ise, bu dalı atlamak mantıklı olabilir
                        if (sonrakiArac == null) {
                            System.out.println("Uyarı: " + suankiDurak.getId() + " -> " + komsuDurak.getId() + " için sonraki araç null, geçiliyor.");
                            continue;
                        }
                        List<Arac> yeniKullanilanAraclar = new ArrayList<>(kullanilanAraclar);
                        yeniKullanilanAraclar.add(sonrakiArac);
                        derinAramaIleRotalariBul(
                                komsuDurak,
                                hedefDurak,
                                durakHaritasi,
                                new ArrayList<>(suankiYol),
                                rotaAlternatifleri,
                                new HashSet<>(ziyaretEdilenler),
                                araclar,
                                yolcu,
                                odemeYontemi,
                                yeniKullanilanAraclar,
                                izinVerilenPublicTransportTipleri
                        );
                    }
                }
            } else {
                System.out.println("Durak " + suankiDurak.getId() + " için sonraki durak yok");
            }

            // Ters yön: diğer durakların nextStops listesinde suankiDurak'a bağlantı var mı?
            for (Durak digerDurak : durakHaritasi.values()) {
                if (izinVerilenPublicTransportTipleri.contains(digerDurak.getType()) && digerDurak.getNextStops() != null) {
                    for (SonrakiDurak snr : digerDurak.getNextStops()) {
                        if (snr.getStopId().equals(suankiDurak.getId())) {
                            // Eğer bu durak zaten mevcut yolda varsa, atla
                            if (suankiYol.stream().anyMatch(d -> d.getId().equals(digerDurak.getId()))) {
                                continue;
                            }
                            // Bir önceki durak kontrolü: A->B sonrası B'den A'ya dönüş engelleniyor.
                            if (suankiYol.size() > 1 && digerDurak.getId().equals(suankiYol.get(suankiYol.size() - 2).getId())) {
                                continue;
                            }
                            String tersZiyaretKontrol = digerDurak.getId() +
                                    ( (kullanilanAraclar.isEmpty() || kullanilanAraclar.get(kullanilanAraclar.size() - 1) == null)
                                            ? "" : kullanilanAraclar.get(kullanilanAraclar.size() - 1).getClass().getSimpleName());
                            if (!ziyaretEdilenler.contains(tersZiyaretKontrol)) {
                                Arac tersArac = belirleSonrakiArac(digerDurak, suankiDurak, araclar);
                                if (tersArac == null) {
                                    System.out.println("Uyarı: " + digerDurak.getId() + " -> " + suankiDurak.getId() + " için ters araç null, geçiliyor.");
                                    continue;
                                }
                                List<Arac> yeniKullanilanAraclar = new ArrayList<>(kullanilanAraclar);
                                yeniKullanilanAraclar.add(tersArac);
                                derinAramaIleRotalariBul(
                                        digerDurak,
                                        hedefDurak,
                                        durakHaritasi,
                                        new ArrayList<>(suankiYol),
                                        rotaAlternatifleri,
                                        new HashSet<>(ziyaretEdilenler),
                                        araclar,
                                        yolcu,
                                        odemeYontemi,
                                        yeniKullanilanAraclar,
                                        izinVerilenPublicTransportTipleri
                                );
                            }
                            break; // ilk bulunan bağlantı üzerinden gidiyoruz
                        }
                    }
                }
            }

            // Transfer kontrolü
            if (suankiDurak.getTransfer() != null) {
                // Önce "aktarma" tipinin izin verilen tiplerde olup olmadığını kontrol et
                if (izinVerilenPublicTransportTipleri.contains("aktarma")) {
                    Durak aktarimDurak = durakHaritasi.get(suankiDurak.getTransfer().getTransferStopId());
                    if (aktarimDurak != null && izinVerilenPublicTransportTipleri.contains(aktarimDurak.getType())) {
                        if (!suankiYol.stream().anyMatch(d -> d.getId().equals(aktarimDurak.getId()))) {
                            // Aktarma aracını oluştur
                            AktarmaAraci aktarmaAraci = new AktarmaAraci(
                                    suankiDurak.getId(),
                                    aktarimDurak.getId(),
                                    suankiDurak.getTransfer().getTransferSure(),
                                    suankiDurak.getTransfer().getTransferUcret()
                            );

                            // Yeni kullanılan araçlar listesini oluştur ve aktarma aracını ekle
                            List<Arac> yeniKullanilanAraclar = new ArrayList<>(kullanilanAraclar);
                            yeniKullanilanAraclar.add(aktarmaAraci);

                            derinAramaIleRotalariBul(
                                    aktarimDurak,
                                    hedefDurak,
                                    durakHaritasi,
                                    new ArrayList<>(suankiYol),
                                    rotaAlternatifleri,
                                    new HashSet<>(ziyaretEdilenler),
                                    araclar,
                                    yolcu,
                                    odemeYontemi,
                                    yeniKullanilanAraclar,
                                    izinVerilenPublicTransportTipleri
                            );
                        }
                    }
                }
                // Eğer "aktarma" izin verilen tiplerde yoksa, bu transferi atla
            }
        }

        // DFS tamamlandığında, suankiYol ve ziyaretEdilenler güncellemelerini geri alıyoruz
        suankiYol.remove(suankiYol.size() - 1);
        ziyaretEdilenler.remove(ziyaretKontrol);
    }


    private Arac belirleSonrakiArac(Durak suankiDurak, Durak sonrakiDurak, Map<String, Arac> araclar) {
        boolean baglantiVarMi = false;
        String baglantiTipi = null;

        // Transfer bağlantısı kontrolü
        if (suankiDurak.getTransfer() != null &&
                suankiDurak.getTransfer().getTransferStopId().equals(sonrakiDurak.getId())) {
            baglantiVarMi = true;
            baglantiTipi = "transfer";

            // Transfer için özel araç oluştur ve dön
            return new AktarmaAraci(
                    suankiDurak.getId(),
                    sonrakiDurak.getId(),
                    suankiDurak.getTransfer().getTransferSure(),
                    suankiDurak.getTransfer().getTransferUcret()
            );
        }

        // Transfer bağlantısı kontrolü
        if (!baglantiVarMi && suankiDurak.getTransfer() != null &&
                suankiDurak.getTransfer().getTransferStopId().equals(sonrakiDurak.getId())) {
            baglantiVarMi = true;
            baglantiTipi = "transfer";
        }

        // Ters yön bağlantısı kontrolü
        if (!baglantiVarMi && sonrakiDurak.getNextStops() != null) {
            for (SonrakiDurak snr : sonrakiDurak.getNextStops()) {
                if (snr.getStopId().equals(suankiDurak.getId())) {
                    baglantiVarMi = true;
                    baglantiTipi = "reverse";
                    break;
                }
            }
        }

        // Public transport kategorisindeki araçlar arasında durak uyumluluğunu kontrol et
        for (Arac arac : araclar.values()) {
            // Transfer durumu için özel işleme
            if (baglantiTipi != null && baglantiTipi.equals("transfer")) {
                return araclar.values().stream()
                        .filter(a -> a.getType().equals("aktarma"))
                        .findFirst()
                        .orElse(null);
            }

            if (arac.getKategori() == AracKategori.PUBLIC_TRANSPORT && arac.duraklaUyumluMu(sonrakiDurak)) {
                return arac;
            }
        }

        return araclar.values().stream()
                .filter(arac -> arac instanceof Taxi)
                .findFirst()
                .orElseGet(() -> {
                    // Eğer Taksi bulunamazsa ilk mevcut aracı döndür
                    return araclar.values().stream().findFirst().orElse(null);
                });
    }

    /**
     * En yakın durakları yalnızca belirtilen public transport türlerinden bulur.
     *
     * @param konum                             Başlangıç veya varış konumu
     * @param duraklar                          Tüm duraklar listesi
     * @param izinVerilenPublicTransportTipleri İzin verilen public transport türleri
     * @return En yakın durak
     */
    private Durak enYakinDurak(Konum konum, List<Durak> duraklar, List<String> izinVerilenPublicTransportTipleri) {
        Durak enYakin = null;
        double minMesafe = Double.MAX_VALUE;
        for (Durak d : duraklar) {
            // Yalnızca izin verilen public transport türlerini kontrol et
            if (izinVerilenPublicTransportTipleri.contains(d.getType())) {
                double mesafe = MesafeHesaplayici.haversine(konum.getLat(), konum.getLon(), d.getLat(), d.getLon());
                if (mesafe < minMesafe) {
                    minMesafe = mesafe;
                    enYakin = d;
                }
            }
        }
        return enYakin;
    }

    private Rota rotaOlustur(List<Durak> yol, Yolcu yolcu, OdemeYontemi odemeYontemi, List<Arac> kullanilanAraclar) {
        // UcretHesaplayici sınıfını kullanarak toplam değerleri hesapla
        double[] sonuclar = ucretHesaplayici.hesaplaRotaToplamDegerleri(yol, yolcu, odemeYontemi, kullanilanAraclar);
        double toplamUcret = sonuclar[0];
        double toplamSure = sonuclar[1];
        double toplamMesafe = sonuclar[2];

        Rota rota = new Rota(new ArrayList<>(yol), toplamMesafe, toplamSure, toplamUcret);
        rota.setKullanilanAraclar(new ArrayList<>(kullanilanAraclar));
        return rota;
    }
}