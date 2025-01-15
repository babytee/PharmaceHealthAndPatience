package com.pharmacy.intelrx.auxilliary.LandingPages;

import com.pharmacy.intelrx.auxilliary.dto.*;
import com.pharmacy.intelrx.auxilliary.models.Blog;
import com.pharmacy.intelrx.auxilliary.models.BlogCategory;
import com.pharmacy.intelrx.auxilliary.repositories.BlogCategoryRepository;
import com.pharmacy.intelrx.auxilliary.repositories.BlogRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service("BlogService")
public class BlogService {
    private final BlogRepository blogRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final Utility utility;
    private final S3Service s3Service;


    public ResponseEntity<?> allBlogs() {

        List<Blog> blogList = blogRepository.findAllByStatus("published");
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

    public ResponseEntity<?> latestBlogs(Integer pageSize) {
        if(pageSize==null){
            return ResponseEntity.ok().body(new Response("failed","pageSize is required"));
        }

        List<Blog> blogList = blogRepository.findAllByStatusOrderByCreatedAtDesc("published", PageRequest.of(0, pageSize, Sort.by("createdAt").descending()));
        if (blogList.isEmpty()) {
            return ResponseEntity.ok().body(new Response("failed", "blog post does not exist or the id Not Found"));
        }

        List<BlogResponse> blogResponseList = blogList.stream().map((address) -> {
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

    public ResponseEntity<?> allCategories() {

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

    public ResponseEntity<?> searchBlogs(String searchQuery) {
        List<Blog> blogList = blogRepository.findByTitleContainingIgnoreCaseOrderByIdDesc(searchQuery);
        if (blogList.isEmpty()) {
            return ResponseEntity.ok().body(new Response("failed", "No blog post found for the search query: " + searchQuery));
        }

        List<BlogResponse> blogResponseList = blogList.stream().map((blog) -> {
            try {
                return this.mapToBlogResponse(blog);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        CustomSingleResponse<BlogResponse> response = new CustomSingleResponse<>();
        response.setStatus("success");
        response.setData(blogResponseList);
        return ResponseEntity.ok(response);
    }

    private BlogCategoryResponse mapToCategoryResponse(BlogCategory category) {

        BlogCategoryResponse categoryResponse = new BlogCategoryResponse();
        categoryResponse.setId(category.getId());
        categoryResponse.setName(category.getName());
        return categoryResponse;

    }


    public ResponseEntity<?> fetchSingleBlog(long id) throws IOException {

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

    public ResponseEntity<?> fetchBlogPinToTop() throws IOException {

        Optional<Blog> optionalBlog = blogRepository.findByPinToTop(true);
        if (!optionalBlog.isPresent()) {
            return ResponseEntity.ok().body(new Response("failed", "blog post does not exist or the id Not Found"));
        }

        Blog blog = optionalBlog.get();
        BlogResponse blogResponse = mapToBlogResponse(blog);

        CustomSingleResponse<BlogResponse> response = new CustomSingleResponse<>();

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

        String postedDate = getTimeAgo(blog.getCreatedAt());

        blogResponse.setCreatedAt(postedDate);

        return blogResponse;

    }

    public static String getTimeAgo(LocalDateTime createdAt) {
        if(createdAt==null){
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

    public ResponseEntity<?> filterByBlogsByCategory(long categoryId) {

        List<Blog> blogList;

        if (utility.isNullOrEmpty(String.valueOf(categoryId))) {
            return ResponseEntity.ok().body(new Response("failed", "categoryId Not Found"));
        } else if (categoryId==0) {
            blogList = blogRepository.findAllByStatus("published");
        }else{
            blogList = blogRepository.findAllByBlogCategoryIdAndStatus(categoryId,"published");
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

}
