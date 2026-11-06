from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.enum.section import WD_SECTION

OUT = r"C:\Users\sharm\Desktop\LostLink\docs\LostLink_Java_Code_and_Concepts_Guide.docx"
NAVY = "17324D"
TEAL = "137C82"
PALE = "EAF3F5"
LIGHT = "F3F6F8"
INK = "243746"
MUTED = "526575"

doc = Document()
sec = doc.sections[0]
sec.top_margin = Inches(.68); sec.bottom_margin = Inches(.68)
sec.left_margin = Inches(.72); sec.right_margin = Inches(.72)

styles = doc.styles
normal = styles["Normal"]
normal.font.name = "Aptos"; normal.font.size = Pt(9.5); normal.font.color.rgb = RGBColor.from_string(INK)
normal.paragraph_format.space_after = Pt(5); normal.paragraph_format.line_spacing = 1.08
for nm, size, color in [("Title", 28, NAVY), ("Heading 1", 19, NAVY), ("Heading 2", 13, TEAL), ("Heading 3", 10.5, NAVY)]:
    s = styles[nm]; s.font.name = "Aptos Display"; s.font.size = Pt(size); s.font.bold = True; s.font.color.rgb = RGBColor.from_string(color)
    s.paragraph_format.space_before = Pt(12 if nm != "Title" else 0); s.paragraph_format.space_after = Pt(6)

def shade(cell, fill):
    pr = cell._tc.get_or_add_tcPr(); shd = OxmlElement('w:shd'); shd.set(qn('w:fill'), fill); pr.append(shd)

def set_repeat_table_header(row):
    trPr = row._tr.get_or_add_trPr(); el = OxmlElement('w:tblHeader'); el.set(qn('w:val'), 'true'); trPr.append(el)

def set_cell_text(cell, text, bold=False, color=INK, size=8.5):
    cell.text = ""
    p = cell.paragraphs[0]; p.paragraph_format.space_after = Pt(1); p.paragraph_format.space_before = Pt(1)
    r = p.add_run(str(text)); r.bold = bold; r.font.name = "Aptos"; r.font.size = Pt(size); r.font.color.rgb = RGBColor.from_string(color)
    cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER

def table(headers, rows, widths=None, size=8.2):
    t = doc.add_table(rows=1, cols=len(headers)); t.alignment = WD_TABLE_ALIGNMENT.CENTER; t.style = "Table Grid"
    t.autofit = False
    if widths:
        for col, width in zip(t.columns, widths): col.width = Inches(width)
    for i, h in enumerate(headers):
        set_cell_text(t.rows[0].cells[i], h, True, "FFFFFF", size); shade(t.rows[0].cells[i], NAVY)
    set_repeat_table_header(t.rows[0])
    for ri, row in enumerate(rows):
        cells = t.add_row().cells
        if widths:
            for c, width in zip(cells, widths): c.width = Inches(width)
        for i, value in enumerate(row):
            set_cell_text(cells[i], value, False, INK, size)
            if ri % 2 == 1: shade(cells[i], LIGHT)
    doc.add_paragraph().paragraph_format.space_after = Pt(1)
    return t

def para(text="", style=None, boldlead=None):
    p = doc.add_paragraph(style=style)
    if boldlead and text.startswith(boldlead):
        p.add_run(boldlead).bold = True; p.add_run(text[len(boldlead):])
    else: p.add_run(text)
    return p

def bullet(text, level=0):
    return para(text, "List Bullet" if level == 0 else "List Bullet 2")

def code(text):
    p = doc.add_paragraph(); p.paragraph_format.left_indent = Inches(.16); p.paragraph_format.right_indent = Inches(.12)
    p.paragraph_format.space_before = Pt(3); p.paragraph_format.space_after = Pt(7)
    pPr = p._p.get_or_add_pPr(); shd = OxmlElement('w:shd'); shd.set(qn('w:fill'), "F1F4F7"); pPr.append(shd)
    for i, line in enumerate(text.strip("\n").splitlines()):
        if i: p.add_run().add_break()
        r = p.add_run(line); r.font.name = "Consolas"; r.font.size = Pt(8); r.font.color.rgb = RGBColor.from_string("263746")
    return p

