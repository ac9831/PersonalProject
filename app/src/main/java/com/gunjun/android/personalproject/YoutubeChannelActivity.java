package com.gunjun.android.personalproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gunjun.android.personalproject.adapter.ChannelAdapter;
import com.gunjun.android.personalproject.models.Youtube;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class YoutubeChannelActivity extends AppCompatActivity {

    private RecyclerView.LayoutManager layoutManager;
    private ChannelAdapter channelAdapter;
    private Realm realm;

    @BindView(R.id.youtube_channel_toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.channel_edit)
    protected EditText editText;

    @BindView(R.id.channel_list)
    protected RecyclerView recyclerView;

    @BindView(R.id.channel_input)
    protected Button channelInput;

    @Override
    protected void onResume() {
        super.onResume();
        channelAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_channel);

        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("YoutubeChannel");
        RealmResults<Youtube> realmResults = realm.where(Youtube.class).findAll();
        layoutManager = new LinearLayoutManager(this);
        channelAdapter = new ChannelAdapter(realmResults,this,realm);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(channelAdapter);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable edit) {
                String s = edit.toString();
                if (s.length() > 0)
                    channelInput.setEnabled(true);
                else
                    channelInput.setEnabled(false);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        };

        editText.addTextChangedListener(textWatcher);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {

        realm.beginTransaction();
        Youtube youtube;
        if(realm.where(Youtube.class).findFirst() == null) {
            youtube = realm.createObject(Youtube.class,1);
        } else {
            youtube = realm.createObject(Youtube.class,realm.where(Youtube.class).max("id").intValue() + 1);
        }
        youtube.setChannelName(editText.getText().toString());
        realm.commitTransaction();
        channelAdapter.notifyDataSetChanged();
    }
}
