import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { TokenService } from '../services/token.service';
import { AuthService } from '../services/auth.service';

const REDIRECT_URI = '?redirect_uri=http://localhost:4200/redirect';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent {
  constructor(
    private titleService: Title,
    private router: Router,
    private authService: AuthService,
    private tokenService: TokenService
  ) {
    this.titleService.setTitle('Register');
  }

  name: string = '';
  email: string = '';
  password: string = '';

  onChangeName(event: any) {
    this.name = event.target.value;
  }

  onChangeEmail(event: any) {
    this.email = event.target.value;
  }

  onChangePassword(event: any) {
    this.password = event.target.value;
  }

  register() {
    const registerDto = {
      name: this.name,
      email: this.email,
      password: this.password,
    };
    this.authService.registerUser(registerDto).subscribe({
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

  login() {
    this.router.navigate(['/login']);
  }
}
