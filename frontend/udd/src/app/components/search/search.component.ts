import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BackendService } from '../../services/backend.service';
import { UploadResponseDTO } from '../../types/types';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './search.component.html',
  styleUrl: './search.component.css'
})
export class SearchComponent {
  searchQuery = '';
  radiusKm: number = 100;
  filteredResults: any[] = [];

  selectedFile: File | null = null;

  isKnn: boolean = false;
  isGeo: boolean = false;

  constructor(private backend: BackendService, private sanitizer: DomSanitizer) { }

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
    if(this.isKnn){
      this.backend.knnSearch(query).subscribe({
        next: res => {this.filteredResults = res.content; console.log(res)}
      })
    }
    else if (this.isGeo){
      this.backend.geoSearch(query, this.radiusKm).subscribe({
        next: res => {this.filteredResults = res.content; console.log(res)}
      })
    }
    else {
      this.backend.search(query).subscribe({
        next: res=> {this.filteredResults = res.content; console.log(res)}
      });
    }

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
    this.backend.declineIndex(this.formData).subscribe({
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
  download(filename: string, title: string) {
    this.backend.downloadFile(filename).subscribe({
      next: res => {
        const objectUrl = URL.createObjectURL(res);
        const a = document.createElement('a');
        a.href = objectUrl;
        a.download = title;
        a.click();
        URL.revokeObjectURL(objectUrl);
      }
    })
  }
  getHighlightFields(item: any): string[] {
    return Object.keys(item.highlightFields || {});
  }

  sanitize(html: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }
}
