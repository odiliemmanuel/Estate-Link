package java.com.estatelink.tests.security;
//
//import com.estatelink.user.data.model.Role;
//import com.estatelink.user.data.model.User;
//import com.estatelink.user.data.model.UserStatus;
//import com.estatelink.user.data.repository.UserRepository;
//import com.estatelink.user.services.JwtService;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * Security tests for JwtAuthFilter + SecurityConfig, run against real
// * endpoints in UserController.
// *
// * NOTE on status codes: your SecurityConfig has no custom
// * AuthenticationEntryPoint configured (no .exceptionHandling(...) block).
// * With Spring Security's lambda DSL and no formLogin()/httpBasic(), the
// * fallback entry point returns 403 for BOTH "no token at all" and "valid
// * token, wrong role" — there's currently no way to tell "you're not logged
// * in" apart from "you're logged in but not allowed" from the status code
// * alone. The test below asserts the actual current behavior (403 for a
// * missing token). If you'd rather have the conventional 401-for-missing /
// * 403-for-wrong-role split, add:
// *   .exceptionHandling(ex -> ex.authenticationEntryPoint(
// *       (req, res, e) -> res.sendError(401, "Missing or invalid token")))
// * to SecurityConfig and flip the first test's expectation to isUnauthorized().
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
public class UserSecurityTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private JwtService jwtService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @AfterEach
//    void cleanUp() {
//        userRepository.deleteAll();
//    }
//
//    private User persistUser(String email, Role role) {
//        return userRepository.save(User.builder()
//                .name("Test User")
//                .email(email)
//                .password(passwordEncoder.encode("StrongPassword123!"))
//                .role(role)
//                .status(UserStatus.ACTIVE)
//                .build());
//    }
//
//    @Test
//    void shouldReturn403WhenNoTokenProvidedToAdminEndpoint() throws Exception {
//        // /api/v1/users/agents is admin-only at both the SecurityConfig
//        // matcher level and the @PreAuthorize level
//        mockMvc.perform(get("/api/v1/users/agents"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void shouldReturn403WhenNonAdminCallsAdminEndpoint() throws Exception {
//        User applicant = persistUser("applicant@example.com", Role.APPLICANT);
//        String token = jwtService.generateToken(applicant);
//
//        mockMvc.perform(get("/api/v1/users/agents")
//                        .header("Authorization", "Bearer " + token))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void shouldReturn200WhenAdminCallsAdminEndpoint() throws Exception {
//        User admin = persistUser("admin@example.com", Role.ADMIN);
//        String token = jwtService.generateToken(admin);
//
//        mockMvc.perform(get("/api/v1/users/agents")
//                        .header("Authorization", "Bearer " + token))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void shouldAllowAnyAuthenticatedRoleToFetchOwnProfile() throws Exception {
//        // GET /api/v1/users/{id} has no @PreAuthorize — SecurityConfig just
//        // requires .anyRequest().authenticated(), so any valid role passes
//        User agent = persistUser("agent@example.com", Role.AGENT);
//        String token = jwtService.generateToken(agent);
//
//        mockMvc.perform(get("/api/v1/users/" + agent.getId())
//                        .header("Authorization", "Bearer " + token))
//                .andExpect(status().isOk());
//    }
}
