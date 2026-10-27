# Verificación de la versión integrada

Entrega preparada el 27 de octubre de 2026, basada en `main` del equipo, commit `925db20ecf26d807ae02d163110a26187401f9d3`. Versión de la app: 1.2 (versionCode 3).

| Comprobación | Resultado |
|---|---|
| Compilación debug | Correcta; APK firmado para pruebas |
| Compilación release | Correcta; APK sin firma de distribución |
| Pruebas JVM | 21 aprobadas, 0 fallos |
| Pruebas Android | 9 aprobadas en Samsung Galaxy A35, Android 14 |
| Android Lint | 0 errores, 37 advertencias |
| Firma del APK debug | Verificada con apksigner, esquema v2 |
| Prueba manual | El usuario informa que probó la app y funciona bien; sin desglose de cada caso |
| Acceso Firebase con cuenta real | Pendiente de comprobación con el administrador/equipo |

## Qué se probó

- Validaciones de muestras y combinación de búsquedas/filtros.
- Credenciales inválidas, reintentos, doble pulsación, recuperación y cierre de sesión, roles de proveedor y bloqueo de demostración en configuración de producción. Estas pruebas usan un proveedor simulado; no crean cuentas en Firebase.
- Persistencia de muestra y dictamen, precarga sin duplicados y reintentos concurrentes de guardado.
- Archivos de imagen válidos e inválidos, recuperación del borrador y rechazo del dictamen de un operador.
- Flujo visual: entrar en demostración, completar datos, recrear la Activity conservando campos, agregar imagen de prueba, guardar, buscar y abrir detalle sin acciones de supervisor.
- Separación de los registros de demostración respecto de la base real.

El primer intento de pruebas por Gradle se detuvo porque el teléfono quedó temporalmente sin autorización USB. Tras recuperarse la conexión, se ejecutó directamente AndroidJUnitRunner por ADB y terminaron los 9 casos correctamente. El registro está en [android-samsung-a35.log](docs/verificacion/android-samsung-a35.log).

La prueba visual automatizada incorpora una imagen de prueba mediante el ViewModel; no opera las aplicaciones externas de cámara/galería. Se solicitó al usuario probar ambas y respondió que la app va muy bien, sin detallar resultados separados. Para una evidencia formal, completar [la lista manual](docs/PRUEBAS_MANUALES.md).

## Comandos

Desde `aldemar`, con JDK 21 y SDK Android 35:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug assembleRelease --max-workers=2
.\gradlew.bat connectedDebugAndroidTest --max-workers=2
```

La ejecución alternativa utilizada en el teléfono, con ambos APK de prueba instalados, fue:

```text
adb shell am instrument -w -r com.aldemar.aquasample.test/androidx.test.runner.AndroidJUnitRunner
```

Las advertencias de Lint corresponden a versiones de dependencias/SDK, recursos sin uso, ubicación de iconos y sugerencias de estilo. No se ocultaron errores con una línea base.

## Alcance

Se conservaron los scripts MySQL y la configuración Firebase del equipo. No se publicaron cambios en GitHub ni se asignaron permisos a cuentas remotas. El workflow de CI está preparado, pero no ejecutado en GitHub durante esta integración.

La base Room conserva versión 1. Los datos existentes no se borran. Las bases real y de demostración están separadas. Esta versión todavía no sincroniza muestras ni implementa autorización de servidor o auditoría empresarial completa; ver [Integración](docs/INTEGRACION.md).

SHA-256 del APK debug: `aaea5aa80e8c1508ef8110c0711bdd3693301c3192b4f1f79cffcd622f45637e`.

Resultados detallados: [JSON](docs/verificacion/resultados.json) y XML de pruebas en [docs/verificacion](docs/verificacion).