def callout(label, text):
    t = doc.add_table(rows=1, cols=1); t.alignment = WD_TABLE_ALIGNMENT.CENTER
    c = t.cell(0,0); shade(c, PALE); c.text = ""
    p = c.paragraphs[0]; p.paragraph_format.space_after = Pt(2)
    r=p.add_run(label + "  "); r.bold=True; r.font.color.rgb=RGBColor.from_string(TEAL); r.font.size=Pt(9)
    r=p.add_run(text); r.font.size=Pt(9); r.font.color.rgb=RGBColor.from_string(INK)
    doc.add_paragraph().paragraph_format.space_after=Pt(1)

def heading(text, level=1): doc.add_heading(text, level=level)

def page(): doc.add_page_break()

# Reusable header/footer
header = sec.header.paragraphs[0]; header.alignment = WD_ALIGN_PARAGRAPH.RIGHT
r=header.add_run("LOSTLINK  /  JAVA CODE GUIDE"); r.font.name="Aptos"; r.font.size=Pt(8); r.font.bold=True; r.font.color.rgb=RGBColor.from_string(TEAL)
footer = sec.footer.paragraphs[0]; footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
r=footer.add_run("Java 21  |  JavaFX  |  SQLite"); r.font.size=Pt(8); r.font.color.rgb=RGBColor.from_string(MUTED)

# Cover and orientation
p=doc.add_paragraph(style="Title"); p.add_run("LostLink Java Code and Concepts Guide")
p=doc.add_paragraph(); p.paragraph_format.space_after=Pt(15)
r=p.add_run("A file-by-file explanation of the code, design choices, and Java ideas behind the application"); r.font.size=Pt(12); r.font.color.rgb=RGBColor.from_string(TEAL)
callout("Purpose", "Use this guide to understand and present the actual Java implementation: what each class does, why its concepts are useful, and how a user action travels from the JavaFX screen through application logic into the local SQLite database.")
heading("The project in one minute")
para("LostLink is a desktop lost-and-found application. A user creates an account, signs in, posts a Lost or Found report, searches other reports, and receives suggestions for likely opposite-type matches. An administrator can verify reports and update their status. JavaFX draws the screens; Java code validates input, applies account and matching rules, and uses JDBC to read and write SQLite.")
para("There is no separate web server or network API in this implementation. The service and database code run inside the desktop process, and each computer has its own local database file. This distinction matters when describing the backend: ReportService is the application's backend/service layer, while SQLite is its local data store.")
heading("How to use this guide", 2)
para("Read Sections 1-3 for the big picture and startup path, Sections 4-9 to understand each Java source file, Section 10 for the concepts used throughout, and Section 11 for end-to-end traces and presentation-ready explanations. Code excerpts are short examples from this project; the explanations describe what the surrounding methods do.")
table(["Layer", "Project code", "Responsibility"], [
    ("Interface", "ui/LostLinkApp.java", "Screens, form controls, events, feedback, and validation at the UI boundary."),
    ("Application services", "service/ReportService.java; matching/MatchEngine.java", "Authentication, report operations, permissions, search, scoring, and match selection."),
    ("Data model", "model/User.java; model/ItemReport.java", "Java objects that represent accounts and item reports."),
    ("Persistence", "db/Database.java", "SQLite file location, connections, schema creation, and initialization checks."),
    ("Security helper", "service/Passwords.java", "Random salts and PBKDF2 password hashing."),
    ("Module/build", "module-info.java; pom.xml", "Java module access, JavaFX/SQLite dependencies, Java version, and run configuration.")
], [1.2,2.65,3.45], 8.2)

