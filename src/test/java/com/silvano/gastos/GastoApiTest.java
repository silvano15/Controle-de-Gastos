package com.silvano.gastos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GastoApiTest {

    @Autowired MockMvc mvc;

    @Test
    void fluxoCompleto() throws Exception {
        // sem PIN -> 401
        mvc.perform(get("/api/gastos")).andExpect(status().isUnauthorized());

        // valor inválido -> 400
        mvc.perform(post("/api/gastos").header("X-Pin", "1234").contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":0}")).andExpect(status().isBadRequest());

        // registra
        mvc.perform(post("/api/gastos").header("X-Pin", "1234").contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":12.5,\"descricao\":\"Almoço\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dataHora").exists());

        mvc.perform(post("/api/gastos").header("X-Pin", "1234").contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":7.49}")).andExpect(status().isCreated());

        // histórico
        for (String p : new String[]{"MES", "7D", "15D", "30D", "3M", "6M", "12M"}) {
            mvc.perform(get("/api/gastos?periodo=" + p).header("X-Pin", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.total").value(19.99));
        }

        // página estática sem PIN
        mvc.perform(get("/index.html")).andExpect(status().isOk());
    }
}
