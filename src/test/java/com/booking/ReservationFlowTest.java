package com.booking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void sansJeton_onEstRefuse() throws Exception {
        mvc.perform(get("/api/resources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unUtilisateurReserve_puisLeMemeCreneauEstRefuse() throws Exception {
        String alice = registerAndLogin("alice@test.com");
        String bob = registerAndLogin("bob@test.com");

        reserve(alice, 1, "2030-01-10T10:00:00", "2030-01-10T12:00:00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EN_ATTENTE"));

        reserve(bob, 1, "2030-01-10T11:00:00", "2030-01-10T13:00:00")
                .andExpect(status().isConflict());
    }

    @Test
    void seulUnAdminPeutValider() throws Exception {
        String claire = registerAndLogin("claire@test.com");
        String body = reserve(claire, 2, "2030-01-11T10:00:00", "2030-01-11T12:00:00")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(body).get("id").asLong();

        mvc.perform(patch("/api/reservations/{id}/validate", id)
                        .header("Authorization", "Bearer " + claire))
                .andExpect(status().isForbidden());

        String admin = login("admin@test.local", "admin-test-password");
        mvc.perform(patch("/api/reservations/{id}/validate", id)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALIDEE"));
    }

    @Test
    void desDonneesInvalidesSontExpliqueesChampParChamp() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("pasunemail", "court")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("L'email n'est pas valide"))
                .andExpect(jsonPath("$.errors.password")
                        .value("Le mot de passe doit faire entre 8 et 72 caractères"));
    }

    @Test
    void laDocumentationEstPublique() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Resource Booking API"));
    }

    // --- Petites méthodes d'aide, pour garder les tests lisibles ---

    private String registerAndLogin(String email) throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, "motdepasse123")))
                .andExpect(status().isCreated());
        return login(email, "motdepasse123");
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    private String credentials(String email, String password) throws Exception {
        return json.writeValueAsString(Map.of("email", email, "password", password));
    }

    private ResultActions reserve(String token, long resourceId, String start, String end) throws Exception {
        String body = json.writeValueAsString(
                Map.of("resourceId", resourceId, "startTime", start, "endTime", end));
        return mvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}