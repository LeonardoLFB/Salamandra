package database;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.SessaoUsuario;

import model.Auditoria;
import model.Produto;
import model.Usuario;


// ============================================================
// DAO DE PRODUTOS
// ============================================================

@SuppressWarnings("unused")
public class ProdutoDAO {

    public List<Produto> lista =
            new ArrayList<Produto>();


    // ============================================================
    // INSERIR PRODUTO
    // ============================================================

    /**
     * Insere no banco de dados o estado atual do produto.
     *
     * @param p Produto preenchido
     * @return Mensagem contendo o resultado da operação
     */
    public String inserir(Produto p) {

        String mensagem =
                "Produto inserido com sucesso!";

        BD bd = new BD();

        bd.getConnection();


        String sql =
                "INSERT INTO produto " +
                "(nome, codigo, lote, descricao, preco_custo, preco_venda, quantidade_estoque) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "RETURNING id_produto";


        try {

            bd.st = bd.con.prepareStatement(
                    sql
            );


            bd.st.setString(
                    1,
                    p.getNome()
            );

            bd.st.setInt(
                    2,
                    p.getCodigo()
            );

            bd.st.setString(
                    3,
                    p.getLote()
            );

            bd.st.setString(
                    4,
                    p.getDescricao()
            );

            bd.st.setDouble(
                    5,
                    p.getPrecoCusto()
            );

            bd.st.setDouble(
                    6,
                    p.getPrecoVenda()
            );

            bd.st.setInt(
                    7,
                    p.getQtdeEstoque()
            );


            bd.rs =
                    bd.st.executeQuery();


            if (bd.rs.next()) {

                int idProdutoGerado =
                        bd.rs.getInt(
                                "id_produto"
                        );


                p.setId(
                        idProdutoGerado
                );


                System.out.println(
                        "Produto inserido - ID: "
                        + idProdutoGerado
                );


                // ========================================================
                // AUDITORIA - CADASTRO
                // ========================================================

                Usuario usuarioLogado =
                        SessaoUsuario.getUsuarioLogado();


                if (usuarioLogado != null) {

                    Auditoria auditoria =
                            new Auditoria(
                                    usuarioLogado.getId(),
                                    "CADASTRO_PRODUTO",
                                    "Cadastrou o produto #"
                                    + idProdutoGerado
                                    + " - "
                                    + p.getNome()
                            );


                    new AuditoriaDAO()
                            .registrar(
                                    auditoria
                            );
                }

            } else {

                mensagem =
                        "Não foi possível cadastrar o produto.";
            }


        } catch (SQLException e) {

            mensagem =
                    "Falha na inclusão do produto: "
                    + e.getMessage();

            e.printStackTrace();

        } finally {

            bd.close();
        }


        return mensagem;
    }


    // ============================================================
    // ATUALIZAR PRODUTO
    // ============================================================

    /**
     * Atualiza um produto já existente.
     *
     * @param p Produto com ID preenchido
     * @return Mensagem contendo o resultado da operação
     */
    public String atualizar(Produto p) {

        String mensagem =
                "Produto atualizado com sucesso!";

        BD bd = new BD();

        bd.getConnection();


        String sql =
                "UPDATE produto " +
                "SET nome = ?, " +
                "codigo = ?, " +
                "lote = ?, " +
                "descricao = ?, " +
                "preco_custo = ?, " +
                "preco_venda = ?, " +
                "quantidade_estoque = ? " +
                "WHERE id_produto = ?";


        try {

            bd.st =
                    bd.con.prepareStatement(
                            sql
                    );


            bd.st.setString(
                    1,
                    p.getNome()
            );

            bd.st.setInt(
                    2,
                    p.getCodigo()
            );

            bd.st.setString(
                    3,
                    p.getLote()
            );

            bd.st.setString(
                    4,
                    p.getDescricao()
            );

            bd.st.setDouble(
                    5,
                    p.getPrecoCusto()
            );

            bd.st.setDouble(
                    6,
                    p.getPrecoVenda()
            );

            bd.st.setInt(
                    7,
                    p.getQtdeEstoque()
            );

            bd.st.setInt(
                    8,
                    p.getId()
            );


            int linhasAfetadas =
                    bd.st.executeUpdate();


            if (linhasAfetadas == 0) {

                mensagem =
                        "Produto não encontrado.";

            } else {

                System.out.println(
                        "Produto atualizado - ID: "
                        + p.getId()
                );


                // ========================================================
                // AUDITORIA - ALTERAÇÃO
                // ========================================================

                Usuario usuarioLogado =
                        SessaoUsuario.getUsuarioLogado();


                if (usuarioLogado != null) {

                    Auditoria auditoria =
                            new Auditoria(
                                    usuarioLogado.getId(),
                                    "ALTERACAO_PRODUTO",
                                    "Alterou o produto #"
                                    + p.getId()
                                    + " - "
                                    + p.getNome()
                            );


                    new AuditoriaDAO()
                            .registrar(
                                    auditoria
                            );
                }
            }


        } catch (SQLException e) {

            mensagem =
                    "Falha na alteração do produto: "
                    + e.getMessage();

            e.printStackTrace();

        } finally {

            bd.close();
        }


        return mensagem;
    }


