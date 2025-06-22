import { Component, inject, OnInit, signal, ViewChild } from '@angular/core';
import { Account, AccountService, getPossibleRoles } from '../../services/account.service';
import { CommonModule, DatePipe } from '@angular/common';
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { ToolbarModule } from 'primeng/toolbar';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { SelectModule } from 'primeng/select';
import { RadioButtonModule } from 'primeng/radiobutton';
import { InputNumberModule } from 'primeng/inputnumber';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { InputIconModule } from 'primeng/inputicon';
import { IconFieldModule } from 'primeng/iconfield';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { AuthService } from '../../services/auth.service';
import { PaginatedRequest } from '../../services/model/request.paginated';
import { forkJoin } from 'rxjs';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { ActivatedRoute, RouterModule } from '@angular/router';

interface Column {
  field: string;
  header: string;
  customExportHeader?: string;
}

interface ExportColumn {
  title: string;
  dataKey: string;
}

@Component({
  selector: 'app-admin-accounts',
  imports: [
    CommonModule,
    RouterModule,
    BreadcrumbModule,
    TableModule,
    FormsModule,
    ButtonModule,
    RippleModule,
    ToastModule,
    ToolbarModule,
    InputTextModule,
    TextareaModule,
    SelectModule,
    RadioButtonModule,
    InputNumberModule,
    DialogModule,
    TagModule,
    InputIconModule,
    IconFieldModule,
    ConfirmDialogModule,
    TooltipModule,
    DatePipe
  ],
  templateUrl: './accounts.html',
  providers: [MessageService, ConfirmationService]
})
export class AccountsPage implements OnInit {
  authService = inject(AuthService);
  accountService = inject(AccountService);
  messageService = inject(MessageService);
  confirmationService = inject(ConfirmationService);

  accountDialog: boolean = false;

  pager = {
    page: 0,
    size: 5,
    f: 0,
    r: 5
  };
  items = signal<MenuItem[]>([]);
  loadingAccounts = signal<boolean>(false);
  accounts = signal<Account[]>([]);
  account!: Account;
  totalAccounts!: number;
  roles!: any[];

  selectedAccounts!: Account[] | null;
  submitted!: boolean;

  currentUserUserID = this.authService.getUserId();

  @ViewChild('dt') dt!: Table;
  exportColumns!: ExportColumn[];
  cols!: Column[];

  constructor(private route: ActivatedRoute) {
    this.items.set([{ icon: 'pi pi-home', route: '/' }, { label: 'Accounts', route: '/pages/admin/accounts' }]);
  }

  ngOnInit() {
    // Carica gli annunci iniziali
    const roles = getPossibleRoles();
    this.roles = roles.map(role => ({ label: role, value: role }));
  }

  loadAccountsLazy(event: TableLazyLoadEvent): void {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const size = event.rows ?? 10;
    const sortBy = Array.isArray(event.sortField) ? event.sortField[0] : (event.sortField || 'username');
    const sortDir = event.sortOrder === 1 ? 'asc' : 'desc';
  
    const request: PaginatedRequest = { page, size, sortBy, sortDir };
  
    this.loadingAccounts.set(true);
    this.accountService.getAccounts(request).subscribe({
      next: res => {
        this.accounts.set(res.content); // aggiorna il signal
        this.totalAccounts = res.totalElements;
      },
      error: err => {
        console.error('Errore caricamento dati:', err);
      },
      complete: () => {
        this.loadingAccounts.set(false);
      }
    });
  }

  refreshTable(): void {
    const lastEvent = this.dt.createLazyLoadMetadata();
    this.loadAccountsLazy(lastEvent);
  }

  onGlobalFilter(table: Table, event: Event) {
    table.filterGlobal((event.target as HTMLInputElement).value, 'contains');
  }

  createNew() {
    this.account = {
      id: '',
      username: '',
      firstname: '',
      lastname: '',
      email: '',
      roles: [],
      createdAt: '',
      updatedAt: ''
    };
    this.submitted = false;
    this.accountDialog = true;
  }

  editAccount(account: Account) {
    this.account = { ...account };
    this.accountDialog = true;
  }

  deleteAccount(account: Account) {
    this.confirmationService.confirm({
      message: 'Are you sure you want to delete ' + account.username + '?',
      header: 'Confirm',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.accountService.deleteAccount(account.id).subscribe({
          next: (response) => {
            this.messageService.add({ severity: 'success', summary: 'Successo', detail: 'Account eliminato.', life: 3000 });
            this.refreshTable();
          },
          error: (error) => {
            this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Errore durante l\'eliminazione dell\'account. error: ' + error , life: 3000 });
          }
        });
      }
    });
  }

  hideDialog() {
    this.accountDialog = false;
    this.submitted = false;
  }

  saveAccount() {
    this.submitted = true;

    if (this.account.username.trim()) {
      if (this.account.id) {
        // Update existing account
        this.accountService.updateAccount(this.account).subscribe({
          next: (response) => {
            this.messageService.add({ severity: 'success', summary: 'Successo', detail: 'Account aggiornato.', life: 3000 });
            this.refreshTable();
          },
          error: (error) => {
            this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Errore durante l\'aggiornamento dell\'account. error: ' + error , life: 3000 });
          }
        });
      } else {
        // Create new account
        this.accountService.createAccount(this.account).subscribe({
          next: (response) => {
            this.messageService.add({ severity: 'success', summary: 'Successo', detail: 'Account creato.', life: 3000 });
            this.refreshTable();
          },
          error: (error) => {
            this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Errore durante la creazione dell\'account. error: ' + error , life: 3000 });
          }
        });
      }
    }
    this.hideDialog();
  }


  exportCSV() {
    this.dt.exportCSV();
  }

  deleteSelectedAccounts() {
    const observables = this.selectedAccounts?.filter((val) => val.id !== this.currentUserUserID)
    .map(account => this.accountService.deleteAccount(account.id)) || [];

    forkJoin(observables).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Successo', detail: 'N. '+observables.length+' accounts eliminati.', life: 3000 });
        this.refreshTable();
      },
      error: (error) => {
        this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Errore durante l\'eliminazione degli account. error: ' + error , life: 3000 });
      }
    });
  }

  promoteAccount(account: Account) {
    this.confirmationService.confirm({
      message: 'Sei sicuro di voler promuovere l\'utente "' + account.username + '" ad admin?',
      header: 'Conferma promozione',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.accountService.promoteAccount(account.id).subscribe({
          next: (response) => {
            
            this.messageService.add({ severity: 'success', summary: 'Successo', detail: 'Account Promosso.', life: 3000 });
            this.refreshTable();
          },
          error: (error) => {
            this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Errore durante la promozione dell\'account. error: ' + error , life: 3000 });
          }
        });
      }
    });
  }

  demoteAccount(account: Account) {
    this.confirmationService.confirm({
      message: 'Sei sicuro di voler impostare l\'utente "' + account.username + '" come user?',
      header: 'Conferma',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.accountService.demoteAccount(account.id).subscribe({
          next: (response) => {
            this.messageService.add({ severity: 'success', summary: 'Successo', detail: 'Account aggiornato.', life: 3000 });
            this.refreshTable();
          },
          error: (error) => {
            this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Errore durante la degradazione dell\'account. error: ' + error , life: 3000 });
          }
        });
      }
    });
  }


  // paginator handlers
  onRowsChange(event: any): void {
    this.pager.size = event;
    this.pager.page = this.pager.f / this.pager.size;
    this.refreshTable();
  }

  onFirstChange(event: any): void {
    this.pager.f = event;
    this.pager.page = this.pager.f / this.pager.size;
    this.refreshTable();
  }


}
