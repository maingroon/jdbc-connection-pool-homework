package com.borovyk;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    private static final String URL = "jdbc:postgresql://93.175.204.87:5432/postgres";
    private static final String USER = "ju22user";
    private static final String PASS = "ju22pass";
    private static final String QUERY = """
            SELECT id, name, price, created_at
            FROM products
            WHERE length(name) = (SELECT max(length(name)) FROM products);
            """;

    @SneakyThrows
    public static void main(String[] args) {
        PGSimpleDataSource dataSource = initializePooledDataSource();
        for (int i = 0; i < 500; i++) {
            @Cleanup Connection connection = dataSource.getConnection();
            @Cleanup Statement statement = connection.createStatement();
            @Cleanup ResultSet resultSet = statement.executeQuery(QUERY);

            List<Product> longestProductsByName = new ArrayList<>();
            while (resultSet.next()) {
                longestProductsByName.add(new Product(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getBigDecimal("price"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()
                ));
            }

            longestProductsByName.stream()
                    .max(Comparator.comparing(Product::price))
                    .ifPresent(System.out::println);
        }
    }

    private static PGSimpleDataSource initializeDataSource() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL(URL);
        dataSource.setUser(USER);
        dataSource.setPassword(PASS);
        return dataSource;
    }

    private static PooledDataSource initializePooledDataSource() {
        return new PooledDataSource(URL, USER, PASS);
    }

    record Product(Integer id, String name, BigDecimal price, LocalDateTime createdAt) {}

}
