package com.sophos.reactive.beans;

import com.sophos.reactive.model.Empleado;
import lombok.Data;

@Data
public class LogRequest {

	private String responsable;
	private String metodo;
	private String codigo;
	private Empleado entidad;

}
