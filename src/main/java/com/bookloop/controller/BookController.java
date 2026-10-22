package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.entity.Condition;
import com.bookloop.entity.Product;
import com.bookloop.entity.User;
import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Public-facing controller for browsing and searching book listings.
 */
@Controller
@RequestMapping("/books")
public class BookController {

    private final ProductService productService;
    private final AuthenticatedUserResolver authResolver;

    public BookController(ProductService productService, AuthenticatedUserResolver authResolver) {
        this.productService = productService;
        this.authResolver = authResolver;
    }

    /**
     * Lists all available books with optional search filters.
     * All parameters are optional and can be combined freely.
     */
    @GetMapping
    public String listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        // Convert condition string to enum safely
        Condition conditionEnum = null;
        if (condition != null && !condition.trim().isEmpty()) {
            try {
                conditionEnum = Condition.valueOf(condition.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Invalid condition value — treat as no filter
            }
        }

        Page<Product> productPage = productService.search(
                keyword, category, campus, conditionEnum, minPrice, maxPrice, page, size);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("categories", productService.findAvailableCategories());
        model.addAttribute("campuses", productService.findAvailableCampuses());
        model.addAttribute("conditions", Condition.values());

        // Preserve filter values so the UI can show them as selected
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedCampus", campus);
        model.addAttribute("selectedCondition", condition);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "books/list";
    }

    /**
     * Shows full details for a single listing.
     */
    @GetMapping("/{id}")
    public String showBook(@PathVariable Long id, Model model) {
        try {
            Product product = productService.findById(id);

            // If the authenticated user owns this listing, allow edit/manage links
            boolean isOwner = false;
            if (authResolver.isAuthenticated()) {
                User currentUser = authResolver.getCurrentUser();
                isOwner = product.getSeller().getId().equals(currentUser.getId());
            }

            model.addAttribute("product", product);
            model.addAttribute("isOwner", isOwner);
            return "books/detail";

        } catch (ListingNotFoundException e) {
            return "redirect:/books?notFound=true";
        }
    }
}
