# 🐾 PetCareIA+ — Java Advanced | Sprint 3

Aplicação web completa desenvolvida para a **Sprint 3 de Java Advanced da FIAP**, utilizando **Spring Boot**, com frontend integrado, controle de versão do banco de dados com **Flyway** e autenticação/autorização utilizando **Spring Security**.

O PetCareIA+ tem como objetivo auxiliar tutores e clínicas veterinárias no gerenciamento dos cuidados dos pets, permitindo cadastro e gerenciamento de animais, consultas, vacinas e acompanhamento das informações por meio de um dashboard.

---

## 👥 Integrantes

| Integrante                      |        RM |
| ------------------------------- | --------: |
| Bruno Vinicius Barbosa          | RM 566366 |
| Guilherme de Andrade Martini    | RM 566087 |
| Nathan Gonçalves Pereira Mendes | RM 564666 |
| Raphael Gomes Mancera           | RM 562279 |

---

# 🎯 Requisitos da Sprint 3

O projeto foi desenvolvido contemplando os requisitos definidos para a Sprint 3 de Java Advanced:

| Requisito                 | Pontuação | Implementação                                   |
| ------------------------- | --------: | ----------------------------------------------- |
| Frontend                  |    30 pts | ✅ Interface web com Thymeleaf                   |
| Flyway                    |    20 pts | ✅ Migration versionada do banco                 |
| Spring Security           |    30 pts | ✅ Autenticação, autorização e proteção de rotas |
| Funcionalidades completas |    20 pts | ✅ Agendamento e aplicação de vacinas            |

### Frontend

Interface web desenvolvida com **Thymeleaf**, contendo:

* Login
* Cadastro de tutor
* Dashboard
* Cadastro e gerenciamento de pets
* Visualização de consultas
* Visualização de vacinas
* Navegação entre as funcionalidades do sistema

### Flyway

O banco de dados é controlado através do **Flyway**, garantindo que a estrutura seja criada e versionada por meio de migrations.

Migration principal:

```text
src/main/resources/db/migration/
└── V1__criar_tabelas_core.sql
```

### Spring Security

A aplicação possui autenticação e autorização utilizando **Spring Security**.

Foram implementados dois perfis de usuário:

* `TUTOR`
* `CLINICA`

Cada perfil possui permissões diferentes e as rotas são protegidas de acordo com o usuário autenticado.

### Funcionalidades completas

Além das operações CRUD, foram implementados fluxos completos com regras de negócio e validações:

1. **Agendamento de consulta**
2. **Aplicação de vacina com cálculo da próxima dose**

---

# 🛠️ Tecnologias utilizadas

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* JWT
* Thymeleaf
* Flyway
* Oracle Database
* Maven
* HTML5
* CSS3

---

# 📋 Pré-requisitos

Antes de executar o projeto, certifique-se de possuir:

* **JDK 21**
* **Maven**
* Acesso a um banco **Oracle**
* Credenciais válidas do banco Oracle utilizado pela aplicação
* Git

Para verificar as instalações:

```bash
java -version
mvn -version
git --version
```

A aplicação foi desenvolvida utilizando **Java 21**.

---

# 📥 Instalação

Clone o repositório:

```bash
git clone <URL_DO_REPOSITORIO>
```

Entre na pasta do projeto:

```bash
cd <NOME_DO_PROJETO>
```

---

# 🗄️ Configuração do Banco de Dados

A aplicação utiliza **Oracle Database**.

Antes de executar o projeto, configure o arquivo:

```text
src/main/resources/application.yml
```

Utilize suas próprias credenciais:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@//<HOST>:<PORTA>/<SERVICE_NAME>
    username: <USUARIO_ORACLE>
    password: <SENHA_ORACLE>
    driver-class-name: oracle.jdbc.OracleDriver
```

### ⚠️ Segurança

**Não coloque credenciais reais no GitHub.**

Substitua:

```text
<HOST>
<PORTA>
<SERVICE_NAME>
<USUARIO_ORACLE>
<SENHA_ORACLE>
```

pelos valores utilizados no seu ambiente local.

Da mesma forma, não publique senhas, tokens JWT ou outras informações sensíveis no repositório.

---

# 🔐 Configuração do JWT

Configure também uma chave secreta para o JWT:

```yaml
jwt:
  secret: <SUA_CHAVE_SECRETA>
