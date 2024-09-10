package Updatable.Aereo;

import java.io.Serializable;
import java.util.Objects;

public class AereoId implements Serializable {
    private long numeroSeriale;
    private String modello;

    public AereoId() {} //Costruttore di default

    public AereoId(long numeroSeriale, String modello) {
        this.numeroSeriale = numeroSeriale;
        this.modello = modello;
    }//Costruttore normale

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AereoId that)) return false;
        return numeroSeriale == that.numeroSeriale && Objects.equals(modello, that.modello);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(numeroSeriale, modello);
    }//hashCode
}//AereoId