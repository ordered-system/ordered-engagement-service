package pl.dybcio.ordered.history.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.dybcio.ordered.history.service.BrowsingHistoryService;

@ExtendWith(MockitoExtension.class)
class InternalBrowsingHistoryControllerTest {

  @Mock private BrowsingHistoryService browsingHistoryService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    InternalBrowsingHistoryController controller =
        new InternalBrowsingHistoryController(browsingHistoryService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void recordView_returns202_andDelegatesToServiceWithExplicitUserId() throws Exception {
    mockMvc
        .perform(
            post("/internal/v1/browsing-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"userId":42,"productId":10}
                    """))
        .andExpect(status().isAccepted());

    verify(browsingHistoryService).recordView(42L, 10L);
  }
}
