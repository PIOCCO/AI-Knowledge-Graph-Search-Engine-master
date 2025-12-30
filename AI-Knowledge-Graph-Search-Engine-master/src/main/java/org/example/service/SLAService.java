package org.example.service;

import org.example.model.SLA;
import org.example.model.Ticket;
import org.example.model.enums.Priority;
import org.example.repository.SLARepository;
import org.example.util.SecurityUtils;
import org.example.util.ValidationUtils;
import org.example.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public class SLAService {
    private final SLARepository slaRepository;

    public SLAService() {
        this.slaRepository = new SLARepository();
    }

    public SLA createSLA(String name, Priority priority, int responseTimeMinutes, int resolutionTimeMinutes) {
        ValidationUtils.validateNotEmpty(name, "SLA name");
        ValidationUtils.validateNotNull(priority, "Priority");
        ValidationUtils.validatePositive(responseTimeMinutes, "Response time");
        ValidationUtils.validatePositive(resolutionTimeMinutes, "Resolution time");

        SLA sla = new SLA();
        sla.setId(SecurityUtils.generateId());
        sla.setName(name);
        sla.setPriority(priority);
        sla.setResponseTimeMinutes(responseTimeMinutes);
        sla.setResolutionTimeMinutes(resolutionTimeMinutes);
        sla.setActive(true);
        sla.setCreatedAt(LocalDateTime.now());

        return slaRepository.save(sla);
    }

    public SLA getSLAById(String id) {
        SLA sla = slaRepository.findById(id);
        if (sla == null) {
            throw new ResourceNotFoundException("SLA not found with id: " + id);
        }
        return sla;
    }

    public List<SLA> getAllSLAs() {
        return slaRepository.findAll();
    }

    public SLA getSLAByPriority(Priority priority) {
        return slaRepository.findByPriority(priority);
    }

    public SLA updateSLA(SLA sla) {
        ValidationUtils.validateNotNull(sla, "SLA");
        ValidationUtils.validateNotNull(sla.getId(), "SLA ID");

        getSLAById(sla.getId()); // Verify exists
        return slaRepository.save(sla);
    }

    public void deleteSLA(String id) {
        getSLAById(id); // Verify exists
        slaRepository.delete(id);
    }

    public LocalDateTime calculateResponseDeadline(Ticket ticket) {
        Priority priority = Priority.valueOf(ticket.getPriority());
        SLA sla = getSLAByPriority(priority);
        if (sla == null) {
            // Default: 4 hours for response
            return ticket.getCreatedAt().plusHours(4);
        }
        return ticket.getCreatedAt().plusMinutes(sla.getResponseTimeMinutes());
    }

    public LocalDateTime calculateResolutionDeadline(Ticket ticket) {
        Priority priority = Priority.valueOf(ticket.getPriority());
        SLA sla = getSLAByPriority(priority);
        if (sla == null) {
            // Default: 24 hours for resolution
            return ticket.getCreatedAt().plusHours(24);
        }
        return ticket.getCreatedAt().plusMinutes(sla.getResolutionTimeMinutes());
    }

    public boolean isResponseOverdue(Ticket ticket) {
        LocalDateTime deadline = calculateResponseDeadline(ticket);
        return LocalDateTime.now().isAfter(deadline) && ticket.getAssignedTo() == null;
    }

    public boolean isResolutionOverdue(Ticket ticket) {
        LocalDateTime deadline = calculateResolutionDeadline(ticket);
        return LocalDateTime.now().isAfter(deadline) && ticket.getResolvedAt() == null;
    }

    public long getMinutesUntilResponseDeadline(Ticket ticket) {
        LocalDateTime deadline = calculateResponseDeadline(ticket);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(deadline))
            return 0;

        return java.time.Duration.between(now, deadline).toMinutes();
    }

    public long getMinutesUntilResolutionDeadline(Ticket ticket) {
        LocalDateTime deadline = calculateResolutionDeadline(ticket);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(deadline))
            return 0;

        return java.time.Duration.between(now, deadline).toMinutes();
    }

    public String getSLAStatus(Ticket ticket) {
        if (isResolutionOverdue(ticket)) {
            return "BREACHED";
        } else if (isResponseOverdue(ticket)) {
            return "RESPONSE_OVERDUE";
        }

        long minutesLeft = getMinutesUntilResolutionDeadline(ticket);
        if (minutesLeft < 60) {
            return "CRITICAL";
        } else if (minutesLeft < 240) {
            return "WARNING";
        }

        return "ON_TRACK";
    }
}
