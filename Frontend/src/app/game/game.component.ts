import { GameService } from './../services/game.service';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import {
  GameplayDto,
  ICard,
  IPlayerStatus,
} from '../interfaces/game.interface';
import * as SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';
import { TokenService } from '../services/token.service';
@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css'],
})
export class GameComponent implements OnInit {
  gameState: GameplayDto | null = null;
  normalBoard: Array<String> | null = null;
  gameSelectValue: number = 0;

  constructor(
    private titleService: Title,
    private tokenService: TokenService,
    private gameService: GameService
  ) {
    this.titleService.setTitle('Game');
  }

  ngOnInit(): void {
    this.gameService.connectToGame().subscribe({
      next: (response) => {
        this.gameState = response;
        this.updateNormalBoard();
        let socket = new SockJS('/ws');
        let stompClient = Stomp.over(socket);
        stompClient.connect(
          {
            Authorization: 'Bearer ' + this.tokenService.getToken(),
          },
          () => {
            stompClient.subscribe('/user/queue', (message) => {
              this.gameState = JSON.parse(message.body);
              this.updateNormalBoard();
            });
          }
        );
      },
    });
  }

  onSelectGameChange(event: any) {
    this.gameSelectValue = event.value;
  }

  submitSelectGame() {
    if (this.gameState)
      this.gameService
        .gameSelect({
          gameId: this.gameState?.gameId,
          move: this.gameSelectValue,
        })
        .subscribe({
          next: (response) => {
            this.gameState = response;
            this.updateNormalBoard();
          },
        });
  }

  submitPlayCard(move: number) {
    if (this.gameState)
      this.gameService
        .playCard({
          gameId: this.gameState?.gameId,
          move,
        })
        .subscribe({
          next: (response) => {
            this.gameState = response;
            this.updateNormalBoard();
          },
        });
  }

  getHandSize(i: number): number {
    if (this.gameState && this.gameState.otherPlayers[i])
      return this.gameState.otherPlayers[i].hand;
    return 0;
  }

  getNormalBoardCardImage(i: number) {
    if (
      this.gameState &&
      this.gameState.normalBoard !== null &&
      this.gameState.normalBoard.length > i
    )
      return (
        '../../assets/cards/' +
        this.gameState.normalBoard[i].rank +
        '_' +
        this.gameState.normalBoard[i].suit +
        '.png'
      );

    return '';
  }

  updateNormalBoard() {
    if (this.gameState && this.gameState.normalBoard) {
      this.normalBoard = ['', '', '', ''];
      for (let i = 0; i < this.gameState.normalBoard.length; i++) {
        let index = this.gameState?.turn - 1 - i;
        if (index < 0) index += 4;
        this.normalBoard[index] =
          '../../assets/cards/' +
          this.gameState.normalBoard[i].rank +
          '_' +
          this.gameState.normalBoard[i].suit +
          '.png';
      }
    } else this.normalBoard = null;
  }

  getPlayer(i: number): IPlayerStatus | null {
    if (this.gameState && this.gameState.otherPlayers.length > i)
      return this.gameState.otherPlayers[i];
    return null;
  }

  getHandCardImage(card: ICard) {
    return {
      'background-image':
        'url(../../assets/cards/' + card.rank + '_' + card.suit + '.png)',
    };
  }

  isTurn(i: number) {
    return {
      color: i === this.gameState?.turn ? 'yellow' : 'white',
    };
  }

  isGameOwner(i: number) {
    return {
      color: i === this.gameState?.gameOwner ? 'tomato' : 'white',
    };
  }

  getTrixBoardCard(i: number): boolean {
    if (
      this.gameState &&
      this.gameState.trixBoard &&
      this.gameState.trixBoard[i]
    )
      return this.gameState.trixBoard && this.gameState.trixBoard[i];
    return false;
  }

  getGameOwnerName() {
    if (
      this.gameState &&
      this.gameState.otherPlayers &&
      this.gameState.otherPlayers.length > this.gameState.gameOwner
    )
      return this.gameState.otherPlayers[this.gameState.gameOwner].name;
    return '';
  }
}
