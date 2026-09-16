# Salamandra Management System

Sistema desktop de gestão desenvolvido em **Java, JavaFX e PostgreSQL** para a **Salamandra**, empresa do segmento de incensaria.

O projeto foi desenvolvido durante o curso de **Análise e Desenvolvimento de Sistemas**, a partir da proposta de criar uma solução de software para um negócio real.

O Salamandra centraliza diferentes processos da empresa em uma única aplicação, permitindo o gerenciamento de clientes, produtos, estoque, fornecedores, vendas e usuários, além de oferecer indicadores, relatórios, controle de acesso e auditoria das operações realizadas.

---

## 📌 Sobre o projeto

O desenvolvimento do Salamandra buscou transformar necessidades reais da empresa em funcionalidades de software.

Durante o projeto foram trabalhadas diferentes etapas do desenvolvimento de um sistema, incluindo:

- Levantamento e análise das necessidades do negócio
- Modelagem e estruturação do banco de dados
- Desenvolvimento das interfaces
- Implementação das regras da aplicação
- Persistência de dados
- Controle de usuários e permissões
- Desenvolvimento de relatórios e indicadores
- Configuração do sistema para funcionamento em rede
- Testes e correções
- Versionamento e desenvolvimento colaborativo

---

## 🚀 Funcionalidades

O sistema possui funcionalidades para diferentes áreas da operação:

- 👥 Cadastro, consulta, edição e exclusão de clientes
- 📦 Cadastro e gerenciamento de produtos
- 📊 Controle de estoque
- 🏭 Gerenciamento de fornecedores
- 🛒 Registro e gerenciamento de vendas
- 👤 Associação das vendas ao usuário responsável
- 📈 Dashboard com indicadores operacionais
- 📑 Relatórios gerenciais
- 🔐 Autenticação de usuários
- 🛡️ Controle de permissões por perfil
- 📝 Registro de auditoria das principais operações
- 🌐 Utilização do banco de dados em rede
- 🚪 Controle de sessão e logout

---

## 👤 Perfis de acesso

O Salamandra possui diferentes níveis de acesso, permitindo disponibilizar funcionalidades de acordo com a função de cada usuário.

### 🛡️ Administrador

Possui acesso às funcionalidades administrativas e operacionais do sistema.

### 🛒 Vendedor

Possui acesso às funcionalidades relacionadas ao processo de vendas e atendimento.

### 📦 Estoquista

Possui acesso às funcionalidades relacionadas aos produtos e ao controle de estoque.

---

## 📊 Dashboard

O dashboard apresenta uma visão rápida das principais informações da operação.

Entre os indicadores disponíveis estão:

- Total de clientes
- Total de produtos
- Produtos com estoque baixo
- Quantidade de vendas
- Faturamento
- Vendas do usuário logado
- Últimas vendas
- Responsável pelas vendas recentes

O objetivo é permitir que informações importantes sejam visualizadas rapidamente ao acessar o sistema.

---

## 📑 Relatórios

O sistema possui um módulo dedicado à visualização de informações gerenciais.

Os relatórios utilizam os dados armazenados no PostgreSQL para apresentar informações relacionadas principalmente a:

- Vendas
- Produtos
- Clientes

Essas informações permitem acompanhar melhor as operações realizadas e auxiliam na análise dos dados cadastrados no sistema.

---

## 📝 Auditoria

O Salamandra possui um mecanismo de auditoria responsável por registrar operações importantes realizadas pelos usuários.

Entre as ações registradas estão:

- Cadastro de clientes
- Alteração de clientes
- Exclusão de clientes
- Cadastro de produtos
- Alteração de produtos
- Exclusão de produtos
- Cadastro de vendas
- Conclusão de vendas
- Cancelamento de vendas
- Exclusão de vendas

Cada registro de auditoria pode armazenar informações como:

- Usuário responsável
- Ação realizada
- Descrição
- Data e hora

Isso permite maior rastreabilidade das operações realizadas no sistema.

---

## 🛠️ Tecnologias utilizadas

### Aplicação

- **Java**
- **JavaFX**
- **JDBC**
- **JFoenix**

### Banco de dados

- **PostgreSQL**
- **SQL**

### Interface

- **FXML**
- **CSS**

### Desenvolvimento e versionamento

- **Eclipse**
- **Git**
- **GitHub**
- **JDK 21**

---

## 🏗️ Estrutura do projeto

O projeto utiliza separação de responsabilidades entre os diferentes componentes da aplicação.

```text
Salamandra/
│
├── lib/                    # Bibliotecas utilizadas pelo sistema
│
├── src/
│   ├── application/        # Inicialização, sessão e permissões
│   ├── controller/         # Controllers das interfaces JavaFX
│   ├── database/           # Conexão com o banco e DAOs
│   ├── img/                # Recursos visuais
│   ├── model/              # Entidades e modelos
│   ├── util/               # Classes utilitárias
│   └── view/               # Telas FXML e estilos CSS
│
├── schema.sql              # Estrutura do banco de dados
├── dados_exemplo.sql       # Dados utilizados para testes
├── dados_vendas.sql        # Dados relacionados às vendas
└── README.md
```

A comunicação entre a aplicação e o **PostgreSQL** é realizada através de **JDBC**.

