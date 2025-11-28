import { Component, inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { RippleModule } from 'primeng/ripple';
import { AppFloatingConfigurator } from '../../layout/component/app.floatingconfigurator';
import { AuthService, ChangePasswordRequest } from '../../services/auth.service';
import { DividerModule } from 'primeng/divider';
import { MessageService } from 'primeng/api';
import { AppLogo } from '../../layout/component/app.logo';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-change-password',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, ButtonModule, CheckboxModule, InputTextModule, PasswordModule, FormsModule, RouterModule, RippleModule, AppFloatingConfigurator, DividerModule, AppLogo],
    template: `
        <app-floating-configurator />
        <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
            <div class="flex flex-col items-center justify-center">
                <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                        <div class="text-center mb-8">
                            <app-logo width="160" height="160" />
                            <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">MicroSynth</div>
                            <span class="text-muted-color font-medium">Change password</span>
                        </div>

                        <div>
                            <form [formGroup]="changePasswordForm">
                                <div class="field">
                                    <label for="oldpassword" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Old Password</label>
                                    <p-password id="oldpassword"  placeholder="old password" [toggleMask]="true" styleClass="mb-4" [fluid]="true" [feedback]="false" formControlName="oldPassword"></p-password>
                                    <small class="p-error" *ngIf="changePasswordForm.get('oldPassword')?.errors?.['required'] && changePasswordForm.get('oldPassword')?.touched">
                                        Password is required
                                    </small>
                                    <small class="p-error" *ngIf="changePasswordForm.get('oldPassword')?.errors?.['minlength'] && changePasswordForm.get('oldPassword')?.touched">
                                        Min 5 chars
                                    </small>
                                    <small class="p-error" *ngIf="changePasswordForm.get('oldPassword')?.errors?.['minlength'] && changePasswordForm.get('oldPassword')?.touched">
                                        Max 10 chars
                                    </small>
                                </div>
                                <div class="field">
                                    <label for="password" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Password</label>
                                    <p-password id="password"  placeholder="Password" [toggleMask]="true" styleClass="mb-4" [fluid]="true" [feedback]="false" formControlName="newPassword"></p-password>
                                    <small class="p-error" *ngIf="changePasswordForm.get('newPassword')?.errors?.['required']">
                                        New password is required
                                    </small>
                                    <small class="p-error" *ngIf="changePasswordForm.get('newPassword')?.errors?.['minlength']">
                                        Min 5 chars
                                    </small>
                                    <small class="p-error" *ngIf="changePasswordForm.get('newPassword')?.errors?.['minlength']">
                                        Max 10 chars
                                    </small>
                                </div>
                                <div class="field mb-8">
                                    <label for="confirmpassword" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Confirm Password</label>
                                    <p-password id="confirmpassword"  placeholder="Confirm Password" [toggleMask]="true" styleClass="mb-4" [fluid]="true" [feedback]="false" formControlName="confirmPassword"></p-password>
                                    <small class="p-error my-8" *ngIf="changePasswordForm.get('confirmPassword')?.errors?.['required']">
                                        Confirm password is required
                                    </small>
                                    <small class="p-error my-8" *ngIf="changePasswordForm.get('confirmPassword')?.errors?.['minlength']">
                                        Min 5 chars
                                    </small>
                                    <small class="p-error my-8" *ngIf="changePasswordForm.get('confirmPassword')?.errors?.['minlength'] ">
                                        Max 10 chars
                                    </small>
                                    <small class="p-error my-8" *ngIf="changePasswordForm.hasError('passwordMismatch') && changePasswordForm.get('confirmPassword')?.touched">
                                        Password doesn't match confirm password
                                    </small>
                                </div>
                                
                                <p-button label="Change" styleClass="w-full mb-8" (onClick)="onChangePassword()" [disabled]="changePasswordForm.invalid || actionsDisabled"></p-button>

                                <p-divider layout="horizontal" align="center"><b>OR</b></p-divider>

                                <p-button label="Back to home" styleClass="p-button-outlined w-full " routerLink="/" [disabled]="actionsDisabled"></p-button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})
export class ChangePassword implements OnInit {
    fb = inject(FormBuilder);
    authService = inject(AuthService);
    messageService = inject(MessageService);

    returnUrl: string = '/'; // Default redirect

    changePasswordForm: FormGroup;

    actionsDisabled = false;

    private username: string | null;
    
    constructor(private route: ActivatedRoute, private router: Router) {
        this.changePasswordForm = this.fb.group({
            oldPassword: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(10)]],
            newPassword: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(10)]],
            confirmPassword: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(10)]]
        }, { validators: this.passwordMatchValidator() });
        this.username = this.authService.getUsername();
    }

    

    //checked: boolean = false;

    ngOnInit() {
       // this.router.navigate(['/auth/login']);
        this.route.queryParams.subscribe(params => {
            this.returnUrl = params['returnUrl'] || '/';
        });
    }

    // onLogin() {
    //     const credentials = { username: this.username, password: this.password };
    //     this.actionsDisabled = true;
    //     this.authService.login(credentials).subscribe(
    //         (response) => {
    //             this.showMessage('success', 'Login succesful', 'Success');
    //             this.router.navigate([this.returnUrl]);
    //         },
    //         (error) => {
    //             this.showMessage('error', 'Error during login', 'Error');
    //         },
    //         () => {
    //             this.actionsDisabled = false
    //         }
    //     );      

    // }

    showMessage(severity: string='success', message: string='Message Content', summary: string='Success Message') {
        this.messageService.add({ severity: severity, summary: summary, detail: message, key: 'br', life: 3000 });
    }

    onChangePassword() {
        if (this.changePasswordForm.invalid) return;

        if(!this.username) return;

        const request: ChangePasswordRequest = { 
            username: this.username, 
            oldPassword: this.changePasswordForm.value.oldPassword, 
            newPassword: this.changePasswordForm.value.newPassword 
        }

        this.authService.changePassword(request).subscribe({
        next: () => {
            this.showMessage('success', 'Password changed succesfully', 'Success');

            setTimeout(() => {
                this.router.navigate(["/logout"]);
            }, 1500);
        },
        error: (err) => {
            console.error('Errore durante il cambio password', err);
            this.showMessage('error', 'Error during change of the password', 'Error');
        }
        });
    }

    passwordMatchValidator(): ValidatorFn  {
        return (group: AbstractControl): ValidationErrors | null => {
            const newPassword = group.get('newPassword')?.value;
            const confirmPassword = group.get('confirmPassword')?.value;
        
            return newPassword === confirmPassword ? null : { passwordMismatch: true };
        };
    }

}
