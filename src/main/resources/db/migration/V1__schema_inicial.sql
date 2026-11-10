-- ============================================================
-- V1 — Hospitais e Unidades Solicitantes
-- ============================================================
CREATE TABLE hospital (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    cnpj        VARCHAR(18)  NOT NULL UNIQUE,
    logradouro  VARCHAR(200),
    cidade      VARCHAR(100),
    estado      CHAR(2),
    telefone    VARCHAR(20),
    email       VARCHAR(100),
    responsavel VARCHAR(150),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE unidade (
    id          BIGSERIAL PRIMARY KEY,
    hospital_id BIGINT       NOT NULL REFERENCES hospital(id),
    nome        VARCHAR(150) NOT NULL,
    ativa       BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ============================================================
-- V1 — Usuários e Perfis (Identidade)
-- ============================================================
CREATE TYPE perfil_usuario AS ENUM (
    'ADMINISTRADOR', 'ESTOQUE', 'ATENDIMENTO', 'EXPEDICAO'
);

CREATE TABLE usuario (
    id           BIGSERIAL PRIMARY KEY,
    login        VARCHAR(60)    NOT NULL UNIQUE,
    senha_hash   VARCHAR(255)   NOT NULL,
    nome         VARCHAR(150)   NOT NULL,
    email        VARCHAR(100)   NOT NULL UNIQUE,
    perfil       perfil_usuario NOT NULL,
    ativo        BOOLEAN        NOT NULL DEFAULT TRUE,
    criado_em    TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- V1 — Estoque de Hemocomponentes
-- ============================================================
CREATE TYPE tipo_abo AS ENUM ('A', 'B', 'AB', 'O');
CREATE TYPE tipo_rh  AS ENUM ('POSITIVO', 'NEGATIVO');
CREATE TYPE componente AS ENUM ('CH', 'PFC', 'CP', 'CRIO');
CREATE TYPE status_bolsa AS ENUM (
    'DISPONIVEL', 'RESERVADA', 'EM_SEPARACAO',
    'EM_EXPEDICAO', 'ENTREGUE', 'DESCARTADA', 'VENCIDA'
);

CREATE TABLE bolsa (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(30)   NOT NULL UNIQUE,
    abo             tipo_abo      NOT NULL,
    rh              tipo_rh       NOT NULL,
    componente      componente    NOT NULL,
    data_coleta     DATE          NOT NULL,
    data_validade   DATE          NOT NULL,
    localizacao     VARCHAR(100),
    status          status_bolsa  NOT NULL DEFAULT 'DISPONIVEL',
    cadastrado_em   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT validade_apos_coleta CHECK (data_validade > data_coleta)
);

CREATE INDEX idx_bolsa_status_tipo ON bolsa(status, abo, rh, componente, data_validade);

-- ============================================================
-- V1 — Requisições e Alocações
-- ============================================================
CREATE TYPE urgencia AS ENUM ('ROTINA', 'URGENCIA', 'EMERGENCIA');
CREATE TYPE status_requisicao AS ENUM (
    'PENDENTE', 'EM_ANALISE', 'PARCIALMENTE_ATENDIDA',
    'ATENDIDA', 'EM_EXPEDICAO', 'CONCLUIDA', 'CANCELADA'
);
CREATE TYPE status_alocacao AS ENUM ('RESERVADA', 'CONFIRMADA', 'CANCELADA');

CREATE TABLE requisicao (
    id                  BIGSERIAL          PRIMARY KEY,
    hospital_id         BIGINT             NOT NULL REFERENCES hospital(id),
    unidade_id          BIGINT             REFERENCES unidade(id),
    abo_solicitado      tipo_abo           NOT NULL,
    rh_solicitado       tipo_rh            NOT NULL,
    componente          componente         NOT NULL,
    quantidade          INTEGER            NOT NULL CHECK (quantidade > 0),
    urgencia            urgencia           NOT NULL DEFAULT 'ROTINA',
    status              status_requisicao  NOT NULL DEFAULT 'PENDENTE',
    observacao          TEXT,
    criado_em           TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    atualizado_em       TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

CREATE TABLE alocacao (
    id              BIGSERIAL         PRIMARY KEY,
    requisicao_id   BIGINT            NOT NULL REFERENCES requisicao(id),
    bolsa_id        BIGINT            NOT NULL REFERENCES bolsa(id),
    data_validade   DATE              NOT NULL,
    status          status_alocacao   NOT NULL DEFAULT 'RESERVADA',
    alocado_em      TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

    -- garante que a mesma bolsa não pode estar em duas alocações RESERVADA/CONFIRMADA
    CONSTRAINT bolsa_unica_ativa UNIQUE (bolsa_id)
        DEFERRABLE INITIALLY DEFERRED
);

-- ============================================================
-- V1 — Expedição
-- ============================================================
CREATE TYPE status_expedicao AS ENUM (
    'PREPARANDO', 'EM_TRANSITO', 'ENTREGUE', 'CANCELADA'
);

CREATE TABLE veiculo (
    id       BIGSERIAL PRIMARY KEY,
    placa    VARCHAR(10) NOT NULL UNIQUE,
    modelo   VARCHAR(80),
    ativo    BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE guia_expedicao (
    id              BIGSERIAL          PRIMARY KEY,
    numero          VARCHAR(30)        NOT NULL UNIQUE,
    requisicao_id   BIGINT             NOT NULL REFERENCES requisicao(id),
    hospital_id     BIGINT             NOT NULL REFERENCES hospital(id),
    veiculo_id      BIGINT             REFERENCES veiculo(id),
    responsavel     VARCHAR(150),
    status          status_expedicao   NOT NULL DEFAULT 'PREPARANDO',
    saida_em        TIMESTAMPTZ,
    entrega_em      TIMESTAMPTZ,
    criado_em       TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

CREATE TABLE guia_bolsa (
    guia_id   BIGINT NOT NULL REFERENCES guia_expedicao(id),
    bolsa_id  BIGINT NOT NULL REFERENCES bolsa(id),
    PRIMARY KEY (guia_id, bolsa_id)
);

-- ============================================================
-- V1 — Telemetria
-- ============================================================
CREATE TABLE leitura_telemetria (
    id          BIGSERIAL   PRIMARY KEY,
    guia_id     BIGINT      REFERENCES guia_expedicao(id),
    veiculo_id  BIGINT      REFERENCES veiculo(id),
    temperatura NUMERIC(5,2),
    latitude    NUMERIC(10,7),
    longitude   NUMERIC(10,7),
    alerta      BOOLEAN     NOT NULL DEFAULT FALSE,
    lido_em     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_telemetria_guia ON leitura_telemetria(guia_id, lido_em DESC);

-- ============================================================
-- V1 — Notificações internas
-- ============================================================
CREATE TYPE tipo_notificacao AS ENUM (
    'NOVA_REQUISICAO', 'REQUISICAO_ATENDIDA', 'BOLSA_VENCENDO',
    'BOLSA_VENCIDA', 'EXPEDICAO_INICIADA', 'ENTREGA_CONCLUIDA',
    'ALERTA_TEMPERATURA'
);

CREATE TABLE notificacao (
    id          BIGSERIAL          PRIMARY KEY,
    tipo        tipo_notificacao   NOT NULL,
    titulo      VARCHAR(200)       NOT NULL,
    mensagem    TEXT               NOT NULL,
    lida        BOOLEAN            NOT NULL DEFAULT FALSE,
    criada_em   TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

-- ============================================================
-- V1 — Dados sintéticos iniciais
-- ============================================================
INSERT INTO hospital (nome, cnpj, logradouro, cidade, estado, telefone, email, responsavel) VALUES
('Hospital das Clínicas Fictício',   '00.000.001/0001-01', 'Av. Dr. Enéas de Carvalho, 155', 'Recife',   'PE', '(81) 3000-0001', 'hcf@exemplo.com',   'Dra. Marina Fonseca'),
('UPA Central Sintética',            '00.000.002/0001-02', 'Rua da Esperança, 220',           'Olinda',   'PE', '(81) 3000-0002', 'upa@exemplo.com',   'Dr. Carlos Melo'),
('Hospital Metropolitano Teste',     '00.000.003/0001-03', 'Estrada do Arraial, 3000',        'Caruaru',  'PE', '(81) 3000-0003', 'hmt@exemplo.com',   'Dra. Renata Lima'),
('Santa Casa de Misericórdia Fictícia','00.000.004/0001-04','Rua Santa Fé, 50',              'Campina Grande','PB','(83) 3000-0004','scm@exemplo.com', 'Dr. Paulo Souza');

INSERT INTO unidade (hospital_id, nome) VALUES
(1, 'UTI Adulto'), (1, 'Pronto-Socorro'), (1, 'Bloco Cirúrgico'),
(2, 'Pronto-Atendimento'), (2, 'Observação'),
(3, 'UTI Neonatal'), (3, 'Hematologia'),
(4, 'Centro Cirúrgico');

INSERT INTO veiculo (placa, modelo) VALUES
('ABC-1D23', 'Fiorino Refrigerada'),
('XYZ-9E87', 'Sprinter Isotérmica'),
('QRS-4F56', 'VW Delivery Refrigerado');

-- senha: admin123 (BCrypt)
INSERT INTO usuario (login, senha_hash, nome, email, perfil) VALUES
('admin',    '$2a$12$N3zHRE9Q4L8KoSrGhFlBx.T0wh1xFX9Yde4sQ6Dm7pCDj6gWwxEHm', 'Administrador',           'admin@rotavital.local',     'ADMINISTRADOR'),
('estoque1', '$2a$12$N3zHRE9Q4L8KoSrGhFlBx.T0wh1xFX9Yde4sQ6Dm7pCDj6gWwxEHm', 'Ana Silva — Estoque',     'estoque@rotavital.local',   'ESTOQUE'),
('atend1',   '$2a$12$N3zHRE9Q4L8KoSrGhFlBx.T0wh1xFX9Yde4sQ6Dm7pCDj6gWwxEHm', 'Bruno Lima — Atendimento','atend@rotavital.local',     'ATENDIMENTO'),
('exped1',   '$2a$12$N3zHRE9Q4L8KoSrGhFlBx.T0wh1xFX9Yde4sQ6Dm7pCDj6gWwxEHm', 'Carla Mota — Expedição',  'expedicao@rotavital.local', 'EXPEDICAO');

-- Bolsas sintéticas de estoque
INSERT INTO bolsa (codigo, abo, rh, componente, data_coleta, data_validade, localizacao, status) VALUES
('RV-2026-00001', 'O',  'NEGATIVO',  'CH',   '2026-10-01', '2026-11-12', 'Prateleira A1', 'DISPONIVEL'),
('RV-2026-00002', 'O',  'POSITIVO',  'CH',   '2026-10-05', '2026-11-20', 'Prateleira A1', 'DISPONIVEL'),
('RV-2026-00003', 'O',  'POSITIVO',  'CH',   '2026-10-10', '2026-11-25', 'Prateleira A1', 'DISPONIVEL'),
('RV-2026-00004', 'A',  'POSITIVO',  'CH',   '2026-10-08', '2026-11-18', 'Prateleira A2', 'DISPONIVEL'),
('RV-2026-00005', 'A',  'NEGATIVO',  'CH',   '2026-10-12', '2026-11-22', 'Prateleira A2', 'DISPONIVEL'),
('RV-2026-00006', 'B',  'POSITIVO',  'CH',   '2026-10-15', '2026-11-28', 'Prateleira A3', 'DISPONIVEL'),
('RV-2026-00007', 'B',  'NEGATIVO',  'CH',   '2026-10-20', '2026-12-01', 'Prateleira A3', 'DISPONIVEL'),
('RV-2026-00008', 'AB', 'POSITIVO',  'CH',   '2026-10-22', '2026-12-05', 'Prateleira A4', 'DISPONIVEL'),
('RV-2026-00009', 'O',  'POSITIVO',  'PFC',  '2026-09-01', '2027-03-01', 'Freezer B1',   'DISPONIVEL'),
('RV-2026-00010', 'A',  'POSITIVO',  'PFC',  '2026-09-05', '2027-03-05', 'Freezer B1',   'DISPONIVEL'),
('RV-2026-00011', 'O',  'POSITIVO',  'CP',   '2026-11-01', '2026-11-06', 'Prateleira C1', 'DISPONIVEL'),
('RV-2026-00012', 'A',  'POSITIVO',  'CP',   '2026-11-02', '2026-11-07', 'Prateleira C1', 'DISPONIVEL'),
('RV-2026-00013', 'O',  'NEGATIVO',  'CH',   '2026-10-01', '2026-11-08', 'Prateleira A1', 'DISPONIVEL'),
('RV-2026-00014', 'O',  'POSITIVO',  'CRIO', '2026-09-10', '2027-09-10', 'Freezer B2',   'DISPONIVEL'),
('RV-2026-00015', 'AB', 'NEGATIVO',  'CH',   '2026-10-25', '2026-12-10', 'Prateleira A4', 'DISPONIVEL');

