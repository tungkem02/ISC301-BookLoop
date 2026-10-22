/**
 * BookLoop — Main JavaScript
 * Handles: auto-dismiss alerts, form enhancements, filter UI, navbar search
 */

document.addEventListener('DOMContentLoaded', function () {

    // -----------------------------------------------------------------------
    // 1. Auto-dismiss flash alerts after 5 seconds
    // -----------------------------------------------------------------------
    const alerts = document.querySelectorAll('.alert-auto-dismiss');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 0.5s ease, max-height 0.5s ease';
            alert.style.opacity = '0';
            alert.style.maxHeight = '0';
            alert.style.overflow = 'hidden';
            alert.style.padding = '0';
            setTimeout(function () { alert.remove(); }, 500);
        }, 5000);
    });

    // -----------------------------------------------------------------------
    // 2. Confirm delete actions (extra client-side guard)
    // -----------------------------------------------------------------------
    const deleteForms = document.querySelectorAll('.delete-form');
    deleteForms.forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (!confirm('Are you sure you want to delete this listing? This cannot be undone.')) {
                e.preventDefault();
            }
        });
    });

    // -----------------------------------------------------------------------
    // 3. Image URL preview for listing form
    // -----------------------------------------------------------------------
    const imageUrlInput = document.getElementById('imageUrl');
    const imagePreview = document.getElementById('imagePreview');
    if (imageUrlInput && imagePreview) {
        imageUrlInput.addEventListener('input', function () {
            const url = this.value.trim();
            if (url) {
                imagePreview.src = url;
                imagePreview.style.display = 'block';
                imagePreview.onerror = function () {
                    imagePreview.style.display = 'none';
                };
            } else {
                imagePreview.style.display = 'none';
            }
        });
        // Trigger on load if pre-filled
        if (imageUrlInput.value.trim()) {
            imageUrlInput.dispatchEvent(new Event('input'));
        }
    }

    // -----------------------------------------------------------------------
    // 4. Filter chip active state toggle (visual enhancement)
    // -----------------------------------------------------------------------
    const filterChips = document.querySelectorAll('.filter-chip[data-filter]');
    filterChips.forEach(function (chip) {
        chip.addEventListener('click', function () {
            const group = chip.dataset.group;
            if (group) {
                document.querySelectorAll('.filter-chip[data-group="' + group + '"]')
                    .forEach(function (c) { c.classList.remove('active'); });
            }
            chip.classList.toggle('active');
        });
    });

    // -----------------------------------------------------------------------
    // 5. Navbar scroll shadow
    // -----------------------------------------------------------------------
    const navbar = document.querySelector('.bl-navbar');
    if (navbar) {
        window.addEventListener('scroll', function () {
            if (window.scrollY > 10) {
                navbar.style.boxShadow = '0 4px 20px rgba(0,0,0,0.08)';
            } else {
                navbar.style.boxShadow = '0 1px 2px rgba(0,0,0,0.05)';
            }
        });
    }

    // -----------------------------------------------------------------------
    // 6. Animate cards on scroll (intersection observer)
    // -----------------------------------------------------------------------
    const observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in-up');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    document.querySelectorAll('.book-card, .stat-card').forEach(function (card) {
        observer.observe(card);
    });

    // -----------------------------------------------------------------------
    // 7. Search form — trim whitespace before submit
    // -----------------------------------------------------------------------
    const searchForms = document.querySelectorAll('.search-form');
    searchForms.forEach(function (form) {
        form.addEventListener('submit', function () {
            const kw = form.querySelector('[name="keyword"]');
            if (kw) kw.value = kw.value.trim();
        });
    });
});
