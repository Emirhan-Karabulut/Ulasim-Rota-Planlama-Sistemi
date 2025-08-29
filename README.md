# Ulaşım Rota Planlama Sistemi

## Screenshots
<table>
  <tr>
    <td>Ana Arayüz ve Konum Seçimi</td>
    <td>Rota Planı ve Seçenekler</td>
    <td>Rota Detayları ve Maliyet Analizi</td>
  </tr>
  <tr>
    <td><img src="screenshots/ss1.jpeg" alt="Ana Arayüz ve Konum Seçimi" width="300px"></td>
    <td><img src="screenshots/ss2.jpeg" alt="Rota Planı ve Seçenekler" width="300px"></td>
    <td><img src="screenshots/ss3.jpeg" alt="Rota Detayları ve Maliyet Analizi" width="300px"></td>
  </tr>
</table>

Bu proje, Kocaeli İli İzmit İlçesi kapsamında entegre bir toplu taşıma ve taksi sistemi tasarlayarak, kullanıcının mevcut konumundan belirli bir hedef noktaya en uygun rota ile ulaşmasını sağlamayı amaçlamaktadır. Rota belirleme sürecinde maliyet, süre ve aktarma sayısı gibi faktörler dikkate alınarak kullanıcıya en verimli güzergah önerileri sunulmaktadır.

## Proje Özeti

- **Tema:** Çoklu ulaşım modlarını entegre eden akıllı rota planlama sistemi
- **Temel İşlevler:**
  - JSON formatında durak ve hat verilerinden otobüs-tramvay bağlantılarının modellenmesi
  - Kullanıcı konumuna en yakın durak tespiti ve yürüme mesafelerinin hesaplanması
  - Aktarma noktalarının belirlenmesi ve optimizasyon stratejilerinin uygulanması
  - Kilometre bazlı fiyatlandırma algoritması ile taksi entegrasyonu
  - Öğrenci, öğretmen ve 65 yaş üstü bireyler için özel indirim sistemleri
  - Mesafe, süre ve maliyet kriterlerine göre optimizasyon seçenekleri
  - Hibrit rotalar (taksi + toplu taşıma kombinasyonları) sunumu
  - Gerçek zamanlı API entegrasyonu ile doğru mesafe ve süre hesaplamaları

Sistem, DFS (Depth-First Search) algoritması kullanarak çoklu ulaşım modlarını kombine eden optimal rotalar bulur ve kullanıcı tercihlerine göre sıralama yapar.

## Kullanılan Teknolojiler ve Yöntemler

- **Java:** Nesne yönelimli programlama prensiplerine uygun modüler sistem mimarisi
- **SOLID İlkeleri:** Tek sorumluluk, açık-kapalı, arayüz ayrımı prensiplerinin uygulanması
- **Tasarım Desenleri:**
  - Strategy Pattern (yolcu tipleri ve ödeme yöntemleri için)
  - Factory Pattern (nesne yaratım süreçlerinde)
  - MVC Pattern (arayüz-veri katmanı ayrımı)
- **Algoritma ve Veri Yapıları:**
  - Depth-First Search (DFS) graf tarama algoritması
  - Haversine formülü ile coğrafi koordinat hesaplamaları
  - Ağırlıklı graf yapısı ile durak bağlantılarının modellenmesi
- **API Entegrasyonu:** Open Route Service (ORS) API ile gerçek dünya yol ağları
- **Veri Yönetimi:** JSON formatında yapılandırılmış durak ve rota verileri
- **Kullanıcı Arayüzü:** Java Swing ile masaüstü GUI uygulaması

## İş Dünyasına Yönelik Kazanımlar

