package org.model.yolcu;

public class OgrenciYolcu implements Yolcu {

    @Override
    public double getIndirimOrani() {
        return 0.25;
    }

    public double getYurumeHizi() {
        return 4.0;
    }
}
