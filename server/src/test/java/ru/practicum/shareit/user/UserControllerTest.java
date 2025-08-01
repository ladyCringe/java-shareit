package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserDto exampleUser;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
        objectMapper = new ObjectMapper();
        exampleUser = new UserDto(1, "Eren", "Eren.Jagger@shiganshina.pa");
    }

    @Test
    void createUserTest() throws Exception {
        when(userService.createUser(any()))
                .thenReturn(exampleUser);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(exampleUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(exampleUser.getId())))
                .andExpect(jsonPath("$.name", is(exampleUser.getName())))
                .andExpect(jsonPath("$.email", is(exampleUser.getEmail())));
    }

    @Test
    void createUserWithoutBody() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserTest() throws Exception {
        when(userService.updateUser(eq(1), any()))
                .thenReturn(exampleUser);

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(exampleUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(exampleUser.getId())))
                .andExpect(jsonPath("$.name", is(exampleUser.getName())))
                .andExpect(jsonPath("$.email", is(exampleUser.getEmail())));
    }

    @Test
    void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    void getUserByIdTest() throws Exception {
        when(userService.getUserById(1))
                .thenReturn(exampleUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(exampleUser.getId())))
                .andExpect(jsonPath("$.name", is(exampleUser.getName())))
                .andExpect(jsonPath("$.email", is(exampleUser.getEmail())));
    }

    @Test
    void getAllUsersTest() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of(exampleUser));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(exampleUser.getId())))
                .andExpect(jsonPath("$[0].name", is(exampleUser.getName())))
                .andExpect(jsonPath("$[0].email", is(exampleUser.getEmail())));
    }

    @Test
    void getAllUsersNoUserExists() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }
}
