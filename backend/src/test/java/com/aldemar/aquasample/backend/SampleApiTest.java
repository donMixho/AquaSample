package com.aldemar.aquasample.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.security.test.context.support.WithMockUser(roles = "SUPERVISOR")
class SampleApiTest {
    @Autowired MockMvc mvc;

    private static String body(String code, int count) {
        return "{\"code\":\"" + code + "\",\"concession\":\"C1\",\"center\":\"Centro Huar\",\"train\":\"T1\",\"line\":\"L1\","
                + "\"sectionLengthCm\":100,\"timestamp\":1700000000000,\"operatorName\":\"Carlos\",\"manualCount\":" + count
                + ",\"markerCoordinatesJson\":\"[]\",\"observations\":\"ok\",\"reviewStatus\":\"PENDIENTE_REVISION\"}";
    }

    @Test
    @org.springframework.security.test.context.support.WithAnonymousUser
    void anonymousCannotReadOrForgeSupervisor() throws Exception {
        mvc.perform(get("/api/samples")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/samples/ANY/review").header("X-User-Role", "SUPERVISOR")
            .contentType(MediaType.APPLICATION_JSON).content("{\"reviewStatus\":\"VALIDADA\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "OPERATOR")
    void operatorCannotValidateThroughGenericCreate() throws Exception {
        mvc.perform(post("/api/samples").contentType(MediaType.APPLICATION_JSON)
            .content(body("FORGED", 4).replace("PENDIENTE_REVISION", "VALIDADA")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/samples").contentType(MediaType.APPLICATION_JSON).content(body("OP-OK", 4)))
            .andExpect(status().isCreated());
        mvc.perform(put("/api/samples/OP-OK").contentType(MediaType.APPLICATION_JSON)
            .content(body("OP-OK", 4).replace("PENDIENTE_REVISION", "VALIDADA")))
            .andExpect(status().isForbidden());
    }

    @Test
    void createDuplicateUpdateAndReviewRoles() throws Exception {
        mvc.perform(post("/api/samples").contentType(MediaType.APPLICATION_JSON).content(body("MUE-T1", 10)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("MUE-T1"));
        mvc.perform(post("/api/samples").contentType(MediaType.APPLICATION_JSON).content(body("MUE-T1", 10)))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/samples/MUE-T1").contentType(MediaType.APPLICATION_JSON).content(body("MUE-T1", 77)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.manualCount").value(77));
        mvc.perform(put("/api/samples/NOPE").contentType(MediaType.APPLICATION_JSON).content(body("NOPE", 1)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/samples").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest());

        String review = "{\"reviewStatus\":\"VALIDADA\",\"supervisorComment\":\"ok\",\"supervisorName\":\"Marcelo\"}";
        for (String role : new String[]{"OPERATOR", "ANALYST"})
            mvc.perform(put("/api/samples/MUE-T1/review").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("test").roles(role)).header("X-User-Role", "SUPERVISOR")
                    .contentType(MediaType.APPLICATION_JSON).content(review)).andExpect(status().isForbidden());
        mvc.perform(get("/api/samples/validated")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(put("/api/samples/MUE-T1/review").contentType(MediaType.APPLICATION_JSON).content(review))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reviewStatus").value("VALIDADA"));
        mvc.perform(get("/api/samples/validated")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/samples").param("center", "centro huar").param("reviewStatus", "VALIDADA"))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/samples/MUE-T1")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void invalidBranchesReturn400AndAliasesWork() throws Exception {
        mvc.perform(post("/api/samples").contentType(MediaType.APPLICATION_JSON).content(body("MUE-T2", 1)))
                .andExpect(status().isCreated());
        // reviewStatus inválido en el filtro de listado
        mvc.perform(get("/api/samples").param("reviewStatus", "NOPE")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/samples").param("reviewStatus", " ").param("center", " ")).andExpect(status().isOk());
        // code distinto entre URL y cuerpo en PUT
        mvc.perform(put("/api/samples/MUE-T2").contentType(MediaType.APPLICATION_JSON).content(body("OTRO", 1)))
                .andExpect(status().isBadRequest());
        // reviewStatus inválido en el cuerpo de create/update y en /review
        String invalid = body("MUE-T3", 1).replace("PENDIENTE_REVISION", "XYZ");
        mvc.perform(post("/api/samples").contentType(MediaType.APPLICATION_JSON).content(invalid)).andExpect(status().isBadRequest());
        mvc.perform(put("/api/samples/MUE-T2").contentType(MediaType.APPLICATION_JSON)
                .content(body("MUE-T2", 1).replace("PENDIENTE_REVISION", "XYZ"))).andExpect(status().isBadRequest());
        String badReview = "{\"reviewStatus\":\"XYZ\",\"supervisorComment\":\"c\",\"supervisorName\":\"s\"}";
        mvc.perform(put("/api/samples/MUE-T2/review").contentType(MediaType.APPLICATION_JSON).content(badReview))
                .andExpect(status().isBadRequest());
        // review de muestra inexistente y rol vacío (se asume SUPERVISOR)
        String ok = "{\"reviewStatus\":\"OBSERVADO\",\"supervisorComment\":\"c\",\"supervisorName\":\"s\"}";
        mvc.perform(put("/api/samples/NOPE/review").contentType(MediaType.APPLICATION_JSON).content(ok))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/samples/MUE-T2/review").header("X-User-Role", " supervisor ")
                .contentType(MediaType.APPLICATION_JSON).content(ok))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reviewStatus").value("OBSERVADA"));
        mvc.perform(get("/api/samples/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void statusParsingAndDtoDefaults() throws Exception {
        org.junit.jupiter.api.Assertions.assertNull(SampleStatus.parse(null));
        org.junit.jupiter.api.Assertions.assertNull(SampleStatus.parse("zzz"));
        org.junit.jupiter.api.Assertions.assertEquals(SampleStatus.PENDIENTE_REVISION, SampleStatus.parse("pendiente"));
        org.junit.jupiter.api.Assertions.assertEquals(SampleStatus.VALIDADA, SampleStatus.parse("VALIDADO"));
        org.junit.jupiter.api.Assertions.assertEquals(SampleStatus.CORREGIDA, SampleStatus.parse(" corregida "));
        // sin markerCoordinatesJson ni reviewStatus: el controlador aplica valores por defecto
        String minimal = body("MUE-T4", 2).replace(",\"markerCoordinatesJson\":\"[]\"", "")
                .replace(",\"reviewStatus\":\"PENDIENTE_REVISION\"", "");
        mvc.perform(post("/api/samples").contentType(MediaType.APPLICATION_JSON).content(minimal))
                .andExpect(status().isCreated());
    }
}
