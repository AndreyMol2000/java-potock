DROP TABLE IF EXISTS bank_account;

CREATE TABLE bank_account (
                              account_number VARCHAR(20) PRIMARY KEY,
                              balance DOUBLE NOT NULL
);