package controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
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
import javafx.scene.layout.GridPane;
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

    @FXML
    private JFXButton btRelatorios;

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
    private Label lblBoasVindasTitulo;

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
    private Label lblMinhasVendas;

    @FXML
    private Label lblUltimaAtualizacao;

    @FXML
    private JFXButton btAtualizarDashboard;

    @FXML
    private HBox boxErroDashboard;

    @FXML
    private Label lblErroDashboard;

    @FXML
    private GridPane gridIndicadores;

    @FXML
    private GridPane gridFinanceiro;

    @FXML
    private GridPane gridPaineis;

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

    private JFXButton botaoMenuAtivo;

    private static final double SIDEBAR_WIDTH = 176;

    private static final DateTimeFormatter FORMATO_HORA =
        DateTimeFormatter.ofPattern("HH:mm");

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Slider.setVisible(false);
        Slider.setManaged(false);
        Slider.setTranslateX(0);

        Menu.setCursor(Cursor.HAND);
        MenuClose.setCursor(Cursor.HAND);

        Menu.setVisible(true);
        Menu.setManaged(true);

        MenuClose.setVisible(false);
        MenuClose.setManaged(false);

        Menu.setOnMouseClicked(e -> openSidebar());
        MenuClose.setOnMouseClicked(e -> closeSidebar());

        dashboardInicial = dashboardPrincipal;

        carregarUsuarioLogado();
        carregarIndicadores();
        aplicarPermissoesUsuario();

        animarEntradaDashboard();
    }

    // ============================================================
    // DASHBOARD
    // ============================================================

    private void carregarIndicadores() {

        esconderErroDashboard();

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

            atualizarHorarioAtualizacao();

        } catch (Exception e) {

            System.err.println(
                "Não foi possível carregar os indicadores: "
                + e.getMessage()
            );

            e.printStackTrace();

            mostrarErroDashboard(
                "Não foi possível carregar os indicadores agora. "
                + "Tente novamente em instantes."
            );
        }

        if (SessaoUsuario.temUsuarioLogado()) {

            Usuario usuario =
                    SessaoUsuario.getUsuarioLogado();

            int vendasUsuario =
                    new VendaDAO()
                            .contarVendasPorUsuario(
                                    usuario.getId()
                            );

            lblMinhasVendas.setText(
                    String.valueOf(vendasUsuario)
            );

        } else {

            lblMinhasVendas.setText("0");
        }
    }

    /**
     * Recarrega os indicadores do dashboard manualmente,
     * acionado pelo botão "Atualizar" ao lado das boas-vindas.
     */
    @FXML
    private void OnBtAtualizarDashboardClick(ActionEvent event) {

        RotateTransition giro =
            new RotateTransition(
                Duration.millis(500),
                btAtualizarDashboard
            );

        giro.setByAngle(360);
        giro.play();

        carregarIndicadores();
    }

    private void atualizarHorarioAtualizacao() {

        if (lblUltimaAtualizacao == null) {
            return;
        }

        lblUltimaAtualizacao.setText(
            "Atualizado às " + LocalTime.now().format(FORMATO_HORA)
        );
    }

    private void mostrarErroDashboard(String mensagem) {

        if (boxErroDashboard == null) {
            return;
        }

        lblErroDashboard.setText(mensagem);

        boxErroDashboard.setVisible(true);
        boxErroDashboard.setManaged(true);
    }

    private void esconderErroDashboard() {

        if (boxErroDashboard == null) {
            return;
        }

        boxErroDashboard.setVisible(false);
        boxErroDashboard.setManaged(false);
    }

    /**
     * Pequena animação de entrada (fade + leve deslocamento) aplicada
     * aos blocos do dashboard quando a tela é aberta, para dar uma
     * sensação de painel "vivo" em vez de conteúdo estático.
     */
    private void animarEntradaDashboard() {

        animarBloco(gridIndicadores, 0);
        animarBloco(gridFinanceiro, 80);
        animarBloco(gridPaineis, 160);
    }

    private void animarBloco(Region bloco, double atrasoMs) {

        if (bloco == null) {
            return;
        }

        bloco.setOpacity(0);
        bloco.setTranslateY(12);

        FadeTransition fade =
            new FadeTransition(Duration.millis(320), bloco);

        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(atrasoMs));

        TranslateTransition desliza =
            new TranslateTransition(Duration.millis(320), bloco);

        desliza.setFromY(12);
        desliza.setToY(0);
        desliza.setDelay(Duration.millis(atrasoMs));

        fade.play();
        desliza.play();
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

            String responsavel = venda.getNomeUsuario();

            if (responsavel == null || responsavel.isBlank()) {
                responsavel = "Não informado";
            }

            Label detalhes = new Label(
                venda.getDataFormatada()
                + " • "
                + venda.getStatus()
                + " • "
                + responsavel
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

        marcarItemAtivo(btProdutos);
        carregarTela("/view/Produtos.fxml");
    }

    @FXML
    public void OnBtEstoqueClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarEstoque()) {

            mostrarAcessoNegado();
            return;
        }

        marcarItemAtivo(btEstoque);
        carregarTela("/view/Estoque.fxml");
    }

    @FXML
    public void OnBtFornecedoresClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarFornecedores()) {

            mostrarAcessoNegado();
            return;
        }

        marcarItemAtivo(btFornecedores);
        carregarTela("/view/Fornecedores.fxml");
    }

    @FXML
    public void OnBtClientesClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarClientes()) {

            mostrarAcessoNegado();
            return;
        }

        marcarItemAtivo(btClientes);
        carregarTela("/view/Clientes.fxml");
    }

    @FXML
    public void OnBtUsuariosClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarUsuarios()) {

            mostrarAcessoNegado();
            return;
        }

        marcarItemAtivo(btUsuarios);
        carregarTela("/view/Usuarios.fxml");
    }

    @FXML
    public void OnBtVendasClick(ActionEvent event) {

        if (!PermissoesUsuario.podeAcessarVendas()) {

            mostrarAcessoNegado();
            return;
        }

        marcarItemAtivo(btVendas);
        carregarTela("/view/Vendas.fxml");
    }

    @FXML
    public void OnBtRelatoriosClick(ActionEvent event) {

        marcarItemAtivo(btRelatorios);
        carregarTela("/view/Relatorios.fxml");
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

    /**
     * Aplica o destaque visual (classe "nav-item-active") apenas
     * no item do menu lateral correspondente à tela aberta, e
     * remove do item que estava marcado anteriormente.
     */
    private void marcarItemAtivo(JFXButton botaoClicado) {

        if (botaoMenuAtivo != null) {

            botaoMenuAtivo
                .getStyleClass()
                .remove("nav-item-active");
        }

        if (botaoClicado != null) {

            botaoClicado
                .getStyleClass()
                .add("nav-item-active");
        }

        botaoMenuAtivo = botaoClicado;
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

        marcarItemAtivo(null);

        carregarIndicadores();
        animarEntradaDashboard();
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

            if (lblBoasVindasTitulo != null) {

                lblBoasVindasTitulo.setText(
                    "Bem-vindo à Salamandra"
                );
            }

            return;
        }

        Usuario usuario =
            SessaoUsuario.getUsuarioLogado();

        lblUsuarioLogado.setText(
        	    "Olá, " + usuario.getNome()
        	);

        	lblPerfilUsuario.setText(
        	    usuario.getTipo()
        	);

        if (lblBoasVindasTitulo != null) {

            String primeiroNome =
                usuario.getNome().split(" ")[0];

            lblBoasVindasTitulo.setText(
                "Bem-vindo, " + primeiroNome + "!"
            );
        }
    }

    // ============================================================
    // PERMISSÕES VISUAIS
    // ============================================================

    private void aplicarPermissoesUsuario() {

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