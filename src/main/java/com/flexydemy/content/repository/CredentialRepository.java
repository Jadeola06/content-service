package com.flexydemy.content.repository;

import com.flexydemy.content.model.GoogleCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CredentialRepository extends JpaRepository<GoogleCredential, String> {
    Optional<GoogleCredential> findByUserId(String id);
}
