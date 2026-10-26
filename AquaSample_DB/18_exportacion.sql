-- =====================================================================
-- AquaSample · Tabla 18 de 19: exportacion
-- Requiere que existan antes: usuario
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS exportacion (
    id_exportacion      INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_usuario          INT UNSIGNED NOT NULL,
    destino             VARCHAR(20)  NOT NULL,
    estado              VARCHAR(10)  NOT NULL DEFAULT 'PENDIENTE',
    detalle             VARCHAR(255) NULL,
    fecha_solicitud     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion  DATETIME     NULL,
    PRIMARY KEY (id_exportacion),
    CONSTRAINT fk_export_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_export_destino CHECK (destino IN ('PLANILLA_SIMULADA', 'API_FICTICIA', 'CSV_LOCAL')),
    CONSTRAINT ck_export_estado  CHECK (estado IN ('PENDIENTE', 'ENVIADA', 'ERROR'))
) ENGINE=InnoDB;
