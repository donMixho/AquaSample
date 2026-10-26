-- =====================================================================
-- AquaSample · Tabla 08 de 19: tren
-- Requiere que existan antes: concesion
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS tren (
    id_tren       INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_concesion  INT UNSIGNED NOT NULL,
    codigo        VARCHAR(20)  NOT NULL,
    nombre        VARCHAR(100) NOT NULL,
    activo        TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_tren),
    UNIQUE KEY uq_tren_concesion_codigo (id_concesion, codigo),
    CONSTRAINT fk_tren_concesion FOREIGN KEY (id_concesion) REFERENCES concesion(id_concesion) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_tren_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB;