heading("1. Architecture and vocabulary")
heading("What happens when the app runs", 2)
code("main(args) -> JavaFX launch(args)\n  -> LostLinkApp.start(stage)\n      -> Database.initialize()\n      -> first run: administrator setup screen\n         later runs: sign-in screen\n  -> user events call ReportService\n      -> Database.connect() -> SQLite statements\n  -> ResultSet rows become User / ItemReport objects\n  -> JavaFX controls display the returned objects")
para("This is a layered desktop design. The UI asks service classes to perform work; the service classes own the main business rules and persistence calls; the model classes carry structured data; Database centralizes SQLite setup. The layers reduce duplicated code and give each class a clearer job.")
table(["Term", "Meaning in this project"], [
    ("Frontend / UI", "The JavaFX windows, forms, table, buttons, filters, dialogs, and status messages in LostLinkApp."),
    ("Backend / service logic", "The Java methods that authenticate users, check ownership/roles, query reports, and calculate matches. This code is packaged in the desktop app, not hosted on a server."),
    ("Database", "A local SQLite file in the user's home directory, created when the app initializes."),
    ("JDBC", "Java's database API. The project uses Connection, PreparedStatement, Statement, and ResultSet to work with SQLite."),
    ("CRUD", "Create, Read, Update, Delete: the core operations for user/report data. Search is a form of Read."),
    ("Event-driven UI", "A button click or key action triggers a handler (lambda) that calls Java methods and then updates the screen.")
], [1.55,5.75])
callout("Scope", "For a faculty presentation, say 'local desktop backend/service layer backed by SQLite.' Do not call this a campus-wide server: separate installs do not share reports.")

heading("2. Startup and JavaFX application")
heading("LostLinkApp.java: the UI entry point", 2)
para("LostLinkApp extends JavaFX Application. Its main method calls launch(args); JavaFX creates the application lifecycle and calls start(Stage). The Stage is the top-level window. start initializes the schema, checks whether an administrator exists, and chooses the first-run setup or login screen.")
code("public class LostLinkApp extends Application {\n    private final ReportService service = new ReportService();\n    private final MatchEngine matcher = new MatchEngine(service);\n\n    @Override\n    public void start(Stage stage) {\n        this.stage = stage;\n        Database.initialize();\n        if (Database.hasAdministrator()) showLogin();\n        else showAdminSetup();\n    }\n}")
table(["Method / field group", "What it does", "Why it is useful"], [
    ("service and matcher fields", "Keep reusable ReportService and MatchEngine objects for UI actions.", "Avoid constructing a new service for each click and make dependencies visible."),
    ("start(Stage)", "Sets window constraints, initializes storage, and picks the first screen.", "A single startup path prepares the app before user interaction."),
    ("showAdminSetup()", "Collects the first administrator's details, checks the form, and creates that account.", "Avoids a shared hard-coded admin password; only the initial account receives admin role."),
    ("showLogin()", "Builds the sign-in and registration screen; handlers authenticate or open the registration dialog.", "Separates access to the reporting features from anonymous browsing."),
    ("registerDialog()", "Collects account data, validates it, and delegates persistence to register().", "Keeps database details out of the interface code."),
    ("showDashboard()", "Builds filters and the report table, adds actions, then loads rows with refresh().", "The dashboard is the main browse/manage screen."),
    ("reportDialog()", "Collects a lost/found report, validates and saves it, then requests possible matches.", "Connects a user action with the service and matching layer."),
    ("editDialog(item)", "Prefills a form, validates changes, and calls update().", "Lets owners and admins correct report data."),
    ("addColumn(...)", "Binds table columns to ItemReport getters through PropertyValueFactory.", "Avoids manually copying every field into every visible table cell."),
    ("refresh()", "Reads selected filters and replaces table rows with service.search(...).", "Keeps the screen synchronized after create/edit/delete/moderation."),
    ("validateAccount / validateReport", "Checks required values, lengths, date, and basic email shape.", "Rejects invalid or excessive input before persistence."),
    ("alert / error / confirm / button", "Small UI helpers for dialog messages, confirmation, and consistent buttons.", "Avoids repeating the same JavaFX setup code."),
    ("runAdmin(SqlAction)", "Runs an admin operation, catches failures, and refreshes the table.", "Shares common error and refresh behavior across admin actions.")
], [1.5,3.15,2.65], 7.75)
heading("Java concepts visible in the UI", 2)
bullet("Inheritance: extends Application so LostLinkApp participates in the JavaFX lifecycle.")
bullet("Method overriding: @Override on start and TableCell.updateItem means this method supplies a specialized implementation of a parent-class method.")
bullet("Event handling: setOnAction(...) receives a lambda, for example e -> refresh(). The lambda is a short implementation of a JavaFX functional interface.")
bullet("State: fields such as current, table, and query retain the current signed-in user and controls while the screen is active.")
bullet("Composition: LostLinkApp has a ReportService and a MatchEngine; it uses them instead of inheriting their responsibilities.")
bullet("Anonymous inner classes: TableCell implementations customize report-action cells without creating separate named source files.")
bullet("Generics: TableView<ItemReport>, ComboBox<String>, and List<ItemReport> declare the kind of value a control or collection holds.")
callout("Important UI detail", "Most database calls happen directly inside button handlers on the JavaFX UI thread. This is acceptable for a small local lab app, but a slow query could briefly freeze the window; a production version would move long-running work to a background Task or service.")

