package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto addBooking(Long userId, BookingDto bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new ValidationException("Некорректные даты бронирования");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NotOwnerException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Доступ к бронированию запрещен");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findAllByBookerId(userId, pageable).getContent();
            case "CURRENT" -> bookingRepository.findAllByBookerIdAndCurrent(userId, now, pageable).getContent();
            case "PAST" -> bookingRepository.findAllByBookerIdAndEndBefore(userId, now, pageable).getContent();
            case "FUTURE" -> bookingRepository.findAllByBookerIdAndStartAfter(userId, now, pageable).getContent();
            case "WAITING" ->
                    bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING, pageable).getContent();
            case "REJECTED" ->
                    bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageable).getContent();
            default -> throw new ValidationException("Unknown state: " + state);
        };

        return toDtos(bookings);
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state, Integer from, Integer size) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findAllByItemOwnerId(userId, pageable).getContent();
            case "CURRENT" -> bookingRepository.findAllByItemOwnerIdAndCurrent(userId, now, pageable).getContent();
            case "PAST" -> bookingRepository.findAllByItemOwnerIdAndEndBefore(userId, now, pageable).getContent();
            case "FUTURE" -> bookingRepository.findAllByItemOwnerIdAndStartAfter(userId, now, pageable).getContent();
            case "WAITING" ->
                    bookingRepository.findAllByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, pageable).getContent();
            case "REJECTED" ->
                    bookingRepository.findAllByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageable).getContent();
            default -> throw new ValidationException("Unknown state: " + state);
        };

        return toDtos(bookings);
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private List<BookingDto> toDtos(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }
}
