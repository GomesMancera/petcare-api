# Arquitetura e evidências — PetCareIA+

Este documento reúne as evidências técnicas pedidas na Sprint 4 (documentação, modelo de dados, scripts SQL e fluxos).

## 1. Visão geral

```mermaid
flowchart LR
    subgraph Clientes
        M["App Mobile\nReact Native + Expo"]
        W["Navegador\nSite Thymeleaf"]
    end
    subgraph "petcare-api (Spring Boot 3.3 / Java 21)"
        S1["Cadeia /api/**\nJWT stateless"]
        S2["Cadeia web\nSessão + CSRF"]
        SV["Services\nregras de negócio"]
        R["Repositories\nSpring Data JPA"]
    end
    DB[("Oracle\nFlyway V1, V2")]

    M -- "HTTPS + Bearer token" --> S1
    W -- "HTTPS + cookie de sessão" --> S2
    S1 --> SV
    S2 --> SV
    SV --> R --> DB
```

Os dois clientes usam **os mesmos services**: a regra de agendamento, de vacina e de posse dos dados existe uma vez só.

## 2. Modelo de dados (Oracle)

```mermaid
erDiagram
    PC_USUARIO ||--o{ PC_PET : "é tutor de"
    PC_PET ||--o{ PC_CONSULTA : tem
    PC_PET ||--o{ PC_VACINA : tem

    PC_USUARIO {
        number ID PK
        varchar NOME
        varchar EMAIL UK
        varchar SENHA "BCrypt"
        varchar PERFIL "TUTOR | CLINICA"
        timestamp CRIADO_EM
    }
    PC_PET {
        number ID PK
        varchar NOME
        varchar RACA
        date DATA_NASCIMENTO
        number PESO
        varchar FOTO_URL
        number TUTOR_ID FK
    }
    PC_CONSULTA {
        number ID PK
        number PET_ID FK
        timestamp DATA_HORA
        varchar MOTIVO
        varchar OBSERVACOES
        varchar STATUS "AGENDADA | REALIZADA | CANCELADA"
    }
    PC_VACINA {
        number ID PK
        number PET_ID FK
        varchar NOME
        date DATA_APLICACAO
        date DATA_PROXIMA_DOSE
    }
```

Scripts SQL versionados: `src/main/resources/db/migration/`
- `V1__criar_tabelas_core.sql` — tabelas, chaves e checks.
- `V2__horario_unico_consulta.sql` — índice único **parcial** (só consultas não canceladas ocupam o horário).

## 3. Fluxo de agendamento (e como evitamos horário duplicado)

```mermaid
sequenceDiagram
    actor U as Tutor (app ou site)
    participant API as petcare-api
    participant DB as Oracle

    U->>API: GET /api/consultas/horarios-disponiveis?data=AAAA-MM-DD
    API->>DB: consultas ativas do dia
    API-->>U: só os horários livres (08:00–18:00, de 30 em 30 min)
    U->>API: POST /api/consultas/agendar (pet, data/hora, motivo)
    API->>API: valida posse do pet, jornada, grade de 30 min e futuro
    API->>DB: existe consulta ativa nesse horário?
    alt horário livre
        API->>DB: INSERT (flush)
        DB-->>API: ok
        API-->>U: 201 Created
    else ocupado (ou outra requisição ganhou a corrida)
        DB-->>API: violação do índice único UX_PC_CONSULTA_HORARIO
        API-->>U: 409 Conflict — "Este horário já está ocupado"
    end
```

## 4. Segurança

| Camada | Regra |
|--------|-------|
| `/api/**` | JWT (access 1 h + refresh 7 dias); sem token → 401; sem permissão → 403 |
| Web | Sessão com CSRF; logout por POST |
| Perfis | `TUTOR` só enxerga os próprios pets; `CLINICA` enxerga tudo, marca consulta como realizada e cadastra novas clínicas |
| Cadastro público | Sempre `TUTOR` (o perfil nunca vem do cliente) |
| Segredos | Somente variáveis de ambiente |

## 5. Decisões de design (para a narrativa)

- **Um backend, dois clientes:** reaproveita regra de negócio e reduz inconsistência entre site e app.
- **Regra crítica no banco:** o índice único parcial protege a agenda mesmo com acessos simultâneos.
- **Oferecer só horários válidos:** o app e o site consultam os horários livres, então o usuário raramente chega ao erro 409.
- **Lembretes no aparelho:** notificações locais derivadas dos dados da API, sem depender de servidor de push.
- **Posse centralizada (`AutorizacaoService`):** uma regra, testada, usada por pets, consultas e vacinas.