heading("3. Data model classes")
heading("User.java", 2)
para("User represents the signed-in account inside the program. It stores an id, name, email, and admin flag. The constructor assigns the values; getters expose them while keeping fields private. This is encapsulation: callers use a stable public interface instead of reaching into an object's internal fields.")
heading("ItemReport.java", 2)
para("ItemReport represents one Lost or Found report. It contains the database id and owner's id, report type, item name/category/colour, date, location, description, contact, status, verification flag, and reporter display name. LocalDate represents the date as a date value instead of an unstructured string. getVerifiedLabel() converts the boolean into the text shown in the table.")
code("private final int id, userId;\nprivate final String type, name, category, colour, ...;\nprivate final LocalDate itemDate;\nprivate final boolean verified;\n\npublic String getVerifiedLabel() {\n    return verified ? \"Yes\" : \"No\";\n}")
table(["Concept", "What the code does", "Benefit"], [
    ("private fields", "Fields cannot be directly changed by unrelated classes.", "Protects object state and lets the class control how it is read."),
    ("final fields", "Values are assigned in the constructor and cannot later be reassigned.", "These model objects behave as read-only snapshots after creation."),
    ("constructor", "Receives all report/account values and initializes the object.", "Ensures a newly created model has the fields needed by its callers."),
    ("getters", "Public methods expose values to service/UI code.", "Provides controlled access while preserving encapsulation."),
    ("boolean + ternary", "verified is true/false; the conditional operator returns Yes/No.", "Stores a logical fact compactly and formats it for display.")
], [1.25,3.0,3.05])
callout("Design note", "These are simple data-holder classes. They do not write themselves to SQLite; ReportService and Database handle persistence.")

heading("4. Database.java and SQLite")
para("Database centralizes low-level storage setup. A static initializer creates %USERPROFILE%\\.lostlink and constructs a JDBC URL for lostlink.db. connect() opens a SQLite Connection and enables foreign-key checks. initialize() creates the users and reports tables and an index if they do not already exist. hasAdministrator() checks whether an admin row exists.")
code("Path dir = Path.of(System.getProperty(\"user.home\"), \".lostlink\");\nFiles.createDirectories(dir);\nURL = \"jdbc:sqlite:\" + dir.resolve(\"lostlink.db\").toAbsolutePath();")
table(["Database element", "Purpose"], [
    ("users", "Stores id, name, unique case-insensitive email, salt, password hash, admin flag, and creation time."),
    ("reports", "Stores item details, owner foreign key, Lost/Found type, status, verified flag, and creation time."),
    ("CHECK constraints", "Restrict report type and status to accepted values inside the database too."),
    ("FOREIGN KEY user_id", "Requires each report owner to refer to an existing user."),
    ("reports_type_status index", "Helps SQLite locate reports by type and status more efficiently."),
    ("PRAGMA foreign_keys=ON", "Turns on SQLite's foreign-key enforcement for each new connection.")
], [2.1,5.2])
heading("Important Java and JDBC techniques here", 2)
bullet("static final URL is shared by all Database calls and is computed once during class initialization.")
bullet("The private Database() constructor prevents code from constructing Database objects; the class exposes static utility methods instead.")
bullet("Path and Files APIs build the file location safely across operating systems and create its directory if missing.")
bullet("Connection, Statement, and ResultSet are used with try-with-resources. Java closes them automatically at the end of the block, including when an exception occurs.")
bullet("SQLException is declared with throws so the layer that calls the database can decide how to report the failure.")
callout("Deployment scope", "The database file belongs to the current operating-system user and local machine. The current project does not synchronize users or reports across student computers.")

