package database;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import model.Usuario;

public class UsuarioDAO {

    // ============================================================
    // CONFIGURAÇÃO DAS SENHAS
    // ============================================================

    private static final String ALGORITMO =
            "PBKDF2WithHmacSHA256";

    private static final int ITERACOES =
            120000;

    private static final int TAMANHO_SALT =
            16;

    private static final int TAMANHO_HASH =
            256;


    // ============================================================
    // INSERIR
    // ============================================================

    public String inserir(Usuario usuario) {

        String mensagem =
                "Usuário inserido com sucesso!";

        BD bd = new BD();

        try {

            bd.getConnection();


            // ====================================================
            // LOGIN DUPLICADO
            // ====================================================

            if (loginJaExiste(
                    bd,
                    usuario.getLogin(),
                    0)) {

                return "Já existe um usuário com este login.";
            }


            // ====================================================
            // E-MAIL DUPLICADO
            // ====================================================

            if (emailJaExiste(
                    bd,
                    usuario.getEmail(),
                    0)) {

                return "Já existe um usuário com este e-mail.";
            }


            // ====================================================
            // SENHA SEGURA
            // ====================================================

            String senhaSegura =
                    gerarHashSenha(
                            usuario.getSenha()
                    );


            // ====================================================
            // INSERT
            // ====================================================

            String sql =
                    "INSERT INTO usuario " +
                    "(nome, login, senha, email, tipo_acesso) " +
                    "VALUES (?, ?, ?, ?, ?)";


            bd.st = bd.con.prepareStatement(
                    sql
            );


            bd.st.setString(
                    1,
                    usuario.getNome().trim()
            );


            bd.st.setString(
                    2,
                    usuario.getLogin().trim()
            );


            bd.st.setString(
                    3,
                    senhaSegura
            );


            bd.st.setString(
                    4,
                    usuario.getEmail().trim()
            );


            bd.st.setString(
                    5,
                    usuario.getTipo()
            );


            int linhasAfetadas =
                    bd.st.executeUpdate();


            if (linhasAfetadas == 0) {

                mensagem =
                        "Não foi possível cadastrar o usuário.";
            }


        } catch (SQLException e) {

            mensagem =
                    tratarErroBanco(
                            e,
                            "Falha ao cadastrar usuário."
                    );

            e.printStackTrace();


        } catch (Exception e) {

            mensagem =
                    "Falha ao proteger a senha do usuário.";

            e.printStackTrace();


        } finally {

            bd.close();
        }


        return mensagem;
    }


    // ============================================================
    // ATUALIZAR
    // ============================================================

    public String atualizar(Usuario usuario) {

        String mensagem =
                "Usuário atualizado com sucesso!";

        BD bd = new BD();

        try {

            bd.getConnection();


            // ====================================================
            // LOGIN DUPLICADO
            // ====================================================

            if (loginJaExiste(
                    bd,
                    usuario.getLogin(),
                    usuario.getId())) {

                return "Já existe outro usuário com este login.";
            }


            // ====================================================
            // E-MAIL DUPLICADO
            // ====================================================

            if (emailJaExiste(
                    bd,
                    usuario.getEmail(),
                    usuario.getId())) {

                return "Já existe outro usuário com este e-mail.";
            }


            /*
             * O usuário carregado do banco normalmente já contém
             * uma senha protegida.
             *
             * Caso ainda seja uma senha antiga em texto puro,
             * aproveitamos a atualização para protegê-la.
             */
            String senha =
                    usuario.getSenha();


            if (senha != null
                    && !senha.isBlank()
                    && !senhaProtegida(senha)) {

                senha =
                        gerarHashSenha(
                                senha
                        );
            }


            String sql =
                    "UPDATE usuario " +
                    "SET nome = ?, " +
                    "login = ?, " +
                    "senha = ?, " +
                    "email = ?, " +
                    "tipo_acesso = ? " +
                    "WHERE id_usuario = ?";


            bd.st = bd.con.prepareStatement(
                    sql
            );


            bd.st.setString(
                    1,
                    usuario.getNome().trim()
            );


            bd.st.setString(
                    2,
                    usuario.getLogin().trim()
            );


            bd.st.setString(
                    3,
                    senha
            );


            bd.st.setString(
                    4,
                    usuario.getEmail().trim()
            );


            bd.st.setString(
                    5,
                    usuario.getTipo()
            );


            bd.st.setInt(
                    6,
                    usuario.getId()
            );


            int linhasAfetadas =
                    bd.st.executeUpdate();


            if (linhasAfetadas == 0) {

                mensagem =
                        "Usuário não encontrado.";
            }


        } catch (SQLException e) {

            mensagem =
                    tratarErroBanco(
                            e,
                            "Falha ao atualizar usuário."
                    );

            e.printStackTrace();


        } catch (Exception e) {

            mensagem =
                    "Falha ao atualizar o usuário.";

            e.printStackTrace();


        } finally {

            bd.close();
        }


        return mensagem;
    }


