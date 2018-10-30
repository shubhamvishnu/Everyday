package com.everyday.skara.everyday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.ExpensePOJO;
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
import java.util.HashMap;
import java.util.Map;

public class CategoriesActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ArrayList<Categories> categoriesArrayList;
    UserInfoPOJO userInfoPOJO;
    RecyclerView mCategoriesRecyclerView;
    int choosenIcon = -2000;
    Categories selectedCat;
    CatAdapter catAdapter;
    ImageButton mChosenIconImageButton;
    BottomSheetDialog mAddNewCatDialog, mChooseIconDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        Toolbar myToolbar = findViewById(R.id.categories_toolbar);
        setSupportActionBar(myToolbar);
        if (user != null) {
            init();
        } else {
            toLoginActivity();
        }
    }

    void init() {
        Intent intent = getIntent();
//        boardPOJO = (BoardPOJO) intent.getSerializableExtra("board_pojo");
        userInfoPOJO = (UserInfoPOJO) intent.getSerializableExtra("user_profile");
        mCategoriesRecyclerView = findViewById(R.id.recyclerview_categories);
        categoriesArrayList = new ArrayList<>();
        initCatRecyclerView();
    }

    void initCatRecyclerView() {
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

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
            if (categoriesArrayList.get(position).getCategoryIconId() > 2006) {
                View view = inflator.inflate(R.layout.recyclerview_expense_custom_categories_row_layout, parent, false);
                return new CatViewHolder(view);
            } else {
                View view = inflator.inflate(R.layout.recyclerview_expense_catgories_row_layout, parent, false);
                return new DefaultCatViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int viewType) {
            int position = getItemViewType(viewType);

            Categories categories = categoriesArrayList.get(position);

            if (categoriesArrayList.get(position).getCategoryIconId() > 2006) {
                ((CatViewHolder) holder).mCatName.setText(categories.getCategoryName());
            } else {
                ((DefaultCatViewHolder) holder).mCatName.setText(categories.getCategoryName());
            }
            showCatIcon(holder, categories);

        }


        void showCatIcon(@NonNull RecyclerView.ViewHolder holder, Categories categories) {
            switch (categories.getCategoryIconId()) {
                case 2000:
                    ((DefaultCatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                    break;
                case 2001:
                    ((DefaultCatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2001);
                    break;
                case 2002:
                    ((DefaultCatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2002);
                    break;
                case 2003:
                    ((DefaultCatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2003);
                    break;
                case 2004:
                    ((DefaultCatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2004);
                    break;
                case 2005:
                    ((DefaultCatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2005);
                    break;
                case 2006:
                    ((DefaultCatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2006);
                    break;
                case 2007:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2007);
                    break;

                case 2008:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2008);
                    break;

                case 2009:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2009);
                    break;

                case 2010:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2010);
                    break;

                case 2011:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2011);
                    break;

                case 2012:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2012);
                    break;

                case 2013:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2013);
                    break;

                case 2014:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2014);
                    break;

                case 2015:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2015);
                    break;

                case 2016:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2016);
                    break;

                case 2017:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2017);
                    break;

                case 2018:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2018);
                    break;

                case 2019:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2019);
                    break;

                case 2020:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2020);
                    break;

                case 2021:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2021);
                    break;

                case 2022:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2022);
                    break;

                case 2023:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2023);
                    break;

                case 2024:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2024);
                    break;

                case 2025:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2025);
                    break;

                case 2026:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2026);
                    break;

                case 2027:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2027);
                    break;

                case 2028:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2028);
                    break;

                case 2029:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2029);
                    break;

                case 2030:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2030);
                    break;
                case 2031:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2031);
                    break;
                case 2032:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2032);
                    break;
                case 2033:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2033);
                    break;
                case 2034:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2034);
                    break;
                case 2035:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2035);
                    break;
                case 2036:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2036);
                    break;
                case 2037:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2037);
                    break;
                case 2038:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2038);
                    break;
                case 2039:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2039);
                    break;
                case 2040:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2040);
                    break;
                case 2041:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2041);
                    break;
                case 2042:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2042);
                    break;
                case 2043:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2043);
                    break;
                case 2044:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2044);
                    break;
                case 2045:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2045);
                    break;
                case 2046:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2046);
                    break;
                case 2047:
                    ((CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2047);
                    break;

            }
        }

        @Override
        public int getItemCount() {
            return categoriesArrayList.size();
        }
        void deleteIcon(final int position){
            final Categories categories = categoriesArrayList.get(position);
            DatabaseReference catReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/expenses");
            catReference.keepSynced(true);
            catReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            ExpensePOJO expensePOJO = snapshot.getValue(ExpensePOJO.class);
                            Categories categories1 = expensePOJO.getCategories();
                            if(categories1.getCategoryKey().equals(categories.getCategoryKey())){

                                Categories categories2 = categoriesArrayList.get(0);
                                expensePOJO.setCategories(categories2);
                                Map<String, Object> catupdateMap = new HashMap<>();
                                catupdateMap.put(expensePOJO.getEntryKey(), expensePOJO);
                                FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/expenses").updateChildren(catupdateMap);

                            }
                        }
                        FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/categories").child(categories.getCategoryKey()).removeValue();
                        categoriesArrayList.remove(position);
                        catAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public class CatViewHolder extends RecyclerView.ViewHolder {
            public Button mCatName;
            public ImageButton mCatIcon;
            public Button mDelete;

            public CatViewHolder(View itemView) {
                super(itemView);
                mCatName = itemView.findViewById(R.id.category_name_row_textview);
                mCatIcon = itemView.findViewById(R.id.expense_cat_icon_row);
                mDelete = itemView.findViewById(R.id.delete_custom_cat_button);
                mCatName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteIcon(getPosition());
                    }
                });
            }
        }

        public class DefaultCatViewHolder extends RecyclerView.ViewHolder {
            public Button mCatName;
            public ImageButton mCatIcon;

            public DefaultCatViewHolder(View itemView) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_categories_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_new_category_item:
                addNewCategoryDialog();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void addNewCategoryDialog() {
        mAddNewCatDialog = new BottomSheetDialog(this);
        mAddNewCatDialog.setContentView(R.layout.dialog_new_cat_layout);
        choosenIcon = -1;
        final EditText mTitle;
        ImageButton mClose;
        Button mChooseIcon;

        Button mDone;

        mTitle = mAddNewCatDialog.findViewById(R.id.new_category_title);
        mClose = mAddNewCatDialog.findViewById(R.id.close_new_category_dialog);
        mChooseIcon = mAddNewCatDialog.findViewById(R.id.choose_icon_button);
        mChosenIconImageButton = mAddNewCatDialog.findViewById(R.id.ic_icon_chosen);
        mDone = mAddNewCatDialog.findViewById(R.id.new_cat_dialog_done);

        mChooseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoseIconDialog();
            }
        });
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = mTitle.getText().toString().trim();

                if (title.isEmpty() || title.equals("")) {
                    Toast.makeText(CategoriesActivity.this, "title cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    if (choosenIcon == -1) {
                        Toast.makeText(CategoriesActivity.this, "Choose an icon", Toast.LENGTH_SHORT).show();
                    } else {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL);
                        DatabaseReference catReference = databaseReference.child("categories").push();
                        catReference.setValue(new Categories(title, catReference.getKey(), choosenIcon));
                        mAddNewCatDialog.dismiss();
                    }
                }
            }
        });
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAddNewCatDialog.dismiss();
            }
        });
        mAddNewCatDialog.setCanceledOnTouchOutside(false);
        mAddNewCatDialog.show();
    }

    void showChoseIconDialog() {
        ImageButton m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20, m21, m22, m23, m24, m25, m26, m27, m28, m29, m30,
                m31, m32, m33, m34, m35, m36, m37, m38, m39, m40, m41, m42, m43, m44, m45, m46, m47;

        mChooseIconDialog = new BottomSheetDialog(this);
        mChooseIconDialog.setContentView(R.layout.dialog_choose_cat_icon_layout);

        m7 = mChooseIconDialog.findViewById(R.id.ic_cat_2007);
        m8 = mChooseIconDialog.findViewById(R.id.ic_cat_2008);
        m9 = mChooseIconDialog.findViewById(R.id.ic_cat_2009);
        m10 = mChooseIconDialog.findViewById(R.id.ic_cat_2010);
        m11 = mChooseIconDialog.findViewById(R.id.ic_cat_2011);
        m12 = mChooseIconDialog.findViewById(R.id.ic_cat_2012);
        m13 = mChooseIconDialog.findViewById(R.id.ic_cat_2013);
        m14 = mChooseIconDialog.findViewById(R.id.ic_cat_2014);
        m15 = mChooseIconDialog.findViewById(R.id.ic_cat_2015);
        m16 = mChooseIconDialog.findViewById(R.id.ic_cat_2016);
        m17 = mChooseIconDialog.findViewById(R.id.ic_cat_2017);
        m18 = mChooseIconDialog.findViewById(R.id.ic_cat_2018);
        m19 = mChooseIconDialog.findViewById(R.id.ic_cat_2019);
        m20 = mChooseIconDialog.findViewById(R.id.ic_cat_2020);
        m21 = mChooseIconDialog.findViewById(R.id.ic_cat_2021);
        m22 = mChooseIconDialog.findViewById(R.id.ic_cat_2022);
        m23 = mChooseIconDialog.findViewById(R.id.ic_cat_2023);
        m24 = mChooseIconDialog.findViewById(R.id.ic_cat_2024);
        m25 = mChooseIconDialog.findViewById(R.id.ic_cat_2025);
        m26 = mChooseIconDialog.findViewById(R.id.ic_cat_2026);
        m27 = mChooseIconDialog.findViewById(R.id.ic_cat_2027);
        m28 = mChooseIconDialog.findViewById(R.id.ic_cat_2028);
        m29 = mChooseIconDialog.findViewById(R.id.ic_cat_2029);
        m30 = mChooseIconDialog.findViewById(R.id.ic_cat_2030);
        m31 = mChooseIconDialog.findViewById(R.id.ic_cat_2031);
        m32 = mChooseIconDialog.findViewById(R.id.ic_cat_2032);
        m33 = mChooseIconDialog.findViewById(R.id.ic_cat_2033);
        m34 = mChooseIconDialog.findViewById(R.id.ic_cat_2034);
        m35 = mChooseIconDialog.findViewById(R.id.ic_cat_2035);
        m36 = mChooseIconDialog.findViewById(R.id.ic_cat_2036);
        m37 = mChooseIconDialog.findViewById(R.id.ic_cat_2037);
        m38 = mChooseIconDialog.findViewById(R.id.ic_cat_2038);
        m39 = mChooseIconDialog.findViewById(R.id.ic_cat_2039);
        m40 = mChooseIconDialog.findViewById(R.id.ic_cat_2040);
        m41 = mChooseIconDialog.findViewById(R.id.ic_cat_2041);
        m42 = mChooseIconDialog.findViewById(R.id.ic_cat_2042);
        m43 = mChooseIconDialog.findViewById(R.id.ic_cat_2043);
        m44 = mChooseIconDialog.findViewById(R.id.ic_cat_2044);
        m45 = mChooseIconDialog.findViewById(R.id.ic_cat_2045);
        m46 = mChooseIconDialog.findViewById(R.id.ic_cat_2046);
        m47 = mChooseIconDialog.findViewById(R.id.ic_cat_2047);


        m7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2007;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2007);
                mChooseIconDialog.dismiss();
            }
        });
        m8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2008;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2008);
                mChooseIconDialog.dismiss();
            }
        });
        m9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2009;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2009);
                mChooseIconDialog.dismiss();
            }
        });
        m10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2010;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2010);
                mChooseIconDialog.dismiss();
            }
        });
        m11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2011;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2011);
                mChooseIconDialog.dismiss();
            }
        });
        m12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2012;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2012);
                mChooseIconDialog.dismiss();
            }
        });
        m13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 20013;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2013);
                mChooseIconDialog.dismiss();
            }
        });
        m14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2014;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2014);
                mChooseIconDialog.dismiss();
            }
        });
        m15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2015;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2015);
                mChooseIconDialog.dismiss();
            }
        });
        m16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2016;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2016);
                mChooseIconDialog.dismiss();
            }
        });
        m17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2017;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2017);
                mChooseIconDialog.dismiss();
            }
        });
        m18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2018;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2018);
                mChooseIconDialog.dismiss();
            }
        });
        m19.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2019;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2019);
                mChooseIconDialog.dismiss();
            }
        });
        m20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2020;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2020);
                mChooseIconDialog.dismiss();
            }
        });
        m21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2021;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2021);
                mChooseIconDialog.dismiss();
            }
        });
        m22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2022;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2022);
                mChooseIconDialog.dismiss();
            }
        });
        m23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2023;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2023);
                mChooseIconDialog.dismiss();
            }
        });
        m24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2024;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2024);
                mChooseIconDialog.dismiss();
            }
        });
        m25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2025;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2025);
                mChooseIconDialog.dismiss();
            }
        });
        m26.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2026;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2026);
                mChooseIconDialog.dismiss();
            }
        });
        m27.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2027;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2027);
                mChooseIconDialog.dismiss();
            }
        });
        m28.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2028;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2028);
                mChooseIconDialog.dismiss();
            }
        });
        m29.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2029;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2029);
                mChooseIconDialog.dismiss();
            }
        });
        m30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2030;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2030);
                mChooseIconDialog.dismiss();
            }
        });
        m31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2031;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2031);
                mChooseIconDialog.dismiss();
            }
        });
        m32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2032;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2032);
                mChooseIconDialog.dismiss();
            }
        });
        m33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2033;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2033);
                mChooseIconDialog.dismiss();
            }
        });
        m34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2034;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2034);
                mChooseIconDialog.dismiss();
            }
        });
        m35.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2035;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2035);
                mChooseIconDialog.dismiss();
            }
        });
        m36.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2036;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2036);
                mChooseIconDialog.dismiss();
            }
        });
        m37.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2037;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2037);
                mChooseIconDialog.dismiss();
            }
        });
        m38.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2038;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2038);
                mChooseIconDialog.dismiss();
            }
        });
        m39.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2039;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2039);
                mChooseIconDialog.dismiss();
            }
        });
        m40.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2040;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2040);
                mChooseIconDialog.dismiss();
            }
        });
        m41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2041;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2041);
                mChooseIconDialog.dismiss();
            }
        });
        m42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2042;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2042);
                mChooseIconDialog.dismiss();
            }
        });
        m43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2043;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2043);
                mChooseIconDialog.dismiss();
            }
        });
        m44.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2044;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2044);
                mChooseIconDialog.dismiss();
            }
        });
        m45.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2045;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2045);
                mChooseIconDialog.dismiss();
            }
        });
        m46.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2046;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2046);
                mChooseIconDialog.dismiss();
            }
        });
        m47.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosenIcon = 2047;
                mChosenIconImageButton.setImageResource(R.drawable.ic_cat_2047);
                mChooseIconDialog.dismiss();
            }
        });

        mChooseIconDialog.setCanceledOnTouchOutside(false);
        mChooseIconDialog.show();
    }

    void toLoginActivity() {
        Intent intent = new Intent(CategoriesActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
