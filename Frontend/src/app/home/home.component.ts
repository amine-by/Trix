import { Router } from '@angular/router';
import { SocialAuthService, SocialUser } from '@abacritt/angularx-social-login';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  loggedUser: SocialUser | null = null;
  isLogged: boolean = false;

  constructor(private authService: SocialAuthService, private router: Router) {}
  ngOnInit(): void {
    this.authService.authState.subscribe((loggedUser) => {
      this.loggedUser = loggedUser;
      this.isLogged = this.loggedUser != null;
    });
  }

  signOut() {
    this.authService.signOut().then(() => this.router.navigate(['/login']));
  }
}
