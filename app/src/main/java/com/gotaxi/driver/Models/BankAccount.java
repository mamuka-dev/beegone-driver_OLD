package com.gotaxi.driver.Models;

public class BankAccount {
    private String bid, account_name, type, bank_name, account_number, IFSC_code, MICR_code, pending, paid, total;

    public String getBid() {
        return bid;
    }

    public String getAccount_name() {
        return account_name;
    }

    public String getType() {
        return type;
    }

    public String getBank_name() {
        return bank_name;
    }

    public String getAccount_number() {
        return account_number;
    }

    public String getIFSC_code() {
        return IFSC_code;
    }

    public String getMICR_code() {
        return MICR_code;
    }

    public String getPending() {
        return pending;
    }

    public String getPaid() {
        return paid;
    }

    public String getTotal() {
        return total;
    }

    public BankAccount(String bid, String account_name, String type, String bank_name, String account_number, String IFSC_code, String MICR_code, String pending, String paid, String total) {
        this.bid = bid;
        this.account_name = account_name;
        this.type = type;
        this.bank_name = bank_name;
        this.account_number = account_number;
        this.IFSC_code = IFSC_code;
        this.MICR_code = MICR_code;
        this.pending = pending;
        this.paid = paid;
        this.total = total;
    }
}
