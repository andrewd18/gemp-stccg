package com.gempukku.lotro.db;

import java.util.Date;

public interface MerchantDAO {
    Transaction getLastTransaction(String blueprintId);

    void addTransaction(String blueprintId, float price, Date date, TransactionType transactionType);

    enum TransactionType {
        SELL, BUY
    }

    class Transaction {
        private final float _price;
        private final Date _date;
        private final TransactionType _transactionType;
        private final int _stock;

        public Transaction(Date date, float price, TransactionType transactionType, int stock) {
            _date = date;
            _price = price;
            _transactionType = transactionType;
            _stock = stock;
        }

        public Date getDate() {
            return _date;
        }

        public float getPrice() {
            return _price;
        }

        public TransactionType getTransactionType() {
            return _transactionType;
        }

        public int getStock() {
            return _stock;
        }
    }
}
