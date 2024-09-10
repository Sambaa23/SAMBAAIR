package UpdatableTest.Volo;


import static org.junit.jupiter.api.Assertions.*;

import Updatable.Aereo.Aereo;
import Updatable.Volo.Volo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;

public class VoloTest {

    private Volo volo;
    private Aereo aereo;

    @BeforeEach
    public void setUp() {
        // Creazione dell'aereo
        aereo = new Aereo(123456789, "Boeing 747", 300, Aereo.TipoVeicolo.INTERCONTINENTALE);

        // Creazione del volo con l'aereo associato
        volo = new Volo("FCO", "JFK", LocalTime.of(14, 0), LocalDate.of(2023, 12, 1), aereo);
    }

    @Test
    public void testCalcolaPunti() {
        int punti = volo.calcolaPunti();
        assertTrue(punti > 0); // Verifica che i punti calcolati siano un valore positivo.
    }

    @Test
    public void testDisponibilitaVolo() {
        volo.setDisponibilita(100);
        assertEquals(100, volo.getDisponibilita());
    }

    @Test
    public void testClone() {
        Volo clone = volo.clone();

        // Verifica che il clone sia uguale all'originale
        assertEquals(volo, clone);

        // Verifica che l'originale e il clone non siano lo stesso oggetto
        assertNotSame(volo, clone);

        // Verifica che il campo Aereo venga clonata correttamente se non nullo
        if (volo.getAereo() != null) {
            assertNotSame(volo.getAereo(), clone.getAereo());
        }

        // Verifica che i campi immutabili siano clonati correttamente
        assertEquals(volo.getPartenza(), clone.getPartenza());
        assertEquals(volo.getDestinazione(), clone.getDestinazione());
        assertEquals(volo.getOrario(), clone.getOrario());
        assertEquals(volo.getGiorno(), clone.getGiorno());
        assertEquals(volo.getDisponibilita(), clone.getDisponibilita());
    }
}
