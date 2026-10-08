package controller;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import database.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.Usuario;

import application.SessaoUsuario;

public class UsuariosController {

    // ============================================================
    // FORMULÁRIO
    // ============================================================

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private ComboBox<String> cbTipo;

    @FXML
    private Button btnCadastrar;


    // ============================================================
    // FILTROS
    // ============================================================

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cbFiltroTipo;


    // ============================================================
    // TABELA
    // ============================================================

    @FXML
    private TableView<Usuario> tableUsuarios;

    @FXML
    private TableColumn<Usuario, String> colNome;

    @FXML
    private TableColumn<Usuario, String> colUsuario;

    @FXML
    private TableColumn<Usuario, String> colEmail;

    @FXML
    private TableColumn<Usuario, String> colTipo;

    @FXML
    private TableColumn<Usuario, Void> colAcoes;


    // ============================================================
    // DADOS
    // ============================================================

    private final ObservableList<Usuario> todosUsuarios =
            FXCollections.observableArrayList();

    private final ObservableList<Usuario> usuariosFiltrados =
            FXCollections.observableArrayList();

    private final UsuarioDAO usuarioDAO =
            new UsuarioDAO();


    // ============================================================
    // VALIDAÇÃO
    // ============================================================

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );


    // ============================================================
    // INITIALIZE
    // ============================================================

    @FXML
    private void initialize() {

        configurarTabela();
        configurarCombos();
        configurarEventos();

        carregarUsuarios();

        tableUsuarios.setPlaceholder(
                new Label("Nenhum usuário encontrado.")
        );
    }


    // ============================================================
    // CONFIGURAÇÃO DA TABELA
    // ============================================================

    private void configurarTabela() {

        colNome.setCellValueFactory(
                new PropertyValueFactory<>("nome")
        );

        colUsuario.setCellValueFactory(
                new PropertyValueFactory<>("login")
        );

        colEmail.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );

        colTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipo")
        );

        configurarColunaAcoes();

        tableUsuarios.setItems(
                usuariosFiltrados
        );
    }


    // ============================================================
    // COMBOS
    // ============================================================

    private void configurarCombos() {

        cbTipo.setItems(
                FXCollections.observableArrayList(
                        "Administrador",
                        "Vendedor",
                        "Estoquista"
                )
        );

        cbTipo.getSelectionModel()
                .selectFirst();


        cbFiltroTipo.setItems(
                FXCollections.observableArrayList(
                        "Todos",
                        "Administrador",
                        "Vendedor",
                        "Estoquista"
                )
        );

        cbFiltroTipo.getSelectionModel()
                .select("Todos");
    }


    // ============================================================
    // EVENTOS
    // ============================================================

    private void configurarEventos() {

        btnCadastrar.setOnAction(
                e -> onCadastrar()
        );


        txtBuscar.textProperty()
                .addListener(
                        (obs, antigo, novo) ->
                                aplicarFiltros()
                );


        cbFiltroTipo.setOnAction(
                e -> aplicarFiltros()
        );


        // Pressionar ENTER no campo senha cadastra o usuário
        txtSenha.setOnAction(
                e -> onCadastrar()
        );
    }


    // ============================================================
    // CARREGAMENTO
    // ============================================================

    private void carregarUsuarios() {

        try {

            List<Usuario> usuarios =
                    usuarioDAO.getAll();

            todosUsuarios.clear();

            if (usuarios != null) {

                todosUsuarios.addAll(
                        usuarios
                );
            }

            aplicarFiltros();

        } catch (Exception e) {

            mostrarErro(
                    "Erro ao carregar usuários",
                    "Não foi possível carregar os usuários cadastrados."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // CADASTRAR
    // ============================================================

    private void onCadastrar() {

        String nome =
                obterTexto(txtNome);

        String email =
                obterTexto(txtEmail);

        String login =
                obterTexto(txtLogin);

        String senha =
                txtSenha.getText() == null
                        ? ""
                        : txtSenha.getText();

        String tipo =
                cbTipo.getValue();


        // ========================================================
        // CAMPOS OBRIGATÓRIOS
        // ========================================================

        if (nome.isEmpty()) {

            mostrarAviso(
                    "Nome obrigatório",
                    "Informe o nome completo do usuário."
            );

            txtNome.requestFocus();

            return;
        }


        if (email.isEmpty()) {

            mostrarAviso(
                    "E-mail obrigatório",
                    "Informe o e-mail do usuário."
            );

            txtEmail.requestFocus();

            return;
        }


        if (!emailValido(email)) {

            mostrarAviso(
                    "E-mail inválido",
                    "Informe um endereço de e-mail válido."
            );

            txtEmail.requestFocus();

            return;
        }


        if (login.isEmpty()) {

            mostrarAviso(
                    "Login obrigatório",
                    "Informe o login que será utilizado para entrar no sistema."
            );

            txtLogin.requestFocus();

            return;
        }


        if (login.contains(" ")) {

            mostrarAviso(
                    "Login inválido",
                    "O login não pode conter espaços."
            );

            txtLogin.requestFocus();

            return;
        }


        if (senha.isBlank()) {

            mostrarAviso(
                    "Senha obrigatória",
                    "Informe uma senha para o usuário."
            );

            txtSenha.requestFocus();

            return;
        }


        if (senha.length() < 4) {

            mostrarAviso(
                    "Senha muito curta",
                    "A senha deve possuir pelo menos 4 caracteres."
            );

            txtSenha.requestFocus();

            return;
        }


        if (tipo == null
                || tipo.isBlank()) {

            mostrarAviso(
                    "Nível de acesso obrigatório",
                    "Selecione o nível de acesso do usuário."
            );

            cbTipo.requestFocus();

            return;
        }


        // ========================================================
        // CRIA USUÁRIO
        // ========================================================

        Usuario usuario =
                new Usuario(
                        nome,
                        login,
                        senha,
                        email,
                        tipo
                );


        try {

            String mensagem =
                    usuarioDAO.inserir(
                            usuario
                    );


            if (mensagem != null
                    && mensagem
                            .toLowerCase()
                            .contains("sucesso")) {

                mostrarSucesso(
                        "Usuário cadastrado",
                        "O usuário foi cadastrado com sucesso."
                );

                limparFormulario();

                carregarUsuarios();

            } else {

                mostrarErro(
                        "Não foi possível cadastrar",
                        mensagem != null
                                ? mensagem
                                : "Ocorreu um erro ao cadastrar o usuário."
                );
            }


        } catch (Exception e) {

            mostrarErro(
                    "Erro ao cadastrar usuário",
                    "Não foi possível concluir o cadastro."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // FILTROS
    // ============================================================

    private void aplicarFiltros() {

        String busca =
                txtBuscar.getText() == null
                        ? ""
                        : txtBuscar
                                .getText()
                                .trim()
                                .toLowerCase();

        String tipo =
                cbFiltroTipo.getValue();


        usuariosFiltrados.clear();


        for (Usuario usuario : todosUsuarios) {

            boolean correspondeBusca =
                    busca.isEmpty()
                    || contemTexto(
                            usuario.getNome(),
                            busca
                    )
                    || contemTexto(
                            usuario.getEmail(),
                            busca
                    )
                    || contemTexto(
                            usuario.getLogin(),
                            busca
                    );


            boolean correspondeTipo =
                    tipo == null
                    || "Todos".equals(tipo)
                    || tipo.equals(
                            usuario.getTipo()
                    );


            if (correspondeBusca
                    && correspondeTipo) {

                usuariosFiltrados.add(
                        usuario
                );
            }
        }
    }


    private boolean contemTexto(
            String valor,
            String busca) {

        return valor != null
                && valor
                        .toLowerCase()
                        .contains(busca);
    }


    // ============================================================
    // AÇÕES DA TABELA
    // ============================================================

    private void configurarColunaAcoes() {

        Callback<TableColumn<Usuario, Void>,
                TableCell<Usuario, Void>> cellFactory = coluna ->

                new TableCell<Usuario, Void>() {

                    private final Button btnEditar =
                            new Button("✎");

                    private final Button btnExcluir =
                            new Button("🗑");

                    private final HBox box =
                            new HBox(
                                    6,
                                    btnEditar,
                                    btnExcluir
                            );


                    {
                        // =================================================
                        // ESTILO
                        // =================================================

                        btnEditar
                                .getStyleClass()
                                .add(
                                        "table-action-edit"
                                );

                        btnExcluir
                                .getStyleClass()
                                .add(
                                        "table-action-delete"
                                );


                        btnEditar.setTooltip(
                                new Tooltip(
                                        "Editar usuário"
                                )
                        );

                        btnExcluir.setTooltip(
                                new Tooltip(
                                        "Excluir usuário"
                                )
                        );


                        // =================================================
                        // EDITAR
                        // =================================================

                        btnEditar.setOnAction(
                                e -> {

                                    Usuario usuario =
                                            getUsuarioDaLinha();

                                    if (usuario != null) {

                                        mostrarDialogoEdicao(
                                                usuario
                                        );
                                    }
                                }
                        );


                        // =================================================
                        // EXCLUIR
                        // =================================================

                        btnExcluir.setOnAction(
                                e -> {

                                    Usuario usuario =
                                            getUsuarioDaLinha();

                                    if (usuario != null) {

                                        excluirUsuario(
                                                usuario
                                        );
                                    }
                                }
                        );
                    }


                    private Usuario getUsuarioDaLinha() {

                        int indice =
                                getIndex();

                        if (indice < 0
                                || indice
                                >= getTableView()
                                        .getItems()
                                        .size()) {

                            return null;
                        }

                        return getTableView()
                                .getItems()
                                .get(indice);
                    }


                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );


                        if (empty) {

                            setGraphic(null);

                        } else {

                            setGraphic(box);
                        }
                    }
                };


        colAcoes.setCellFactory(
                cellFactory
        );
    }


    // ============================================================
    // EXCLUIR
    // ============================================================

    private void excluirUsuario(
            Usuario usuario) {

        Usuario usuarioLogado =
                SessaoUsuario.getUsuarioLogado();

        if (usuarioLogado != null
                && usuarioLogado.getId()
                == usuario.getId()) {

            mostrarAviso(
                    "Ação não permitida",
                    "Você não pode excluir o usuário que está atualmente conectado ao sistema."
            );

            return;
        }


        Alert confirmacao =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        confirmacao.setTitle(
                "Excluir usuário"
        );

        confirmacao.setHeaderText(
                "Deseja excluir este usuário?"
        );

        confirmacao.setContentText(
                usuario.getNome()
                        + "\nLogin: "
                        + usuario.getLogin()
        );


        Optional<ButtonType> resposta =
                confirmacao.showAndWait();


        if (resposta.isEmpty()
                || resposta.get()
                != ButtonType.OK) {

            return;
        }


        try {

            String mensagem =
                    usuarioDAO.deletar(
                            usuario.getId()
                    );


            if (mensagem != null
                    && mensagem
                            .toLowerCase()
                            .contains("sucesso")) {

                mostrarSucesso(
                        "Usuário excluído",
                        "O usuário foi removido com sucesso."
                );

                carregarUsuarios();

            } else {

                mostrarErro(
                        "Não foi possível excluir",
                        mensagem != null
                                ? mensagem
                                : "Não foi possível excluir o usuário."
                );
            }


        } catch (Exception e) {

            mostrarErro(
                    "Erro ao excluir usuário",
                    "Não foi possível remover o usuário."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // EDITAR
    // ============================================================

    private void mostrarDialogoEdicao(
            Usuario usuario) {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "Editar usuário"
        );

        dialog.setHeaderText(
                "Altere os dados do usuário."
        );


        ButtonType btnSalvar =
                new ButtonType(
                        "Salvar alterações",
                        ButtonBar.ButtonData.OK_DONE
                );


        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnSalvar,
                        ButtonType.CANCEL
                );


        TextField campoNome =
                new TextField(
                        usuario.getNome()
                );


        TextField campoEmail =
                new TextField(
                        usuario.getEmail()
                );


        ComboBox<String> campoTipo =
                new ComboBox<>(
                        FXCollections.observableArrayList(
                                "Administrador",
                                "Vendedor",
                                "Estoquista"
                        )
                );


        campoTipo.setMaxWidth(
                Double.MAX_VALUE
        );


        campoTipo.getSelectionModel()
                .select(
                        usuario.getTipo()
                );
        
        Usuario usuarioLogado =
                SessaoUsuario.getUsuarioLogado();

        if (usuarioLogado != null
                && usuarioLogado.getId()
                == usuario.getId()) {

            campoTipo.setDisable(true);
        }


        GridPane grid =
                new GridPane();


        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(
                new Insets(
                        10
                )
        );


        grid.add(
                new Label("Nome completo"),
                0,
                0
        );

        grid.add(
                campoNome,
                1,
                0
        );


        grid.add(
                new Label("E-mail"),
                0,
                1
        );

        grid.add(
                campoEmail,
                1,
                1
        );


        grid.add(
                new Label("Nível de acesso"),
                0,
                2
        );

        grid.add(
                campoTipo,
                1,
                2
        );


        dialog.getDialogPane()
                .setContent(grid);


        Button salvar =
                (Button) dialog
                        .getDialogPane()
                        .lookupButton(
                                btnSalvar
                        );


        salvar.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                evento -> {

                    String nome =
                            campoNome
                                    .getText()
                                    .trim();

                    String email =
                            campoEmail
                                    .getText()
                                    .trim();

                    String tipo =
                            campoTipo
                                    .getValue();


                    if (nome.isEmpty()) {

                        mostrarAviso(
                                "Nome obrigatório",
                                "Informe o nome do usuário."
                        );

                        campoNome.requestFocus();

                        evento.consume();

                        return;
                    }


                    if (email.isEmpty()) {

                        mostrarAviso(
                                "E-mail obrigatório",
                                "Informe o e-mail do usuário."
                        );

                        campoEmail.requestFocus();

                        evento.consume();

                        return;
                    }


                    if (!emailValido(email)) {

                        mostrarAviso(
                                "E-mail inválido",
                                "Informe um endereço de e-mail válido."
                        );

                        campoEmail.requestFocus();

                        evento.consume();

                        return;
                    }


                    if (tipo == null) {

                        mostrarAviso(
                                "Nível de acesso",
                                "Selecione o nível de acesso."
                        );

                        evento.consume();

                        return;
                    }


                    UsuarioDAO dao =
                            new UsuarioDAO();


                    // Guardamos os dados anteriores caso algo dê errado.
                    String nomeAnterior =
                            usuario.getNome();

                    String emailAnterior =
                            usuario.getEmail();

                    String tipoAnterior =
                            usuario.getTipo();


                    usuario.setNome(
                            nome
                    );

                    usuario.setEmail(
                            email
                    );

                    usuario.setTipo(
                            tipo
                    );


                    try {

                        String mensagem =
                                dao.atualizar(
                                        usuario
                                );


                        if (mensagem == null
                                || !mensagem
                                        .toLowerCase()
                                        .contains("sucesso")) {

                            usuario.setNome(
                                    nomeAnterior
                            );

                            usuario.setEmail(
                                    emailAnterior
                            );

                            usuario.setTipo(
                                    tipoAnterior
                            );


                            mostrarErro(
                                    "Não foi possível atualizar",
                                    mensagem != null
                                            ? mensagem
                                            : "Não foi possível salvar as alterações."
                            );

                            evento.consume();

                            return;
                        }


                        mostrarSucesso(
                                "Usuário atualizado",
                                "As alterações foram salvas com sucesso."
                        );


                    } catch (Exception e) {

                        usuario.setNome(
                                nomeAnterior
                        );

                        usuario.setEmail(
                                emailAnterior
                        );

                        usuario.setTipo(
                                tipoAnterior
                        );


                        mostrarErro(
                                "Erro ao atualizar usuário",
                                "Não foi possível salvar as alterações."
                        );

                        e.printStackTrace();

                        evento.consume();
                    }
                }
        );


        Optional<ButtonType> resultado =
                dialog.showAndWait();


        if (resultado.isPresent()
                && resultado.get()
                == btnSalvar) {

            carregarUsuarios();
        }
    }


    // ============================================================
    // LIMPAR FORMULÁRIO
    // ============================================================

    private void limparFormulario() {

        txtNome.clear();
        txtEmail.clear();
        txtLogin.clear();
        txtSenha.clear();

        cbTipo.getSelectionModel()
                .selectFirst();

        txtNome.requestFocus();
    }


    // ============================================================
    // VALIDAÇÕES
    // ============================================================

    private String obterTexto(
            TextField campo) {

        if (campo.getText() == null) {

            return "";
        }

        return campo
                .getText()
                .trim();
    }


    private boolean emailValido(
            String email) {

        return EMAIL_PATTERN
                .matcher(email)
                .matches();
    }


    // ============================================================
    // ALERTAS
    // ============================================================

    private void mostrarSucesso(
            String titulo,
            String mensagem) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Sucesso"
        );

        alert.setHeaderText(
                titulo
        );

        alert.setContentText(
                mensagem
        );

        alert.showAndWait();
    }


    private void mostrarAviso(
            String titulo,
            String mensagem) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Atenção"
        );

        alert.setHeaderText(
                titulo
        );

        alert.setContentText(
                mensagem
        );

        alert.showAndWait();
    }


    private void mostrarErro(
            String titulo,
            String mensagem) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Erro"
        );

        alert.setHeaderText(
                titulo
        );

        alert.setContentText(
                mensagem
        );

        alert.showAndWait();
    }
}