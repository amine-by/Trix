import { GameService } from './../services/game.service';
import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { TokenService } from '../services/token.service';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  constructor(
    private gameService: GameService,
    private router: Router,
    private tokenService: TokenService,
    private titleService: Title
  ) {
    this.titleService.setTitle('Home');
  }
  ngOnInit(): void {}

  createGame() {
    this.gameService.createGame().subscribe({
      next: (response: boolean) => response && this.router.navigate(['/game']),
      error: (error) => console.error(error),
    });
  }

  signOut() {
    this.tokenService.logOut();
  }
}
