package com.sophos.reactive.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Data
@Table(value = "empleados")
public class Empleado implements Persistable<UUID> {

	@Id
	private UUID codigo;

	private String cedula;
	private String nombre;
	private Integer edad;
	private String ciudad;

	@Override
	@Transient
	@JsonIgnore
	public UUID getId() {
		return codigo;
	}

	@Override
	@Transient
	@JsonIgnore
	public boolean isNew() {
		return Objects.isNull(codigo);
	}

}
