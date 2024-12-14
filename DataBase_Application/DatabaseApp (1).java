import java.sql.*;
import java.util.*;
import java.io.IOException;
import java.nio.file.*;

/**
 * A template for a simple app that interfaces with a database on the class server.
 * This program is complicated by the fact that the class database server is behind a firewall and we are not allowed to
 * connect directly to the MariaDB server running on it. As a workaround, we set up an ssh tunnel (this is the purpose
 * of the SSHTunnel class) and then connect through that. In a more normal database application setting (in
 * particular if you are writing a database app that connects to a server running on the same computer) you would not
 * have to bother with the tunnel and could just connect directly.
 */

// If you change the name of this class (and you should) you need to change it in at least two other places:
//   - The constructor below
//   - In main(), where the class is instantiated
public class DatabaseApp implements AutoCloseable {

    // Default connection information (most can be overridden with command-line arguments)
    // Change these as needed for your app. (You should create a token for your database and use its username
    // and password here.)
    private static final String DB_NAME = "yx0103_sales";
    private static final String DB_USER = "token_ceda";
    private static final String DB_PASSWORD = "coB7CYB3Hx1jJKDQ";

    // You can define queries using static final Strings like this.
    private static final String SQL_INSERT_CUSTOMER = "INSERT INTO Customer (first, last, email) VALUES (?, ?, ?)";
    private static final String SQL_INSERT_INVOICE = "INSERT INTO Invoice (cust_num, date) VALUES (?, NOW())";
    private static final String SQL_INSERT_INVOICE_LINE = "INSERT INTO InvoiceLine (invoice_num, line_no, item_code, qty, cost) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_QUERY_ITEM = "SELECT description, unit_price FROM Item WHERE code = ?";
    private static final String SQL_QUERY_INVOICE_TOTAL = "SELECT SUM(cost) AS total FROM InvoiceLine WHERE invoice_num = ?";
    private static final String SQL_QUERY_INVOICE_LINES = """
        SELECT line_no, item_code, qty, cost
        FROM InvoiceLine
        WHERE invoice_num = ?
    """;
    private static final String SQL_GET_MAX_LINE_NO = "SELECT COALESCE(MAX(line_no), 0) AS max_line_no FROM InvoiceLine WHERE invoice_num = ?";
    // Declare one of these for every query your program will use.
    private PreparedStatement insertCustomerStmt;
    private PreparedStatement insertInvoiceStmt;
    private PreparedStatement insertInvoiceLineStmt;
    private PreparedStatement queryItemStmt;
    private PreparedStatement queryInvoiceTotalStmt;
    private PreparedStatement queryInvoiceLinesStmt;
    private PreparedStatement getMaxLineNoStmt;

    // Connection information to use
    private final String dbHost;
    private final int dbPort;
    private final String dbName;
    private final String dbUser;
    private final String dbPassword;
    private Connection connection;

    /**
     * Creates an {@code IMDbEpisodeQuery} with the specified connection information.
     * @param sshKeyfile the filename of the private key to use for ssh
     * @param dbName the name of the database to use
     * @param dbUser the username to use when connecting
     * @param dbPassword the password to use when connecting
     * @throws SQLException if unable to connect
     */
    public DatabaseApp(String dbHost, int dbPort, String dbName,
                       String dbUser, String dbPassword) throws SQLException {
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        connect();
    }

