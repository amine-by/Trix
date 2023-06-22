import { GameService } from './../services/game.service';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { AvailableGamesDto } from '../interfaces/game.interface';
import { Router } from '@angular/router';

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html',
  styleUrls: ['./games.component.css'],
})
export class GamesComponent implements OnInit {
  availableGames: Array<string> = [];

  constructor(private titleService: Title, private gameService: GameService, private router: Router) {
    this.titleService.setTitle('Games');
  }
  ngOnInit(): void {
    this.gameService.findAvailableGames().subscribe({
      next: (response: AvailableGamesDto) => {
        this.availableGames = response.games;
      },
      error: (error) => console.error(error),
    });
  }

  joinGame(gameId: string) {
    const joinGameDto = {
      gameId,
    };
    this.gameService.joinGame(joinGameDto).subscribe({
      next: (response) => response && this.router.navigate(['/game']),
      error: (error) => console.error(error),
    });
  }
}
