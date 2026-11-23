-- =====================================================================
--  Cenaduría La Comadre · Datos de ejemplo
-- =====================================================================
--  Cómo usarlo:
--    1. Corre el sistema una vez: Hibernate crea la base de datos
--       "cenaduria_la_comadre" y todas las tablas (vacías).
--    2. Cierra el sistema.
--    3. Ejecuta este script (MySQL Workbench: File > Open SQL Script > ⚡,
--       o en consola: mysql -u root -p < datos_ejemplo.sql).
--    4. Vuelve a abrir el sistema.
--
--  OJO: el script BORRA todo lo que haya en las tablas y deja solo estos
--  datos, así que se puede correr las veces que quieras.
--
--  Para entrar como administrador: contraseña  admin123
--
--  Las fechas son relativas al momento en que corres el script
--  (pedidos de "hoy" y de "ayer"), para que Inicio y Reportes tengan datos.
-- =====================================================================

USE cenaduria_la_comadre;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE partidas_pago;
TRUNCATE TABLE pagos;
TRUNCATE TABLE detalle_pedido_variaciones;
TRUNCATE TABLE detalles_pedido;
TRUNCATE TABLE pedidos;
TRUNCATE TABLE variaciones_platillo;
TRUNCATE TABLE platillos;
TRUNCATE TABLE movimientos_insumo;
TRUNCATE TABLE insumos;
TRUNCATE TABLE usuarios;
TRUNCATE TABLE configuracion_negocio;
SET FOREIGN_KEY_CHECKS = 1;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- Datos del negocio (salen en el ticket)
-- ---------------------------------------------------------------------
INSERT INTO configuracion_negocio (id, nombre_negocio, direccion, telefono, mensaje_ticket, nombre_caja, numero_mesas)
VALUES (1, 'Cenaduría La Comadre', 'Calle Juárez 123, Centro', '644 123 4567',
        '¡Gracias por su visita, vuelva pronto!', 'Caja 01', 20);

-- ---------------------------------------------------------------------
-- Usuarios
--   El administrador entra con la contraseña admin123 (se guarda solo el hash).
--   "caja" es el usuario con el que se registran los pedidos sin iniciar sesión:
--   no lo borres ni lo desactives. Cajeros y meseros no usan contraseña.
-- ---------------------------------------------------------------------
INSERT INTO usuarios (id, nombre, nombre_usuario, contrasena_hash, rol, activo) VALUES
(1, 'Administrador', 'admin', '210000:bbLSM9bYmxSWQ7tRaDWmzg==:HW7TKXBUA/xq0wz4c7viMPp1jLNPKGFpsHaOZD0C+EA=', 'ADMINISTRADOR', 1),
(2, 'Caja',          'caja',  '210000:z1gOhm0mENq+LPjdmDzGew==:Esn3EZcNAjSoRNiHIZsWKuUtWZ8KExz45v5PKIFR+M4=', 'CAJERO',        1),
(3, 'Lupita Gómez',  'lupita','210000:4X1Qd1vqZBNf51TjklrC1Q==:NMUM+WGO6BTrmO4qSCRLH3gyr90Edknlq65r5vVaqZo=', 'MESERO',        1);

-- ---------------------------------------------------------------------
-- Insumos (el estado Disponible / Bajo / Agotado se calcula con stock y stock_minimo)
-- ---------------------------------------------------------------------
INSERT INTO insumos (id, nombre, stock, stock_minimo, unidad, categoria, activo) VALUES
(1, 'Jitomate',         2,  3, 'KG',      'VERDURAS',  1),
(2, 'Cebolla',          0,  2, 'KG',      'VERDURAS',  1),
(3, 'Queso',            1,  2, 'KG',      'LACTEOS',   1),
(4, 'Pechuga de pollo', 8,  3, 'KG',      'CARNES',    1),
(5, 'Chile poblano',    3,  2, 'PIEZA',   'VERDURAS',  1),
(6, 'Tortillas',        2,  3, 'PAQUETE', 'ABARROTES', 1),
(7, 'Lechuga',          5,  2, 'PIEZA',   'VERDURAS',  1),
(8, 'Maíz pozolero',   10,  4, 'KG',      'ABARROTES', 1),
(9, 'Refresco',        24,  6, 'PIEZA',   'BEBIDAS',   1),
(10,'Vasos desechables',50, 20, 'PIEZA',  'DESECHABLES', 1);

