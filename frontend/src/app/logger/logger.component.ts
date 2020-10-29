import { Component, OnInit } from '@angular/core';
import {LoggerService} from "../servicios/logger.service";
import {LogResponse} from "../interfaces/log-response";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {HttpErrorResponse} from "@angular/common/http";
import {interval, Subscription} from "rxjs";
import {take} from "rxjs/operators";
import {AlertaModal} from "../alerta-modal/alerta-modal.component";

@Component({
  selector: 'app-logger',
  templateUrl: './logger.component.html',
  styleUrls: ['./logger.component.scss']
})
export class LoggerComponent implements OnInit {

  listaLogs: LogResponse[];
  procesando = false;
  mensajeExito = '';
  mensajeError = '';
  cierreMensajes$: Subscription;

  constructor(
    private loggerService: LoggerService,
    private modalService: NgbModal
  ) { }

  ngOnInit(): void {
    this.consultar();

    this.loggerService.getLogEvents().subscribe(event => {
      console.log(event);
      if(this.listaLogs.length >= 10) {
        this.listaLogs.pop();
      }
      this.listaLogs.unshift(JSON.parse(event.data));
    })
  }

  consultar(): void {
    this.procesando = true;
    this.loggerService.getAll().subscribe({
      next: logs => {
        console.log(logs);
        this.listaLogs = logs;
        this.procesando = false;
      },
      error: err => {
        this.listaLogs = [];
        this.procesando = false;
        if(err instanceof HttpErrorResponse) {
          let httpError: HttpErrorResponse = err as HttpErrorResponse;
          if(httpError.status == 404) {
            this.mensajeError = 'No hay logs registrados';
            this.cierreMensajes$ = interval(5000).pipe(take(1)).subscribe(() => this.mensajeError = '');
            return;
          }
        }
        this.mostrarError("Error consultando todos los logs", err);
      }
    })
  }

  mostrarError(mensaje: string, err: any): void {
    console.log(mensaje);
    console.log(err);
    const modalRef: NgbModalRef = this.modalService.open(AlertaModal);
    modalRef.componentInstance.titulo = "Error";
    modalRef.componentInstance.mensaje = mensaje;
  }

  ngOnDestroy(): void {
    this.cierreMensajes$?.unsubscribe();
  }

}
