package com.example.opentillmobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.opentill.Supplier;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SupplierList extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SupplierList() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SupplierList newInstance(int columnCount) {
        SupplierList fragment = new SupplierList();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(suppliers, mListener));
            getSuppliersListFromServer();
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Supplier item);
    }

    public void getSuppliersListFromServer() {
        final Snackbar snackerbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Fetching data", Snackbar.LENGTH_LONG).setAction("Action", null);
        String url = "https://www.goldstandardresearch.co.uk/kvs/api/kvs.php?function=GETALLSUPPLIERS";
        RequestQueue requestQueue = Volley.newRequestQueue(this.getContext());
        requestQueue.start();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                snackerbar.dismiss();
                Log.d("Response", response.toString());
                try {
                    JSONObject suppliersArray = response.getJSONObject("suppliers");
                    Iterator<String> iter = suppliersArray.keys();
                    suppliers.clear();
                    while (iter.hasNext()) {

                        String key = iter.next();
                        JSONObject tempSupplier = suppliersArray.getJSONObject(key);
                        Supplier tempSupplierObject = new Supplier(tempSupplier.getString("id"), tempSupplier.getString("name"));
                        suppliers.add(tempSupplierObject);
                    }
                    Collections.sort(suppliers, Supplier.compare);
                    recyclerView.getAdapter().notifyDataSetChanged();
                    //recyclerView.setAdapter(new MyItemRecyclerViewAdapter(suppliers, mListener));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                snackerbar.dismiss();
                new AlertDialog.Builder(getContext()).setTitle("Could not fetch data").setMessage("Could not reach server").setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                }).create().show();
                Log.d("Error", error.getMessage());
            }
        });
        // Access the RequestQueue through your singleton class.
        snackerbar.show();
        requestQueue.add(jsonObjectRequest);
    }
}
