package com.example.demo.repositories;

import com.example.demo.entity.Part;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface PartRepository extends CrudRepository <Part,Long> {
    // SQL injection prevention: uses parameter binding via JPQL
    @Query("SELECT p FROM Part p WHERE p.name LIKE %?1%")
    public List<Part> search(String keyword);
}
