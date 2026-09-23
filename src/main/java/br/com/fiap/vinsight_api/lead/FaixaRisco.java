package br.com.fiap.vinsight_api.lead;

/**
 * Faixa de risco de evasao, DERIVADA do score (nunca armazenada), conforme o contrato:
 * ALTO (score >= 0.70) · MEDIO (0.40 a 0.69) · BAIXO (< 0.40).
 */
public enum FaixaRisco {
    BAIXO(0.0, 0.40),
    MEDIO(0.40, 0.70),
    ALTO(0.70, null);

    private final Double scoreMinimo;
    private final Double scoreMaximoExclusivo;

    FaixaRisco(Double scoreMinimo, Double scoreMaximoExclusivo) {
        this.scoreMinimo = scoreMinimo;
        this.scoreMaximoExclusivo = scoreMaximoExclusivo;
    }

    public static FaixaRisco de(double score) {
        if (score >= ALTO.scoreMinimo) return ALTO;
        if (score >= MEDIO.scoreMinimo) return MEDIO;
        return BAIXO;
    }

    /** Limites para o filtro ?risco= virar condicao na consulta (null = sem limite). */
    public Double scoreMinimo() {
        return scoreMinimo;
    }

    public Double scoreMaximoExclusivo() {
        return scoreMaximoExclusivo;
    }
}
