-- =====================================================================
-- AquaSample · Tabla 11 de 19: transicion_estado
-- Requiere que existan antes: estado_revision, rol
-- =====================================================================
USE aquasample;

-- Qué transición puede ejecutar qué rol (el flujo vive en datos, no en código)
CREATE TABLE IF NOT EXISTS transicion_estado (
    id_estado_origen   TINYINT UNSIGNED NOT NULL,
    id_estado_destino  TINYINT UNSIGNED NOT NULL,
    id_rol             INT UNSIGNED     NOT NULL,
    PRIMARY KEY (id_estado_origen, id_estado_destino, id_rol),
    CONSTRAINT fk_trans_origen  FOREIGN KEY (id_estado_origen)  REFERENCES estado_revision(id_estado) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_trans_destino FOREIGN KEY (id_estado_destino) REFERENCES estado_revision(id_estado) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_trans_rol     FOREIGN KEY (id_rol)            REFERENCES rol(id_rol) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
