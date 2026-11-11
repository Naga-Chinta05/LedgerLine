package com.ledgerline.repository;

import com.ledgerline.entity.User;
import com.ledgerline.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);

    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    List<User> findByRole(UserRole role);

    List<User> findByEnabled(boolean enabled);
}
