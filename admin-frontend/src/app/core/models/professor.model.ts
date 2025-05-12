export interface Professor {
  id?: number;
  professor_id?: number; // Add this field to handle backend responses
  name: string;
  email: string;
  department: string;
  password?: string;
}

export interface ProfessorResponse {
  professors: Professor[];
}