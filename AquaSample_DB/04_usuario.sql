-- =====================================================================
-- AquaSample · Tabla 04 de 19: usuario
-- Requiere que existan antes: rol
-- =====================================================================
USE aquasample;

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario       INT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo_ficticio  VARCHAR(20)  NOT NULL,                       -- ej. OP-001 (identificador ficticio)
    nombre_usuario   VARCHAR(30)  NOT NULL,                       -- login ficticio
    nombre_visible   VARCHAR(60)  NOT NULL,                       -- alias ficticio (sin nombres reales)
    contrasena_hash  VARCHAR(255) NOT NULL,                       -- nunca guardar texto plano
    id_rol           INT UNSIGNED NOT NULL,
    activo           TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_usuario_codigo (codigo_ficticio),
    UNIQUE KEY uq_usuario_login  (nombre_usuario),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON UPDATE CASCADE,
    CONSTRAINT ck_usuario_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB;