heading("5. Passwords.java and account security")
para("Passwords is a small utility class. newSalt() generates 16 cryptographically random bytes and encodes them as text for storage. hash() derives a 256-bit key using PBKDF2WithHmacSHA256, 120,000 iterations, and the stored salt. Registration stores the salt and derived hash, not the original password. Authentication loads the salt, recomputes the hash from the submitted password, and compares the results with MessageDigest.isEqual.")
code("PBEKeySpec spec = new PBEKeySpec(\n    password.toCharArray(), saltBytes, 120_000, 256);\nbyte[] key = SecretKeyFactory\n    .getInstance(\"PBKDF2WithHmacSHA256\")\n    .generateSecret(spec).getEncoded();")
table(["Term", "Plain explanation", "Why this helps"], [
    ("Salt", "A random value stored beside each password hash.", "Two accounts with the same password will have different stored hashes."),
    ("PBKDF2", "A deliberately repeated key-derivation calculation using the password and salt.", "Makes guessing many candidate passwords more computationally expensive."),
    ("Iterations / output size", "The code requests 120,000 iterations and a 256-bit result.", "These are explicit parameters for the current implementation; changing them later requires compatible verification and migration planning."),
    ("Constant-time comparison helper", "Compares UTF-8 bytes through MessageDigest.isEqual.", "Avoids a simple first-different-character comparison for the stored and computed hash strings."),
    ("PBEKeySpec.clearPassword()", "Clears the spec's internal password copy after deriving the hash.", "Reduces the time that this particular temporary copy remains in memory.")
], [1.55,2.9,2.85])
callout("Be precise", "Password hashing protects passwords from being stored as readable text. It does not encrypt the SQLite database file, report descriptions, or contact details at rest.")

heading("6. ReportService.java: business rules and database operations")
para("ReportService is the central application/service layer. It translates requests from the UI into SQL operations, applies ownership and administrator checks, converts database rows into model objects, and implements the matching score. Keeping this work outside the screen-building methods makes the UI easier to read and keeps important rules closer to the operations they govern.")
table(["Method", "Behavior and concepts"], [
    ("authenticate(email, password)", "Queries a user by parameterized email, reads the stored salt/hash, recomputes PBKDF2, safely compares hashes, and returns a User or null. Uses JDBC, try-with-resources, and a ResultSet."),
    ("register(name, email, password)", "Creates a fresh salt, hashes the password, then inserts account fields using PreparedStatement placeholders."),
    ("createFirstAdministrator(...)", "Opens a connection, starts a transaction, checks whether an admin exists, inserts the first admin, and commits. On SQL insert failure it rolls back. This groups related writes/checks into a transaction."),
    ("search(query, type, category, status)", "Builds a SELECT with optional filters, binds each filter value, executes it, converts every result row with read(), and returns a List<ItemReport>."),
    ("create(user, ...)", "Inserts a report and associates it with the current user's id."),
    ("matches(item)", "Searches Open reports of the opposite Lost/Found type, keeps candidates scoring at least 25, sorts highest first, and returns at most five."),
    ("score(a, b)", "Adds points for equal category, colour, location, close dates, and one shared descriptive term."),
    ("updateStatus(actor, id, status)", "Checks that the actor is admin and the status is allowed, then updates the database."),
    ("update(actor, id, ...)", "Updates a report only when the actor owns it or is admin; zero changed rows becomes a not-found/permission error."),
    ("delete(actor, id)", "Deletes only when the actor owns the report or the actor is admin."),
    ("verify(actor, id, value)", "Requires admin role and updates the report's verified flag."),
    ("read(ResultSet)", "Maps column values to an ItemReport; parses the stored ISO date string into LocalDate and maps integer flags to booleans.")
], [1.55,5.75], 7.6)
heading("Why PreparedStatement matters", 2)
code("PreparedStatement p = c.prepareStatement(\n    \"SELECT * FROM users WHERE email=?\");\np.setString(1, email.trim());\nResultSet r = p.executeQuery();")
para("The question mark is a parameter placeholder. The value is bound separately instead of being joined into the SQL text. This helps prevent input from changing the intended SQL command and makes values easier to handle consistently. The same pattern is used for inserts, updates, and deletes.")
heading("How dynamic search works", 2)
para("search() begins with a SELECT that always joins a report to its reporter. It appends a WHERE condition only when a filter is active, and adds the corresponding value to an args list. Once the SQL is complete, it binds each argument in order. This lets one method support many combinations of filters without writing a separate query for every combination.")
heading("Authorization checks", 2)
para("The UI hides or restricts actions for ordinary users, and ReportService checks critical permissions again. For update/delete, the SQL itself includes owner id or admin status as a condition. Rechecking in the service/database path is useful because UI visibility alone should not be the only protection for a sensitive operation.")
callout("Transaction detail", "createFirstAdministrator explicitly calls commit() after insertion and rollback() when that insert fails. A more robust transaction pattern would also ensure rollback on every failure after setAutoCommit(false); explain the current behavior accurately if asked.")

