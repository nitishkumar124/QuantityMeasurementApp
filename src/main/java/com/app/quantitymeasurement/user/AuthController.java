package com.app.quantitymeasurement.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	// REGISTER API
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		try {
			String message = authService.register(request);
			return ResponseEntity.ok(message);
		} catch (RuntimeException e) {

			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// LOGIN API (RETURNS TOKEN)
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		try {
			String token = authService.login(request);
			return ResponseEntity.ok(new AuthResponse(token));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}
}