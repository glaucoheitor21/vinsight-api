package br.com.fiap.vinsight_api.veiculo;

/**
 * Formato do VIN (Vehicle Identification Number), o "CPF do carro": 17 caracteres, letras
 * maiusculas e numeros, sem I, O e Q (para nao confundir com 1 e 0).
 * Ex.: 9BF8313PF9WBWDX01 -> 9BF = Ford Brasil.
 */
public final class Vin {

    public static final String REGEX = "[A-HJ-NPR-Z0-9]{17}";
    public static final String MENSAGEM =
            "VIN inválido: deve ter 17 caracteres, letras maiúsculas e números, sem I, O e Q";

    private Vin() {
    }
}
