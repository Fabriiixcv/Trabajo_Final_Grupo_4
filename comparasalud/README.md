# ComparaSalud - API REST

Spring Boot 4.0.8 · Java 21 · Maven · PostgreSQL · Lombok · ModelMapper · Swagger · Spring Security + JWT

## Como ejecutar en IntelliJ
1. Crear la base de datos **vacia** en PostgreSQL llamada `base_datos` (o cambiar el nombre en `application.properties`).
   > Si ya tenias una base de la version anterior, borrala y crea otra vacia: `farmacia` y `administrador` cambiaron de columnas.
2. Ajustar `spring.datasource.username` y `spring.datasource.password` si es necesario.
3. `File > Open` y elegir la carpeta `comparasalud` (IntelliJ detecta el `pom.xml`; esperar la descarga de dependencias).
4. Ejecutar `ComparasaludApplication`. Las tablas se crean solas (`ddl-auto=update`) y todos los ids se autogeneran.
5. Swagger: http://localhost:8088/swagger-ui/index.html

> Si IntelliJ no compila Lombok: Settings > Build > Compiler > Annotation Processors > *Enable annotation processing*.

## Administrador
Es un usuario fijo: no se registra por la API. Al arrancar, si la tabla esta vacia, se crea (`config/DatosIniciales`):
`admin@comparasalud.com` / `admin123` (se guarda con BCrypt).

## Orden sugerido para probar
Administrador (login) -> Categoria -> Farmacia (registro) -> Medicamento -> Oferta -> Cliente -> Favoritos -> Calificacion -> Totales del administrador.