As operações de persistência são organizadas utilizando o padrão **DAO (Data Access Object)**, separando o acesso aos dados das demais responsabilidades da aplicação.

---

## 🗄️ Banco de dados

O Salamandra utiliza **PostgreSQL** para persistência dos dados.

Entre as principais entidades do sistema estão:

```text
Usuario
Cliente
Fornecedor
Produto
Venda
Item_Venda
Auditoria
```

Os relacionamentos permitem, entre outras operações, identificar:

- O cliente relacionado a uma venda
- O usuário responsável pela venda
- Os produtos pertencentes a cada venda
- O usuário responsável por uma ação registrada na auditoria

### Estrutura do banco

O arquivo:

```text
schema.sql
```

contém a estrutura necessária para criação do banco de dados, incluindo:

- Tabelas
- Chaves primárias
- Chaves estrangeiras
- Constraints
- Índices
- Relacionamentos
- Usuário administrador inicial

O script foi validado através da criação de um banco PostgreSQL separado a partir do zero.

---

## 🌐 Funcionamento em rede

O sistema pode ser utilizado por diferentes computadores conectados à mesma rede, compartilhando uma instância central do PostgreSQL.

Para possibilitar esse funcionamento foram realizadas configurações envolvendo:

- PostgreSQL Server
- `postgresql.conf`
- `pg_hba.conf`
- Firewall
- Porta `5432`
- Configuração JDBC
- Testes de comunicação entre diferentes computadores

Dessa forma, diferentes máquinas podem executar a aplicação utilizando o mesmo banco de dados.

---

## ⚙️ Como executar o projeto

### Pré-requisitos

Antes de executar o Salamandra, é necessário possuir:

- **Java JDK 21**
- **PostgreSQL**
- **Git**
- **Eclipse** ou outra IDE compatível com JavaFX

---

### 1. Clone o repositório

```bash
git clone https://github.com/LeonardoLFB/Salamandra
```

Entre na pasta criada:

```bash
cd Salamandra
```

---

### 2. Crie o banco de dados

No PostgreSQL, crie um banco para o sistema.

Exemplo:

```text
salamandra_incensaria
```

Depois execute o arquivo:

```text
schema.sql
```

Esse script criará a estrutura necessária para utilização da aplicação.

---

### 3. Configure a conexão com o PostgreSQL

A aplicação utiliza o arquivo:

```text
src/config.properties
```

para armazenar as informações necessárias para conexão com o banco de dados.

Esse arquivo não é versionado no repositório para evitar o compartilhamento de credenciais e configurações específicas de cada ambiente.

Configure-o de acordo com o PostgreSQL instalado na máquina que será utilizada.

---

### 4. Verifique as dependências

As bibliotecas necessárias para o funcionamento do projeto estão organizadas em:

```text
lib/
```

Certifique-se de que elas estejam adicionadas corretamente ao **Build Path** do projeto.

---

### 5. Execute a aplicação

Após configurar o banco e as dependências, execute a classe principal localizada no pacote:

```text
application
```

A aplicação deverá iniciar apresentando a tela de login.

---

## 🔐 Primeiro acesso

Durante a criação inicial do banco, o `schema.sql` disponibiliza um usuário administrador para permitir o primeiro acesso ao sistema.

As credenciais iniciais podem ser consultadas no próprio arquivo:

```text
schema.sql
```

Após a configuração inicial, recomenda-se alterar as credenciais utilizadas.

---

## 📸 Interface do sistema

### 🔐 Login

> Adicionar screenshot da tela de login.

### 📊 Dashboard

> Adicionar screenshot do dashboard.

### 👥 Clientes

> Adicionar screenshot da tela de clientes.

### 📦 Produtos e estoque

> Adicionar screenshot da tela de produtos e estoque.

### 🛒 Vendas

> Adicionar screenshot da tela de vendas.

### 📑 Relatórios

> Adicionar screenshot da tela de relatórios.

---

## 🎯 Objetivo acadêmico

O projeto busca aplicar conceitos estudados durante a graduação em um cenário próximo ao desenvolvimento de um sistema real.

Durante sua evolução foram trabalhados conceitos de:

`Java` • `Orientação a Objetos` • `JavaFX` • `FXML` • `CSS` • `SQL` • `PostgreSQL` • `JDBC` • `DAO` • `Git` • `GitHub` • `Desenvolvimento em equipe`

Além dos aspectos técnicos, o projeto também envolve análise de necessidades, resolução de problemas, testes, organização do código e evolução contínua da aplicação.

---

## 👨‍💻 Equipe

Projeto desenvolvido em equipe por estudantes do curso de **Análise e Desenvolvimento de Sistemas**.

### Integrantes

- João Vitor Bury 
- Leonardo Lacerda 
- Thiago Marcelo 
- Thiago Sanchez Nascimento

---

## 📚 Contexto

O Salamandra foi desenvolvido para fins acadêmicos utilizando como base necessidades apresentadas por uma empresa real do segmento de incensaria.

A proposta permitiu aproximar o desenvolvimento acadêmico de um cenário real, envolvendo banco de dados, desenvolvimento desktop, regras de negócio, experiência do usuário e trabalho colaborativo.
