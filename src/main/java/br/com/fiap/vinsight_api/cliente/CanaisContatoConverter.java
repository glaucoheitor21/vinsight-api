package br.com.fiap.vinsight_api.cliente;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * consentimento_canais e gravado como texto separado por virgula ("WHATSAPP,EMAIL", ver V9)
 * e exposto como lista de enum, que e o formato do contrato ("canais": ["WHATSAPP", "EMAIL"]).
 */
@Converter
public class CanaisContatoConverter implements AttributeConverter<List<CanalContato>, String> {

    @Override
    public String convertToDatabaseColumn(List<CanalContato> canais) {
        if (canais == null || canais.isEmpty()) {
            return null;
        }
        return canais.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    @Override
    public List<CanalContato> convertToEntityAttribute(String coluna) {
        if (coluna == null || coluna.isBlank()) {
            return List.of();
        }
        return Arrays.stream(coluna.split(","))
                .map(String::trim)
                .map(CanalContato::valueOf)
                .toList();
    }
}
