-- =====================================================================
-- AquaSample · Tabla 12 de 19: muestra
-- Requiere que existan antes: linea_cultivo, usuario, estado_revision
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS muestra (
    id_muestra           INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    codigo_muestra       VARCHAR(30)      NOT NULL,
    id_linea             INT UNSIGNED     NOT NULL,
    id_operador          INT UNSIGNED     NOT NULL,
    fecha_hora_muestreo  DATETIME         NOT NULL,
    longitud_tramo_m     DECIMAL(8,2)     NOT NULL,
    id_estado_actual     TINYINT UNSIGNED NOT NULL DEFAULT 1,
    autorizada_cliente   TINYINT(1)       NOT NULL DEFAULT 0,
    estado_sync          VARCHAR(15)      NOT NULL DEFAULT 'PENDIENTE',
    fecha_ultima_sync    DATETIME         NULL,
    fecha_creacion       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    fecha_cierre         DATETIME         NULL,
    PRIMARY KEY (id_muestra),
    UNIQUE KEY uq_muestra_codigo (codigo_muestra),
    KEY ix_muestra_linea_fecha (id_linea, fecha_hora_muestreo),
    KEY ix_muestra_fecha       (fecha_hora_muestreo),
    KEY ix_muestra_operador    (id_operador),
    KEY ix_muestra_estado      (id_estado_actual),
    CONSTRAINT fk_muestra_linea    FOREIGN KEY (id_linea)          REFERENCES linea_cultivo(id_linea) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_muestra_operador FOREIGN KEY (id_operador)       REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_muestra_estado   FOREIGN KEY (id_estado_actual)  REFERENCES estado_revision(id_estado) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_muestra_longitud CHECK (longitud_tramo_m > 0),
    CONSTRAINT ck_muestra_autoriz  CHECK (autorizada_cliente IN (0, 1)),
    CONSTRAINT ck_muestra_sync     CHECK (estado_sync IN ('PENDIENTE', 'SINCRONIZADO', 'ERROR'))
) ENGINE=InnoDB;
