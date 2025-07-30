export type LoginResponseDTO = {
    accessToken: string,
    refreshToken: string
}

export type RegisterResponseDTO = {
    username: string,
}

export type UploadResponseDTO = {
    title: string,
    employeeName: string,
    securityOrganization: string,
    affectedOrganization: string,
    incidentSeverity: string,
    affectedOrganizationAddress: string,
    documentId: string
}

export interface Page<T> {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;           
  numberOfElements: number; 
  pageable: Pageable;
  size: number;             
  sort: Sort[];
  totalElements: number;    
  totalPages: number;       
}

export interface GeoLocation {
  lat: number;
  lon: number;
}

export interface DummyIndex {
  id: string;
  employeeName: string;
  securityOrganization: string;
  affectedOrganization: string;
  incidentSeverity: string; 
  affectedOrganizationAddress: string;
  contentSr: string;
  databaseId: number;
  organizationLocation: GeoLocation;
  serverFilename: string;
  title: string;
  vectorizedContent: number[]; 
}

export interface Pageable {
  pageNumber: number;
  pageSize: number;
  offset: number;
  paged?: boolean;
  unpaged?: boolean;
}

export interface Sort {
  empty?: boolean;
  sorted?: boolean;
  unsorted?: boolean;
}