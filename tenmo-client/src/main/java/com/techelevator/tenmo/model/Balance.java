package com.techelevator.tenmo.model;

import java.math.BigDecimal;


public class Balance {

    private BigDecimal amount;

    public Balance() {
        this.amount = BigDecimal.ZERO;  // Default balance is 0
    }

    public Balance(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void add(BigDecimal value) {
        this.amount = this.amount.add(value);  // Add value to balance
    }

    public void subtract(BigDecimal value) {
        this.amount = this.amount.subtract(value);  // Subtract value from balance
    }


}



