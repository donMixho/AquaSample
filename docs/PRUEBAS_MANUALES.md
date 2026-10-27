# Pruebas manuales

Registrar dispositivo, versión de Android, fecha, responsable y resultado real de cada comprobación. La presencia de una prueba en esta lista no implica que haya sido ejecutada.

| Caso | Resultado esperado |
|---|---|
| Acceso Firebase y registro | Cuenta válida ingresa; errores visibles para credenciales incorrectas. |
| Demostración debug | Base separada y encabezado DEMOSTRACIÓN; permite ambos perfiles. |
| Cuenta sin atributo de supervisor | Solo consulta muestras; no muestra acciones de dictamen. |
| Campos vacíos, longitud cero/negativa o texto | No avanza; muestra cómo corregir el dato. |
| Longitud con coma decimal | Se guarda el valor correcto. |
| Conteo cero, negativo, texto y desbordamiento | Acepta cero; rechaza valores inválidos sin convertirlos silenciosamente. |
| Cámara real y cancelación | Confirma una foto válida; cancelar permite reintentar. |
| Galería real | Copia la imagen al almacenamiento privado; persiste tras reiniciar. |
| Recreación de Activity | Mantiene los campos del borrador. |
| Guardar dos veces o reintentar | No duplica la misma muestra. |
| Cerrar y abrir la app | Conserva registros y fotografías guardados. |
| Búsqueda y filtros | Combinan texto, centro y estado; abrir detalle corresponde a la muestra elegida. |
| Supervisor observa sin motivo | Rechaza la acción; permite guardar al escribir un motivo. |
| Operador intenta emitir dictamen | La interfaz y el ViewModel rechazan la acción. |
| Cerrar sesión y cambiar de modo | No mezcla el historial de demostración con el real. |

Ver VERIFICACION.md para los resultados efectivamente ejecutados de esta entrega. La prueba automatizada de formulario introduce una imagen de prueba; no sustituye cámara/galería reales.
