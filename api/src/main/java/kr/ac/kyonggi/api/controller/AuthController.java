package kr.ac.kyonggi.api.controller;

import jakarta.validation.Valid;
import kr.ac.kyonggi.api.dto.request.RegisterRequest;
import kr.ac.kyonggi.api.dto.response.UserResponse;
import kr.ac.kyonggi.api.service.AuthApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApiService authApiService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authApiService.register(request);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = authApiService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
