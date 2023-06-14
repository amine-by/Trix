import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

interface TokenDto {
  value: string;
}

const header = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class OAuthService {
  constructor(private httpClient: HttpClient) {}
  apiPrefix = '/api/auth/';
  public facebook(tokenDto: TokenDto): Observable<TokenDto> {
    return this.httpClient.post<TokenDto>(
      this.apiPrefix + 'facebook',
      tokenDto,
      header
    );
  }
}
