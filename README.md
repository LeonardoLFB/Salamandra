# Salamandra — Gestão de Incensaria

Projeto Integrador desenvolvido em Java para auxiliar a gestão de uma incensaria. A aplicação desktop reúne cadastros de clientes, fornecedores e produtos, controle de estoque e registro de vendas em um banco PostgreSQL.

## Funcionalidades

- Login com usuário e senha.
- Painel com indicadores de clientes, produtos, estoque baixo, vendas pendentes e faturamento de vendas concluídas.
- Cadastro, edição, exclusão e pesquisa de clientes, com validação de CPF e campos de endereço.
- Cadastro de produtos com código, lote, descrição, preços de custo e venda e quantidade em estoque.
- Consulta de estoque com busca e filtros de disponibilidade.
- Cadastro, edição, exclusão e pesquisa de fornecedores ativos ou inativos.
- Cadastro e gerenciamento de usuários com os tipos Administrador, Vendedor e Estoquista.
- Registro de vendas com vários itens, cálculo do total, consulta de detalhes, conclusão e cancelamento.

Ao registrar uma venda, o sistema atribui o status **Pendente** e desconta os itens do estoque. O cancelamento devolve as quantidades ao estoque. A forma de pagamento é armazenada no campo de observação da venda.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Linguagem e ambiente configurados no projeto |
| JavaFX e FXML | Interface desktop e definição das telas |
| CSS | Estilização da interface |
| JFoenix 9.0.10 | Componentes visuais |
| PostgreSQL | Persistência dos dados |
| JDBC PostgreSQL 42.7.8 | Comunicação entre Java e banco |
| Eclipse | Estrutura de projeto incluída no repositório |

## Pré-requisitos

- Git para clonar o repositório.
- JDK 21 instalado.
- Eclipse IDE com suporte a Java.
- JavaFX SDK 21 compatível com seu sistema operacional e arquitetura.
- PostgreSQL instalado e em execução, com acesso ao utilitário `psql` (e `createdb` para os comandos abaixo).

As bibliotecas JDBC e JFoenix já estão em `src/database`. O JavaFX SDK precisa ser configurado na máquina. O projeto não utiliza Maven ou Gradle.

## Instalação

### 1. Clonar o repositório

```sh
git clone https://github.com/LeonardoLFB/Salamandra.git
cd Salamandra
```

### 2. Criar e preparar o banco

No terminal, a partir da pasta do projeto:

```sh
createdb -h localhost -p 5432 -U postgres salamandra_incensaria
psql -h localhost -p 5432 -U postgres -d salamandra_incensaria -v ON_ERROR_STOP=1 -f schema.sql
```

Informe a senha do seu usuário PostgreSQL quando solicitado. Caso os comandos não sejam reconhecidos, adicione a pasta `bin` da instalação do PostgreSQL ao `PATH` ou execute-os pelo caminho completo.

O script `schema.sql` cria as tabelas `usuario`, `cliente`, `produto`, `fornecedor`, `venda` e `item_venda`, seus relacionamentos e um usuário inicial de acesso.

Para carregar dados de demonstração, execute opcionalmente, nesta ordem:

```sh
psql -h localhost -p 5432 -U postgres -d salamandra_incensaria -v ON_ERROR_STOP=1 -f dados_exemplo.sql
psql -h localhost -p 5432 -U postgres -d salamandra_incensaria -v ON_ERROR_STOP=1 -f dados_vendas.sql
```

Utilize os dados de demonstração em um banco de desenvolvimento. O primeiro script inclui clientes, fornecedores e produtos; o segundo depende desses registros para criar vendas de exemplo.

### 3. Configurar a conexão

Abra [`src/database/BD.java`](src/database/BD.java) e ajuste as constantes conforme sua instalação:

| Configuração | Valor atual |
| --- | --- |
| Banco (`DATABASE`) | `salamandra_incensaria` |
| Endereço (`URL`) | `jdbc:postgresql://localhost:5432/salamandra_incensaria` |
| Usuário (`LOGIN`) | `postgres` |
| Senha (`SENHA`) | Configure a senha do seu PostgreSQL |

Use em `SENHA` a senha do seu PostgreSQL. Se o servidor estiver em outra máquina ou porta, ajuste também `URL`. Atualmente a aplicação lê essas configurações diretamente dessa classe.