heading("7. Matching logic and the MatchEngine facade")
para("MatchEngine is a small facade: it exposes findMatches() and compatibilityScore() to the UI while delegating the actual query and scoring rules to ReportService. The facade gives the screen a named matching API without duplicating the database/service logic.")
table(["Comparison", "Points"], [
    ("Same category (case-insensitive)", "35"),
    ("Same nonblank colour", "20"),
    ("Same nonblank location", "20"),
    ("Date distance of 0-1 days / 2-7 days / 8-30 days", "20 / 12 / 5"),
    ("At least one shared word longer than 3 characters in name/description", "5"),
    ("Maximum possible score", "100")
], [5.5,1.8])
heading("Matching flow", 2)
code("new report is saved\n  -> search Open reports of opposite type\n  -> calculate score for each candidate\n  -> discard score < 25\n  -> sort descending by score\n  -> keep up to 5\n  -> show suggested item and contact details")
para("The score is an explainable heuristic, not machine learning and not proof that two reports refer to the same object. The user should review a suggestion before contacting anyone. A shared descriptive word is worth five points only once per report pair, even if several words overlap.")
para("The keyword step lowercases text with Locale.ROOT, splits on non-word characters, and uses a HashSet for the first report's words. A word must have more than three characters to count. Date distance is computed with ChronoUnit.DAYS.between and Math.abs.")

heading("8. module-info.java and pom.xml")
heading("Java module descriptor", 2)
code("module edu.jiit.lostlink {\n    requires javafx.controls;\n    requires java.sql;\n    requires java.prefs;\n    requires org.xerial.sqlitejdbc;\n    opens edu.jiit.lostlink.model to javafx.base;\n    exports edu.jiit.lostlink.ui;\n}")
para("The module descriptor names the application module, declares modules it depends on, opens model properties to JavaFX reflection where needed, and exports the UI package. Java's module system makes dependencies and package access more explicit than an unrestricted classpath.")
heading("Maven build file", 2)
para("pom.xml is XML configuration, not Java source. It tells Maven the project coordinates, source encoding, Java release level (21), JavaFX version, SQLite JDBC dependency, compiler plugin, and JavaFX run plugin. Maven downloads declared libraries and runs build goals such as clean, compile, and javafx:run.")
callout("Launch configuration", "The JavaFX plugin's mainClass names edu.jiit.lostlink.ui.LostLinkApp. When running locally, Maven must use a JDK (with javac), not only a JRE; the project also needs the configured JavaFX/SQLite dependencies.")

