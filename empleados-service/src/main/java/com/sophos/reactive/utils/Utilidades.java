package com.sophos.reactive.utils;

import com.sophos.reactive.model.Empleado;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utilidades {

	private Utilidades() {

	}

	public static String capitalize(String texto) {
		return Stream.ofNullable(texto.strip().split("\\s+"))
			.flatMap(Stream::of)
			.map(word -> word.length() == 1 ? word.toUpperCase() : word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
			.collect(Collectors.joining(" "))
		;
	}

	public static boolean validarLongitudONulo(String texto, int longitudMaxima) {
		return Objects.nonNull(texto) && texto.length() <= longitudMaxima;
	}

	public static void validarRequest(Empleado request) {
		final String KEY_VALOR = "valor";
		final String KEY_LONGITUD = "longitud";

		Map.of(
			"cédula", Map.of(KEY_VALOR, request.getCedula(), KEY_LONGITUD, "15"),
			"nombre", Map.of(KEY_VALOR, request.getNombre(), KEY_LONGITUD, "50"),
			"ciudad", Map.of(KEY_VALOR, request.getCiudad(), KEY_LONGITUD, "25")
		).forEach((campo, detalles) -> {
			if(!Utilidades.validarLongitudONulo(detalles.get(KEY_VALOR), Integer.parseInt(detalles.get(KEY_LONGITUD)))) {
				throw new ResponseStatusException(
					HttpStatus.UNPROCESSABLE_ENTITY,
					"Campo '" + campo + "' demasiado largo o nulo (máximo " + detalles.get("longitud") + " caracteres)"
				);
			}
		});
	}

}
