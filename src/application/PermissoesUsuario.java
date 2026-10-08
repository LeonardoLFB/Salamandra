package application;

import model.Usuario;

public class PermissoesUsuario {

    private PermissoesUsuario() {
    }

    private static String getPerfil() {

        Usuario usuario = SessaoUsuario.getUsuarioLogado();

        if (usuario == null || usuario.getTipo() == null) {
            return "";
        }

        return usuario.getTipo().trim().toLowerCase();
    }

    public static boolean isAdministrador() {
        return getPerfil().equals("administrador");
    }

    public static boolean isVendedor() {
        return getPerfil().equals("vendedor");
    }

    public static boolean isEstoquista() {
        return getPerfil().equals("estoquista");
    }

    public static boolean podeAcessarClientes() {
        return isAdministrador() || isVendedor();
    }

    public static boolean podeAcessarVendas() {
        return isAdministrador() || isVendedor();
    }

    public static boolean podeAcessarEstoque() {
        return isAdministrador()
                || isVendedor()
                || isEstoquista();
    }

    public static boolean podeAcessarProdutos() {
        return isAdministrador() || isEstoquista();
    }

    public static boolean podeAcessarFornecedores() {
        return isAdministrador() || isEstoquista();
    }

    public static boolean podeAcessarUsuarios() {
        return isAdministrador();
    }
}