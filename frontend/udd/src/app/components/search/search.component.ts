import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BackendService } from '../../services/backend.service';
import { UploadResponseDTO } from '../../types/types';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './search.component.html',
  styleUrl: './search.component.css'
})
export class SearchComponent {
  searchQuery = '';
  filteredResults: string[] = [];

  selectedFile: File | null = null;

  constructor(private backend: BackendService) { }

  showModal = false;
  formData: UploadResponseDTO = {
    title: '',
    employeeName: '',
    securityOrganization: '',
    affectedOrganization: '',
    incidentSeverity: '',
    affectedOrganizationAddress: '',
    documentId: ''
  };

  onSearch() {
    const query = this.searchQuery.toLowerCase().trim();
    console.log(query)

  }

  onFileSelected(event: Event) {
    const target = event.target as HTMLInputElement;
    this.selectedFile = target.files?.[0] || null;
  }

  uploadFile() {
    if (this.selectedFile) {
      console.log(`Uploading: ${this.selectedFile.name}`);
      this.backend.uploadFile(this.selectedFile).subscribe({
        next: (res: UploadResponseDTO) => {
          this.formData = { ...res };
          this.showModal = true
        },
        error: err => { console.log(err); alert('Upload failed') }
      })
    } else {
      alert('Please select a file first.');
    }
  }

  closeModal() {
    this.showModal = false;
    this.backend.declineINdex(this.formData).subscribe({
      next: res => {
        console.log(res)
        this.selectedFile = null;
      }
    });
  }

  submitModal() {
    console.log('Modal form submitted with:', this.formData);
    this.backend.confirmIndex(this.formData).subscribe({
      next: res => {
        console.log(res)
        this.selectedFile = null;
        this.showModal = false;
      }
    })
  }
}