## Seguridad (Spring Security + JWT)
Igual que `backend_proveedores`, pero con tres roles en tablas separadas (`cliente`, `farmacia`, `administrador`). **Las rutas y los cuerpos no cambiaron**: los tres login siguen siendo `POST /api/clientes/login`, `/api/farmacias/login` y `/api/administradores/login`.
- El login devuelve lo mismo de antes mas `token`: `{"rol","idCliente|idFarmacia|idAdministrador","nombre","token"}`. El frontend lo envia en cada peticion como `Authorization: Bearer <token>` (dura 60 min, `jwt.expiracion-minutos`).
- Paquete `security`: `SecurityConfig` (reglas por rol y CORS), `JwtUtil`, `JwtRequestFilter`, `CustomUserDetailsService`, `UsuarioSesion`, `SesionActual`.
- En cada peticion se vuelve a cargar la cuenta desde la base de datos: un cliente eliminado o una farmacia dada de baja (HU53) deja de funcionar aunque su token no haya vencido.
- Configuracion en `application.properties`: `jwt.secret` (Base64, minimo 64 bytes; en produccion usa la variable de entorno `JWT_SECRET`) e `ip.frontend` (origenes permitidos, separados por coma: Live Server 5500 y Angular 4200).
- Swagger (http://localhost:8088/swagger-ui/index.html) tiene el boton **Authorize** (candado): haz login, copia el `token` de la respuesta y pegalo ahi (sin escribir `Bearer`).
- Contrasenas con BCrypt. Las cuentas antiguas en texto plano siguen entrando y se convierten a BCrypt en su primer login. Nunca se devuelven en las respuestas.

| Quien | Puede |
|---|---|
| Publico | Los 3 login, `POST /api/clientes` (HU01), `POST /api/farmacias` (HU09), Swagger |
| Cliente | Solo `/api/clientes/{id}/**` con **su** id (cuenta y favoritos), calificar farmacias |
| Farmacia | `PUT /api/farmacias/{id}`, `.../perfil-comercial`, `GET /api/farmacias/{id}/ofertas` con **su** id; crear medicamentos; crear, editar y eliminar **sus** ofertas; editar los medicamentos que registro |
| Administrador | Categorias (alta, edicion, baja), `DELETE /api/farmacias/{id}`, `DELETE /api/medicamentos/{id}`, todo `/api/administradores/**` |
| Cualquier usuario con sesion | Consultas: farmacias, medicamentos, ofertas, categorias, calificaciones |

- **401** `No autenticado` (sin token, vencido o alterado). **403** `No tiene permiso para esta operacion` (rol incorrecto o datos de otra cuenta). Mismo formato de error que el resto: `fecha`, `estado`, `mensaje`.
- Los ids del cuerpo (`idFarmacia` en HU22/HU42, `idCliente` en HU47) deben ser los de la sesion; si no, 403. Se comprueba en los servicios (`SesionActual`), sin cambiar los DTO.
- **Pendiente:** logout (HU07, HU54, HU55). Con JWT sin estado el cierre de sesion lo hace el frontend borrando el token; no se agrego un endpoint nuevo.

## Ofertas y comparacion de medicamentos
- **Oferta** = lo que publica una farmacia sobre un medicamento del catalogo: precio y stock (`/api/ofertas`). Los clientes ven las ofertas de las distintas farmacias con `GET /api/ofertas` (HU58, solo farmacias activas y con stock; filtros opcionales `farmaciaId` y `distrito`).
- **Comparacion** = el cliente escribe el nombre de un medicamento y recibe la lista de medicamentos que coinciden, cada uno con las farmacias que lo ofrecen, su distrito, precio y stock, de la mas barata a la mas cara: `GET /api/medicamentos/buscar?nombre=paracetamol` (HU35). `GET /api/medicamentos/comparacion?ids=1,2` (HU26) compara varios medicamentos ya elegidos.
- Los precios de la comparacion salen de las ofertas que registran las farmacias. No hay un listado general del catalogo: `GET /api/medicamentos` (HU21, HU24, HU25) se retiro porque la consulta se hace por nombre (HU35). La ruta `/api/medicamentos` solo sirve para `POST` (HU22).

## Reglas generales
- Farmacia activa = `ACTIVA`. La baja (HU17) es logica: pasa a `INACTIVA`, desaparece de consultas, comparaciones, favoritos y totales, y no puede iniciar sesion.
- Codigos: 200 en consultas, altas, actualizaciones y bajas (las bajas sin cuerpo); 201 y 204 solo en favoritos; 400 validacion o duplicados; 401 credenciales o sin sesion; 403 sin permiso por rol, farmacia inactiva o datos de otra cuenta; 404 id inexistente; 409 favorito repetido.
- Los errores devuelven `mensaje` y, si aplica, la lista `errores`.
- Stock: en HU42 debe ser > 0; en HU29 y HU45 puede quedar en 0 (oferta agotada). Precio siempre > 0 (HU33) y con dos decimales.
- Ruta compartida, un solo metodo de controlador: `GET /api/farmacias` (HU12, HU13, HU14).
- HU45 esta como `PUT` (igual que la lista); para `PATCH`, cambiar `@PutMapping` por `@PatchMapping` en `OfertaController`.

Los 5 totales del administrador (HU34, HU36, HU37, HU46, HU50) devuelven `{"total": N, "detalle": [ ...registros... ]}`.

## Endpoints por HU (55 - 3 retiradas + 3 nuevas: HU56, HU57 y HU58)
| HU | Rol | Endpoint |
|----|-----|----------|
| HU01 | Usuario | POST /api/clientes |
| HU02 | Cliente | POST /api/clientes/login |
| HU03 | Cliente | POST /api/clientes/{id}/medicamentos-favoritos |
| HU04 | Cliente | GET /api/clientes/{id} |
| HU05 | Cliente | DELETE /api/clientes/{id} |
| HU06 | Sistema | filtro de acceso por rol (`SecurityConfig`) |
| HU07 | Cliente | (logout: lo hace el frontend borrando el token) |
| HU08 | Sistema | validaciones dentro de POST /api/clientes y POST /api/farmacias |
| HU09 | Farmacia | POST /api/farmacias |
| HU10 | Cliente | GET /api/farmacias/{id} |
| HU11 | Cliente | GET /api/farmacias/{id}/ubicacion |
| HU12 | Cliente | GET /api/farmacias?distrito= |
| HU13 | Cliente | GET /api/farmacias?nombre= |
| HU14 | Cliente | GET /api/farmacias?estado=activa |
| HU15 | Farmacia | PUT /api/farmacias/{id} |
| HU16 | Farmacia | PUT /api/farmacias/{id}/perfil-comercial |
| HU17 | Administrador | DELETE /api/farmacias/{id} |
| HU18 | Cliente | DELETE /api/clientes/{id}/medicamentos-favoritos/{medicamentoId} |
| HU19 | Cliente | GET /api/clientes/{id}/medicamentos-favoritos |
| HU20 | Cliente | GET /api/clientes/{id}/farmacias-favoritas |
| HU21 | Cliente | (retirada: no hay listado general; se usa HU35) |
| HU22 | Farmacia | POST /api/medicamentos |
| HU23 | Cliente | GET /api/medicamentos/{id} |
| HU24 | Cliente | (retirada) |
| HU25 | Cliente | (retirada) |
| HU26 | Cliente | GET /api/medicamentos/comparacion?ids=1,2 |
| HU27 | Cliente | GET /api/farmacias/{id}/calificaciones |
| HU28 | Cliente | POST /api/clientes/{id}/farmacias-favoritas |
| HU29 | Farmacia | PUT /api/ofertas/{id} |
| HU30 | Farmacia | PUT /api/medicamentos/{id} |
| HU31 | Farmacia | DELETE /api/ofertas/{id} |
| HU32 | Administrador | DELETE /api/medicamentos/{id} |
| HU33 | Sistema | validacion de precio dentro de HU42, HU29 y HU45 |
| HU34 | Administrador | GET /api/administradores/farmacias |
| HU35 | Cliente | GET /api/medicamentos/buscar?nombre= (comparar precios por farmacia) |
| HU36 | Administrador | GET /api/administradores/ofertas |
| HU37 | Administrador | GET /api/administradores/clientes |
| HU38 | Administrador | POST /api/categorias |
| HU39 | Cliente | GET /api/categorias |
| HU40 | Administrador | PUT /api/categorias/{id} |
| HU41 | Administrador | DELETE /api/categorias/{id} |
| HU42 | Farmacia | POST /api/ofertas |
| HU43 | Farmacia | GET /api/farmacias/{id}/ofertas |
| HU44 | Cliente | PUT /api/clientes/{id}/nombre |
| HU45 | Farmacia | PUT /api/ofertas/{id}/precio-stock |
| HU46 | Administrador | GET /api/administradores/categorias |
| HU47 | Cliente | POST /api/farmacias/{id}/calificaciones |
| HU48 | Cliente | GET /api/medicamentos/{id}/reporte-precios |
| HU49 | Cliente | GET /api/medicamentos/{id}/recomendaciones |
| HU50 | Administrador | GET /api/administradores/medicamentos |
| HU51 | Cliente | DELETE /api/clientes/{id}/farmacias-favoritas/{farmaciaId} |
| HU52 | Administrador | POST /api/administradores/login |
| HU53 | Farmacia | POST /api/farmacias/login |
| HU54 | Administrador | (logout: lo hace el frontend borrando el token) |
| HU55 | Farmacia | (logout: lo hace el frontend borrando el token) |
| HU56 (nueva) | Administrador | DELETE /api/administradores/clientes/{id} (borrado fisico, igual que HU05) |
| HU57 (nueva) | Administrador | DELETE /api/administradores/farmacias/{id} (baja logica, igual que HU17) |
| HU58 (nueva) | Cliente | GET /api/ofertas?farmaciaId=&distrito= |

## Ejemplos de body
```json
POST /api/clientes        {"nombre":"Ana","apellidos":"Perez","correo":"ana@mail.com","contrasena":"123456"}
POST /api/clientes/login  {"correo":"ana@mail.com","contrasena":"123456"}
PUT  /api/clientes/1/nombre {"nombre":"Ana Maria","apellidos":"Perez Lopez"}
POST /api/farmacias       {"nombre":"Farmacia Central","licencia":"LIC-001","correo":"central@mail.com","contrasena":"123456","direccion":"Av. Lima 123","distrito":"Miraflores","telefono":"999111222","horario":"24 horas"}
POST /api/farmacias/login {"correo":"central@mail.com","contrasena":"123456"}
PUT  /api/farmacias/1     {"nombre":"Farmacia Central","licencia":"LIC-001","direccion":"Av. Lima 123","distrito":"Miraflores"}
PUT  /api/farmacias/1/perfil-comercial {"telefono":"987654321","horario":"8am - 10pm"}
POST /api/administradores/login {"correo":"admin@comparasalud.com","contrasena":"admin123"}
POST /api/categorias      {"nombre":"Analgesicos","descripcion":"Para el dolor"}
POST /api/medicamentos    {"idFarmacia":1,"nombre":"Paracetamol","nombreComercial":"Panadol","principioActivo":"Paracetamol","concentracion":"500 mg","presentacion":"Tableta","laboratorio":"GSK","idCategoria":1}
POST /api/ofertas         {"idMedicamento":1,"idFarmacia":1,"precio":4.90,"stock":30}
PUT  /api/ofertas/1       {"precio":5.20,"stock":0}
PUT  /api/ofertas/1/precio-stock {"precio":4.90}
POST /api/clientes/1/medicamentos-favoritos {"idMedicamento":1}
POST /api/clientes/1/farmacias-favoritas    {"idFarmacia":1}
POST /api/farmacias/1/calificaciones        {"idCliente":1,"puntuacion":5,"comentario":"Excelente atencion"}
GET  /api/ofertas?distrito=Miraflores
GET  /api/medicamentos/comparacion?ids=1,2
GET  /api/medicamentos/buscar?nombre=paracetamol
```
