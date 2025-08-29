package org.arayuz.grafikselarayuz;

import org.model.Durak;
import org.model.Konum;
import org.model.Rota;
import org.model.arac.Arac;
import org.model.arac.AracKategori;
import org.model.arac.Yurume;
import org.yardimci.RotaYardimcisi;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Map;

class HaritaPanel extends JPanel {
    private double minLat = Double.MAX_VALUE;
    private double maxLat = -Double.MAX_VALUE;
    private double minLon = Double.MAX_VALUE;
    private double maxLon = -Double.MAX_VALUE;
    private double scaleX, scaleY, scale;
    private double offsetX, offsetY;
    private Point startPoint;
    private Point endPoint;
    private List<Durak> duraklar;
    private Map<String, Durak> durakMap;
    private Image durakIcon;
    private Image baslangicIcon;
    private Image varisIcon;
    private Image durakRotaIcon;
    private Image backgroundMap;
    private Rota seciliRota;

    HaritaPanel() {
        setPreferredSize(new Dimension(1000, 600));
        setBackground(new Color(245, 245, 245));

        // Resimleri yükle
        try {
            durakIcon = new ImageIcon("src\\main\\resources\\textures\\durak.png").getImage();
            baslangicIcon = new ImageIcon("src\\main\\resources\\textures\\baslangic.png").getImage();
            varisIcon = new ImageIcon("src\\main\\resources\\textures\\hedef.png").getImage();
            durakRotaIcon = new ImageIcon("src\\main\\resources\\textures\\durak_rota.png").getImage();
            backgroundMap = new ImageIcon("src\\main\\resources\\textures\\izmit_harita.png").getImage();
        } catch (Exception e) {
            System.err.println("Resim yüklenirken hata: " + e.getMessage());
            durakIcon = null;
            baslangicIcon = null;
            varisIcon = null;
            durakRotaIcon = null;
            backgroundMap = null;
        }
    }

    protected void setDuraklar(List<Durak> duraklar) {
        this.duraklar = duraklar;
    }

    protected void setDurakMap(Map<String, Durak> durakMap) {
        this.durakMap = durakMap;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (duraklar == null || duraklar.isEmpty()) return;

        calculateBounds();
        calculateScale();

        // Arka plan haritasını çiz - tüm paneli kaplayacak şekilde
        if (backgroundMap != null) {
            g.drawImage(backgroundMap,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this);
        }

        drawElements((Graphics2D) g);
        Graphics2D g2d = (Graphics2D) g;

        // Başlangıç ve bitiş noktalarını çiz
        if (startPoint != null) {
            if (baslangicIcon != null) {
                int iconWidth = 32;
                int iconHeight = 32;
                g2d.drawImage(baslangicIcon, startPoint.x - iconWidth / 2, startPoint.y - iconHeight / 2, iconWidth, iconHeight, this);
            } else {
                g2d.setColor(Color.GREEN);
                g2d.fill(new Ellipse2D.Double(startPoint.x - 10, startPoint.y - 10, 20, 20));
            }
            g2d.setColor(Color.BLACK);
            g2d.drawString("Başlangıç", startPoint.x + 15, startPoint.y + 5);
        }

        if (endPoint != null) {
            if (varisIcon != null) {
                int iconWidth = 32;
                int iconHeight = 32;
                g2d.drawImage(varisIcon, endPoint.x - iconWidth / 2, endPoint.y - iconHeight / 2, iconWidth, iconHeight, this);
            } else {
                g2d.setColor(Color.RED);
                g2d.fill(new Ellipse2D.Double(endPoint.x - 10, endPoint.y - 10, 20, 20));
            }
            g2d.setColor(Color.BLACK);
            g2d.drawString("Varış", endPoint.x + 15, endPoint.y + 5);
        }

        if (seciliRota != null) {
            drawSelectedRoute(g2d);
        }
    }

    private void calculateBounds() {
        minLat = Double.MAX_VALUE;
        maxLat = -Double.MAX_VALUE;
        minLon = Double.MAX_VALUE;
        maxLon = -Double.MAX_VALUE;

        duraklar.forEach(d -> {
            minLat = Math.min(minLat, d.getLat());
            maxLat = Math.max(maxLat, d.getLat());
            minLon = Math.min(minLon, d.getLon());
            maxLon = Math.max(maxLon, d.getLon());
        });
    }

    private void calculateScale() {
        int width = getWidth();
        int height = getHeight();

        scaleX = width / (maxLon - minLon);
        scaleY = height / (maxLat - minLat);
        scale = Math.min(scaleX, scaleY) * 0.9;

        offsetX = (width - (maxLon - minLon) * scale) / 2;
        offsetY = (height - (maxLat - minLat) * scale) / 2;
    }

