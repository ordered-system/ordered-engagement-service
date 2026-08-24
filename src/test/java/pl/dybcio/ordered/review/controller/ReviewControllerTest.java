package pl.dybcio.ordered.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.dybcio.ordered.common.exception.GlobalExceptionHandler;
import pl.dybcio.ordered.review.entity.Review;
import pl.dybcio.ordered.review.service.DuplicateReviewException;
import pl.dybcio.ordered.review.service.ProductNotPurchasedException;
import pl.dybcio.ordered.review.service.ReviewService;
import pl.dybcio.ordered.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

  @Mock private ReviewService reviewService;
  private MockMvc mockMvc;

  private final AuthenticatedUser buyer =
      new AuthenticatedUser(42L, "adam@example.com", List.of("ROLE_USER"));

  @BeforeEach
  void setUp() {
    ReviewController controller = new ReviewController(reviewService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
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

  private Review sampleReview() {
    return Review.builder()
        .id("abc123")
        .productId(10L)
        .userId(42L)
        .userEmail("adam@example.com")
        .rating(5)
        .comment("Great!")
        .createdAt(Instant.now())
        .build();
  }

  @Test
  void addReview_returns201_onSuccess() throws Exception {
    when(reviewService.addReview(eq(42L), eq("adam@example.com"), any()))
        .thenReturn(sampleReview());

    mockMvc
        .perform(
            post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"productId":10,"rating":5,"comment":"Great!"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.productId").value(10))
        .andExpect(jsonPath("$.rating").value(5));
  }

  @Test
  void addReview_returns403_whenProductNotPurchased() throws Exception {
    when(reviewService.addReview(eq(42L), eq("adam@example.com"), any()))
        .thenThrow(new ProductNotPurchasedException(10L));

    mockMvc
        .perform(
            post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"productId":10,"rating":5,"comment":"Great!"}
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void addReview_returns409_whenAlreadyReviewed() throws Exception {
    when(reviewService.addReview(eq(42L), eq("adam@example.com"), any()))
        .thenThrow(new DuplicateReviewException(10L));

    mockMvc
        .perform(
            post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"productId":10,"rating":5,"comment":"Great!"}
                    """))
        .andExpect(status().isConflict());
  }

  @Test
  void addReview_returns400_whenRatingOutOfRange() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"productId":10,"rating":9,"comment":"Great!"}
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getReviewsForProduct_returnsPagedReviews() throws Exception {
    when(reviewService.getReviewsForProduct(eq(10L), any()))
        .thenReturn(new PageImpl<>(List.of(sampleReview())));

    mockMvc
        .perform(get("/api/v1/reviews/product/10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].productId").value(10))
        .andExpect(jsonPath("$.totalElements").value(1));
  }
}
