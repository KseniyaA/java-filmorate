package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class Film {

    private int id;

    @NonNull
    private String name;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate releaseDate;

    private String description;

    private int duration;

    private int rate;
}
