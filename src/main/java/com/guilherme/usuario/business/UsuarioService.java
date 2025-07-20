package com.guilherme.usuario.business;


import com.guilherme.usuario.business.converter.UsuarioConverter;
import com.guilherme.usuario.business.dto.EnderecoDTO;
import com.guilherme.usuario.business.dto.TelefoneDTO;
import com.guilherme.usuario.business.dto.UsuarioDTO;
import com.guilherme.usuario.infrastructure.entity.Endereco;
import com.guilherme.usuario.infrastructure.entity.Telefone;
import com.guilherme.usuario.infrastructure.entity.Usuario;
import com.guilherme.usuario.infrastructure.excpetions.ConflictException;
import com.guilherme.usuario.infrastructure.excpetions.ResourceNotFoundException;
import com.guilherme.usuario.infrastructure.repository.EnderecoRepository;
import com.guilherme.usuario.infrastructure.repository.TelefoneRepository;
import com.guilherme.usuario.infrastructure.repository.UsuarioRepository;
import com.guilherme.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;



    public UsuarioDTO saveUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email){
        try{
            boolean existe = verificaEmailExistente(email);
            if(existe){
                throw new ConflictException("Email já cadastrado " +email);
            }
        }catch (ConflictException e){
            throw new ConflictException("Email já cadastrado "+e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTO buscarUsuarioPorEmail(String email){
        try {
            return usuarioConverter.paraUsuarioDTO(
                    usuarioRepository.findByEmail(email)
                            .orElseThrow(
                    () -> new ResourceNotFoundException("Email não encontrado" + email)
                            )
            );
        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Email não encontrado" +email);
        }
    }

    public void deletarUsuarioPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token,UsuarioDTO usuarioDTO){
        //busca o email atraves do token(tirar o obrigatoriedade do email)
        String email = jwtUtil.extractUsername(token.substring(7));

        //criptografia de senha
        usuarioDTO.setSenha(usuarioDTO.getSenha() != null ? passwordEncoder.encode(usuarioDTO.getSenha()) : null );

        //busca os dados do usuario no banco de dados
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(
                ()->new ResourceNotFoundException("Email não encontrado")
        );
        //mesclou os dados que recebemos na requisicao DTO com os dados do banco de dados
        Usuario usuario = usuarioConverter.updateUsuario(usuarioDTO, usuarioEntity);


        //salvou o usuario convertido e depois pegou o retorno e converteu para UsuarioDTO
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTO atualizaEndereco(Long id, EnderecoDTO enderecoDTO){

        Endereco entity = enderecoRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("Id não encontrado" +id));

        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizaTelefone(Long id, TelefoneDTO telefoneDTO){
        Telefone entity= telefoneRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("Id não encontrado"));

        Telefone telefone = usuarioConverter.updateTelefone(telefoneDTO, entity);

        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }


}
