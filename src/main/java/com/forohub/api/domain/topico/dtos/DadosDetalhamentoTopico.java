package com.forohub.api.domain.topico.dtos;

import com.forohub.api.domain.curso.Curso;
import com.forohub.api.domain.topico.Topico;
import com.forohub.api.domain.usuario.Usuario;
import java.time.LocalDateTime;

public record DadosDetalhamentoTopico(
        Long id,
        String titulo,
        String mensagem,
        LocalDateTime dataCriacao,
        Boolean status,
        String autor,
        String curso
) {
    public DadosDetalhamentoTopico(Topico topico) {
        this(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensagem(),
                topico.getDataCriacao(),
                topico.getStatus(),
                topico.getAutor().getLogin(),
                topico.getCurso().getNome()
        );
    }
}
