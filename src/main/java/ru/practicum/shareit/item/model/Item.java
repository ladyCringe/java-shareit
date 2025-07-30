package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(name = "is_available", nullable = false)
    private Boolean available;
}
