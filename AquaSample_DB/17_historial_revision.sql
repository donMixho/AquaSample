-- =====================================================================
-- AquaSample · Tabla 17 de 19: historial_revision
-- Requiere que existan antes: muestra, estado_revision, usuario
-- =====================================================================
USE aquasample;

-- Bitácora de estados + comentario del supervisor (RF09, trazabilidad)
CREATE TABLE IF NOT EXISTS historial_revision (
    id_historial        INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    id_muestra          INT UNSIGNED     NOT NULL,
    id_estado_anterior  TINYINT UNSIGNED NULL,                    -- NULL solo en la creación
    id_estado_nuevo     TINYINT UNSIGNED NOT NULL,
    id_usuario          INT UNSIGNED     NOT NULL,
    comentario          VARCHAR(500)     NULL,
    fecha_registro      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_historial),
    KEY ix_hist_muestra (id_muestra, id_historial),
    CONSTRAINT fk_hist_muestra  FOREIGN KEY (id_muestra)         REFERENCES muestra(id_muestra) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_hist_anterior FOREIGN KEY (id_estado_anterior) REFERENCES estado_revision(id_estado) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_hist_nuevo    FOREIGN KEY (id_estado_nuevo)    REFERENCES estado_revision(id_estado) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_hist_usuario  FOREIGN KEY (id_usuario)         REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
