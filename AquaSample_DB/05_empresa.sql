-- =====================================================================
-- AquaSample · Tabla 05 de 19: empresa
-- Requiere que existan antes: ninguna
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS empresa (
    id_empresa  INT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(20)  NOT NULL,
    nombre      VARCHAR(100) NOT NULL,
    activo      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_empresa),
    UNIQUE KEY uq_empresa_codigo (codigo),
    CONSTRAINT ck_empresa_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB;
