package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.Usuario;

import java.util.Optional;

import database.UsuarioDAO;

public class UsuariosController {

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

	@FXML
	private TextField txtBuscar;
	@FXML
	private ComboBox<String> cbFiltroTipo;

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

	private ObservableList<Usuario> data = FXCollections.observableArrayList();

	@FXML
	private void initialize() {

		// Colunas
		colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
		colUsuario.setCellValueFactory(new PropertyValueFactory<>("login"));
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
		addActionsColumn();

		// Carrega dados
		UsuarioDAO dao = new UsuarioDAO();
		data.addAll(dao.getAll());
		tableUsuarios.setItems(data);

		// Combo principal
		cbTipo.getItems().addAll("Administrador", "Vendedor", "Estoquista");
		cbTipo.getSelectionModel().selectFirst();

		// Filtro
		cbFiltroTipo.getItems().addAll("Todos", "Administrador", "Vendedor", "Estoquista");
		cbFiltroTipo.getSelectionModel().select("Todos");

		// Eventos
		btnCadastrar.setOnAction(e -> onCadastrar());
		txtBuscar.textProperty().addListener((obs, oldV, newV) -> applyFilters());
		cbFiltroTipo.setOnAction(e -> applyFilters());
	}

	/* ================== AÇÕES ================== */

	private void onCadastrar() {
		String nome = txtNome.getText().trim();
		String email = txtEmail.getText().trim();
		String login = txtLogin.getText().trim();
		String senha = txtSenha.getText().trim();
		String tipo = cbTipo.getValue();

		if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
			new Alert(Alert.AlertType.WARNING, "Preencha nome, e-mail e senha.").showAndWait();
			return;
		}

		Usuario u = new Usuario(nome, login, senha, email, tipo);

		UsuarioDAO dao = new UsuarioDAO();
		String msg = dao.inserir(u);

		new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();

		// Atualiza tabela
		data.clear();
		data.addAll(dao.getAll());
		tableUsuarios.refresh();

		// limpa
		txtNome.clear();
		txtEmail.clear();
		txtLogin.clear();
		txtSenha.clear();
		cbTipo.getSelectionModel().selectFirst();
	}

	private void applyFilters() {
		String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase();
		String tipo = cbFiltroTipo.getValue();

		ObservableList<Usuario> filtered = FXCollections.observableArrayList();
		for (Usuario u : data) {
			boolean matchesQ = u.getNome().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q)
					|| u.getLogin().toLowerCase().contains(q);

			boolean matchesTipo = tipo == null || tipo.equals("Todos") || u.getTipo().equals(tipo);

			if (matchesQ && matchesTipo)
				filtered.add(u);
		}
		tableUsuarios.setItems(filtered);
	}

	private void addActionsColumn() {
		Callback<TableColumn<Usuario, Void>, TableCell<Usuario, Void>> cellFactory = param -> new TableCell<>() {
			private final Button btnEdit = new Button("✎");
			private final Button btnDel = new Button("🗑");
			private final HBox box = new HBox(8, btnEdit, btnDel);

			{
				btnEdit.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");
				btnDel.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");

				btnEdit.setOnAction(e -> {
					Usuario u = getTableView().getItems().get(getIndex());
					showEditDialog(u);
				});

				btnDel.setOnAction(e -> {
					Usuario u = getTableView().getItems().get(getIndex());
					Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remover usuário " + u.getNome() + " ?");
					Optional<ButtonType> res = confirm.showAndWait();
					if (res.isPresent() && res.get() == ButtonType.OK) {
						UsuarioDAO dao = new UsuarioDAO();
						dao.deletar(u.getId());

						data.remove(u);
						tableUsuarios.setItems(data);
					}
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty)
					setGraphic(null);
				else
					setGraphic(box);
			}
		};

		colAcoes.setCellFactory(cellFactory);
	}

	private void showEditDialog(Usuario u) {
		Dialog<Usuario> dialog = new Dialog<>();
		dialog.setTitle("Editar usuário");
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		TextField nome = new TextField(u.getNome());
		TextField email = new TextField(u.getEmail());
		ComboBox<String> tipo = new ComboBox<>(
				FXCollections.observableArrayList("Administrador", "Vendedor", "Estoquista"));
		tipo.getSelectionModel().select(u.getTipo());

		GridPane grid = new GridPane();
		grid.setHgap(8);
		grid.setVgap(8);
		grid.addRow(0, new Label("Nome:"), nome);
		grid.addRow(1, new Label("E-mail:"), email);
		grid.addRow(2, new Label("Tipo:"), tipo);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(btn -> {
			if (btn == ButtonType.OK) {
				u.setNome(nome.getText().trim());
				u.setEmail(email.getText().trim());
				u.setTipo(tipo.getValue());

				UsuarioDAO dao = new UsuarioDAO();
				dao.atualizar(u);

				tableUsuarios.refresh();
				return u;
			}
			return null;
		});

		dialog.showAndWait();
	}
}
