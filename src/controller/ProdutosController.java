package controller;

import database.ProdutoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
	private void cadastrarProduto() {
		try {
			
			//converte a virgula em ponto pq double nao salva com virgula
			String precoCustoStr = txtPrecoCusto.getText().replace(",", ".");
			String precoVendaStr = txtPrecoVenda.getText().replace(",", ".");
			
			Produto p = new Produto();
			p.setNome(txtNome.getText());
			p.setCodigo(Integer.parseInt(txtCodigo.getText()));
			p.setLote(txtLote.getText());
			p.setDescricao(txtDescricao.getText());
			p.setPrecoCusto(Double.parseDouble(precoCustoStr));
			p.setPrecoVenda(Double.parseDouble(precoVendaStr));
			p.setQtdeEstoque(Integer.parseInt(txtQuantidadeEstoque.getText()));
			
			if (p.getLote().length() > 5) {
			    Alert alert = new Alert(Alert.AlertType.ERROR);
			    alert.setHeaderText("Lote inválido");
			    alert.setContentText("O lote deve ter no máximo 5 caracteres.");
			    alert.showAndWait();
			    return;
			}

			ProdutoDAO dao = new ProdutoDAO();
			String msg = dao.inserir(p);

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText(null);
			alert.setContentText(msg);
			alert.showAndWait();
			

		} catch (Exception e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Erro ao cadastrar produto");
			alert.setContentText(e.getMessage());
			alert.show();
		}
	}
	
	@FXML
	private void cancelar() {
		Stage stage = (Stage) btnCancelar.getScene().getWindow();
	    stage.close();
	}
}