package org.model.yolcu;

public class YasliYolcu implements Yolcu {

    @Override
    public double getIndirimOrani() {
        return 0.50;
    }

    public double getYurumeHizi() {
        return 2.0;
    }
}

