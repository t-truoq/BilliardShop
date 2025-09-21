package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.entity.Review;
import swd.billiardshop.service.ReviewService;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }
}
