package br.com.fiap.vinsight_api.ordemservico;

import java.time.LocalDate;

/**
 * Projecao da consulta agregada de ordens de um cliente. Os tipos seguem o que o JPQL devolve:
 * COUNT -> Long, AVG -> Double, MAX(data) -> LocalDate. Sem ordens: total 0 e os outros null.
 */
public record ResumoOrdens(Long total, Double valorMedio, LocalDate ultimaData) {
}