    // ============================================================
    // DELETAR
    // ============================================================

    public String deletar(int id) {

        String mensagem =
                "Usuário deletado com sucesso!";

        BD bd = new BD();

        try {

            bd.getConnection();


            String sql =
                    "DELETE FROM usuario " +
                    "WHERE id_usuario = ?";


            bd.st = bd.con.prepareStatement(
                    sql
            );


            bd.st.setInt(
                    1,
                    id
            );


            int linhasAfetadas =
                    bd.st.executeUpdate();


            if (linhasAfetadas == 0) {

                mensagem =
                        "Usuário não encontrado.";
            }


        } catch (SQLException e) {

            mensagem =
                    "Falha ao deletar o usuário: "
                    + e.getMessage();

            e.printStackTrace();


        } finally {

            bd.close();
        }


        return mensagem;
    }


    // ============================================================
    // LISTAR TODOS
    // ============================================================

    public List<Usuario> getAll() {

        List<Usuario> usuarios =
                new ArrayList<>();

        BD bd = new BD();

        try {

            bd.getConnection();


            String sql =
                    "SELECT " +
                    "id_usuario, " +
                    "nome, " +
                    "login, " +
                    "senha, " +
                    "email, " +
                    "tipo_acesso " +
                    "FROM usuario " +
                    "ORDER BY nome";


            bd.st = bd.con.prepareStatement(
                    sql
            );


            bd.rs =
                    bd.st.executeQuery();


            while (bd.rs.next()) {

                int id =
                        bd.rs.getInt(
                                "id_usuario"
                        );


                String nome =
                        bd.rs.getString(
                                "nome"
                        );


                String login =
                        bd.rs.getString(
                                "login"
                        );


                String senha =
                        bd.rs.getString(
                                "senha"
                        );


                String email =
                        bd.rs.getString(
                                "email"
                        );


                String tipo =
                        bd.rs.getString(
                                "tipo_acesso"
                        );


                Usuario usuario =
                        new Usuario(
                                id,
                                nome,
                                login,
                                senha,
                                email,
                                tipo
                        );


                usuarios.add(
                        usuario
                );
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro em UsuarioDAO.getAll(): "
                    + e.getMessage()
            );

            e.printStackTrace();


        } finally {

            bd.close();
        }


        System.out.println(
                "UsuarioDAO.getAll() retornou "
                + usuarios.size()
                + " registros."
        );


        return usuarios;
    }


    // ============================================================
    // AUTENTICAÇÃO
    // ============================================================

