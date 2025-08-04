package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByOwnerId(Integer ownerId);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true AND " +
            "(upper(i.name) LIKE upper(concat('%', ?1, '%')) OR " +
            "upper(i.description) LIKE upper(concat('%', ?1, '%')))")
    List<Item> searchAvailableItems(String text);

    List<Item> findByRequestId(Integer requestId);

    @Query("SELECT i FROM Item i WHERE i.request.id IN :requestIds")
    List<Item> findAllByRequestIds(@Param("requestIds") List<Integer> requestIds);

}
