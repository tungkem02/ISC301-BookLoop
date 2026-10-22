package com.bookloop.repository;

import com.bookloop.entity.Condition;
import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Product entity. Uses JpaSpecificationExecutor for dynamic filtering.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    /**
     * Returns all AVAILABLE listings ordered by most recent first (for homepage).
     */
    List<Product> findTop8ByStatusOrderByCreatedAtDesc(ListingStatus status);

    /**
     * Returns all listings belonging to a seller (for My Listings page).
     */
    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    /**
     * Returns all AVAILABLE listings with combined filters (used in search).
     * All filter parameters are optional; null values are ignored.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
              AND (:campus IS NULL OR LOWER(p.campus) LIKE LOWER(CONCAT('%', :campus, '%')))
              AND (:condition IS NULL OR p.condition = :condition)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            ORDER BY p.createdAt DESC
            """)
    Page<Product> searchAvailable(
            @Param("status") ListingStatus status,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("campus") String campus,
            @Param("condition") Condition condition,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Returns distinct categories from all available listings (for filter dropdown).
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.status = :status AND p.category IS NOT NULL ORDER BY p.category")
    List<String> findDistinctCategoriesByStatus(@Param("status") ListingStatus status);

    /**
     * Returns distinct campuses from all available listings (for filter dropdown).
     */
    @Query("SELECT DISTINCT p.campus FROM Product p WHERE p.status = :status ORDER BY p.campus")
    List<String> findDistinctCampusesByStatus(@Param("status") ListingStatus status);

    /**
     * Count of available listings.
     */
    long countByStatus(ListingStatus status);
}
