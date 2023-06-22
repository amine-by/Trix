interface ICard {
  rank: string;
  suit: string;
}

interface IPlayer {
  id: string;
  score: number;
  hand: Array<ICard>;
  collectedCards: Array<ICard>;
  availableGames: Array<string>;
}

interface IPlayerStatus {
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
  normalBoard: Array<ICard> | null;
  player: IPlayer;
  otherPlayers: IPlayerStatus;
}

interface JoinGameDto {
  gameId: string;
}

interface MoveDto {
  move: number;
  gameId: string;
}

interface AvailableGamesDto {
  games: Array<string>;
}

export {ICard,IPlayer, IPlayerStatus, AvailableGamesDto, GameplayDto, JoinGameDto, MoveDto }