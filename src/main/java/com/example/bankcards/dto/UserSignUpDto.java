package com.example.bankcards.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import com.example.bankcards.entity.User;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на регистрацию")
public class UserSignUpDto {
    @Schema(description = "Имя пользователя", example = "John")
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 5,  max = 50 , message = "Имя пользователя должно содержать от 5 символов")
    private String nickname;

    @Schema(description = "Адрес электронной почты", example = "johndoe@gmail.com")
    @Email
    @NotBlank
    @Size(min = 5,  max = 255 , message = "Не валидная почта")
    private String email;


    @Schema(description = "Пароль", example = "secret_password")
    @Size( min = 5, max = 255, message = "Длинна пароля должна быть не более 255 символов")
    private String password;

    public UserSignUpDto() {

    }

    public User toUser(){
        User user = new User();
        user.setUsername(nickname);
        user.setEmail(email);
        user.setPassword(password);

        return user;

    }

}