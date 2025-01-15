package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.auxilliary.dto.*;
import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.auxilliary.models.Blog;
import com.pharmacy.intelrx.auxilliary.models.BlogCategory;
import com.pharmacy.intelrx.admin.repositories.AdminRepository;
import com.pharmacy.intelrx.auxilliary.repositories.BlogCategoryRepository;
import com.pharmacy.intelrx.auxilliary.repositories.BlogRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service("AdminBlogService")
public class BlogService {
    private final BlogCategoryRepository blogCategoryRepository;
    private final AdminRepository adminRepository;
    private final BlogRepository blogRepository;
    private final Utility utility;
    private final S3Service s3Service;

    @Value("${aws.bucket.name}")
    private String bucketName;

    public static String getTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long days = duration.toDays();
        if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        }

        long hours = duration.toHours();
        if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }

        long minutes = duration.toMinutes();
        if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }

        long seconds = duration.getSeconds();
        return seconds + (seconds == 1 ? " second ago" : " seconds ago");
    }

    public ResponseEntity<?> store(BlogRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(request.getTitle())) {
            return ResponseEntity.ok().body(new Response("failed", "title is required"));
        } else if (request.getBanner() == null || request.getBanner().isEmpty()) {
            return ResponseEntity.ok().body(new Response("failed", "banner is required"));
        } else if (utility.isNullOrEmpty(request.getContent())) {
            return ResponseEntity.ok().body(new Response("failed", "content is required"));
        } else if (utility.isNullOrEmpty(request.getEstimatedReadThroughTime())) {
            return ResponseEntity.ok().body(new Response("failed", "estimatedReadThroughTime is required"));
        } else if (utility.isNullOrEmpty(String.valueOf(request.getBlog_category_id()))) {
            return ResponseEntity.ok().body(new Response("failed", "blog_category_id is required"));
        } else if (utility.isNullOrEmpty(String.valueOf(request.getPinToTop()))) {
            return ResponseEntity.ok().body(new Response("failed", "pinToTop is required"));
        } else if (utility.isNullOrEmpty(request.getStatus())) {
            return ResponseEntity.ok().body(new Response("failed", "status is required"));
        } else {
            try {
                // Get the original file name

                // Save the file to the upload directory
                String fileName = s3Service.uploadFile(request.getBanner());
                // Process the title and article as needed (e.g., save to a database)
                // ...
                if (fileName == null || fileName.isEmpty()) {
                    return ResponseEntity.ok().body(new BlogResponse("failed", "banner is empty or need to be chnaged"));
                }

                String userEmail = authentication.getName();
                Optional<Admin> adminOptional = adminRepository.findByEmail(userEmail);
                if (!adminOptional.isPresent()) {
                    return ResponseEntity.ok().body(new Response("failed", "User Not Found"));
                }
                Admin admin = adminOptional.get();

                Optional<BlogCategory> optionalBlogCategory = blogCategoryRepository.findById(request.getBlog_category_id());
                if (!optionalBlogCategory.isPresent()) {
                    return ResponseEntity.ok().body(new Response("failed", "Category Not Found"));
                }
                BlogCategory blogCategory = optionalBlogCategory.get();
                if (request.getPinToTop() == true) {
                    Optional<Blog> optionalBlog1 = blogRepository.findByPinToTop(true);
                    if (optionalBlog1.isPresent()) {
                        //remove previous one to false
                        Blog updateBlog = optionalBlog1.get();
                        updateBlog.setPinToTop(false);
                        blogRepository.save(updateBlog);
                    }
                    //set new one to true
                    Blog blog = Blog.builder()
                            .title(request.getTitle())
                            .content(request.getContent())
                            .banner(fileName)
                            .status(request.getStatus())
                            .pinToTop(true)
                            .estimatedReadThroughTime(request.getEstimatedReadThroughTime())
                            .blogCategory(blogCategory)
                            .admin(admin)
                            .createdAt(LocalDateTime.now())
                            .build();
                    blogRepository.save(blog);
                    blog.setPinToTop(true);
                    return ResponseEntity.ok().body(new BlogResponse(blog.getId(), "success", "Post Added Successfully"));

                } else {

                    Blog blog = Blog.builder()
                            .title(request.getTitle())
                            .content(request.getContent())
                            .banner(fileName)
                            .status(request.getStatus())
                            .pinToTop(false)
                            .estimatedReadThroughTime(request.getEstimatedReadThroughTime())
                            .blogCategory(blogCategory)
                            .admin(admin)
                            .createdAt(LocalDateTime.now())
                            .build();
                    blogRepository.save(blog);

                    return ResponseEntity.ok().body(new BlogResponse(blog.getId(), "success", "Post Added Successfully"));

                }

            } catch (IOException e) {
                return ResponseEntity.ok().body(new Response("failed", "File upload failed: " + e.getMessage()));
            }
        }
    }

    public ResponseEntity<?> update(long id, BlogRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(request.getTitle())) {
            return ResponseEntity.ok().body(new Response("failed", "title is required"));
        } else if (utility.isNullOrEmpty(request.getContent())) {
            return ResponseEntity.ok().body(new Response("failed", "content is required"));
        } else if (utility.isNullOrEmpty(request.getEstimatedReadThroughTime())) {
            return ResponseEntity.ok().body(new Response("failed", "estimatedReadThroughTime is required"));
        } else if (utility.isNullOrEmpty(String.valueOf(request.getBlog_category_id()))) {
            return ResponseEntity.ok().body(new Response("failed", "blog_category_id is required"));
        } else if (utility.isNullOrEmpty(String.valueOf(request.getPinToTop()))) {
            return ResponseEntity.ok().body(new Response("failed", "pinToTop is required"));
        } else if (utility.isNullOrEmpty(request.getStatus())) {
            return ResponseEntity.ok().body(new Response("failed", "status is required"));
        } else {

            // Process the title and article as needed (e.g., save to a database)
            // ...

            String userEmail = authentication.getName();
            Optional<Admin> adminOptional = adminRepository.findByEmail(userEmail);
            if (!adminOptional.isPresent()) {
                return ResponseEntity.ok().body(new Response("failed", "User Not Found"));
            }

            Optional<Blog> optionalBlog = blogRepository.findById(request.getId());
            if (!optionalBlog.isPresent()) {
                return ResponseEntity.ok().body(new Response("failed", "Blog Post Not Found"));
            }
            Blog blog = optionalBlog.get();
            //check if true
            if (request.getPinToTop() == true) {
                Optional<Blog> optionalBlog1 = blogRepository.findByPinToTop(true);
                if (optionalBlog1.isPresent()) {
                    //remove previous one to false
                    Blog updateBlog = optionalBlog1.get();
                    updateBlog.setPinToTop(false);
                    blogRepository.save(updateBlog);
                }
//set new one to true
                blog.setPinToTop(true);
            }

            blog.setTitle(request.getTitle());
            blog.setContent(request.getContent());
            blog.setUpdatedAt(LocalDateTime.now());

            if (request.getBanner() != null) {
                try {

                    String fileName = s3Service.uploadFile(request.getBanner());

                    blog.setBanner(fileName);
                } catch (IOException e) {
                    //return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
                    return ResponseEntity.ok().body(new Response("failed", "File upload failed: " + e.getMessage()));
                }
            }

            blog.setStatus(request.getStatus());
            blog.setEstimatedReadThroughTime(request.getEstimatedReadThroughTime());

            Optional<BlogCategory> optionalBlogCategory = blogCategoryRepository.findById(request.getBlog_category_id());
            if (!optionalBlogCategory.isPresent()) {
                return ResponseEntity.ok().body(new Response("failed", "Category Not Found"));
            } else {
                BlogCategory blogCategory = optionalBlogCategory.get();

                blog.setBlogCategory(blogCategory);
            }

            Admin admin = adminOptional.get();
            blog.setAdmin(admin);
            blogRepository.save(blog);
            return ResponseEntity.ok().body(new Response("success", "Post Updated Successfully"));

        }
    }

    public ResponseEntity<?> updateStatus(long id, BlogRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(new CustomResponse("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(String.valueOf(id))) {
            return ResponseEntity.ok().body(new Response("failed", "id is required"));
        } else if (utility.isNullOrEmpty(request.getStatus())) {
            return ResponseEntity.ok().body(new Response("failed", "status is required"));
        } else {


            Optional<Blog> blogOptional = blogRepository.findById(id);

            if (!blogOptional.isPresent()) {
                return ResponseEntity.ok().body(new Response("failed", "Blog Post Not Found"));
            }

            Blog blog = blogOptional.get();
            blog.setStatus(request.getStatus());
            blogRepository.save(blog);

            return ResponseEntity.ok().body(new BlogCategoryResponse("success", "Post Status Updated Successfully To " + request.getStatus()));

        }
    }

    public ResponseEntity<?> updatePinToTop(long id, BlogRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(new CustomResponse("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(String.valueOf(id))) {
            return ResponseEntity.ok().body(new Response("failed", "id is required"));
        } else if (utility.isNullOrEmpty(String.valueOf(request.getPinToTop()))) {
            return ResponseEntity.ok().body(new Response("failed", "pinToTop is required"));
        } else {


            Optional<Blog> blogOptional = blogRepository.findById(id);


            if (!blogOptional.isPresent()) {
                return ResponseEntity.ok().body(new Response("failed", "Blog Post Not Found"));
            }

            Blog blog = blogOptional.get();
            if (request.getPinToTop() == true) {
                Optional<Blog> optionalBlog1 = blogRepository.findByPinToTop(true);
                if (optionalBlog1.isPresent()) {
                    //remove previous one to false
                    Blog updateBlog = optionalBlog1.get();
                    updateBlog.setPinToTop(false);
                    blogRepository.save(updateBlog);
                }
//set new one to true
                blog.setPinToTop(true);
                blogRepository.save(blog);
            }




            return ResponseEntity.ok().body(new BlogCategoryResponse("success", "Post Pin To Top Successfully"));

        }
    }

    public ResponseEntity<?> allBlogs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        }

        List<Blog> blogList = blogRepository.findAll();
        if (blogList.isEmpty()) {
            return ResponseEntity.ok().body(new Response("failed", "blog post does not exist or the id Not Found"));
        }

        List<BlogResponse> blogResponseList = (List) blogList.stream().map((address) -> {
            try {
                return this.mapToBlogResponse(address);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        CustomSingleResponse<BlogResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(blogResponseList);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> fetchSingleCategory(long id) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(String.valueOf(id))) {
            return ResponseEntity.ok().body(new Response("failed", "is is required"));
        }

        Optional<Blog> optionalBlog = blogRepository.findById(id);
        if (!optionalBlog.isPresent()) {
            return ResponseEntity.ok().body(new Response("failed", "category does not exist or the id Not Found"));
        }

        Blog blog = optionalBlog.get();

        BlogResponse blogResponse = mapToBlogResponse(blog);

        CustomSingleResponse<BlogCategoryResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(blogResponse);
        return ResponseEntity.ok(response);
    }

    private BlogResponse mapToBlogResponse(Blog blog) throws IOException {

        BlogResponse blogResponse = new BlogResponse();
        blogResponse.setId(blog.getId());
        blogResponse.setTitle(blog.getTitle());

        S3Service.FetchedImage fetchedImage = s3Service.fetchImage(blog.getBanner()); // Replace "your_image_name.jpg" with the actual image name
        String banner = fetchedImage.getImageUrl();

        blogResponse.setBanner(banner);

        BlogCategoryResponse blogCategoryResponse = new BlogCategoryResponse();
        blogCategoryResponse.setName(blog.getBlogCategory().getName());
        blogCategoryResponse.setId(blog.getBlogCategory().getId());

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setId(blog.getAdmin().getId());

        String fullname = blog.getAdmin().getLastname() + " " + blog.getAdmin().getFirstname();

        registerRequest.setName(fullname);

        blogResponse.setPostedBy(registerRequest);
        blogResponse.setCategory(blogCategoryResponse);
        blogResponse.setStatus(blog.getStatus());
        blogResponse.setEstimatedReadThroughTime(blog.getEstimatedReadThroughTime());
        blogResponse.setPinToTop(blog.isPinToTop());
        blogResponse.setDescription(blog.getContent());
        blogResponse.setCreatedAt(getTimeAgo(blog.getCreatedAt()));

        return blogResponse;

    }

    public ResponseEntity<?> deleteBlog(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
        } else if (utility.isNullOrEmpty(String.valueOf(id))) {
            return ResponseEntity.ok().body(new Response("failed", "id is required"));
        }

        Optional<Blog> optionalBlog = blogRepository.findById(id);
        if (!optionalBlog.isPresent()) {
            return ResponseEntity.ok().body(new Response("failed", "blog does not exist or the id Not Found"));
        }

        Blog blog = optionalBlog.get();
        blog.setBlogCategory(null);
        blog.setAdmin(null);
        blogRepository.save(blog);
        blogRepository.delete(blog);
        return ResponseEntity.ok().body(new Response("success", "deleted successfully"));
    }

    public ResponseEntity<?> filterByBlogsByCategory(long categoryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(new Response("failed", "You are unauthorized"));
        }

        List<Blog> blogList;

        if (utility.isNullOrEmpty(String.valueOf(categoryId))) {
            blogList = blogRepository.findAll();
        } else {
            blogList = blogRepository.findAllByBlogCategoryId(categoryId);
        }

        if (blogList.isEmpty()) {
            return ResponseEntity.ok().body(new Response("failed", "blog post does not exist or the id Not Found"));
        }

        List<BlogResponse> blogResponseList = (List) blogList.stream().map((address) -> {
            try {
                return this.mapToBlogResponse(address);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        CustomSingleResponse<BlogResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(blogResponseList);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> filterByBlogsByStatus(String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(new Response("failed", "You are unauthorized"));
        }

        if (utility.isNullOrEmpty(String.valueOf(status))) {
            return ResponseEntity.badRequest().body(new Response("failed", "status is required"));
        }

        List<Blog> blogList = blogRepository.findAllByStatus(status);

        if (blogList.isEmpty()) {
            return ResponseEntity.ok().body(new BlogResponse("failed", "blog post does not exist or the id Not Found"));
        }

        List<BlogResponse> blogResponseList = (List) blogList.stream().map((address) -> {
            try {
                return this.mapToBlogResponse(address);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        CustomSingleResponse<BlogResponse> response = new CustomSingleResponse<>();

        response.setStatus("success");
        response.setData(blogResponseList);
        return ResponseEntity.ok(response);
    }


}
