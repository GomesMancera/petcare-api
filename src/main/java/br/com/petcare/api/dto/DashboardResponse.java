package br.com.petcare.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(
        long quantidadePets,
        long proximasConsultas,
        long vacinasPendentes,
        LocalDateTime ultimaConsulta,
        List<ConsultaDto.Response> agendaProximosSete
) {}
