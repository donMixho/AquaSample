-- =====================================================================
-- AquaSample · Tabla 14 de 19: medicion_muestra
-- Requiere que existan antes: muestra, usuario
-- =====================================================================
USE aquasample;

-- Datos opcionales (RF06): relación 1:1 con muestra
CREATE TABLE IF NOT EXISTS medicion_muestra (
    id_muestra           INT UNSIGNED  NOT NULL,
    calibre_promedio_mm  DECIMAL(6,2)  NULL,
    calibre_minimo_mm    DECIMAL(6,2)  NULL,
    calibre_maximo_mm    DECIMAL(6,2)  NULL,
    peso_muestra_kg      DECIMAL(8,2)  NULL,
    comentario_tecnico   VARCHAR(500)  NULL,
    id_usuario           INT UNSIGNED  NOT NULL,
    fecha_registro       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_muestra),
    CONSTRAINT fk_medicion_muestra FOREIGN KEY (id_muestra) REFERENCES muestra(id_muestra) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_medicion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_med_prom   CHECK (calibre_promedio_mm > 0),
    CONSTRAINT ck_med_min    CHECK (calibre_minimo_mm > 0),
    CONSTRAINT ck_med_max    CHECK (calibre_maximo_mm > 0),
    CONSTRAINT ck_med_peso   CHECK (peso_muestra_kg > 0),
    CONSTRAINT ck_med_min_max  CHECK (calibre_minimo_mm IS NULL OR calibre_maximo_mm IS NULL OR calibre_minimo_mm <= calibre_maximo_mm),
    CONSTRAINT ck_med_prom_min CHECK (calibre_promedio_mm IS NULL OR calibre_minimo_mm IS NULL OR calibre_promedio_mm >= calibre_minimo_mm),
    CONSTRAINT ck_med_prom_max CHECK (calibre_promedio_mm IS NULL OR calibre_maximo_mm IS NULL OR calibre_promedio_mm <= calibre_maximo_mm)
) ENGINE=InnoDB;
