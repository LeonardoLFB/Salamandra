package controller;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import application.PermissoesUsuario;
import application.SessaoUsuario;
import database.ClienteDAO;
import database.ProdutoDAO;
import database.VendaDAO;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Produto;
import model.Usuario;
import model.Venda;

public class MenuController implements Initializable {

    // ============================================================
    // MENU / SIDEBAR
    // ============================================================

    @FXML
    private Label Menu;

    @FXML
    private Label MenuClose;

    @FXML
    private AnchorPane Slider;

    @FXML
    private JFXButton btClientes;

    @FXML
    private JFXButton btEstoque;

    @FXML
    private JFXButton btFornecedores;

    @FXML
    private JFXButton btProdutos;

    @FXML
    private JFXButton btUsuarios;

    @FXML
    private JFXButton btVendas;

    // ============================================================
    // ATALHOS DO CABEÇALHO
    // ============================================================

    @FXML
    private JFXButton btAtalhoClientes;

    @FXML
    private JFXButton btAtalhoFornecedores;

    @FXML
    private JFXButton btAtalhoVendas;

    // ============================================================
    // USUÁRIO LOGADO
    // ============================================================

    @FXML
    private Label lblUsuarioLogado;

    @FXML
    private Label lblPerfilUsuario;

    // ============================================================
    // DASHBOARD
    // ============================================================

    @FXML
    private Label lblQtdClientes;

    @FXML
    private Label lblQtdProdutos;

    @FXML
    private Label lblEstoqueBaixo;

    @FXML
    private Label lblQtdVendas;

    @FXML
    private Label lblFaturamento;

    @FXML
    private Label lblPendentes;

    @FXML
    private VBox boxUltimasVendas;

    @FXML
    private VBox boxEstoqueBaixo;

    @FXML
    private StackPane conteudoPrincipal;

    @FXML
    private VBox dashboardPrincipal;

    @FXML
    private JFXButton btSair;

    private Parent dashboardInicial;

    private static final double SIDEBAR_WIDTH = 176;

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Sidebar começa fora do layout
        Slider.setVisible(false);
        Slider.setManaged(false);
        Slider.setTranslateX(0);

        // Cursores
        Menu.setCursor(Cursor.HAND);
        MenuClose.setCursor(Cursor.HAND);

        // Estado inicial dos botões do menu
        Menu.setVisible(true);
        Menu.setManaged(true);

        MenuClose.setVisible(false);
        MenuClose.setManaged(false);

        // Eventos
        Menu.setOnMouseClicked(e -> openSidebar());
        MenuClose.setOnMouseClicked(e -> closeSidebar());

        dashboardInicial = dashboardPrincipal;

