package eduard.zaripov.innocamp2022;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import eduard.zaripov.innocamp2022.app.service.MqttHelper;
import eduard.zaripov.innocamp2022.model.Thing;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ListView listView = findViewById(R.id.listView);

        ArrayList<Thing> things = new ArrayList<>();
        ThingAdapter arrayAdapter = new ThingAdapter(getApplicationContext(), R.layout.list_item, things);
        listView.setAdapter(arrayAdapter);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MqttHelper.connect(getApplicationContext(), () -> {
                    String content = MqttHelper.getContent();

                    /**
                     * "thingId" : "device1", ...
                     */
                    try {

                        JSONObject jsonObject = new JSONObject(content);

                        String id = jsonObject.getString("thingId");
                        String typeOf = jsonObject.getString("type");
                        boolean wartering = jsonObject.getBoolean("isWatering");
                        boolean working = jsonObject.getBoolean("isWorking");
                        Double lat = jsonObject.getJSONObject("current").getJSONObject("geo0").getDouble("lat");
                        Double lon = jsonObject.getJSONObject("current").getJSONObject("geo0").getDouble("lon");




                        Thing thing = new Thing(id, typeOf, wartering, working, lat, lat);

                        boolean isAdded = false;

                        for (int i = 0;i < things.size(); i ++ ){
                            if (things.get(i).getId().equals(thing.getId())) {
                                isAdded = true;

                                things.set(i, thing);
                                break;

                            }
                        }
                        if (!isAdded) {
                            things.add(thing);
                        }


                        arrayAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("TAG", e.getMessage());
                    }

                });
            }
        });

        thread.start();
    }


}
