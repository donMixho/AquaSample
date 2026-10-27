# Integración del proyecto

Base: rama `main` de donMixho/AquaSample, commit `925db20ecf26d807ae02d163110a26187401f9d3` (26 de octubre de 2026).
Trabajo local: rama `integracion/ep2-ep3`. No se ha publicado en GitHub.

## Trabajo conservado

- Acceso y registro con Firebase incorporados por Antoine en `935cf64`, integrado mediante el PR #2.
- Configuración del proyecto Firebase y dependencias Google Services, Auth y Analytics.
- SDK de compilación y destino 35.
- Scripts originales de `AquaSample_DB`, documentos de diseño y prototipos del equipo.
- Esquema Room versión 1: no se borran ni migran registros existentes.

## Mejoras integradas

- Campos obligatorios, longitud positiva con punto o coma, conteo entero y foto verificada antes de guardar.
- Reintento del mismo borrador sin duplicar la muestra y precarga transaccional sin duplicados.
- Conservación del formulario con SavedStateHandle y ViewModels gestionados por Android.
- Cámara delegada, importación desde galería y copia de imágenes al almacenamiento privado.
- Búsqueda, filtros, estados de carga/error y detalle de consulta para operadores.
- Dictamen reservado al supervisor y motivo obligatorio al observar una muestra.
- Autenticación separada en un repositorio para poder probar errores, recuperación y roles con un proveedor simulado.
- Rol real obtenido del atributo `role` del token Firebase. La selección de supervisor solo está disponible en la demostración.
- Demostración disponible únicamente en compilaciones debug, con base `aquasample_demo.db` separada.
- Se eliminó la descarga obligatoria de Amazon JDK 23; se utiliza el JDK compatible seleccionado en Android Studio.

## Límites actuales

La app guarda las muestras localmente. Firebase autentica cuentas, pero no sincroniza muestras ni fotografías. Los scripts MySQL no están conectados a Android.

El control de roles protege el flujo de la app; no sustituye la autorización de una futura API. Antes de producción hacen falta autorización en el servidor, asignación de cuentas a una organización, sincronización, auditoría persistente, identificadores de autor en las muestras y una política de protección/retención de datos.

Las muestras antiguas conservan sus nombres originales y no se vinculan retroactivamente con usuarios de Firebase. Si una instalación anterior ya tenía muestras de ejemplo en la base principal, se conservan para evitar borrar información del equipo. Las instalaciones nuevas generan ejemplos únicamente en demostración.

## Incorporación a GitHub

Trabajar sobre la rama de integración de esta copia Git conserva el historial del equipo. Antes de publicar, revisar `git diff`, ejecutar las comprobaciones indicadas en VERIFICACION.md y comparar nuevamente con `origin/main` por si el equipo agregó avances.

El ZIP contiene fuentes, no el historial `.git`. No reemplazar un repositorio más nuevo con el ZIP completo. Revisar especialmente AuthViewModel, LoginScreen, AquaNavigation, AndroidManifest y Gradle.
