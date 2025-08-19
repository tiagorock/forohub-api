package com.forohub.api.infra.seeding;

import com.forohub.api.domain.usuario.Usuario;
import com.forohub.api.domain.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioTestLoader implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Verifica se o usuário já existe para evitar duplicação
        if (usuarioRepository.findByLogin("teste@forohub.com") == null) {
            String senhaCriptografada = passwordEncoder.encode("123456");
            var usuario = new Usuario(null, "teste@forohub.com", senhaCriptografada);
            usuarioRepository.save(usuario);
            System.out.println("Usuário de teste 'teste@forohub.com' criado com sucesso!");
        }
    }
}
