package com.example.opentillmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.opentill.Product;
import com.opentill.UpdatedProduct;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ProductList extends ArrayAdapter<Product> {
    private final Context context;
    private final List<Product> values;

    public ProductList(Context context, List<Product> values) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.values = values;
    }

    public Product findProductByBarcode(String barcode) {
        for (Product product : this.values) {
            if (product.getBarcode().equals(barcode)) {
                return product;
            }
        }
        return null;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView productName = (TextView) rowView.findViewById(R.id.firstLine);
        TextView numberOfItems = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        Product product = values.get(position);
        if (product.getAmountNeeded() == 0) {
            rowView.setVisibility(View.GONE);
        }
        else {
            productName.setText(product.getName());
            if (product.getAmountAdded() > 0) {
                numberOfItems.setText(String.format("%s needed (%s/%s) - %s added", product.getAmountNeeded(), product.getCurrentStock(), product.getMaxStock(), product.getAmountAdded()));
            }
            else {
                numberOfItems.setText(String.format("%s needed (%s/%s)", product.getAmountNeeded(), product.getCurrentStock(), product.getMaxStock()));
            }
            // Change the icon for Windows and iPhone

            if (product.orderFilled()) {
                imageView.setImageResource(R.drawable.check);
            } else {
                imageView.setImageResource(R.drawable.cross);
            }
        }

        return rowView;
    }

    public ArrayList<UpdatedProduct> getUpdatedProjects() {
        ArrayList<UpdatedProduct> updatedProducts = new ArrayList<UpdatedProduct>();
        for (Product product : values) {
            if (product.isUpdated()) {
                updatedProducts.add(new UpdatedProduct(product.getId(), product.getAddedStock()));
            }
        }
        return updatedProducts;
    }
}