package org.sysfuncionario.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Endereco {

    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    @Override
    public String toString() {
        return "{" +
                "logradouro= " + logradouro +
                ", numero= " + numero +
                ", complemento= " + complemento +
                ", bairro= " + bairro +
                ", cidade= " + cidade +
                ", estado= " + estado +
                ", cep='" + cep +
                '}';
    }
}
