package edu.jiit.lostlink.model;

public class User {
    private final int id;
    private final String name;
    private final String email;
    private final boolean admin;

    public User(int id, String name, String email, boolean admin) {
        this.id = id; this.name = name; this.email = email; this.admin = admin;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isAdmin() { return admin; }
}
