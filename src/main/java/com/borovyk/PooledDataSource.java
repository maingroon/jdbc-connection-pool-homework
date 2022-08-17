package com.borovyk;

import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PooledDataSource extends PGSimpleDataSource {

    private final Queue<ConnectionProxy> connectionQueue = new LinkedBlockingQueue<>();

    @SneakyThrows
    public PooledDataSource(String url, String user, String password) {
        setURL(url);
        setUser(user);
        setPassword(password);

        for (int i = 0; i < 10; i++) {
            connectionQueue.add(new ConnectionProxy(super.getConnection(), this));
        }
    }

    @SneakyThrows
    @Override
    public Connection getConnection() throws SQLException {
        while (connectionQueue.isEmpty()) {
            wait(10);
        }
        return connectionQueue.poll();
    }

    public void returnConnection(ConnectionProxy connection) {
        connectionQueue.add(connection);
    }

}
