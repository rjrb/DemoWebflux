import {EmpleadoResponse} from "./empleado-response";

export interface LogResponse {
  fecha: string;
  responsable: string;
  metodo: string;
  codigo: string;
  empleado: EmpleadoResponse;
}
