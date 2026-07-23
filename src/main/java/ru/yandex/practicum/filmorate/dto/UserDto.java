package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;
    private String login;
    private String email;
    private String name;
    private LocalDate birthday;
}
