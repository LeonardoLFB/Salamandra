package controller;

import java.util.Optional;
import java.util.prefs.Preferences;

import application.Main;
import application.SessaoUsuario;
import database.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Usuario;

public class LoginController {

    // ============================================================
    // CAMPOS
    // ============================================================

    @FXML
    private TextField tfLogin;

    @FXML
    private PasswordField pfSenha;

    @FXML
    private TextField tfSenhaVisivel;

    @FXML
    private CheckBox cbLembrarLogin;

    @FXML
    private Label lblMensagem;

    @FXML
    private Button btMostrarSenha;

    @FXML
    private Button btLogar;

    @FXML
    private Button btCancelar;


    // ============================================================
    // DAO
    // ============================================================

    private final UsuarioDAO usuarioDAO =
            new UsuarioDAO();


    // ============================================================
    // PREFERÊNCIAS
    // ============================================================

    private final Preferences preferences =
            Preferences.userNodeForPackage(
                    LoginController.class
            );


    private static final String CHAVE_LOGIN =
            "ultimo_login";

    private static final String CHAVE_LEMBRAR =
            "lembrar_login";


    // ============================================================
    // ESTADO
    // ============================================================

    private boolean senhaVisivel =
            false;


    // ============================================================
    // INITIALIZE
    // ============================================================

    @FXML
    public void initialize() {

        // Garante que exista o administrador inicial
        usuarioDAO.criarUsuarioPadrao();


        configurarCampos();

        configurarMostrarSenha();

        carregarLoginSalvo();

        ocultarMensagem();


        if (tfLogin.getText().isBlank()) {

            tfLogin.requestFocus();

        } else {

            pfSenha.requestFocus();
        }
    }


    // ============================================================
    // CONFIGURAÇÕES
    // ============================================================

    private void configurarCampos() {

        // Enter no login vai para senha
        tfLogin.setOnAction(
                e -> {

                    ocultarMensagem();

                    pfSenha.requestFocus();
                }
        );


        // Enter no PasswordField faz login
        pfSenha.setOnAction(
                e -> onClickLogar(null)
        );


        // Enter no campo de senha visível também faz login
        tfSenhaVisivel.setOnAction(
                e -> onClickLogar(null)
        );


        // Ao digitar, escondemos a mensagem de erro anterior
        tfLogin.textProperty()
                .addListener(
                        (obs, antigo, novo) ->
                                ocultarMensagem()
                );


        pfSenha.textProperty()
                .addListener(
                        (obs, antigo, novo) -> {

                            ocultarMensagem();

                            if (!senhaVisivel) {

                                tfSenhaVisivel.setText(
                                        novo
                                );
                            }
                        }
                );


        tfSenhaVisivel.textProperty()
                .addListener(
                        (obs, antigo, novo) -> {

                            ocultarMensagem();

                            if (senhaVisivel) {

                                pfSenha.setText(
                                        novo
                                );
                            }
                        }
                );
    }


    // ============================================================
    // MOSTRAR / OCULTAR SENHA
    // ============================================================

    private void configurarMostrarSenha() {

        btMostrarSenha.setOnAction(
                e -> alternarVisibilidadeSenha()
        );
    }


    private void alternarVisibilidadeSenha() {

        senhaVisivel =
                !senhaVisivel;


        if (senhaVisivel) {

            tfSenhaVisivel.setText(
                    pfSenha.getText()
            );


            tfSenhaVisivel.setVisible(
                    true
            );

            tfSenhaVisivel.setManaged(
                    true
            );


            pfSenha.setVisible(
                    false
            );

            pfSenha.setManaged(
                    false
            );


            btMostrarSenha.setText(
                    "◉"
            );


            tfSenhaVisivel.requestFocus();

            tfSenhaVisivel.positionCaret(
                    tfSenhaVisivel
                            .getText()
                            .length()
            );


        } else {

            pfSenha.setText(
                    tfSenhaVisivel.getText()
            );


            pfSenha.setVisible(
                    true
            );

            pfSenha.setManaged(
                    true
            );


            tfSenhaVisivel.setVisible(
                    false
            );

            tfSenhaVisivel.setManaged(
                    false
            );


            btMostrarSenha.setText(
                    "👁"
            );


            pfSenha.requestFocus();

            pfSenha.positionCaret(
                    pfSenha
                            .getText()
                            .length()
            );
        }
    }


    // ============================================================
    // LOGIN
    // ============================================================