    public Usuario autenticar(
            String login,
            String senhaInformada) {

        Usuario usuario = null;

        BD bd = new BD();


        if (login == null
                || login.isBlank()
                || senhaInformada == null
                || senhaInformada.isBlank()) {

            return null;
        }


        try {

            bd.getConnection();


            /*
             * Agora buscamos somente pelo login.
             *
             * A senha não é mais comparada diretamente no SQL,
             * pois ela fica armazenada como hash.
             */
            String sql =
                    "SELECT " +
                    "id_usuario, " +
                    "nome, " +
                    "login, " +
                    "senha, " +
                    "email, " +
                    "tipo_acesso " +
                    "FROM usuario " +
                    "WHERE LOWER(login) = LOWER(?)";


            bd.st = bd.con.prepareStatement(
                    sql
            );


            bd.st.setString(
                    1,
                    login.trim()
            );


            bd.rs =
                    bd.st.executeQuery();


            if (bd.rs.next()) {

                int id =
                        bd.rs.getInt(
                                "id_usuario"
                        );


                String nome =
                        bd.rs.getString(
                                "nome"
                        );


                String loginBanco =
                        bd.rs.getString(
                                "login"
                        );


                String senhaBanco =
                        bd.rs.getString(
                                "senha"
                        );


                String email =
                        bd.rs.getString(
                                "email"
                        );


                String tipo =
                        bd.rs.getString(
                                "tipo_acesso"
                        );


                boolean senhaCorreta;


                // =================================================
                // SENHA NOVA COM HASH
                // =================================================

                if (senhaProtegida(
                        senhaBanco)) {

                    senhaCorreta =
                            verificarSenha(
                                    senhaInformada,
                                    senhaBanco
                            );

                } else {

                    // =============================================
                    // COMPATIBILIDADE COM USUÁRIOS ANTIGOS
                    // =============================================

                    senhaCorreta =
                            senhaBanco != null
                            && MessageDigest.isEqual(
                                    senhaBanco.getBytes(),
                                    senhaInformada.getBytes()
                            );


                    /*
                     * Se o login antigo estiver correto,
                     * substituímos automaticamente a senha em
                     * texto puro por uma senha protegida.
                     */
                    if (senhaCorreta) {

                        atualizarSenhaAntiga(
                                bd,
                                id,
                                senhaInformada
                        );
                    }
                }


                if (senhaCorreta) {

                    /*
                     * Evitamos carregar a senha real no objeto
                     * retornado após autenticar.
                     */
                    usuario =
                            new Usuario(
                                    id,
                                    nome,
                                    loginBanco,
                                    senhaBanco,
                                    email,
                                    tipo
                            );
                }
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Falha na conexão com o banco de dados.",
                    e
            );

        } catch (Exception e) {

            System.err.println(
                    "Erro ao verificar senha: "
                    + e.getMessage()
            );

            e.printStackTrace();


        } finally {

            bd.close();
        }


        return usuario;
    }


    // ============================================================
    // USUÁRIO PADRÃO
    // ============================================================

    public void criarUsuarioPadrao() {

        BD bd = new BD();

        try {

            bd.getConnection();


            String sqlCheck =
                    "SELECT COUNT(*) " +
                    "FROM usuario " +
                    "WHERE LOWER(login) = LOWER(?)";


            bd.st = bd.con.prepareStatement(
                    sqlCheck
            );


            bd.st.setString(
                    1,
                    "admin"
            );


            bd.rs =
                    bd.st.executeQuery();


            boolean existeAdmin =
                    false;


            if (bd.rs.next()) {

                existeAdmin =
                        bd.rs.getInt(1) > 0;
            }


            if (!existeAdmin) {

                String senhaPadrao =
                        gerarHashSenha(
                                "admin123"
                        );


                String sqlInsert =
                        "INSERT INTO usuario " +
                        "(nome, login, senha, email, tipo_acesso) " +
                        "VALUES (?, ?, ?, ?, ?)";


                bd.st = bd.con.prepareStatement(
                        sqlInsert
                );


                bd.st.setString(
                        1,
                        "Administrador"
                );


                bd.st.setString(
                        2,
                        "admin"
                );


                bd.st.setString(
                        3,
                        senhaPadrao
                );


                bd.st.setString(
                        4,
                        "admin@sistema.com"
                );


                bd.st.setString(
                        5,
                        "Administrador"
                );


                int resultado =
                        bd.st.executeUpdate();


                if (resultado > 0) {

                    System.out.println(
                            "Usuário administrador padrão criado."
                    );

                    System.out.println(
                            "Login inicial: admin"
                    );

                    System.out.println(
                            "Altere a senha padrão após o primeiro acesso."
                    );
                }

            } else {

                System.out.println(
                        "Usuário administrador padrão já existe."
                );
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro ao criar usuário padrão: "
                    + e.getMessage()
            );

            e.printStackTrace();


        } catch (Exception e) {

            System.err.println(
                    "Erro ao proteger a senha do administrador."
            );

            e.printStackTrace();


        } finally {

            bd.close();
        }
    }


    // ============================================================
    // LOGIN DUPLICADO
    // ============================================================

    private boolean loginJaExiste(
            BD bd,
            String login,
            int idIgnorar)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                "FROM usuario " +
                "WHERE LOWER(login) = LOWER(?) " +
                "AND id_usuario <> ?";


        bd.st = bd.con.prepareStatement(
                sql
        );


        bd.st.setString(
                1,
                login.trim()
        );


