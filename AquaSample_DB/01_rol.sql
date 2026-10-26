CREATE TABLE IF NOT EXISTS rol (
    id_rol       INT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo       VARCHAR(20)  NOT NULL,
    nombre       VARCHAR(60)  NOT NULL,
    descripcion  VARCHAR(200) NOT NULL,
    PRIMARY KEY (id_rol),
    UNIQUE KEY uq_rol_codigo (codigo)
) ENGINE=InnoDB;
