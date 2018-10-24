package com.zeeroapps.wssp.admin;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.gson.Gson;
import com.wang.avi.AVLoadingIndicatorView;
import com.zeeroapps.wssp.R;
import com.zeeroapps.wssp.fragments.ComplaintDetailFragment;
import com.zeeroapps.wssp.fragments.PieChartFragment;
import com.zeeroapps.wssp.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class HomeFragemt extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    AVLoadingIndicatorView avi;
    String all_complaints="0", pending_complaints="0", inprogress_complaints="0",
            completed_complaints="0",
            overdue_complaints="0";
    TextView Tv_all_complaints, Tv_pending_complaints, Tv_inprogress_complaints,
            Tv_completed_complaints, Tv_overdue_complaints;
    PieChart pieChart;
    ArrayList<String> PieEntryLabels ;
    PieDataSet pieDataSet ;
    PieData pieData ;
    ArrayList<Entry> entries ;

    public HomeFragemt() {
        // Required empty public constructor
    }


    public static HomeFragemt newInstance(String param1, String param2) {
        HomeFragemt fragment = new HomeFragemt();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home_fragemt, container, false);

        avi = (AVLoadingIndicatorView) v.findViewById(R.id.loadingIndicator);
        avi.show();
        pieChart = (PieChart) v.findViewById(R.id.chart1);
        Tv_all_complaints = (TextView) v.findViewById(R.id.allcomp);
        Tv_pending_complaints = (TextView) v.findViewById(R.id.pending);
        Tv_inprogress_complaints = (TextView) v.findViewById(R.id.progress);
        Tv_completed_complaints = (TextView) v.findViewById(R.id.completed);
        Tv_overdue_complaints = (TextView) v.findViewById(R.id.overdue);

        //adding values to pie chart
        entries = new ArrayList<>();
        PieEntryLabels = new ArrayList<String>();


        getServerData();

        return v;
    }


    private void getServerData() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.Admin_Home, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        avi.hide();
                        Log.e("ServerResponse", response + "");
                        try {

                            String responseString = response.getString("status");

                            if (responseString.equals("true")) {

                                all_complaints = response.getString("all_complaints");
                                pending_complaints = response.getString("pending_complaints");
                                completed_complaints = response.getString("completed_complaints");
                                inprogress_complaints = response.getString("inprogress_complaints");


                                Log.e("All_complaints", all_complaints + pending_complaints
                                        + completed_complaints + inprogress_complaints);

                                Tv_all_complaints.setText(all_complaints);
                                Tv_completed_complaints.setText(completed_complaints);
                                Tv_inprogress_complaints.setText(inprogress_complaints);
                                Tv_pending_complaints.setText(pending_complaints);


                                AddValuesToPIEENTRY();
                                AddValuesToPieEntryLabels();

                                ArrayList<Integer> colors = new ArrayList<Integer>();
                                colors.add(ContextCompat.getColor(getActivity(), R.color.completed));
                                colors.add(ContextCompat.getColor(getActivity(), R.color.inprogress));
                                colors.add(ContextCompat.getColor(getActivity(), R.color.pending));
                                colors.add(ContextCompat.getColor(getActivity(), R.color.overdue));

                                pieDataSet = new PieDataSet(entries, "");
                                pieData = new PieData(PieEntryLabels, pieDataSet);
                                //create own formater for values
                                pieData.setValueFormatter(new MyValueFormatter());
                                pieDataSet.setColors(colors);
                                pieChart.setDescription("");
                                pieChart.setCenterText("Total Complaints");
                                pieChart.setData(pieData);
                                pieChart.animateY(2000);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        avi.hide();
                        System.out.println(error.toString());
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(jsonObjectRequest);
    }


    public class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal if needed
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value) + ""; // e.g. append a dollar-sign
        }
    }

    public void AddValuesToPIEENTRY(){

        if(Integer.parseInt(completed_complaints) > 0){
            entries.add(new BarEntry(Integer.parseInt(completed_complaints), 0));
        }
        if(Integer.parseInt(inprogress_complaints) > 0){
            entries.add(new BarEntry(Integer.parseInt(inprogress_complaints), 1));
        }
        if(Integer.parseInt(pending_complaints) > 0) {
            entries.add(new BarEntry(Integer.parseInt(pending_complaints), 2));
        }
        if(Integer.parseInt(overdue_complaints) > 0) {
            entries.add(new BarEntry(Integer.parseInt(overdue_complaints), 3));
        }

    }

    public void AddValuesToPieEntryLabels(){

        PieEntryLabels.add("Completed");
        PieEntryLabels.add("InProgress");
        PieEntryLabels.add("Pending");
        PieEntryLabels.add("OverDue");

    }



    @Override
    public void onDetach() {
        super.onDetach();
    }


}
