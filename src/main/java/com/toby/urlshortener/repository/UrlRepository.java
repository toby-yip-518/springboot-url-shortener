package com.toby.urlshortener.repository;

import com.toby.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    //查 short URL
    Optional<Url> findByShortCode(String shortCode);

    //檢查是否重複
    boolean existsByShortCode(String shortCode);

}
