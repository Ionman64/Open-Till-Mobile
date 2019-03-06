package com.opentill;

import java.util.Comparator;

public class Product {
    public String id = new String();
    public String name = new String();
    public String barcode = new String();
    public int currentStock = 0;
    public int maxStock = 0;

    public int getAddedStock() {
        return addedStock;
    }

    public int addedStock = 0;

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Product(String id, String name, String barcode, int currentStock, int maxStock) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.currentStock = currentStock;
        this.maxStock = maxStock;
    }

    public int getAmountNeeded() {
        if ((maxStock - currentStock) > 0) {
            return maxStock - currentStock;
        }
        else {
            return 0;
        }
    }

    public Boolean isUpdated() {
        return !(addedStock == 0);
    }

    public String getBarcode() {
        return barcode;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public Boolean orderFilled() {
        return this.addedStock >= this.getAmountNeeded();
    }

    public static Comparator<? super Product> compare = new Comparator<Product>() {
        @Override
        public int compare(Product s1, Product s2) {
            return s1.getName().compareTo(s2.getName());
        }
    };


    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public void incrementAddedAmount() {
        this.addedStock++;
    }

    public int getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(int maxStock) {
        this.maxStock = maxStock;
    }

    public void setAmountAdded(int addedStock) {
        this.addedStock = addedStock;
    }

    public int getAmountAdded() {
        return this.addedStock;
    }
}
