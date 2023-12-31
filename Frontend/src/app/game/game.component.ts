import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ICard } from '../interfaces/game.interface';
import * as SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';
import { TokenService } from '../services/token.service';
@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css'],
})
export class GameComponent implements OnInit {
  cards = [
    {
      rank: 'SEVEN',
      suit: 'HEART',
    },
    {
      rank: 'EIGHT',
      suit: 'HEART',
    },
    {
      rank: 'NINE',
      suit: 'HEART',
    },
    {
      rank: 'TEN',
      suit: 'HEART',
    },
    {
      rank: 'JACK',
      suit: 'HEART',
    },
    {
      rank: 'QUEEN',
      suit: 'HEART',
    },
    {
      rank: 'KING',
      suit: 'HEART',
    },
    {
      rank: 'ACE',
      suit: 'HEART',
    },
  ];

  availableGames: Array<string> = ['Trix', 'King of Hearts'];

  normalBoard: Array<ICard> | null = null;

  trixBoard: Array<boolean> | null = null;

  constructor(private titleService: Title, private tokenService: TokenService) {
    this.titleService.setTitle('Game');
  }

  ngOnInit(): void {
    let socket = new SockJS('/ws');
    let stompClient = Stomp.over(socket);
    stompClient.connect(
      {
        Authorization: 'Bearer ' + this.tokenService.getToken(),
      },
      function (frame) {
        console.log(frame);
        stompClient.subscribe('/user/queue', (message) => {
          console.log(message);
        });
      }
    );
  }

  getNormalBoardCardImage(i: number) {
    if (this.normalBoard !== null && this.normalBoard.length > i)
      return (
        '../../assets/cards/' +
        this.normalBoard[i].rank +
        '_' +
        this.normalBoard[i].suit +
        '.png'
      );
    return '';
  }

  getHandCardImage(card: ICard) {
    return {
      'background-image':
        'url(../../assets/cards/' + card.rank + '_' + card.suit + '.png)',
    };
  }
}
