package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.auxilliary.dto.BlogCategoryRequest;
import com.pharmacy.intelrx.auxilliary.dto.BlogCategoryResponse;
import com.pharmacy.intelrx.auxilliary.dto.CustomSingleResponse;
import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.auxilliary.models.BlogCategory;
import com.pharmacy.intelrx.admin.repositories.AdminRepository;
import com.pharmacy.intelrx.auxilliary.repositories.BlogCategoryRepository;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("AdminBlogCategoryService")
public class BlogCategoryService {
    private final BlogCategoryRepository blogCategoryRepository;
    private final AdminRepository adminRepository;
    private final Utility utility;

    public ResponseEntity<?> store(BlogCategoryRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(request.getName())) {
            return ResponseEntity.ok().body(new Response("failed", "name is required"));
        }

        BlogCategory blogCategory = BlogCategory.builder().name(request.getName()).build();
        blogCategoryRepository.save(blogCategory);
        return ResponseEntity.ok().body(new Response("success", "Category Added Successfully"));
    }

    public ResponseEntity<?> update(long id, BlogCategoryRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(request.getName())) {
            return ResponseEntity.ok().body(new Response("failed", "name is required"));
        } else if (utility.isNullOrEmpty(String.valueOf(id))) {
            return ResponseEntity.ok().body(new Response("failed", "is is required"));
        } else {

            Optional<BlogCategory> optionalBlogCategory = blogCategoryRepository.findById(id);
            if (!optionalBlogCategory.isPresent()) {
                return ResponseEntity.ok().body(new Response("failed", "category does not exist or the id Not Found"));
            }

            BlogCategory blogCategory = optionalBlogCategory.get();
            blogCategory.setName(request.getName());
            blogCategoryRepository.save(blogCategory);
            return ResponseEntity.ok().body(new Response("success", "Category Is Updated Successfully"));
        }
    }

    public ResponseEntity<?> allCategories() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        }

        List<BlogCategory> categoryList = blogCategoryRepository.findAll();
        if (categoryList.isEmpty()) {
            return ResponseEntity.ok().body(new Response("failed", "category does not exist or the id Not Found"));
        }

        List<BlogCategoryResponse> categoryResponseList = (List) categoryList.stream().map((address) -> {
            return this.mapToCategoryResponse(address);
        }).collect(Collectors.toList());

        CustomSingleResponse<BlogCategoryResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(categoryResponseList);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> fetchSingleCategory(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(String.valueOf(id))) {
            return ResponseEntity.ok().body(new Response("failed", "is is required"));
        }

        Optional<BlogCategory> optionalBlogCategory = blogCategoryRepository.findById(id);
        if (!optionalBlogCategory.isPresent()) {
            return ResponseEntity.ok().body(new Response("failed", "category does not exist or the id Not Found"));
        }

        BlogCategory blogCategory = optionalBlogCategory.get();

        BlogCategoryResponse blogCategoryResponse = mapToCategoryResponse(blogCategory);

        CustomSingleResponse<BlogCategoryResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(blogCategoryResponse);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> deleteCategory(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(String.valueOf(id))) {
            return ResponseEntity.ok().body(new Response("failed", "id is required"));
        }

        Optional<BlogCategory> optionalBlogCategory = blogCategoryRepository.findById(id);
        if (!optionalBlogCategory.isPresent()) {
            return ResponseEntity.ok().body(new Response("failed", "category does not exist or the id Not Found"));
        }

        BlogCategory blogCategory = optionalBlogCategory.get();
        blogCategoryRepository.delete(blogCategory);
        return ResponseEntity.ok().body(new Response("success", "deleted successfully"));
    }

    private BlogCategoryResponse mapToCategoryResponse(BlogCategory category) {

        BlogCategoryResponse categoryResponse = new BlogCategoryResponse();
        categoryResponse.setId(category.getId());
        categoryResponse.setName(category.getName());
        return categoryResponse;

    }
}
