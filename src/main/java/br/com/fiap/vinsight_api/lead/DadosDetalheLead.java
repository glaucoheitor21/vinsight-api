package br.com.fiap.vinsight_api.lead;

import java.time.LocalDateTime;

public record DadosDetalheLead(
        Long id,
        ClienteResumo cliente,
        VeiculoResumo veiculo,
        Double score,
        PrioridadeLead prioridade,
        StatusLead status,
        String motivo,
        LocalDateTime dataGeracao,
        LocalDateTime dataConversao
) {
    public DadosDetalheLead(Lead l) {
        this(l.getId(),
                new ClienteResumo(l.getCliente().getId(),
                        l.getCliente().getDadosPessoais().getNome(),
                        l.getCliente().getDadosPessoais().getCpf(),
                        l.getCliente().getContato().getEmail()),
                new VeiculoResumo(l.getVeiculo().getId(),
                        l.getVeiculo().getVin(),
                        l.getVeiculo().getPlaca(),
                        l.getVeiculo().getModelo()),
                l.getScore(),
                l.getPrioridade(),
                l.getStatus(),
                l.getMotivo(),
                l.getDataGeracao(),
                l.getDataConversao());
    }

    public record ClienteResumo(Long id, String nome, String cpf, String email) {
    }

    public record VeiculoResumo(Long id, String vin, String placa, String modelo) {
    }
}
