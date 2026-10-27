# Estructura del proyecto

El proyecto Android está en `aldemar`. Para abrirlo y ejecutarlo, sigue las instrucciones del [README principal](../README.md).

El código de la aplicación está en `aldemar/app/src/main/java/com/aldemar/aquasample`:

| Carpeta | Contenido |
|---|---|
| `data/local/entity` | Entidades que representan las tablas de Room. |
| `data/local/dao` | Consultas y operaciones sobre los registros. |
| `data/local` | Configuración de la base de datos. |
| `data/repository` | Acceso a los datos y almacenamiento de imágenes. |
| `ui/viewmodel` | Estado de las pantallas y reglas de validación. |
| `ui/screens` | Pantallas construidas con Compose. |
| `ui/navigation` | Navegación entre pantallas. |

La interfaz recibe el estado de los ViewModels y les comunica las acciones del usuario. Los ViewModels usan los repositorios para consultar o guardar información con Room.

## Configuración

- Paquete: `com.aldemar.aquasample`.
- Android mínimo: API 24. SDK de compilación y destino: 35.
- Bytecode Java: 17.
- Versiones de dependencias: `aldemar/gradle/libs.versions.toml`.
- Esquema Room: versión 1, exportado en `aldemar/app/schemas`.

## Pruebas

Desde la carpeta `aldemar`, en PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

Para ejecutar las pruebas instrumentadas, conecta un dispositivo o inicia un emulador y ejecuta:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Los resultados obtenidos y las comprobaciones pendientes están en [VERIFICACION.md](../VERIFICACION.md).

El acceso se gestiona mediante AuthViewModel y AuthRepository. FirebaseAuthRepository resuelve credenciales y roles. La demostración utiliza otra instancia de Room y no otorga permisos a cuentas reales.
