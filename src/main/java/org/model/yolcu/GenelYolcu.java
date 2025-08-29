package org.model.yolcu;

public class GenelYolcu implements Yolcu {

    @Override
    public double getIndirimOrani() {
        return 0.0; // İndirim yok
    }
    public double getYurumeHizi() {
        return 3.0;
    }
}

