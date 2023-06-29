import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ICard } from '../interfaces/game.interface';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css'],
})
export class GameComponent {
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

  availableGames: Array<string> | null = null

  normalBoard: Array<ICard> | null = null;

  trixBoard: Array<boolean> | null = null;

  constructor(private titleService: Title) {
    this.titleService.setTitle('Game');
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
