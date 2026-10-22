package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.ProductForm;
import com.bookloop.entity.Condition;
import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.Product;
import com.bookloop.entity.User;
import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for authenticated seller operations:
 * - My Listings
 * - Create listing
 * - Edit listing
 * - Delete listing
 *
 * All operations verify that the authenticated user owns the resource
 * before performing any modifications.
 */
@Controller
@RequestMapping("/my-listings")
public class ListingController {

    private final ProductService productService;
    private final AuthenticatedUserResolver authResolver;

    public ListingController(ProductService productService, AuthenticatedUserResolver authResolver) {
        this.productService = productService;
        this.authResolver = authResolver;
    }

    // --- My Listings ---

    @GetMapping
    public String myListings(Model model) {
        User user = authResolver.getCurrentUser();
        List<Product> listings = productService.findBySellerId(user.getId());
        model.addAttribute("listings", listings);
        return "listings/my-listings";
    }

    // --- Create Listing ---

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("conditions", Condition.values());
        model.addAttribute("statuses", ListingStatus.values());
        model.addAttribute("isEdit", false);
        return "listings/form";
    }

    @PostMapping("/new")
    public String createListing(
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("statuses", ListingStatus.values());
            model.addAttribute("isEdit", false);
            return "listings/form";
        }

        User user = authResolver.getCurrentUser();
        Product saved = productService.createListing(form, user);
        redirectAttributes.addFlashAttribute("successMessage",
                "Listing \"" + saved.getTitle() + "\" created successfully!");
        return "redirect:/my-listings";
    }

    // --- Edit Listing ---

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            User user = authResolver.getCurrentUser();

            // Verify ownership before showing edit form
            if (!product.getSeller().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "You are not authorized to edit this listing.");
                return "redirect:/my-listings";
            }

            // Pre-populate the form with current values
            ProductForm form = new ProductForm();
            form.setTitle(product.getTitle());
            form.setDescription(product.getDescription());
            form.setPrice(product.getPrice());
            form.setCondition(product.getCondition());
            form.setCategory(product.getCategory());
            form.setAuthor(product.getAuthor());
            form.setPublisher(product.getPublisher());
            form.setEdition(product.getEdition());
            form.setIsbn(product.getIsbn());
            form.setCourseCode(product.getCourseCode());
            form.setImageUrl(product.getImageUrl());
            form.setDefectNotes(product.getDefectNotes());
            form.setCampus(product.getCampus());
            form.setStatus(product.getStatus());

            model.addAttribute("productForm", form);
            model.addAttribute("productId", id);
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("statuses", ListingStatus.values());
            model.addAttribute("isEdit", true);
            return "listings/form";

        } catch (ListingNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Listing not found.");
            return "redirect:/my-listings";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateListing(
            @PathVariable Long id,
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("statuses", ListingStatus.values());
            model.addAttribute("isEdit", true);
            return "listings/form";
        }

        try {
            User user = authResolver.getCurrentUser();
            Product updated = productService.updateListing(id, form, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Listing \"" + updated.getTitle() + "\" updated successfully!");
            return "redirect:/my-listings";

        } catch (ListingNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Listing not found.");
            return "redirect:/my-listings";
        } catch (UnauthorizedOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my-listings";
        }
    }

    // --- Delete Listing ---

    /**
     * Shows a confirmation page before deletion.
     */
    @GetMapping("/{id}/delete")
    public String showDeleteConfirmation(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            User user = authResolver.getCurrentUser();

            if (!product.getSeller().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "You are not authorized to delete this listing.");
                return "redirect:/my-listings";
            }

            model.addAttribute("product", product);
            return "listings/delete-confirm";

        } catch (ListingNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Listing not found.");
            return "redirect:/my-listings";
        }
    }

    /**
     * Processes confirmed deletion via POST.
     */
    @PostMapping("/{id}/delete")
    public String deleteListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = authResolver.getCurrentUser();
            productService.deleteListing(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Listing deleted successfully.");

        } catch (ListingNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Listing not found.");
        } catch (UnauthorizedOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/my-listings";
    }
}
