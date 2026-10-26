-- =====================================================================
-- AquaSample · Tabla 15 de 19: foto_muestra
-- Requiere que existan antes: muestra, usuario
-- =====================================================================
USE aquasample;

-- Evidencia fotográfica (RF04): cada foto pertenece obligatoriamente a una muestra.
-- es_principal = 1 marca la foto principal y NULL las demás (solo una principal por muestra).
CREATE TABLE IF NOT EXISTS foto_muestra (
    id_foto        INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_muestra     INT UNSIGNED NOT NULL,
    ruta_archivo   VARCHAR(255) NOT NULL,                         -- URI local (imágenes de prueba, sin personas)
    origen         VARCHAR(10)  NOT NULL,
    es_principal   TINYINT      NULL DEFAULT NULL,
    id_usuario     INT UNSIGNED NOT NULL,
    fecha_captura  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_foto),
    UNIQUE KEY uq_foto_ruta      (id_muestra, ruta_archivo),
    UNIQUE KEY ux_foto_principal (id_muestra, es_principal),
    CONSTRAINT fk_foto_muestra FOREIGN KEY (id_muestra) REFERENCES muestra(id_muestra) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_foto_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_foto_origen    CHECK (origen IN ('CAMARA', 'GALERIA', 'SIMULADA')),
    CONSTRAINT ck_foto_principal CHECK (es_principal IS NULL OR es_principal = 1)
) ENGINE=InnoDB;
