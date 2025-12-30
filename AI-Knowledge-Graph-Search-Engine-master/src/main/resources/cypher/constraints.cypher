CREATE CONSTRAINT user_id_unique FOR (u:User) REQUIRE u.id IS UNIQUE;
CREATE CONSTRAINT user_email_unique FOR (u:User) REQUIRE u.email IS UNIQUE;
CREATE CONSTRAINT ticket_id_unique FOR (t:Ticket) REQUIRE t.id IS UNIQUE;
CREATE CONSTRAINT team_id_unique FOR (t:Team) REQUIRE t.id IS UNIQUE;
CREATE CONSTRAINT category_id_unique FOR (c:Category) REQUIRE c.id IS UNIQUE;
