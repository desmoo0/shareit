package ru.practicum.shareit.booking;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-02T00:31:18+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class BookingMapperImpl implements BookingMapper {

    @Override
    public BookingDto toBookingDto(Booking booking) {
        if ( booking == null ) {
            return null;
        }

        BookingDto bookingDto = new BookingDto();

        bookingDto.setItemId( bookingItemId( booking ) );
        bookingDto.setBooker( userToUserDto( booking.getBooker() ) );
        bookingDto.setItem( toItemDto( booking.getItem() ) );
        bookingDto.setId( booking.getId() );
        bookingDto.setStart( booking.getStart() );
        bookingDto.setEnd( booking.getEnd() );
        bookingDto.setStatus( booking.getStatus() );

        return bookingDto;
    }

    @Override
    public Booking toBooking(BookingDto bookingDto, Item item, User booker) {
        if ( bookingDto == null && item == null && booker == null ) {
            return null;
        }

        Booking booking = new Booking();

        if ( bookingDto != null ) {
            booking.setStart( bookingDto.getStart() );
            booking.setEnd( bookingDto.getEnd() );
        }
        booking.setItem( item );
        booking.setBooker( booker );
        booking.setStatus( BookingStatus.WAITING );

        return booking;
    }

    @Override
    public ItemDto toItemDto(Item item) {
        if ( item == null ) {
            return null;
        }

        Long requestId = null;
        Long id = null;
        String name = null;
        String description = null;
        Boolean available = null;

        requestId = itemRequestId( item );
        id = item.getId();
        name = item.getName();
        description = item.getDescription();
        available = item.getAvailable();

        ItemDto itemDto = new ItemDto( id, name, description, available, requestId );

        return itemDto;
    }

    private Long bookingItemId(Booking booking) {
        if ( booking == null ) {
            return null;
        }
        Item item = booking.getItem();
        if ( item == null ) {
            return null;
        }
        Long id = item.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected UserDto userToUserDto(User user) {
        if ( user == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String email = null;

        id = user.getId();
        name = user.getName();
        email = user.getEmail();

        UserDto userDto = new UserDto( id, name, email );

        return userDto;
    }

    private Long itemRequestId(Item item) {
        if ( item == null ) {
            return null;
        }
        ItemRequest request = item.getRequest();
        if ( request == null ) {
            return null;
        }
        Long id = request.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
