package com.visoft.jobfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.visoft.jobfinder.Objects.ProUser;
import com.visoft.jobfinder.misc.Constants;
import com.visoft.jobfinder.misc.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    private ArrayList<ProUser> contacts;
    private FirebaseAuth mAuth;
    private DatabaseReference contactsRef, userRef;
    private int j, i;

    //Componentes gráficas
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        listView = findViewById(R.id.listViewContacts);

        mAuth = FirebaseAuth.getInstance();

        contactsRef = Database.getDatabase()
                .getReference(Constants.FIREBASE_CONTACTS_CONTAINER_NAME)
                .child(mAuth.getCurrentUser().getUid());

        contactsRef.keepSynced(true);

        userRef = Database.getDatabase()
                .getReference(Constants.FIREBASE_USERS_CONTAINER_NAME);

        populateContacts();

        Toolbar toolbar = findViewById(R.id.ToolbarContacts);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_transparent)));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void populateContacts() {
        contacts = new ArrayList<>();
        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Boolean> map = (HashMap<String, Boolean>) dataSnapshot.getValue();
                if (map != null) {
                    for (String k : map.keySet()) {
                        getProUserFromUID(k);
                        i++;
                    }
                }
                setAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setAdapter() {
        if (i == j) {
            if (contacts.size() > 0) {
                ListViewAdapter adapter = new ListViewAdapter(this, R.layout.profile_search_result_row, contacts);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getApplication(), ProfileActivity.class);
                        intent.putExtra("user", contacts.get(position));
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private void getProUserFromUID(String uid) {
        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                j++;
                ProUser user = dataSnapshot.getValue(ProUser.class);
                if (user != null) {
                    contacts.add(user);
                }
                setAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private class ListViewAdapter extends ArrayAdapter<ProUser> {
        private LayoutInflater inflater;
        private List<ProUser> list;

        public ListViewAdapter(@NonNull Context context, int resource, @NonNull List<ProUser> objects) {
            super(context, resource, objects);
            this.inflater = LayoutInflater.from(context);
            this.list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.profile_search_result_row, null);

                holder.ivPic = convertView.findViewById(R.id.ivProfilePic);
                holder.tvUsername = convertView.findViewById(R.id.tvUsername);
                holder.tvRubro = convertView.findViewById(R.id.tvRubro);
                holder.tvNumReviews = convertView.findViewById(R.id.tvNumReviews);
                holder.ratingBar = convertView.findViewById(R.id.ratingBar);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ProUser user = list.get(position);
            int id = getResources().getIdentifier(user.getRubroEspecifico(),
                    "string",
                    getPackageName());
            String subRubro = getResources().getString(id);
            holder.tvUsername.setText(user.getUsername());
            holder.tvRubro.setText(subRubro);
            holder.tvNumReviews.setText(user.getNumberReviews() + " " + getString(R.string.reviews));
            holder.ratingBar.setRating(user.getRating());

            return convertView;
        }

        private class ViewHolder {
            ImageView ivPic;
            TextView tvUsername, tvRubro, tvNumReviews;
            SimpleRatingBar ratingBar;
        }
    }
}
