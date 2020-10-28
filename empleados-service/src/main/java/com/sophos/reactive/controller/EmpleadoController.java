package com.sophos.reactive.controller;

import com.sophos.reactive.model.Empleado;
import com.sophos.reactive.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/empleados")
@CrossOrigin
public class EmpleadoController {

	private final EmpleadoService empleadoService;

	@Autowired
	public EmpleadoController(EmpleadoService empleadoService) {
		this.empleadoService = empleadoService;
	}

	@GetMapping("")
	public Flux<Empleado> getAll() {
		return empleadoService.getAll();
	}

	@GetMapping("/{codigo}")
	public Mono<Empleado> get(@PathVariable UUID codigo) {
		return empleadoService.get(codigo);
	}

	@PostMapping("")
	public Mono<Empleado> post(@RequestBody Empleado request) {
		return empleadoService.post(request);
	}

	@PutMapping("/{codigo}")
	public Mono<Empleado> put(@PathVariable UUID codigo, @RequestBody Empleado request) {
		return empleadoService.put(codigo, request);
	}

	@DeleteMapping("/{codigo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> delete(@PathVariable UUID codigo) {
		return empleadoService.delete(codigo);
	}

	@GetMapping("/buscar/{query}")
	public Flux<Empleado> search(@PathVariable String query) {
		return empleadoService.search(query);
	}

}
