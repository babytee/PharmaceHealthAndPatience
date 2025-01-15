package com.pharmacy.intelrx.pharmacy.repositories.auxilliary;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion,Long> {
   Optional<SecurityQuestion>findByQuestion(String question);
}
