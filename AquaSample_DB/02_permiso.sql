-- =====================================================================
-- AquaSample · Tabla 02 de 19: permiso
-- Requiere que existan antes: ninguna
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS permiso (
    id_permiso   INT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo       VARCHAR(40)  NOT NULL,
    descripcion  VARCHAR(200) NOT NULL,
    PRIMARY KEY (id_permiso),
    UNIQUE KEY uq_permiso_codigo (codigo)
) ENGINE=InnoDB;
