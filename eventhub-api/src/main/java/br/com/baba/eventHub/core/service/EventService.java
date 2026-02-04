package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.dto.EventFormDTO;
import br.com.baba.eventHub.core.enums.EventStatusEnum;
import br.com.baba.eventHub.core.exceptions.EventException;
import br.com.baba.eventHub.core.interfaces.IEmail;
import br.com.baba.eventHub.core.model.Event;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.EventRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private IEmail email;

    @Transactional
    public Event createEvent(@Valid EventFormDTO eventFormDTO, User organizer) {
        Event event = new Event(eventFormDTO, organizer);
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<Event> getAllActiveEvents(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate) {
        return eventRepository.findActiveEventsWithFilters(EventStatusEnum.ACTIVE, startDate, endDate, pageable);
    }

    @Transactional
    public void cancelEvent(UUID eventId) throws EventException {
        Event event = getEventById(eventId);
        validateEventIsActive(event);
        event.setStatusEnum(EventStatusEnum.CANCELLED);
    }

    public Event getEventById(UUID eventId) throws EventException {
        return eventRepository.findById(eventId).orElseThrow(() -> new EventException("Event not found"));
    }

    private void validateEventIsActive(Event event) throws EventException {
        if (event.getStatusEnum().equals(EventStatusEnum.CANCELLED) || event.getStatusEnum().equals(EventStatusEnum.FINISHED)) {
            throw new EventException("Event is already finished or cancelled");
        }
    }

    public void checkEventStatusForPurchase(UUID eventId, int ticketCount) throws EventException {
        Event event = getEventById(eventId);
        validateEventIsActive(event);
        if (ticketCount >= event.getCapacity()) {
            throw new EventException("Event is full");
        }
    }

    @Scheduled(fixedRate = 60000 * 60)
    @Transactional
    public void checkLowAttendanceWarning() {
        List<Event> events = eventRepository.findAllByStatusEnumAndDateLessThanEqual(EventStatusEnum.ACTIVE, LocalDateTime.now().plusHours(48));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Event event : events) {
            int ticketCount = ticketService.getTicketCountPerEvent(event.getId());
            double percentage = (double) ticketCount / event.getCapacity() * 100;
            if (percentage < 20) {
                String formattedDate = event.getDate().format(formatter);
                String emailBody = """
                        Warning: Low Attendance Detected
                        
                        The event "%s" is scheduled for %s and has low sales.
                        
                        Current Status:
                        - Sold: %d tickets
                        - Capacity: %d seats
                        - Occupancy: %.1f%%
                        """.formatted(
                        event.getTitle(),
                        formattedDate,
                        ticketCount,
                        event.getCapacity(),
                        percentage
                );
                email.send(
                        event.getOrganizer().getEmail(),
                        "Low Attendance Warning",
                        emailBody);
            }
        }
    }

}