export interface Auth {
}

export interface AdminLogin {
  email: string;
  password: string;
}

export interface AdminResponse {
  message: string;
  admin_id: number;
  name: string;
  role: string;
  token: string;
}
