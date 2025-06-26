import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, input, Input, Output, ViewChild } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { FileUpload, FileUploadModule } from 'primeng/fileupload';

@Component({
  standalone: true,
  selector: 'app-upload-file-dialog',
  imports: [
    CommonModule, ButtonModule, DialogModule, ReactiveFormsModule, FileUploadModule, ToastModule
  ],
  templateUrl: './upload-file-dialog.component.html',
  styleUrl: './upload-file-dialog.component.scss'
})
export class UploadFileDialogComponent {
  @ViewChild('fu') fileUpload!: FileUpload;

  @Input() visible: boolean = false;
  @Output() visibleChange = new EventEmitter<boolean>(); // for [(visible)]

  @Input() closeOnfinish: boolean = true;
  @Input() multiple: boolean = false;
  @Input() fileMimeType: string = '*/*';
  @Input() maxFileSize: number = 5 * 1024 * 1024; // 5MB default

  @Input() headerTitle: string = 'Upload File';
  @Input() description: string = 'Seleziona un file da caricare';

  @Output() fileParsed = new EventEmitter<{ name: string; content: string }>();
  @Output() onCancel = new EventEmitter<void>();


  uploadedFiles: { name: string; size: number; content: string }[] = [];

  onFileSelect(event: any) {
    const files: File[] = event.files;

    for (const file of files) {
      const reader = new FileReader();

      reader.onload = () => {
        const content = reader.result as string;

        this.fileParsed.emit({
          name: file.name,
          content,
        });
        
        if (!this.multiple && this.closeOnfinish) {
          this.closeDialog();
        }
      };

      reader.onerror = () => {};

      reader.readAsText(file);
    }
  }

  closeDialog() {
    this.visible = false;
    this.visibleChange.emit(false);
    this.onCancel.emit();
  }

  

}
