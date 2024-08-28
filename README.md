# Library Management System

## Overview
This project is a Java-based Library Management System using PostgreSQL. It focuses on database transactions and locking with JDBC. From course SWEN304.

## Features
- **Book Management**: Lookup, borrow, and return books.
- **User Management**: Retrieve author and customer information.
- **Transaction Handling**: Ensures consistency with proper transaction management.
- **GUI Integration**: User interface connected to the library system.

## Setup & Demo Instructions

1. **Open Xming and Cygwin**

2. **Connect to the Remote System**:
    ```bash
    plink -ssh -2 -X <username>@greta-pt.ecs.vuw.ac.nz
    ```

3. **Run the Application**:
    ```bash
    env CLASSPATH=/usr/pkg/lib/java/postgresql_jdbc.jar:. java LibraryUI
    ```

This will launch the library system for remote interaction.
