# 🐾 PetCareIA+ — API e Web (Java Advanced | Sprint 4)

Plataforma para clínicas veterinárias e tutores: cadastro de pets, **agendamento de consultas** e **carteira de vacinação**,
com site web (Thymeleaf), API REST (JWT) consumida pelo app mobile e banco Oracle versionado com Flyway.

> **Aplicação online:** https://petcare-api-aod8.onrender.com
> **Vídeo de apresentação (até 15 min):** `<COLE_AQUI_O_LINK_DO_VÍDEO>`
> **App mobile:** https://github.com/RaphaelGomesMancera/petcare-mobileChallenge

## 👥 Integrantes

| Integrante                      | RM        |
|---------------------------------|----------:|
| Bruno Vinicius Barbosa          | RM 566366 |
| Guilherme de Andrade Martini    | RM 566087 |
| Nathan Gonçalves Pereira Mendes | RM 564666 |
| Raphael Gomes Mancera           | RM 562279 |

## 🎯 Problema e solução

Tutores perdem datas de vacina e consultas; clínicas sofrem com agenda conflitante. O PetCareIA+ centraliza a vida
do pet: o tutor agenda só em horários realmente livres e é lembrado das vacinas; a clínica enxerga e conduz tudo
em um só lugar. O mesmo backend atende o **site** (sessão) e o **app** (JWT).

## 🧩 Funcionalidades

- **Pets:** cadastro, edição e exclusão (a exclusão remove também o histórico do pet).
- **Agendamento de consulta (fluxo completo):** atendimento de 30 em 30 min, das 08:00 às 18:00, **um por horário**.
  A tela só oferece horários livres; o servidor valida de novo, e um **índice único no Oracle (migration V2)** impede
  duas pessoas de ocuparem o mesmo horário ao mesmo tempo (resposta 409).
- **Aplicação de vacina (fluxo completo):** a próxima dose é calculada pelo tipo da vacina (V10/V8: 21 dias; demais: 365).
- **Dashboard:** pets, próximas consultas, vacinas pendentes (reforço em até 30 dias ou atrasado) e última consulta.
- **Perfis:** `TUTOR` (só enxerga os próprios pets) e `CLINICA` (enxerga tudo e marca consultas como realizadas).

## 🔐 Segurança

| Item | Como funciona |
|------|---------------|
| Duas cadeias de segurança | `/api/**` → JWT stateless; demais rotas → sessão + CSRF |
| Cadastro público | Sempre cria `TUTOR` (o perfil **não** é aceito no corpo da requisição) |
| Conta `CLINICA` | Criada na inicialização a partir de `ADMIN_EMAIL`/`ADMIN_SENHA`; novas clínicas via `POST /api/admin/clinicas` (só `CLINICA`) |
| Rotas por perfil | `/api/admin/**` e `PATCH /api/consultas/*/realizar` só para `CLINICA` (também na web) |
| Posse dos dados | `AutorizacaoService` único: tutor só acessa recursos dos próprios pets (403 caso contrário) |
| Tokens | Access token (1 h) + refresh token (7 dias) em `/api/auth/refresh`; token inválido/expirado → 401 |
| Segredos | **Nenhum segredo no repositório**: tudo por variáveis de ambiente (`.env.example`) |

## 🛠️ Tecnologias

Java 21 · Spring Boot 3.3 · Spring Web · Spring Data JPA · Spring Security · JWT (jjwt) · Thymeleaf · Flyway · Oracle · Maven · Docker

## ⚙️ Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|----------|:-----------:|-----------|
| `DB_URL` | ✅ | Ex.: `jdbc:oracle:thin:@HOST:1521:SID` |
| `DB_USER` / `DB_PASSWORD` | ✅ | Credenciais do Oracle |
| `JWT_SECRET` | ✅ | Chave aleatória com 32+ caracteres |
| `ADMIN_EMAIL` / `ADMIN_SENHA` | recomendada | Cria a conta da clínica no primeiro start |
| `ADMIN_NOME` | não | Nome da conta da clínica |
| `PORT` | não | Porta HTTP (padrão 8080) |
| `JWT_EXPIRATION_MS` / `JWT_REFRESH_EXPIRATION_MS` | não | Duração dos tokens |

## ▶️ Rodando localmente

