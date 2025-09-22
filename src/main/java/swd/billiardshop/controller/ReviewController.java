package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.entity.Review;
import swd.billiardshop.service.ReviewService;
import swd.billiardshop.service.UserService;
import swd.billiardshop.dto.request.ReviewRequest;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/user/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @PostMapping
    public Review createReview(@jakarta.validation.Valid @RequestBody ReviewRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Unauthenticated");
        var user = userService.getUserEntityByUsername(auth.getName());
        return reviewService.createReviewForProduct(user, req.getProductId(), req.getRating(), req.getTitle(), req.getComment());
    }

    @PostMapping("/{id}/approve")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Review approveReview(@PathVariable Integer id, @RequestParam(required = false) swd.billiardshop.enums.ReviewStatus status) {
        return reviewService.approveReview(id, status);
    }
}
