package com.pharmacy.intelrx.auxilliary.controllers.landingPageController;


import com.pharmacy.intelrx.auxilliary.LandingPages.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController("BlogController")
@RequestMapping({"/api/v1/blog"})
public class BlogController {
    private final BlogService blogService;

    @GetMapping()
    public ResponseEntity<?> allBlogs() {
        return blogService.allBlogs();
    }

    @GetMapping({"categories"})
    public ResponseEntity<?> allCategories() {
        return blogService.allCategories();
    }
    @GetMapping({"{id}"})
    public ResponseEntity<?> fetchSingleBlog(@PathVariable long id) throws IOException {
        return blogService.fetchSingleBlog(id);
    }

    @GetMapping({"category/{categoryId}"})
    public ResponseEntity<?> filterByBlogsByCategory(@PathVariable long categoryId) throws IOException {
        return blogService.filterByBlogsByCategory(categoryId);
    }

    @GetMapping({"latest/{pageSize}"})
    public ResponseEntity<?> latestBlogs(@PathVariable Integer pageSize) throws IOException {
        return blogService.latestBlogs(pageSize);
    }

    @GetMapping({"search_query/{searchQuery}"})
    public ResponseEntity<?> searchBlogs(@PathVariable String searchQuery) throws IOException {
        return blogService.searchBlogs(searchQuery);
    }

    @GetMapping({"pin_to_top"})
    public ResponseEntity<?> fetchBlogPinToTop() throws IOException {
        return blogService.fetchBlogPinToTop();
    }

}
