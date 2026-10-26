-- =====================================================================
-- AquaSample · Tabla 16 de 19: observacion_muestra
-- Requiere que existan antes: muestra, usuario
-- =====================================================================
USE aquasample;

-- Observaciones del muestreo (RF03): varias por muestra
CREATE TABLE IF NOT EXISTS observacion_muestra (
    id_observacion  INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_muestra      INT UNSIGNED NOT NULL,
    id_usuario      INT UNSIGNED NOT NULL,
    texto           VARCHAR(500) NOT NULL,
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_observacion),
    KEY ix_obs_muestra (id_muestra),
    CONSTRAINT fk_obs_muestra FOREIGN KEY (id_muestra) REFERENCES muestra(id_muestra) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_obs_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_obs_texto CHECK (CHAR_LENGTH(TRIM(texto)) > 0)
) ENGINE=InnoDB;
