package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    private ItemDto item;
    private ItemWithBookingsDto itemWithBooking;
    private CommentDto comment;
    private User user;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        user = new User(1, "Saitama", "saitama@hero.com");
        item = new ItemDto(1, "Cape", "A sturdy hero cape", true, null);
        itemWithBooking = new ItemWithBookingsDto(1, "Cape", "A sturdy hero cape", true,
                null, null, null);
        comment = new CommentDto(1, "Sensei is amazing", null, null,
                "Genos", LocalDateTime.now());
    }

    @Test
    void addItemTest() throws Exception {
        when(itemService.addItem(any(), anyInt())).thenReturn(item);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId())
                        .content(mapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cape"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void updateItemTest() throws Exception {
        when(itemService.updateItem(anyInt(), any(), anyInt())).thenReturn(item);

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId())
                        .content(mapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cape"))
                .andExpect(jsonPath("$.description").value("A sturdy hero cape"));
    }

    @Test
    void getItemsByOwnerTest() throws Exception {
        when(itemService.getItemsByOwner(user.getId())).thenReturn(List.of(itemWithBooking));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getItemByIdTest() throws Exception {
        when(itemService.getItemById(1, user.getId())).thenReturn(itemWithBooking);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cape"))
                .andExpect(jsonPath("$.description").value("A sturdy hero cape"));
    }

    @Test
    void searchItemsTest() throws Exception {
        when(itemService.searchItems("cape", user.getId())).thenReturn(List.of(item));

        mockMvc.perform(get("/items/search?text=cape")
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cape"));
    }

    @Test
    void addCommentTest() throws Exception {
        when(itemService.addComment(1, user.getId(), "Sensei is amazing")).thenReturn(comment);

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId())
                        .content(mapper.writeValueAsString(Map.of("text", "Sensei is amazing"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Sensei is amazing"))
                .andExpect(jsonPath("$.authorName").value("Genos"));
    }
}
