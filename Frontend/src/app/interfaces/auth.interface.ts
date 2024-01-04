interface LoginDto {
  email: string;
  password: string;
}

interface RegisterDto {
  name: string;
  email: string;
  password: string;
}

interface AuthDto {
  accessToken: string;
}

export { LoginDto, RegisterDto, AuthDto };
