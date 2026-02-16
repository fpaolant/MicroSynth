import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { SliderModule } from 'primeng/slider';
import { Languages } from '../presets';


@Component({
  selector: 'generate-diagram-dialog-component',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, ButtonModule, DialogModule, Message, InputTextModule, SliderModule, CheckboxModule],
  templateUrl: './generate-diagram-dialog-component.component.html',
  styleUrl: './generate-diagram-dialog-component.component.scss'
})
export class GenerateDiagramDialogComponentComponent {
  private fb = inject(FormBuilder);

  @Input() visible: boolean = false;
  @Output() onGenerate  = new EventEmitter<any>();
  @Output() visibleChange = new EventEmitter<boolean>();

  submitted = false;

  languageList = Languages;
  defaultLanguage = this.languageList[1]

  generateGraphForm: FormGroup = this.fb.group({
    nodes: [5, [Validators.required, Validators.min(1), Validators.max(100)]],
    roots: [2, [Validators.required, Validators.min(0), Validators.max(80)]],
    density: [0.5, [Validators.required, Validators.min(0), Validators.max(1)]],
    languages: [[this.defaultLanguage], Validators.required],
    outgoingProbabiltySum: [true]
  }, { validators: this.rootsValidator });


  // Dialog actions
  onGenerateClick() {
    this.submitted = true;
    if (!this.generateGraphForm.valid) return;
    this.onGenerate.emit(this.generateGraphForm.value);
  }

  closeDialog() {
    this.visible = false;
    this.visibleChange.emit(this.visible);
  }

  // custom validator
  private rootsValidator(group: AbstractControl): ValidationErrors | null {
    const nodes = group.get('nodes')?.value;
    const roots = group.get('roots')?.value;

    if (roots < nodes) {
      return null;
    }
    return { invalidRoots: true };
  }

}
