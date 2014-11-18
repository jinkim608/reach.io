package io.reach.reachiodemo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reach.reachiodemo.R;

/**
 * Created by Jin on 11/7/14.
 */
public class SecondFragment extends android.support.v4.app.Fragment {
    Activity mActivity;
    private List<Map<String, Object>> data;

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        mActivity = act;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);


        List<String> arr = new ArrayList<String>();

        //List of Items
        String[] name_of_bookmarks = {
                "Germany",
                "Argentina",
                "Netherlands",
                "Brazil",
                "Colombia",
                "Belgium",
                "France",
                "Costa Rica"
        };

        //Create your List object for the ArrayAdapter
        //and make it the same size as name_of_books
        final List<String> listTeams = new ArrayList<String>(Array.getLength(name_of_bookmarks));

        //Add name_of_bookmarks contents to listBookmarks
        Collections.addAll(listTeams, name_of_bookmarks);

        //Create an ArrayAdapter passing it the Context, a generic list item and your list
        //An alternative to "this" would be "getApplicationContext()" from your main activity
        //or "getActivity()" from a fragment. "getBaseContext()" is not recommended.
        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, listTeams);

        //Set the adapter to your ListView
        ListView lvSoccer = (ListView) view.findViewById(R.id.lv_soccer);
        lvSoccer.setAdapter(arrayAdapter);
        lvSoccer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), listTeams.get(position).toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
