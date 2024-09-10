package UpdatableTest.Biglietto;

import static org.junit.jupiter.api.Assertions.*;

import Updatable.Aereo.Aereo;
import Updatable.Biglietto.Biglietto;
import Updatable.Cliente.Cliente;
import Updatable.Volo.Volo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;

public class BigliettoTest {

    private Biglietto biglietto;
    private Cliente cliente;
    private Volo volo;
    private Aereo aereo;

    @BeforeEach
    void setUp() {
        aereo = new Aereo(123456789L, "Boeing 747", Aereo.TipoVeicolo.INTERNAZIONALE);
        volo = new Volo("Roma", "New York", LocalTime.of(10, 30), LocalDate.of(2024, 9, 15), aereo);
        cliente = new Cliente("email@test.com", "Mario", "Rossi", "Via Roma 1", LocalDate.of(1980, 5, 15));
        biglietto = new Biglietto(cliente, "Luca", "Bianchi", volo, 150.0, "economy");
    }

    @Test
    void testGetId() {
        assertEquals(0, biglietto.getId());
    }

    @Test
    void testGetCliente() {
        assertEquals(cliente, biglietto.getCliente());
    }

    @Test
    void testGetNomeBeneficiario() {
        assertEquals("Luca", biglietto.getNomeBeneficiario());
    }

    @Test
    void testGetCognomeBeneficiario() {
        assertEquals("Bianchi", biglietto.getCognomeBeneficiario());
    }

    @Test
    void testGetVolo() {
        assertEquals(volo, biglietto.getVolo());
    }

    @Test
    void testGetPrezzo() {
        assertEquals(150.0, biglietto.getPrezzo());
    }

    @Test
    void testGetTariffa() {
        assertEquals("economy", biglietto.getTariffa());
    }

    @Test
    void testCalcolaTassaModifica() {
        assertEquals(20.0, biglietto.calcolaTassaModifica());
        biglietto = new Biglietto(cliente, "Luca", "Bianchi", volo, 150.0, "business");
        assertEquals(5.0, biglietto.calcolaTassaModifica());
    }

    @Test
    void testModificaDataOrario() {
        Volo nuovoVolo = new Volo("Roma", "Tokyo", LocalTime.of(12, 45), LocalDate.of(2024, 9, 18), aereo);
        biglietto.modificaDataOrario(nuovoVolo, "Luca", "Bianchi");
        assertEquals(nuovoVolo, biglietto.getVolo());
    }

    @Test
    void testClone() {
        Biglietto clone = biglietto.clone();
        assertEquals(biglietto, clone);
        assertNotSame(biglietto, clone);
    }

    @Test
    void testEquals() {
        Biglietto sameBiglietto = new Biglietto(cliente, "Luca", "Bianchi", volo, 150.0, "economy");
        assertTrue(biglietto.equals(sameBiglietto));
        Biglietto differentBiglietto = new Biglietto(cliente, "Luca", "Verdi", volo, 150.0, "economy");
        assertFalse(biglietto.equals(differentBiglietto));
    }

    @Test
    void testHashCode() {
        Biglietto sameBiglietto = new Biglietto(cliente, "Luca", "Bianchi", volo, 150.0, "economy");
        assertEquals(biglietto.hashCode(), sameBiglietto.hashCode());
    }

    @Test
    void testToString() {
        String expected = "Biglietto{id=0, nomeBeneficiario='Luca', cognomeBeneficiario='Bianchi', orario=10:30, data=2024-09-15, prezzo=150.0}";
        assertEquals(expected, biglietto.toString());
    }
}