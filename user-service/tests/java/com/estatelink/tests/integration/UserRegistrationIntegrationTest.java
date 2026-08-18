package java.com.estatelink.tests.integration;
//
//import com.estatelink.user.data.model.Role;
//import com.estatelink.user.data.model.User;
//import com.estatelink.user.data.model.UserStatus;
//import com.estatelink.user.data.repository.UserRepository;
//import com.estatelink.user.dtos.requests.RegisterRequest;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * Integration tests for POST /api/v1/auth/register.
// *
// * Pure Spring Boot Test - no Mockito anywhere in this file. Boots the real
// * app context, hits the real endpoint through MockMvc, and checks the real
// * database through UserRepository. The only assertion library besides JUnit
// * is AssertJ (assertThat(...)) - that's a fluent-assertions library, not a
// * mocking library, so it's unrelated to Mockito.
// *
// * Kafka is left completely alone here: EventProducer.send() is fire-and-forget
// * and register() never waits on the result, so with no broker running in your
// * test environment the call just quietly doesn't deliver an email - nothing
// * in these tests depends on that, so there's nothing to fake or skip.
// *
// * Uses the "test" profile -> application-test.properties (H2 in-memory DB +
// * a test JWT secret) so it never touches your real Supabase database.
// *
// * shouldRejectDuplicateEmailWithConflict is EXPECTED TO FAIL the first time
// * you run this suite. That's intentional - read the comment on that test.
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
public class UserRegistrationIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @AfterEach
//    void cleanUp() {
//        userRepository.deleteAll();
//    }
//
//    @Test
//    void shouldRegisterNewUserAsUnverified() throws Exception {
//        RegisterRequest request = new RegisterRequest();
//        request.setName("Ada Obi");
//        request.setEmail("ada.obi@example.com");
//        request.setPassword("StrongPassword123!");
//        request.setRole(Role.APPLICANT);
//
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.email").value("ada.obi@example.com"))
//                .andExpect(jsonPath("$.status").value("UNVERIFIED"));
//
//        User saved = userRepository.findByEmail("ada.obi@example.com").orElseThrow();
//        assertThat(saved.getStatus()).isEqualTo(UserStatus.UNVERIFIED);
//        assertThat(saved.getVerificationToken()).isNotBlank();
//        assertThat(saved.getTokenExpiresAt()).isAfter(java.time.LocalDateTime.now());
//    }
//
//    @Test
//    void shouldReturn400WhenEmailFormatIsInvalid() throws Exception {
//        RegisterRequest request = new RegisterRequest();
//        request.setName("Bad Email");
//        request.setEmail("not-an-email");
//        request.setPassword("StrongPassword123!");
//        request.setRole(Role.APPLICANT);
//
//        // @Email on RegisterRequest triggers a MethodArgumentNotValidException,
//        // which Spring MVC's default handling already turns into a 400 - no
//        // custom exception handler needed for this one, unlike the next test.
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        assertThat(userRepository.findByEmail("not-an-email")).isEmpty();
//    }
//
//    /**
//     * THIS TEST WILL FAIL THE FIRST TIME YOU RUN IT - that's the point.
//     *
//     * UserService.register() throws a plain IllegalArgumentException for
//     * "Email already in use". user-service has no @RestControllerAdvice
//     * anywhere to catch it (property-service has one, user-service doesn't),
//     * so Spring's default error handling turns that into a 500 Internal
//     * Server Error instead of a clean 409 Conflict.
//     *
//     * Run the suite, watch this one go red with a 500 where it expected 409,
//     * then add a handler to user-service, e.g.:
//     *
//     *   @RestControllerAdvice
//     *   public class GlobalExceptionHandler {
//     *       @ExceptionHandler(IllegalArgumentException.class)
//     *       public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
//     *           return ResponseEntity.status(HttpStatus.CONFLICT)
//     *                   .body(Map.of("error", e.getMessage()));
//     *       }
//     *   }
//     *
//     * property-service/src/main/java/com/estatelink/property/controller/GlobalExceptionHandler.java
//     * already has this exact shape for its own exceptions - copy the pattern
//     * into user-service and point it at IllegalArgumentException. Re-run the
//     * test after adding it; it should go green.
//     */
//    @Test
//    void shouldRejectDuplicateEmailWithConflict() throws Exception {
//        RegisterRequest request = new RegisterRequest();
//        request.setName("Chidi Eze");
//        request.setEmail("chidi.eze@example.com");
//        request.setPassword("StrongPassword123!");
//        request.setRole(Role.OWNER);
//        String payload = objectMapper.writeValueAsString(request);
//
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(payload))
//                .andExpect(status().isCreated());
//
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(payload))
//                .andExpect(status().isConflict());
//
//        assertThat(userRepository.findAll()).hasSize(1);
//    }
}
