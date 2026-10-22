package com.bookloop.controller;

import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.Product;
import com.bookloop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller for the BookLoop homepage.
 */
@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Renders the homepage with stats and recent book listings.
     */
    @GetMapping("/")
    public String home(Model model) {
        List<Product> recentListings = productService.findRecentAvailable();
        long totalListings = productService.countAvailable();

        model.addAttribute("recentListings", recentListings);
        model.addAttribute("totalListings", totalListings);

        return "home";
    }
}
