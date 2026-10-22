package com.bookloop.service;

import com.bookloop.dto.ProductForm;
import com.bookloop.entity.Condition;
import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.Product;
import com.bookloop.entity.User;
import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles all business logic for book listings.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Returns the 8 most recently posted available listings (for homepage).
     */
    @Transactional(readOnly = true)
    public List<Product> findRecentAvailable() {
        return productRepository.findTop8ByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE);
    }

    /**
     * Searches available listings with optional filters.
     * All filter parameters are optional — null values are ignored.
     */
    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, String category, String campus,
                                 Condition condition, BigDecimal minPrice, BigDecimal maxPrice,
                                 int page, int size) {
        // Sanitize empty strings to null so the JPQL IS NULL checks work
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cat = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        String cam = (campus != null && !campus.trim().isEmpty()) ? campus.trim() : null;

        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchAvailable(
                ListingStatus.AVAILABLE, kw, cat, cam, condition, minPrice, maxPrice, pageable);
    }

    /**
     * Retrieves a listing by ID. Throws if not found.
     */
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found: " + id));
    }

    /**
     * Returns all listings belonging to a specific seller (including non-AVAILABLE).
     */
    @Transactional(readOnly = true)
    public List<Product> findBySellerId(Long sellerId) {
        return productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    /**
     * Returns distinct categories present in AVAILABLE listings.
     */
    @Transactional(readOnly = true)
    public List<String> findAvailableCategories() {
        return productRepository.findDistinctCategoriesByStatus(ListingStatus.AVAILABLE);
    }

    /**
     * Returns distinct campuses present in AVAILABLE listings.
     */
    @Transactional(readOnly = true)
    public List<String> findAvailableCampuses() {
        return productRepository.findDistinctCampusesByStatus(ListingStatus.AVAILABLE);
    }

    /**
     * Returns the total count of available listings.
     */
    @Transactional(readOnly = true)
    public long countAvailable() {
        return productRepository.countByStatus(ListingStatus.AVAILABLE);
    }

    /**
     * Creates a new listing. The seller is set from the authenticated user — never from form input.
     */
    @Transactional
    public Product createListing(ProductForm form, User seller) {
        Product product = new Product();
        applyFormToProduct(form, product);
        product.setSeller(seller);
        product.setStatus(ListingStatus.AVAILABLE);
        return productRepository.save(product);
    }

    /**
     * Updates an existing listing. Verifies ownership before applying changes.
     *
     * @throws UnauthorizedOperationException if the authenticated user does not own the listing
     */
    @Transactional
    public Product updateListing(Long productId, ProductForm form, User authenticatedUser) {
        Product product = findById(productId);
        verifyOwnership(product, authenticatedUser);
        applyFormToProduct(form, product);
        return productRepository.save(product);
    }

    /**
     * Deletes a listing. Verifies ownership before deleting.
     *
     * @throws UnauthorizedOperationException if the authenticated user does not own the listing
     */
    @Transactional
    public void deleteListing(Long productId, User authenticatedUser) {
        Product product = findById(productId);
        verifyOwnership(product, authenticatedUser);
        productRepository.delete(product);
    }

    // --- Private helpers ---

    private void applyFormToProduct(ProductForm form, Product product) {
        product.setTitle(form.getTitle());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setCondition(form.getCondition());
        product.setCategory(form.getCategory());
        product.setAuthor(form.getAuthor());
        product.setPublisher(form.getPublisher());
        product.setEdition(form.getEdition());
        product.setIsbn(form.getIsbn());
        product.setCourseCode(form.getCourseCode());
        product.setImageUrl(form.getImageUrl());
        product.setDefectNotes(form.getDefectNotes());
        product.setCampus(form.getCampus());
        if (form.getStatus() != null) {
            product.setStatus(form.getStatus());
        }
    }

    private void verifyOwnership(Product product, User authenticatedUser) {
        if (!product.getSeller().getId().equals(authenticatedUser.getId())) {
            throw new UnauthorizedOperationException(
                    "You are not authorized to modify this listing.");
        }
    }
}