    private void connect() throws SQLException {
        final String url = String.format("jdbc:mariadb://%s:%d/%s?user=%s&password=%s", dbHost, dbPort, dbName, dbUser, dbPassword);
        this.connection = DriverManager.getConnection(url);

        this.insertCustomerStmt = this.connection.prepareStatement(SQL_INSERT_CUSTOMER, Statement.RETURN_GENERATED_KEYS);
        this.insertInvoiceStmt = this.connection.prepareStatement(SQL_INSERT_INVOICE, Statement.RETURN_GENERATED_KEYS);
        this.insertInvoiceLineStmt = this.connection.prepareStatement(SQL_INSERT_INVOICE_LINE);
        this.queryItemStmt = this.connection.prepareStatement(SQL_QUERY_ITEM);
        this.queryInvoiceTotalStmt = this.connection.prepareStatement(SQL_QUERY_INVOICE_TOTAL);
        this.queryInvoiceLinesStmt = this.connection.prepareStatement(SQL_QUERY_INVOICE_LINES);
        this.getMaxLineNoStmt = this.connection.prepareStatement(SQL_GET_MAX_LINE_NO);
    }

    /**
     * Runs the application.
     */
    public void runApp() throws SQLException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Add a new customer");
            System.out.println("2. Create an invoice for an existing customer");
            System.out.println("3. Display an invoice");
            System.out.println("4. Quit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addNewCustomer(scanner);
                    break;

                case "2":
                    System.out.print("Enter customer number: ");
                    String customerInput = scanner.nextLine();
                    int customerNum = Integer.parseInt(customerInput);
                    int invoiceNum = createInvoice(customerNum);
                    processItems(scanner, invoiceNum);
                    break;

                case "3":
                    System.out.print("Enter invoice number to display: ");
                    String invoiceInput = scanner.nextLine();
                    int invoiceToDisplay = Integer.parseInt(invoiceInput);
                    displayInvoice(invoiceToDisplay);
                    break;

                case "4":
                    System.out.println("Exiting the application.");
                    return;

                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

    // Add one method here for each database operation your app will perform, then call them from runApp() above

    // An example of a method that runs a query
    private int addNewCustomer(Scanner scanner) throws SQLException {
        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        insertCustomerStmt.setString(1, firstName);
        insertCustomerStmt.setString(2, lastName);
        insertCustomerStmt.setString(3, email);
        insertCustomerStmt.executeUpdate();

        try (ResultSet generatedKeys = insertCustomerStmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve customer ID.");
            }
        }
    }

