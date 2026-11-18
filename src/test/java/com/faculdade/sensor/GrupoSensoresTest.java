package com.faculdade.sensor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GrupoSensoresTest {

    private GrupoSensores planta, envase, caldeiraria, caldeira1;
    private SensorFolha t01, p01, t02, p02, v01;

    @BeforeEach
    void montar() {
        planta = new GrupoSensores("Planta");
        envase = new GrupoSensores("Envase");
        caldeiraria = new GrupoSensores("Caldeiraria");
        caldeira1 = new GrupoSensores("Caldeira 1");
        t01 = new SensorFolha("T-01", "°C", 80, 90);
        p01 = new SensorFolha("P-01", "bar", 8, 10);
        t02 = new SensorFolha("T-02", "°C", 80, 90);
        p02 = new SensorFolha("P-02", "bar", 8, 10);
        v01 = new SensorFolha("V-01", "mm/s", 4.5, 7.1);

        envase.adicionar(t01);
        envase.adicionar(p01);
        caldeira1.adicionar(t02);
        caldeira1.adicionar(p02);
        caldeiraria.adicionar(caldeira1);
        planta.adicionar(envase);
        planta.adicionar(caldeiraria);
        planta.adicionar(v01);
    }

    @Test
    void totalDeSensoresDeveContarTodosOsNiveis() {
        assertEquals(5, planta.totalSensores());
        assertEquals(2, envase.totalSensores());
        assertEquals(2, caldeiraria.totalSensores());
        assertEquals(2, caldeira1.totalSensores());
    }

    @Test
    void sensoresDevemVirEmOrdemDeCadastroAtravessandoSubgrupos() {
        assertEquals(List.of(t01, p01, t02, p02, v01), planta.sensores());
        assertEquals(List.of(t02, p02), caldeiraria.sensores());
    }

    @Test
    void grupoVazioDeveSerNormalESemSensores() {
        GrupoSensores vazio = new GrupoSensores("Vazio");

        assertEquals(Nivel.NORMAL, vazio.nivel());
        assertEquals(0, vazio.totalSensores());
        assertTrue(vazio.sensores().isEmpty());
        assertEquals(Map.of(Nivel.NORMAL, 0L, Nivel.ALERTA, 0L, Nivel.CRITICO, 0L), vazio.contagemPorNivel());
    }

    @Test
    void nivelDoGrupoDeveSerOPiorDosFilhosEmQualquerProfundidade() {
        t01.registrar(20);
        p01.registrar(5);
        t02.registrar(85);
        p02.registrar(5);
        v01.registrar(1);

        assertEquals(Nivel.ALERTA, caldeira1.nivel());
        assertEquals(Nivel.ALERTA, caldeiraria.nivel());
        assertEquals(Nivel.ALERTA, planta.nivel());
        assertEquals(Nivel.NORMAL, envase.nivel());

        p02.registrar(11);
        assertEquals(Nivel.CRITICO, planta.nivel());
    }

    @Test
    void ordemDosFilhosNaoDeveInfluenciarONivel() {
        GrupoSensores g = new GrupoSensores("G");
        SensorFolha critico = new SensorFolha("C", "u", 1, 2);
        SensorFolha normal = new SensorFolha("N", "u", 1, 2);
        critico.registrar(5);
        normal.registrar(0);
        g.adicionar(critico);
        g.adicionar(normal);

        assertEquals(Nivel.CRITICO, g.nivel());
    }

    @Test
    void nivelDeveAcompanharMudancasDasFolhasSemRecalcularNada() {
        assertEquals(Nivel.NORMAL, planta.nivel());

        v01.registrar(8.0);
        assertEquals(Nivel.CRITICO, planta.nivel());

        v01.registrar(1.0);
        assertEquals(Nivel.NORMAL, planta.nivel());
    }

    @Test
    void contagemPorNivelDeveIncluirZerosEContarCadaSensorUmaVez() {
        t01.registrar(85);
        p01.registrar(11);
        t02.registrar(10);
        p02.registrar(10);

        assertEquals(Map.of(Nivel.NORMAL, 2L, Nivel.ALERTA, 1L, Nivel.CRITICO, 2L), planta.contagemPorNivel());
        assertEquals(Map.of(Nivel.NORMAL, 1L, Nivel.ALERTA, 0L, Nivel.CRITICO, 1L), caldeira1.contagemPorNivel());
    }

    @Test
    void buscarDeveEncontrarEmQualquerProfundidadeInclusiveOProprioGrupo() {
        assertSame(p02, planta.buscar("P-02").orElseThrow());
        assertSame(caldeira1, planta.buscar("Caldeira 1").orElseThrow());
        assertSame(planta, planta.buscar("Planta").orElseThrow());
        assertSame(v01, planta.buscar("V-01").orElseThrow());
    }

    @Test
    void buscarNaoDeveSairDaSubarvore() {
        assertEquals(Optional.empty(), envase.buscar("P-02"));
        assertEquals(Optional.empty(), planta.buscar("inexistente"));
    }

    @Test
    void caminhoDeveSubirAteARaiz() {
        assertEquals("Planta/Caldeiraria/Caldeira 1/P-02", p02.caminho());
        assertEquals("Planta/V-01", v01.caminho());
        assertEquals("Planta", planta.caminho());
    }

    @Test
    void descreverDeveMostrarArvoreComRecuo() {
        t01.registrar(72);
        p02.registrar(10.5);

        String esperado = String.join("\n",
                "Planta [CRITICO] (5 sensores)",
                "  Envase [NORMAL] (2 sensores)",
                "    T-01: 72.0 °C [NORMAL]",
                "    P-01: sem leitura [NORMAL]",
                "  Caldeiraria [CRITICO] (2 sensores)",
                "    Caldeira 1 [CRITICO] (2 sensores)",
                "      T-02: sem leitura [NORMAL]",
                "      P-02: 10.5 bar [CRITICO]",
                "  V-01: sem leitura [NORMAL]",
                "");

        assertEquals(esperado, planta.descrever());
    }

    @Test
    void adicionarDeveRecusarNuloEODeveSerOProprioGrupo() {
        assertThrows(NullPointerException.class, () -> planta.adicionar(null));
        assertThrows(IllegalArgumentException.class, () -> planta.adicionar(planta));
    }

    @Test
    void adicionarDeveRecusarCicloComAncestral() {
        assertThrows(IllegalArgumentException.class, () -> caldeira1.adicionar(planta));
        assertThrows(IllegalArgumentException.class, () -> caldeira1.adicionar(caldeiraria));
        assertEquals(5, planta.totalSensores());
        assertTrue(planta.pai().isEmpty());
    }

    @Test
    void componenteSoPodeTerUmPai() {
        GrupoSensores outro = new GrupoSensores("Outro");

        assertThrows(IllegalStateException.class, () -> outro.adicionar(t01));
        assertEquals(0, outro.totalSensores());
        assertEquals(5, planta.totalSensores());
    }

    @Test
    void irmaosDevemTerNomesDistintos() {
        GrupoSensores g = new GrupoSensores("G");
        g.adicionar(new SensorFolha("T", "u", 1, 2));

        assertThrows(IllegalArgumentException.class, () -> g.adicionar(new SensorFolha("T", "u", 1, 2)));
        assertEquals(1, g.totalSensores());
    }

    @Test
    void nomesIguaisEmGruposDiferentesSaoPermitidos() {
        GrupoSensores g1 = new GrupoSensores("G1");
        GrupoSensores g2 = new GrupoSensores("G2");

        assertDoesNotThrow(() -> g1.adicionar(new SensorFolha("T", "u", 1, 2)));
        assertDoesNotThrow(() -> g2.adicionar(new SensorFolha("T", "u", 1, 2)));
    }

    @Test
    void removerDeveAtualizarTotaisESoltarOFilho() {
        assertTrue(envase.remover(p01));

        assertEquals(1, envase.totalSensores());
        assertEquals(4, planta.totalSensores());
        assertTrue(p01.pai().isEmpty());
        assertEquals("P-01", p01.caminho());
    }

    @Test
    void removerNaoDeveAtingirNetosNemEstranhos() {
        assertFalse(planta.remover(p02));
        assertFalse(planta.remover(new SensorFolha("X", "u", 1, 2)));
        assertFalse(planta.remover(null));
        assertEquals(5, planta.totalSensores());
        assertSame(caldeira1, p02.pai().orElseThrow());
    }

    @Test
    void componenteRemovidoDevePoderSerMovidoParaOutroGrupo() {
        planta.remover(envase);

        caldeiraria.adicionar(envase);

        assertEquals("Planta/Caldeiraria/Envase/T-01", t01.caminho());
        assertEquals(5, planta.totalSensores());
        assertEquals(4, caldeiraria.totalSensores());
    }

    @Test
    void removidoNaoDeveMaisInfluenciarONivelDoAntigoPai() {
        p01.registrar(11);
        assertEquals(Nivel.CRITICO, planta.nivel());

        envase.remover(p01);

        assertEquals(Nivel.NORMAL, planta.nivel());
    }

    @Test
    void filhosDevemSerSomenteLeitura() {
        assertThrows(UnsupportedOperationException.class, () -> planta.filhos().clear());
        assertThrows(UnsupportedOperationException.class, () -> planta.sensores().clear());
        assertEquals(3, planta.filhos().size());
    }
}
