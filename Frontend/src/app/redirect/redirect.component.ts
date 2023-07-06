import { ActivatedRoute } from '@angular/router';
import { TokenService } from './../services/token.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-redirect',
  templateUrl: './redirect.component.html',
  styleUrls: ['./redirect.component.css']
})
export class RedirectComponent implements OnInit {
  constructor(private tokenService: TokenService, private route: ActivatedRoute){
    
  }
  ngOnInit(): void {
    this.tokenService.setToken(this.route.snapshot.queryParams['token'])
  }
}
