package com.aldemar.aquasample.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** Contrato idéntico a SampleDto de AquaSampleBackendApi.kt (+ photoPath y updatedAt). */
public record SampleDto(
        @NotBlank String code,
        String concession,
        @NotBlank String center,
        @NotBlank String train,
        @NotBlank String line,
        @Positive int sectionLengthCm,
        long timestamp,
        @NotBlank String operatorName,
        @PositiveOrZero int manualCount,
        String markerCoordinatesJson,
        @NotNull String observations,
        String photoPath,
        String reviewStatus,
        String supervisorComment,
        String supervisorName,
        Long updatedAt) {

    static SampleDto from(SampleRecord r) {
        return new SampleDto(r.getCode(), r.getConcession(), r.getCenter(), r.getTrain(), r.getLine(),
                r.getSectionLengthCm(), r.getTimestamp(), r.getOperatorName(), r.getManualCount(),
                r.getMarkerCoordinatesJson(), r.getObservations(), r.getPhotoPath(),
                r.getReviewStatus().name(), r.getSupervisorComment(), r.getSupervisorName(), r.getUpdatedAt());
    }
}
