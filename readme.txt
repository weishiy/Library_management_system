### Project 2 - Library Management System - PostgreSQL and Java

## Overview
This project involves creating a library management system using Java and PostgreSQL, focusing on database transaction processing and locking using JDBC.

## What I Did

1. **Loaded the Database**
   - Loaded the database schema and initial data from `Project2_24.data`.

2. **Implemented the LibraryModel Class**
   - **Constructor**: Defined the constructor to open a connection to the PostgreSQL database using JDBC.
   - **Methods**: Implemented methods to perform appropriate SQL queries and return results as strings. The methods include:
     - Book lookup
     - Catalog display
     - Borrowing and returning books
     - Retrieving author and customer information
   - **Transaction Handling**: Ensured correct transaction handling (starting, ending, and locking) to maintain database consistency.
   - **Exception Handling**: Handled exceptions raised by JDBC commands to ensure smooth operation.

3. **Implemented the BorrowBook Transaction**
   - Checked if the customer exists and locked their record.
   - Locked the book record if it exists and a copy is available.
   - Inserted a record into the `Cust_Book` table.
   - Updated the `Book` table to reflect the loan.
   - Committed the transaction if all actions were successful, otherwise performed a rollback.
   - Added an interaction command to simulate a multi-user environment, allowing testing for lost updates and dirty/unrepeatable reads.

4. **Implemented the ReturnBook Transaction**
   - Checked if the customer and book records exist and locked them.
   - Deleted the appropriate record from the `Cust_Book` table.
   - Updated the `Book` table to reflect the returned book.
   - Committed the transaction if all actions were successful, otherwise performed a rollback.

5. **Connected GUI**
   - Ensured that the `LibraryUI.java` interacts correctly with the methods in `LibraryModel.java`.

## Demo Instructions

To run the demo, follow these steps:

1. **Open Xming and Cygwin**

2. **Connect to the Remote System**

    ```bash
    plink -ssh -2 -X <username>@greta-pt.ecs.vuw.ac.nz
    ```

3. **Run the Application**

    ```bash
    env CLASSPATH=/usr/pkg/lib/java/postgresql_jdbc.jar:. java LibraryUI
    ```

This will start the application, and you can interact with the library system remotely.

