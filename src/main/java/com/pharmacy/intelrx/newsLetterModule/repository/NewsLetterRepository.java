package com.pharmacy.intelrx.newsLetterModule.repository;

import com.pharmacy.intelrx.newsLetterModule.models.NewsLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsLetterRepository extends JpaRepository<NewsLetter,Long> {
}
