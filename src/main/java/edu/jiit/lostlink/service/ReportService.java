package edu.jiit.lostlink.service;

import edu.jiit.lostlink.db.Database;
import edu.jiit.lostlink.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class ReportService {
    public User authenticate(String email,String password) throws SQLException {
        try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement("SELECT * FROM users WHERE email=?")){
            p.setString(1,email.trim()); try(ResultSet r=p.executeQuery()){
                if(!r.next() || !MessageDigestEqual(Passwords.hash(password,r.getString("salt")),r.getString("password_hash"))) return null;
                return new User(r.getInt("id"),r.getString("name"),r.getString("email"),r.getInt("is_admin")==1);
            }
        }
    }
    private boolean MessageDigestEqual(String a,String b){return java.security.MessageDigest.isEqual(a.getBytes(java.nio.charset.StandardCharsets.UTF_8),b.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    public void register(String name,String email,String password) throws SQLException {
        String salt=Passwords.newSalt();
        try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement("INSERT INTO users(name,email,salt,password_hash) VALUES(?,?,?,?)")){
            p.setString(1,name.trim());p.setString(2,email.trim().toLowerCase(Locale.ROOT));p.setString(3,salt);p.setString(4,Passwords.hash(password,salt));p.executeUpdate();
        }
    }
    public void createFirstAdministrator(String name,String email,String password) throws SQLException {
        try(Connection c=Database.connect()) {
            c.setAutoCommit(false);
            try(PreparedStatement check=c.prepareStatement("SELECT COUNT(*) FROM users WHERE is_admin=1");ResultSet r=check.executeQuery()) {
                if(r.next()&&r.getInt(1)>0) throw new SQLException("An administrator has already been set up.");
            }
            String salt=Passwords.newSalt();
            try(PreparedStatement p=c.prepareStatement("INSERT INTO users(name,email,salt,password_hash,is_admin) VALUES(?,?,?,?,1)")) {
                p.setString(1,name.trim());p.setString(2,email.trim().toLowerCase(Locale.ROOT));p.setString(3,salt);p.setString(4,Passwords.hash(password,salt));p.executeUpdate();c.commit();
            } catch(SQLException e){c.rollback();throw e;}
        }
    }
    public List<ItemReport> search(String query,String type,String category,String status) throws SQLException {
        StringBuilder sql=new StringBuilder("SELECT r.*,u.name AS reporter FROM reports r JOIN users u ON u.id=r.user_id WHERE 1=1");
        List<String> args=new ArrayList<>();
        if(query!=null&&!query.trim().isEmpty()){sql.append(" AND (r.name LIKE ? OR r.category LIKE ? OR r.colour LIKE ? OR r.location LIKE ? OR r.description LIKE ?)");String q="%"+query.trim()+"%";for(int i=0;i<5;i++)args.add(q);}
        if(type!=null&&!type.equals("Any")){sql.append(" AND r.type=?");args.add(type);}
        if(category!=null&&!category.equals("Any")){sql.append(" AND r.category=?");args.add(category);}
        if(status!=null&&!status.equals("Any")){sql.append(" AND r.status=?");args.add(status);}
        sql.append(" ORDER BY r.created_at DESC,r.id DESC");
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement(sql.toString())){
            for(int i=0;i<args.size();i++)p.setString(i+1,args.get(i));try(ResultSet r=p.executeQuery()){List<ItemReport> out=new ArrayList<>();while(r.next())out.add(read(r));return out;}
        }
    }
    public void create(User user,String type,String name,String category,String colour,LocalDate date,String location,String description,String contact)throws SQLException{
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("INSERT INTO reports(user_id,type,name,category,colour,item_date,location,description,contact) VALUES(?,?,?,?,?,?,?,?,?)")){
            p.setInt(1,user.getId());p.setString(2,type);p.setString(3,name.trim());p.setString(4,category);p.setString(5,colour.trim());p.setString(6,date.toString());p.setString(7,location.trim());p.setString(8,description.trim());p.setString(9,contact.trim());p.executeUpdate();
        }
    }
    public List<ItemReport> matches(ItemReport item)throws SQLException{
        List<ItemReport> candidates=search("",item.getType().equals("Lost")?"Found":"Lost","Any","Open");
        List<ItemReport> matches=new ArrayList<>();for(ItemReport c:candidates)if(score(item,c)>=25)matches.add(c);matches.sort((a,b)->Integer.compare(score(item,b),score(item,a)));return matches.size()>5?new ArrayList<>(matches.subList(0,5)):matches;
    }
    public int score(ItemReport a,ItemReport b){int n=0;if(a.getCategory().equalsIgnoreCase(b.getCategory()))n+=35;if(!a.getColour().isBlank()&&a.getColour().equalsIgnoreCase(b.getColour()))n+=20;
        if(!a.getLocation().isBlank()&&a.getLocation().equalsIgnoreCase(b.getLocation()))n+=20;
        long days=Math.abs(java.time.temporal.ChronoUnit.DAYS.between(a.getItemDate(),b.getItemDate()));if(days<=1)n+=20;else if(days<=7)n+=12;else if(days<=30)n+=5;
        Set<String> terms=new HashSet<>(Arrays.asList((a.getName()+" "+a.getDescription()).toLowerCase(Locale.ROOT).split("\\W+")));for(String w:(b.getName()+" "+b.getDescription()).toLowerCase(Locale.ROOT).split("\\W+"))if(w.length()>3&&terms.contains(w)){n+=5;break;}return n;}
    public void updateStatus(User actor,int id,String status)throws SQLException{if(!actor.isAdmin())throw new SQLException("Administrator access is required.");if(!Arrays.asList("Open","Matched","Returned").contains(status))throw new SQLException("Invalid report status.");try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("UPDATE reports SET status=? WHERE id=?")){p.setString(1,status);p.setInt(2,id);if(p.executeUpdate()==0)throw new SQLException("Report not found.");}}
    public void update(User actor,int id,String type,String name,String category,String colour,LocalDate date,String location,String description,String contact)throws SQLException{
        String sql="UPDATE reports SET type=?,name=?,category=?,colour=?,item_date=?,location=?,description=?,contact=? WHERE id=? AND (user_id=? OR ?=1)";
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,type);p.setString(2,name.trim());p.setString(3,category);p.setString(4,colour.trim());p.setString(5,date.toString());p.setString(6,location.trim());p.setString(7,description.trim());p.setString(8,contact.trim());p.setInt(9,id);p.setInt(10,actor.getId());p.setInt(11,actor.isAdmin()?1:0);if(p.executeUpdate()==0)throw new SQLException("Report not found or you do not have permission to edit it.");}
    }
    public void delete(User actor,int id)throws SQLException{
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("DELETE FROM reports WHERE id=? AND (user_id=? OR ?=1)")){p.setInt(1,id);p.setInt(2,actor.getId());p.setInt(3,actor.isAdmin()?1:0);if(p.executeUpdate()==0)throw new SQLException("Report not found or you do not have permission to remove it.");}
    }
    public void verify(User actor,int id,boolean value)throws SQLException{if(!actor.isAdmin())throw new SQLException("Administrator access is required.");try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("UPDATE reports SET verified=? WHERE id=?")){p.setInt(1,value?1:0);p.setInt(2,id);if(p.executeUpdate()==0)throw new SQLException("Report not found.");}}
    private ItemReport read(ResultSet r)throws SQLException{return new ItemReport(r.getInt("id"),r.getInt("user_id"),r.getString("type"),r.getString("name"),r.getString("category"),r.getString("colour"),LocalDate.parse(r.getString("item_date")),r.getString("location"),r.getString("description"),r.getString("contact"),r.getString("status"),r.getInt("verified")==1,r.getString("reporter"));}
}
