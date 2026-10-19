CREATE TABLE cliente (
    id_cliente   VARCHAR2(20)  PRIMARY KEY,
    nombre       VARCHAR2(60)  NOT NULL,
    apellido     VARCHAR2(60)  NOT NULL,
    telefono     VARCHAR2(15),
    direccion    VARCHAR2(150),
    correo       VARCHAR2(100)
);

CREATE TABLE mecanico (
    id_mecanico  VARCHAR2(20)  PRIMARY KEY,
    nombre       VARCHAR2(60)  NOT NULL,
    apellido     VARCHAR2(60)  NOT NULL,
    telefono     VARCHAR2(15),
    especialidad VARCHAR2(60)
);

CREATE TABLE servicio (
    id_servicio     VARCHAR2(20)  PRIMARY KEY,
    descripcion     VARCHAR2(150) NOT NULL,
    costo_mano_obra NUMBER(10,2)  NOT NULL CHECK (costo_mano_obra >= 0)
);

CREATE TABLE repuesto (
    id_repuesto  VARCHAR2(20)  PRIMARY KEY,
    nombre       VARCHAR2(80)  NOT NULL,
    descripcion  VARCHAR2(200),
    precio       NUMBER(10,2)  NOT NULL CHECK (precio >= 0),
    existencia   NUMBER(8)     NOT NULL CHECK (existencia >= 0)
);

CREATE TABLE vehiculo (
    placa        VARCHAR2(10)  PRIMARY KEY,
    marca        VARCHAR2(40)  NOT NULL,
    modelo       VARCHAR2(40)  NOT NULL,
    anio         NUMBER(4),
    color        VARCHAR2(30),
    id_cliente   VARCHAR2(20)  NOT NULL,
    CONSTRAINT fk_vehiculo_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

CREATE TABLE orden_trabajo (
    id_orden             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    placa                VARCHAR2(10)  NOT NULL,
    id_mecanico          VARCHAR2(20)  NOT NULL,
    descripcion_problema VARCHAR2(500) NOT NULL,
    estado               VARCHAR2(15)  DEFAULT 'PENDIENTE' NOT NULL
                         CHECK (estado IN ('PENDIENTE','EN PROCESO','FINALIZADA','CANCELADA')),
    fecha_creacion       DATE DEFAULT SYSDATE NOT NULL,
    fecha_finalizacion   DATE,
    total_mano_obra      NUMBER(12,2) DEFAULT 0,
    total_repuestos      NUMBER(12,2) DEFAULT 0,
    total                NUMBER(12,2) DEFAULT 0,
    CONSTRAINT fk_orden_vehiculo FOREIGN KEY (placa) REFERENCES vehiculo(placa),
    CONSTRAINT fk_orden_mecanico FOREIGN KEY (id_mecanico) REFERENCES mecanico(id_mecanico)
);

CREATE TABLE detalle_servicio (
    id_detalle   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_orden     NUMBER        NOT NULL,
    id_servicio  VARCHAR2(20)  NOT NULL,
    costo        NUMBER(10,2)  NOT NULL CHECK (costo >= 0),
    CONSTRAINT fk_dserv_orden FOREIGN KEY (id_orden) REFERENCES orden_trabajo(id_orden),
    CONSTRAINT fk_dserv_servicio FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio)
);

CREATE TABLE detalle_repuesto (
    id_detalle      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_orden        NUMBER        NOT NULL,
    id_repuesto     VARCHAR2(20)  NOT NULL,
    cantidad        NUMBER(6)     NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMBER(10,2)  NOT NULL CHECK (precio_unitario >= 0),
    subtotal        NUMBER(12,2)  NOT NULL,
    CONSTRAINT fk_drep_orden FOREIGN KEY (id_orden) REFERENCES orden_trabajo(id_orden),
    CONSTRAINT fk_drep_repuesto FOREIGN KEY (id_repuesto) REFERENCES repuesto(id_repuesto)
);

CREATE TABLE abastecimiento (
    id_abastecimiento NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_repuesto       VARCHAR2(20) NOT NULL,
    cantidad          NUMBER(8)    NOT NULL CHECK (cantidad > 0),
    fecha             DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_abast_repuesto FOREIGN KEY (id_repuesto) REFERENCES repuesto(id_repuesto)
);
