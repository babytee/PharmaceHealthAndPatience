package com.pharmacy.intelrx.auxilliary.repositories;

import com.pharmacy.intelrx.auxilliary.models.Blog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog,Long> {
    List<Blog> findAllByBlogCategoryId(long categoryId);

    List<Blog> findAllByBlogCategoryIdAndStatus(long categoryId,String status);

    List<Blog> findAllByStatus(String status);

    //@Query("SELECT e FROM YourEntity e WHERE e.pinToTop = true")
    Optional<Blog> findByPinToTop(boolean status);

    Optional<Blog> findByIdAndPinToTop(long id,boolean status);

    List<Blog> findAllByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    List<Blog> findByTitleContainingIgnoreCaseOrderByIdDesc(String title);
}
