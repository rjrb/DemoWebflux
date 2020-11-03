package com.sophos.logger.beans;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogRequest {

	private LocalDateTime fecha;
	private String responsable;
	private String metodo;
	private String codigo;
	private Empleado entidad;

}