heading("9. Core Java concepts used in the code")
table(["Concept", "Where / how it appears", "What it helps with"], [
    ("Class and object", "Each .java class defines a type; new creates instances such as User or ReportService.", "Organizes related data and behavior into named units."),
    ("Encapsulation", "private fields with public getters in model classes.", "Controls access to object state and reduces accidental changes."),
    ("Constructor", "User(...), ItemReport(...), and service initializations.", "Sets up an object when it is created."),
    ("Access modifiers", "public methods, private helpers/fields, package and module declarations.", "Defines which code can use a class or member."),
    ("static", "Database utility methods, Passwords utilities, main(), and class initialization.", "Lets shared operations or state belong to a class rather than an instance."),
    ("final", "Immutable model fields and long-lived service references.", "Prevents reassignment after initialization."),
    ("Inheritance", "LostLinkApp extends Application; custom JavaFX cells extend TableCell.", "Integrates with JavaFX lifecycle and control extension points."),
    ("Overriding", "@Override start(...) and updateItem(...).", "Supplies behavior expected by a JavaFX superclass."),
    ("Interface", "Runnable and custom SqlAction functional interface used to represent an action.", "Allows code to accept behavior as a parameter."),
    ("Lambda expression", "e -> refresh(), () -> action, and comparator lambdas.", "Writes short event handlers and callbacks with less boilerplate."),
    ("Anonymous inner class", "Custom TableCell implementations inside the dashboard method.", "Keeps one-off specialized UI behavior near its use."),
    ("Generics", "List<ItemReport>, TableView<ItemReport>, ComboBox<String>, Result types.", "Makes the intended value type explicit and reduces unsafe casts."),
    ("Collections", "ArrayList, List, Set, HashSet, Arrays.asList.", "Stores variable-length results, arguments, and unique search terms."),
    ("Control flow", "if/else, loops, early return, and conditional expressions.", "Chooses paths based on user, data, and validation conditions."),
    ("Exception handling", "try/catch in UI handlers; throws SQLException in data/service methods.", "Lets lower layers report failure and UI show useful feedback."),
    ("Try-with-resources", "Connections, statements, and result sets.", "Automatically releases database resources."),
    ("JDBC", "Connection, PreparedStatement, ResultSet, SQL.", "Connects Java code to SQLite storage."),
    ("StringBuilder", "Builds filtered SQL and match-suggestion text.", "Efficiently accumulates strings in steps."),
    ("Date/time API", "LocalDate, ChronoUnit.DAYS, date validation and comparison.", "Represents calendar dates and measures date proximity."),
    ("Enum-like fixed strings", "Lost/Found and Open/Matched/Returned values.", "Represents a small set of supported choices; database checks also constrain values."),
    ("Ternary operator", "verified ? 'Yes' : 'No'; admin ownership flags.", "Chooses one of two values compactly."),
    ("Package organization", "ui, service, model, db, matching packages.", "Groups code by responsibility and prevents one giant source file.")
], [1.45,3.2,2.65], 7.5)
heading("Concepts not used as a main design feature", 2)
para("The implementation does not define its own inheritance hierarchy for User or ItemReport, does not use a REST API, and does not use streams as its main collection-processing style. It uses JavaFX's inheritance points, loops, lambdas, and ordinary collections. The fixed status/type values are strings with validation rather than Java enum types.")

heading("10. End-to-end examples")
heading("A. Sign-in", 2)
table(["Step", "Code path", "What happens"], [
    ("1", "Login button handler in LostLinkApp", "Reads email/password fields and calls service.authenticate(...)."),
    ("2", "ReportService.authenticate", "Runs a parameterized user query, reads salt/hash, recomputes and compares the password hash."),
    ("3", "Authentication result", "Returns User on success or null on mismatch; UI shows a generic credential error for mismatch."),
    ("4", "Dashboard", "Stores current User in UI state and builds dashboard controls for that account's role.")
], [.55,2.5,4.25])
heading("B. Create a report and find possible matches", 2)
table(["Step", "Code path", "What happens"], [
    ("1", "reportDialog", "Collects form values and runs validateReport before saving."),
    ("2", "ReportService.create", "Binds report values to an INSERT; user id ties the report to its author."),
    ("3", "MatchEngine.findMatches", "Asks ReportService for opposite-type Open candidates and scores each one."),
    ("4", "UI refresh and alert", "Refreshes the table and shows up to five suggestions, scores, and relevant contact details.")
], [.55,2.5,4.25])
heading("C. Delete a report", 2)
para("The UI checks whether the current user owns the selected report or is an admin before presenting the action. It asks for confirmation, then ReportService.delete executes a parameterized DELETE constrained by report id and ownership/admin condition. If no row is deleted, the method reports that the report was unavailable or permission was insufficient. On success, refresh reloads the table.")

