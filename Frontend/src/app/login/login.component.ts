import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';

const REDIRECT_URI = '?redirect_uri=http://localhost:4200/redirect';
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  constructor(private titleService: Title) {
    this.titleService.setTitle('Login');
  }

  continueWithFacebook() {
    location.href = '/oauth2/authorization/facebook' + REDIRECT_URI;
  }

  continueWithGoogle() {
    location.href = '/oauth2/authorization/google' + REDIRECT_URI;
  }
}