    @FXML
    void onClickLogar(
            ActionEvent event) {

        ocultarMensagem();


        String login =
                tfLogin.getText() == null
                        ? ""
                        : tfLogin
                                .getText()
                                .trim();


        String senha =
                obterSenha();


        // ========================================================
        // VALIDAÇÃO
        // ========================================================

        if (login.isEmpty()
                && senha.isEmpty()) {

            mostrarMensagem(
                    "Informe seu usuário e sua senha."
            );

            tfLogin.requestFocus();

            return;
        }


        if (login.isEmpty()) {

            mostrarMensagem(
                    "Informe seu usuário."
            );

            tfLogin.requestFocus();

            return;
        }


        if (senha.isEmpty()) {

            mostrarMensagem(
                    "Informe sua senha."
            );

            focarSenha();

            return;
        }


        // ========================================================
        // PROCESSANDO LOGIN
        // ========================================================

        btLogar.setDisable(
                true
        );

        btLogar.setText(
                "Entrando..."
        );


        try {

            Usuario usuario =
                    usuarioDAO.autenticar(
                            login,
                            senha
                    );


            if (usuario == null) {

                limparSenha();

                mostrarMensagem(
                        "Usuário ou senha incorretos. Verifique os dados e tente novamente."
                );

                focarSenha();

                return;
            }


            // ====================================================
            // SALVA LOGIN CASO SOLICITADO
            // ====================================================

            salvarPreferencias(
                    login
            );


            // ====================================================
            // INICIA SESSÃO
            // ====================================================

            SessaoUsuario.iniciarSessao(
                    usuario
            );


            System.out.println(
                    "Usuário autenticado: "
                    + usuario.getNome()
                    + " | Perfil: "
                    + usuario.getTipo()
            );


            // ====================================================
            // ABRE SISTEMA
            // ====================================================

            Main.goTo(
                    "/view/MenuPrincipal.fxml"
            );


        } catch (Exception e) {

            mostrarMensagem(
                    "Não foi possível entrar no sistema. Verifique a conexão com o banco de dados."
            );

            System.err.println(
                    "Falha ao conectar com o banco de dados."
            );

        } finally { {

            btLogar.setDisable(
                    false
            );

            btLogar.setText(
                    "Entrar"
            );
        }}
    }


    // ============================================================
    // SENHA
    // ============================================================

    private String obterSenha() {

        if (senhaVisivel) {

            return tfSenhaVisivel
                    .getText() == null
                    ? ""
                    : tfSenhaVisivel
                            .getText();

        }


        return pfSenha
                .getText() == null
                ? ""
                : pfSenha
                        .getText();
    }


    private void limparSenha() {

        pfSenha.clear();

        tfSenhaVisivel.clear();
    }


    private void focarSenha() {

        if (senhaVisivel) {

            tfSenhaVisivel.requestFocus();

        } else {

            pfSenha.requestFocus();
        }
    }


    // ============================================================
    // LEMBRAR LOGIN
    // ============================================================

    private void carregarLoginSalvo() {

        boolean lembrar =
                preferences.getBoolean(
                        CHAVE_LEMBRAR,
                        false
                );


        cbLembrarLogin.setSelected(
                lembrar
        );


        if (lembrar) {

            String loginSalvo =
                    preferences.get(
                            CHAVE_LOGIN,
                            ""
                    );


            tfLogin.setText(
                    loginSalvo
            );
        }
    }


    private void salvarPreferencias(
            String login) {

        if (cbLembrarLogin.isSelected()) {

            preferences.put(
                    CHAVE_LOGIN,
                    login
            );


            preferences.putBoolean(
                    CHAVE_LEMBRAR,
                    true
            );


        } else {

            preferences.remove(
                    CHAVE_LOGIN
            );


            preferences.putBoolean(
                    CHAVE_LEMBRAR,
                    false
            );
        }
    }


    // ============================================================
    // MENSAGEM NA TELA
    // ============================================================

    private void mostrarMensagem(
            String mensagem) {

        lblMensagem.setText(
                mensagem
        );


        lblMensagem.setVisible(
                true
        );


        lblMensagem.setManaged(
                true
        );
    }


    private void ocultarMensagem() {

        if (lblMensagem == null) {
            return;
        }


        lblMensagem.setVisible(
                false
        );


        lblMensagem.setManaged(
                false
        );


        lblMensagem.setText(
                ""
        );
    }


    // ============================================================
    // CANCELAR
    // ============================================================

    @FXML
    void onClickCancelar(
            ActionEvent event) {

        Alert alert =
                new Alert(
                        AlertType.CONFIRMATION
                );


        alert.setTitle(
                "Sair do Salamandra"
        );


        alert.setHeaderText(
                "Deseja encerrar o sistema?"
        );


        alert.setContentText(
                "Confirme para fechar o Salamandra."
        );


        Optional<ButtonType> resultado =
                alert.showAndWait();


        if (resultado.isPresent()
                && resultado.get()
                == ButtonType.OK) {

            System.exit(0);
        }
    }
}