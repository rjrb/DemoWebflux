package com.sophos.reactive.repository;

import com.sophos.reactive.model.Empleado;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface EmpleadoRepository extends ReactiveCrudRepository<Empleado, UUID> {

	Flux<Empleado> findByCedulaContainingIgnoreCaseOrNombreContainingIgnoreCaseOrCiudadContainingIgnoreCase(String cedula, String nombre, String ciudad);

}
