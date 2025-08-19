package com.forohub.api.controller;

import com.forohub.api.domain.curso.CursoRepository;
import com.forohub.api.domain.topico.Topico;
import com.forohub.api.domain.topico.TopicoRepository;
import com.forohub.api.domain.topico.dtos.DadosCadastroTopico;
import com.forohub.api.domain.topico.dtos.DadosDetalhamentoTopico;
import com.forohub.api.domain.usuario.Usuario;
import com.forohub.api.domain.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/topicos")
public class TopicoController {

    @Autowired
    private TopicoRepository topicoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CursoRepository cursoRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoTopico> cadastrar(
            @RequestBody @Valid DadosCadastroTopico dados,
            @AuthenticationPrincipal Usuario usuarioLogado,
            UriComponentsBuilder uriBuilder
    ) {
        // Validação da regra de negócio: Tópico duplicado
        if (topicoRepository.existsByTituloAndMensagem(dados.titulo(), dados.mensagem())) {
            return ResponseEntity.badRequest().build();
        }

        var autor = usuarioRepository.findById(usuarioLogado.getId())
                .orElseThrow(() -> new IllegalArgumentException("Autor não encontrado"));

        var curso = cursoRepository.findById(dados.cursoId())
                .orElseThrow(() -> new IllegalArgumentException("Curso não encontrado"));

        var topico = new Topico(dados.titulo(), dados.mensagem(), autor, curso);
        topicoRepository.save(topico);

        URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoTopico(topico));
    }

    @GetMapping
    public ResponseEntity<Page<DadosDetalhamentoTopico>> listar(
            @PageableDefault(size = 10, sort = {"dataCriacao"}) Pageable paginacao
    ) {
        var page = topicoRepository.findAll(paginacao).map(DadosDetalhamentoTopico::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoTopico> detalhar(@PathVariable Long id) {
        Optional<Topico> topicoOptional = topicoRepository.findById(id);
        return topicoOptional.map(topico -> ResponseEntity.ok(new DadosDetalhamentoTopico(topico)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<DadosDetalhamentoTopico> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid DadosCadastroTopico dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        var topico = topicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tópico não encontrado"));

        // Validação da regra de negócio: Apenas o autor pode editar
        if (!topico.getAutor().getId().equals(usuarioLogado.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        topico.atualizar(dados.titulo(), dados.mensagem());
        return ResponseEntity.ok(new DadosDetalhamentoTopico(topico));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        var topico = topicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tópico não encontrado"));

        // Validação da regra de negócio: Apenas o autor pode deletar
        if (!topico.getAutor().getId().equals(usuarioLogado.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        topicoRepository.delete(topico);
        return ResponseEntity.noContent().build();
    }
}
