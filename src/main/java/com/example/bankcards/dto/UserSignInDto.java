package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "Запрос на аутентификапцию")
public class UserSignInDto {

    @Schema(description = "Имя пользователя", example = "John")
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 5,  max = 50 , message = "Имя пользователя должно содержать от 5 символов")
    private String login;

    @Schema(description = "Пароль", example = "secret_password")
    @Size( min = 5, max = 255, message = "Длинна пароля должна быть не более 255 символов")
    private String password;


}