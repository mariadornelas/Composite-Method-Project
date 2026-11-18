package com.faculdade.sensor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class ComponenteMonitorado {

    private final String nome;
    private GrupoSensores pai;

    protected ComponenteMonitorado(String nome) {
        Objects.requireNonNull(nome, "nome não pode ser nulo");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("nome não pode ser vazio");
        }
        if (nome.contains("/")) {
            throw new IllegalArgumentException("nome não pode conter '/': " + nome);
        }
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }

    public Optional<GrupoSensores> pai() {
        return Optional.ofNullable(pai);
    }

    void definirPai(GrupoSensores novoPai) {
        this.pai = novoPai;
    }

    public String caminho() {
        return pai == null ? nome : pai.caminho() + "/" + nome;
    }

    public abstract Nivel nivel();

    public abstract int totalSensores();

    public abstract List<SensorFolha> sensores();

    public abstract Optional<ComponenteMonitorado> buscar(String nome);

    abstract void descrever(StringBuilder saida, int profundidade);

    public Map<Nivel, Long> contagemPorNivel() {
        Map<Nivel, Long> contagem = new EnumMap<>(Nivel.class);
        for (Nivel n : Nivel.values()) {
            contagem.put(n, 0L);
        }
        for (SensorFolha s : sensores()) {
            contagem.merge(s.nivel(), 1L, Long::sum);
        }
        return contagem;
    }

    public String descrever() {
        StringBuilder sb = new StringBuilder();
        descrever(sb, 0);
        return sb.toString();
    }
}
