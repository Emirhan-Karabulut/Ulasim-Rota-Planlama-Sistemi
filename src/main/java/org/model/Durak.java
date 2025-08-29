package org.model;

import java.util.List;

public class Durak {
    private String id;
    private String name;
    private String type;
    private double lat;
    private double lon;
    private boolean sonDurak;
    private List<SonrakiDurak> sonrakiDuraks;
    private Aktarma transfer;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLon() {
        return lon;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public boolean isSonDurak() {
        return sonDurak;
    }
    public void setSonDurak(boolean sonDurak) {
        this.sonDurak = sonDurak;
    }
    public List<SonrakiDurak> getNextStops() {
        return sonrakiDuraks;
    }
    public void setNextStops(List<SonrakiDurak> sonrakiDuraks) {
        this.sonrakiDuraks = sonrakiDuraks;
    }
    public Aktarma getTransfer() {
        return transfer;
    }
    public void setTransfer(Aktarma transfer) {
        this.transfer = transfer;
    }
}

