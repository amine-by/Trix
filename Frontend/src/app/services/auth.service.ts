import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthDto, LoginDto, RegisterDto } from '../interfaces/auth.interface';

const header = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor(private HttpClient: HttpClient) {}

  apiPrefix = '/auth/';

  public authenticateUser(loginDto: LoginDto): Observable<AuthDto> {
    return this.HttpClient.post<AuthDto>(
      this.apiPrefix + 'login',
      loginDto,
      header
    );
  }

  public registerUser(registerDto: RegisterDto): Observable<AuthDto> {
    return this.HttpClient.post<AuthDto>(
      this.apiPrefix + 'register',
      registerDto,
      header
    );
  }
}
