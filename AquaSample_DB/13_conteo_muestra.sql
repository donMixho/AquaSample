-- =====================================================================
-- AquaSample · Tabla 13 de 19: conteo_muestra
-- Requiere que existan antes: muestra, usuario
-- =====================================================================
USE aquasample;

-- Conteo de individuos. Varias filas por muestra (historial de correcciones);
-- vigente = 1 marca el conteo actual y NULL los históricos (solo puede haber un 1 por muestra).
CREATE TABLE IF NOT EXISTS conteo_muestra (
    id_conteo            INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_muestra           INT UNSIGNED NOT NULL,
    cantidad_individuos  INT UNSIGNED NOT NULL,
    tipo_conteo          VARCHAR(20)  NOT NULL DEFAULT 'MANUAL',
    vigente              TINYINT      NULL     DEFAULT 1,
    id_usuario           INT UNSIGNED NOT NULL,
    motivo_correccion    VARCHAR(255) NULL,
    fecha_registro       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_conteo),
    UNIQUE KEY ux_conteo_vigente (id_muestra, vigente),
    CONSTRAINT fk_conteo_muestra  FOREIGN KEY (id_muestra) REFERENCES muestra(id_muestra) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_conteo_usuario  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_conteo_tipo     CHECK (tipo_conteo IN ('MANUAL', 'SIMULADO', 'EXPERIMENTAL_IMAGEN')),
    CONSTRAINT ck_conteo_vigente  CHECK (vigente IS NULL OR vigente = 1)
) ENGINE=InnoDB;
