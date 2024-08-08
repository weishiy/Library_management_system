/*
 * LibraryModel.java
 * Author: Shiyan Wei, 300569298
 * Created on:
 */


import javax.swing.*;
import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryModel {

    private final String url;
    private final String userid;
    private final String password;

    private final List<Connection> connections = new ArrayList<>();
    JFrame temp;
    public LibraryModel(JFrame parent, String userid, String password) {
        temp=parent;
        url = "jdbc:postgresql://depot.ecs.vuw.ac.nz:5432/weishiy_jdbc";
        this.userid = userid;
        this.password = password;
    }

    public String bookLookup(int isbn) {
        return show("SELECT * FROM book WHERE isbn = ?",
                List.of(Integer.class),
                List.of(isbn),
                List.of("isbn", "title", "edition_no", "numofcop", "numleft"),
                List.of(Integer.class, String.class, Integer.class, Integer.class, Integer.class));
    }

    public String showCatalogue() {
        return show("SELECT * FROM book",
                List.of(),
                List.of(),
                List.of("isbn", "title", "edition_no", "numofcop", "numleft"),
                List.of(Integer.class, String.class, Integer.class, Integer.class, Integer.class));
    }

    public String showLoanedBooks() {
        return show("""
                                select b.isbn, b.title, c.customerid, c.l_name, c.f_name, cb.duedate
                                from book b, cust_book cb, customer c
                                where b.isbn = cb.isbn
                                and cb.customerid= c.customerid
                        """,
                List.of(),
                List.of(),
                List.of("isbn", "title", "customerid", "l_name", "f_name", "duedate"),
                List.of(Integer.class, String.class, Integer.class, String.class, String.class, Date.class));
    }

    public String showAuthor(int authorID) {
        return show("SELECT * FROM author WHERE authorid = ?",
                List.of(Integer.class),
                List.of(authorID),
                List.of("authorid", "name", "surname"),
                List.of(Integer.class, String.class, String.class));
    }

    public String showAllAuthors() {
        return show("SELECT * FROM author",
                List.of(),
                List.of(),
                List.of("authorid", "name", "surname"),
                List.of(Integer.class, String.class, String.class));
    }

    public String showCustomer(int customerID) {
        return show("SELECT * FROM customer WHERE customerid = ?",
                List.of(Integer.class),
                List.of(customerID),
                List.of("customerid", "l_name", "f_name", "city"),
                List.of(Integer.class, String.class, String.class, String.class));
    }

    public String showAllCustomers() {
        return show("SELECT * FROM customer",
                List.of(),
                List.of(),
                List.of("customerid", "l_name", "f_name", "city"),
                List.of(Integer.class, String.class, String.class, String.class));
    }

    public String borrowBook(int isbn, int customerID,
                             int day, int month, int year) {
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            Class.forName("org.postgresql.Driver");

            conn = DriverManager.getConnection(url, userid, password);
            connections.add(conn);

            conn.setAutoCommit(false);

            stmt1 = conn.prepareStatement("SELECT * FROM book WHERE isbn = ? FOR UPDATE");
            stmt1.setInt(1, isbn);
            rs1 = stmt1.executeQuery();

            if (!rs1.next()) {
                conn.rollback();
                return "No matching book found";
            }

            if (rs1.getInt("numleft") == 0) {
                conn.rollback();
                return "No book left";
            }

            stmt2 = conn.prepareStatement("SELECT * FROM customer WHERE customerid = ? FOR UPDATE");
            stmt2.setInt(1, customerID);
            rs2 = stmt2.executeQuery();

            if (!rs2.next()) {
                conn.rollback();
                return "No matching customer found";
            }

            stmt3 = conn.prepareStatement("INSERT INTO cust_book VALUES (?, ?, ?)");
            stmt3.setInt(1, isbn);
            stmt3.setDate(2, Date.valueOf(LocalDate.of(year, month, day)));
            stmt3.setInt(3, customerID);
            stmt3.executeUpdate();

            int response = JOptionPane.showConfirmDialog(temp, "abs","more text", JOptionPane.YES_NO_OPTION);

            stmt4 = conn.prepareStatement("UPDATE book SET numleft = numleft - 1 WHERE isbn = ?");
            stmt4.setInt(1, isbn);
            stmt4.executeUpdate();

            conn.commit();

            return "Done";
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back due to an error.");
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return "Internal server error";
        } finally {
            connections.remove(conn);
            try {
                if (rs1 != null) rs1.close();
                if (rs2 != null) rs2.close();
                if (stmt1 != null) stmt1.close();
                if (stmt2 != null) stmt2.close();
                if (stmt3 != null) stmt3.close();
                if (stmt4 != null) stmt4.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String returnBook(int isbn, int customerid) {
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;
        PreparedStatement stmt5 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;

        try {
            Class.forName("org.postgresql.Driver");

            conn = DriverManager.getConnection(url, userid, password);
            connections.add(conn);

            conn.setAutoCommit(false);

            stmt1 = conn.prepareStatement("SELECT * FROM book WHERE isbn = ? FOR UPDATE");
            stmt1.setInt(1, isbn);
            rs1 = stmt1.executeQuery();

            if (!rs1.next()) {
                conn.rollback();
                return "No matching book found";
            }

            stmt2 = conn.prepareStatement("SELECT * FROM customer WHERE customerid = ? FOR UPDATE");
            stmt2.setInt(1, customerid);
            rs2 = stmt2.executeQuery();

            if (!rs2.next()) {
                conn.rollback();
                return "No matching customer found";
            }

            stmt3 = conn.prepareStatement("SELECT * FROM cust_book WHERE isbn = ? AND customerid = ? FOR UPDATE");
            stmt3.setInt(1, isbn);
            stmt3.setInt(2, customerid);
            rs3 = stmt3.executeQuery();

            if (!rs3.next()) {
                conn.rollback();
                return "No matching borrow record found";
            }

            stmt4 = conn.prepareStatement("DELETE FROM cust_book WHERE isbn = ? AND customerid = ?");
            stmt4.setInt(1, isbn);
            stmt4.setInt(2, customerid);
            stmt4.executeUpdate();

            stmt5 = conn.prepareStatement("UPDATE book SET numleft = numleft + 1 WHERE isbn = ?");
            stmt5.setInt(1, isbn);
            stmt5.executeUpdate();

            conn.commit();

            return "Done";
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back due to an error.");
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return "Internal server error";
        } finally {
            connections.remove(conn);
            try {
                if (rs1 != null) rs1.close();
                if (rs2 != null) rs2.close();
                if (rs3 != null) rs3.close();
                if (stmt1 != null) stmt1.close();
                if (stmt2 != null) stmt2.close();
                if (stmt3 != null) stmt3.close();
                if (stmt4 != null) stmt4.close();
                if (stmt5 != null) stmt5.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeDBConnection() {
        connections.forEach(c -> {
            try {
                if (c != null) c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public String deleteCus(int customerID) {
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            Class.forName("org.postgresql.Driver");

            conn = DriverManager.getConnection(url, userid, password);
            connections.add(conn);

            conn.setAutoCommit(false);

            stmt1 = conn.prepareStatement("SELECT * FROM customer WHERE customerid = ? FOR UPDATE");
            stmt1.setInt(1, customerID);
            rs1 = stmt1.executeQuery();

            if (!rs1.next()) {
                conn.rollback();
                return "No matching customer found";
            }

            stmt2 = conn.prepareStatement("SELECT * FROM cust_book WHERE customerid = ? FOR UPDATE");
            stmt2.setInt(1, customerID);
            rs2 = stmt2.executeQuery();

            if (rs2.next()) { 
                conn.rollback();
                return "Customer ID " + customerID + " cannot be deleted as they have borrowed books";
            }

            stmt3 = conn.prepareStatement("DELETE FROM customer WHERE customerid = ?");
            stmt3.setInt(1, customerID);
            stmt3.executeUpdate();

            conn.commit();

            return "Deleted customer " + customerID;
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back due to an error.");
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return "Internal server error";
        } finally {
            connections.remove(conn);
            try {
                if (rs1 != null) rs1.close();
                if (rs2 != null) rs2.close();
                if (stmt1 != null) stmt1.close();
                if (stmt2 != null) stmt2.close();
                if (stmt3 != null) stmt3.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String deleteAuthor(int authorID) {
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            Class.forName("org.postgresql.Driver");

            conn = DriverManager.getConnection(url, userid, password);
            connections.add(conn);

            conn.setAutoCommit(false);

            stmt1 = conn.prepareStatement("SELECT * FROM author WHERE authorid = ? FOR UPDATE");
            stmt1.setInt(1, authorID);
            rs1 = stmt1.executeQuery();

            if (!rs1.next()) {
                conn.rollback();
                return "No matching author found";
            }

            stmt2 = conn.prepareStatement("SELECT * FROM book_author WHERE authorid = ?  FOR UPDATE");
            stmt2.setInt(1, authorID);
            rs2 = stmt2.executeQuery();

            if (rs2.next()) {
                conn.rollback();
                return "Author ID " + authorID + " cannot be deleted as they books associated";
            }

            stmt3 = conn.prepareStatement("DELETE FROM author WHERE authorid = ?");
            stmt3.setInt(1, authorID);
            stmt3.executeUpdate();

            conn.commit();

            return "Deleted author " + authorID;
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back due to an error.");
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return "Internal server error";
        } finally {
            connections.remove(conn);
            try {
                if (rs1 != null) rs1.close();
                if (rs2 != null) rs2.close();
                if (stmt1 != null) stmt1.close();
                if (stmt2 != null) stmt2.close();
                if (stmt3 != null) stmt3.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String deleteBook(int isbn) {
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            Class.forName("org.postgresql.Driver");

            conn = DriverManager.getConnection(url, userid, password);
            connections.add(conn);

            conn.setAutoCommit(false);

            stmt1 = conn.prepareStatement("SELECT * FROM book WHERE isbn = ? FOR UPDATE");
            stmt1.setInt(1, isbn);
            rs1 = stmt1.executeQuery();

            if (!rs1.next()) {
                conn.rollback();
                return "No matching book found";
            }

            stmt2 = conn.prepareStatement("SELECT * FROM cust_book WHERE isbn = ? FOR UPDATE");
            stmt2.setInt(1, isbn);
            rs2 = stmt2.executeQuery();

            if (rs2.next()) { 
                conn.rollback();
                return "Cannot delete the book from the catalog as some copies are currently borrowed";
            }

            stmt3 = conn.prepareStatement("DELETE FROM book_author WHERE isbn = ?");
            stmt3.setInt(1, isbn);
            stmt3.executeUpdate();

            stmt4 = conn.prepareStatement("DELETE FROM book WHERE isbn = ?");
            stmt4.setInt(1, isbn);
            stmt4.executeUpdate();

            conn.commit();

            return "Delete book " + isbn + " from catalogue";
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back due to an error.");
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return "Internal server error";
        } finally {
            connections.remove(conn);
            try {
                if (rs1 != null) rs1.close();
                if (rs2 != null) rs2.close();
                if (stmt1 != null) stmt1.close();
                if (stmt2 != null) stmt2.close();
                if (stmt3 != null) stmt3.close();
                if (stmt4 != null) stmt4.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String show(String sqlQuery,
                        List<Type> paramTypes,
                        List<Object> paramValues,
                        List<String> columnNames,
                        List<Type> columnTypes) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("org.postgresql.Driver");

            conn = DriverManager.getConnection(url, userid, password);
            connections.add(conn);

            conn.setAutoCommit(false);
            conn.setReadOnly(true);

            stmt = conn.prepareStatement(sqlQuery);

            for (int i = 0; i < paramTypes.size(); i++) {
                if (Integer.class.equals(paramTypes.get(i))) {
                    stmt.setInt(i + 1, (Integer) paramValues.get(i));
                } else if (String.class.equals(paramTypes.get(i))) {
                    stmt.setString(i + 1, (String) paramValues.get(i));
                } else if (Date.class.equals(paramTypes.get(i))) {
                    stmt.setDate(i + 1, (Date) paramValues.get(i));
                } else {
                    throw new UnsupportedOperationException("Unsupported type: " + paramTypes.get(i));
                }
            }

            rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) { 
                return "No data found";
            }

            var builder = new StringBuilder();
            builder.append(String.join(", ", columnNames).toUpperCase());
            builder.append("\n");
            while (rs.next()) {
                var values = new ArrayList<String>();

                for (int i = 0; i < columnNames.size(); i++) {
                    var name = columnNames.get(i);
                    var type = columnTypes.get(i);

                    if (Integer.class.equals(type)) {
                        values.add(Integer.toString(rs.getInt(name)));
                    } else if (String.class.equals(type)) {
                        values.add(rs.getString(name));
                    } else if (Date.class.equals(type)) {
                        values.add(rs.getDate(name).toString());
                    } else {
                        throw new UnsupportedOperationException("Unsupported type: " + type);
                    }
                }

                builder.append(String.join(", ", values));
                builder.append("\n");
            }
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Internal server error";
        } finally {
            connections.remove(conn);
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}