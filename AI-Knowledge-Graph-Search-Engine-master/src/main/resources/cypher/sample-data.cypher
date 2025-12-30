// Roles and Users
CREATE (u1:User {id: 'USR-AGENT-001', username: 'jsmith', email: 'j.smith@company.com', fullName: 'John Smith', role: 'AGENT', active: true})
CREATE (u2:User {id: 'USR-ADMIN-001', username: 'admin', email: 'admin@company.com', fullName: 'System Administrator', role: 'ADMIN', active: true})

// Categories
CREATE (c1:Category {id: 'CAT-TECH', name: 'Technical', description: 'Hardware and Software issues', color: '#e74c3c'})
CREATE (c2:Category {id: 'CAT-BILL', name: 'Billing', description: 'Payment and subscription management', color: '#3498db'})

// SLAs
CREATE (s1:SLA {id: 'SLA-URGENT', name: 'Urgent Support', priority: 'HIGH', responseTimeMinutes: 60, resolutionTimeMinutes: 240, active: true})
CREATE (s2:SLA {id: 'SLA-STANDARD', name: 'Standard Support', priority: 'MEDIUM', responseTimeMinutes: 240, resolutionTimeMinutes: 1440, active: true})

// Teams
CREATE (t1:Team {id: 'TEAM-L1', name: 'Level 1 Support', department: 'Customer Success', leadId: 'USR-ADMIN-001'})

// Tickets
CREATE (tk1:Ticket {id: 'TKT-001', title: 'System Crash', description: 'Application shuts down randomly', status: 'OPEN', priority: 'HIGH', category: 'CAT-TECH', createdBy: 'admin', createdAt: datetime()})
CREATE (tk1)-[:IN_CATEGORY]->(c1)
CREATE (tk1)-[:HAS_SLA]->(s1)

CREATE (tk2:Ticket {id: 'TKT-002', title: 'Payment Failed', description: 'Credit card was declined but charged', status: 'IN_PROGRESS', priority: 'MEDIUM', category: 'CAT-BILL', assignedTo: 'jsmith', createdBy: 'admin', createdAt: datetime()})
CREATE (tk2)-[:IN_CATEGORY]->(c2)
CREATE (tk2)-[:HAS_SLA]->(s2)
CREATE (tk2)-[:ASSIGNED_TO]->(u1)
