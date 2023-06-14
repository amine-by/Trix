import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../services/token.service';
import { inject } from '@angular/core';

export const connectedGuard: CanActivateFn = (route, state) => {
  const tokenService: TokenService = inject(TokenService);
  const router: Router = inject(Router);
  if (!tokenService.getToken()) router.navigate(['/login']);
  return !!tokenService.getToken();
};
