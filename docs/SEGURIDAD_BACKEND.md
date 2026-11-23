# Acceso al backend

El backend exige un ID token Firebase en Authorization: Bearer. Verifica firma RS256, emisor del proyecto, audiencia, expiración e identidad. El proyecto predeterminado coincide con google-services.json; se puede configurar con aquasample.firebase-project.

El rol viene del token verificado: SUPERVISOR, ANALYST o, por defecto, OPERATOR. El encabezado X-User-Role ya no concede acceso. Un usuario anónimo recibe 401. Un operador no puede validar mediante /review ni introducir un dictamen mediante POST/PUT genérico. Un analista solo consulta.

Android solicita el ID token al usuario Firebase autenticado antes del envío. Sin sesión conserva el trabajo para reintentar. El modo de demostración sigue siendo local y no representa cuentas reales del backend.

Usar HTTPS para el backend. Solo debug permite HTTP para localhost, 127.0.0.1 o el host de emulador 10.0.2.2. En teléfono de desarrollo se puede utilizar adb reverse tcp:8080 tcp:8080 con la URL localhost; release requiere HTTPS. No registrar ni compartir tokens.

Las pruebas actuales usan identidades simuladas. No acreditan un inicio de sesión real: falta confirmar cuentas de operador y supervisor y asignar sus claims desde un entorno administrativo autorizado. No crear un selector de rol que conceda privilegios en producción.

Recepción: después del envío, WorkManager consulta el servidor y combina registros por código. Solo actualiza filas locales ya sincronizadas; conserva sus identificadores y fotos privadas. Los registros pendientes o con error se conservan para evitar pérdida de ediciones. Al iniciar sesión real se solicita sincronización; también sigue activa la ejecución periódica.

Pendientes: sincronización de fotografías, propiedad de registros por usuario y resolución de conflictos entre dispositivos. Los controles actuales son por rol dentro del proyecto, no una separación entre organizaciones. El reenvío de muestras ya revisadas se rechaza para operadores; si tienen cambios locales pendientes, requieren reconciliación explícita. Una edición durante el envío conserva el estado pendiente cuando el contenido difiere de la copia enviada.

Referencia: https://firebase.google.com/docs/auth/admin/verify-id-tokens
