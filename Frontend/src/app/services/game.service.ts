import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

interface Card {
  rank: string;
  suit: string;
}

interface Player {
  id: string;
  score: number;
  hand: Array<Card>;
  collectedCards: Array<Card>;
  availableGames: Array<string>;
}

interface PlayerStatus {
  id: string;
  score: number;
  hand: number;
  collectedCards: number;
}

interface GameplayDto {
  gameId: string;
  gameOwner: number;
  turn: number;
  status: string;
  currentKingdom: string;
  trixBoard: Array<boolean> | null;
  normalBoard: Array<Card> | null;
  player: Player;
  otherPlayers: PlayerStatus;
}

interface ConnectToGameDto {
  gameId: string;
}

interface MoveDto {
  move: number;
  gameId: string;
}

const header = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class GameService {
  constructor(private HttpClient: HttpClient) {}

  apiPrefix = '/api/game/';

  public createGame(): Observable<GameplayDto> {
    return this.HttpClient.post<GameplayDto>(this.apiPrefix + 'create', header);
  }
  public connectToGame(
    connectToGameDto: ConnectToGameDto
  ): Observable<GameplayDto> {
    return this.HttpClient.post<GameplayDto>(
      this.apiPrefix + 'connect',
      connectToGameDto,
      header
    );
  }
  public gameSelect(moveDto: MoveDto): Observable<GameplayDto> {
    return this.HttpClient.post<GameplayDto>(
      this.apiPrefix + 'select',
      moveDto,
      header
    );
  }
  public playCard(moveDto: MoveDto): Observable<GameplayDto> {
    return this.HttpClient.post<GameplayDto>(
      this.apiPrefix + 'play',
      moveDto,
      header
    );
  }
}
