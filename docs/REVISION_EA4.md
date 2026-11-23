# Revisión EA4 del 23 de noviembre de 2026

Base revisada: c0a0734, cambios del equipo del 21 de noviembre. Rama local: revision/ea4. Esta revisión no certifica todavía la entrega completa.

## Pendientes prioritarios

- Verificar acceso con cuentas Firebase reales. La revisión local ya incorpora validación JWT y permisos; X-User-Role ya no concede acceso. Ver SEGURIDAD_BACKEND.md.
- Comprobar recepción con cuentas reales: SampleReceiver ya descarga registros y dictámenes sin sobrescribir cambios locales pendientes. Falta demostrar una modificación desde Swagger visible en el teléfono.
- Completar el ciclo CRUD y definir el tratamiento de fotos remotas. El DTO Android actual no transmite los archivos.
- Completar resolución de conflictos entre dispositivos. La comprobación transaccional ya conserva una edición local realizada durante el envío.
- Probar migración con códigos duplicados: INSERT OR IGNORE no los elimina porque el índice único se crea después de copiar las filas.
- Actualizar cobertura tras cambios de sincronización y verificar APK release firmada.

## Corrección inicial

Comprobaciones posteriores: 90 pruebas unitarias Android aprobadas, 5 pruebas backend aprobadas y APK debug compilado. Antes de añadir la recepción, la cobertura de líneas fue ViewModel 420/436 (96,3%) y Repository 147/149 (98,7%); debe regenerarse para el estado final. No equivale a prueba funcional con cuentas reales ni a cobertura de ramas superior al 90%. Las pruebas de seguridad verifican rechazo anónimo, rol falsificado y validación por POST/PUT genérico. Las tres nuevas pruebas verifican recepción del dictamen, conservación de foto local y protección de ediciones pendientes. Falta probar firma JWT real y recorrido entre dispositivos.

La configuración predeterminada del backend pasa de H2 en memoria a H2 en disco, en backend/data. AQUASAMPLE_DB_URL permite cambiar la dirección. Las pruebas usan una base en memoria independiente con create-drop. La consola H2 queda desactivada. Falta comprobar el reinicio real del servidor con un registro creado por HTTP.

## Tarjetas de prueba para Trello

Cada caso debe registrar responsable, versión/commit, fecha, resultado real y captura o log. Estado inicial de todos: pendiente. No marcar aprobado por tener código implementado.

| Caso | Pasos | Resultado esperado |
|---|---|---|
| Acceso y roles | Entrar como operador, supervisor y analista; intentar acciones ajenas al rol | Acciones permitidas funcionan y las prohibidas se rechazan también en servidor |
| Formulario | Dejar obligatorios vacíos; probar longitud cero y conteo negativo; corregir | Mensajes claros y guardado solo con datos válidos |
| Cámara | Capturar, cancelar, repetir y confirmar | Vista previa inmediata; foto correcta en resumen y detalle |
| Galería | Elegir imagen válida; cancelar selección | Foto conservada y flujo utilizable |
| Marcas | Ampliar foto, marcar, deshacer y corregir total | Coordenadas y conteo consistentes tras reabrir |
| Persistencia móvil | Guardar y reiniciar la app | Muestra y foto permanecen |
| Persistencia backend | Crear por Swagger, detener y arrancar servidor | Registro permanece |
| Sin conexión | Guardar con servidor apagado, luego reconectar | Pendiente visible, envío exitoso y sin duplicados |
| Recepción | Cambiar un registro en Swagger y actualizar app | Cambio visible en teléfono |
| Revisión | Observar sin motivo, agregar motivo, validar con supervisor | Reglas aplicadas y estado conservado |
| Historial | Combinar centro, tren, línea, fechas y estado | Solo aparecen coincidencias y detalle correcto |
| Dashboard | Comparar indicadores con registros conocidos | Totales, unidades y períodos correctos |
| Reporte | Exportar registros validados y abrir archivo | PDF o XLSX legible, campos completos y sin pendientes incluidos |
| API marina | Consultar conectado; cortar red | Fecha, ubicación y carácter estimado visibles; error o caché identificado |
| APK final | Instalar release firmada en teléfono y recorrer flujo | Mismo ejecutable que se entrega, versión visible y sin errores |

La pauta EA4 exige cobertura superior al 90%, al menos un Compose UI Test, integración con backend y API externa, APK release firmada y evidencias GitHub/Trello. Las pruebas manuales complementan, pero no reemplazan, esas evidencias.
