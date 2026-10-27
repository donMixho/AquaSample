# Guía para presentar AquaSample

La EP3 es una defensa individual de hasta 15 minutos sobre la versión entregada en la EP2. La pauta no exige diapositivas y no permite apoyo de IA durante la defensa. Practicar antes con Android Studio y el teléfono.

## Antes de presentar

- Abrir `aldemar` en Android Studio, sincronizar y ejecutar antes de empezar.
- Llevar teléfono cargado, cable de datos y depuración USB autorizada.
- Preparar una imagen de prueba para la galería y comprobar la cámara.
- Tener abiertos los archivos de validación, ViewModel, DAO y navegación.
- Usar la misma versión entregada. La pauta indica no modificar el repositorio entre el cierre de entrega y la evaluación, salvo instrucción del docente.
- Tener disponibles el repositorio, los commits propios y el tablero real del equipo. Esta integración local todavía debe revisarse y publicarse por el equipo.
- Para mostrar Firebase, probar previamente una cuenta real. El modo de demostración sirve para recorrer las muestras sin Internet, pero se debe identificar como demostración.

## Recorrido de 15 minutos

| Tiempo | Acción | Explicación |
|---|---|---|
| 0–2 min | Ejecutar y presentar el caso | AquaSample registra muestras de cultivo de choritos para ALDEMAR: ubicación, evidencia, conteo y revisión. Explicar el requerimiento que se mostrará. |
| 2–5 min | Crear una muestra y provocar errores | Dejar tren vacío, escribir longitud 0 y corregir a 1,5. Mostrar mensajes, navegación y resumen. Explicar validaciones. |
| 5–7 min | Buscar y filtrar | Buscar una muestra por código y combinar filtros. Explicar cómo StateFlow comunica el estado a Compose. |
| 7–10 min | Guardar, consultar y recuperar | Guardar una muestra, cerrar y reabrir la app. Mostrarla en Room y explicar Entity, DAO, Database y Repository. Vincular un avance con un commit real. |
| 10–12 min | Ejecutar cámara y galería | Tomar y confirmar una foto, y mostrar una imagen seleccionada desde la galería. Explicar almacenamiento privado y cancelación. Se pueden mostrar durante el registro y usar este tramo para explicar el código. |
| 12–15 min | Cambio solicitado por el profesor | Localizar el componente o regla, modificarla, ejecutar y demostrar el resultado. |

Si hace falta mostrar el dictamen, guardar la muestra como Operador en demostración, cerrar sesión y entrar como Supervisor de demostración. Observar sin motivo debe producir un error; escribir el motivo y guardar. Ambos perfiles de demostración usan la misma base de prueba. No presentar esta selección como autorización de una cuenta real.

## Archivos que conviene conocer

Las rutas siguientes son relativas a `aldemar/app/src/main/java/com/aldemar/aquasample`.

| Tema | Archivo |
|---|---|
| Reglas de muestras | `ui/viewmodel/SampleValidation.kt` |
| Estado, foto y guardado | `ui/viewmodel/SampleCreateViewModel.kt` |
| Formulario | `ui/screens/sample/NewSampleScreen.kt` |
| Conteo | `ui/screens/sample/CountObservationsScreen.kt` |
| Búsqueda y filtros | `ui/viewmodel/SampleListViewModel.kt`, `SampleFilter.kt` |
| Navegación | `ui/navigation/AquaNavigation.kt` |
| Tabla y consultas | `data/local/entity/SampleEntity.kt`, `data/local/dao/SampleDao.kt` |
| Base y repositorio | `data/local/AquaSampleDatabase.kt`, `data/repository/SampleRepository.kt` |
| Imágenes | `ui/screens/sample/PhotoCaptureScreen.kt`, `data/repository/PhotoStorage.kt` |
| Acceso y roles | `ui/viewmodel/AuthViewModel.kt`, `data/repository/AuthRepository.kt` |

## Mostrar Room

Con la app ejecutándose en el teléfono, abrir App Inspection → Database Inspector y seleccionar el proceso `com.aldemar.aquasample`.

- En demostración: consultar `aquasample_demo.db`.
- Con acceso real: consultar `aquasample_aldemar.db`.

Ejemplo de consulta:

```sql
SELECT code, centerName, sectionLengthMeters, musselCount, status
FROM samples
ORDER BY createdAt DESC
LIMIT 10;
```

Explicar que Room usa SQLite en Android. Los scripts de `AquaSample_DB` son para MySQL y todavía no reciben los registros de la aplicación.

## Cambios para practicar antes

1. Ajustar el máximo de conteo en `SampleValidation` y actualizar también el mensaje y su prueba.
2. Agregar una regla a la longitud y comprobar un valor permitido y otro rechazado.
3. Incluir las observaciones en la búsqueda de `SampleFilter` y comprobar una coincidencia.

Estos ejercicios son preparación. El docente puede pedir otro cambio. No agregar una columna Room improvisadamente: requiere pensar la entidad, el esquema y la migración.

## Explicaciones breves

- Compose dibuja la pantalla según el estado; las acciones del usuario llegan al ViewModel.
- El ViewModel aplica reglas y solicita operaciones al repositorio; el DAO define cómo se leen o escriben los datos con Room.
- StateFlow permite observar cambios; no reemplaza la base de datos.
- SavedStateHandle ayuda a recuperar el borrador tras recreación de Android; Room conserva las muestras guardadas.
- Firebase verifica el acceso. El permiso de Supervisor viene del token administrado; la demo usa otra base.
- La captura usa la app de cámara mediante un contrato de Android. La galería entrega acceso a la imagen seleccionada, que se copia al almacenamiento privado.

## Evidencias y límites

Consultar VERIFICACION.md para los resultados de las pruebas. Las muestras y fotografías aún no se sincronizan entre teléfonos; el flujo de revisión es local. No afirmar que los scripts MySQL están integrados o que esta versión está certificada para producción.

Cada integrante debe explicar sus aportes reales y conocer el flujo completo. En la EP3, explicar/modificar validaciones y modificar un componente o función suman 42% de la pauta; practicar cambios es tan importante como recorrer pantallas.