- **Proje Yönetimi ve Sistem Analizi:** Karmaşık bir gerçek dünya problemini tanımlayıp, çözüm odaklı yaklaşımla sistemli bir şekilde çözdüm.
- **Yazılım Mimarisi Tasarımı:** Büyük ölçekli projelerde sürdürülebilir, genişletilebilir kod yapıları kurma deneyimi kazandım.
- **Veri Analizi ve Optimizasyon:** Çoklu kriterleri değerlendirerek optimal çözümler üretme, performans analizi yapma becerim gelişti.
- **API Entegrasyonu ve Dış Servis Yönetimi:** Harici sistemlerle entegrasyon kurarak dinamik veri işleme konusunda pratik deneyim edindim.
- **Kullanıcı Deneyimi Odaklı Geliştirme:** Farklı kullanıcı tiplerinin ihtiyaçlarını analiz ederek kişiselleştirilmiş çözümler sunma yeteneğimi geliştirdim.
- **Problem Çözme ve Analitik Düşünme:** Graf teorisi ve optimizasyon algoritmalarıyla kompleks sistemleri modelleyerek etkili çözümler ürettim.

## Öne Çıkan Özellikler

- **Çoklu Ulaşım Modu Desteği:** Yürüyüş, otobüs, tramvay ve taksi kombinasyonlarıyla hibrit rota çözümleri
- **Akıllı Optimizasyon Sistemi:** Süre, mesafe ve maliyet kriterlerine göre ağırlıklı değerlendirme ve sıralama
- **Dinamik Fiyatlandırma:** Yolcu tipi ve ödeme yöntemine göre gerçek zamanlı ücret hesaplama ve indirim mekanizmaları
- **İleri Düzey Algoritma Uygulaması:** DFS graf taraması, en kısa yol algoritmaları ve ağırlıklı graf yapıları
- **Gerçek Zamanlı API Entegrasyonu:** Open Route Service ile canlı mesafe ve süre hesaplamaları
- **Kullanıcı Dostu Arayüz:** Java Swing ile etkileşimli masaüstü uygulaması ve detaylı analiz raporları
- **Performans Optimizasyonu:** Önbellekleme stratejileri ve lazy-loading ile hızlı sistem yanıt süreleri

## Örnek Deneysel Rota Sonuçları

- **ROTA #1 (Ekonomik Seçenek)**
  - Araçlar: Yürüme → Otobüs → Tramvay → Yürüme
  - Toplam Mesafe: **2.34 km**
  - Toplam Süre: **31.8 dk**
  - Toplam Ücret: **3.00 TL**

- **ROTA #2 (Dengeli Seçenek)**
  - Araçlar: Yürüme → Otobüs → Aktarma → Tramvay → Yürüme
  - Toplam Mesafe: **6.64 km**
  - Toplam Süre: **43.8 dk**
  - Toplam Ücret: **5.00 TL**

- **ROTA #3 (Hızlı Seçenek)**
  - Araçlar: Taksi
  - Toplam Mesafe: **3.43 km**
  - Toplam Süre: **2.57 dk**
  - Toplam Ücret: **23.73 TL**

## Sistem Özellikleri

- **Çoklu Optimizasyon:** Mesafe, süre ve maliyet kriterlerine göre farklı rota seçenekleri
- **Akıllı Aktarma Sistemi:** Otobüs-tramvay arası geçişlerde optimal transfer noktaları
- **Dinamik Fiyatlandırma:** Farklı yolcu tipleri ve ödeme yöntemlerine göre ücret hesaplama
- **Hibrit Çözümler:** 3 km üzerindeki mesafeler için taksi + toplu taşıma kombinasyonları
- **Gerçek Veri Entegrasyonu:** API tabanlı mesafe ve süre hesaplamaları
- **Kullanıcı Dostu Arayüz:** Grafiksel ve metin tabanlı rota gösterimleri

## Sonuç

Bu projede, modern yazılım mühendisliği prensipleriyle gerçek dünya problemine kapsamlı bir çözüm geliştirdim. Çoklu ulaşım modlarının entegrasyonu, algoritma tasarımı ve kullanıcı deneyimi optimizasyonu konularında hem teknik hem de analitik yönden kendimi ileriye taşıdım. Sistem mimarisi, gelecekte farklı şehirlere veya yeni ulaşım modlarına kolaylıkla adapte edilebilecek esneklikte tasarlandı.

---

*Not: Bu proje, iş başvuruları kapsamında portföy olarak sunulmuştur. Dolayısıyla, kullanıcı kurulumu veya kullanım talimatları yerine, yapılan işin genel hatlarını ve teknolojik altyapısını vurgulamak amaçlanmıştır.*
