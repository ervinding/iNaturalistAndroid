package org.inaturalist.android;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProjectDetailsCheckList extends Fragment {
    public static final String KEY_PROJECT = "project";
    private BetterJSONObject mProject;
    private ProgressBar mProgress;
    private TextView mProjectTaxaEmpty;
    private ListView mProjectTaxa;
    private CheckListReceiver mCheckListReceiver;
    
    private class CheckListAdapter extends ArrayAdapter<JSONObject> {

        private List<JSONObject> mItems;
        private Context mContext;

        public CheckListAdapter(Context context, List<JSONObject> objects) {
            super(context, R.layout.taxon_item, objects);

            mItems = objects;
            mContext = context;
        }

        public void addItemAtBeginning(JSONObject newItem) {
            mItems.add(0, newItem);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public JSONObject getItem(int index) {
            return mItems.get(index);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) { 
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View view = inflater.inflate(R.layout.taxon_item, parent, false); 
            BetterJSONObject item = null;
            BetterJSONObject defaultName = null;
            try {
                item = new BetterJSONObject(mItems.get(position).getJSONObject("taxon"));
                defaultName = new BetterJSONObject(item.getJSONObject("default_name"));
            } catch (JSONException e) {
                e.printStackTrace();
                return view;
            }

            TextView idName = (TextView) view.findViewById(R.id.id_name);
            idName.setText(defaultName.getString("name"));
            TextView taxonName = (TextView) view.findViewById(R.id.taxon_name);
            taxonName.setText(item.getString("name"));
            taxonName.setTypeface(null, Typeface.ITALIC);
            ImageView taxonPic = (ImageView) view.findViewById(R.id.taxon_pic);
            UrlImageViewHelper.setUrlDrawable(taxonPic, item.getString("photo_url"));
            
            Button addObservation = (Button) view.findViewById(R.id.add_observation);
            final BetterJSONObject defaultName2 = defaultName;
            addObservation.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    BetterJSONObject item = (BetterJSONObject) view.getTag();
                    Intent intent = new Intent(Intent.ACTION_INSERT, Observation.CONTENT_URI, getActivity(), ObservationEditor.class);
                    intent.putExtra(ObservationEditor.SPECIES_GUESS, String.format("%s (%s)", item.getString("name"), defaultName2.getString("name")));
                    startActivity(intent);
                }
            });

            view.setTag(item);

            return view;
        }
    }
    
    private class CheckListReceiver extends BroadcastReceiver {
        private CheckListAdapter mAdapter;
        private ArrayList<JSONObject> mCheckList;

        @Override
        public void onReceive(Context context, Intent intent) {
            getActivity().unregisterReceiver(mCheckListReceiver);
            
            SerializableJSONArray checkListSerializable = (SerializableJSONArray) intent.getSerializableExtra(INaturalistService.CHECK_LIST_RESULT);
            JSONArray checkList = (checkListSerializable == null ? new SerializableJSONArray() : checkListSerializable).getJSONArray();
            mCheckList = new ArrayList<JSONObject>();
            
            for (int i = 0; i < checkList.length(); i++) {
                try {
                    mCheckList.add(checkList.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mProgress.setVisibility(View.GONE);
            
            if (checkList.length() > 0) {
                mProjectTaxaEmpty.setVisibility(View.GONE);
                mProjectTaxa.setVisibility(View.VISIBLE);
                
                mAdapter = new CheckListAdapter(getActivity(), mCheckList);
                mProjectTaxa.setAdapter(mAdapter);
                
            } else {
                mProjectTaxaEmpty.setText(R.string.no_check_list);
                mProjectTaxaEmpty.setVisibility(View.VISIBLE);
                mProjectTaxa.setVisibility(View.GONE);
            }
            
        }
    }
  
    
    @Override
    public void onPause() {
        super.onPause();
        
        try {
            if (mCheckListReceiver != null) {
                getActivity().unregisterReceiver(mCheckListReceiver);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.project_details_check_list, container, false);
        
        mCheckListReceiver = new CheckListReceiver();
        IntentFilter filter = new IntentFilter(INaturalistService.ACTION_CHECK_LIST_RESULT);
        getActivity().registerReceiver(mCheckListReceiver, filter);  
        
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        mProjectTaxaEmpty = (TextView) v.findViewById(R.id.project_taxa_empty);
        mProjectTaxa = (ListView) v.findViewById(R.id.project_check_list);
        
        mProgress.setVisibility(View.VISIBLE);
        mProjectTaxa.setVisibility(View.GONE);
        mProjectTaxaEmpty.setVisibility(View.GONE);
        
        Bundle bundle = getArguments();
        
        if (bundle != null) {
            mProject = (BetterJSONObject) bundle.getSerializable(KEY_PROJECT);
        }
        
        return v;
    }
}
