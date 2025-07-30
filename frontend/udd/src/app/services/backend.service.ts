import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DummyIndex, LoginResponseDTO, Page, RegisterResponseDTO, UploadResponseDTO } from '../types/types';

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

  declineIndex(data: UploadResponseDTO){
    return this.http.delete(`${this.baseUrl}/api/index/decline/${data.documentId}`)
  }

  search(expression: string){
    return this.http.post<Page<DummyIndex>>(`${this.baseUrl}/api/search`, {expression});
  }

  knnSearch(query: string){
    return this.http.post<Page<DummyIndex>>(`${this.baseUrl}/api/search/knn`, {query});
  }

  geoSearch(location: string, radiusKm: number){
    return this.http.post<Page<DummyIndex>>(`${this.baseUrl}/api/search/geo`, {location, radiusKm});
  }

  downloadFile(filename: string) {
    return this.http.get(`${this.baseUrl}/api/file/${filename}`, { responseType: 'blob' })
  }
}
