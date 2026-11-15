
# BookVerse

Aplicación de consola desarrollada en Java para gestionar una librería mediante JDBC.

Proyecto de la práctica UD2: Manejo de conectores JDBC.

## Tecnologías

- Java 17
- Maven
- JDBC
- MySQL 8
- H2
- HikariCP
- Log4j 2
- JUnit 5

## Funcionalidades

- Consulta del catálogo de libros y autores.
- Catálogo paginado mediante SQL.
- Gestión de inventario y reposición de stock.
- Registro de movimientos de entrada, salida y devolución.
- Pedidos con varios libros.
- Simulación de pagos.
- Cancelación de pedidos con recuperación del stock.
- Estadísticas de ventas mediante JOIN y GROUP BY.
- Procedimiento almacenado MySQL con parámetros IN y OUT.
- Demostración de errores SQL controlados.
- Transacciones JDBC con commit y rollback.
- Simulación de fallos para comprobar la atomicidad.

## Modelo de datos

El proyecto utiliza nueve tablas:

1. libro
2. autor
3. libro_autor
4. cliente
5. inventario
6. movimiento_stock
7. pedido
8. detalle_pedido
9. pago

Relaciones principales:

- Libro y autor: muchos a muchos.
- Libro e inventario: uno a uno.
- Cliente y pedido: uno a muchos.
- Pedido y detalle_pedido: uno a muchos.
- Pedido y pago: uno a uno.
- Libro y movimiento_stock: uno a muchos.

## Configuración

El archivo `src/main/resources/application.properties` permite seleccionar el motor de base de datos y el sistema de conexiones.

Ejemplo:

```properties
database.engine=MYSQL
connection.mode=HIKARI
simulation.mode=NORMAL

mysql.url=jdbc:mysql://localhost:3306/unidad2
mysql.user=root

h2.url=jdbc:h2:mem:bookverse;MODE=MySQL;DB_CLOSE_DELAY=-1
h2.user=sa

hikari.maximumPoolSize=5
```

La contraseña MySQL se obtiene de la variable de entorno:

`BOOKVERSE_DB_PASSWORD`

No se almacena la contraseña en el repositorio.

## Combinaciones JDBC

Se han probado las cuatro configuraciones:

| Motor | Conexión |
|---|---|
| MySQL | DriverManager |
| MySQL | HikariCP |
| H2 | DriverManager |
| H2 | HikariCP |

## Ejecución

1. Tener instalado JDK 17 o superior y Maven.
2. Para usar MySQL, iniciar el servidor y disponer de la base de datos `unidad2`.
3. Crear `src/main/resources/application.properties` a partir del archivo de ejemplo.
4. Configurar `BOOKVERSE_DB_PASSWORD` si se utiliza MySQL.
5. Ejecutar `com.bookverse.Main` desde IntelliJ.

**Nota:** la versión de demostración reinicia las tablas y carga los datos iniciales cada vez que se ejecuta.

## Menú de consola

1. Ver catálogo.
2. Ver catálogo paginado.
3. Consultar estadísticas.
4. Consultar libros bajo mínimo.
5. Reponer stock.
6. Realizar pedido.
7. Cancelar pedido.
8. Ejecutar procedimiento almacenado MySQL.
9. Demostrar rollback.
10. Provocar error SQL controlado.
0. Salir.

## Pruebas

El proyecto incluye seis pruebas automatizadas con JUnit 5 sobre H2:

- Creación de pedidos.
- Descuento de stock.
- Recuperación de stock al cancelar.
- Rechazo de compras sin stock suficiente.
- Rollback tras un fallo simulado.
- Prevención de devoluciones duplicadas.

Para ejecutarlas:

```bash
mvn test
```

## Registros

Log4j 2 utiliza una configuración externa en:

`src/main/resources/log4j2.xml`

Los registros se generan en:

`logs/runtime/bookverse.log`

La configuración incluye rotación y compresión de archivos `.gz`.

## Estructura

- `database`: conexiones JDBC, DAO, inicialización y consultas.
- `model`: entidades y líneas de pedido.
- `service`: lógica de pedidos e inventario.
- `Main`: menú de consola.
- `src/test`: pruebas JUnit.
- `docs`: documentación y diagrama de relaciones.

## Autoría y uso de IA

El uso de inteligencia artificial se documenta en `IA.md`.
