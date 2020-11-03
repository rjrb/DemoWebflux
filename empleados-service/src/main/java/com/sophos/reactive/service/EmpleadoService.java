package com.sophos.reactive.service;

import com.sophos.reactive.beans.LogRegistry;
import com.sophos.reactive.model.Empleado;
import com.sophos.reactive.repository.EmpleadoRepository;
import com.sophos.reactive.utils.Utilidades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpleadoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmpleadoService.class);
	private static final String RESPONSABLE = "Webflux";

	private final EmpleadoRepository empleadoRepository;
	private final DatabaseClient databaseClient;
	private final RabbitService rabbitService;

	public EmpleadoService(EmpleadoRepository empleadoRepository, DatabaseClient databaseClient, RabbitService rabbitService) {
		this.empleadoRepository = empleadoRepository;
		this.databaseClient = databaseClient;
		this.rabbitService = rabbitService;
	}

	public Flux<Empleado> getAll() {
		return empleadoRepository.findAll();
	}

	public Mono<Empleado> get(UUID codigo) {
		return empleadoRepository.findById(codigo)
			.flatMap(empleado -> queueLog("GET", empleado))
			.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado")))
		;
	}

	@Transactional
	public Mono<Empleado> post(Empleado request) {
		return Mono.just(request)
			.map(this::mapAndValidateRequest)
			.flatMap(empleado -> findByCedula(empleado.getCedula()))
			.flatMap(empleado -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe la cédula '" + request.getCedula() + "'")))
			.cast(Empleado.class)
			.switchIfEmpty(empleadoRepository.save(request))
			.flatMap(empleado -> queueLog("POST", empleado))
		;
	}

	@Transactional
	public Mono<Empleado> put(UUID codigo, Empleado request) {
		return Mono.just(request)
			.map(this::mapAndValidateRequest)
			.flatMap(empleado -> findByCedula(empleado.getCedula()))
			.filter(empleado -> !codigo.equals(empleado.getCodigo()))
			.flatMap(empleado -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe la cédula '" + request.getCedula() + "'")))
			.switchIfEmpty(empleadoRepository.findById(codigo))
			.flatMap(empleado -> {
				request.setCodigo(codigo);
				return empleadoRepository.save(request);
			})
			.flatMap(empleado -> queueLog("PUT", empleado))
			.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado")))
		;
	}

	@Transactional
	public Mono<Void> delete(UUID codigo) {
		return empleadoRepository.findById(codigo)
			.flatMap(empleado -> queueLog("DELETE", empleado))
			.flatMap(empleadoRepository::delete)
		;
	}

	public Flux<Empleado> search(String query) {
		return empleadoRepository.findByCedulaContainingIgnoreCaseOrNombreContainingIgnoreCaseOrCiudadContainingIgnoreCase(query, query, query);
	}

	private Mono<Empleado> findByCedula(String cedula) {
		return databaseClient
			.select()
			.from(Empleado.class)
			.matching(Criteria.where("cedula").is(cedula))
			.orderBy(Sort.Order.asc("cedula"))
			.as(Empleado.class)
			.one()
		;
	}

	private Empleado mapAndValidateRequest(Empleado request) {
		Utilidades.validarRequest(request);

		request.setCedula(request.getCedula().strip());
		request.setNombre(Utilidades.capitalize(request.getNombre()));
		request.setEdad(request.getEdad());
		request.setCiudad(Utilidades.capitalize(request.getCiudad()));

		return request;
	}

	private Mono<Empleado> queueLog(String metodo, Empleado empleado) {
		return Mono.just(empleado)
			.map(empleadoMono -> mapLogRegistry(metodo, empleadoMono))
			.flatMapMany(rabbitService::send)
			.doOnError(error -> LOGGER.error("Error encolando el mensaje del log", error))
			.doOnNext(result -> LOGGER.info("Mensaje enviado -> Cola: {} - Tipo: {} - ID: {}", result.getOutboundMessage().getRoutingKey(), metodo, empleado.getCodigo()))
			.flatMap(outboundMessageResult -> Mono.just(empleado))
			.single()
		;
	}

	private LogRegistry mapLogRegistry(String metodo, Empleado empleado) {
		final LogRegistry logRegistry = new LogRegistry();
		logRegistry.setFecha(LocalDateTime.now());
		logRegistry.setResponsable(RESPONSABLE);
		logRegistry.setMetodo(metodo);
		logRegistry.setCodigo(empleado.getCodigo().toString());
		logRegistry.setEntidad(empleado);
		return logRegistry;
	}

}
