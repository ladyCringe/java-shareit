package ru.practicum.shareit.comments;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.comments.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemId(Integer itemId);

    List<Comment> findByItemIdIn(List<Integer> itemIds);
}