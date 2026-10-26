-- =====================================================================
-- AquaSample · Tabla 09 de 19: linea_cultivo
-- Requiere que existan antes: tren
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS linea_cultivo (
    id_linea  INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_tren   INT UNSIGNED NOT NULL,
    codigo    VARCHAR(20)  NOT NULL,
    nombre    VARCHAR(100) NOT NULL,
    activo    TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_linea),
    UNIQUE KEY uq_linea_tren_codigo (id_tren, codigo),
    CONSTRAINT fk_linea_tren FOREIGN KEY (id_tren) REFERENCES tren(id_tren) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_linea_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB;
