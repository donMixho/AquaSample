package com.aldemar.aquasample.backend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/samples")
@Tag(name = "Samples", description = "Muestras de AquaSample (sincronización offline desde la app)")
public class SampleController {
    private final SampleRepository repo;

    public SampleController(SampleRepository repo) { this.repo = repo; }

    @Operation(summary = "Lista muestras con filtros opcionales")
    @GetMapping
    public List<SampleDto> list(@RequestParam(required = false) String center,
                                @RequestParam(required = false) String train,
                                @RequestParam(required = false) String line,
                                @RequestParam(required = false) String reviewStatus) {
        SampleStatus st = null;
        if (reviewStatus != null && !reviewStatus.isBlank()) {
            st = SampleStatus.parse(reviewStatus);
            if (st == null) throw bad("reviewStatus inválido: " + reviewStatus);
        }
        return repo.search(blank(center), blank(train), blank(line), st).stream().map(SampleDto::from).toList();
    }

    @Operation(summary = "Solo muestras VALIDADA (reportes)")
    @GetMapping("/validated")
    public List<SampleDto> validated() {
        return repo.search(null, null, null, SampleStatus.VALIDADA).stream().map(SampleDto::from).toList();
    }

    @Operation(summary = "Detalle por código único")
    @GetMapping("/{code}")
    public SampleDto get(@PathVariable String code) { return SampleDto.from(find(code)); }

    @Operation(summary = "Crea muestra; 409 si el code ya existe")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SampleDto create(@Valid @RequestBody SampleDto dto) {
        if (repo.findByCode(dto.code()).isPresent()) throw conflict(dto.code());
        SampleRecord r = new SampleRecord();
        r.setCode(dto.code());
        apply(r, dto);
        try {
            return SampleDto.from(repo.saveAndFlush(r));
        } catch (DataIntegrityViolationException e) { // carrera entre dos POST simultáneos
            throw conflict(dto.code());
        }
    }

    @Operation(summary = "Actualiza muestra existente; 404 si no existe")
    @PutMapping("/{code}")
    public SampleDto update(@PathVariable String code, @Valid @RequestBody SampleDto dto) {
        if (!code.equals(dto.code())) throw bad("El code del cuerpo no coincide con la URL");
        SampleRecord r = find(code);
        apply(r, dto);
        return SampleDto.from(repo.save(r));
    }

    @Operation(summary = "Cambia el estado de revisión (solo SUPERVISOR)",
            description = "Requiere token Firebase válido con claim role SUPERVISOR. X-User-Role no concede permisos.")
    @PutMapping("/{code}/review")
    public SampleDto review(@PathVariable String code,
                            @Parameter(description = "Rol del usuario") @RequestHeader(value = "X-User-Role", required = false) String role,
                            @Valid @RequestBody ReviewDto dto) {
        if (!isSupervisor())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el SUPERVISOR puede revisar muestras");
        SampleStatus st = SampleStatus.parse(dto.reviewStatus());
        if (st == null) throw bad("reviewStatus inválido: " + dto.reviewStatus());
        SampleRecord r = find(code);
        r.setReviewStatus(st);
        r.setSupervisorComment(dto.supervisorComment());
        r.setSupervisorName(dto.supervisorName());
        return SampleDto.from(repo.save(r));
    }

    private SampleRecord find(String code) {
        return repo.findByCode(code).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Muestra no encontrada: " + code));
    }

    private static ResponseStatusException bad(String msg) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg); }

    private static ResponseStatusException conflict(String code) {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una muestra con code " + code);
    }

    private static String blank(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    private static void apply(SampleRecord r, SampleDto d) {
        // Generic create/update must not bypass the dedicated review permission.
        SampleStatus requested = d.reviewStatus() == null ? SampleStatus.PENDIENTE_REVISION : SampleStatus.parse(d.reviewStatus());
        if (requested == null) throw bad("reviewStatus inválido");
        if (!isSupervisor() && (requested != SampleStatus.PENDIENTE_REVISION
                || r.getReviewStatus() != SampleStatus.PENDIENTE_REVISION
                || d.supervisorComment() != null || d.supervisorName() != null))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el supervisor puede modificar un dictamen");
        r.setConcession(d.concession());
        r.setCenter(d.center());
        r.setTrain(d.train());
        r.setLine(d.line());
        r.setSectionLengthCm(d.sectionLengthCm());
        r.setTimestamp(d.timestamp());
        r.setOperatorName(d.operatorName());
        r.setManualCount(d.manualCount());
        r.setMarkerCoordinatesJson(d.markerCoordinatesJson() == null ? "[]" : d.markerCoordinatesJson());
        r.setObservations(d.observations());
        r.setPhotoPath(d.photoPath());
        if (d.reviewStatus() != null) {
            SampleStatus st = SampleStatus.parse(d.reviewStatus());
            if (st == null) throw bad("reviewStatus inválido: " + d.reviewStatus());
            r.setReviewStatus(st);
        }
        r.setSupervisorComment(d.supervisorComment());
        r.setSupervisorName(d.supervisorName());
    }

    private static boolean isSupervisor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR"));
    }
}
