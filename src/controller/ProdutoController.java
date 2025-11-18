package controller;

import model.Produto;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import database.ProdutoDAO;

public class ProdutoController {
	
	@FXML
    private TextField txtNome;
    
    @FXML
    private TextField txtCodigo;
    
    @FXML
    private TextField txtQuantidade;
    
    @FXML
    private TextField txtPreco;
    
    @FXML
    private TextField txtCategoria;
    
    @FXML
    private TextField txtLote;
    
    @FXML
    private TextField txtFornecedor;
    
    @FXML
    private TextField txtDescricao;
    
    @FXML
    private Button btnSalvar;
    
    @FXML
    private Button btnCancelar;

    @FXML
    public void initialize() {
        // Configurações iniciais se necessário
    }
    
    @FXML
    private void cancelar() {
        // Limpa os campos
        limparCampos();
        
        // Fecha a janela
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
    
	
	private void limparCampos() {
        txtNome.clear();
        txtCodigo.clear();
        txtQuantidade.clear();
        txtPreco.clear();
        txtCategoria.clear();
        txtLote.clear();
        txtFornecedor.clear();
        txtDescricao.clear();
    }
    
    /**
     * Exibe um alerta na tela
     */
    private void mostrarAlerta(String titulo, String mensagem, AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
	
	private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty() ||
            txtCodigo.getText().trim().isEmpty() ||
            txtQuantidade.getText().trim().isEmpty() ||
            txtPreco.getText().trim().isEmpty() ||
            txtLote.getText().trim().isEmpty() ||
            txtDescricao.getText().trim().isEmpty()) {
            
            mostrarAlerta("Atenção", 
                "Por favor, preencha todos os campos obrigatórios!", 
                AlertType.WARNING);
            return false;
        }
        return true;
    }
	
    /**
     * Método chamado quando o botão Salvar é clicado
     */
    @FXML
    private void salvarProduto() {
        try {
            // Validação dos campos obrigatórios
            if (!validarCampos()) {
                return;
            }
            
            // Captura os dados dos campos
            int codigo = Integer.parseInt(txtCodigo.getText().trim());
            String nome = txtNome.getText().trim();
            int qtdeEstoque = Integer.parseInt(txtQuantidade.getText().trim());
            double preco = Double.parseDouble(txtPreco.getText().trim());
            String lote = txtLote.getText().trim();
            String descricao = txtDescricao.getText().trim();
            
            // Cria o objeto Produto
            Produto produto = new Produto(codigo, nome, qtdeEstoque, preco, lote, descricao);
            
            // Cria o DAO e insere no banco
            ProdutoDAO dao = new ProdutoDAO();
            // Preenche o DAO com os dados do produto
            dao.setCodigo(codigo);
            dao.setNome(nome);
            dao.setQtdeEstoque(qtdeEstoque);
            dao.setPreco(preco);
            dao.setLote(lote);
            dao.setDescricao(descricao);
            
            String resultado = dao.inserir();
            
            // Exibe mensagem de sucesso ou erro
            if (resultado.contains("sucesso")) {
                mostrarAlerta("Sucesso", resultado, AlertType.INFORMATION);
                limparCampos();
            } else {
                mostrarAlerta("Erro", resultado, AlertType.ERROR);
            }
            
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Formato", 
                "Por favor, verifique se os campos numéricos estão preenchidos corretamente:\n" +
                "- Código: número inteiro\n" +
                "- Quantidade: número inteiro\n" +
                "- Preço: número decimal", 
                AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar produto: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Método chamado quando o botão Cancelar é clicado
     */
    
	
}
