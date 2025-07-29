import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginResponseDTO, RegisterResponseDTO } from '../types/types';

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
}
