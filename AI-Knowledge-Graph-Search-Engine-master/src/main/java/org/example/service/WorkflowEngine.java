package org.example.service;

import org.example.model.Workflow;
import org.example.model.Ticket;
import org.example.repository.WorkflowRepository;
import org.example.util.SecurityUtils;
import org.example.util.ValidationUtils;
import org.example.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public class WorkflowEngine {
    private final WorkflowRepository workflowRepository;

    public WorkflowEngine() {
        this.workflowRepository = new WorkflowRepository();
    }

    public Workflow createWorkflow(String name, String triggerEvent, String createdBy) {
        ValidationUtils.validateNotEmpty(name, "Workflow name");
        ValidationUtils.validateNotEmpty(triggerEvent, "Trigger event");

        Workflow workflow = new Workflow();
        workflow.setId(SecurityUtils.generateId());
        workflow.setName(name);
        workflow.setTriggerEvent(triggerEvent);
        workflow.setCreatedBy(createdBy);
        workflow.setActive(true);
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setExecutionCount(0);

        return workflowRepository.save(workflow);
    }

    public Workflow getWorkflowById(String id) {
        Workflow workflow = workflowRepository.findById(id);
        if (workflow == null) {
            throw new ResourceNotFoundException("Workflow not found with id: " + id);
        }
        return workflow;
    }

    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    public List<Workflow> getActiveWorkflows() {
        return workflowRepository.findActiveWorkflows();
    }

    public List<Workflow> getWorkflowsByTrigger(String triggerEvent) {
        return workflowRepository.findByTriggerEvent(triggerEvent);
    }

    public Workflow updateWorkflow(Workflow workflow) {
        ValidationUtils.validateNotNull(workflow, "Workflow");
        ValidationUtils.validateNotNull(workflow.getId(), "Workflow ID");

        getWorkflowById(workflow.getId()); // Verify exists
        return workflowRepository.save(workflow);
    }

    public void deleteWorkflow(String id) {
        getWorkflowById(id); // Verify exists
        workflowRepository.delete(id);
    }

    public void activateWorkflow(String id) {
        Workflow workflow = getWorkflowById(id);
        workflow.setActive(true);
        workflowRepository.save(workflow);
    }

    public void deactivateWorkflow(String id) {
        Workflow workflow = getWorkflowById(id);
        workflow.setActive(false);
        workflowRepository.save(workflow);
    }

    public void executeWorkflows(String triggerEvent, Object context) {
        List<Workflow> workflows = getWorkflowsByTrigger(triggerEvent);

        for (Workflow workflow : workflows) {
            if (workflow.isActive()) {
                executeWorkflow(workflow, context);
            }
        }
    }

    private void executeWorkflow(Workflow workflow, Object context) {
        try {
            // Execute workflow steps
            for (Workflow.WorkflowStep step : workflow.getSteps()) {
                executeStep(step, context);
            }

            // Increment execution count
            workflow.incrementExecutionCount();
            workflowRepository.save(workflow);

        } catch (Exception e) {
            System.err.println("Error executing workflow " + workflow.getName() + ": " + e.getMessage());
        }
    }

    private void executeStep(Workflow.WorkflowStep step, Object context) {
        // Check condition if present
        if (step.getCondition() != null && !evaluateCondition(step.getCondition(), context)) {
            return;
        }

        // Execute action based on step type
        switch (step.getAction()) {
            case "ASSIGN_TICKET":
                assignTicket(context, step.getParameters());
                break;
            case "SEND_NOTIFICATION":
                sendNotification(context, step.getParameters());
                break;
            case "UPDATE_PRIORITY":
                updatePriority(context, step.getParameters());
                break;
            case "ADD_COMMENT":
                addComment(context, step.getParameters());
                break;
            default:
                System.out.println("Unknown action: " + step.getAction());
        }
    }

    private boolean evaluateCondition(String condition, Object context) {
        // Simple condition evaluation - can be enhanced
        if (context instanceof Ticket) {
            Ticket ticket = (Ticket) context;
            if (condition.contains("priority")) {
                return condition.contains(ticket.getPriority());
            }
            if (condition.contains("status")) {
                return condition.contains(ticket.getStatus());
            }
        }
        return true;
    }

    private void assignTicket(Object context, String parameters) {
        if (context instanceof Ticket) {
            Ticket ticket = (Ticket) context;
            // Auto-assign logic based on parameters
            System.out.println("Auto-assigning ticket: " + ticket.getId());
        }
    }

    private void sendNotification(Object context, String parameters) {
        System.out.println("Sending notification with parameters: " + parameters);
    }

    private void updatePriority(Object context, String parameters) {
        if (context instanceof Ticket) {
            Ticket ticket = (Ticket) context;
            System.out.println("Updating priority for ticket: " + ticket.getId());
        }
    }

    private void addComment(Object context, String parameters) {
        if (context instanceof Ticket) {
            Ticket ticket = (Ticket) context;
            System.out.println("Adding automated comment to ticket: " + ticket.getId());
        }
    }
}
