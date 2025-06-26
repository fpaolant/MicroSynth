import { Routes } from '@angular/router';
import { Access } from './access';
import { Login } from './login';
import { Error } from './error';
import { Signup } from './signup';
import { ChangePassword } from './change.password';
import { authGuard } from '../../services/auth.guard';

export default [
    { path: 'access', component: Access },
    { path: 'error', component: Error },
    { path: 'login', component: Login },
    { path: 'signup', component: Signup},
    { path: 'change-password', component: ChangePassword, canActivate:  [authGuard]}
] as Routes;
