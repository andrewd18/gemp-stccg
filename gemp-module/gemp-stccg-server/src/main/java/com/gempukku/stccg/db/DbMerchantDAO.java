package com.gempukku.stccg.db;

import java.sql.*;
import java.util.Date;

public class DbMerchantDAO implements MerchantDAO {
    private final DbAccess _dbAccess;

    public DbMerchantDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public void addTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        final Transaction lastTransaction = getLastTransaction(blueprintId);
        if (lastTransaction == null) {
            insertTransaction(blueprintId, price, date, transactionType);
        } else {
            updateTransaction(blueprintId, price, date, transactionType);
        }
    }

    private void updateTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sql;
                if (transactionType == TransactionType.BUY)
                    sql = "update merchant_data set transaction_price=?, transaction_date=?, transaction_type=?, buy_count=buy_count+1 where blueprint_id=?";
                else
                    sql = "update merchant_data set transaction_price=?, transaction_date=?, transaction_type=?, sell_count=sell_count+1 where blueprint_id=?";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setFloat(1, price);
                    statement.setTimestamp(2, new Timestamp(date.getTime()));
                    statement.setString(3, transactionType.name());
                    statement.setString(4, blueprintId);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to update last transaction from DB", exp);
        }
    }

    private void insertTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sql;
                if (transactionType == TransactionType.BUY)
                    sql = "insert into merchant_data (transaction_price, transaction_date, transaction_type, blueprint_id, sell_count, buy_count) values (?,?,?,?,0,1)";
                else
                    sql = "insert into merchant_data (transaction_price, transaction_date, transaction_type, blueprint_id, sell_count, buy_count) values (?,?,?,?,1,0)";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setFloat(1, price);
                    statement.setTimestamp(2, new Timestamp(date.getTime()));
                    statement.setString(3, transactionType.name());
                    statement.setString(4, blueprintId);
                    statement.execute();
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to insert last transaction from DB", exp);
        }
    }

    @Override
    public Transaction getLastTransaction(String blueprintId) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("select blueprint_id, transaction_price, transaction_date, transaction_type, buy_count-sell_count from merchant_data where blueprint_id=?")) {
                    statement.setString(1, blueprintId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            float price = rs.getFloat(2);
                            Date date = rs.getTimestamp(3);
                            String type = rs.getString(4);
                            int stock = rs.getInt(5);

                            return new Transaction(date, price, TransactionType.valueOf(type), stock);
                        } else {
                            return null;
                        }
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get last transaction from DB", exp);
        }
    }
}
