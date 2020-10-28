import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
    window.location.href = 'https://demoserverless.auth.us-east-2.amazoncognito.com/login?client_id=7274ueks1ssbrseua6h7i8hlp4&response_type=token&scope=aws.cognito.signin.user.admin+openid+profile&redirect_uri=http://localhost:4200/empleados';
  }

}
