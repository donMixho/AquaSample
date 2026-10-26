-- =====================================================================
-- AquaSample · Tabla 07 de 19: concesion
-- Requiere que existan antes: centro
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS concesion (
    id_concesion  INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_centro     INT UNSIGNED NOT NULL,
    codigo        VARCHAR(20)  NOT NULL,
    nombre        VARCHAR(100) NOT NULL,
    activo        TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_concesion),
    UNIQUE KEY uq_concesion_centro_codigo (id_centro, codigo),
    CONSTRAINT fk_concesion_centro FOREIGN KEY (id_centro) REFERENCES centro(id_centro) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_concesion_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB;
