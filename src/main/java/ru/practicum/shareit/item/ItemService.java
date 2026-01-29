package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long userId, Long itemId);

    ItemDto getItemById(Long itemId); // Используется в ItemServiceImpl

    List<ItemDto> getUserItems(Long userId);

    List<ItemDto> searchItems(String text);

    @Transactional
    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
