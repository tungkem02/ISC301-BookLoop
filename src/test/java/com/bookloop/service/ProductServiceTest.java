package com.bookloop.service;

import com.bookloop.dto.ProductForm;
import com.bookloop.entity.*;
import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.ProductRepository;
import com.bookloop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProductService (CRUD, search, filters, ownership).
 * Runs against H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User seller1;
    private User seller2;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();

        seller1 = createUser("Seller One", "seller1@test.com");
        seller2 = createUser("Seller Two", "seller2@test.com");
    }

    // --- Create ---

    @Test
    @DisplayName("Create a new listing")
    void createListing_success() {
        ProductForm form = createForm("Test Book", new BigDecimal("25.00"),
                Condition.GOOD, "Main Campus");

        Product saved = productService.createListing(form, seller1);

        assertNotNull(saved.getId());
        assertEquals("Test Book", saved.getTitle());
        assertEquals(new BigDecimal("25.00"), saved.getPrice());
        assertEquals(Condition.GOOD, saved.getCondition());
        assertEquals(ListingStatus.AVAILABLE, saved.getStatus());
        assertEquals(seller1.getId(), saved.getSeller().getId());
    }

    // --- Read ---

    @Test
    @DisplayName("Find listing by ID")
    void findById_success() {
        Product created = productService.createListing(
                createForm("Findable Book", new BigDecimal("10.00"), Condition.NEW, "Campus"),
                seller1);

        Product found = productService.findById(created.getId());

        assertEquals("Findable Book", found.getTitle());
    }

    @Test
    @DisplayName("Find by ID throws exception for nonexistent listing")
    void findById_notFound() {
        assertThrows(ListingNotFoundException.class, () -> productService.findById(99999L));
    }

    // --- Update ---

    @Test
    @DisplayName("Owner can update their listing")
    void updateListing_ownerSuccess() {
        Product created = productService.createListing(
                createForm("Original Title", new BigDecimal("30.00"), Condition.GOOD, "Campus"),
                seller1);

        ProductForm updateForm = createForm("Updated Title", new BigDecimal("25.00"),
                Condition.LIKE_NEW, "New Campus");

        Product updated = productService.updateListing(created.getId(), updateForm, seller1);

        assertEquals("Updated Title", updated.getTitle());
        assertEquals(new BigDecimal("25.00"), updated.getPrice());
    }

    @Test
    @DisplayName("Non-owner cannot update someone else's listing")
    void updateListing_nonOwnerFails() {
        Product created = productService.createListing(
                createForm("Someone's Book", new BigDecimal("20.00"), Condition.GOOD, "Campus"),
                seller1);

        ProductForm form = createForm("Hijacked", new BigDecimal("1.00"), Condition.NEW, "X");

        assertThrows(UnauthorizedOperationException.class,
                () -> productService.updateListing(created.getId(), form, seller2));
    }

    // --- Delete ---

    @Test
    @DisplayName("Owner can delete their listing")
    void deleteListing_ownerSuccess() {
        Product created = productService.createListing(
                createForm("To Delete", new BigDecimal("10.00"), Condition.ACCEPTABLE, "Campus"),
                seller1);

        productService.deleteListing(created.getId(), seller1);

        assertThrows(ListingNotFoundException.class, () -> productService.findById(created.getId()));
    }

    @Test
    @DisplayName("Non-owner cannot delete someone else's listing")
    void deleteListing_nonOwnerFails() {
        Product created = productService.createListing(
                createForm("Protected", new BigDecimal("10.00"), Condition.GOOD, "Campus"),
                seller1);

        assertThrows(UnauthorizedOperationException.class,
                () -> productService.deleteListing(created.getId(), seller2));
    }

    // --- Search / Filters ---

    @Test
    @DisplayName("Search by keyword returns matching listings")
    void search_byKeyword() {
        productService.createListing(
                createForm("Java Programming", new BigDecimal("20.00"), Condition.GOOD, "Campus"),
                seller1);
        productService.createListing(
                createForm("Python Basics", new BigDecimal("25.00"), Condition.NEW, "Campus"),
                seller1);

        Page<Product> results = productService.search(
                "Java", null, null, null, null, null, 0, 10);

        assertEquals(1, results.getTotalElements());
        assertEquals("Java Programming", results.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Search with no filters returns all available")
    void search_noFilters() {
        productService.createListing(
                createForm("Book A", new BigDecimal("10.00"), Condition.GOOD, "Campus"), seller1);
        productService.createListing(
                createForm("Book B", new BigDecimal("20.00"), Condition.NEW, "Campus"), seller2);

        Page<Product> results = productService.search(
                null, null, null, null, null, null, 0, 10);

        assertEquals(2, results.getTotalElements());
    }

    @Test
    @DisplayName("Search by price range filters correctly")
    void search_byPriceRange() {
        productService.createListing(
                createForm("Cheap", new BigDecimal("5.00"), Condition.ACCEPTABLE, "Campus"), seller1);
        productService.createListing(
                createForm("Medium", new BigDecimal("30.00"), Condition.GOOD, "Campus"), seller1);
        productService.createListing(
                createForm("Expensive", new BigDecimal("100.00"), Condition.NEW, "Campus"), seller1);

        Page<Product> results = productService.search(
                null, null, null, null, new BigDecimal("10.00"), new BigDecimal("50.00"), 0, 10);

        assertEquals(1, results.getTotalElements());
        assertEquals("Medium", results.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Search by condition filters correctly")
    void search_byCondition() {
        productService.createListing(
                createForm("New Book", new BigDecimal("50.00"), Condition.NEW, "Campus"), seller1);
        productService.createListing(
                createForm("Used Book", new BigDecimal("20.00"), Condition.GOOD, "Campus"), seller1);

        Page<Product> results = productService.search(
                null, null, null, Condition.NEW, null, null, 0, 10);

        assertEquals(1, results.getTotalElements());
        assertEquals("New Book", results.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Only AVAILABLE listings appear in search results")
    void search_onlyAvailable() {
        Product sold = productService.createListing(
                createForm("Sold Book", new BigDecimal("10.00"), Condition.GOOD, "Campus"), seller1);
        sold.setStatus(ListingStatus.SOLD);
        productRepository.save(sold);

        productService.createListing(
                createForm("Available Book", new BigDecimal("20.00"), Condition.NEW, "Campus"), seller1);

        Page<Product> results = productService.search(
                null, null, null, null, null, null, 0, 10);

        assertEquals(1, results.getTotalElements());
        assertEquals("Available Book", results.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("My listings returns all statuses for a seller")
    void findBySellerId() {
        productService.createListing(
                createForm("My Book 1", new BigDecimal("10.00"), Condition.GOOD, "Campus"), seller1);
        Product hidden = productService.createListing(
                createForm("My Book 2", new BigDecimal("20.00"), Condition.NEW, "Campus"), seller1);
        hidden.setStatus(ListingStatus.HIDDEN);
        productRepository.save(hidden);

        // Other seller
        productService.createListing(
                createForm("Other Book", new BigDecimal("15.00"), Condition.GOOD, "Campus"), seller2);

        List<Product> myListings = productService.findBySellerId(seller1.getId());

        assertEquals(2, myListings.size());
    }

    // --- Helpers ---

    private User createUser(String name, String email) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("test123"));
        user.setRole(Role.STUDENT);
        return userRepository.save(user);
    }

    private ProductForm createForm(String title, BigDecimal price,
                                    Condition condition, String campus) {
        ProductForm form = new ProductForm();
        form.setTitle(title);
        form.setPrice(price);
        form.setCondition(condition);
        form.setCampus(campus);
        return form;
    }
}
