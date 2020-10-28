package com.sophos.logger.beans;

import lombok.Data;

import java.util.UUID;

@Data
public class Empleado {

	private UUID codigo;
	private String cedula;
	private String nombre;
	private Integer edad;
	private String ciudad;

}
