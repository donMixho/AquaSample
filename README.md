# AquaSample

AquaSample es una aplicación Android del proyecto ALDEMAR, desarrollada para la asignatura Desarrollo de Aplicaciones Móviles (DSY1105). Permite registrar muestras de cultivo de choritos, guardar fotografías y consultar el historial de muestreos en el teléfono.

## Integrantes

- Matías Mena
- Antoine Rivas
- Leandro Ruiz
- Franco Ruz

## Funcionalidades

- Registro e inicio de sesión con correo y contraseña mediante Firebase.
- Registro de muestras con centro, tren, línea, longitud, conteo y observaciones.
- Fotografía con cámara o selección desde la galería.
- Validaciones con mensajes antes de avanzar y guardar.
- Historial con búsqueda, filtros y detalle de cada muestra.
- Revisión de muestras por un supervisor; observar una muestra exige indicar el motivo.
- Persistencia local con Room y recuperación del formulario ante recreación de la pantalla.

## Cómo ejecutar

1. Abre la carpeta `aldemar` en Android Studio.
2. Usa el JDK integrado compatible con Gradle (se comprobó con JDK 21) y el SDK Android 35.
3. Sincroniza Gradle. La primera sincronización necesita Internet.
4. Conecta un teléfono con Android 7.0 o superior, activa Depuración USB y acepta la autorización. También puedes usar un emulador ya instalado.
5. Selecciona el dispositivo y ejecuta el módulo `app` con Run.

La ruta del SDK se configura localmente en `local.properties` y no se incluye en el repositorio.

## Acceso

El acceso real usa Firebase y requiere su configuración activa. Las cuentas nuevas ingresan como Operador; el administrador asigna el permiso de Supervisor. Los detalles están en [Acceso Firebase](docs/ACCESO_FIREBASE.md).

La versión debug también permite probar sin cuentas mediante **Ingresar en Modo Demostración (Offline)**. En este modo se puede elegir un perfil y se trabaja con una base de datos separada, con centros y muestras de ejemplo. El encabezado identifica la demostración. Este acceso no está disponible en release.

## Uso básico

Como operador, crea una muestra, completa los datos, agrega una foto y registra el conteo. Revisa el resumen y guarda. Busca el registro en el historial para abrir su detalle.

Como supervisor, abre una muestra y elige Validar u Observar. Si la observas, escribe el motivo. La revisión se realiza en el mismo dispositivo donde se guardó la muestra.

## Tecnologías y alcance

Kotlin, Jetpack Compose, Material 3, Navigation, ViewModel, StateFlow, Room y Firebase Authentication, con organización MVVM.

Las muestras y fotos se guardan localmente; todavía no se sincronizan entre teléfonos. La carpeta `AquaSample_DB` contiene los scripts MySQL preparados por el equipo, pero esa base aún no está conectada a la app. El alcance de seguridad y las tareas pendientes se detallan en [Integración](docs/INTEGRACION.md).

## Documentación

- [Estructura del código](docs/ESTRUCTURA.md)
- [Resultados de verificación](VERIFICACION.md)
- [Guía de presentación y defensa](docs/GUIA_PRESENTACION.md)
- [Pruebas manuales](docs/PRUEBAS_MANUALES.md)
- [Cambios de integración](docs/INTEGRACION.md)
- [Diseños y evidencias originales](docs)
