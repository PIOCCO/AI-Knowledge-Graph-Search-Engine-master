package org.example.service;

import org.example.model.Ticket;
import org.example.model.enums.TicketStatus;
import org.example.model.enums.Priority;
import org.example.repository.TicketRepository;
import org.example.util.SecurityUtils;
import org.example.util.ValidationUtils;
import org.example.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService() {
        this.ticketRepository = new TicketRepository();
    }

    public Ticket createTicket(String title, String description, String categoryId, Priority priority,
            String createdBy) {
        ValidationUtils.validateNotEmpty(title, "Title");
        ValidationUtils.validateNotEmpty(description, "Description");
        ValidationUtils.validateNotNull(priority, "Priority");

        Ticket ticket = new Ticket();
        ticket.setId(SecurityUtils.generateId());
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setCategoryId(categoryId);
        ticket.setPriority(priority.name());
        ticket.setStatus(TicketStatus.OPEN.name());
        ticket.setCreatedBy(createdBy);
        ticket.setCreatedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    public Ticket getTicketById(String id) {
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new ResourceNotFoundException("Ticket not found with id: " + id);
        }
        return ticket;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> getTicketsByPriority(Priority priority) {
        return ticketRepository.findByPriority(priority);
    }

    public List<Ticket> getTicketsByAssignee(String assigneeId) {
        return ticketRepository.findByAssignee(assigneeId);
    }

    public Ticket updateTicket(Ticket ticket) {
        ValidationUtils.validateNotNull(ticket, "Ticket");
        ValidationUtils.validateNotNull(ticket.getId(), "Ticket ID");

        getTicketById(ticket.getId()); // Verify exists
        ticket.setUpdatedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    public Ticket assignTicket(String ticketId, String assigneeId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setAssignedTo(assigneeId);
        ticket.setStatus(TicketStatus.IN_PROGRESS.name());
        ticket.setUpdatedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    public Ticket updateStatus(String ticketId, TicketStatus newStatus) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setStatus(newStatus.name());
        ticket.setUpdatedAt(LocalDateTime.now());

        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        return ticketRepository.save(ticket);
    }

    public Ticket updatePriority(String ticketId, Priority newPriority) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setPriority(newPriority.name());
        ticket.setUpdatedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    public void deleteTicket(String id) {
        getTicketById(id); // Verify exists
        ticketRepository.delete(id);
    }

    public List<Ticket> searchTickets(String searchTerm) {
        ValidationUtils.validateNotEmpty(searchTerm, "Search term");
        return ticketRepository.search(searchTerm);
    }

    public long getTicketCount() {
        return ticketRepository.count();
    }

    public long getOpenTicketCount() {
        return ticketRepository.countByStatus(TicketStatus.OPEN);
    }

    public long getTicketCountByPriority(Priority priority) {
        return ticketRepository.countByPriority(priority);
    }

    public boolean isOverdue(Ticket ticket) {
        if (ticket.getDueDate() == null)
            return false;
        return LocalDateTime.now().isAfter(ticket.getDueDate()) &&
                !TicketStatus.RESOLVED.name().equals(ticket.getStatus()) &&
                !TicketStatus.CLOSED.name().equals(ticket.getStatus());
    }

    public List<Ticket> getOverdueTickets() {
        List<Ticket> allTickets = getAllTickets();
        return allTickets.stream()
                .filter(this::isOverdue)
                .toList();
    }
}
