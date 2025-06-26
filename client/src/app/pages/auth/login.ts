import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { RippleModule } from 'primeng/ripple';
import { AppFloatingConfigurator } from '../../layout/component/app.floatingconfigurator';
import { AuthService } from '../../services/auth.service';
import { DividerModule } from 'primeng/divider';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { AppLogo } from '../../layout/component/app.logo';
import { finalize } from 'rxjs';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [ButtonModule, CheckboxModule, InputTextModule, PasswordModule, FormsModule, RouterModule, RippleModule, AppFloatingConfigurator, DividerModule, ToastModule, AppLogo],
    template: `
        <app-floating-configurator />
        <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
            <div class="flex flex-col items-center justify-center">
                <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                        <div class="text-center mb-8">
                            <app-logo width="160" height="160" />
                            <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">Welcome to MicroSynth!</div>
                            <span class="text-muted-color font-medium">Login to continue</span>
                        </div>

                        <div>
                            <label for="username1" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">Username</label>
                            <input pInputText id="username1" type="text" placeholder="Username" class="w-full md:w-[30rem] mb-8" [(ngModel)]="username" />

                            <label for="password1" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Password</label>
                            <p-password id="password1" [(ngModel)]="password" placeholder="Password" [toggleMask]="true" styleClass="mb-4" [fluid]="true" [feedback]="false"></p-password>

                            <div class="flex items-center justify-between mt-2 mb-8 gap-8">
                                <!-- <div class="flex items-center">
                                    <p-checkbox [(ngModel)]="checked" id="rememberme1" binary class="mr-2"></p-checkbox>
                                    <label for="rememberme1">Remember me</label>
                                </div> -->
                                <!-- <span class="font-medium no-underline ml-2 text-right cursor-pointer text-primary">Forgot password?</span> -->
                            </div>
                            <p-button label="Sign in" styleClass="w-full mb-8" (onClick)="onLogin()" [disabled]="actionsDisabled"></p-button>

                            <p-divider layout="horizontal" align="center"><b>OR</b></p-divider>
                            <p-button label="Sign up" icon="pi pi-user-plus" severity="success" class="w-full" styleClass="w-full mx-auto mb-4" routerLink="/auth/signup" [disabled]="actionsDisabled" />

                            <p-button label="Back to home" styleClass="p-button-outlined w-full " routerLink="/" [disabled]="actionsDisabled"></p-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <p-toast position="bottom-right" key="br" />
    `
})
export class Login implements OnInit {
    returnUrl: string = '/'; // Default redirect

    username: string = 'admin';
    password: string = '12345';

    authService = inject(AuthService);
    messageService = inject(MessageService);

    actionsDisabled = false;
    
    constructor(private route: ActivatedRoute, private router: Router) {}

    

    //checked: boolean = false;

    ngOnInit() {
       // this.router.navigate(['/auth/login']);
        this.route.queryParams.subscribe(params => {
            this.returnUrl = params['returnUrl'] || '/';
        });
    }

    onLogin() {
        const credentials = { username: this.username, password: this.password };
        this.actionsDisabled = true;
        this.authService.login(credentials)
        .pipe(
            finalize(() => this.actionsDisabled = false)
        )
        .subscribe(
            (response) => {
                this.showMessage('success', 'Login succesful', 'Success');
                this.router.navigate([this.returnUrl], { replaceUrl: true });
            },
            (error) => {
                this.showMessage('error', 'Error during login', 'Error');
            }
        );      
    }

    showMessage(severity: string='success', message: string='Message Content', summary: string='Success Message') {
        this.messageService.add({ severity: severity, summary: summary, detail: message, key: 'br', life: 3000 });
    }

}
