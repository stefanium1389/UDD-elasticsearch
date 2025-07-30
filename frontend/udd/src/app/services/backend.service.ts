import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginResponseDTO, RegisterResponseDTO, UploadResponseDTO } from '../types/types';

@Injectable({
  providedIn: 'root'
})
export class BackendService {


  constructor(private http: HttpClient) {
  }
  baseUrl = "http://localhost:8080"

  login(username: string, password: string) {
    return this.http.post<LoginResponseDTO>(`${this.baseUrl}/api/auth/login`, {username, password})
  }

  register(username: string, password: string) {
    return this.http.post<RegisterResponseDTO>(`${this.baseUrl}/api/auth/register`, {username, password})
  }

  uploadFile(file: File) {
    const formData = new FormData();
    formData.append('file', file)
    return this.http.post<UploadResponseDTO>(`${this.baseUrl}/api/index`, formData)
  }

  confirmIndex(data: UploadResponseDTO){
    return this.http.post(`${this.baseUrl}/api/index/confirm`, data)
  }

  declineINdex(data: UploadResponseDTO){
    return this.http.delete(`${this.baseUrl}/api/index/decline/${data.documentId}`)
  }
}
