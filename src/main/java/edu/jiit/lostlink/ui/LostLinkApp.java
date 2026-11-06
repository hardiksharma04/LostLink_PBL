package edu.jiit.lostlink.ui;

import edu.jiit.lostlink.db.Database;
import edu.jiit.lostlink.matching.MatchEngine;
import edu.jiit.lostlink.model.*;
import edu.jiit.lostlink.service.ReportService;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.List;

public class LostLinkApp extends Application {
    private final ReportService service=new ReportService();
    private final MatchEngine matcher=new MatchEngine(service);
    private Stage stage;
    private User current;
    private TableView<ItemReport> table;
    private TextField query;
    private ComboBox<String> typeFilter,categoryFilter,statusFilter;
    private Label feedback;

    @Override public void start(Stage stage){
        this.stage=stage;stage.setTitle("LostLink | JIIT Campus");stage.setMinWidth(900);stage.setMinHeight(620);
        try{Database.initialize();if(Database.hasAdministrator())showLogin();else showAdminSetup();}catch(Exception e){error("Could not initialize LostLink",e);}
    }
    private void showAdminSetup(){
        TextField name=new TextField();name.setPromptText("Administrator name");TextField email=new TextField();email.setPromptText("Administrator email");PasswordField password=new PasswordField();password.setPromptText("Create a strong password (12+ characters)");PasswordField confirm=new PasswordField();confirm.setPromptText("Confirm password");Label message=new Label();message.setWrapText(true);message.setStyle("-fx-text-fill:#b42318;");
        Button create=button("Set up administrator","#176e78");create.setOnAction(e->{try{validateAccount(name.getText(),email.getText(),password.getText());if(password.getText().length()<12)throw new IllegalArgumentException("Use at least 12 characters for the administrator password.");if(!password.getText().equals(confirm.getText()))throw new IllegalArgumentException("The passwords do not match.");service.createFirstAdministrator(name.getText(),email.getText(),password.getText());showLogin();}catch(Exception ex){message.setText(ex.getMessage());}});
        VBox card=new VBox(14,new Label("Set up LostLink"),new Label("Create the administrator account for this computer. This one-time setup does not use a shared default password."),name,email,password,confirm,create,message);card.setMaxWidth(480);card.setPadding(new Insets(32));card.setStyle("-fx-background-color:white;-fx-background-radius:16;");card.getChildren().get(0).setStyle("-fx-font-size:26px;-fx-font-weight:bold;");
        StackPane root=new StackPane(card);root.setPadding(new Insets(30));root.setStyle("-fx-background-color:#f4f7fa;");stage.setScene(new Scene(root,760,600));stage.show();
    }
    private void showLogin(){
        Label brand=new Label("LostLink");brand.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:#eef6ff;");
        Label tagline=new Label("A better way to bring campus items home.");tagline.setStyle("-fx-font-size:15px;-fx-text-fill:#c3d8eb;");
        VBox hero=new VBox(12,brand,tagline,new Label("Report it. Find it. Return it."));hero.setAlignment(Pos.CENTER_LEFT);hero.setPadding(new Insets(48));hero.setStyle("-fx-background-color:linear-gradient(to bottom right,#123557,#176e78);-fx-background-radius:18;");
        ((Label)hero.getChildren().get(2)).setStyle("-fx-font-size:19px;-fx-text-fill:white;-fx-font-weight:bold;");
        TextField email=new TextField();email.setPromptText("Campus email");PasswordField password=new PasswordField();password.setPromptText("Password");
        Label msg=new Label();msg.setStyle("-fx-text-fill:#b42318;");
        Button login=button("Sign in","#176e78"),register=button("Create account","#edf3f7");
        Runnable signIn=()->{try{User u=service.authenticate(email.getText(),password.getText());if(u==null){msg.setText("Email or password is incorrect.");return;}current=u;showDashboard();}catch(Exception e){msg.setText("Sign in failed: "+e.getMessage());}};
        login.setOnAction(e->signIn.run());password.setOnAction(e->signIn.run());
        register.setOnAction(e->registerDialog());
        VBox form=new VBox(13,new Label("Welcome back"),new Label("Sign in to browse reports or post an item."),email,password,new HBox(10,login,register),msg);
        form.setPadding(new Insets(36));form.setAlignment(Pos.CENTER_LEFT);form.setMaxWidth(440);form.getChildren().get(0).setStyle("-fx-font-size:24px;-fx-font-weight:bold;");
        HBox body=new HBox(28,hero,form);body.setAlignment(Pos.CENTER);body.setPadding(new Insets(32));HBox.setHgrow(hero,Priority.ALWAYS);hero.setMaxWidth(440);
        Label footer=new Label("JIIT Noida  ·  Campus lost & found");footer.setStyle("-fx-text-fill:#667085;");
        BorderPane root=new BorderPane(body);root.setBottom(footer);BorderPane.setAlignment(footer,Pos.CENTER);BorderPane.setMargin(footer,new Insets(0,0,18,0));root.setStyle("-fx-background-color:#f4f7fa;");
        stage.setScene(new Scene(root,1000,650));stage.show();
    }
    private void registerDialog(){
        Dialog<ButtonType>d=new Dialog<>();d.setTitle("Create a LostLink account");d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.OK);
        TextField name=new TextField();name.setPromptText("Full name");TextField email=new TextField();email.setPromptText("Email address");PasswordField pass=new PasswordField();pass.setPromptText("Password (at least 8 characters)");
        Label info=new Label();VBox box=new VBox(10,name,email,pass,info);box.setPadding(new Insets(15));d.getDialogPane().setContent(box);
        d.setResultConverter(b->b);d.showAndWait().ifPresent(b->{if(b==ButtonType.OK){try{validateAccount(name.getText(),email.getText(),pass.getText());service.register(name.getText(),email.getText(),pass.getText());alert("Account created","Your account is ready. Sign in to continue.");}catch(Exception e){alert("Could not create account",e.getMessage());}}});
    }
    private void showDashboard(){
        Label logo=new Label("LostLink");logo.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:#164565;");
        Label who=new Label("Hi, "+current.getName()+(current.isAdmin()?"  ·  Admin":""));Button signout=button("Sign out","#edf3f7");signout.setOnAction(e->{current=null;showLogin();});
        Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);HBox top=new HBox(16,logo,spacer,who,signout);top.setAlignment(Pos.CENTER_LEFT);top.setPadding(new Insets(18,25,12,25));
        Label title=new Label("Campus reports");title.setStyle("-fx-font-size:26px;-fx-font-weight:bold;");Label sub=new Label("Search reports, or add one to help someone find their item. Contact details appear only in likely match suggestions.");
        query=new TextField();query.setPromptText("Search item, colour, location, keywords…");query.setPrefWidth(300);
        typeFilter=new ComboBox<>(FXCollections.observableArrayList("Any","Lost","Found"));typeFilter.setValue("Any");
        categoryFilter=new ComboBox<>(FXCollections.observableArrayList("Any","Electronics","ID & Cards","Books & Stationery","Bags","Clothing","Keys","Accessories","Other"));categoryFilter.setValue("Any");
        statusFilter=new ComboBox<>(FXCollections.observableArrayList("Any","Open","Matched","Returned"));statusFilter.setValue("Any");
        Button search=button("Search","#176e78"),clear=button("Clear","#edf3f7"),report=button("＋  Report an item","#164565");search.setOnAction(e->refresh());clear.setOnAction(e->{query.clear();typeFilter.setValue("Any");categoryFilter.setValue("Any");statusFilter.setValue("Any");refresh();});report.setOnAction(e->reportDialog());
        HBox filters=new HBox(9,query,typeFilter,categoryFilter,statusFilter,search,clear);filters.setAlignment(Pos.CENTER_LEFT);
        table=new TableView<>();table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);table.setPlaceholder(new Label("No reports yet. Add one to get started."));
        addColumn("Type", "type",75);addColumn("Item", "name",145);addColumn("Category","category",120);addColumn("Colour","colour",90);addColumn("Date", "itemDate",100);addColumn("Location","location",130);addColumn("Description","description",220);addColumn("Status","status",90);addColumn("Verified","verifiedLabel",85);addColumn("Reported by","reporter",115);
        TableColumn<ItemReport,Void> ownActions=new TableColumn<>("My report");ownActions.setPrefWidth(140);ownActions.setCellFactory(col->new TableCell<>(){private final Button edit=button("Edit","#edf3f7"),remove=button("Delete","#fff0ed");private final HBox row=new HBox(5,edit,remove);{edit.setOnAction(e->{ItemReport item=selected();if(item!=null&&(current.isAdmin()||item.getUserId()==current.getId()))editDialog(item);});remove.setOnAction(e->{ItemReport item=selected();if(item!=null&&(current.isAdmin()||item.getUserId()==current.getId())&&confirm("Delete this report permanently?")){try{service.delete(current,item.getId());refresh();}catch(Exception ex){error("Could not delete report",ex);}}});}
            private ItemReport selected(){int i=getIndex();return i>=0&&i<getTableView().getItems().size()?getTableView().getItems().get(i):null;}
            @Override protected void updateItem(Void v,boolean empty){super.updateItem(v,empty);ItemReport item=empty?null:selected();boolean allowed=item!=null&&(current.isAdmin()||item.getUserId()==current.getId());setGraphic(allowed?row:null);}});table.getColumns().add(ownActions);
        if(current.isAdmin()){
                TableColumn<ItemReport,Void> actions=new TableColumn<>("Admin actions");actions.setPrefWidth(190);actions.setCellFactory(col->new TableCell<>(){private final ComboBox<String> state=new ComboBox<>(FXCollections.observableArrayList("Open","Matched","Returned"));private final Button verified=button("Verify","#edf3f7"),remove=button("Remove","#fff0ed");private final HBox row=new HBox(4,state,verified,remove);{row.setAlignment(Pos.CENTER);state.setPrefWidth(98);verified.setOnAction(e->{ItemReport item=selected();if(item!=null)runAdmin(()->service.verify(current,item.getId(),true));});remove.setOnAction(e->{ItemReport item=selected();if(item!=null&&confirm("Remove this report?")){try{service.delete(current,item.getId());refresh();}catch(Exception ex){error("Could not remove report",ex);}}});state.setOnAction(e->{ItemReport item=selected();if(item!=null)runAdmin(()->service.updateStatus(current,item.getId(),state.getValue()));});}
                private ItemReport selected(){int index=getIndex();return index>=0&&index<getTableView().getItems().size()?getTableView().getItems().get(index):null;}
                @Override protected void updateItem(Void v,boolean empty){super.updateItem(v,empty);if(empty||getIndex()>=getTableView().getItems().size())setGraphic(null);else{ItemReport item=getTableView().getItems().get(getIndex());state.setValue(item.getStatus());verified.setText(item.isVerified()?"Verified":"Verify");setGraphic(row);}}});table.getColumns().add(actions);
        }
        feedback=new Label();feedback.setStyle("-fx-text-fill:#176e78;");
        VBox content=new VBox(14,new VBox(5,title,sub),new HBox(12,filters,report),table,feedback);content.setPadding(new Insets(18,25,25,25));VBox.setVgrow(table,Priority.ALWAYS);
        BorderPane root=new BorderPane(content);root.setTop(top);root.setStyle("-fx-background-color:#f5f7fa;");stage.setScene(new Scene(root,1240,760));refresh();
    }
    private void addColumn(String title,String property,int width){TableColumn<ItemReport,Object> c=new TableColumn<>(title);c.setCellValueFactory(new PropertyValueFactory<>(property));c.setPrefWidth(width);table.getColumns().add(c);}
    private void refresh(){try{table.setItems(FXCollections.observableArrayList(service.search(query.getText(),typeFilter.getValue(),categoryFilter.getValue(),statusFilter.getValue())));}catch(Exception e){error("Could not load reports",e);}}
    private void reportDialog(){
        Dialog<ButtonType>d=new Dialog<>();d.setTitle("Report a lost or found item");d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.OK);
        ComboBox<String> type=new ComboBox<>(FXCollections.observableArrayList("Lost","Found"));type.setValue("Lost");TextField name=new TextField();name.setPromptText("Item name");ComboBox<String> category=new ComboBox<>(FXCollections.observableArrayList("Electronics","ID & Cards","Books & Stationery","Bags","Clothing","Keys","Accessories","Other"));category.setValue("Other");TextField colour=new TextField();colour.setPromptText("Colour");DatePicker date=new DatePicker(LocalDate.now());TextField location=new TextField();location.setPromptText("Campus location");TextField desc=new TextField();desc.setPromptText("Distinctive details or keywords");TextField contact=new TextField(current.getEmail());contact.setPromptText("How the owner can contact you");
        GridPane grid=new GridPane();grid.setHgap(10);grid.setVgap(10);grid.setPadding(new Insets(15));Object[][] rows={{"Report type",type},{"Item name",name},{"Category",category},{"Colour",colour},{"Date",date},{"Location",location},{"Description",desc},{"Contact",contact}};for(int i=0;i<rows.length;i++){grid.add(new Label((String)rows[i][0]),0,i);grid.add((javafx.scene.Node)rows[i][1],1,i);}d.getDialogPane().setContent(grid);
        d.setResultConverter(b->b);d.showAndWait().ifPresent(button->{if(button==ButtonType.OK){try{
            validateReport(name.getText(),colour.getText(),date.getValue(),location.getText(),desc.getText(),contact.getText());
            service.create(current,type.getValue(),name.getText(),category.getValue(),colour.getText(),date.getValue(),location.getText(),desc.getText(),contact.getText());
            ItemReport fresh=new ItemReport(0,current.getId(),type.getValue(),name.getText(),category.getValue(),colour.getText(),date.getValue(),location.getText(),desc.getText(),contact.getText(),"Open",false,current.getName());
            List<ItemReport> found=matcher.findMatches(fresh);refresh();
            if(found.isEmpty())alert("Report added","Your report is now visible to campus users. No likely matches were found yet.");else{StringBuilder text=new StringBuilder("We found these possible matches (review them and contact the reporter):\n\n");for(ItemReport m:found)text.append(m.getType()).append(" · ").append(m.getName()).append(" · ").append(m.getLocation()).append(" · ").append(matcher.compatibilityScore(fresh,m)).append("% match\nContact: ").append(m.getContact()).append("\n\n");alert("Possible matches",text.toString());}
        }catch(Exception e){alert("Could not save report",e.getMessage());}}});
    }
    private void editDialog(ItemReport item){
        Dialog<ButtonType>d=new Dialog<>();d.setTitle("Edit report");d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.OK);
        ComboBox<String> type=new ComboBox<>(FXCollections.observableArrayList("Lost","Found"));type.setValue(item.getType());TextField name=new TextField(item.getName());ComboBox<String> category=new ComboBox<>(FXCollections.observableArrayList("Electronics","ID & Cards","Books & Stationery","Bags","Clothing","Keys","Accessories","Other"));category.setEditable(true);category.setValue(item.getCategory());TextField colour=new TextField(item.getColour());DatePicker date=new DatePicker(item.getItemDate());TextField location=new TextField(item.getLocation());TextField description=new TextField(item.getDescription());TextField contact=new TextField(item.getContact());
        GridPane grid=new GridPane();grid.setHgap(10);grid.setVgap(10);grid.setPadding(new Insets(15));Object[][] fields={{"Type",type},{"Item",name},{"Category",category},{"Colour",colour},{"Date",date},{"Location",location},{"Description",description},{"Contact",contact}};for(int i=0;i<fields.length;i++){grid.add(new Label((String)fields[i][0]),0,i);grid.add((javafx.scene.Node)fields[i][1],1,i);}d.getDialogPane().setContent(grid);d.setResultConverter(b->b);
        d.showAndWait().ifPresent(b->{if(b==ButtonType.OK)try{validateReport(name.getText(),colour.getText(),date.getValue(),location.getText(),description.getText(),contact.getText());service.update(current,item.getId(),type.getValue(),name.getText(),category.getValue(),colour.getText(),date.getValue(),location.getText(),description.getText(),contact.getText());refresh();feedback.setText("Report updated.");}catch(Exception e){alert("Could not update report",e.getMessage());}});
    }
    private void validateAccount(String name,String email,String password){String value=email==null?"":email.trim();int at=value.indexOf('@');if(name==null||name.trim().isEmpty()||name.trim().length()>80||at<1||at!=value.lastIndexOf('@')||value.indexOf('.',at)<at+2||value.endsWith(".")||value.length()>254||password==null||password.length()<8||password.length()>256)throw new IllegalArgumentException("Enter a name, valid email, and password between 8 and 256 characters.");}
    private void validateReport(String name,String colour,LocalDate date,String location,String description,String contact){if(name==null||name.trim().isEmpty()||name.length()>120||colour==null||colour.trim().isEmpty()||colour.length()>80||date==null||date.isAfter(LocalDate.now())||location==null||location.trim().isEmpty()||location.length()>160||description==null||description.trim().isEmpty()||description.length()>1000||contact==null||contact.trim().isEmpty()||contact.length()>254)throw new IllegalArgumentException("Complete every field, keep within the field limits, and use a date no later than today.");}
    private void runAdmin(SqlAction action){try{action.run();refresh();}catch(Exception e){error("Admin action failed",e);}}
    private boolean confirm(String text){Alert a=new Alert(Alert.AlertType.CONFIRMATION,text,ButtonType.CANCEL,ButtonType.OK);return a.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK;}
    private Button button(String text,String color){Button b=new Button(text);b.setStyle("-fx-background-color:"+color+";-fx-text-fill:"+(color.equals("#edf3f7")||color.equals("#fff0ed")?"#243746":"white")+";-fx-font-weight:bold;-fx-padding:9 14 9 14;-fx-background-radius:8;");return b;}
    private void alert(String title,String message){Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle(title);a.setHeaderText(null);a.setContentText(message);a.showAndWait();}
    private void error(String title,Exception e){alert(title,e.getMessage()==null?e.toString():e.getMessage());}
    @FunctionalInterface private interface SqlAction{void run()throws Exception;}
    public static void main(String[]args){launch(args);}
}