        carregarIndicadores();
        carregarUsuarioLogado();
        aplicarPermissoesUsuario();
    }

    // ============================================================
    // DASHBOARD
    // ============================================================

    private void carregarIndicadores() {

        try {

            List<Produto> produtos = new ProdutoDAO().getAll();
            List<Venda> vendas = new VendaDAO().getAll();

            int estoqueBaixo = 0;

            for (Produto p : produtos) {

                if (p.getQtdeEstoque() <= 10) {
                    estoqueBaixo++;
                }
            }

            int pendentes = 0;
            double faturamento = 0.0;

            for (Venda v : vendas) {

                if ("Pendente".equals(v.getStatus())) {
                    pendentes++;
                }

                if ("Concluída".equals(v.getStatus())) {
                    faturamento += v.getValorTotal();
                }
            }

            lblQtdClientes.setText(
                String.valueOf(
                    new ClienteDAO().getAll().size()
                )
            );

            lblQtdProdutos.setText(
                String.valueOf(produtos.size())
            );

            lblEstoqueBaixo.setText(
                String.valueOf(estoqueBaixo)
            );

            lblQtdVendas.setText(
                String.valueOf(vendas.size())
            );

            lblPendentes.setText(
                String.valueOf(pendentes)
            );

            lblFaturamento.setText(
                String.format("R$ %.2f", faturamento)
            );

            carregarUltimasVendas(vendas);
            carregarProdutosEstoqueBaixo(produtos);

        } catch (Exception e) {

            System.err.println(
                "Não foi possível carregar os indicadores: "
                + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private void carregarUltimasVendas(List<Venda> vendas) {

        boxUltimasVendas.getChildren().clear();

        if (vendas.isEmpty()) {

            Label vazio =
                new Label("Nenhuma venda encontrada.");

            vazio.getStyleClass().add(
                "dashboard-empty"
            );

            boxUltimasVendas
                .getChildren()
                .add(vazio);

            return;
        }

        vendas.sort(
            Comparator.comparing(
                Venda::getData,
                Comparator.nullsLast(
                    Comparator.naturalOrder()
                )
            ).reversed()
        );

        int limite = Math.min(vendas.size(), 5);

        for (int i = 0; i < limite; i++) {

            Venda venda = vendas.get(i);

            VBox informacoes = new VBox(2);

            String cliente = venda.getNomeCliente();

            if (cliente == null || cliente.isBlank()) {
                cliente = "Cliente não informado";
            }

            Label titulo = new Label(
                "Venda #" + venda.getIdVenda()
                + " - " + cliente
            );

            titulo.getStyleClass().add(
                "dashboard-item-title"
            );

            Label detalhes = new Label(
                venda.getDataFormatada()
                + " • "
                + venda.getStatus()
            );

            detalhes.getStyleClass().add(
                "dashboard-item-sub"
            );

            informacoes.getChildren().addAll(
                titulo,
                detalhes
            );

            Label valor = new Label(
                venda.getValorFormatado()
            );

            valor.getStyleClass().add(
                "dashboard-item-title"
            );

            HBox linha = new HBox();

            linha.setSpacing(10);

            Region espaco = new Region();

            HBox.setHgrow(
                espaco,
                Priority.ALWAYS
            );

            linha.getChildren().addAll(
                informacoes,
                espaco,
                valor
            );

            linha.getStyleClass().add(
                "dashboard-item"
            );

            boxUltimasVendas
                .getChildren()
                .add(linha);
        }
    }

    private void carregarProdutosEstoqueBaixo(
            List<Produto> produtos) {

        boxEstoqueBaixo
            .getChildren()
            .clear();

        List<Produto> produtosBaixos =
            produtos.stream()
                .filter(
                    p -> p.getQtdeEstoque() <= 10
                )
                .sorted(
                    Comparator.comparingInt(
                        Produto::getQtdeEstoque
                    )
                )
                .limit(5)
                .toList();

        if (produtosBaixos.isEmpty()) {

            Label vazio = new Label(
                "Nenhum produto com estoque baixo."
            );

            vazio.getStyleClass().add(
                "dashboard-empty"
            );

            boxEstoqueBaixo
                .getChildren()
                .add(vazio);

            return;
        }

        for (Produto produto : produtosBaixos) {

            VBox informacoes = new VBox(2);

            Label nome =
                new Label(produto.getNome());

            nome.getStyleClass().add(
                "dashboard-item-title"
            );

            Label codigo = new Label(
                "Código: " + produto.getCodigo()
            );

            codigo.getStyleClass().add(
                "dashboard-item-sub"
            );

            informacoes.getChildren().addAll(
                nome,
                codigo
            );

            Label estoque = new Label(
                produto.getQtdeEstoque()
                + " un."
            );

            estoque.getStyleClass().add(
                "dashboard-item-title"
            );

            HBox linha = new HBox();

            linha.setSpacing(10);

            Region espaco = new Region();

            HBox.setHgrow(
                espaco,
                Priority.ALWAYS
            );

            linha.getChildren().addAll(
                informacoes,
                espaco,
                estoque
            );

            linha.getStyleClass().add(
                "dashboard-item"
            );

            boxEstoqueBaixo
                .getChildren()
                .add(linha);
        }
    }

    // ============================================================
    // SIDEBAR
    // ============================================================

    private void openSidebar() {

        Slider.setManaged(true);
        Slider.setVisible(true);

        Slider.setTranslateX(
            -SIDEBAR_WIDTH
        );

        Menu.setVisible(false);
        Menu.setManaged(false);

        MenuClose.setVisible(true);
        MenuClose.setManaged(true);

        TranslateTransition slide =
            new TranslateTransition(
                Duration.seconds(0.3),
                Slider
            );

        slide.setToX(0);
        slide.play();
    }

    private void closeSidebar() {

        TranslateTransition slide =
            new TranslateTransition(
                Duration.seconds(0.3),
                Slider
            );

        slide.setToX(
            -SIDEBAR_WIDTH
        );

        slide.setOnFinished(e -> {

            Slider.setVisible(false);
            Slider.setManaged(false);

            Slider.setTranslateX(0);

            MenuClose.setVisible(false);
            MenuClose.setManaged(false);

            Menu.setVisible(true);
            Menu.setManaged(true);
        });

        slide.play();
    }

    // ============================================================
    // NAVEGAÇÃO + PROTEÇÃO DE ACESSO
    // ============================================================

    @FXML
    public void OnBtProdutosClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarProdutos()) {

            mostrarAcessoNegado();
            return;
        }

        carregarTela("/view/Produtos.fxml");
    }

    @FXML
    public void OnBtEstoqueClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarEstoque()) {

            mostrarAcessoNegado();
            return;
        }

        carregarTela("/view/Estoque.fxml");
    }

    @FXML
    public void OnBtFornecedoresClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarFornecedores()) {

            mostrarAcessoNegado();
            return;
        }

        carregarTela("/view/Fornecedores.fxml");
    }

    @FXML
    public void OnBtClientesClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarClientes()) {

            mostrarAcessoNegado();
            return;
        }

        carregarTela("/view/Clientes.fxml");
    }

    @FXML
    public void OnBtUsuariosClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarUsuarios()) {

            mostrarAcessoNegado();
            return;
        }

        carregarTela("/view/Usuarios.fxml");
    }

    @FXML
    public void OnBtVendasClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarVendas()) {

            mostrarAcessoNegado();
            return;
        }

        carregarTela("/view/Vendas.fxml");
    }

    private void mostrarAcessoNegado() {

        Alert alerta =
            new Alert(Alert.AlertType.WARNING);

        alerta.setTitle(
            "Acesso restrito"
        );

        alerta.setHeaderText(
            "Você não possui permissão para acessar esta área."
        );

        alerta.setContentText(
            "Esta funcionalidade não está disponível "
            + "para o seu perfil de usuário."
        );

        alerta.showAndWait();
    }

    private void carregarTela(
            String caminhoFXML) {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        caminhoFXML
                    )
                );

            Parent tela = loader.load();

            conteudoPrincipal
                .getChildren()
                .clear();

            conteudoPrincipal
                .getChildren()
                .add(tela);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // ============================================================
    // VOLTAR PARA O DASHBOARD
    // ============================================================

    @FXML
    private void voltarDashboard() {

        conteudoPrincipal
            .getChildren()
            .clear();

        conteudoPrincipal
            .getChildren()
            .add(dashboardInicial);

        carregarIndicadores();
    }

    // ============================================================
    // USUÁRIO LOGADO
    // ============================================================

    private void carregarUsuarioLogado() {

        if (!SessaoUsuario.temUsuarioLogado()) {

            lblUsuarioLogado.setText(
                "Usuário"
            );

            lblPerfilUsuario.setText("");

            return;
        }

        Usuario usuario =
            SessaoUsuario.getUsuarioLogado();

        lblUsuarioLogado.setText(
            usuario.getNome()
        );

        lblPerfilUsuario.setText(
            usuario.getTipo()
        );
    }

    // ============================================================
    // PERMISSÕES VISUAIS
    // ============================================================

    private void aplicarPermissoesUsuario() {

        // Sidebar

        configurarBotao(
            btProdutos,
            PermissoesUsuario.podeAcessarProdutos()
        );

        configurarBotao(
            btClientes,
            PermissoesUsuario.podeAcessarClientes()
        );

        configurarBotao(
            btFornecedores,
            PermissoesUsuario.podeAcessarFornecedores()
        );

        configurarBotao(
            btVendas,
            PermissoesUsuario.podeAcessarVendas()
        );

        configurarBotao(
            btEstoque,
            PermissoesUsuario.podeAcessarEstoque()
        );

        configurarBotao(
            btUsuarios,
            PermissoesUsuario.podeAcessarUsuarios()
        );

        // Atalhos do cabeçalho

        configurarBotao(
            btAtalhoClientes,
            PermissoesUsuario.podeAcessarClientes()
        );

        configurarBotao(
            btAtalhoFornecedores,
            PermissoesUsuario.podeAcessarFornecedores()
        );

        configurarBotao(
            btAtalhoVendas,
            PermissoesUsuario.podeAcessarVendas()
        );
    }

    private void configurarBotao(
            JFXButton botao,
            boolean permitido) {

        botao.setVisible(permitido);
        botao.setManaged(permitido);
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    @FXML
    private void OnBtSairClick(
            ActionEvent event) {

        Alert alerta =
            new Alert(
                Alert.AlertType.CONFIRMATION
            );

        alerta.setTitle(
            "Confirmação"
        );

        alerta.setHeaderText(
            "Deseja realmente sair do sistema?"
        );

        alerta.setContentText(
            "Você será redirecionado para a tela de login."
        );

        Optional<ButtonType> resultado =
            alerta.showAndWait();

        if (resultado.isPresent()
                && resultado.get()
                == ButtonType.OK) {

            try {

                FXMLLoader loader =
                    new FXMLLoader(
                        getClass().getResource(
                            "/view/TelaLogin.fxml"
                        )
                    );

                Parent telaLogin =
                    loader.load();

                // Encerra a sessão depois que
                // a tela de login carregou corretamente
                SessaoUsuario.encerrarSessao();

                Stage stage =
                    (Stage) btSair
                        .getScene()
                        .getWindow();

                Scene scene =
                    new Scene(telaLogin);

                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}