### 4. Importar no Eclipse

1. Acesse **File > Import > General > Existing Projects into Workspace**.
2. Selecione a pasta clonada e importe o projeto **Salamandra**.
3. Em **Project > Properties > Java Build Path**, configure o JDK 21.
4. Na aba **Libraries**, resolva as bibliotecas de usuário `javafx21` e `javaFx21` declaradas no `.classpath`, apontando ambas para os JARs da pasta `lib` do JavaFX SDK 21. Os nomes diferenciam maiúsculas e minúsculas.
5. Confira se as dependências abaixo estão disponíveis no caminho de classes:
   - `src/database/postgresql-42.7.8.jar`
   - `src/database/jfoenix-9.0.10 (1).jar`

### 5. Executar a aplicação

Em **Run > Run Configurations > Java Application**, crie uma configuração com:

- **Project:** `Salamandra`
- **Main class:** `application.Main`
- **JRE:** JDK 21

Na aba **Arguments**, em **VM arguments**, informe o caminho da pasta `lib` do seu JavaFX SDK. Exemplo para Windows:

```text
--module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml
```

Substitua o caminho pelo local real do SDK. Mantenha o PostgreSQL em execução e inicie a configuração. A primeira tela é a de login.

## Primeiro acesso

| Campo | Valor inicial |
| --- | --- |
| Login | `admin` |
| Senha | Consulte o usuário inicial em `schema.sql` |

O usuário inicial é criado pelo `schema.sql`; a tela de login também tenta criá-lo caso não exista. Essas credenciais são destinadas ao ambiente de desenvolvimento. Atualmente as senhas são armazenadas em texto simples e os tipos de usuário cadastrados não implementam restrições de acesso às telas.

## Estrutura do projeto

```text
Salamandra/
├── src/
│   ├── application/    # Inicialização e navegação entre telas
│   ├── controller/     # Eventos das telas e validações
│   ├── database/       # Conexão, DAOs e bibliotecas JDBC/JFoenix
│   ├── model/          # Classes de cliente, produto, venda e demais entidades
│   ├── view/           # Telas FXML e estilos CSS
│   └── img/            # Logos e ícones
├── schema.sql          # Estrutura do banco e usuário inicial
├── dados_exemplo.sql   # Cadastros para demonstração
├── dados_vendas.sql     # Vendas para demonstração
├── .classpath          # Dependências do Eclipse
└── .project            # Configuração do projeto Eclipse
```

As telas acionam os controladores, que utilizam os modelos e os DAOs para consultar ou gravar informações no banco.

## Problemas comuns

| Sintoma | O que verificar |
| --- | --- |
| Falha de conexão ou tela não carrega dados | Serviço PostgreSQL, porta, nome do banco e credenciais em `BD.java` |
| Erro indicando tabela inexistente | Execute `schema.sql` no mesmo banco configurado na aplicação |
| `JavaFX runtime components are missing` | JavaFX SDK e argumentos `--module-path` e `--add-modules` |
| Biblioteca `javafx21` ou `javaFx21` não encontrada | Configure as duas referências em **Java Build Path** |
| `ClassNotFoundException` para PostgreSQL ou JFoenix | Confira os JARs em `src/database` e no caminho de classes |
| Aviso sobre versão do FXML | As telas declaram JavaFX 24.0.1, enquanto o projeto referencia JavaFX 21; há uma diferença de versões a considerar no diagnóstico |
| Login inicial não funciona | Confira a conexão e a existência do usuário `admin` na tabela `usuario` |

## Verificação manual

Após configurar o ambiente:

1. Entre com o usuário inicial.
2. Confira os indicadores do menu e abra as telas de cadastro.
3. Cadastre um cliente e um produto com estoque positivo, ou carregue os dados de exemplo.
4. Registre uma venda e confira a redução da quantidade em estoque.
5. Cancele a venda pendente e confira a devolução da quantidade.

O repositório ainda não possui uma suíte de testes automatizados. Este roteiro permite conferir o fluxo principal na instalação local.

## Contribuição

Crie uma branch para sua alteração, mantenha o escopo do commit bem definido e descreva no pull request o que foi alterado e como foi verificado.

```sh
git switch -c docs/minha-documentacao
git add README.md
git commit -m "docs: atualiza documentacao do projeto"
git push -u origin docs/minha-documentacao
```
