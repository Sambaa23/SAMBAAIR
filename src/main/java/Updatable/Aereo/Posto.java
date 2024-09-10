package Updatable.Aereo;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Posto implements Cloneable {

     @Id @ManyToOne private final Aereo aereo;
     @Id private final int fila;
     @Id private final Character posto;

    protected Posto() {
        this.aereo = null;
        this.fila = 0;
        this.posto = 'A';
    }//Costruttore di default

    public Posto(Aereo aereo, int fila, Character posto) {
        this.aereo = aereo;
        this.fila = fila;
        this.posto = Character.toUpperCase(posto);
    }//Costruttore normale

    //Getters
    public Aereo getAereo() {
        return aereo;
    }

    public int getFila() {
        return fila;
    }

    public char getPosto() {
        return posto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Posto posto1)) return false;
        return fila == posto1.fila &&
                Objects.equals(aereo, posto1.aereo) &&
                Objects.equals(posto, posto1.posto);
    } // equals

    @Override
    public int hashCode() {
        return Objects.hash(aereo, fila, posto);
    } // hashCode

    @Override
    public String toString() {
        return ""+ aereo + "fila " +fila+ " posto " + posto;
    }//toString

    @Override
    public Posto clone() {
        try {
            return (Posto) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // Gestione delle eccezioni
        }
    } // clone

}//Posto
