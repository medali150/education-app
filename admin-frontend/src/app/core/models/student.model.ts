export interface Student {
  id?: number;
  students_id?: number;
  name: string;
  age: number;
  email: string;
  department: string;
  password?: string; // Make it optional with the ? symbol
}

export interface StudentResponse {
  students: Student[];
}