    private void drawElements(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        duraklar.forEach(durak -> durak.getNextStops().forEach(next -> {
            Durak target = durakMap.get(next.getStopId());
            if (target != null) {
                drawConnection(g2d, durak, target, false);
            }
        }));

        duraklar.forEach(durak -> {
            if (durak.getTransfer() != null) {
                Durak transferDurak = durakMap.get(durak.getTransfer().getTransferStopId());
                if (transferDurak != null) {
                    drawTransferConnection(g2d, durak, transferDurak);
                }
            }
        });

        duraklar.forEach(durak -> drawStop(g2d, durak));
    }

    private void drawConnection(Graphics2D g2d, Durak source, Durak target, boolean isTransfer) {
        Point p1 = convertCoordinatesToPoint(source.getLat(), source.getLon());
        Point p2 = convertCoordinatesToPoint(target.getLat(), target.getLon());

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.BLUE);
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    // Transfer bağlantıları için ayrı metot
    private void drawTransferConnection(Graphics2D g2d, Durak source, Durak target) {
        Point p1 = convertCoordinatesToPoint(source.getLat(), source.getLon());
        Point p2 = convertCoordinatesToPoint(target.getLat(), target.getLon());

        float[] dash = {5.0f, 8.0f};
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2d.setColor(Color.CYAN);
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void drawStop(Graphics2D g2d, Durak durak) {
        Point p = convertCoordinatesToPoint(durak.getLat(), durak.getLon());

        if (durakIcon != null) {
            int iconWidth = 24;
            int iconHeight = 24;
            g2d.drawImage(durakIcon, p.x - iconWidth / 2, p.y - iconHeight / 2, iconWidth, iconHeight, this);
        } else {
            g2d.setColor(new Color(200, 50, 50));
            g2d.fill(new Ellipse2D.Double(p.x - 8, p.y - 8, 16, 16));
        }

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(durak.getId(), p.x + 10, p.y + 5);
    }

    private void drawArrow(Graphics2D g2d, Point start, Point end, boolean isTransfer) {
        double angle = Math.atan2(end.y - start.y, end.x - start.x);
        int arrowSize = 15;
        int midX = (start.x + end.x) / 2;
        int midY = (start.y + end.y) / 2;

        Polygon arrow = new Polygon();
        arrow.addPoint(midX, midY);
        arrow.addPoint((int) (midX - arrowSize * Math.cos(angle - Math.PI / 6)),
                (int) (midY - arrowSize * Math.sin(angle - Math.PI / 6)));
        arrow.addPoint((int) (midX - arrowSize * Math.cos(angle + Math.PI / 6)),
                (int) (midY - arrowSize * Math.sin(angle + Math.PI / 6)));

        if (isTransfer) {
            g2d.setColor(Color.BLACK);
        }
        g2d.fill(arrow);
    }

    protected Point convertCoordinatesToPoint(double lat, double lon) {
        int x = (int) (offsetX + (lon - minLon) * scale);
        int y = (int) (offsetY + (maxLat - lat) * scale);
        return new Point(x, y);
    }

    public Point getRelativePoint(Point p) {
        return new Point(p.x - (int) offsetX, p.y - (int) offsetY);
    }

    public double[] convertPointToCoordinates(Point p) {
        double lon = minLon + (p.x / scale);
        double lat = maxLat - (p.y / scale);
        return new double[]{lat, lon};
    }

    public void setStartPoint(Point point) {
        this.startPoint = point;
    }

    public void setEndPoint(Point point) {
        this.endPoint = point;
    }

    public void setSeciliRota(Rota rota) {
        this.seciliRota = rota;
    }

    // Yeni eklenen metot: OSRM'den alınan rota koordinatlarını kullanarak çizim yapar.
    private void drawRealRoute(Graphics2D g2d, double startLat, double startLon, double endLat, double endLon, Color color) {
        // RoutingHelper sınıfı Jackson kullanarak OSRM API çağrısı yapıyor
        List<double[]> routeCoordinates = RotaYardimcisi.getRouteCoordinates(startLat, startLon, endLat, endLon);
        if (routeCoordinates.isEmpty()) {
            // Eğer rota alınamazsa, varsayılan düz çizgi çiz
            Point p1 = convertCoordinatesToPoint(startLat, startLon);
            Point p2 = convertCoordinatesToPoint(endLat, endLon);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            return;
        }

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < routeCoordinates.size() - 1; i++) {
            double[] coord1 = routeCoordinates.get(i);
            double[] coord2 = routeCoordinates.get(i + 1);
            Point p1 = convertCoordinatesToPoint(coord1[0], coord1[1]);
            Point p2 = convertCoordinatesToPoint(coord2[0], coord2[1]);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void drawDashedLine(Graphics2D g2d, Point p1, Point p2, Color color) {
        float[] dash = {10.0f};
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2d.setColor(color);
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void drawPoint(Graphics2D g2d, Point p, Color color, String label) {
        if (label.equals("Başlangıç") && baslangicIcon != null) {
            int iconWidth = 32;
            int iconHeight = 32;
            g2d.drawImage(baslangicIcon, p.x - iconWidth / 2, p.y - iconHeight / 2, iconWidth, iconHeight, this);
        } else if (label.equals("Varış") && varisIcon != null) {
            int iconWidth = 32;
            int iconHeight = 32;
            g2d.drawImage(varisIcon, p.x - iconWidth / 2, p.y - iconHeight / 2, iconWidth, iconHeight, this);
        } else {
            g2d.setColor(color);
            g2d.fill(new Ellipse2D.Double(p.x - 10, p.y - 10, 20, 20));
        }
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, p.x + 15, p.y + 5);
    }

    private void drawSelectedRoute(Graphics2D g2d) {
        Konum baslangic = seciliRota.getBaslangicKonum();
        Konum varis = seciliRota.getHedefKonum();

        Point startPoint = convertCoordinatesToPoint(baslangic.getLat(), baslangic.getLon());
        Point endPoint = convertCoordinatesToPoint(varis.getLat(), varis.getLon());
        drawPoint(g2d, startPoint, Color.GREEN, "Başlangıç");
        drawPoint(g2d, endPoint, Color.RED, "Varış");

        List<Durak> yol = seciliRota.getYol();

        // İlk ve son kullanılan araçları belirle
        Arac ilkArac = seciliRota.getKullanilanAraclar().getFirst();
        Arac sonArac = seciliRota.getKullanilanAraclar().getLast();

        // Eğer yol durakları boşsa, başlangıç ve varış noktaları arasında gerçek rota çizimi yap
        if (yol == null || yol.isEmpty()) {
            if (ilkArac.getKategori() == AracKategori.ON_DEMAND) {
                Color routeColor = (ilkArac instanceof Yurume) ? Color.BLACK : Color.YELLOW;
                drawRealRoute(g2d, baslangic.getLat(), baslangic.getLon(), varis.getLat(), varis.getLon(), routeColor);
                //drawArrow(g2d, startPoint, endPoint, false);
            }
            return;
        }

        // Durakları özel sembol ile çiz
        yol.forEach(durak -> {
            Point p = convertCoordinatesToPoint(durak.getLat(), durak.getLon());
            if (durakRotaIcon != null) {
                int iconWidth = 24;
                int iconHeight = 24;
                g2d.drawImage(durakRotaIcon, p.x - iconWidth / 2, p.y - iconHeight / 2, iconWidth, iconHeight, this);
            } else {
                g2d.setColor(new Color(50, 200, 50));
                g2d.fill(new Ellipse2D.Double(p.x - 10, p.y - 10, 20, 20));
            }
        });

        // Başlangıçtan ilk durak arasındaki çizgi
        Durak ilkDurak = yol.get(0);
        Point firstStopPoint = convertCoordinatesToPoint(ilkDurak.getLat(), ilkDurak.getLon());

        if (ilkArac.getKategori() == AracKategori.ON_DEMAND) {
            Color routeColor = (ilkArac instanceof Yurume) ? Color.BLACK : Color.YELLOW;
            drawRealRoute(g2d, baslangic.getLat(), baslangic.getLon(), ilkDurak.getLat(), ilkDurak.getLon(), routeColor);
            //drawArrow(g2d, startPoint, firstStopPoint, false);
        }

        // Duraklar arası kalın kırmızı çizgi ve oklar
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(4));
        for (int i = 0; i < yol.size() - 1; i++) {
            Durak current = yol.get(i);
            Durak next = yol.get(i + 1);
            Point p1 = convertCoordinatesToPoint(current.getLat(), current.getLon());
            Point p2 = convertCoordinatesToPoint(next.getLat(), next.getLon());
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            drawArrow(g2d, p1, p2, false);
        }

        // Son durak ile hedef arasındaki çizgi
        Durak sonDurak = yol.get(yol.size() - 1);
        Point lastStopPoint = convertCoordinatesToPoint(sonDurak.getLat(), sonDurak.getLon());

        if (sonArac.getKategori() == AracKategori.ON_DEMAND) {
            Color routeColor = (sonArac instanceof Yurume) ? Color.BLACK : Color.YELLOW;
            drawRealRoute(g2d, sonDurak.getLat(), sonDurak.getLon(), varis.getLat(), varis.getLon(), routeColor);
            //drawArrow(g2d, lastStopPoint, endPoint, false);
        }
    }

    public void resetHarita() {
        seciliRota = null;
        startPoint = null;
        endPoint = null;
        repaint();
    }
}