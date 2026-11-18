package com.faculdade.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GrupoSensores extends ComponenteMonitorado {

    private final List<ComponenteMonitorado> filhos = new ArrayList<>();

    public GrupoSensores(String nome) {
        super(nome);
    }

    public void adicionar(ComponenteMonitorado componente) {
        Objects.requireNonNull(componente, "componente não pode ser nulo");

        for (GrupoSensores g = this; g != null; g = g.pai().orElse(null)) {
            if (g == componente) {
                throw new IllegalArgumentException("ciclo: '" + componente.nome()
                        + "' é ancestral de '" + nome() + "'");
            }
        }
        if (componente.pai().isPresent()) {
            throw new IllegalStateException("'" + componente.nome() + "' já pertence ao grupo '"
                    + componente.pai().get().nome() + "'");
        }
        for (ComponenteMonitorado f : filhos) {
            if (f.nome().equals(componente.nome())) {
                throw new IllegalArgumentException("já existe '" + componente.nome() + "' em '" + nome() + "'");
            }
        }
        filhos.add(componente);
        componente.definirPai(this);
    }

    public boolean remover(ComponenteMonitorado componente) {
        if (componente != null && filhos.remove(componente)) {
            componente.definirPai(null);
            return true;
        }
        return false;
    }

    public List<ComponenteMonitorado> filhos() {
        return Collections.unmodifiableList(filhos);
    }

    @Override
    public Nivel nivel() {
        return filhos.stream().map(ComponenteMonitorado::nivel)
                .max(Comparator.naturalOrder()).orElse(Nivel.NORMAL);
    }

    @Override
    public int totalSensores() {
        int total = 0;
        for (ComponenteMonitorado f : filhos) {
            total += f.totalSensores();
        }
        return total;
    }

    @Override
    public List<SensorFolha> sensores() {
        List<SensorFolha> todos = new ArrayList<>();
        for (ComponenteMonitorado f : filhos) {
            todos.addAll(f.sensores());
        }
        return Collections.unmodifiableList(todos);
    }

    @Override
    public Optional<ComponenteMonitorado> buscar(String nomeProcurado) {
        if (nome().equals(nomeProcurado)) {
            return Optional.of(this);
        }
        for (ComponenteMonitorado f : filhos) {
            Optional<ComponenteMonitorado> achado = f.buscar(nomeProcurado);
            if (achado.isPresent()) {
                return achado;
            }
        }
        return Optional.empty();
    }

    @Override
    void descrever(StringBuilder saida, int profundidade) {
        saida.append("  ".repeat(profundidade)).append(nome()).append(" [").append(nivel()).append("] (")
                .append(totalSensores()).append(" sensores)\n");
        for (ComponenteMonitorado f : filhos) {
            f.descrever(saida, profundidade + 1);
        }
    }
}
