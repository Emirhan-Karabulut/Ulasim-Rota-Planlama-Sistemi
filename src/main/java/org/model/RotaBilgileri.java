package org.model;

import org.denetleyici.RotaOptimizasyonTipi;
import org.model.yolcu.Yolcu;
import org.model.odeme.OdemeYontemi;

import java.util.List;

public class RotaBilgileri {
    private Konum baslangicKonum;
    private Konum varisKonum;
    private Yolcu yolcu;
    private OdemeYontemi odeme;
    private List<String> izinVerilenOnDemandAracTipleri;
    private List<String> izinVerilenPublicTransportAracTipleri;

    private RotaOptimizasyonTipi optimizasyonTipi;

    public RotaBilgileri(Konum baslangicKonum, Konum varisKonum, Yolcu yolcu, OdemeYontemi odeme, RotaOptimizasyonTipi optimizasyonTipi,
                         List<String> onDemandAracTipleri, List<String> publicTransportAracTipleri) {
        this.baslangicKonum = baslangicKonum;
        this.varisKonum = varisKonum;
        this.yolcu = yolcu;
        this.odeme = odeme;
        this.optimizasyonTipi = optimizasyonTipi;
        this.izinVerilenOnDemandAracTipleri = onDemandAracTipleri;
        this.izinVerilenPublicTransportAracTipleri = publicTransportAracTipleri;
    }

    // Getter metodları
    public List<String> getizinVerilenOnDemandAracTipleri() {
        return izinVerilenOnDemandAracTipleri;
    }

    public List<String> getizinVerilenPublicTransportAracTipleri() {
        return izinVerilenPublicTransportAracTipleri;
    }

    public Konum getBaslangicKonum() {
        return baslangicKonum;
    }

    public Konum getVarisKonum() {
        return varisKonum;
    }

    public Yolcu getYolcu() {
        return yolcu;
    }

    public OdemeYontemi getOdeme() {
        return odeme;
    }

    public RotaOptimizasyonTipi getOptimizasyonTipi() {return optimizasyonTipi;}
}
