package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.SecurityQuestionsAnswer;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityQuestionsAnswerRepository extends JpaRepository<SecurityQuestionsAnswer, Long> {
    Optional<SecurityQuestionsAnswer> findByIntelRxIdAndUser(String intelRxId, User user);

    Optional<SecurityQuestionsAnswer> findBySecurityQuestionOneAndAnswerOneAndUser(
            SecurityQuestion SecurityQuestion, String answer, User user
    );

    Optional<SecurityQuestionsAnswer> findBySecurityQuestionOneAndUser(
            SecurityQuestion SecurityQuestion, User user
    );
}
