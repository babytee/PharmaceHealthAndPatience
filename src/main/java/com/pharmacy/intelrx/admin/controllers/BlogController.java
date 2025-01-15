package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.auxilliary.dto.BlogRequest;
import com.pharmacy.intelrx.admin.services.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController("adminBlogController")
@RequestMapping({"/api/v1/admin/blog"})
public class BlogController {
    private final BlogService blogService;

    @PostMapping({"store"})
    public ResponseEntity<?> store(@ModelAttribute BlogRequest request) {
        return blogService.store(request);
    }


    @PatchMapping({"update/{id}"})
    public ResponseEntity<?> update(@PathVariable long id, @ModelAttribute BlogRequest request) {
        return blogService.update(id,request);
    }

    @PatchMapping({"update_status/{id}"})
    public ResponseEntity<?> updateStatus(@PathVariable long id, @ModelAttribute BlogRequest request) {

        return blogService.updateStatus(id,request);
    }

    @PatchMapping({"update_pin_to_top/{id}"})
    public ResponseEntity<?> updatePinToTop(@PathVariable long id, @ModelAttribute BlogRequest request) {

        return blogService.updatePinToTop(id,request);
    }

    @GetMapping({"{id}"})
    public ResponseEntity<?> fetchSingleCategory(@PathVariable long id) throws IOException {
        return blogService.fetchSingleCategory(id);
    }

    @DeleteMapping({"delete/{id}"})
    public ResponseEntity<?> deleteBlog(@PathVariable long id) {
        return blogService.deleteBlog(id);
    }

    @GetMapping()
    public ResponseEntity<?> allBlogs() {
        return blogService.allBlogs();
    }

    @GetMapping({"category/{categoryId}"})
    public ResponseEntity<?> filterByBlogsByCategory(@PathVariable long categoryId) throws IOException {
        return blogService.filterByBlogsByCategory(categoryId);
    }

    @GetMapping({"status/{status}"})
    public ResponseEntity<?> filterByBlogsByStatus(@PathVariable String status) throws IOException {
        return blogService.filterByBlogsByStatus(status);
    }
}