```

Utilize uma chave própria no ambiente de execução.

**Não publique uma chave secreta real no repositório.**

---

# ▶️ Execução

Após configurar o banco de dados, execute:

```bash
mvn clean
```

Depois:

```bash
mvn compile
```

E inicialize a aplicação:

```bash
mvn spring-boot:run
```

Se a aplicação iniciar corretamente, ela estará disponível em:

```text
http://localhost:8080
```

---

# 🌐 Acesso à aplicação

## Cadastro

Acesse:

```text
http://localhost:8080/registrar
```

Utilize essa página para realizar o cadastro de um novo usuário.

## Login

```text
http://localhost:8080/login
```

Após autenticar, o usuário poderá acessar as funcionalidades permitidas para seu perfil.

## Dashboard

```text
http://localhost:8080/dashboard
```

O dashboard apresenta informações agregadas relacionadas ao tutor e aos seus pets.

---

# 🔑 Autenticação

A aplicação possui dois modelos de autenticação.

## Interface Web

O frontend utiliza autenticação baseada em **sessão**, permitindo que o usuário navegue pelas páginas protegidas após realizar o login.

## API REST

A API utiliza autenticação **JWT**.

O login é realizado através de:

```http
POST /api/auth/login
```

Após o login, o token deve ser enviado nas requisições protegidas através do header:

```http
Authorization: Bearer <TOKEN>
```

---

# 👤 Perfis e autorização

## TUTOR

O tutor possui acesso aos recursos relacionados aos seus próprios pets.

As regras de segurança garantem que um tutor não consiga acessar ou alterar recursos pertencentes a outro tutor.

Exemplos:

* Visualizar seus pets
* Cadastrar pets
* Alterar seus pets
* Excluir seus pets
* Consultar seus agendamentos
* Consultar suas vacinas

## CLINICA

O perfil de clínica possui permissões ampliadas para gerenciamento dos recursos da aplicação.

As permissões são controladas pelo Spring Security e pelas regras de negócio implementadas nos Services.

---

# 🔄 Fluxos completos da aplicação

A Sprint 3 exige pelo menos **dois fluxos completos que não sejam apenas CRUD**, incluindo validações básicas nos formulários e dados.

O PetCareIA+ implementa os seguintes fluxos:

## 1. 📅 Agendamento de consulta

O usuário pode realizar o agendamento de uma consulta para um pet.

### Fluxo

1. Usuário realiza login.
2. Seleciona o pet.
3. Informa os dados da consulta.
4. O sistema valida os dados enviados.
5. O sistema verifica conflitos de horário.
6. Caso não exista conflito, a consulta é registrada.
7. Caso exista conflito, a operação é recusada.

### Regra de negócio

Não é permitido realizar outro agendamento para o mesmo pet dentro de uma janela de **30 minutos**.

Em caso de conflito, a API retorna:

```http
409 Conflict
```

---

## 2. 💉 Aplicação de vacina

O sistema permite registrar a aplicação de uma vacina e calcula automaticamente a próxima dose.

### Fluxo

1. Usuário seleciona o pet.
2. Informa a vacina aplicada.
3. Registra a data da aplicação.
4. O sistema identifica o intervalo correspondente.
5. A próxima dose é calculada automaticamente.
6. A aplicação é registrada no banco.

### Regras implementadas

| Vacina      | Próxima dose |
| ----------- | -----------: |
| V10         |      21 dias |
| V8          |      21 dias |
| Antirrábica |     365 dias |
| Outras      |     365 dias |

---

# 📊 Dashboard

O sistema possui um dashboard com informações agregadas para facilitar o acompanhamento dos pets.

São apresentadas informações como:

* Quantidade de pets
* Próximas consultas
* Vacinas pendentes
* Última consulta
* Consultas previstas para os próximos 7 dias

Endpoint da API:

```http
GET /api/dashboard
```

Interface web:

```http
GET /dashboard
```

---

# 🔒 Estrutura de Segurança

A aplicação possui duas cadeias de segurança:

```text
/api/**
    ↓
Autenticação JWT
    ↓
API REST

/**
    ↓
Autenticação por sessão
    ↓
Frontend Thymeleaf
```

Componentes principais:

```text
security/
├── JwtService
├── JwtAuthFilter
└── CustomUserDetailsService
```

Configuração:

```text
config/
└── SecurityConfig
```

---

# 📁 Estrutura do projeto

```text
src/
├── main/
│   ├── java/
│   │   └── ...
│   │       ├── config/
│   │       │   └── SecurityConfig
│   │       │
│   │       ├── controller/
│   │       │   ├── REST
│   │       │   └── Web
│   │       │
│   │       ├── dto/
│   │       │   └── Request / Response
│   │       │
│   │       ├── entity/
│   │       │   ├── Usuario
│   │       │   ├── Pet
│   │       │   ├── Consulta
│   │       │   └── Vacina
│   │       │
│   │       ├── enums/
│   │       │   ├── Perfil
│   │       │   └── StatusConsulta
│   │       │
│   │       ├── exception/
│   │       │   ├── Exceptions customizadas
│   │       │   └── Global Handler
│   │       │
│   │       ├── repository/
│   │       │   └── Interfaces JPA
│   │       │
│   │       ├── security/
│   │       │   ├── JwtService
│   │       │   ├── JwtAuthFilter
│   │       │   └── CustomUserDetailsService
│   │       │
│   │       └── service/
│   │           └── Regras de negócio
│   │
│   └── resources/
│       ├── db/
│       │   └── migration/
│       │       └── V1__criar_tabelas_core.sql
│       │
│       ├── templates/
│       │   ├── login
│       │   ├── registrar
│       │   ├── dashboard
│       │   └── pets
│       │
│       ├── static/
│       │   └── css/
│       │       └── estilo.css
│       │
│       └── application.yml
│
└── pom.xml
```

---

# 🧪 Validações e regras de negócio

As regras de negócio estão concentradas na camada de Services.

Entre as principais validações estão:

* Autenticação do usuário
* Autorização por perfil
* Proteção de recursos por proprietário
* Validação de dados recebidos
* Verificação de conflito de agendamento
* Cálculo automático da próxima dose de vacina
* Tratamento global de exceções

Quando um tutor tenta acessar um recurso pertencente a outro tutor, a aplicação impede o acesso e retorna:

```http
403 Forbidden
```

---

# 🔌 Principais endpoints

## Autenticação

```http
POST /api/auth/login
POST /api/auth/registrar
```

## Pets

```http
GET    /api/pets
POST   /api/pets
PUT    /api/pets/{id}
DELETE /api/pets/{id}
```

## Consultas

```http
GET  /api/consultas
POST /api/consultas
POST /api/consultas/agendar
```

## Vacinas

```http
GET  /api/vacinas
POST /api/vacinas
POST /api/vacinas/aplicar
```

## Dashboard

```http
GET /api/dashboard
```

---

# 🗃️ Banco de dados

O banco utilizado pela aplicação é **Oracle Database**.

A estrutura inicial é criada e versionada pelo Flyway através da migration:

```text
V1__criar_tabelas_core.sql
```

As principais entidades utilizadas pela aplicação são:

```text
Usuario
Pet
Consulta
Vacina
```

---

# 🚀 Fluxo recomendado para testar a aplicação

Depois de iniciar o projeto:

### 1. Criar usuário

Acesse:

```text
http://localhost:8080/registrar
```

### 2. Fazer login

```text
http://localhost:8080/login
```

### 3. Cadastrar um pet

Acesse o gerenciamento de pets pelo dashboard.

### 4. Agendar uma consulta

Crie um agendamento e teste a validação de conflito.

### 5. Aplicar uma vacina

Registre uma vacina e verifique o cálculo automático da próxima dose.

### 6. Consultar o dashboard

Verifique as informações agregadas sobre pets, consultas e vacinas.

### 7. Testar autorização

Utilize usuários com perfis diferentes e valide o acesso às funcionalidades protegidas.

---

# 🤖 Uso de Inteligência Artificial

Durante o desenvolvimento, ferramentas de Inteligência Artificial puderam ser utilizadas como apoio ao processo de desenvolvimento.

A avaliação oral da Sprint 3 pode abordar:

* Trechos específicos do código
* Decisões de implementação
* Dificuldades encontradas
* Como ferramentas de IA foram utilizadas durante o desenvolvimento

Portanto, todos os integrantes devem estar preparados para explicar o código entregue e justificar as decisões tomadas no projeto.

---

# 📹 Demonstração

Vídeo demonstrando a aplicação em funcionamento (Sprint 3 — Java Advanced):

**https://youtu.be/oz2-PCyDAsY**

O vídeo apresenta as principais funcionalidades da aplicação web entregue neste repositório.

**Tempo máximo indicado no documento da Sprint: 10 minutos.**

---

# ⚠️ Observações importantes

* Utilize **JDK 21**.
* Configure corretamente o acesso ao Oracle antes de iniciar a aplicação.
* Não publique senhas, tokens JWT ou outras credenciais no GitHub.
* O projeto deve ser executado utilizando as configurações descritas neste README.
* As migrations do Flyway são executadas pela aplicação durante a inicialização.
* O projeto deve ser entregue com o código-fonte completo.

---

# ✅ Checklist antes da entrega

* [ ] Código-fonte completo publicado no GitHub
* [ ] Repositório público
* [ ] README atualizado
* [ ] Instruções de instalação presentes
* [ ] Instruções de execução presentes
* [ ] Instruções de acesso presentes
* [ ] JDK 21 informado
* [ ] Configuração do Oracle documentada
* [ ] Flyway funcionando
* [ ] Spring Security funcionando
* [ ] Dois perfis de usuário funcionando
* [ ] Rotas protegidas por perfil
* [ ] Fluxo de agendamento funcionando
* [ ] Fluxo de aplicação de vacina funcionando
* [ ] Validações funcionando
* [ ] Vídeo demonstrando a aplicação entregue
* [ ] Nenhuma senha ou token real publicado no repositório

---

## 📌 Sobre o projeto

O **PetCareIA+** integra frontend, API REST, persistência de dados, autenticação e regras de negócio em uma única aplicação Spring Boot.

A solução foi desenvolvida para atender aos requisitos técnicos da **Java Advanced — Sprint 3**, priorizando organização do código, segurança, controle de versão do banco e implementação de fluxos completos de negócio.
