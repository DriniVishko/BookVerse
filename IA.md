# Uso de inteligencia artificial — BookVerse

## 1. Herramienta utilizada

Durante el desarrollo de BookVerse se ha utilizado **ChatGPT (OpenAI)** como herramienta de apoyo para consultar aspectos técnicos de JDBC, contrastar posibles implementaciones y agilizar determinadas tareas de programación y documentación.

El proyecto se ha desarrollado y probado en IntelliJ IDEA, utilizando Java, Maven, MySQL y H2.

## 2. Ámbitos de utilización

La herramienta se ha empleado principalmente en los siguientes aspectos:

- **Conectividad JDBC:** configuración de distintos motores de base de datos y mecanismos de conexión.
- **Persistencia y transacciones:** implementación y revisión de operaciones sobre pedidos, inventario y pagos.
- **Consultas SQL:** elaboración de consultas con relaciones entre tablas, agrupaciones y paginación.
- **Pruebas:** preparación de casos de prueba para comprobar la integridad de los datos y el comportamiento de las transacciones.
- **Documentación:** organización de la información técnica y descripción de las funcionalidades implementadas.

## 3. Ejemplos de consultas realizadas

Se recogen a continuación algunos mensajes literales de la conversación utilizados durante el desarrollo:

1. "que es hikari y h2?"
2. "tengo que tener otra aplicacion a parte de workbench?"
3. "Explciame la relación"
4. "Puedo hacerlo de manera diferente?"

Estas consultas forman parte del proceso de trabajo y están relacionadas con la configuración del entorno, la organización del proyecto y la planificación de las tareas.

## 4. Comprobación de resultados

Las implementaciones se verificaron mediante ejecuciones de la aplicación y pruebas automatizadas.

Entre las comprobaciones realizadas destacan:

- Funcionamiento con MySQL y H2.
- Conexiones mediante DriverManager y HikariCP.
- Creación de pedidos con varias líneas y actualización del inventario.
- Cancelación de pedidos y recuperación de existencias.
- Ejecución de transacciones con rollback ante errores simulados.
- Ejecución de seis pruebas automatizadas con JUnit 5.
- Comprobación de procedimientos almacenados y restricciones de integridad SQL.

## 5. Valoración

El uso de ChatGPT permitió consultar alternativas de implementación, resolver incidencias concretas y agilizar el desarrollo de determinadas partes del proyecto.

Las respuestas se contrastaron con el comportamiento real de la aplicación, especialmente en las operaciones que afectan a la integridad de los datos.

La experiencia también puso de manifiesto la importancia de revisar las soluciones propuestas, comprobar su compatibilidad con el entorno y validar su funcionamiento mediante pruebas.