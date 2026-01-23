package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.enums.EventStatusEnum;
import br.com.baba.eventHub.core.exceptions.EventException;
import br.com.baba.eventHub.core.interfaces.IEmail;
import br.com.baba.eventHub.core.model.Event;
import br.com.baba.eventHub.core.model.Ticket;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.EventRepository;
import br.com.baba.eventHub.core.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private IEmail email;

    @Transactional
    public void purchaseTicket(UUID eventId, User user) throws EventException {
        Event event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> new EventException("Event not found"));
        if (event.getStatusEnum() != EventStatusEnum.ACTIVE) {
            throw new EventException("Event is not active");
        }
        if (ticketRepository.existsByUserAndEventId(user, eventId)) {
            throw new EventException("User already has a ticket for this event");
        }
        long currentTickets = ticketRepository.countByEventId(eventId);
        if (currentTickets >= event.getCapacity()) {
            throw new EventException("Event is full");
        }
        Ticket ticket = new Ticket(user, event);
        ticketRepository.save(ticket);
        notifyUser(ticket);
        if (currentTickets + 1 == event.getCapacity()) {
            notifyOrganizerSoldOut(event);
        }
    }

    public int getTicketCountPerEvent(UUID eventId) {
        return Math.toIntExact(ticketRepository.countByEventId(eventId));
    }

    private void notifyUser(Ticket ticket) {
        email.send(
                ticket.getUser().getEmail(),
                "Ticket Purchased Successfully",
                """
                        You bought a ticket for the event %s
                        Thank you for your purchase!
                        """.formatted(ticket.getEvent().getTitle())
        );
    }

    private void notifyOrganizerSoldOut(Event event) {
        int ticketCount = getTicketCountPerEvent(event.getId());
        if (ticketCount == event.getCapacity()) {
            email.send(
                    event.getOrganizer().getEmail(),
                    "Event SOLD OUT",
                    """
                            The event %s is sold out!
                            """.formatted(event.getTitle())
            );
        }
    }
}
