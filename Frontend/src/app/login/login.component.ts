import { TokenService } from './../services/token.service';
import { AuthService } from './../services/auth.service';
import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';

const REDIRECT_URI = `?redirect_uri=${window.location.origin}/redirect`;
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  email: string = '';
  password: string = '';

  constructor(
    private titleService: Title,
    private router: Router,
    private authService: AuthService,
    private tokenService: TokenService
  ) {
    this.titleService.setTitle('Login');
  }

  onChangeEmail(event: any) {
    this.email = event.target.value;
  }

  onChangePassword(event: any) {
    this.password = event.target.value;
  }

  login() {
    const loginDto = { email: this.email, password: this.password };
    this.authService.authenticateUser(loginDto).subscribe({
      next: (response) => {
        this.tokenService.setToken(response.accessToken);
        this.router.navigate(['/']);
      },
      error: (error) => console.error(error),
    });
  }

  continueWithFacebook() {
    location.href = '/oauth2/authorization/facebook' + REDIRECT_URI;
  }

  continueWithGoogle() {
    location.href = '/oauth2/authorization/google' + REDIRECT_URI;
  }

  register() {
    this.router.navigate(['/register']);
  }
}
