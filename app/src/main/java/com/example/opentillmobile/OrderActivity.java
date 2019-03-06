package com.example.opentillmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.opentill.Product;
import com.opentill.Supplier;
import com.opentill.UpdatedProduct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OrderActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    ArrayList<Product> products = new ArrayList<Product>();
    String supplierId = null;
    ProductList adapter = null;
    RequestQueue requestQueue = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        supplierId = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        setTitle(String.format("%s order", name));

        requestQueue = Volley.newRequestQueue(this);
        requestQueue.start();


        getOrderFromSupplier();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final ListView listView = (ListView) findViewById(R.id.list_view);

        adapter = new ProductList(this, products);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product tempProduct = products.get(position);
                show(tempProduct);
            }
        });





        EditText textbox = (EditText) findViewById(R.id.barcode);
        textbox.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                boolean handled = false;
                String barcode = v.getText().toString();
                v.setText("");
                v.requestFocusFromTouch();

                if (barcode.length() == 0) {
                    return false;
                }
                Product product = adapter.findProductByBarcode(barcode);
                if (product == null) {
                    Toast.makeText(getApplicationContext(), "cannot find product", Toast.LENGTH_LONG).show();
                }
                else {
                    product.incrementAddedAmount();
                    adapter.notifyDataSetChanged();
                    handled = true;
                }
                return handled;
            }
        });
    }

    public void onCompleteOrder(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to complete the order, stock levels will be automatically updated?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    completeOrder();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    public void completeOrder() {
        final Snackbar snackerbar = Snackbar.make(this.findViewById(android.R.id.content), "Saving Order", Snackbar.LENGTH_LONG).setAction("Action", null);
        snackerbar.show();
        final String url = "https://www.goldstandardresearch.co.uk/kvs/api/kvs.php?function=SETPRODUCTSTOCKLEVELS";
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    snackerbar.dismiss();
                    JSONObject jo = new JSONObject(response);
                    Boolean success = jo.getBoolean("success");
                    if (success) {
                        getOrderFromSupplier();
                        return;
                    }
                    //adapter.notifyDataSetChanged();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                new AlertDialog.Builder(OrderActivity.this).setTitle("Order not updated").setMessage("Could not complete this order, you may want to try again").setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                }).create().show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                snackerbar.dismiss();
                new AlertDialog.Builder(OrderActivity.this).setTitle("Could not fetch data").setMessage("Could not reach server").setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                }).create().show();
                Log.d("Error", error.getMessage());
            }
        }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                Gson gson = new Gson();
                params.put("products", gson.toJson(adapter.getUpdatedProjects()));
                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }



    public void getOrderFromSupplier() {
        final Snackbar snackerbar = Snackbar.make(this.findViewById(android.R.id.content), "Fetching data", Snackbar.LENGTH_LONG).setAction("Action", null);
        String url = "https://www.goldstandardresearch.co.uk/kvs/api/kvs.php?function=GETORDER";

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jo = new JSONObject(response);
                    JSONArray order = jo.getJSONArray("order");
                    products.clear();
                    for (int i=0;i<order.length();i++) {
                        JSONObject tempProductObject = order.getJSONObject(i);
                        Product tempSupplierObject = new Product(tempProductObject.getString("id"), tempProductObject.getString("name"), tempProductObject.getString("barcode"), tempProductObject.getInt("current_stock"), tempProductObject.getInt("max_stock"));
                        products.add(tempSupplierObject);
                    }
                    if (products.size() == 0) {
                        new AlertDialog.Builder(OrderActivity.this).setTitle("Order Empty").setMessage("There are no products in this order").setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create().show();
                    }
                    Collections.sort(products, Product.compare);
                    adapter.notifyDataSetChanged();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                snackerbar.dismiss();
                new AlertDialog.Builder(OrderActivity.this).setTitle("Could not fetch data").setMessage("Could not reach server").setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                }).create().show();
                Log.d("Error", error.getMessage());
            }
        }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("supplier", supplierId);
                return params;
            }
        };
        // Access the RequestQueue through your singleton class.
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        Log.i("value is",""+newVal);

    }
    public void show(final Product product)
    {
        final Dialog d = new Dialog(this);
        d.setTitle(String.format("Number of '%s'", product.getName()));
        d.setContentView(R.layout.sample_dialog);
        final Button button_ok = (Button) d.findViewById(R.id.button_ok);
        Button button_clear = (Button) d.findViewById(R.id.button_clear);
        final TextView input_amount = (TextView) d.findViewById(R.id.input_amount);

        Button button_0 = (Button) d.findViewById(R.id.button_0);
        Button button_1 = (Button) d.findViewById(R.id.button_1);
        Button button_2 = (Button) d.findViewById(R.id.button_2);
        Button button_3 = (Button) d.findViewById(R.id.button_3);
        Button button_4 = (Button) d.findViewById(R.id.button_4);
        Button button_5 = (Button) d.findViewById(R.id.button_5);
        Button button_6 = (Button) d.findViewById(R.id.button_6);
        Button button_7 = (Button) d.findViewById(R.id.button_7);
        Button button_8 = (Button) d.findViewById(R.id.button_8);
        Button button_9 = (Button) d.findViewById(R.id.button_9);

        button_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input_amount.getText() != "0") {
                    input_amount.setText(input_amount.getText() + "0");
                }
            }
        });

        button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_amount.setText(input_amount.getText() + "1");
            }
        });

        button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_amount.setText(input_amount.getText() + "2");
            }
        });

        button_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_amount.setText(input_amount.getText() + "3");
            }
        });

        button_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_amount.setText(input_amount.getText() + "4");
            }
        });

        button_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_amount.setText(input_amount.getText() + "5");
            }
        });

        button_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_amount.setText(input_amount.getText() + "6");
            }
        });

        button_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_amount.setText(input_amount.getText() + "7");
            }
        });

        button_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_amount.setText(input_amount.getText() + "8");
            }
        });

        button_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_amount.setText(input_amount.getText() + "9");
            }
        });

        input_amount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    button_ok.callOnClick();
                }
                return false;
            }
        });


        button_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
            product.setAmountAdded(Integer.parseInt(input_amount.getText().toString()));
            adapter.notifyDataSetChanged();
            d.dismiss();
            }
        });
        button_clear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                input_amount.setText("");
            }
        });
        d.show();

    }

}
