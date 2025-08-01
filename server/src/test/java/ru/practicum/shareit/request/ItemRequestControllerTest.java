package ru.practicum.shareit.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService itemRequestService;

    @InjectMocks
    private ItemRequestController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User thorfinn;
    private ItemDto sword;
    private ItemRequestDto incomingRequest;
    private ItemRequest requestEntity;
    private ItemRequestResponseDto savedRequest;
    private LocalDateTime fixedTime;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        objectMapper.registerModule(new JavaTimeModule());

        fixedTime = LocalDateTime.of(2023, 1, 1, 12, 0);

        thorfinn = new User(1, "Thorfinn Karlsefni", "thorfinn@vinland.com");

        requestEntity = new ItemRequest(
                1,
                "Searching for relics",
                thorfinn,
                fixedTime
        );

        incomingRequest = new ItemRequestDto(
                null,
                "Searching for relics",
                fixedTime
        );

        sword = new ItemDto(
                1,
                "Broken Sword",
                "Old Norse relic",
                true,
                requestEntity.getId()
        );

        savedRequest = new ItemRequestResponseDto(
                1,
                "Searching for relics",
                LocalDateTime.now(),
                List.of(sword)
        );
    }


    @Test
    void createRequestTest() throws Exception {
        when(itemRequestService.createRequest(eq(thorfinn.getId()), any(ItemRequestDto.class)))
                .thenReturn(savedRequest);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", thorfinn.getId())
                        .content(objectMapper.writeValueAsString(incomingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRequest.getId()))
                .andExpect(jsonPath("$.description").value(savedRequest.getDescription()))
                .andExpect(jsonPath("$.items").isArray());
    }


    @Test
    void getRequestByIdTest() throws Exception {
        when(itemRequestService.getRequestById(anyInt()))
                .thenReturn(savedRequest);

        mockMvc.perform(get("/requests/{id}", savedRequest.getId())
                        .header("X-Sharer-User-Id", thorfinn.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRequest.getId()))
                .andExpect(jsonPath("$.description").value("Searching for relics"));
    }

    @Test
    void getAllRequestsTest() throws Exception {
        when(itemRequestService.getAllRequests(anyInt()))
                .thenReturn(List.of(savedRequest));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", thorfinn.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    List<ItemRequestResponseDto> resultList =
                            objectMapper.readValue(body, new TypeReference<>() {});
                    if (resultList.isEmpty()) {
                        throw new AssertionError("Expected non-empty list of requests");
                    }
                });
    }

    @Test
    void getUserRequestsTest() throws Exception {
        when(itemRequestService.getRequestsByUserId(anyInt()))
                .thenReturn(List.of(savedRequest));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", thorfinn.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    List<ItemRequestResponseDto> resultList =
                            objectMapper.readValue(body, new TypeReference<>() {});
                    if (resultList.isEmpty()) {
                        throw new AssertionError("Expected non-empty list of user requests");
                    }
                });
    }

    @Test
    void badRequestInGetRequestWithoutUserHeader() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