    // An example of a method that inserts a new row
    private int createInvoice(int customerNum) throws SQLException {
        insertInvoiceStmt.setInt(1, customerNum);
        insertInvoiceStmt.executeUpdate();

        try (ResultSet generatedKeys = insertInvoiceStmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve invoice number.");
            }
        }
    }

    private void processItems(Scanner scanner, int invoiceNum) throws SQLException {
        while (true) {
            System.out.print("Enter item code (or blank to finish): ");
            String itemCode = scanner.nextLine();
            if (itemCode.isBlank()) break;

            queryItemStmt.setString(1, itemCode);
            try (ResultSet itemResult = queryItemStmt.executeQuery()) {
                if (itemResult.next()) {
                    String description = itemResult.getString("description");
                    double unitPrice = itemResult.getDouble("unit_price");

                    System.out.print("Enter quantity: ");
                    int quantity = Integer.parseInt(scanner.nextLine());
                    double cost = unitPrice * quantity;

                    int lineNo = getNextLineNo(invoiceNum);

                    insertInvoiceLineStmt.setInt(1, invoiceNum);
                    insertInvoiceLineStmt.setInt(2, lineNo); // add line number here
                    insertInvoiceLineStmt.setString(3, itemCode);
                    insertInvoiceLineStmt.setInt(4, quantity);
                    insertInvoiceLineStmt.setDouble(5, cost);
                    insertInvoiceLineStmt.execute();

                    System.out.printf("Added %d of %s (%.2f each) for a total of %.2f\n", quantity, description, unitPrice, cost);
                } else {
                    System.out.println("Invalid item code. Please try again.");
                }
            }
        }
    }

    private int getNextLineNo(int invoiceNum) throws SQLException {
        getMaxLineNoStmt.setInt(1, invoiceNum);
        try (ResultSet rs = getMaxLineNoStmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("max_line_no") + 1;
            } else {
                return 1; // Start with line_no 1 if no entries exist
            }
        }
    }

    private void displayInvoice(int invoiceNum) throws SQLException {
        System.out.println("\nCurrent Invoice:");
        queryInvoiceLinesStmt.setInt(1, invoiceNum);

        try (ResultSet rs = queryInvoiceLinesStmt.executeQuery()) {
            while (rs.next()) {
                int lineNo = rs.getInt("line_no");
                String itemCode = rs.getString("item_code");
                int quantity = rs.getInt("qty");
                double cost = rs.getDouble("cost");

                System.out.printf("Line %d: Code %s, Quantity: %d, Cost: %.2f\n", lineNo, itemCode, quantity, cost);
            }
        }

        queryInvoiceTotalStmt.setInt(1, invoiceNum);
        try (ResultSet rsTotal = queryInvoiceTotalStmt.executeQuery()) {
            if (rsTotal.next()) {
                double total = rsTotal.getDouble("total");
                System.out.printf("Invoice Total: %.2f\n", total);
            }
        }
    }
    /**
     * Closes the connection to the database.
     */
    @Override
    public void close() throws SQLException {
        connection.close();
    }

    /**
     * Entry point of the application. Uses command-line parameters to override database
     * connection settings, then invokes runApp().
     */
    public static void main(String... args) {
        // Default connection parameters (can be overridden on command line)
        Map<String, String> params = new HashMap<>(Map.of(
                "dbname", "" + DB_NAME,
                "user", DB_USER,
                "password", DB_PASSWORD
        ));

        boolean printHelp = false;

        // Parse command-line arguments, overriding values in params
        for (int i = 0; i < args.length && !printHelp; ++i) {
            String arg = args[i];
            boolean isLast = (i + 1 == args.length);

            switch (arg) {
                case "-h":
                case "-help":
                    printHelp = true;
                    break;

                case "-dbname":
                case "-user":
                case "-password":
                    if (isLast)
                        printHelp = true;
                    else
                        params.put(arg.substring(1), args[++i]);
                    break;

                default:
                    System.err.println("Unrecognized option: " + arg);
                    printHelp = true;
            }
        }

        // If help was requested, print it and exit
        if (printHelp) {
            printHelp();
            return;
        }

        // Connect to the database. This use of "try" ensures that the database connection
        // is closed, even if an exception occurs while running the app.
        try (SSHTunnel tunnel = new SSHTunnel();
             DatabaseApp app = new DatabaseApp(
                     "localhost", tunnel.getLocalPort(), params.get("dbname"),
                     params.get("user"), params.get("password")
             )) {

            // Run the application
            try {
                app.runApp();
            } catch (SQLException ex) {
                System.err.println("\n\n=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
                System.err.println("SQL error when running database app!\n");
                ex.printStackTrace();
                System.err.println("\n\n=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            }
        } catch (SQLException ex) {
            System.err.println("Error communicating with the database (see full message below).");
            ex.printStackTrace();
            System.err.println("\nParameters used to connect to the database:");
            System.err.printf("\tSSH keyfile: %s\n\tDatabase name: %s\n\tUser: %s\n\tPassword: %s\n\n",
                    params.get("sshkeyfile"), params.get("dbname"),
                    params.get("user"), params.get("password")
            );
            System.err.println("(Is the MySQL connector .jar in the CLASSPATH?)");
            System.err.println("(Are the username and password correct?)");
        }

    }

    private static void printHelp() {
        System.out.println("Accepted command-line arguments:");
        System.out.println();
        System.out.println("\t-help, -h          display this help text");
        System.out.println("\t-dbname <text>     override name of database to connect to");
        System.out.printf( "\t                   (default: %s)\n", DB_NAME);
        System.out.println("\t-user <text>       override database user");
        System.out.printf( "\t                   (default: %s)\n", DB_USER);
        System.out.println("\t-password <text>   override database password");
        System.out.println();
    }
}
