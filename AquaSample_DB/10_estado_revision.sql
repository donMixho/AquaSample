-- =====================================================================
-- AquaSample · Tabla 10 de 19: estado_revision
-- Requiere que existan antes: ninguna
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS estado_revision (
    id_estado    TINYINT UNSIGNED NOT NULL,                       -- IDs fijos (ver cabecera)
    codigo       VARCHAR(20)      NOT NULL,
    nombre       VARCHAR(40)      NOT NULL,
    descripcion  VARCHAR(200)     NOT NULL,
    orden        TINYINT UNSIGNED NOT NULL,
    es_final     TINYINT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id_estado),
    UNIQUE KEY uq_estado_codigo (codigo),
    CONSTRAINT ck_estado_final CHECK (es_final IN (0, 1))
) ENGINE=InnoDB;
