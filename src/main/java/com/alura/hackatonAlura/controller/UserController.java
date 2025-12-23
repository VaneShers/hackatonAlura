package com.alura.hackatonAlura.controller;

import com.alura.hackatonAlura.domain.usuario.UserRegisterRequest;
import com.alura.hackatonAlura.domain.usuario.UserResponse;
import com.alura.hackatonAlura.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/users")
public class UserController {

    //Aunto inyección manual
    private final UserService userService;
    public UserController (UserService userService){
            this.userService = userService;
    }

    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRegisterRequest request,
                                                 UriComponentsBuilder uriComponentsBuilder){
        UserResponse userResponse = userService.register(request);
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(userResponse.id()).toUri();
        return ResponseEntity.created(uri).body(userResponse);
    }
}
