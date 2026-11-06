package com.example.demo.repositories;

import com.example.demo.models.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Integer> {
    Optional<Imagen> findTopByProductoIdOrderByIdDesc(Integer productoId);
}