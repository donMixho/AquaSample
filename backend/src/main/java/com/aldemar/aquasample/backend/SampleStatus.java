package com.aldemar.aquasample.backend;

/** Mismos nombres que SampleStatus.kt de la app Android. */
public enum SampleStatus {
    BORRADOR, PENDIENTE_REVISION, OBSERVADA, CORREGIDA, VALIDADA;

    /** Acepta también los alias antiguos de la app (PENDIENTE, VALIDADO, OBSERVADO). Null si es inválido. */
    public static SampleStatus parse(String v) {
        if (v == null) return null;
        String u = v.trim().toUpperCase();
        switch (u) {
            case "PENDIENTE": return PENDIENTE_REVISION;
            case "VALIDADO": return VALIDADA;
            case "OBSERVADO": return OBSERVADA;
            default:
                try { return valueOf(u); } catch (IllegalArgumentException e) { return null; }
        }
    }
}
