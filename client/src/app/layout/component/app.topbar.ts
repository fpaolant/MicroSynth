import { Component, inject, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { StyleClassModule } from 'primeng/styleclass';
import { AppConfigurator } from './app.configurator';
import { LayoutService } from '../service/layout.service';
import { AccountService } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { MenuModule } from 'primeng/menu';
import { AppLogo } from './app.logo';

@Component({
    selector: 'app-topbar',
    standalone: true,
    imports: [RouterModule, CommonModule, StyleClassModule, AppConfigurator, MenuModule, AppLogo],
    template: ` <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" (click)="layoutService.onMenuToggle()">
                <i class="pi pi-bars"></i>
            </button>
            <a class="layout-topbar-logo" routerLink="/">
                <app-logo/>
                <span>MicroSynth</span>
            </a>
        </div>

        <div class="layout-topbar-actions">
            <div class="layout-config-menu">
                <button type="button" class="layout-topbar-action" (click)="toggleDarkMode()">
                    <i [ngClass]="{ 'pi ': true, 'pi-moon': layoutService.isDarkTheme(), 'pi-sun': !layoutService.isDarkTheme() }"></i>
                </button>
                <div class="relative">
                    <button
                        class="layout-topbar-action layout-topbar-action-highlight"
                        pStyleClass="@next"
                        enterFromClass="hidden"
                        enterActiveClass="animate-scalein"
                        leaveToClass="hidden"
                        leaveActiveClass="animate-fadeout"
                        [hideOnOutsideClick]="true"
                    >
                        <i class="pi pi-palette"></i>
                    </button>
                    <app-configurator />
                </div>
            </div>

            <button type="button" class="ml-8 mr-8 layout-topbar-action" (click)="onAccountClick()" *ngIf="!this.authService.isLogged()">
                <i class="pi pi-user"></i>
                <span>Profile</span>
            </button>

                    
            <!-- <button type="button" pButton icon="pi pi-chevron-down" label="Options"  style="width:auto"></button> -->
            <button type="button" class="ml-8 mr-8 layout-topbar-action" label="Options" (click)="menuProfile.toggle($event)" *ngIf="this.authService.isLogged()">
                <i class="pi pi-user"></i>
                &nbsp;{{ username || '' }}&nbsp;
                <i class="pi pi-chevron-down"></i>
            </button>
            <p-menu #menuProfile [popup]="true" [model]="overlayMenuItems"></p-menu>
        </div>
    </div>`,
    styles: `
        .layout-topbar-action i {
            padding: 0 5px;
        } `
})
export class AppTopbar implements OnInit {
    items!: MenuItem[];

    authService = inject(AuthService);
    router = inject(Router);

    username = this.authService.getUsername();

    overlayMenuItems: any = [];


    constructor(public layoutService: LayoutService) {}

    ngOnInit(): void {
        this.overlayMenuItems = [
            {
                label: 'My Profile', icon: 'pi pi-home', routerLink: ['/pages/account']
            },
            {
                label: 'Change Password', icon: 'pi pi-home',
                routerLink: ['/auth/change-password'],
                queryParams: { returnUrl: this.router.url }
            },
            {
                separator: true
            },
            { label: 'Exit', icon: 'pi pi-fw pi-sign-out', routerLink: ['/logout'] }]
    }

    toggleDarkMode() {
        this.layoutService.layoutConfig.update((state) => ({ ...state, darkTheme: !state.darkTheme }));
    }

    onAccountClick() {
        this.router.navigate(['/auth/login'], { queryParams: { returnUrl: this.router.url } });
    }
}
