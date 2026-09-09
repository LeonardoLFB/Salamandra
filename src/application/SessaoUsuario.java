package application;

import model.Usuario;

public class SessaoUsuario {

    private static Usuario usuarioLogado;

    private SessaoUsuario() {
        // Impede a criação de objetos desta classe
    }

    public static void iniciarSessao(Usuario usuario) {
        usuarioLogado = usuario;
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static boolean temUsuarioLogado() {
        return usuarioLogado != null;
    }

    public static void encerrarSessao() {
        usuarioLogado = null;
    }
}