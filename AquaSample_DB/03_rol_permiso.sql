-- =====================================================================
-- AquaSample · Tabla 03 de 19: rol_permiso
-- Requiere que existan antes: rol, permiso
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS rol_permiso (
    id_rol      INT UNSIGNED NOT NULL,
    id_permiso  INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    CONSTRAINT fk_rolperm_rol     FOREIGN KEY (id_rol)     REFERENCES rol(id_rol)         ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rolperm_permiso FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
