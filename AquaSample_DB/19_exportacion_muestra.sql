-- =====================================================================
-- AquaSample · Tabla 19 de 19: exportacion_muestra
-- Requiere que existan antes: exportacion, muestra
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS exportacion_muestra (
    id_exportacion  INT UNSIGNED NOT NULL,
    id_muestra      INT UNSIGNED NOT NULL,
    estado_envio    VARCHAR(10)  NOT NULL DEFAULT 'PENDIENTE',
    PRIMARY KEY (id_exportacion, id_muestra),
    KEY ix_expmue_muestra (id_muestra),
    CONSTRAINT fk_expmue_export  FOREIGN KEY (id_exportacion) REFERENCES exportacion(id_exportacion) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_expmue_muestra FOREIGN KEY (id_muestra)     REFERENCES muestra(id_muestra)         ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_expmue_estado CHECK (estado_envio IN ('PENDIENTE', 'ENVIADO', 'ERROR'))
) ENGINE=InnoDB;
