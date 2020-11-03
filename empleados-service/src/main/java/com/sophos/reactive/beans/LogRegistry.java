package com.sophos.reactive.beans;

import com.sophos.reactive.model.Empleado;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogRegistry {

	private LocalDateTime fecha;
	private String responsable;
	private String metodo;
	private String codigo;
	private Empleado entidad;

}
