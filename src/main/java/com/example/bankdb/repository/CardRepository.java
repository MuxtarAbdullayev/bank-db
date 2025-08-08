package com.example.bankdb.repository;

import com.example.bankdb.model.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByLinkedAccount_Id(Long accountId);

    long countByLinkedAccount_Id(Long accountId);
}