        bd.st.setInt(
                2,
                idIgnorar
        );


        bd.rs =
                bd.st.executeQuery();


        return bd.rs.next()
                && bd.rs.getInt(1) > 0;
    }


    // ============================================================
    // E-MAIL DUPLICADO
    // ============================================================

    private boolean emailJaExiste(
            BD bd,
            String email,
            int idIgnorar)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                "FROM usuario " +
                "WHERE LOWER(email) = LOWER(?) " +
                "AND id_usuario <> ?";


        bd.st = bd.con.prepareStatement(
                sql
        );


        bd.st.setString(
                1,
                email.trim()
        );


        bd.st.setInt(
                2,
                idIgnorar
        );


        bd.rs =
                bd.st.executeQuery();


        return bd.rs.next()
                && bd.rs.getInt(1) > 0;
    }


    // ============================================================
    // HASH DA SENHA
    // ============================================================

    private String gerarHashSenha(
            String senha)
            throws Exception {

        SecureRandom random =
                new SecureRandom();


        byte[] salt =
                new byte[TAMANHO_SALT];


        random.nextBytes(
                salt
        );


        PBEKeySpec spec =
                new PBEKeySpec(
                        senha.toCharArray(),
                        salt,
                        ITERACOES,
                        TAMANHO_HASH
                );


        SecretKeyFactory factory =
                SecretKeyFactory.getInstance(
                        ALGORITMO
                );


        byte[] hash =
                factory
                        .generateSecret(spec)
                        .getEncoded();


        spec.clearPassword();


        return "pbkdf2$"
                + ITERACOES
                + "$"
                + Base64.getEncoder()
                        .encodeToString(salt)
                + "$"
                + Base64.getEncoder()
                        .encodeToString(hash);
    }


    // ============================================================
    // VERIFICAR SENHA
    // ============================================================

    private boolean verificarSenha(
            String senhaInformada,
            String senhaSalva)
            throws Exception {

        String[] partes =
                senhaSalva.split("\\$");


        if (partes.length != 4
                || !"pbkdf2".equals(
                        partes[0])) {

            return false;
        }


        int iteracoes =
                Integer.parseInt(
                        partes[1]
                );


        byte[] salt =
                Base64.getDecoder()
                        .decode(
                                partes[2]
                        );


        byte[] hashSalvo =
                Base64.getDecoder()
                        .decode(
                                partes[3]
                        );


        PBEKeySpec spec =
                new PBEKeySpec(
                        senhaInformada.toCharArray(),
                        salt,
                        iteracoes,
                        hashSalvo.length * 8
                );


        SecretKeyFactory factory =
                SecretKeyFactory.getInstance(
                        ALGORITMO
                );


        byte[] hashInformado =
                factory
                        .generateSecret(spec)
                        .getEncoded();


        spec.clearPassword();


        return MessageDigest.isEqual(
                hashSalvo,
                hashInformado
        );
    }


    // ============================================================
    // IDENTIFICA SE É HASH
    // ============================================================

    private boolean senhaProtegida(
            String senha) {

        return senha != null
                && senha.startsWith(
                        "pbkdf2$"
                );
    }


    // ============================================================
    // MIGRA SENHA ANTIGA
    // ============================================================

    private void atualizarSenhaAntiga(
            BD bd,
            int idUsuario,
            String senha)
            throws Exception {

        String senhaSegura =
                gerarHashSenha(
                        senha
                );


        String sql =
                "UPDATE usuario " +
                "SET senha = ? " +
                "WHERE id_usuario = ?";


        bd.st = bd.con.prepareStatement(
                sql
        );


        bd.st.setString(
                1,
                senhaSegura
        );


        bd.st.setInt(
                2,
                idUsuario
        );


        bd.st.executeUpdate();


        System.out.println(
                "Senha antiga convertida para armazenamento seguro - usuário "
                + idUsuario
        );
    }


    // ============================================================
    // TRATAMENTO DE ERRO DO POSTGRESQL
    // ============================================================

    private String tratarErroBanco(
            SQLException e,
            String mensagemPadrao) {

        /*
         * PostgreSQL:
         * 23505 = unique_violation
         */
        if ("23505".equals(
                e.getSQLState())) {

            return "Já existe um usuário com estes dados.";
        }


        return mensagemPadrao;
    }
}