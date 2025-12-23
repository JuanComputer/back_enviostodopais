package com.sanchez.Envios.Repositories;


import com.sanchez.Envios.Models.Informes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface InformesRepository extends JpaRepository<Informes, UUID> {
}