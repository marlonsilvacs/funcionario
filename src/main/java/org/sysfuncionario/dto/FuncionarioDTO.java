package org.sysfuncionario.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import org.sysfuncionario.model.Endereco;
import org.sysfuncionario.model.Funcionario;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FuncionarioDTO(
        @NotNull
        @Pattern(regexp = "\\d{6}", message = "Sua matricula deve conter exatamente 6 dígitos")
        String matricula,

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 40, message = "Nome deve ter entre 3 e 40 caracteres")
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF inválido deve ter 11 digitos")
        String cpf,

        @NotNull @Past(message = "Data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        @NotBlank(message = "Cargo é obrigatório")
        String cargo,

        @NotNull(message = "Salário é obrigatório")
        @Positive(message = "Salário deve ser positivo")
        BigDecimal salario,

        @NotNull @PastOrPresent(message = "Data de contratação não pode ser futura")
        LocalDate dataContratacao,

        @NotNull
        Endereco endereco
) {
    public FuncionarioDTO(Funcionario funcionario) {
        this(
                funcionario.getMatricula(),
                funcionario.getNome(),
                funcionario.getCpf(),
                funcionario.getDataNascimento(),
                funcionario.getCargo(),
                funcionario.getSalario(),
                funcionario.getDataContratacao(),
                funcionario.getEndereco()
        );
    }

    @AssertTrue(message = "Funcionário deve ter 18 anos ou mais")
    public boolean isMaiorDeIdade() {
        return dataNascimento != null &&
                !dataNascimento.plusYears(18).isAfter(LocalDate.now());
    }
}
