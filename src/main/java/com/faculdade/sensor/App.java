package com.faculdade.sensor;

public class App {

    public static void main(String[] args) {
        GrupoSensores planta = new GrupoSensores("Fabrica Norte");
        GrupoSensores envase = new GrupoSensores("Linha de Envase");
        GrupoSensores caldeiraria = new GrupoSensores("Caldeiraria");
        GrupoSensores caldeira1 = new GrupoSensores("Caldeira 1");

        SensorFolha t01 = new SensorFolha("T-01", "°C", 80, 90);
        SensorFolha p01 = new SensorFolha("P-01", "bar", 8, 10);
        SensorFolha t02 = new SensorFolha("T-02", "°C", 80, 90);
        SensorFolha p02 = new SensorFolha("P-02", "bar", 8, 10);
        SensorFolha v01 = new SensorFolha("V-01", "mm/s", 4.5, 7.1);

        envase.adicionar(t01);
        envase.adicionar(p01);
        caldeira1.adicionar(t02);
        caldeira1.adicionar(p02);
        caldeiraria.adicionar(caldeira1);
        planta.adicionar(envase);
        planta.adicionar(caldeiraria);
        planta.adicionar(v01);

        t01.registrar(72.0);
        p01.registrar(6.5);
        t02.registrar(84.5);
        p02.registrar(10.5);
        v01.registrar(3.0);

        System.out.println("=== Árvore da planta ===");
        System.out.print(planta.descrever());

        System.out.println();
        System.out.println("=== Mesma pergunta, nós diferentes ===");
        for (ComponenteMonitorado c : new ComponenteMonitorado[]{planta, caldeiraria, envase, t02}) {
            resumir(c);
        }

        System.out.println();
        System.out.println("=== Busca e caminho ===");
        planta.buscar("P-02").ifPresent(c -> System.out.println("P-02 está em: " + c.caminho()));

        System.out.println();
        System.out.println("=== O nível sobe sozinho quando uma folha muda ===");
        p02.registrar(7.0);
        resumir(caldeira1);
        resumir(planta);
    }

    private static void resumir(ComponenteMonitorado c) {
        System.out.printf("%-16s nível %-8s sensores %d  contagem %s%n",
                c.nome(), c.nivel(), c.totalSensores(), c.contagemPorNivel());
    }
}
