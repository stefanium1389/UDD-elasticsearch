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