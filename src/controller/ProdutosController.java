package controller;

import database.ProdutoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import model.Produto;

public class ProdutosController extends ProdutoDAO {

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtLote;

    @FXML
    private TextField txtDescricao;

    @FXML
    private TextField txtPrecoCusto;

    @FXML
    private TextField txtPrecoVenda;

    @FXML
    private TextField txtQuantidadeEstoque;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnSalvar;


    @FXML
    private void initialize() {

        // Código: somente números
        txtCodigo.textProperty().addListener((obs, valorAntigo, valorNovo) -> {
            if (!valorNovo.matches("\\d*")) {
                txtCodigo.setText(valorNovo.replaceAll("[^\\d]", ""));
            }
        });

        // Estoque: somente números
        txtQuantidadeEstoque.textProperty().addListener((obs, valorAntigo, valorNovo) -> {
            if (!valorNovo.matches("\\d*")) {
                txtQuantidadeEstoque.setText(valorNovo.replaceAll("[^\\d]", ""));
            }
        });

        // Lote: máximo 5 caracteres
        txtLote.textProperty().addListener((obs, valorAntigo, valorNovo) -> {
            if (valorNovo.length() > 5) {
                txtLote.setText(valorAntigo);
            }
        });

        // Preço de custo: aceita números, vírgula ou ponto
        txtPrecoCusto.textProperty().addListener((obs, valorAntigo, valorNovo) -> {
            if (!valorNovo.matches("\\d*([,.]\\d*)?")) {
                txtPrecoCusto.setText(valorAntigo);
            }
        });

        // Preço de venda: aceita números, vírgula ou ponto
        txtPrecoVenda.textProperty().addListener((obs, valorAntigo, valorNovo) -> {
            if (!valorNovo.matches("\\d*([,.]\\d*)?")) {
                txtPrecoVenda.setText(valorAntigo);
            }
        });
    }


    @FXML
    private void cadastrarProduto() {

        if (!validarCampos()) {
            return;
        }

        try {

            String precoCustoStr =
                    txtPrecoCusto.getText()
                            .trim()
                            .replace(",", ".");

            String precoVendaStr =
                    txtPrecoVenda.getText()
                            .trim()
                            .replace(",", ".");

            double precoCusto =
                    Double.parseDouble(precoCustoStr);

            double precoVenda =
                    Double.parseDouble(precoVendaStr);

            int codigo =
                    Integer.parseInt(txtCodigo.getText().trim());

            int quantidadeEstoque =
                    Integer.parseInt(
                            txtQuantidadeEstoque.getText().trim()
                    );


            if (precoCusto < 0) {
                mostrarErro(
                        "Preço de custo inválido",
                        "O preço de custo não pode ser negativo."
                );

                txtPrecoCusto.requestFocus();
                return;
            }

            if (precoVenda < 0) {
                mostrarErro(
                        "Preço de venda inválido",
                        "O preço de venda não pode ser negativo."
                );

                txtPrecoVenda.requestFocus();
                return;
            }

            if (quantidadeEstoque < 0) {
                mostrarErro(
                        "Estoque inválido",
                        "A quantidade em estoque não pode ser negativa."
                );

                txtQuantidadeEstoque.requestFocus();
                return;
            }


            Produto produto = new Produto();

            produto.setNome(
                    txtNome.getText().trim()
            );

            produto.setCodigo(codigo);

            produto.setLote(
                    txtLote.getText().trim()
            );

            produto.setDescricao(
                    txtDescricao.getText().trim()
            );

            produto.setPrecoCusto(precoCusto);

            produto.setPrecoVenda(precoVenda);

            produto.setQtdeEstoque(
                    quantidadeEstoque
            );


            ProdutoDAO dao = new ProdutoDAO();

            String mensagem =
                    dao.inserir(produto);


            Alert alert =
                    new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Produto cadastrado");
            alert.setHeaderText(
                    "Produto cadastrado com sucesso"
            );

            alert.setContentText(mensagem);

            alert.showAndWait();


            limparCampos();

            txtNome.requestFocus();


        } catch (NumberFormatException e) {

            mostrarErro(
                    "Valor inválido",
                    "Verifique os campos numéricos do cadastro."
            );

        } catch (Exception e) {

            mostrarErro(
                    "Erro ao cadastrar produto",
                    "Não foi possível cadastrar o produto.\n\n"
                    + e.getMessage()
            );
        }
    }


    private boolean validarCampos() {

        if (txtNome.getText().trim().isEmpty()) {

            mostrarErro(
                    "Campo obrigatório",
                    "Informe o nome do produto."
            );

            txtNome.requestFocus();

            return false;
        }


        if (txtCodigo.getText().trim().isEmpty()) {

            mostrarErro(
                    "Campo obrigatório",
                    "Informe o código do produto."
            );

            txtCodigo.requestFocus();

            return false;
        }


        if (txtLote.getText().trim().isEmpty()) {

            mostrarErro(
                    "Campo obrigatório",
                    "Informe o lote do produto."
            );

            txtLote.requestFocus();

            return false;
        }


        if (txtPrecoCusto.getText().trim().isEmpty()) {

            mostrarErro(
                    "Campo obrigatório",
                    "Informe o preço de custo."
            );

            txtPrecoCusto.requestFocus();

            return false;
        }


        if (txtPrecoVenda.getText().trim().isEmpty()) {

            mostrarErro(
                    "Campo obrigatório",
                    "Informe o preço de venda."
            );

            txtPrecoVenda.requestFocus();

            return false;
        }


        if (txtQuantidadeEstoque.getText().trim().isEmpty()) {

            mostrarErro(
                    "Campo obrigatório",
                    "Informe a quantidade inicial em estoque."
            );

            txtQuantidadeEstoque.requestFocus();

            return false;
        }


        return true;
    }


    @FXML
    private void cancelar() {

        limparCampos();

        txtNome.requestFocus();
    }


    private void limparCampos() {

        txtNome.clear();
        txtCodigo.clear();
        txtLote.clear();
        txtDescricao.clear();
        txtPrecoCusto.clear();
        txtPrecoVenda.clear();
        txtQuantidadeEstoque.clear();
    }


    private void mostrarErro(
            String titulo,
            String mensagem) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Atenção");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);

        alert.showAndWait();
    }
}