Requisitos: **JDK 21**, **Maven**, acesso a um Oracle.

```bash
git clone https://github.com/RaphaelGomesMancera/petcare-api.git
cd petcare-api
cp .env.example .env      # preencha com os seus valores (o .env NÃO vai para o Git)
mvn spring-boot:run
```

Acesse `http://localhost:8080` → crie uma conta em `/registrar` ou entre com a conta da clínica.
Testes: `mvn test`.

## 🐳 Docker e deploy

```bash
docker build -t petcare-api .
docker run -p 8080:8080 --env-file .env petcare-api
```

Em qualquer plataforma que rode Docker (Render, Azure Web App for Containers, Railway...):
aponte para este repositório, cadastre as variáveis acima no painel e use `/health` como verificação de saúde.
O banco Oracle precisa estar acessível pela internet a partir da plataforma escolhida.

## 🔌 Endpoints principais

| Método | Rota | Perfil |
|--------|------|--------|
| POST | `/api/auth/registrar` · `/api/auth/login` · `/api/auth/refresh` | público |
| GET/POST/PUT/DELETE | `/api/pets` · `/api/pets/{id}` | TUTOR, CLINICA |
| GET | `/api/consultas` · `/api/consultas/horarios-disponiveis?data=AAAA-MM-DD` | TUTOR, CLINICA |
| POST | `/api/consultas/agendar` | TUTOR, CLINICA |
| PUT/DELETE | `/api/consultas/{id}` (DELETE cancela) | TUTOR, CLINICA |
| PATCH | `/api/consultas/{id}/realizar` | CLINICA |
| GET/POST | `/api/vacinas` · `/api/vacinas/pet/{petId}` · `/api/vacinas/aplicar` | TUTOR, CLINICA |
| GET | `/api/dashboard` | TUTOR, CLINICA |
| POST | `/api/admin/clinicas` | CLINICA |

Erros seguem o padrão `{timestamp, status, erro, mensagem}`: 400 (validação/regra), 401, 403, 404, 409 (conflito de horário/e-mail).

## 🗃️ Banco de dados (Flyway)

```
db/migration/
├── V1__criar_tabelas_core.sql          # PC_USUARIO, PC_PET, PC_CONSULTA, PC_VACINA
└── V2__horario_unico_consulta.sql      # índice único parcial: 1 consulta ativa por horário
```

## 📁 Estrutura

```
config/        SecurityConfig, AdminInitializer
controller/    REST (Auth, Admin, Pets, Consultas, Vacinas, Dashboard, Health)
controller/web Páginas Thymeleaf (uma classe por área) + WebControllerAdvice
service/       Regras de negócio (Auth, Usuario, Pet, Consulta, Vacina, Dashboard, Autorizacao)
security/      JwtService, JwtAuthFilter, CustomUserDetailsService
dto · entity · enums · exception · repository
resources/templates  fragmentos.html (menu/cabeçalho/mensagens reutilizados) + páginas
```

## 📚 Documentação e evidências

- [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md) — arquitetura, modelo de dados (ER), fluxo de agendamento e segurança (diagramas Mermaid)
- [`docs/requisicoes.http`](docs/requisicoes.http) — requisições de exemplo (inclui 409 e 403)
- [`docs/ROTEIRO_VIDEO.md`](docs/ROTEIRO_VIDEO.md) — roteiro de 15 min dividido entre os 4 integrantes

## 🔗 Integração multidisciplinar

| Disciplina | Como aparece na solução |
|------------|-------------------------|
| Mobile Application Development | App React Native/Expo consome esta API (login, refresh de token, pets, consultas, vacinas, notificações) |
| Banco de dados (Oracle) | Modelo `PC_*`, migrations V1/V2 e índice único parcial |
| Java Advanced | API REST, Spring Security, Flyway, Thymeleaf, deploy em container |

## ✅ Checklist de entrega

- [x] Link da aplicação online no topo deste README
- [ ] Link do vídeo (máx. 15 min, **todos os integrantes** falando)
- [x] Variáveis de ambiente configuradas na plataforma (nenhuma senha no repositório)
- [ ] Senha do Oracle trocada (a anterior ficou no histórico do Git)
- [x] Tabela de integração multidisciplinar preenchida
- [ ] Vídeo mostra a **mesma versão** que está no ar
