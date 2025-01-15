package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.auxilliary.dto.BlogCategoryRequest;
import com.pharmacy.intelrx.admin.services.BlogCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController("adminBlogCategory")
@RequestMapping({"/api/v1/admin/blog_category"})
public class BlogCategoryController {

    private final BlogCategoryService blogCategoryService;

    @PostMapping({"store"})
    public ResponseEntity<?> store(@RequestBody BlogCategoryRequest request) {
        return blogCategoryService.store(request);
    }

    @PatchMapping({"update/{id}"})
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody BlogCategoryRequest request) {
        return blogCategoryService.update(id,request);
    }

    @GetMapping({"{id}"})
    public ResponseEntity<?> fetchSingleCategory(@PathVariable long id) {
        return blogCategoryService.fetchSingleCategory(id);
    }

    @DeleteMapping({"delete/{id}"})
    public ResponseEntity<?> deleteCategory(@PathVariable long id) {
        return blogCategoryService.deleteCategory(id);
    }

    @GetMapping()
    public ResponseEntity<?> allCategories() {
        return blogCategoryService.allCategories();
    }
}
