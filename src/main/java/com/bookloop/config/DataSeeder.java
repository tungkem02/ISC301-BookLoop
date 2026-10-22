package com.bookloop.config;

import com.bookloop.entity.*;
import com.bookloop.repository.ProductRepository;
import com.bookloop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds the database with fictional demonstration data on first run.
 * Only active when the "dev" profile is enabled AND when no users exist yet.
 *
 * Usage: run with -Dspring.profiles.active=dev
 *
 * ALL DATA BELOW IS FICTIONAL AND FOR DEMONSTRATION PURPOSES ONLY.
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      ProductRepository productRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Only seed if the database is empty
        if (userRepository.count() > 0) {
            log.info("Database already has data — skipping seed.");
            return;
        }

        log.info("Seeding database with demonstration data...");

        // --- Create demo users (all passwords are "password123") ---
        User alice = createUser("Alice Johnson", "alice@university.edu",
                "State University", "Main Campus", "+1-555-0101");
        User bob = createUser("Bob Martinez", "bob@university.edu",
                "State University", "North Campus", "+1-555-0102");
        User carol = createUser("Carol Chen", "carol@university.edu",
                "Tech Institute", "Downtown Campus", null);

        // --- Create demo product listings ---
        createProduct("Head First Java", "Kathy Sierra, Bert Bates",
                "O'Reilly Media", "2nd", "978-0596009205", "CS101",
                "Computer Science", "Textbook",
                new BigDecimal("35.00"), Condition.GOOD,
                "Main Campus Library",
                "Some highlighting in chapters 3-5. Cover has minor wear.",
                "https://m.media-amazon.com/images/I/81U5RkFRGlL._SL1500_.jpg",
                alice);

        createProduct("Introduction to Algorithms", "Thomas H. Cormen et al.",
                "MIT Press", "3rd", "978-0262033848", "CS201",
                "Computer Science", "Reference textbook, very thorough coverage of algorithm design.",
                new BigDecimal("55.00"), Condition.LIKE_NEW,
                "North Campus Bookstore",
                null,
                "https://m.media-amazon.com/images/I/61ZXunAMYyL._SL1233_.jpg",
                bob);

        createProduct("Principles of Marketing", "Philip Kotler",
                "Pearson", "17th", "978-0134492513", "MKT201",
                "Business", "Used for Marketing 201. All pages intact.",
                new BigDecimal("42.50"), Condition.ACCEPTABLE,
                "Student Center",
                "Pen markings on some pages. Spine slightly creased.",
                "https://m.media-amazon.com/images/I/61rxNnGr+LL._SL1000_.jpg",
                carol);

        createProduct("Calculus: Early Transcendentals", "James Stewart",
                "Cengage Learning", "8th", "978-1285741550", "MATH101",
                "Mathematics", "Standard calculus textbook for first and second year students.",
                new BigDecimal("48.00"), Condition.GOOD,
                "Downtown Campus Café",
                "Previous owner's name on the first page.",
                "https://m.media-amazon.com/images/I/51byHhcRxAL._SL1000_.jpg",
                carol);

        createProduct("Operating System Concepts", "Abraham Silberschatz",
                "Wiley", "10th", "978-1119800361", "CS301",
                "Computer Science", "The dinosaur book. Comprehensive coverage of OS design.",
                new BigDecimal("60.00"), Condition.NEW,
                "Main Campus Library",
                null,
                "https://m.media-amazon.com/images/I/81RrFzjbdFL._SL1500_.jpg",
                alice);

        createProduct("Microeconomics", "N. Gregory Mankiw",
                "Cengage Learning", "9th", "978-1337617406", "ECON101",
                "Economics", "Great introductory economics textbook.",
                new BigDecimal("38.00"), Condition.LIKE_NEW,
                "North Campus Bookstore",
                null,
                null,
                bob);

        createProduct("Organic Chemistry", "Paula Y. Bruice",
                "Pearson", "8th", "978-0134042282", "CHEM201",
                "Chemistry", "Orgo textbook with study guide and practice problems.",
                new BigDecimal("50.00"), Condition.GOOD,
                "Science Building Lobby",
                "Some tabs stuck on key chapters. Clean interior.",
                null,
                alice);

        createProduct("Database System Concepts", "Abraham Silberschatz",
                "McGraw Hill", "7th", "978-0078022159", "CS305",
                "Computer Science", "Covers relational model, SQL, and database design.",
                new BigDecimal("45.00"), Condition.ACCEPTABLE,
                "Student Center",
                "Water stain on back cover. Pages are clean and intact.",
                null,
                bob);

        log.info("Seed data created: {} users, {} products.",
                userRepository.count(), productRepository.count());
    }

    private User createUser(String fullName, String email,
                            String university, String campus, String phone) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setUniversity(university);
        user.setCampus(campus);
        user.setPhone(phone);
        user.setRole(Role.STUDENT);
        return userRepository.save(user);
    }

    private void createProduct(String title, String author, String publisher,
                                String edition, String isbn, String courseCode,
                                String category, String description,
                                BigDecimal price, Condition condition,
                                String campus, String defectNotes,
                                String imageUrl, User seller) {
        Product p = new Product();
        p.setTitle(title);
        p.setAuthor(author);
        p.setPublisher(publisher);
        p.setEdition(edition);
        p.setIsbn(isbn);
        p.setCourseCode(courseCode);
        p.setCategory(category);
        p.setDescription(description);
        p.setPrice(price);
        p.setCondition(condition);
        p.setCampus(campus);
        p.setDefectNotes(defectNotes);
        p.setImageUrl(imageUrl);
        p.setStatus(ListingStatus.AVAILABLE);
        p.setSeller(seller);
        productRepository.save(p);
    }
}
