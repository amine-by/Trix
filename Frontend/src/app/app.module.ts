import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { HttpClientModule } from '@angular/common/http';
import { GameComponent } from './game/game.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { gameInterceptor } from './interceptors/game.interceptor';
import { GamesComponent } from './games/games.component';
import { RedirectComponent } from './redirect/redirect.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    GameComponent,
    NotFoundComponent,
    GamesComponent,
    RedirectComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
  ],
  providers: [gameInterceptor],
  bootstrap: [AppComponent],
})
export class AppModule {}
