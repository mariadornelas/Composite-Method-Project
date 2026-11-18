package com.faculdade.sensor;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class SensorFolha extends ComponenteMonitorado {

    private final String unidade;
    private final double limiteAlerta;
    private final double limiteCritico;
    private Double ultimaLeitura;

    public SensorFolha(String nome, String unidade, double limiteAlerta, double limiteCritico) {
        super(nome);
        this.unidade = Objects.requireNonNull(unidade, "unidade não pode ser nula");
        if (!Double.isFinite(limiteAlerta) || !Double.isFinite(limiteCritico)) {
            throw new IllegalArgumentException("limites devem ser finitos");
        }
        if (!(limiteAlerta < limiteCritico)) {
            throw new IllegalArgumentException("o limite de alerta deve ser menor que o crítico");
        }
        this.limiteAlerta = limiteAlerta;
        this.limiteCritico = limiteCritico;
    }

    public void registrar(double valor) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("leitura deve ser um número finito: " + valor);
        }
        this.ultimaLeitura = valor;
    }

    public Optional<Double> ultimaLeitura() {
        return Optional.ofNullable(ultimaLeitura);
    }

    public String unidade() {
        return unidade;
    }

    @Override
    public Nivel nivel() {
        if (ultimaLeitura == null) return Nivel.NORMAL;
        if (ultimaLeitura >= limiteCritico) return Nivel.CRITICO;
        if (ultimaLeitura >= limiteAlerta) return Nivel.ALERTA;
        return Nivel.NORMAL;
    }

    @Override
    public int totalSensores() {
        return 1;
    }

    @Override
    public List<SensorFolha> sensores() {
        return List.of(this);
    }

    @Override
    public Optional<ComponenteMonitorado> buscar(String nomeProcurado) {
        return nome().equals(nomeProcurado) ? Optional.of(this) : Optional.empty();
    }

    @Override
    void descrever(StringBuilder saida, int profundidade) {
        String leitura = ultimaLeitura == null
                ? "sem leitura"
                : String.format(Locale.ROOT, "%.1f %s", ultimaLeitura, unidade);
        saida.append("  ".repeat(profundidade)).append(nome()).append(": ").append(leitura)
                .append(" [").append(nivel()).append("]\n");
    }
}
