package swd.billiardshop.service;


import org.springframework.stereotype.Service;
import swd.billiardshop.entity.Review;
import java.util.List;
import swd.billiardshop.repository.ReviewRepository;
import swd.billiardshop.repository.ProductRepository;
import swd.billiardshop.entity.Product;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;
import swd.billiardshop.enums.ReviewStatus;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Transactional
    public Review createReviewForProduct(swd.billiardshop.entity.User user, Integer productId, Integer rating, String title, String comment) {
        if (rating == null || rating < 1 || rating > 5) throw new AppException(ErrorCode.INVALID_REQUEST, "Rating must be between 1 and 5");
        Product p = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));
        Review r = Review.builder()
                .user(user)
                .reviewableType("product")
                .reviewableId(productId)
                .rating(rating)
                .title(title)
                .comment(comment)
                .isVerified(false)
                .status(swd.billiardshop.enums.ReviewStatus.APPROVED)
                .build();
        Review saved = reviewRepository.save(r);

        // recalc
        recalcAndSaveProductRating(p);
        return saved;
    }

    @Transactional
    public void deleteReview(Integer reviewId) {
        Review r = reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Review not found"));
        Integer pid = r.getReviewableId();
        reviewRepository.delete(r);
        productRepository.findById(pid).ifPresent(this::recalcAndSaveProductRating);
    }

    @Transactional
    public Review approveReview(Integer reviewId, ReviewStatus newStatus) {
        Review r = reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Review not found"));
        r.setStatus(newStatus == null ? ReviewStatus.APPROVED : newStatus);
        Review saved = reviewRepository.save(r);
        productRepository.findById(r.getReviewableId()).ifPresent(this::recalcAndSaveProductRating);
        return saved;
    }

    private void recalcAndSaveProductRating(Product p) {
    List<Review> reviews = reviewRepository.findAll().stream()
        .filter(rv -> "product".equalsIgnoreCase(rv.getReviewableType())
            && rv.getReviewableId().equals(p.getProductId())
            && rv.getStatus() == ReviewStatus.APPROVED)
        .collect(Collectors.toList());
        int count = reviews.size();
        BigDecimal avg = BigDecimal.ZERO;
        if (count > 0) {
            double sum = reviews.stream().mapToInt(Review::getRating).sum();
            avg = BigDecimal.valueOf(sum / count).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        productService.recalculateRating(p.getProductId(), avg, count);
    }
}
