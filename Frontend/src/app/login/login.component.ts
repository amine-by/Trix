import { TokenService } from './../services/token.service';
import {
  FacebookLoginProvider,
  SocialAuthService,
  SocialUser,
} from '@abacritt/angularx-social-login';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthService } from '../services/oauth.service';
import { Title } from '@angular/platform-browser';
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  socialUser: SocialUser | null = null;
  loggedUser: SocialUser | null = null;
  isLogged: boolean = false;

  constructor(
    private authService: SocialAuthService,
    private router: Router,
    private oAuthService: OAuthService,
    private tokenService: TokenService,
    private titleService: Title
  ) {
    this.titleService.setTitle('Login');
  }

  ngOnInit(): void {
    this.authService.authState.subscribe({
      next: (loggedUser) => {
        this.loggedUser = loggedUser;
        this.isLogged =
          this.loggedUser != null && this.tokenService.getToken != null;
      },
      error: (error) => {
        console.log(error);
      },
    });
  }

  signInWithFacebook(): void {
    this.authService
      .signIn(FacebookLoginProvider.PROVIDER_ID, {
        scope: 'public_profile',
      })
      .then((socialUser) => {
        this.socialUser = socialUser;
        const tokenDto = {
          value: this.socialUser.authToken,
        };
        this.oAuthService.facebook(tokenDto).subscribe({
          next: (response) => {
            this.tokenService.setToken(response.value);
            this.router.navigate(['/']);
          },
          error: (error) => {
            console.log(error);
          },
        });
      })
      .catch((error) => console.log(error));
  }
}
