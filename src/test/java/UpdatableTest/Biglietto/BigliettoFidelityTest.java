package UpdatableTest.Biglietto;

import static org.junit.jupiter.api.Assertions.*;

import Updatable.Aereo.Aereo;
import Updatable.Biglietto.BigliettoFidelity;
import Updatable.Cliente.ClienteFedelta;
import Updatable.Volo.Volo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;

public class BigliettoFidelityTest {

    private BigliettoFidelity bigliettoFidelity;
    private ClienteFedelta clienteFedelta;
    private Volo volo;
    private Aereo aereo;

    @BeforeEach
    void setUp() {
        aereo = new Aereo(123456789L, "Boeing 747", Aereo.TipoVeicolo.INTERNAZIONALE);
        volo = new Volo("FCO", "JFK", LocalTime.of(10, 30), LocalDate.of(2024, 9, 15), aereo);
        clienteFedelta = new ClienteFedelta("email@test.com", "Mario", "Rossi", "Via Roma 1", LocalDate.of(1980, 5, 15));
        bigliettoFidelity = new BigliettoFidelity(clienteFedelta, "Luca", "Bianchi", volo, 200.0, "premium");
    }

    @Test
    void testGetId() {
        assertEquals(0, bigliettoFidelity.getId());
    }


    @Test
    void testGetCliente() {
        assertEquals(clienteFedelta, bigliettoFidelity.getCliente());
    }

    @Test
    void testGetNomeBeneficiario() {
        assertEquals("Luca", bigliettoFidelity.getNomeBeneficiario());
    }

    @Test
    void testGetCognomeBeneficiario() {
        assertEquals("Bianchi", bigliettoFidelity.getCognomeBeneficiario());
    }

    @Test
    void testGetPrezzo() {
        assertEquals(200.0, bigliettoFidelity.getPrezzo());
    }

    @Test
    void testGetTariffa() {
        assertEquals("premium", bigliettoFidelity.getTariffa());
    }

    @Test
    void testCalcolaTassaModifica() {
        assertEquals(10.0, bigliettoFidelity.calcolaTassaModifica());
    }

    @Test
    void testClone() {
        BigliettoFidelity clone = bigliettoFidelity.clone();

        // Verifica che il clone sia uguale all'originale
        assertEquals(bigliettoFidelity, clone);

        // Verifica che l'originale e il clone non siano lo stesso oggetto
        assertNotSame(bigliettoFidelity, clone);

        // Verifica che i campi siano clonati correttamente
        assertEquals(bigliettoFidelity.getCliente(), clone.getCliente());
        assertEquals(bigliettoFidelity.getNomeBeneficiario(), clone.getNomeBeneficiario());
        assertEquals(bigliettoFidelity.getCognomeBeneficiario(), clone.getCognomeBeneficiario());
        assertEquals(bigliettoFidelity.getVolo(), clone.getVolo());
        assertEquals(bigliettoFidelity.getPrezzo(), clone.getPrezzo());
        assertEquals(bigliettoFidelity.getPunti(), clone.getPunti());
        assertEquals(bigliettoFidelity.getTariffa(), clone.getTariffa());
    }

}