package org.yardimci;
import org.model.Durak;
import org.model.arac.Taxi;
import java.util.List;

public class JSONVeriYukleyici {
    private String city;
    private Taxi taxi;
    private List<Durak> duraklar;

    // Getter ve Setter metotları
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public Taxi getTaxi() {
        return taxi;
    }
    public void setTaxi(Taxi taxi) {
        this.taxi = taxi;
    }
    public List<Durak> getDuraklar() {
        return duraklar;
    }
    public void setDuraklar(List<Durak> duraklar) {
        this.duraklar = duraklar;
    }
}
