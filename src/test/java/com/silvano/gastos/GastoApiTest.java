package com.silvano.gastos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GastoApiTest {

    @Autowired MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private String cadastrar(String email) throws Exception {
        String corpo = "{\"nome\":\"Teste\",\"email\":\"" + email + "\",\"senha\":\"12345678\"}";
        MvcResult resultado = mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        return mapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void fluxoCompleto() throws Exception {
        // sem token -> 401
        mvc.perform(get("/api/gastos")).andExpect(status().isUnauthorized());

        String token = cadastrar("teste@example.com");

        // e-mail duplicado -> 409
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Teste\",\"email\":\"teste@example.com\",\"senha\":\"12345678\"}"))
            .andExpect(status().isConflict());

        // senha errada -> 401
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"teste@example.com\",\"senha\":\"errada123\"}"))
            .andExpect(status().isUnauthorized());

        // login certo
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"teste@example.com\",\"senha\":\"12345678\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());

        // /api/auth/eu também é protegido (só /cadastro, /login, /esqueci-senha e /resetar-senha são públicos)
        mvc.perform(get("/api/auth/eu")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("teste@example.com"));

        // valor inválido -> 400
        mvc.perform(post("/api/gastos").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":0}")).andExpect(status().isBadRequest());

        // registra dois gastos
        mvc.perform(post("/api/gastos").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":12.5,\"descricao\":\"Almoço\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dataHora").exists());

        mvc.perform(post("/api/gastos").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":7.49}")).andExpect(status().isCreated());

        // histórico
        for (String p : new String[]{"MES", "7D", "15D", "30D", "3M", "6M", "12M"}) {
            mvc.perform(get("/api/gastos?periodo=" + p).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.total").value(19.99));
        }

        // outro usuário não vê os gastos do primeiro (cada conta é isolada)
        String tokenOutro = cadastrar("outro@example.com");
        mvc.perform(get("/api/gastos").header("Authorization", "Bearer " + tokenOutro))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantidade").value(0));

        // token de um usuário não deixa mexer no gasto do outro
        MvcResult historico = mvc.perform(get("/api/gastos").header("Authorization", "Bearer " + token))
            .andReturn();
        long idGasto = mapper.readTree(historico.getResponse().getContentAsString())
                .get("gastos").get(0).get("id").asLong();
        mvc.perform(delete("/api/gastos/" + idGasto).header("Authorization", "Bearer " + tokenOutro))
            .andExpect(status().isNotFound());

        // recuperação de senha com e-mail inexistente não revela nada e não quebra
        mvc.perform(post("/api/auth/esqueci-senha").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"naoexiste@example.com\"}"))
            .andExpect(status().isOk());

        // página estática sem token
        mvc.perform(get("/login.html")).andExpect(status().isOk());
    }
}
