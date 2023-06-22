import { GameService } from './../services/game.service';
import { Router } from '@angular/router';
import { SocialAuthService, SocialUser } from '@abacritt/angularx-social-login';
import { Component, OnInit } from '@angular/core';
import { TokenService } from '../services/token.service';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  loggedUser: SocialUser | null = null;
  isLogged: boolean = false;

  constructor(
    private authService: SocialAuthService,
    private gameService: GameService,
    private router: Router,
    private tokenService: TokenService,
    private titleService: Title
  ) {
    this.titleService.setTitle('Home');
  }
  ngOnInit(): void {
    this.authService.authState.subscribe({
      next: (response) => {
        this.loggedUser = response;
        this.isLogged =
          this.loggedUser != null && this.tokenService.getToken != null;
      },
      error: (error) => {
        console.log(error);
      },
    });
  }

  createGame() {
    this.gameService.createGame().subscribe({
      next: (response: boolean) => response && this.router.navigate(['/game']),
      error: (error) => console.error(error),
    });
  }

  signOut() {
    this.authService
      .signOut()
      .then(() => {
        this.tokenService.logOut();
        this.isLogged = false;
        this.router.navigate(['/login']);
      })
      .catch((error) => console.error(error));
  }
}
