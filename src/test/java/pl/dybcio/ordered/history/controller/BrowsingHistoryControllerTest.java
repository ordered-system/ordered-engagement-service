package pl.dybcio.ordered.history.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.dybcio.ordered.commons.security.AuthenticatedUser;
import pl.dybcio.ordered.history.entity.BrowsingHistoryEntry;
import pl.dybcio.ordered.history.service.BrowsingHistoryService;

@ExtendWith(MockitoExtension.class)
class BrowsingHistoryControllerTest {

  @Mock private BrowsingHistoryService browsingHistoryService;
  private MockMvc mockMvc;

  private final AuthenticatedUser buyer =
      new AuthenticatedUser(42L, "adam@example.com", List.of("ROLE_USER"));

  @BeforeEach
  void setUp() {
    BrowsingHistoryController controller = new BrowsingHistoryController(browsingHistoryService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(
                new AuthenticationPrincipalArgumentResolver(),
                new PageableHandlerMethodArgumentResolver())
            .build();

    var authorities = buyer.roles().stream().map(SimpleGrantedAuthority::new).toList();
    var token = new UsernamePasswordAuthenticationToken(buyer, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(token);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void recordView_returns202_andDelegatesToService() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/browsing-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"productId":10}
                    """))
        .andExpect(status().isAccepted());

    verify(browsingHistoryService).recordView(42L, 10L);
  }

  @Test
  void getMyHistory_returnsEntriesForRequestingUser() throws Exception {
    BrowsingHistoryEntry entry =
        BrowsingHistoryEntry.builder()
            .id("1")
            .userId(42L)
            .productId(10L)
            .viewedAt(Instant.now())
            .build();
    when(browsingHistoryService.getHistoryForUser(eq(42L), any())).thenReturn(List.of(entry));

    mockMvc
        .perform(get("/api/v1/browsing-history"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].productId").value(10));
  }
}
