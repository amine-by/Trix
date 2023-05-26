import {
  FacebookLoginProvider,
  SocialAuthService,
  SocialUser,
} from '@abacritt/angularx-social-login';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  socialUser: SocialUser | null = null;
  loggedUser: SocialUser | null = null;
  isLogged: boolean = false;

  constructor(private authService: SocialAuthService, private router: Router) {}

  ngOnInit(): void {
    this.authService.authState.subscribe((loggedUser) => {
      this.loggedUser = loggedUser;
      this.isLogged = this.loggedUser != null;
    });
  }

  signInWithFacebook(): void {
    this.authService
      .signIn(FacebookLoginProvider.PROVIDER_ID, {
        scope: 'public_profile',
      })
      .then((socialUser) => {
        console.log(socialUser.authToken)
        this.socialUser = socialUser;
        this.isLogged = true;
        this.router.navigate(['/']);
      });
  }
}
