package ru.practicum.shareit.item.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private Integer id;
    private String name;
    private String description;
    private Integer ownerId;
    private Boolean available;
}
