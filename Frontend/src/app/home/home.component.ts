import { GameService } from './../services/game.service';
import { Router } from '@angular/router';
import { SocialAuthService, SocialUser } from '@abacritt/angularx-social-login';
import { Component, OnInit } from '@angular/core';
import { TokenService } from '../services/token.service';

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
    private tokenService: TokenService
  ) {}
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

  createGame() {
    this.gameService.createGame().subscribe({
      next: (gameplayResponse) => console.log(gameplayResponse),
      error: (error) => console.log(error),
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
      .catch((error) => console.log(error));
  }
}
