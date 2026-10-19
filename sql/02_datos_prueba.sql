INSERT INTO cliente VALUES ('1234567890101', 'Carlos', 'Pérez', '55123456', 'Zona 1, Guatemala', 'carlos@correo.com');
INSERT INTO cliente VALUES ('2345678901012', 'María', 'López', '55234567', 'Zona 10, Guatemala', 'maria@correo.com');
INSERT INTO cliente VALUES ('3456789010123', 'José', 'García', '55345678', 'Mixco', 'jose@correo.com');
INSERT INTO cliente VALUES ('4567890101234', 'Ana', 'Martínez', '55456789', 'Villa Nueva', 'ana@correo.com');
INSERT INTO cliente VALUES ('5678901012345', 'Luis', 'Ramírez', '55567890', 'Zona 18, Guatemala', NULL);

INSERT INTO mecanico VALUES ('M001', 'Pedro', 'Hernández', '44111111', 'Motor');
INSERT INTO mecanico VALUES ('M002', 'Jorge', 'Castillo', '44222222', 'Frenos y suspensión');
INSERT INTO mecanico VALUES ('M003', 'Rosa', 'Morales', '44333333', 'Electricidad');

INSERT INTO servicio VALUES ('S001', 'Cambio de aceite', 100);
INSERT INTO servicio VALUES ('S002', 'Revisión de frenos', 150);
INSERT INTO servicio VALUES ('S003', 'Alineación', 125);
INSERT INTO servicio VALUES ('S004', 'Reparación de motor', 1500);
INSERT INTO servicio VALUES ('S005', 'Diagnóstico eléctrico', 200);

INSERT INTO repuesto VALUES ('R001', 'Aceite 15W40', 'Galón de aceite para motor', 175.00, 20);
INSERT INTO repuesto VALUES ('R002', 'Filtro de aceite', 'Filtro universal', 45.00, 15);
INSERT INTO repuesto VALUES ('R003', 'Pastillas de freno', 'Juego delantero', 250.00, 8);
INSERT INTO repuesto VALUES ('R004', 'Disco de freno', 'Disco ventilado', 400.00, 4);
INSERT INTO repuesto VALUES ('R005', 'Bujía', 'Bujía de iridio', 60.00, 30);
INSERT INTO repuesto VALUES ('R006', 'Batería 12V', 'Batería 45 amperios', 850.00, 3);
INSERT INTO repuesto VALUES ('R007', 'Filtro de aire', 'Filtro de aire de motor', 80.00, 5);
INSERT INTO repuesto VALUES ('R008', 'Banda de distribución', 'Kit de distribución', 650.00, 0);
INSERT INTO repuesto VALUES ('R009', 'Líquido de frenos', 'DOT 4, 500 ml', 55.00, 12);
INSERT INTO repuesto VALUES ('R010', 'Amortiguador', 'Amortiguador delantero', 525.00, 2);

INSERT INTO vehiculo VALUES ('P123ABC', 'Toyota', 'Corolla', 2015, 'Blanco', '1234567890101');
INSERT INTO vehiculo VALUES ('P456DEF', 'Honda', 'Civic', 2018, 'Gris', '1234567890101');
INSERT INTO vehiculo VALUES ('P789GHI', 'Mazda', '3', 2020, 'Rojo', '2345678901012');
INSERT INTO vehiculo VALUES ('P321JKL', 'Nissan', 'Sentra', 2012, 'Negro', '3456789010123');
INSERT INTO vehiculo VALUES ('P654MNO', 'Hyundai', 'Tucson', 2019, 'Azul', '4567890101234');
INSERT INTO vehiculo VALUES ('P987PQR', 'Kia', 'Rio', 2021, 'Blanco', '4567890101234');
INSERT INTO vehiculo VALUES ('P111STU', 'Suzuki', 'Swift', 2017, 'Plateado', '5678901012345');
INSERT INTO vehiculo VALUES ('P222VWX', 'Toyota', 'Hilux', 2016, 'Gris', '3456789010123');

INSERT INTO orden_trabajo (placa, id_mecanico, descripcion_problema, estado, fecha_creacion)
VALUES ('P123ABC', 'M001', 'Ruido extraño en el motor', 'PENDIENTE', SYSDATE);

INSERT INTO orden_trabajo (placa, id_mecanico, descripcion_problema, estado, fecha_creacion, fecha_finalizacion, total_mano_obra, total_repuestos, total)
VALUES ('P789GHI', 'M002', 'Frenos rechinan al frenar', 'FINALIZADA', SYSDATE - 10, SYSDATE - 8, 275, 555, 830);
INSERT INTO detalle_servicio (id_orden, id_servicio, costo) VALUES (2, 'S002', 150);
INSERT INTO detalle_servicio (id_orden, id_servicio, costo) VALUES (2, 'S003', 125);
INSERT INTO detalle_repuesto (id_orden, id_repuesto, cantidad, precio_unitario, subtotal) VALUES (2, 'R003', 2, 250, 500);
INSERT INTO detalle_repuesto (id_orden, id_repuesto, cantidad, precio_unitario, subtotal) VALUES (2, 'R009', 1, 55, 55);

INSERT INTO orden_trabajo (placa, id_mecanico, descripcion_problema, estado, fecha_creacion)
VALUES ('P654MNO', 'M001', 'Mantenimiento de 50,000 km', 'EN PROCESO', SYSDATE - 1);
INSERT INTO detalle_servicio (id_orden, id_servicio, costo) VALUES (3, 'S001', 100);
INSERT INTO detalle_repuesto (id_orden, id_repuesto, cantidad, precio_unitario, subtotal) VALUES (3, 'R001', 1, 175, 175);
INSERT INTO detalle_repuesto (id_orden, id_repuesto, cantidad, precio_unitario, subtotal) VALUES (3, 'R002', 1, 45, 45);

INSERT INTO orden_trabajo (placa, id_mecanico, descripcion_problema, estado, fecha_creacion)
VALUES ('P111STU', 'M003', 'No enciende', 'CANCELADA', SYSDATE - 5);

INSERT INTO abastecimiento (id_repuesto, cantidad, fecha) VALUES ('R001', 20, SYSDATE - 30);
INSERT INTO abastecimiento (id_repuesto, cantidad, fecha) VALUES ('R005', 30, SYSDATE - 30);

COMMIT;
