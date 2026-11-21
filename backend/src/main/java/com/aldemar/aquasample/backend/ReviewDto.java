package com.aldemar.aquasample.backend;

import jakarta.validation.constraints.NotBlank;

public record ReviewDto(@NotBlank String reviewStatus, String supervisorComment, String supervisorName) {}
