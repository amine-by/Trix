import { ActivatedRoute, Router } from '@angular/router';
import { TokenService } from './../services/token.service';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-redirect',
  templateUrl: './redirect.component.html',
  styleUrls: ['./redirect.component.css'],
})
export class RedirectComponent implements OnInit {
  constructor(
    private titleService: Title,
    private tokenService: TokenService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.titleService.setTitle('Redirecting');
  }
  ngOnInit(): void {
    this.tokenService.setToken(this.route.snapshot.queryParams['token']);
    this.router.navigate(['/']);
  }
}
