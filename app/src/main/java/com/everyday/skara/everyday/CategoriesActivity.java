package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CategoriesActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ArrayList<Categories> categoriesArrayList;
    UserInfoPOJO userInfoPOJO;
    RecyclerView mCategoriesRecyclerView;
    Categories selectedCat;
    CatAdapter catAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }
    void init(){
        Intent intent = getIntent();
//        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");
        mCategoriesRecyclerView = findViewById(R.id.recyclerview_categories);
        categoriesArrayList = new ArrayList<>();
        initCatRecyclerView();
    }

    void initCatRecyclerView(){
        mCategoriesRecyclerView.invalidate();
        mCategoriesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mCategoriesRecyclerView.setLayoutManager(linearLayoutManager);
        catAdapter = new CatAdapter();
        mCategoriesRecyclerView.setAdapter(catAdapter);

        fetchCategories();
    }
    void fetchCategories() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/categories");
        databaseReference.keepSynced(true);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Categories categories = dataSnapshot.getValue(Categories.class);
                categoriesArrayList.add(categories);
                catAdapter.notifyItemInserted(categoriesArrayList.size());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    public class CatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;
        //ArrayList<Categories> categoriesArrayList;

        public CatAdapter() {
            try {
                this.inflator = LayoutInflater.from(CategoriesActivity.this);
                // this.categoriesArrayList = categoriesArrayList;
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recyclerview_expense_catgories_row_layout, parent, false);
            return new CatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Categories categories = categoriesArrayList.get(position);
            ((CatViewHolder) holder).mCatName.setText(categories.getCategoryName());
            showCatIcon(holder, categories);
        }


        void showCatIcon(@NonNull RecyclerView.ViewHolder holder, Categories categories) {
            switch (categories.getCategoryIconId()) {
                case 2000:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                    break;
                case 2001:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2001);
                    break;
                case 2002:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2002);
                    break;
                case 2003:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2003);
                    break;
                case 2004:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2004);
                    break;
                case 2005:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2005);
                    break;
                case 2006:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2006);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return categoriesArrayList.size();
        }

        public class CatViewHolder extends RecyclerView.ViewHolder {
            public Button mCatName;
            public ImageButton mCatIcon;

            public CatViewHolder(View itemView) {
                super(itemView);
                mCatName = itemView.findViewById(R.id.category_name_row_textview);
                mCatIcon = itemView.findViewById(R.id.expense_cat_icon_row);
                mCatName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }

    }
    void toLoginActivity() {
        Intent intent = new Intent(CategoriesActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
