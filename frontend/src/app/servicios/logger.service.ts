import {Injectable, NgZone} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {LogResponse} from "../interfaces/log-response";

@Injectable({
  providedIn: 'root'
})
export class LoggerService {

  private url = `${environment.urlApiLogger}`;

  constructor(
    private http: HttpClient,
    private ngZone: NgZone
  ) { }

  getAll(): Observable<LogResponse[]> {
    return this.http.get<LogResponse[]>(`${this.url}`);
  }

  getLogEvents(): Observable<any> {
    return new Observable(observer => {
      const eventSource = new EventSource(`${this.url}/sse`);

      eventSource.onmessage = event => {
        this.ngZone.run(() => {
          observer.next(event);
        });
      };

      eventSource.onerror = error => {
        this.ngZone.run(() => {
          observer.error(error);
        });
      };
    });
  }

}
