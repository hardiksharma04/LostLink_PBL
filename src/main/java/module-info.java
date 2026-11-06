module edu.jiit.lostlink {
    requires javafx.controls;
    requires java.sql;
    requires java.prefs;
    requires org.xerial.sqlitejdbc;
    opens edu.jiit.lostlink.model to javafx.base;
    exports edu.jiit.lostlink.ui;
}