-- Movimientos de inventario (compras y cierres del día de ayer y hoy)
INSERT INTO movimientos_insumo (id, tipo, motivo, fecha, cantidad, observacion, id_insumo, id_usuario) VALUES
(1, 'ENTRADA', 'COMPRA',          TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '09:00:00'), 10, 'Mercado de abastos', 1, 1),
(2, 'SALIDA',  'CONSUMO_JORNADA', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '22:30:00'),  7, NULL,                 1, 1),
(3, 'SALIDA',  'MERMA',           TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '22:30:00'),  1, 'Se echó a perder',   1, 1),
(4, 'ENTRADA', 'COMPRA',          TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '09:15:00'), 12, NULL,                 4, 1),
(5, 'SALIDA',  'CONSUMO_JORNADA', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '22:30:00'),  4, NULL,                 4, 1),
(6, 'SALIDA',  'CONSUMO_JORNADA', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '22:30:00'),  3, NULL,                 2, 1),
(7, 'ENTRADA', 'COMPRA',          NOW() - INTERVAL 120 MINUTE,                       10, NULL,                 8, 1);

-- ---------------------------------------------------------------------
-- Menú
-- ---------------------------------------------------------------------
INSERT INTO platillos (id, nombre, descripcion, precio, categoria, disponible) VALUES
(1,  'Pozole rojo',       'Orden grande con tostadas', 65.00, 'PLATILLO', 1),
(2,  'Pozole blanco',     'Orden grande con tostadas', 65.00, 'PLATILLO', 1),
(3,  'Tostada de tinga',  NULL,                        35.00, 'PLATILLO', 1),
(4,  'Tacos dorados',     'Orden de 4',                55.00, 'PLATILLO', 1),
(5,  'Sope',              NULL,                        30.00, 'PLATILLO', 1),
(6,  'Chilaquiles rojos', NULL,                        40.00, 'PLATILLO', 1),
(7,  'Enchiladas',        'Orden de 4',                85.00, 'PLATILLO', 1),
(8,  'Agua de jamaica',   'Vaso grande',               25.00, 'BEBIDA',   1),
(9,  'Agua de horchata',  'Vaso grande',               25.00, 'BEBIDA',   1),
(10, 'Refresco',          '600 ml',                    30.00, 'BEBIDA',   1);

-- Extras con costo
INSERT INTO variaciones_platillo (id, nombre, precio_extra, id_platillo, activa) VALUES
(1, 'Aguacate extra', 10.00, 3, 1),
(2, 'Extra queso',    10.00, 4, 1),
(3, 'Extra queso',    15.00, 7, 1);

-- ---------------------------------------------------------------------
-- Pedidos (registrados por "Caja", id_usuario = 2)
--   P-0001  ayer   Mesa 01      Pagado (efectivo)
--   P-0002  ayer   Para llevar  Pagado (tarjeta)
--   P-0003  ayer   Mesa 04      Cancelado
--   P-0004  hoy    Mesa 02      Pagado (mixto)
--   P-0005  hoy    Mesa 03      Activo, cuenta separada a medias (pagaron 2 pozoles)
--   P-0006  hoy    Mesa 07      Activo, sin pagar
--   P-0007  hoy    Para llevar  Activo, sin pagar
-- ---------------------------------------------------------------------
INSERT INTO pedidos (id, folio, fecha_hora, tipo, mesa, nombre_cliente, notas, estado,
                     motivo_cancelacion, fecha_cancelacion, id_usuario) VALUES
(1, 1, TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '19:10:00'), 'EN_MESA',     1,    NULL, NULL, 'PAGADO',    NULL, NULL, 2),
(2, 2, TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '20:00:00'), 'PARA_LLEVAR', NULL, NULL, NULL, 'PAGADO',    NULL, NULL, 2),
(3, 3, TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '20:30:00'), 'EN_MESA',     4,    NULL, NULL, 'CANCELADO',
     'El cliente se retiró antes de ordenar', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '20:35:00'), 2),
