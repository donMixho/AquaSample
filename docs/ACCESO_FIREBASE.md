# Acceso y demostración

## Probar sin crear cuentas

La compilación debug muestra el botón de demostración. Permite elegir Operador o Supervisor y utiliza `aquasample_demo.db`. El encabezado muestra DEMOSTRACIÓN. Esa selección nunca concede permisos a una cuenta Firebase.

La compilación release no muestra ni permite ese acceso. Las contraseñas no se guardan en SavedStateHandle.

## Cuentas Firebase

Se conserva la configuración `aldemar/app/google-services.json` del equipo. El administrador del proyecto debe comprobar en Firebase Console que Authentication tenga habilitado el proveedor Email/Password. Tener el archivo de configuración no demuestra que el servicio esté habilitado ni que una cuenta funcione.

Al registrar una cuenta se guarda el nombre de perfil y, por defecto, la aplicación le asigna acceso de Operador. El rol de Supervisor requiere un token emitido por Firebase con el atributo personalizado:

```json
{"role": "SUPERVISOR"}
```

Ese atributo debe asignarlo un administrador desde un entorno de confianza mediante Firebase Admin SDK. No se agrega una clave de administrador a Android ni al repositorio. Deben conservarse los demás atributos que ya tenga la cuenta al modificarlos.

Después de asignar o cambiar un rol, cerrar sesión y volver a ingresar: el acceso solicita un token actualizado. Al restaurar una sesión, Firebase puede utilizar un token vigente en caché. Por ello la app local no garantiza revocación inmediata de permisos mientras esté desconectada; una futura API debe validar los permisos para cada operación protegida.

Si el token no tiene exactamente `role: SUPERVISOR`, se utiliza Operador. El nombre escrito en la pantalla de acceso no reemplaza el nombre del perfil de una cuenta existente.

## Comprobación manual

1. Registrar una cuenta autorizada para pruebas y comprobar el acceso como Operador.
2. Cerrar sesión y entrar con las mismas credenciales.
3. Probar correo incorrecto, contraseña incorrecta y falta de conexión.
4. Con el administrador, asignar Supervisor a una cuenta de prueba; cerrar sesión y volver a entrar.
5. Comprobar que solo esa cuenta puede emitir dictámenes y que Observar exige un motivo.
6. Probar la demostración y comprobar que sus muestras no aparecen en el historial real.

El registro de cuentas se mantiene habilitado como en el aporte original. Para uso empresarial debe restringirse a usuarios autorizados de ALDEMAR.

Referencias: [atributos y permisos](https://firebase.google.com/docs/auth/admin/custom-claims), [acceso por correo](https://firebase.google.com/docs/auth/android/password-auth).
