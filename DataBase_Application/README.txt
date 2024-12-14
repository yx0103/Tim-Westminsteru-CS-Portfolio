This project is a Java-based point-of-sale application designed to interface with a hardware store’s sales database. The database stores key information about customers, products, and invoices. By interacting with the database, the application allows a store clerk (or an automated checkout station) to quickly create new customers, record purchases, and generate invoices.

Key Features
	1.	Customer Lookup and Creation
	•	Existing Customer Entry:
The application starts by prompting the user for a customer number. If a valid existing customer number is entered, that customer’s information is retrieved from the database.
	•	New Customer Enrollment:
If the clerk types “new” instead of a customer number, the program will ask for the new customer’s name and email address. It then automatically creates a new customer record in the database. After this, the system behaves as if this new customer was the one who just checked in.
	2.	Invoice Creation
	•	Once the customer has been identified or created, the application starts a new invoice for them.
	•	The invoice automatically stores the current date and time to keep accurate transaction records.
	3.	Item Scanning and Invoicing
	•	The application then enters a loop where it prompts for item codes. In a real-world scenario, these would be scanned from a barcode, but here the user can manually input them.
	•	Item Validation:
If an invalid code (one that doesn’t match any known product) is entered, the application notifies the user and continues prompting for valid codes.
	•	Quantity Entry:
For each valid item code, the user is asked for the quantity they are selling. The application then calculates the line cost (quantity × unit price) and adds a line to the invoice.
	•	Invoice Preview:
After each item is added, the application displays a running view of the invoice — including line number, item code, description, quantity, and line cost — so that the clerk can confirm entries before finalizing the sale.
	4.	Finalization and Total Calculation
	•	When the clerk finishes adding items (by pressing enter or leaving the item code prompt blank), the application calculates the total invoice amount.
	•	Database-Based Calculation:
Rather than summing the line items within the application’s logic, the total is computed using a database query. This ensures the integrity of the data and leverages the database engine’s computation capabilities.
	•	The final invoice total is displayed to the user, marking the end of that transaction.
