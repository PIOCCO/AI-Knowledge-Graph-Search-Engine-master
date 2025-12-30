// Node Labels: User, Team, Ticket, Category, Comment, KnowledgeBaseArticle, SLA, Workflow, Notification, AuditLog

// Relationships:
// (User)-[:MEMBER_OF]->(Team)
// (User)-[:CREATED]->(Ticket)
// (Ticket)-[:ASSIGNED_TO]->(User)
// (Ticket)-[:IN_CATEGORY]->(Category)
// (Comment)-[:ON_TICKET]->(Ticket)
// (Comment)-[:BY_USER]->(User)
// (Ticket)-[:HAS_SLA]->(SLA)
// (Workflow)-[:APPLIED_TO]->(Ticket)
// (User)-[:HAS_NOTIFICATION]->(Notification)
