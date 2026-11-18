package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** O que caracteriza o Composite: tratamento uniforme, estrutura recursiva e interface segura. */
class CompositeDesignTest {

    /** Um cliente que só conhece o tipo comum: serve para folha e para grupo. */
    private static String relatorio(ComponenteMonitorado c) {
        return c.nome() + ": " + c.nivel() + ", " + c.totalSensores() + " sensor(es)";
    }

    @Test
    void clienteDeveTratarFolhaEGrupoPeloMesmoTipo() {
        SensorFolha sensor = new SensorFolha("T-01", "°C", 80, 90);
        sensor.registrar(95);
        GrupoSensores grupo = new GrupoSensores("Linha");
        grupo.adicionar(sensor);
        grupo.adicionar(new SensorFolha("T-02", "°C", 80, 90));

        assertEquals("T-01: CRITICO, 1 sensor(es)", relatorio(sensor));
        assertEquals("Linha: CRITICO, 2 sensor(es)", relatorio(grupo));
    }

    @Test
    void grupoPodeConterGruposEFolhasMisturados() {
        GrupoSensores raiz = new GrupoSensores("R");
        GrupoSensores sub = new GrupoSensores("S");
        raiz.adicionar(sub);
        raiz.adicionar(new SensorFolha("A", "u", 1, 2));
        sub.adicionar(new SensorFolha("B", "u", 1, 2));
        sub.adicionar(new GrupoSensores("vazio"));

        assertEquals(2, raiz.filhos().size());      // um grupo e uma folha
        assertEquals(2, sub.filhos().size());       // uma folha e um grupo vazio
        assertEquals(2, raiz.totalSensores());      // grupos vazios não contam como sensor
    }

    @Test
    void totalDeveSerIgualAoTamanhoDaListaAchatada() {
        GrupoSensores raiz = new GrupoSensores("R");
        for (int i = 0; i < 4; i++) {
            GrupoSensores g = new GrupoSensores("G" + i);
            for (int j = 0; j < 3; j++) {
                g.adicionar(new SensorFolha("S" + i + j, "u", 1, 2));
            }
            raiz.adicionar(g);
        }

        assertEquals(12, raiz.totalSensores());
        assertEquals(raiz.totalSensores(), raiz.sensores().size());
        assertEquals(12L, raiz.contagemPorNivel().values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void arvoreProfundaDeveFuncionarComAMesmaInterface() {
        GrupoSensores raiz = new GrupoSensores("nivel0");
        GrupoSensores atual = raiz;
        for (int i = 1; i <= 200; i++) {
            GrupoSensores prox = new GrupoSensores("nivel" + i);
            atual.adicionar(prox);
            atual = prox;
        }
        SensorFolha fundo = new SensorFolha("fundo", "u", 1, 2);
        atual.adicionar(fundo);
        fundo.registrar(5);

        assertEquals(Nivel.CRITICO, raiz.nivel());
        assertEquals(1, raiz.totalSensores());
        assertSame(fundo, raiz.buscar("fundo").orElseThrow());
        assertTrue(fundo.caminho().startsWith("nivel0/nivel1/"));
        assertTrue(fundo.caminho().endsWith("/nivel200/fundo"));
    }

    @Test
    void resultadoDaFolhaEDoGrupoUnitarioDevemSerIguais() {
        SensorFolha s = new SensorFolha("T", "u", 1, 2);
        s.registrar(1.5);
        GrupoSensores g = new GrupoSensores("G");
        SensorFolha s2 = new SensorFolha("T", "u", 1, 2);
        s2.registrar(1.5);
        g.adicionar(s2);

        assertEquals(s.nivel(), g.nivel());
        assertEquals(s.totalSensores(), g.totalSensores());
        assertEquals(s.contagemPorNivel(), g.contagemPorNivel());
    }

    // ---- interface segura: só o grupo gerencia filhos ----

    private static boolean temMetodo(Class<?> c, String nome) {
        return Arrays.stream(c.getMethods()).map(Method::getName).anyMatch(nome::equals);
    }

    @Test
    void gerenciarFilhosDeveExistirSomenteNoGrupo() {
        assertFalse(temMetodo(ComponenteMonitorado.class, "adicionar"));
        assertFalse(temMetodo(ComponenteMonitorado.class, "remover"));
        assertFalse(temMetodo(SensorFolha.class, "adicionar"));
        assertFalse(temMetodo(SensorFolha.class, "remover"));
        assertTrue(temMetodo(GrupoSensores.class, "adicionar"));
        assertTrue(temMetodo(GrupoSensores.class, "remover"));
    }

    @Test
    void folhaEGrupoDevemEstenderOComponente() {
        assertTrue(ComponenteMonitorado.class.isAssignableFrom(SensorFolha.class));
        assertTrue(ComponenteMonitorado.class.isAssignableFrom(GrupoSensores.class));
        assertTrue(Modifier.isAbstract(ComponenteMonitorado.class.getModifiers()));
    }

    @Test
    void ninguemForaDoPacoteDeveConseguirMudarOPaiDiretamente() throws Exception {
        Method m = ComponenteMonitorado.class.getDeclaredMethod("definirPai", GrupoSensores.class);

        assertFalse(Modifier.isPublic(m.getModifiers()));
        assertFalse(Modifier.isProtected(m.getModifiers()));
    }

    @Test
    void contagemPorNivelDeveSerUmMapaCompleto() {
        Map<Nivel, Long> m = new GrupoSensores("G").contagemPorNivel();

        assertEquals(List.of(Nivel.values()), List.copyOf(m.keySet()));
    }
}
