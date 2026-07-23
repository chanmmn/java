package com.example;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Reads and modifies rows from the Northwind "products" table in PostgreSQL,
 * using Hibernate instead of plain JDBC.
 *
 * Connection settings are read from environment variables so no credentials
 * are hard-coded:
 *   PGHOST     (default: localhost)
 *   PGPORT     (default: 5432)
 *   PGDATABASE (default: northwind)
 *   PGUSER     (default: postgres)
 *   PGPASSWORD (default: Abcd1234)
 */
public class NorthwindProducts {

    public static void main(String[] args) {
        try (SessionFactory sessionFactory = buildSessionFactory()) {
            System.out.println("Products before changes:");
            listProducts(sessionFactory);

            Product inserted = insertProduct(sessionFactory, "Test Widget", "24 - 1 kg pkgs.", 9.99);
            System.out.printf("%nInserted product with id %d%n", inserted.getProductId());

            // boolean modified = modifyProduct(sessionFactory, inserted.getProductId(),
            //         "Test Widget (Updated)", "12 - 1 kg pkgs.", 12.49);
            // System.out.printf("Modified product %d: %b%n", inserted.getProductId(), modified);

            System.out.println("\nProducts after insert/modify:");
            listProducts(sessionFactory);

            // boolean deleted = deleteProduct(sessionFactory, inserted.getProductId());
            // System.out.printf("%nDeleted product %d: %b%n", inserted.getProductId(), deleted);

            System.out.println("\nProducts after delete:");
            listProducts(sessionFactory);
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static SessionFactory buildSessionFactory() {
        String host = env("PGHOST", "localhost");
        String port = env("PGPORT", "5432");
        String database = env("PGDATABASE", "northwind");
        String user = env("PGUSER", "postgres");
        String password = env("PGPASSWORD", "Abcd1234");

        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);

        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", url);
        configuration.setProperty("hibernate.connection.username", user);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.addAnnotatedClass(Product.class);

        return configuration.buildSessionFactory();
    }

    /**
     * Reads and prints all rows from the "products" table.
     */
    public static void listProducts(SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery(
                    "from Product order by productId", Product.class);
            List<Product> products = query.list();

            System.out.printf("%-4s  %-40s  %-10s  %6s%n",
                    "ID", "Product Name", "unit", "Price");
            System.out.println("-".repeat(80));

            int count = 0;
            for (Product product : products) {
                System.out.printf("%-4d  %-40s  %-10s  %6.2f%n",
                        product.getProductId(),
                        product.getProductName(),
                        product.getUnit(),
                        product.getPrice());
                count++;
            }

            System.out.println("-".repeat(80));
            System.out.printf("%d product(s) read.%n", count);
        }
    }

    /**
     * Inserts a new product row and returns the generated product with its id populated.
     */
    public static Product insertProduct(SessionFactory sessionFactory, String productName, String unit, Double price) {
        Product product = new Product(productName, unit, price);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.persist(product);
                transaction.commit();
                return product;
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    /**
     * Updates the name, unit and price of an existing product identified by productId.
     * Returns true if a matching product was found and updated, false otherwise.
     */
    // public static boolean modifyProduct(SessionFactory sessionFactory, Integer productId,
    //                                      String productName, String unit, Double price) {
    //     try (Session session = sessionFactory.openSession()) {
    //         Transaction transaction = session.beginTransaction();
    //         try {
    //             Product product = session.get(Product.class, productId);
    //             if (product == null) {
    //                 transaction.rollback();
    //                 return false;
    //             }
    //             product.setProductName(productName);
    //             product.setUnit(unit);
    //             product.setPrice(price);
    //             transaction.commit();
    //             return true;
    //         } catch (Exception e) {
    //             if (transaction.isActive()) {
    //                 transaction.rollback();
    //             }
    //             throw e;
    //         }
    //     }
    // }

    /**
     * Deletes the product identified by productId.
     * Returns true if a matching product was found and deleted, false otherwise.
     */
    // public static boolean deleteProduct(SessionFactory sessionFactory, Integer productId) {
    //     try (Session session = sessionFactory.openSession()) {
    //         Transaction transaction = session.beginTransaction();
    //         try {
    //             Product product = session.get(Product.class, productId);
    //             if (product == null) {
    //                 transaction.rollback();
    //                 return false;
    //             }
    //             session.remove(product);
    //             transaction.commit();
    //             return true;
    //         } catch (Exception e) {
    //             if (transaction.isActive()) {
    //                 transaction.rollback();
    //             }
    //             throw e;
    //         }
    //     }
    // }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
