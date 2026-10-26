-- =====================================================================
-- AquaSample · Tabla 06 de 19: centro
-- Requiere que existan antes: empresa
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS centro (
    id_centro              INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_empresa             INT UNSIGNED NOT NULL,
    codigo                 VARCHAR(20)  NOT NULL,
    nombre                 VARCHAR(100) NOT NULL,
    ubicacion_referencial  VARCHAR(150) NULL,                     -- texto ficticio, sin coordenadas reales
    activo                 TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_centro),
    UNIQUE KEY uq_centro_empresa_codigo (id_empresa, codigo),
    CONSTRAINT fk_centro_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_centro_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB;
