package UpdatableTest.Aereo;

import Updatable.Aereo.Aereo;
import Updatable.Aereo.Posto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AereoTest {

    private Aereo aereoOriginale;

    @BeforeEach
    void setUp() {
        // Crea un'istanza di Aereo per i test
        aereoOriginale = new Aereo(12345L, "Boeing 747", 10, Aereo.TipoVeicolo.INTERNAZIONALE);
    }

    @Test
    void testClone() {
        // Clona l'oggetto
        Aereo aereoClonato = aereoOriginale.clone();

        // Verifica che il clone sia uguale all'originale
        assertEquals(aereoOriginale, aereoClonato);

        // Verifica che l'originale e il clone non siano lo stesso oggetto
        assertNotSame(aereoOriginale, aereoClonato);

        // Verifica che i campi siano clonati correttamente
        assertEquals(aereoOriginale.getNumeroSeriale(), aereoClonato.getNumeroSeriale());
        assertEquals(aereoOriginale.getModello(), aereoClonato.getModello());
        assertEquals(aereoOriginale.getTipo(), aereoClonato.getTipo());
        assertEquals(aereoOriginale.getPosti(), aereoClonato.getPosti());

        // Verifica che le liste di posti siano clonate correttamente
        assertNotSame(aereoOriginale.getP(), aereoClonato.getP());
        for (int i = 0; i < aereoOriginale.getP().size(); i++) {
            Posto postoOriginale = aereoOriginale.getP().get(i);
            Posto postoClonato = aereoClonato.getP().get(i);
            assertEquals(postoOriginale, postoClonato);
            assertNotSame(postoOriginale, postoClonato);
        }
    }
}

