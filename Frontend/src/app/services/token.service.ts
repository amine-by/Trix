import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  constructor() { }
  public getToken(): string | null {
    return sessionStorage.getItem('AuthToken');
  }

  public setToken(token: string): void {
    sessionStorage.removeItem('AuthToken');
    sessionStorage.setItem('AuthToken', token);
  }

  logOut(): void {
    sessionStorage.clear();
  }
}
