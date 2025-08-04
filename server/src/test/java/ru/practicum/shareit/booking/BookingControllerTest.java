package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mockMvc;

    private User naruto;
    private Item rasengan;
    private BookingDto shadowCloneJutsu;
    private LocalDateTime missionStart;

    @BeforeEach
    void init() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();

        missionStart = LocalDateTime.now();
        naruto = new User(1, "Naruto Uzumaki", "uzumaki@konoha.jp");
        rasengan = new Item(2, "Rasengan", "A powerful spinning ball of chakra",
                naruto.getId(), true, null);

        shadowCloneJutsu = new BookingDto(
                1,
                missionStart.plusHours(2),
                missionStart.plusDays(1),
                rasengan.getId(),
                ItemMapper.toDto(rasengan),
                UserMapper.toDto(naruto),
                BookingStatus.APPROVED
        );
    }

    @Test
    void createBookingTest() throws Exception {
        when(bookingService.createBooking(any(), anyInt())).thenReturn(shadowCloneJutsu);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", naruto.getId())
                        .content(objectMapper.writeValueAsString(shadowCloneJutsu))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shadowCloneJutsu.getId()))
                .andExpect(jsonPath("$.item.id").value(rasengan.getId()))
                .andExpect(jsonPath("$.booker.id").value(naruto.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approveBookingTest() throws Exception {
        when(bookingService.approveBooking(anyInt(), anyInt(), anyBoolean())).thenReturn(shadowCloneJutsu);

        mockMvc.perform(patch("/bookings/{bookingId}", shadowCloneJutsu.getId())
                        .header("X-Sharer-User-Id", naruto.getId())
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shadowCloneJutsu.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.getBookingById(anyInt(), anyInt())).thenReturn(shadowCloneJutsu);

        mockMvc.perform(get("/bookings/{bookingId}", shadowCloneJutsu.getId())
                        .header("X-Sharer-User-Id", naruto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booker.name").value("Naruto Uzumaki"));
    }

    @Test
    void getUserBookingsTest() throws Exception {
        when(bookingService.getUserBookings(anyInt(), any())).thenReturn(List.of(shadowCloneJutsu));

        mockMvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", naruto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(shadowCloneJutsu.getId()));
    }

    @Test
    void getOwnerBookingsTest() throws Exception {
        when(bookingService.getOwnerBookings(anyInt(), any()))
                .thenReturn(List.of(shadowCloneJutsu));

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", naruto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].item.name").value("Rasengan"));
    }

    @Test
    void invalidApprovalParamTest() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", 99)
                        .param("approved", "believe_it")
                        .header("X-Sharer-User-Id", naruto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
