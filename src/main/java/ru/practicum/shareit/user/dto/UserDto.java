package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Integer id;
    @NotBlank
    private String name;
    @NotBlank
    @Pattern(regexp = ".*@.*", message = "Поле должно содержать символ '@'")
    private String email;
}
