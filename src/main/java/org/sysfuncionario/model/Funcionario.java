package org.sysfuncionario.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@NoArgsConstructor
@Getter
@Setter
public class Funcionario {
    private String matricula;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String cargo;
    private BigDecimal salario;
    private LocalDate dataContratacao;
    private Endereco endereco;


    public Funcionario(String matricula, String nome, String cpf,
                       LocalDate dataNascimento, String cargo,
                       BigDecimal salario, LocalDate dataContratacao,
                       Endereco endereco) {

        if (dataNascimento == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }
        int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
        if (idade < 18) {
            throw new IllegalArgumentException("Idade mínima é 18 anos (idade atual: " + idade + ")");
        }

        if (endereco == null) {
            throw new IllegalArgumentException("Endereço é obrigatório");
        }
        String cep = endereco.getCep();
        String cepNum = (cep == null) ? "" : cep.replaceAll("\\D", "");
        if (cepNum.length() != 8) {
            throw new IllegalArgumentException("CEP deve conter exatamente 8 dígitos");
        }

        this.matricula = matricula;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.cargo = cargo;
        this.salario = salario;
        this.dataContratacao = dataContratacao;
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return "{" +
                "matricula= " + matricula +
                ", nome= " + nome +
                ", cpf= " + cpf +
                ", dataNascimento= " + dataNascimento +
                ", cargo= " + cargo +
                ", salario= " + salario +
                ", dataContratacao= " + dataContratacao +
                ", endereco= " + endereco +
                '}';
    }
}
