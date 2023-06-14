import { HomeComponent } from './home/home.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { GameComponent } from './game/game.component';
import { connectedGuard } from './guards/connected.guard';
import { disconnectedGuard } from './guards/disconnected.guard';
import { NotFoundComponent } from './not-found/not-found.component';

const routes: Routes = [
  { path: '', component: HomeComponent, canActivate: [connectedGuard] },
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [disconnectedGuard],
  },
  { path: 'game', component: GameComponent, canActivate: [connectedGuard] },
  { path: '404', component: NotFoundComponent },
  { path: '**', redirectTo: '404' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
