import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AvailableGamesDto,
  GameplayDto,
  JoinGameDto,
  MoveDto,
} from '../interfaces/game.interface';

const header = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class GameService {
  constructor(private HttpClient: HttpClient) {}

  apiPrefix = '/api/game/';

  public findAvailableGames(): Observable<AvailableGamesDto> {
    return this.HttpClient.get<AvailableGamesDto>(
      this.apiPrefix + 'available',
      header
    );
  }

  public isPlayerInGame(): Observable<boolean> {
    return this.HttpClient.post<boolean>(this.apiPrefix + 'check', header);
  }

  public createGame(): Observable<boolean> {
    return this.HttpClient.post<boolean>(this.apiPrefix + 'create', header);
  }

  public joinGame(joinGameDto: JoinGameDto): Observable<boolean> {
    return this.HttpClient.post<boolean>(
      this.apiPrefix + 'join',
      joinGameDto,
      header
    );
  }

  public connectToGame(): Observable<GameplayDto> {
    return this.HttpClient.post<GameplayDto>(
      this.apiPrefix + 'connect',
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
