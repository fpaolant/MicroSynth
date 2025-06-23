import { Component, inject, OnInit } from "@angular/core";
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { ActivatedRoute, Router, RouterModule } from "@angular/router";
import { ButtonModule } from "primeng/button";
import { CheckboxModule } from "primeng/checkbox";
import { InputTextModule } from "primeng/inputtext";
import { PasswordModule } from "primeng/password";
import { RippleModule } from "primeng/ripple";
import { AppFloatingConfigurator } from "../../layout/component/app.floatingconfigurator";
import { OpenAccountRequest } from "../../services/account.service";
import { IconField } from "primeng/iconfield";
import { InputIcon } from "primeng/inputicon";
import { MessageModule } from "primeng/message";
import { CommonModule } from "@angular/common";
import { ToastModule } from "primeng/toast";
import { MessageService } from "primeng/api";
import { AuthService, UserRequest } from "../../services/auth.service";
import { AppLogo } from "../../layout/component/app.logo";

@Component({
  selector: "app-signup",
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    CheckboxModule,
    InputTextModule,
    PasswordModule,
    FormsModule,
    RouterModule,
    RippleModule,
    AppFloatingConfigurator,
    IconField,
    InputIcon,
    MessageModule,
    ToastModule,
    AppLogo
  ],
  templateUrl: './signup.html'
})
export class Signup implements OnInit {
  messageService = inject(MessageService);
  authService: AuthService = inject(AuthService);

  returnUrl: string = "/"; // Default redirect

  submitted = false;
  signupForm = new FormGroup({
    firstname: new FormControl<string>('', [Validators.required, Validators.nullValidator]),
    lastname: new FormControl<string>('', [Validators.required, Validators.nullValidator]),
    username: new FormControl<string>('', [Validators.required, Validators.nullValidator]),
    password: new FormControl<string>('', [Validators.required, Validators.nullValidator]),
    email: new FormControl<string>('', [Validators.required, Validators.email, Validators.nullValidator])
  });



  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  //checked: boolean = false;

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      this.returnUrl = params["returnUrl"] || "/";
    });
  }

  onLogin() {
    this.router.navigate(["/auth/login"], {
      queryParams: { returnUrl: this.returnUrl },
    });
  }

  onSignup() {
    this.submitted = true;

    this.signupForm.disable();
    if (this.signupForm.invalid) {
      this.signupForm.enable();
      return;
    }

    const userData: UserRequest = Object.assign({}, this.signupForm.getRawValue(), {
      firstname: this.signupForm.get('firstname')?.value ?? '',
      lastname: this.signupForm.get('lastname')?.value ?? '',
      username: this.signupForm.get('username')?.value ?? '',
      password: this.signupForm.get('password')?.value ?? '',
      email: this.signupForm.get('email')?.value ?? '',
    });

    this.authService.register(userData).subscribe({
      next: (response) => {
        this.submitted = false;
        this.showMessage(
          "success",
          "Signup completed! You will redirect soon",
          "Signup completed"
        );
        setTimeout(() => {
          this.router.navigate(["/auth/login"]);
        }, 3000);
        this.signupForm.enable();
      },
      error: (err) => {
        this.showMessage("error", "Error during signup", "Error");
        console.log("Signup error", err);
        this.submitted = false;
        this.signupForm.enable();
      },
    });
  }

  showMessage(
    severity: string = "success",
    message: string = "Message Content",
    summary: string = "Success Message"
  ) {
    this.messageService.add({
      severity: severity,
      summary: summary,
      detail: message,
      key: "br",
      life: 3000,
    });
  }
}
