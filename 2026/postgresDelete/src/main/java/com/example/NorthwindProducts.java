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

            System.out.println("\nProducts after insert/modify:");
            listProducts(sessionFactory);

            boolean deleted = deleteProduct(sessionFactory, 85);
            System.out.printf("%nDeleted product %d: %b%n", 85, deleted);

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
        String password = env("PGPASSWORD", "password");

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
     * Deletes the product identified by productId.
     * Returns true if a matching product was found and deleted, false otherwise.
     */
    public static boolean deleteProduct(SessionFactory sessionFactory, Integer productId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Product product = session.get(Product.class, productId);
                if (product == null) {
                    transaction.rollback();
                    return false;
                }
                session.remove(product);
                transaction.commit();
                return true;
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
