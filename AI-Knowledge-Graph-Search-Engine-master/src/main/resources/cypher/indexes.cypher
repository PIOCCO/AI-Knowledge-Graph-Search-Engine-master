CREATE INDEX ticket_id_index FOR (t:Ticket) ON (t.id);
CREATE INDEX user_id_index FOR (u:User) ON (u.id);
CREATE INDEX user_username_index FOR (u:User) ON (u.username);
CREATE INDEX category_id_index FOR (c:Category) ON (c.id);
CREATE INDEX team_id_index FOR (t:Team) ON (t.id);
CREATE INDEX kb_id_index FOR (k:KnowledgeBaseArticle) ON (k.id);
CREATE INDEX sla_id_index FOR (s:SLA) ON (s.id);
