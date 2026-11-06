package edu.jiit.lostlink.model;

import java.time.LocalDate;

public class ItemReport {
    private final int id, userId;
    private final String type, name, category, colour, location, description, contact, status, reporter;
    private final LocalDate itemDate;
    private final boolean verified;

    public ItemReport(int id, int userId, String type, String name, String category, String colour,
                      LocalDate itemDate, String location, String description, String contact,
                      String status, boolean verified, String reporter) {
        this.id=id; this.userId=userId; this.type=type; this.name=name; this.category=category;
        this.colour=colour; this.itemDate=itemDate; this.location=location; this.description=description;
        this.contact=contact; this.status=status; this.verified=verified; this.reporter=reporter;
    }
    public int getId(){return id;} public int getUserId(){return userId;} public String getType(){return type;}
    public String getName(){return name;} public String getCategory(){return category;} public String getColour(){return colour;}
    public LocalDate getItemDate(){return itemDate;} public String getLocation(){return location;}
    public String getDescription(){return description;} public String getContact(){return contact;}
    public String getStatus(){return status;} public boolean isVerified(){return verified;} public String getReporter(){return reporter;}
    public String getVerifiedLabel(){return verified?"Yes":"No";}
}
