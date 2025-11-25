package org.sysfuncionario.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.sysfuncionario.dao.FuncionarioDAO;
import org.sysfuncionario.dto.FuncionarioDTO;
import org.sysfuncionario.model.Endereco;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.util.StringConverter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FuncionarioController {

    @FXML private TextField matriculaField, nomeField, cpfField, cargoField, salarioField;
    @FXML private DatePicker dataNascimentoPicker, dataContratacaoPicker;

    @FXML private TextField logradouroField, numeroField, complementoField, bairroField, cidadeField, cepField;
    @FXML private ComboBox<String> estadoCombo;

    @FXML private TextField cargoFiltroField;
    @FXML private TextField salarioMinField;
    @FXML private TextField salarioMaxField;

    @FXML private TableView<FuncionarioDTO> tabelaFuncionarios;
    @FXML private TableColumn<FuncionarioDTO, String> colMatricula, colNome, colCpf, colCargo,
            colSalario, colDataContratacao, colCidade, colEstado, colCep;

    @FXML private Label statusLabel;

    private final FuncionarioDAO funcionarioDao = new FuncionarioDAO();
    private final ObservableList<FuncionarioDTO> itensTabela = FXCollections.observableArrayList();

    private final Validator validador;


    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FuncionarioController() {
        ValidatorFactory fabrica = Validation.buildDefaultValidatorFactory();
        this.validador = fabrica.getValidator();
    }

    @FXML
    public void initialize() {
        estadoCombo.getItems().setAll(
                "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA",
                "MT","MS","MG","PA","PB","PR","PE","PI","RJ","RN",
                "RS","RO","RR","SC","SP","SE","TO"
        );

        // >>> Converters para os DatePickers em data <<<
        StringConverter<LocalDate> conv = new StringConverter<>() {
            public String toString(LocalDate date) {
                return (date == null) ? "" : BR.format(date);
            }
            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.trim().isEmpty()) return null;
                return LocalDate.parse(string.trim(), BR);
            }
        };
        dataNascimentoPicker.setConverter(conv);
        dataContratacaoPicker.setConverter(conv);
        dataNascimentoPicker.setPromptText("dd/MM/aaaa");
        dataContratacaoPicker.setPromptText("dd/MM/aaaa");


        colMatricula.setCellValueFactory(c -> new ReadOnlyStringWrapper(valorOuVazio(c.getValue().matricula())));
        colNome.setCellValueFactory(c -> new ReadOnlyStringWrapper(valorOuVazio(c.getValue().nome())));
        colCpf.setCellValueFactory(c -> new ReadOnlyStringWrapper(valorOuVazio(c.getValue().cpf())));
        colCargo.setCellValueFactory(c -> new ReadOnlyStringWrapper(valorOuVazio(c.getValue().cargo())));
        colSalario.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().salario() == null ? "" : c.getValue().salario().toPlainString()
        ));

        colDataContratacao.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().dataContratacao() == null ? "" : BR.format(c.getValue().dataContratacao())
        ));
        colCidade.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().endereco() == null ? "" : valorOuVazio(c.getValue().endereco().getCidade())
        ));
        colEstado.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().endereco() == null ? "" : valorOuVazio(c.getValue().endereco().getEstado())
        ));
        colCep.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().endereco() == null ? "" : valorOuVazio(c.getValue().endereco().getCep())
        ));

        tabelaFuncionarios.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, selecionado) -> { if (selecionado != null) preencherFormulario(selecionado); });

        onListarTodos();
    }

    @FXML
    public void onCadastrar() {
        try {
            FuncionarioDTO dto = lerFormularioDTO();

            Set<ConstraintViolation<FuncionarioDTO>> erros = validador.validate(dto);
            if (!erros.isEmpty()) { definirStatus(errosParaMensagem(erros), true); return; }

            FuncionarioDTO existente = funcionarioDao.buscarPorMatricula(dto.matricula());
            if (existente != null) { definirStatus("Matrícula já cadastrada: " + dto.matricula(), true); return; }

            funcionarioDao.cadastrarFuncionario(dto);

            definirStatus("Funcionário cadastrado: " + dto.matricula(), false);
            onListarTodos();
            onLimpar();
        } catch (Exception e) {
            definirStatus("Falha ao cadastrar: " + e.getMessage(), true);
        }
    }

    @FXML
    public void onAtualizar() {
        try {
            String mat = valorOuVazio(matriculaField.getText());
            if (mat.isEmpty()) { definirStatus("Informe a matrícula.", true); return; }

            FuncionarioDTO dto = lerFormularioDTO();
            Set<ConstraintViolation<FuncionarioDTO>> erros = validador.validate(dto);
            if (!erros.isEmpty()) { definirStatus(errosParaMensagem(erros), true); return; }

            funcionarioDao.excluirFuncionario(mat);
            funcionarioDao.cadastrarFuncionario(dto);

            definirStatus("Funcionário atualizado: " + mat, false);
            onListarTodos();
            onLimpar();
        } catch (Exception e) {
            definirStatus("Falha ao atualizar: " + e.getMessage(), true);
        }
    }

    @FXML
    public void onExcluir() {
        String mat = valorOuVazio(matriculaField.getText());
        if (mat.isEmpty()) { definirStatus("Informe a matrícula.", true);
            return;
        }
        try {
            funcionarioDao.excluirFuncionario(mat);
            definirStatus("Funcionário excluído: " + mat, false);
            onListarTodos();
            onLimpar();
        } catch (Exception e) {
            definirStatus("Falha ao excluir: " + e.getMessage(), true);
        }
    }

    @FXML
    public void onConsultar() {
        String mat = valorOuVazio(matriculaField.getText());
        if (mat.isEmpty()) { definirStatus("Informe a matrícula.", true); return; }

        FuncionarioDTO dto = funcionarioDao.buscarPorMatricula(mat);
        if (dto == null) { definirStatus("Matrícula não encontrada: " + mat, true); return; }

        preencherFormulario(dto);
        selecionarNaTabela(mat);
        definirStatus("Registro carregado: " + mat, false);
    }

    @FXML
    public void onListarTodos() {
        List<FuncionarioDTO> lista = funcionarioDao.listarFuncionarios();
        itensTabela.setAll(lista);
        tabelaFuncionarios.setItems(itensTabela);
        definirStatus("Registros: " + itensTabela.size(), false);
    }


    @FXML
    public void onLimpar() {
        matriculaField.clear(); nomeField.clear(); cpfField.clear();
        dataNascimentoPicker.setValue(null);
        cargoField.clear(); salarioField.clear(); dataContratacaoPicker.setValue(null);

        logradouroField.clear(); numeroField.clear(); complementoField.clear();
        bairroField.clear(); cidadeField.clear(); estadoCombo.getSelectionModel().clearSelection(); cepField.clear();

        tabelaFuncionarios.getSelectionModel().clearSelection();
    }

    @FXML
    public void onFiltrarCargo() {
        String termo = valorOuVazio(cargoFiltroField.getText()).trim().toLowerCase(Locale.ROOT);
        if (termo.isEmpty()) {
            definirStatus("Informe um cargo para filtrar.", true);
            return;
        }

        var filtrados = itensTabela.stream()
                .filter(f -> valorOuVazio(f.cargo()).toLowerCase().contains(termo))
                .collect(Collectors.toList());

        tabelaFuncionarios.setItems(FXCollections.observableArrayList(filtrados));
        definirStatus("Filtrados por cargo (" + termo + "): " + filtrados.size(), false);
    }


    @FXML
    public void onFiltrarFaixa() {
        BigDecimal min = parseMoeda(valorOuVazio(salarioMinField.getText()));
        BigDecimal max = parseMoeda(valorOuVazio(salarioMaxField.getText()));

        var fluxo = itensTabela.stream().filter(f -> f.salario() != null);

        if (min != null) fluxo = fluxo.filter(f -> f.salario().compareTo(min) >= 0);
        if (max != null) fluxo = fluxo.filter(f -> f.salario().compareTo(max) <= 0);

        var filtrados = fluxo.toList();
        tabelaFuncionarios.setItems(FXCollections.observableArrayList(filtrados));

        String faixa = "";
        if (min != null) faixa += " ≥ " + min.toPlainString();
        if (max != null) faixa += " ≤ " + max.toPlainString();
        if (faixa.isEmpty()) faixa = " (sem limites)";
        definirStatus("Faixa salarial" + faixa + ": " + filtrados.size() + " registro(s).", false);
    }

    @FXML
    public void onMediaPorCargo() {
        var medias = itensTabela.stream()
                .filter(f -> f.salario() != null && f.cargo() != null && !f.cargo().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        f -> f.cargo().trim(),
                        Collectors.averagingDouble(f -> f.salario().doubleValue())
                ));

        if (medias.isEmpty()) {
            definirStatus("Sem dados de salário para calcular média.", true);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Cargo ; Média\n");
        medias.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(e -> sb.append(e.getKey()).append(" ; ").append(formatarNumero(e.getValue())).append("\n"));

        mostrarMapa("Média Salarial por Cargo", "Cargo ; Média", sb.toString());
    }

    @FXML
    public void onAgruparPorCidade() {
        var grupos = itensTabela.stream()
                .collect(Collectors.groupingBy(this::chaveCidade, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("Cidade ; Qtd\n");
        grupos.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(e -> sb.append(e.getKey()).append(" ; ").append(e.getValue()).append("\n"));

        mostrarMapa("Funcionários por Cidade", "Cidade ; Qtd", sb.toString());
    }

    @FXML
    public void onAgruparPorEstado() {
        var grupos = itensTabela.stream()
                .collect(Collectors.groupingBy(this::chaveEstado, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("UF ; Qtd\n");
        grupos.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(e -> sb.append(e.getKey()).append(" ; ").append(e.getValue()).append("\n"));

        mostrarMapa("Funcionários por Estado (UF)", "UF ; Qtd", sb.toString());
    }

    @FXML
    public void onLimparFiltros() {
        if (cargoFiltroField != null) cargoFiltroField.clear();
        if (salarioMinField != null) salarioMinField.clear();
        if (salarioMaxField != null) salarioMaxField.clear();
        tabelaFuncionarios.setItems(itensTabela);
        definirStatus("Filtros limpos. Exibindo todos (" + itensTabela.size() + ").", false);
    }

    private FuncionarioDTO lerFormularioDTO() {
        String matricula = valorOuVazio(matriculaField.getText());
        String nome = valorOuVazio(nomeField.getText());
        String cpf = valorOuVazio(cpfField.getText());
        LocalDate dataNasc = dataNascimentoPicker.getValue();
        String cargo = valorOuVazio(cargoField.getText());
        LocalDate dataContr = dataContratacaoPicker.getValue();

        BigDecimal salario = null;
        String salTxt = valorOuVazio(salarioField.getText());
        if (!salTxt.isEmpty()) {
            String raw = salTxt.replace(".", "").replace(",", ".");
            salario = new BigDecimal(raw);
        }

        Endereco end = new Endereco();
        end.setLogradouro(valorOuVazio(logradouroField.getText()));
        end.setNumero(valorOuVazio(numeroField.getText()));
        end.setComplemento(valorOuVazio(complementoField.getText()));
        end.setBairro(valorOuVazio(bairroField.getText()));
        end.setCidade(valorOuVazio(cidadeField.getText()));
        end.setEstado(estadoCombo.getSelectionModel().getSelectedItem());
        end.setCep(valorOuVazio(cepField.getText()));

        return new FuncionarioDTO(matricula, nome, cpf, dataNasc, cargo, salario, dataContr, end);
    }

    private void preencherFormulario(FuncionarioDTO f) {
        matriculaField.setText(valorOuVazio(f.matricula()));
        nomeField.setText(valorOuVazio(f.nome()));
        cpfField.setText(valorOuVazio(f.cpf()));
        dataNascimentoPicker.setValue(f.dataNascimento());
        cargoField.setText(valorOuVazio(f.cargo()));
        salarioField.setText(f.salario() == null ? "" : f.salario().toPlainString());
        dataContratacaoPicker.setValue(f.dataContratacao());

        if (f.endereco() != null) {
            logradouroField.setText(valorOuVazio(f.endereco().getLogradouro()));
            numeroField.setText(valorOuVazio(f.endereco().getNumero()));
            complementoField.setText(valorOuVazio(f.endereco().getComplemento()));
            bairroField.setText(valorOuVazio(f.endereco().getBairro()));
            cidadeField.setText(valorOuVazio(f.endereco().getCidade()));
            estadoCombo.getSelectionModel().select(valorOuVazio(f.endereco().getEstado()));
            cepField.setText(valorOuVazio(f.endereco().getCep()));
        } else {
            logradouroField.clear(); numeroField.clear(); complementoField.clear();
            bairroField.clear(); cidadeField.clear(); estadoCombo.getSelectionModel().clearSelection(); cepField.clear();
        }
    }

    private void selecionarNaTabela(String matricula) {
        for (FuncionarioDTO f : tabelaFuncionarios.getItems()) {
            if (matricula.equals(f.matricula())) {
                tabelaFuncionarios.getSelectionModel().select(f);
                tabelaFuncionarios.scrollTo(f);
                break;
            }
        }
    }

    private String errosParaMensagem(Set<? extends ConstraintViolation<?>> erros) {
        return erros.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(" | "));
    }

    private void definirStatus(String msg, boolean erro) {
        statusLabel.setText(msg);
        statusLabel.setStyle(erro ? "-fx-text-fill: #c62828;" : "-fx-text-fill: #2e7d32;");
    }

    private String valorOuVazio(String s) { return s == null ? "" : s; }

    private BigDecimal parseMoeda(String txt) {
        if (txt == null) return null;
        String s = txt.trim();
        if (s.isEmpty()) return null;
        s = s.replace(".", "").replace(",", ".");
        try { return new BigDecimal(s); }
        catch (NumberFormatException e) { definirStatus("Valor inválido: " + txt, true); return null; }
    }

    private String formatarNumero(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    private void mostrarMapa(String titulo, String cabecalho, String corpo) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);

        TextArea area = new TextArea(corpo);
        area.setEditable(false);
        area.setWrapText(false);
        area.setPrefColumnCount(40);
        area.setPrefRowCount(15);

        alerta.getDialogPane().setContent(area);
        alerta.showAndWait();
    }

    private String chaveCidade(FuncionarioDTO f) {
        if (f.endereco() == null) return "(sem cidade)";
        String c = f.endereco().getCidade();
        if (c == null) return "(sem cidade)";
        return c.trim();
    }

    private String chaveEstado(FuncionarioDTO f) {
        if (f.endereco() == null) return "(sem UF)";
        String uf = f.endereco().getEstado();
        if (uf == null) return "(sem UF)";
        return uf.trim();
    }
}
