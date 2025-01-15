package com.pharmacy.intelrx.auxilliary.repositories;

import com.pharmacy.intelrx.auxilliary.models.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FaqRepository extends JpaRepository<Faq,Long> {
    List<Faq> findAllByUserType(String userType);

}