heading("11. Explaining design decisions in a presentation")
table(["Faculty question", "Clear answer based on this code"], [
    ("Why JavaFX?", "It provides Java-based desktop controls, layouts, dialogs, tables, and event handling for the application screens."),
    ("Why SQLite?", "It is a lightweight embedded relational database that can store data locally without setting up a separate database server."),
    ("Where is the backend?", "The service layer is ReportService, with matching delegated through MatchEngine and persistence through Database. It runs inside the Java desktop process; it is not a remote server."),
    ("How are passwords stored?", "The app stores a random per-account salt and a PBKDF2WithHmacSHA256-derived hash, not the plain password."),
    ("How are SQL values protected?", "Values are bound using PreparedStatement rather than concatenated into the SQL statement."),
    ("How are matches chosen?", "The app adds weighted points for matching category, colour, location, date proximity, and shared words; it suggests up to five Open opposite-type reports scoring 25 or higher."),
    ("Why separate classes?", "The UI, models, database setup, account/report service, password helper, and matching facade have different responsibilities, which makes code easier to locate and change."),
    ("Is the app campus-wide?", "No. This version stores each user's data in a local SQLite file and does not synchronize between computers."),
    ("Is this machine-learning matching?", "No. It is a deterministic weighted heuristic that can be explained and manually checked."),
    ("What does admin do?", "The first-run setup creates an administrator. The app allows that account to verify reports, update statuses, and moderate reports."),
    ("What could be improved?", "Move long database work off the UI thread, strengthen tests, use enum types for status/type, improve transaction rollback coverage, and design a secured shared backend if campus-wide synchronization is required.")
], [2.0,5.3], 7.7)

heading("12. Source-file checklist")
table(["File", "What to remember when opening it"], [
    ("src/main/java/edu/jiit/lostlink/ui/LostLinkApp.java", "JavaFX app lifecycle, screens, controls, input validation, event handlers, and UI feedback."),
    ("src/main/java/edu/jiit/lostlink/service/ReportService.java", "Authentication, report CRUD/search, ownership/admin rules, result mapping, and match score."),
    ("src/main/java/edu/jiit/lostlink/service/Passwords.java", "Random salt generation and PBKDF2 hash derivation."),
    ("src/main/java/edu/jiit/lostlink/db/Database.java", "Local DB path, connections, schema, index, and initial admin lookup."),
    ("src/main/java/edu/jiit/lostlink/model/User.java", "Signed-in account data and role flag."),
    ("src/main/java/edu/jiit/lostlink/model/ItemReport.java", "Report data and values exposed to the UI."),
    ("src/main/java/edu/jiit/lostlink/matching/MatchEngine.java", "Small public facade for possible-match lookup and compatibility scores."),
    ("src/main/java/module-info.java", "Java module dependencies and package access."),
    ("pom.xml", "Maven dependencies, compiler release, and JavaFX launch class.")
], [4.0,3.3], 7.8)

heading("Short presentation summary")
callout("Suggested explanation", "LostLink is a Java 21 desktop app built with JavaFX and SQLite. LostLinkApp handles screens and user actions. ReportService contains the account, report, permission, and matching rules. Database handles SQLite connections and schema. User and ItemReport represent application data, while Passwords hashes credentials using salted PBKDF2. A weighted rule-based matcher suggests likely reports. The database is local to one computer, so this build is for a local demonstration rather than a synchronized campus service.")
para("Implementation detail to remember: the code validates many fields in the UI and has database constraints for selected fields, but the project is not a hardened multi-user server. Be transparent about the local deployment scope and the fact that the SQLite database is not encrypted at rest.")

# Keep metadata useful and avoid personal author data.
doc.core_properties.title = "LostLink Java Code and Concepts Guide"
doc.core_properties.subject = "File-by-file explanation of Java code and concepts used in LostLink"
doc.core_properties.author = "LostLink Project"
doc.core_properties.keywords = "Java, JavaFX, SQLite, JDBC, concepts, code guide"
doc.save(OUT)
print(OUT)
