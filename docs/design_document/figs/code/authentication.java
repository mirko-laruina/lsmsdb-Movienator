// 1 - check user uniqueness
usersCollection.find(
        or(
            eq("username", u.getUsername()),
            eq("email", u.getEmail())
        )
).first();

// 2 - insert user
usersCollection.insertOne(User.Adapter.toDBObject(u));

// 3 - edit password
usersCollection.updateOne(
    eq("username", u.getUsername()),
    set("password", u.getPassword())
);

// 4 - add session
usersCollection.updateOne(
    eq("_id", u.getId()),
    push("sessions", Session.Adapter.toDBObject(s))
);

// 5 - finds the user whose session is s
usersCollection.find(
        and(
            eq("sessions._id", s.getId())
        ))
    .first();

// 6 - remove session
usersCollection.updateOne(
    eq("_id", u.getId()),
    pull("sessions", eq("_id", s.getId()))
);
