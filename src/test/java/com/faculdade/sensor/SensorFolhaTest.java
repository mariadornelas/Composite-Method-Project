package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SensorFolhaTest {

    private SensorFolha sensor() {
        return new SensorFolha("T-01", "°C", 80, 90);
    }

    @Test
    void sensorSemLeituraDeveSerNormal() {
        SensorFolha s = sensor();

        assertEquals(Nivel.NORMAL, s.nivel());
        assertTrue(s.ultimaLeitura().isEmpty());
    }

    @Test
    void deveClassificarComLimitesInclusivos() {
        SensorFolha s = sensor();

        s.registrar(79.9);
        assertEquals(Nivel.NORMAL, s.nivel());
        s.registrar(80.0);
        assertEquals(Nivel.ALERTA, s.nivel());
        s.registrar(89.9);
        assertEquals(Nivel.ALERTA, s.nivel());
        s.registrar(90.0);
        assertEquals(Nivel.CRITICO, s.nivel());
    }

    @Test
    void deveUsarSempreAUltimaLeitura() {
        SensorFolha s = sensor();
        s.registrar(95.0);
        s.registrar(20.0);

        assertEquals(Nivel.NORMAL, s.nivel());
        assertEquals(20.0, s.ultimaLeitura().orElseThrow());
    }

    @Test
    void deveRecusarLeituraNaoNumerica() {
        SensorFolha s = sensor();
        s.registrar(50.0);

        assertThrows(IllegalArgumentException.class, () -> s.registrar(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> s.registrar(Double.POSITIVE_INFINITY));
        assertEquals(50.0, s.ultimaLeitura().orElseThrow());
    }

    @Test
    void deveValidarConstrucao() {
        assertThrows(NullPointerException.class, () -> new SensorFolha(null, "°C", 1, 2));
        assertThrows(IllegalArgumentException.class, () -> new SensorFolha(" ", "°C", 1, 2));
        assertThrows(IllegalArgumentException.class, () -> new SensorFolha("A/B", "°C", 1, 2));
        assertThrows(NullPointerException.class, () -> new SensorFolha("T", null, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> new SensorFolha("T", "°C", 2, 2));
        assertThrows(IllegalArgumentException.class, () -> new SensorFolha("T", "°C", 3, 2));
        assertThrows(IllegalArgumentException.class, () -> new SensorFolha("T", "°C", Double.NaN, 2));
    }

    @Test
    void folhaDeveSeTratarComoUmGrupoDeUmSensor() {
        SensorFolha s = sensor();
        s.registrar(85.0);

        assertEquals(1, s.totalSensores());
        assertEquals(List.of(s), s.sensores());
        assertEquals(Map.of(Nivel.NORMAL, 0L, Nivel.ALERTA, 1L, Nivel.CRITICO, 0L), s.contagemPorNivel());
    }

    @Test
    void folhaDeveSeEncontrarPeloProprioNomeEMaisNada() {
        SensorFolha s = sensor();

        assertSame(s, s.buscar("T-01").orElseThrow());
        assertEquals(Optional.empty(), s.buscar("outro"));
    }

    @Test
    void folhaSemPaiDeveTerCaminhoIgualAoNome() {
        assertEquals("T-01", sensor().caminho());
        assertTrue(sensor().pai().isEmpty());
    }

    @Test
    void descricaoDeveMostrarLeituraOuAusenciaDela() {
        SensorFolha s = sensor();
        assertEquals("T-01: sem leitura [NORMAL]\n", s.descrever());

        s.registrar(84.5);
        assertEquals("T-01: 84.5 °C [ALERTA]\n", s.descrever());
    }
}
