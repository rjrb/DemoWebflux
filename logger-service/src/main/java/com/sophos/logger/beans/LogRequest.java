package com.sophos.logger.beans;

import lombok.Data;

@Data
public class LogRequest {

	private String responsable;
	private String metodo;
	private String codigo;
	private Empleado entidad;

}
