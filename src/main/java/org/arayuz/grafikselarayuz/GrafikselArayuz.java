package org.arayuz.grafikselarayuz;

import org.model.*;
import org.denetleyici.RotaOptimizasyonTipi;
import org.model.arac.Arac;
import org.model.arac.AracKategori;
import org.model.arac.Yurume;
import org.model.odeme.OdemeYontemi;
import org.model.yolcu.Yolcu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GrafikselArayuz extends JFrame {

    private Map<String, Supplier<Yolcu>> yolcuFactory;
    private Map<String, Supplier<OdemeYontemi>> odemeFactory;
    private JComboBox<String> yolcuTipiCombo;
    private JComboBox<String> odemeTipiCombo;
    private JComboBox<RotaOptimizasyonTipi> optimizasyonTipiCombo;

    private JTextField baslangicLatField;
    private JTextField baslangicLonField;
    private JTextField varisLatField;
    private JTextField varisLonField;
    private HaritaPanel haritaPanel;

    private Map<String, Arac> araclar;
    private List<JCheckBox> onDemandCheckBoxes;
    private List<JCheckBox> publicTransportCheckBoxes;

    private JPanel rotaListePanel;
    private JButton rotaOlusturButton;
    private JButton sifirlaButton;
    private List<Rota> rotalar;

    private List<Arac> onDemandAraclar;
    private List<Arac> topluTasimaAraclar;

    private RotaBilgileri rotaBilgileri = null;
    private List<Durak> duraklar;
    private Map<String, Durak> durakMap = new HashMap<>();
    private Runnable rotaOlusturListener;

    public GrafikselArayuz(Map<String, Supplier<Yolcu>> yolcuFactory, Map<String, Supplier<OdemeYontemi>> odemeFactory, Map<String, Arac> araclar) {
        super("Ekomobil2");
        this.yolcuFactory = yolcuFactory;
        this.odemeFactory = odemeFactory;
        this.araclar = araclar;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        haritaPanel = new HaritaPanel();

        //Arayüzdeki paneller
        JPanel mainControlPanel = new JPanel(new BorderLayout());

        mainControlPanel.add(createControlPanel(), BorderLayout.NORTH);
        mainControlPanel.add(createVehicleSelectionPanel(), BorderLayout.CENTER);

        add(mainControlPanel, BorderLayout.NORTH);
        add(new JScrollPane(haritaPanel), BorderLayout.CENTER);
        add(createRotaListePanel(), BorderLayout.WEST);

        addControlButtons();
        setupListeners();
    }

    private void addControlButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        sifirlaButton = new JButton("Sıfırla");
        rotaOlusturButton = new JButton("Rota Oluştur");

        sifirlaButton.addActionListener(e -> resetForm());

        buttonPanel.add(rotaOlusturButton);
        buttonPanel.add(sifirlaButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JScrollPane createRotaListePanel() {
        rotaListePanel = new JPanel();

        rotaListePanel.setLayout(new BoxLayout(rotaListePanel, BoxLayout.Y_AXIS));
        rotaListePanel.setBorder(BorderFactory.createTitledBorder("Önerilen Rotalar"));
        rotaListePanel.setBackground(Color.ORANGE);

        // Bu, bileşenin alt öğelerini yeniden boyutlandırmaya çalışmasını önleyecektir
        rotaListePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(rotaListePanel);
        scrollPane.setPreferredSize(new Dimension(300, 800));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    public void setRotalar(List<Rota> rotalar) {
        this.rotalar = rotalar;
        updateRotaListesi();
    }

    private void updateRotaListesi() {
        rotaListePanel.removeAll();

        if (rotalar == null || rotalar.isEmpty()) {
            JLabel noRouteLabel = new JLabel("Rota bulunamadı");
            noRouteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            rotaListePanel.add(noRouteLabel);
        } else {
            //Dikey yerleştirme için panel düzeni BoxLayout olarak kalmalıdır
            for (Rota rota : rotalar) {
                JPanel rotaPanel = createRotaPanel(rota);
                rotaListePanel.add(rotaPanel);

                //Rota panelleri arasına ufak boşluklar ekle
                rotaListePanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        rotaListePanel.revalidate();
        rotaListePanel.repaint();
    }

    private JPanel createRotaPanel(Rota rota) {
        JPanel panel = new JPanel(new BorderLayout());

        //Paneldeki rota yazılarının şeklinin tutarlılığı için panel boyut ayarlama
        panel.setMinimumSize(new Dimension(280, 80));
        panel.setPreferredSize(new Dimension(280, 80));

        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setBackground(new Color(255, 255, 255));

        // Bulunan rotaların başlangıç araçlarına göre ekranın solunda renkli işaret koyar
        Color rotaRengi;
        Arac aracTipi = rota.getKullanilanAraclar().getFirst();

        if (aracTipi.getKategori() == AracKategori.ON_DEMAND) {
            if(aracTipi instanceof Yurume)
                rotaRengi = Color.BLACK;
            else
                rotaRengi = Color.YELLOW;
        }
        else {
            rotaRengi = Color.blue; // Varsayılan renk (toplu taşıma için)
        }

        JLabel bilgiLabel = new JLabel("<html><b>" + rota.aracBilgisiGetir() + "</b><br>" +
                String.format("Toplam: %.2f TL, %.2f dk, %.2f km",
                        rota.getToplamUcret(), rota.getToplamSure(), rota.getToplamMesafe()));

        // Bilgi etiketinin arka plan rengini ayarla
        JPanel colorPanel = new JPanel();

        colorPanel.setBackground(rotaRengi);
        colorPanel.setPreferredSize(new Dimension(20, 20));

        JPanel contentPanel = new JPanel(new BorderLayout());

        contentPanel.add(colorPanel, BorderLayout.WEST);
        contentPanel.add(bilgiLabel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                haritaPanel.setSeciliRota(rota);
                haritaPanel.repaint();
            }
        });
        return panel;
    }

    private JPanel createControlPanel() {
        // Ana panel, üstte koordinat girişleri, altta seçim kutularını içerecek
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.DARK_GRAY);

        // Koordinat girişleri için 2x4 grid panel
        JPanel koordinatPanel = new JPanel(new GridLayout(2, 4, 10, 10));

        koordinatPanel.setBorder(BorderFactory.createTitledBorder("Konum Bilgileri"));
        koordinatPanel.setBackground(Color.ORANGE);

        // Seçenekler için panel
        JPanel optionsPanel = new JPanel(new GridLayout(1, 6, 10, 10));

        optionsPanel.setBorder(BorderFactory.createTitledBorder("Tercihler"));
        optionsPanel.setBackground(Color.orange);

        // Alanları oluştur
        yolcuTipiCombo = createComboBox(yolcuFactory.keySet());
        odemeTipiCombo = createComboBox(odemeFactory.keySet());

        // Optimizasyon tipi combobox'ını oluştur
        optimizasyonTipiCombo = new JComboBox<>(RotaOptimizasyonTipi.values());
        optimizasyonTipiCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof RotaOptimizasyonTipi) {
                    RotaOptimizasyonTipi tip = (RotaOptimizasyonTipi) value;
                    switch (tip) {
                        case MESAFE:
                            setText("En Kısa Mesafe");
                            break;
                        case SURE:
                            setText("En Hızlı Rota");
                            break;
                        case UCRET:
                            setText("En Ekonomik Rota");
                            break;
                    }
                }
                return this;
            }
        });

        // Varsayılan olarak seç
        optimizasyonTipiCombo.setSelectedItem(RotaOptimizasyonTipi.UCRET);
        yolcuTipiCombo.setSelectedItem("Genel Yolcu");
        odemeTipiCombo.setSelectedItem("KentKart");

        baslangicLatField = new JTextField();
        baslangicLonField = new JTextField();
        varisLatField = new JTextField();
        varisLonField = new JTextField();

        // Koordinat paneline elemanları ekle
        koordinatPanel.add(new JLabel("Başlangıç Enlem:"));
        koordinatPanel.add(baslangicLatField);
        koordinatPanel.add(new JLabel("Başlangıç Boylam:"));
        koordinatPanel.add(baslangicLonField);

        koordinatPanel.add(new JLabel("Varış Enlem:"));
        koordinatPanel.add(varisLatField);
        koordinatPanel.add(new JLabel("Varış Boylam:"));
        koordinatPanel.add(varisLonField);

        // Seçim paneline etiketleri ve combobox'ları ekle
        optionsPanel.add(new JLabel("Yolcu Tipi:"));
        optionsPanel.add(yolcuTipiCombo);
        optionsPanel.add(new JLabel("Ödeme Türü:"));
        optionsPanel.add(odemeTipiCombo);
        optionsPanel.add(new JLabel("Sırala:"));
        optionsPanel.add(optimizasyonTipiCombo);

        // Ana panele parçaları ekle
        mainPanel.add(koordinatPanel, BorderLayout.NORTH);
        mainPanel.add(optionsPanel, BorderLayout.CENTER);

        return mainPanel;
    }
    //Yolcu ve ödeme hashMaplerindeki tüm keyleri ComboBox a otomatik şekilde koyar
    private JComboBox<String> createComboBox(Set<String> keys) {
        return new JComboBox<>(keys.toArray(new String[0]));
    }

    private JPanel createVehicleSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(Color.darkGray);

        // On-Demand Araçlar
        JPanel onDemandPanel = new JPanel();
        onDemandPanel.setBorder(BorderFactory.createTitledBorder("Ulaşım Yöntemleri"));
        onDemandPanel.setBackground(Color.yellow);
        onDemandPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Toplu Taşıma Araçları
        JPanel publicTransportPanel = new JPanel();
        publicTransportPanel.setBorder(BorderFactory.createTitledBorder("Toplu Taşıma Araçları"));
        publicTransportPanel.setBackground(Color.CYAN);
        publicTransportPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        //Araçları türüne göre filtrele
        onDemandAraclar = new ArrayList<>();
        topluTasimaAraclar = new ArrayList<>();

        for (Map.Entry<String, Arac> entry : araclar.entrySet()) {
            Arac arac = entry.getValue();
            if (arac.getKategori() == AracKategori.ON_DEMAND) {
                onDemandAraclar.add(arac);
            } else {
                topluTasimaAraclar.add(arac);
            }
        }

        onDemandCheckBoxes = new ArrayList<>();
        publicTransportCheckBoxes = new ArrayList<>();

        // on-demand araçları için checkboxları oluştur
        for (Arac arac : onDemandAraclar) {
            JCheckBox checkBox = new JCheckBox(arac.getDisplayName(), true);
            checkBox.setActionCommand(arac.getType());
            onDemandPanel.add(checkBox);
            onDemandCheckBoxes.add(checkBox);
        }

        //Toplu taşıma araçları için checkboxları oluştur
        for (Arac arac : topluTasimaAraclar) {
            JCheckBox checkBox = new JCheckBox(arac.getDisplayName(), true);
            checkBox.setActionCommand(arac.getType());
            publicTransportPanel.add(checkBox);
            publicTransportCheckBoxes.add(checkBox);
        }

        panel.add(onDemandPanel);
        panel.add(publicTransportPanel);

        return panel;
    }

    private void setupListeners() {
        haritaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //Eğer durak seçilmez ise handleDurakClick metodu sonunda handleMapClick metoduna gider
                handleDurakClick(e);
            }
        });

        rotaOlusturButton.addActionListener(e -> {
            handleRouteCreation();
            // Rota oluşturma işlemi tamamlandığında listener'ı çağır
            if (rotaOlusturListener != null && rotaBilgileri != null) {
                rotaOlusturListener.run();
            }
        });
    }

    // Haritada konum olarak durak seçme
    private void handleDurakClick(MouseEvent e) {
        Point clickPoint = e.getPoint();

        Durak clickedDurak = null;
        double minDistance = 15; // Durağa tıklama olarak algılanması için resim pixel eşiği

        for (Durak durak : duraklar) {
            Point durakPoint = haritaPanel.convertCoordinatesToPoint(durak.getLat(), durak.getLon());

            double distance = clickPoint.distance(durakPoint);
            if (distance < minDistance) {
                clickedDurak = durak;
                minDistance = distance;
                break;
            }
        }

        // Durağa tıklanırsa
        if (clickedDurak != null) {
            final Durak selectedDurak = clickedDurak;

            if (baslangicLatField.getText().isEmpty()) {

                baslangicLatField.setText(String.format("%.5f", selectedDurak.getLat()));
                baslangicLonField.setText(String.format("%.5f", selectedDurak.getLon()));

                Point durakPoint = haritaPanel.convertCoordinatesToPoint(selectedDurak.getLat(), selectedDurak.getLon());
                haritaPanel.setStartPoint(durakPoint);
            } else {

                varisLatField.setText(String.format("%.5f", selectedDurak.getLat()));
                varisLonField.setText(String.format("%.5f", selectedDurak.getLon()));

                Point durakPoint = haritaPanel.convertCoordinatesToPoint(selectedDurak.getLat(), selectedDurak.getLon());
                haritaPanel.setEndPoint(durakPoint);
            }

            haritaPanel.repaint();
            return;
        }

        // Hiçbir durak seçilmediyse normal mapte tıklama işlemine devam et
        handleMapClick(e);
    }

    private void handleMapClick(MouseEvent e) {
        Point point = haritaPanel.getRelativePoint(e.getPoint());
        double[] coordinates = haritaPanel.convertPointToCoordinates(point);

        if (baslangicLatField.getText().isEmpty()) {
            baslangicLatField.setText(String.format("%.5f", coordinates[0]));
            baslangicLonField.setText(String.format("%.5f", coordinates[1]));
            // Başlangıç noktasını kaydet
            haritaPanel.setStartPoint(e.getPoint());
        } else {
            varisLatField.setText(String.format("%.5f", coordinates[0]));
            varisLonField.setText(String.format("%.5f", coordinates[1]));
            // Bitiş noktasını kaydet
            haritaPanel.setEndPoint(e.getPoint());
        }
        haritaPanel.repaint(); // Paneli yeniden çiz
    }

    private void handleRouteCreation() {
        try {
            //Önceki seçilen rotayı mapte temizle
            haritaPanel.setSeciliRota(null);
            haritaPanel.repaint();

            double baslangicLat = Double.parseDouble(baslangicLatField.getText().replace(',', '.'));
            double baslangicLon = Double.parseDouble(baslangicLonField.getText().replace(',', '.'));
            double varisLat = Double.parseDouble(varisLatField.getText().replace(',', '.'));
            double varisLon = Double.parseDouble(varisLonField.getText().replace(',', '.'));

            org.model.Konum baslangicKonum = new org.model.Konum(baslangicLat, baslangicLon);
            org.model.Konum varisKonum = new org.model.Konum(varisLat, varisLon);

            String yolcuTipi = (String) yolcuTipiCombo.getSelectedItem();
            Yolcu yolcu = createYolcu(yolcuTipi);

            String odemeTipi = (String) odemeTipiCombo.getSelectedItem();
            OdemeYontemi odeme = createOdeme(odemeTipi);

            List<String> onDemandAracTipleri = new ArrayList<>();
            for (JCheckBox cb : onDemandCheckBoxes) {
                if (cb.isSelected()) onDemandAracTipleri.add(cb.getActionCommand());
            }
            //Seçilen on-demand araçları boşsa yürümeyi varsayılan ayarla
            if (onDemandAracTipleri.isEmpty()) {
                boolean flag = false;

                for(Arac arac : onDemandAraclar) {
                    if(arac instanceof Yurume) {
                        for (JCheckBox cb : onDemandCheckBoxes) {
                            if (cb.getText().equals(arac.getDisplayName())) {
                                cb.setSelected(true);
                                onDemandAracTipleri.add(cb.getActionCommand());
                                flag = true;
                                break;
                            }
                        }
                    }
                    if(flag) break;
                }
            }

            List<String> publicTransportAracTipleri = new ArrayList<>();
            for (JCheckBox cb : publicTransportCheckBoxes) {
                if (cb.isSelected()) publicTransportAracTipleri.add(cb.getActionCommand());
            }

            rotaBilgileri = new RotaBilgileri(
                    baslangicKonum,
                    varisKonum,
                    yolcu,
                    odeme,
                    (RotaOptimizasyonTipi) optimizasyonTipiCombo.getSelectedItem(),
                    onDemandAracTipleri,
                    publicTransportAracTipleri
            );

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Geçersiz koordinat formatı!", "Hata", JOptionPane.ERROR_MESSAGE);
            rotaBilgileri = null;
        }
    }

    private Yolcu createYolcu(String yolcuTipi) {
        Supplier<Yolcu> supplier = yolcuFactory.get(yolcuTipi);
        if (supplier == null) {
            // Herhangi bir tür bulunamadıysa varsayılan GenelYolcu
            return new org.model.yolcu.GenelYolcu();
        }
        return supplier.get();
    }

    private OdemeYontemi createOdeme(String odemeTipi) {
        Supplier<OdemeYontemi> supplier = odemeFactory.get(odemeTipi);
        if (supplier == null) {
            // Herhangi bir tür bulunamadıysa varsayılan KentKart
            return new org.model.odeme.KentKart();
        }
        return supplier.get();
    }

    public void resetForm() {
        baslangicLatField.setText("");
        baslangicLonField.setText("");
        varisLatField.setText("");
        varisLonField.setText("");

        // Resetleyince varsayılan olarak ayarla
        yolcuTipiCombo.setSelectedItem("Genel Yolcu");
        odemeTipiCombo.setSelectedItem("KentKart");
        optimizasyonTipiCombo.setSelectedItem(RotaOptimizasyonTipi.UCRET);

        for (JCheckBox cb : onDemandCheckBoxes) cb.setSelected(true);
        for (JCheckBox cb : publicTransportCheckBoxes) cb.setSelected(true);
        rotaBilgileri = null;
        rotalar = null;

        // Önerilen rotalar panelini temizle
        rotaListePanel.removeAll();
        rotaListePanel.revalidate();
        rotaListePanel.repaint();

        // Harita panelini sıfırla
        if (haritaPanel != null) {
            haritaPanel.setSeciliRota(null);
            haritaPanel.resetHarita();
            haritaPanel.repaint();
        }
    }

    public void setDuraklar(List<Durak> duraklar) {
        this.duraklar = duraklar;
        haritaPanel.setDuraklar(this.duraklar);
        durakMap.clear();
        for (Durak d : duraklar) {
            durakMap.put(d.getId(), d);
        }
        haritaPanel.setDurakMap(this.durakMap);
        haritaPanel.repaint();
    }

    // Rota oluşturma işlemini dinleyici metodu
    public void setRotaOlusturListener(Runnable listener) {
        this.rotaOlusturListener = listener;
    }

    // Rota bilgilerini döndüren metot
    public RotaBilgileri getRotaBilgileri() {
        return rotaBilgileri;
    }
}