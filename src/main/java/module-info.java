module org.sysfuncionario {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    // Bean Validation
    requires jakarta.validation;
    requires jakarta.el;              // necessário pelo Hibernate Validator
    requires org.hibernate.validator;

    // JavaFX acessa controllers via reflexão
    opens org.sysfuncionario.controller to javafx.fxml;

    // Hibernate Validator acessa DTOs/Models via reflexão
    opens org.sysfuncionario.dto to org.hibernate.validator;
    opens org.sysfuncionario.model to org.hibernate.validator;

    // Exportações públicas
    exports org.sysfuncionario;
    exports org.sysfuncionario.controller;
}