(4, 4, NOW() - INTERVAL 90 MINUTE,                        'EN_MESA',     2,    NULL, NULL, 'PAGADO',    NULL, NULL, 2),
(5, 5, NOW() - INTERVAL 60 MINUTE,                        'EN_MESA',     3,    NULL, NULL, 'ACTIVO',    NULL, NULL, 2),
(6, 6, NOW() - INTERVAL 40 MINUTE,                        'EN_MESA',     7,    NULL, NULL, 'ACTIVO',    NULL, NULL, 2),
(7, 7, NOW() - INTERVAL 15 MINUTE,                        'PARA_LLEVAR', NULL, NULL, NULL, 'ACTIVO',    NULL, NULL, 2);

-- Líneas de cada pedido. precio_unitario = precio del platillo + extras al momento de la venta.
INSERT INTO detalles_pedido (id, cantidad, precio_unitario, nota, id_pedido, id_platillo) VALUES
-- P-0001: total $180.00
(1,  2,  65.00, NULL,           1, 1),
(2,  2,  25.00, NULL,           1, 8),
-- P-0002: total $220.00 (tacos con extra queso: 55 + 10)
(3,  3,  65.00, NULL,           2, 4),
(4,  1,  25.00, NULL,           2, 9),
-- P-0003 (cancelado): $60.00
(5,  2,  30.00, NULL,           3, 5),
-- P-0004: total $170.00
(6,  1,  65.00, 'Sin cebolla',  4, 2),
(7,  3,  35.00, NULL,           4, 3),
-- P-0005: total $360.00
(8,  4,  65.00, NULL,           5, 1),
(9,  4,  25.00, NULL,           5, 8),
-- P-0006: total $290.00 (tostadas con aguacate extra: 35 + 10)
(10, 1,  65.00, 'Sin cebolla',  6, 2),
(11, 3,  45.00, NULL,           6, 3),
(12, 2,  25.00, NULL,           6, 8),
(13, 1,  40.00, NULL,           6, 6),
-- P-0007: total $230.00 (enchiladas con extra queso: 85 + 15)
(14, 2, 100.00, 'Salsa aparte', 7, 7),
(15, 1,  30.00, NULL,           7, 10);

-- Extras elegidos en cada línea
INSERT INTO detalle_pedido_variaciones (id_detalle_pedido, id_variacion) VALUES
(3, 2),
(11, 1),
(14, 3);

-- ---------------------------------------------------------------------
-- Pagos y qué cubrió cada uno (partidas)
-- ---------------------------------------------------------------------
INSERT INTO pagos (id, numero, fecha_hora, metodo, monto, monto_tarjeta, efectivo_recibido,
                   referencia_terminal, id_pedido, id_usuario) VALUES
-- P-0001: efectivo, recibió $200 → cambio $20
(1, 1, TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '19:55:00'), 'EFECTIVO', 180.00,   0.00, 200.00, NULL,              1, 2),
-- P-0002: tarjeta, sin cambio
(2, 1, TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '20:10:00'), 'TARJETA',  220.00, 220.00,   0.00, 'Aprobada · 4471', 2, 2),
-- P-0004: mixto, $100 tarjeta + $100 efectivo → aplica $70 efectivo, cambio $30
(3, 1, NOW() - INTERVAL 30 MINUTE,                        'MIXTO',    170.00, 100.00, 100.00, NULL,              4, 2),
-- P-0005: cuenta separada, primer pago por 2 pozoles; recibió $200 → cambio $70
(4, 1, NOW() - INTERVAL 20 MINUTE,                        'EFECTIVO', 130.00,   0.00, 200.00, NULL,              5, 2);

INSERT INTO partidas_pago (id, cantidad, importe, id_pago, id_detalle_pedido) VALUES
(1, 2, 130.00, 1, 1),
(2, 2,  50.00, 1, 2),
(3, 3, 195.00, 2, 3),
(4, 1,  25.00, 2, 4),
(5, 1,  65.00, 3, 6),
(6, 3, 105.00, 3, 7),
(7, 2, 130.00, 4, 8);

COMMIT;

-- Comprobación rápida: total, pagado y saldo de cada pedido
SELECT p.folio,
       p.estado,
       (SELECT SUM(d.cantidad * d.precio_unitario) FROM detalles_pedido d WHERE d.id_pedido = p.id) AS total,
       COALESCE((SELECT SUM(g.monto) FROM pagos g WHERE g.id_pedido = p.id), 0)                   AS pagado
FROM pedidos p
ORDER BY p.folio;