    // ============================================================
    // DELETAR PRODUTO
    // ============================================================

    /**
     * Exclui um produto pelo ID.
     *
     * @param id ID do produto
     * @return Mensagem contendo o resultado da operação
     */
    public String deletar(int id) {

        String mensagem =
                "Produto deletado com sucesso!";

        BD bd = new BD();

        bd.getConnection();


        String sql =
                "DELETE FROM produto " +
                "WHERE id_produto = ? " +
                "RETURNING id_produto, nome";


        try {

            bd.st =
                    bd.con.prepareStatement(
                            sql
                    );


            bd.st.setInt(
                    1,
                    id
            );


            bd.rs =
                    bd.st.executeQuery();


            if (!bd.rs.next()) {

                mensagem =
                        "Produto não encontrado ou não foi possível deletar.";

            } else {

                int idProdutoDeletado =
                        bd.rs.getInt(
                                "id_produto"
                        );


                String nomeProduto =
                        bd.rs.getString(
                                "nome"
                        );


                System.out.println(
                        "Produto deletado - ID: "
                        + idProdutoDeletado
                );


                // ========================================================
                // AUDITORIA - EXCLUSÃO
                // ========================================================

                Usuario usuarioLogado =
                        SessaoUsuario.getUsuarioLogado();


                if (usuarioLogado != null) {

                    Auditoria auditoria =
                            new Auditoria(
                                    usuarioLogado.getId(),
                                    "EXCLUSAO_PRODUTO",
                                    "Excluiu o produto #"
                                    + idProdutoDeletado
                                    + " - "
                                    + nomeProduto
                            );


                    new AuditoriaDAO()
                            .registrar(
                                    auditoria
                            );
                }
            }


        } catch (SQLException e) {

            mensagem =
                    "Falha ao deletar o produto: "
                    + e.getMessage();

            e.printStackTrace();

        } finally {

            bd.close();
        }


        return mensagem;
    }


    // ============================================================
    // LISTAR PRODUTOS
    // ============================================================

    public List<Produto> getAll() {

        List<Produto> lista =
                new ArrayList<>();


        BD bd =
                new BD();


        bd.getConnection();


        String sql =
                "SELECT * FROM produto";


        try {

            bd.st =
                    bd.con.prepareStatement(
                            sql
                    );


            bd.rs =
                    bd.st.executeQuery();


            while (bd.rs.next()) {

                int id =
                        bd.rs.getInt(
                                "id_produto"
                        );


                int codigo =
                        bd.rs.getInt(
                                "codigo"
                        );


                String nome =
                        bd.rs.getString(
                                "nome"
                        );


                int qtdeEstoque =
                        bd.rs.getInt(
                                "quantidade_estoque"
                        );


                double precoCusto =
                        bd.rs.getDouble(
                                "preco_custo"
                        );


                double precoVenda =
                        bd.rs.getDouble(
                                "preco_venda"
                        );


                String lote =
                        bd.rs.getString(
                                "lote"
                        );


                String descricao =
                        bd.rs.getString(
                                "descricao"
                        );


                Produto produto =
                        new Produto(
                                id,
                                codigo,
                                nome,
                                qtdeEstoque,
                                precoCusto,
                                precoVenda,
                                lote,
                                descricao
                        );


                lista.add(
                        produto
                );
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro em ProdutoDAO.getAll(): "
                    + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            bd.close();
        }


        System.out.println(
                "ProdutoDAO.getAll() retornou "
                + lista.size()
                + " registros."
        );


        return lista;
    }


    // ============================================================
    // EXPORTAR PARA CSV
    // ============================================================

    public String toCSV() {

        String mensagem =
                "Arquivo CSV gerado com sucesso!";


        BD bd =
                new BD();


        bd.getConnection();


        String sql =
                "SELECT * FROM produto";


        try {

            PrintWriter pw =
                    new PrintWriter(
                            "produtos.csv"
                    );


            bd.st =
                    bd.con.prepareStatement(
                            sql
                    );


            bd.rs =
                    bd.st.executeQuery();


            while (bd.rs.next()) {

                pw.print(
                        bd.rs.getInt(1)
                        + ";"
                );

                pw.print(
                        bd.rs.getString(2)
                        + ";"
                );

                pw.print(
                        bd.rs.getInt(3)
                        + ";"
                );

                pw.print(
                        bd.rs.getString(4)
                        + "\n"
                );

                pw.print(
                        bd.rs.getString(5)
                        + "\n"
                );

                pw.print(
                        bd.rs.getDouble(6)
                        + "\n"
                );

                pw.print(
                        bd.rs.getDouble(7)
                        + "\n"
                );

                pw.print(
                        bd.rs.getInt(8)
                        + ";"
                );

                pw.print(
                        bd.rs.getInt(9)
                        + ";"
                );

                pw.print(
                        bd.rs.getBoolean(10)
                        + ";"
                );
            }


            pw.close();


        } catch (Exception e) {

            mensagem =
                    "Falha ao gerar arquivo CSV.";

            e.printStackTrace();

        } finally {

            bd.close();
        }


        return mensagem;
    }
}