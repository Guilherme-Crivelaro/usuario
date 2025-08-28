package com.guilherme.usuario.business;

import com.guilherme.usuario.infrastructure.Client.ViaCepClient;
import com.guilherme.usuario.infrastructure.Client.ViaCepDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ViaCepService {

    private final ViaCepClient viaCepClient;

    public ViaCepDTO buscaDadosEndereco(String cep) {
        return viaCepClient.buscaDadosEndereco(cep);
    }


    private String processarCep(String cep){
        String cepFormatado=cep.replace(" ", "").replace("-", "");

        if(!cepFormatado.matches("\\d+") || !Objects.equals(cepFormatado.length(),8)){
            throw new IllegalArgumentException("O cep contém caracteres inválidos, favor verificar"+cepFormatado);
        }

        return cepFormatado;
    }

}
