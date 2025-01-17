package com.everyday.skara.everyday.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.everyday.skara.everyday.DonutProgress;
import com.everyday.skara.everyday.LoginActivity;
import com.everyday.skara.everyday.R;
import com.everyday.skara.everyday.classes.BasicSettings;
import com.everyday.skara.everyday.classes.DateExpenseHolder;
import com.everyday.skara.everyday.classes.ExpenseTypes;
import com.everyday.skara.everyday.classes.FirebaseReferences;
import com.everyday.skara.everyday.classes.SPNames;
import com.everyday.skara.everyday.pojo.Categories;
import com.everyday.skara.everyday.pojo.FinanceEntryPOJO;
import com.everyday.skara.everyday.pojo.UserInfoPOJO;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class PersonalFinanceAnalytics extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    UserInfoPOJO userInfoPOJO;
    ArrayList<FinanceEntryPOJO> expensePOJOArrayList;
    ValueEventListener mExpenseValueEventListener;
    DatabaseReference mExpensesDatabaseReference;

    BottomSheetDialog mMonthBottomSheetDialog, mCategoriesBottomSheetDialog;

    HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>>> catYearMonthExpenseArrayListHashMap;
    HashMap<Integer, HashMap<Integer, HashMap<String, ArrayList<FinanceEntryPOJO>>>> yearMonthDateHashMap;

    ArrayList<Categories> categoriesArrayList;

    FloatingTextButton mMonthSelectionButton, mCategorySelectionButton;
    TextView mTotalExpenseTextView, mTotalIncomeTextView, mRemaining;
    TextView mCurencyTextView, mPositiveCurr;

    View view;

    int currentYear;
    int currentMonth;
    HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>> yearMonthExpenseArrayListHashMap;
    HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>> yearMonthIncomeArrayListHashMap;

    ArrayList<CategoryExpensePOJO> mCategoryExpenseArrayList = new ArrayList<>();
    HashMap<String, ArrayList<WeekDayWiseExpense>> weekWiseExpenseHashMap;

    TextView mMaxExpenseCat, mMaxExpenseCatName;
    TextView mExpenseDateTextView, mExpenseDayAmount;
    TextView mExpensiveExpenseTextView, mExpensiveExpenseCatTextView;
    DonutProgress mDonutProgress;
    PieChart mPieChart;
    BarChart mWeekDayWiseBarChart, mCategoryWiseBarChart;
    ImageButton mCatIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            view = inflater.inflate(R.layout.fragment_personal_finance_analytics_layout_light, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_personal_finance_analytics_layout, container, false);

        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            if (user != null) {
                init();
            } else {
                toLoginActivity();
            }
        }
    }

    void init() {
        userInfoPOJO = (UserInfoPOJO) getArguments().getSerializable("user_profile");

        mMonthSelectionButton = view.findViewById(R.id.month_selection_button);
        mCategorySelectionButton = view.findViewById(R.id.category_selection_chart_ftb);
        mTotalExpenseTextView = view.findViewById(R.id.total_amount_textview);
        mTotalIncomeTextView = view.findViewById(R.id.total_income_textview);
        mCurencyTextView = view.findViewById(R.id.currency_textview);
        mMaxExpenseCatName = view.findViewById(R.id.max_expense_cat_name_textview);
        mPositiveCurr = view.findViewById(R.id.positive_currency_all_textview);
        mMaxExpenseCat = view.findViewById(R.id.max_expense_cat_textview);
        mExpenseDateTextView = view.findViewById(R.id.max_expense_day_date_textview);
        mExpenseDayAmount = view.findViewById(R.id.max_expense_day_textview);
        mRemaining = view.findViewById(R.id.total_remaining);
        mExpensiveExpenseTextView = view.findViewById(R.id.maximum_expense_amount);
        mExpensiveExpenseCatTextView = view.findViewById(R.id.maximum_expense_cat);
        mWeekDayWiseBarChart = view.findViewById(R.id.weekday_wise_bar_chart);
        mCategoryWiseBarChart = view.findViewById(R.id.category_wise_bar_chart);
        mDonutProgress = view.findViewById(R.id.donut_progress);
        mDonutProgress.setProgress(0.0f);
        mCatIcon = view.findViewById(R.id.expense_cat_icon_analytics_fragment);

        mPieChart = view.findViewById(R.id.expense_pie_chart);

        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        mCurencyTextView.setText(currency);
        mPositiveCurr.setText(currency);
        expensePOJOArrayList = new ArrayList<>();

        currentYear = PersonalFinancialBoardFragment.mViewCurrentYear;
        currentMonth = PersonalFinancialBoardFragment.mViewCurrentMonth;

        mTotalExpenseTextView.setText("0.00");
        mTotalIncomeTextView.setText("0.00");
        mRemaining.setText("0.00");
        mDonutProgress.setProgress(0.0f);
        updateMonthTitle();

        mMonthSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthSelectionDialog();
            }
        });

        mCategorySelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryChartSelectionDialog();
            }
        });

        mMaxExpenseCatName.setText(null);
        mMaxExpenseCat.setText(null);
        mExpenseDateTextView.setText(null);
        mExpenseDayAmount.setText(null);
        mExpensiveExpenseTextView.setText(null);
        mExpensiveExpenseCatTextView.setText(null);
        initCategories();
    }

    void showMonthSelectionDialog() {
        mMonthBottomSheetDialog = new BottomSheetDialog(getActivity());
        int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            mMonthBottomSheetDialog.setContentView(R.layout.dialog_month_selection_layout_light);

        } else {
            mMonthBottomSheetDialog.setContentView(R.layout.dialog_month_selection_layout);
        }
        ImageButton mClose = mMonthBottomSheetDialog.findViewById(R.id.close_month_selection_dialog);
        Button mDone = mMonthBottomSheetDialog.findViewById(R.id.month_selection_done);

        Button m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12;
        m1 = mMonthBottomSheetDialog.findViewById(R.id.month_1_button);
        m2 = mMonthBottomSheetDialog.findViewById(R.id.month_2_button);
        m3 = mMonthBottomSheetDialog.findViewById(R.id.month_3_button);
        m4 = mMonthBottomSheetDialog.findViewById(R.id.month_4_button);
        m5 = mMonthBottomSheetDialog.findViewById(R.id.month_5_button);
        m6 = mMonthBottomSheetDialog.findViewById(R.id.month_6_button);
        m7 = mMonthBottomSheetDialog.findViewById(R.id.month_7_button);
        m8 = mMonthBottomSheetDialog.findViewById(R.id.month_8_button);
        m9 = mMonthBottomSheetDialog.findViewById(R.id.month_9_button);
        m10 = mMonthBottomSheetDialog.findViewById(R.id.month_10_button);
        m11 = mMonthBottomSheetDialog.findViewById(R.id.month_11_button);
        m12 = mMonthBottomSheetDialog.findViewById(R.id.month_12_button);

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthBottomSheetDialog.dismiss();
            }
        });
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthBottomSheetDialog.dismiss();
            }
        });
        m1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 0;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();
                mMonthBottomSheetDialog.dismiss();
            }
        });
        m2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 1;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 2;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 3;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 4;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 5;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 6;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 7;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 8;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 9;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 10;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        m12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = 11;
                updateTotalExpense();
                updateDayExpenses();
                updateCategoryExpenses();

                mMonthBottomSheetDialog.dismiss();
            }
        });
        mMonthBottomSheetDialog.setCanceledOnTouchOutside(true);
        mMonthBottomSheetDialog.show();
    }

    void showCategoryChartSelectionDialog() {

        mCategoriesBottomSheetDialog = new BottomSheetDialog(getActivity());
        int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {

            mCategoriesBottomSheetDialog.setContentView(R.layout.dialog_choose_category_analytics_layout_light);
        } else {
            mCategoriesBottomSheetDialog.setContentView(R.layout.dialog_choose_category_analytics_layout_dark);

        }
        ImageButton mAllCategoryImageButton = mCategoriesBottomSheetDialog.findViewById(R.id.all_category_image_button);
        Button mAllCategoryButton = mCategoriesBottomSheetDialog.findViewById(R.id.all_category_button);

        ImageButton mClose = mCategoriesBottomSheetDialog.findViewById(R.id.close_cat_option_dialog);
        RecyclerView mCategoriesRecyclerView = mCategoriesBottomSheetDialog.findViewById(R.id.recyclerview_choose_category);

        mCategoriesRecyclerView.invalidate();
        mCategoriesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mCategoriesRecyclerView.setLayoutManager(linearLayoutManager);
        CatAdapter catAdapter = new CatAdapter();
        mCategoriesRecyclerView.setAdapter(catAdapter);

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoriesBottomSheetDialog.dismiss();
            }
        });

        mAllCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
                if (theme == BasicSettings.LIGHT_THEME) {
                    reflectedBarChart();

                } else {
                    reflectedBarChartDark();

                }
                mCategorySelectionButton.setTitle("All");
                mCategoriesBottomSheetDialog.dismiss();

            }

        });

        mAllCategoryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
                if (theme == BasicSettings.LIGHT_THEME) {
                    reflectedBarChart();

                } else {
                    reflectedBarChartDark();

                }
                mCategorySelectionButton.setTitle("All");
                mCategoriesBottomSheetDialog.dismiss();

            }
        });

        mCategoriesBottomSheetDialog.setCanceledOnTouchOutside(true);
        mCategoriesBottomSheetDialog.show();
    }


    public class CatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater inflator;
        //ArrayList<Categories> categoriesArrayList;

        public CatAdapter() {
            try {
                this.inflator = LayoutInflater.from(mCategoriesBottomSheetDialog.getContext());
                // this.categoriesArrayList = categoriesArrayList;
            } catch (NullPointerException e) {

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
            if (theme == BasicSettings.LIGHT_THEME) {
                View view = inflator.inflate(R.layout.recyclerview_expense_catgories_row_layout_light, parent, false);
                return new CatAdapter.CatViewHolder(view);

            } else {
                View view = inflator.inflate(R.layout.recyclerview_expense_catgories_row_layout, parent, false);
                return new CatAdapter.CatViewHolder(view);

            }

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Categories categories = categoriesArrayList.get(position);
            ((CatAdapter.CatViewHolder) holder).mCatName.setText(categories.getCategoryName());
            showCatIcon(holder, categories);
            showRecyclerCatIconBackground(holder, categories);
        }


        void showCatIcon(@NonNull RecyclerView.ViewHolder holder, Categories categories) {
            switch (categories.getCategoryIconId()) {
                case 2000:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                    break;
                case 2001:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2001);
                    break;
                case 2002:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2002);
                    break;
                case 2003:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2003);
                    break;
                case 2004:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2004);
                    break;
                case 2005:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2005);
                    break;
                case 2006:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2006);
                    break;
                case 2007:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2007);
                    break;

                case 2008:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2008);
                    break;

                case 2009:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2009);
                    break;

                case 2010:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2010);
                    break;

                case 2011:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2011);
                    break;

                case 2012:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2012);
                    break;

                case 2013:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2013);
                    break;

                case 2014:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2014);
                    break;

                case 2015:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2015);
                    break;

                case 2016:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2016);
                    break;

                case 2017:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2017);
                    break;

                case 2018:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2018);
                    break;

                case 2019:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2019);
                    break;

                case 2020:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2020);
                    break;

                case 2021:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2021);
                    break;

                case 2022:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2022);
                    break;

                case 2023:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2023);
                    break;

                case 2024:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2024);
                    break;

                case 2025:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2025);
                    break;

                case 2026:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2026);
                    break;

                case 2027:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2027);
                    break;

                case 2028:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2028);
                    break;

                case 2029:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2029);
                    break;

                case 2030:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2030);
                    break;
                case 2031:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2031);
                    break;
                case 2032:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2032);
                    break;
                case 2033:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2033);
                    break;
                case 2034:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2034);
                    break;
                case 2035:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2035);
                    break;
                case 2036:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2036);
                    break;
                case 2037:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2037);
                    break;
                case 2038:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2038);
                    break;
                case 2039:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2039);
                    break;
                case 2040:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2040);
                    break;
                case 2041:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2041);
                    break;
                case 2042:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2042);
                    break;
                case 2043:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2043);
                    break;
                case 2044:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2044);
                    break;
                case 2045:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2045);
                    break;
                case 2046:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2046);
                    break;
                case 2047:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2047);
                    break;
                default:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                    break;
            }
        }

        void showRecyclerCatIconBackground(@NonNull RecyclerView.ViewHolder holder, Categories categories) {
            switch (categories.getColorId()) {
                case 1:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_red);
                    break;
                case 2:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_yellow);
                    break;
                case 3:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_blue);
                    break;
                case 4:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_green);
                    break;
                case 5:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_green_blue);
                    break;
                case 6:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_pink);
                    break;
                default:
                    ((CatAdapter.CatViewHolder) holder).mCatIcon.setBackgroundResource(R.drawable.circle_background_blue);
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
                        updateCategorySelected(getPosition());
                    }
                });
            }
        }
    }

    void updateCategorySelected(int position) {
        mCategorySelectionButton.setTitle(categoriesArrayList.get(position).getCategoryName());
        mCategoriesBottomSheetDialog.dismiss();
        int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            reflectCategoryWiseBarChart(categoriesArrayList.get(position).getCategoryKey(), categoriesArrayList.get(position).getCategoryName());

        } else {
            reflectCategoryWiseBarChartDark(categoriesArrayList.get(position).getCategoryKey(), categoriesArrayList.get(position).getCategoryName());

        }

    }

    void initCategories() {
        categoriesArrayList = new ArrayList<>();

        mExpensesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/categories");
        mExpensesDatabaseReference.keepSynced(true);
        mExpensesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getValue(Categories.class).getCategoryIconId() != 2048) {
                        categoriesArrayList.add(snapshot.getValue(Categories.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        initExpenses();


    }

    void updateMonthTitle() {
        switch (currentMonth) {
            case 0:
                mMonthSelectionButton.setTitle("Jan");
                break;
            case 1:
                mMonthSelectionButton.setTitle("Feb");
                break;
            case 2:
                mMonthSelectionButton.setTitle("Mar");
                break;
            case 3:
                mMonthSelectionButton.setTitle("Apr");
                break;
            case 4:
                mMonthSelectionButton.setTitle("May");
                break;
            case 5:
                mMonthSelectionButton.setTitle("Jun");
                break;
            case 6:
                mMonthSelectionButton.setTitle("Jul");
                break;
            case 7:
                mMonthSelectionButton.setTitle("Aug");
                break;
            case 8:
                mMonthSelectionButton.setTitle("Sep");
                break;
            case 9:
                mMonthSelectionButton.setTitle("Oct");
                break;
            case 10:
                mMonthSelectionButton.setTitle("Nov");
                break;
            case 11:
                mMonthSelectionButton.setTitle("Dec");
                break;

        }
    }

    void initExpenses() {
        expensePOJOArrayList = new ArrayList<>();

        yearMonthExpenseArrayListHashMap = new HashMap<>();
        yearMonthIncomeArrayListHashMap = new HashMap<>();
        yearMonthDateHashMap = new HashMap<>();
        catYearMonthExpenseArrayListHashMap = new HashMap<>();

        mExpensesDatabaseReference = firebaseDatabase.getReference(FirebaseReferences.FIREBASE_USER_DETAILS + userInfoPOJO.getUser_key() + "/" + FirebaseReferences.FIREBASE_PERSONAL_BOARD_FINANCIAL + "/expenses");
        mExpensesDatabaseReference.keepSynced(true);
        mExpenseValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        FinanceEntryPOJO expensePOJO = dataSnapshot.getValue(FinanceEntryPOJO.class);
                        if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_EXPENSE) {
                            expensePOJOArrayList.add(expensePOJO);

                            /**----------------------------------------------------------------------*/
                            // expense YEAR/MONTH wise
                            if (yearMonthExpenseArrayListHashMap.containsKey(expensePOJO.getYear())) {
                                if (yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                                    expensePOJOArrayList2.add(expensePOJO);
                                    yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList2);
                                } else {
                                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = new ArrayList<>();
                                    expensePOJOArrayList2.add(expensePOJO);
                                    yearMonthExpenseArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList2);
                                }
                            } else {
                                HashMap<Integer, ArrayList<FinanceEntryPOJO>> monthHashMap = new HashMap<>();
                                ArrayList<FinanceEntryPOJO> expensePOJOArrayListTemp = new ArrayList<>();
                                expensePOJOArrayListTemp.add(expensePOJO);
                                monthHashMap.put(expensePOJO.getMonth(), expensePOJOArrayListTemp);
                                yearMonthExpenseArrayListHashMap.put(expensePOJO.getYear(), monthHashMap);
                            }

                            /**----------------------------------------------------------------------*/
                            // expense YEAR/MONTH/DATE wise
                            if (yearMonthDateHashMap.containsKey(expensePOJO.getYear())) {
                                if (yearMonthDateHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                                    if (yearMonthDateHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth()).containsKey(expensePOJO.getDate())) {
                                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = yearMonthDateHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth()).get(expensePOJO.getDate());
                                        expensePOJOArrayList2.add(expensePOJO);
                                        yearMonthDateHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth()).put(expensePOJO.getDate(), expensePOJOArrayList2);
                                    } else {
                                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList5 = new ArrayList<>();
                                        expensePOJOArrayList5.add(expensePOJO);
                                        yearMonthDateHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth()).put(expensePOJO.getDate(), expensePOJOArrayList5);
                                    }
                                } else {
                                    HashMap<String, ArrayList<FinanceEntryPOJO>> dateExpenseHashMap = new HashMap<>();
                                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = new ArrayList<>();
                                    expensePOJOArrayList2.add(expensePOJO);
                                    dateExpenseHashMap.put(expensePOJO.getDate(), expensePOJOArrayList2);
                                    yearMonthDateHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), dateExpenseHashMap);
                                }
                            } else {
                                HashMap<Integer, HashMap<String, ArrayList<FinanceEntryPOJO>>> monthDateExpenseHashMap = new HashMap<>();
                                HashMap<String, ArrayList<FinanceEntryPOJO>> dateExpenseHashMap = new HashMap<>();
                                ArrayList<FinanceEntryPOJO> expensePOJOArrayList2 = new ArrayList<>();
                                expensePOJOArrayList2.add(expensePOJO);
                                dateExpenseHashMap.put(expensePOJO.getDate(), expensePOJOArrayList2);
                                monthDateExpenseHashMap.put(expensePOJO.getMonth(), dateExpenseHashMap);
                                yearMonthDateHashMap.put(expensePOJO.getYear(), monthDateExpenseHashMap);
                            }
                            /**----------------------------------------------------------------------*/
                            if (catYearMonthExpenseArrayListHashMap.containsKey(expensePOJO.getCategories().getCategoryKey())) {
                                if (catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).containsKey(expensePOJO.getYear())) {
                                    if (catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList4 = catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                                        expensePOJOArrayList4.add(expensePOJO);
                                        catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList4);
                                    } else {
                                        ArrayList<FinanceEntryPOJO> expensePOJOArrayList4 = new ArrayList<>();
                                        expensePOJOArrayList4.add(expensePOJO);
                                        catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).get(expensePOJO.getYear()).put(expensePOJO.getMonth(), expensePOJOArrayList4);

                                    }

                                } else {
                                    HashMap<Integer, ArrayList<FinanceEntryPOJO>> monthHashMap = new HashMap<>();

                                    ArrayList<FinanceEntryPOJO> expensePOJOArrayListTemp = new ArrayList<>();
                                    expensePOJOArrayListTemp.add(expensePOJO);

                                    monthHashMap.put(expensePOJO.getMonth(), expensePOJOArrayListTemp);

                                    catYearMonthExpenseArrayListHashMap.get(expensePOJO.getCategories().getCategoryKey()).put(expensePOJO.getYear(), monthHashMap);
                                }
                            } else {
                                HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>> yearMonthHashMap = new HashMap<>();
                                HashMap<Integer, ArrayList<FinanceEntryPOJO>> monthHashMap = new HashMap<>();

                                ArrayList<FinanceEntryPOJO> expensePOJOArrayListTemp = new ArrayList<>();
                                expensePOJOArrayListTemp.add(expensePOJO);

                                monthHashMap.put(expensePOJO.getMonth(), expensePOJOArrayListTemp);
                                yearMonthHashMap.put(expensePOJO.getYear(), monthHashMap);
                                catYearMonthExpenseArrayListHashMap.put(expensePOJO.getCategories().getCategoryKey(), yearMonthHashMap);
                            }
                        } else if (expensePOJO.getEntryType() == ExpenseTypes.ENTRY_TYPE_INCOME) {
                            /** ----------------------------------------------------*/
                            // income YEAR/MONTH wise
                            if (yearMonthIncomeArrayListHashMap.containsKey(expensePOJO.getYear())) {
                                if (yearMonthIncomeArrayListHashMap.get(expensePOJO.getYear()).containsKey(expensePOJO.getMonth())) {
                                    ArrayList<FinanceEntryPOJO> incomePOJOArrayList2 = yearMonthIncomeArrayListHashMap.get(expensePOJO.getYear()).get(expensePOJO.getMonth());
                                    incomePOJOArrayList2.add(expensePOJO);
                                    yearMonthIncomeArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), incomePOJOArrayList2);
                                } else {
                                    ArrayList<FinanceEntryPOJO> incomePOJOArrayList2 = new ArrayList<>();
                                    incomePOJOArrayList2.add(expensePOJO);
                                    yearMonthIncomeArrayListHashMap.get(expensePOJO.getYear()).put(expensePOJO.getMonth(), incomePOJOArrayList2);
                                }
                            } else {
                                HashMap<Integer, ArrayList<FinanceEntryPOJO>> monthHashMap = new HashMap<>();
                                ArrayList<FinanceEntryPOJO> incomePOJOArrayList2Temp = new ArrayList<>();
                                incomePOJOArrayList2Temp.add(expensePOJO);
                                monthHashMap.put(expensePOJO.getMonth(), incomePOJOArrayList2Temp);
                                yearMonthIncomeArrayListHashMap.put(expensePOJO.getYear(), monthHashMap);
                            }
                        }
                    }
                }

                /**----------------------------------------------------------------------*/

                // reflect updated data
                updateTotalExpense();
                updateCategoryExpenses();
                updateDayExpenses();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mExpensesDatabaseReference.addValueEventListener(mExpenseValueEventListener);
    }

    void updateTotalExpense() {
        mDonutProgress.setProgress(0.0f);
        mRemaining.setText("0.00");
        double tempTotal = 0.0;
        mTotalExpenseTextView.setText("0.00");
        updateMonthTitle();
        if (yearMonthExpenseArrayListHashMap.containsKey(currentYear)) {
            if (yearMonthExpenseArrayListHashMap.get(currentYear).containsKey(currentMonth)) {
                if (yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth).size() == 0) {
                    mTotalExpenseTextView.setText("0.00");
                } else {
                    ArrayList<FinanceEntryPOJO> tempExpensePOJO = yearMonthExpenseArrayListHashMap.get(currentYear).get(currentMonth);
                    tempTotal = 0.0;
                    for (int i = 0; i < tempExpensePOJO.size(); i++) {
                        if (tempExpensePOJO.get(i).getEntryType() == ExpenseTypes.ENTRY_TYPE_EXPENSE) {
                            tempTotal += tempExpensePOJO.get(i).getAmount();
                        }
                    }
                    mTotalExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", tempTotal));
                }
            }
        }
        updateTotalIncome(tempTotal);
    }

    void updateTotalIncome(double tempExpense) {
        double tempTotal = 0.0;
        if (yearMonthIncomeArrayListHashMap.containsKey(currentYear)) {
            if (yearMonthIncomeArrayListHashMap.get(currentYear).containsKey(currentMonth)) {
                if (yearMonthIncomeArrayListHashMap.get(currentYear).get(currentMonth).size() == 0) {
                    mTotalIncomeTextView.setText("0.00");
                } else {
                    ArrayList<FinanceEntryPOJO> tempIncomePOJO = yearMonthIncomeArrayListHashMap.get(currentYear).get(currentMonth);
                    tempTotal = 0.0;
                    for (int i = 0; i < tempIncomePOJO.size(); i++) {
                        if (tempIncomePOJO.get(i).getEntryType() == ExpenseTypes.ENTRY_TYPE_INCOME) {
                            tempTotal += tempIncomePOJO.get(i).getAmount();
                        }
                    }
                    mTotalIncomeTextView.setText(String.format(Locale.getDefault(), "%.2f", tempTotal));
                }
            }
        }

        double percentageValue = 0.0;
        if (tempTotal > 0) {
            percentageValue = (tempExpense * 100) / tempTotal;
        }
        ;
        mDonutProgress.setProgress(Math.round(Float.valueOf(String.valueOf(percentageValue))));
        updateRemainingIncomeView(tempExpense, tempTotal);


    }

    void updateRemainingIncomeView(double expense, double income) {
        String currency = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getString("currency", getResources().getString(R.string.inr));
        double difference = income - expense;
        if (difference > 0) {
            mRemaining.setTextColor(getResources().getColor(R.color.green_selected));
            mRemaining.setText(" + " + currency + String.format(Locale.getDefault(), "%.2f", difference));
        } else {
            mRemaining.setTextColor(getResources().getColor(R.color.red));
            mRemaining.setText(" - " + currency + String.format(Locale.getDefault(), "%.2f", Math.abs(difference)));
        }
    }

    class DayExpensePOJO {
        double maxDayExpenseAmount;
        String dateOfMaxExpense;
        ArrayList<FinanceEntryPOJO> expensePOJOArrayList;

        public DayExpensePOJO() {
        }

        public DayExpensePOJO(double maxDayExpenseAmount, String dateOfMaxExpense, ArrayList<FinanceEntryPOJO> expensePOJOArrayList) {
            this.maxDayExpenseAmount = maxDayExpenseAmount;
            this.dateOfMaxExpense = dateOfMaxExpense;
            this.expensePOJOArrayList = expensePOJOArrayList;
        }


        public double getMaxDayExpenseAmount() {
            return maxDayExpenseAmount;
        }

        public void setMaxDayExpenseAmount(double maxDayExpenseAmount) {
            this.maxDayExpenseAmount = maxDayExpenseAmount;
        }

        public String getDateOfMaxExpense() {
            return dateOfMaxExpense;
        }

        public void setDateOfMaxExpense(String dateOfMaxExpense) {
            this.dateOfMaxExpense = dateOfMaxExpense;
        }

        public ArrayList<FinanceEntryPOJO> getExpensePOJOArrayList() {
            return expensePOJOArrayList;
        }

        public void setExpensePOJOArrayList(ArrayList<FinanceEntryPOJO> expensePOJOArrayList) {
            this.expensePOJOArrayList = expensePOJOArrayList;
        }
    }

    class WeekDayWiseExpense {
        String weekDay;
        ArrayList<FinanceEntryPOJO> expensePOJOArrayList;
        double totalWeekDayExpense;
        String date;

        public WeekDayWiseExpense(String weekDay, ArrayList<FinanceEntryPOJO> expensePOJOArrayList, double totalWeekDayExpense, String date) {
            this.weekDay = weekDay;
            this.expensePOJOArrayList = expensePOJOArrayList;
            this.totalWeekDayExpense = totalWeekDayExpense;
            this.date = date;
        }


        public String getWeekDay() {
            return weekDay;
        }

        public void setWeekDay(String weekDay) {
            this.weekDay = weekDay;
        }

        public ArrayList<FinanceEntryPOJO> getExpensePOJOArrayList() {
            return expensePOJOArrayList;
        }

        public void setExpensePOJOArrayList(ArrayList<FinanceEntryPOJO> expensePOJOArrayList) {
            this.expensePOJOArrayList = expensePOJOArrayList;
        }

        public double getTotalWeekDayExpense() {
            return totalWeekDayExpense;
        }

        public void setTotalWeekDayExpense(double totalWeekDayExpense) {
            this.totalWeekDayExpense = totalWeekDayExpense;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    void updateDayExpenses() {
        mCategorySelectionButton.setTitle("All");
        DayExpensePOJO dayExpensePOJO = new DayExpensePOJO(0.0, "-", new ArrayList<FinanceEntryPOJO>());
        FinanceEntryPOJO maxExpensePOJO = new FinanceEntryPOJO();
        maxExpensePOJO.setAmount(0.0);
        ArrayList<DateExpenseHolder> dateExpenseHolderArrayList;
        weekWiseExpenseHashMap = new HashMap<>();
        if (yearMonthDateHashMap.containsKey(currentYear)) {
            if (yearMonthDateHashMap.get(currentYear).containsKey(currentMonth)) {
                HashMap<String, ArrayList<FinanceEntryPOJO>> expensePOJOHashMapArrayList = yearMonthDateHashMap.get(currentYear).get(currentMonth);
                dateExpenseHolderArrayList = new ArrayList<>();
                for (Map.Entry<String, ArrayList<FinanceEntryPOJO>> entry : expensePOJOHashMapArrayList.entrySet()) {
                    dateExpenseHolderArrayList.add(new DateExpenseHolder(entry.getKey(), entry.getValue()));
                }

                for (int i = 0; i < dateExpenseHolderArrayList.size(); i++) {
                    DateExpenseHolder dateExpenseHolder = dateExpenseHolderArrayList.get(i);
                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList = dateExpenseHolder.getExpensePOJOArrayList();
                    double dayAmount = 0.0;
                    for (int j = 0; j < expensePOJOArrayList.size(); j++) {
                        FinanceEntryPOJO expensePOJO = expensePOJOArrayList.get(j);
                        dayAmount += expensePOJO.getAmount();
                        if (expensePOJO.getAmount() >= maxExpensePOJO.getAmount()) {
                            maxExpensePOJO = expensePOJO;
                        }
                    }

                    if (dayAmount >= dayExpensePOJO.getMaxDayExpenseAmount()) {
                        dayExpensePOJO = new DayExpensePOJO(dayAmount, dateExpenseHolder.getDate(), expensePOJOArrayList);
                    }

                    /**---------------------------------------------------------------------------*/
                    String dayOfWeek = null;
                    try {
                        dayOfWeek = new SimpleDateFormat("EE").format(new SimpleDateFormat("dd/MM/yyyy").parse(dateExpenseHolder.getDate()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (weekWiseExpenseHashMap.containsKey(dayOfWeek)) {
                        WeekDayWiseExpense weekDayWiseExpense = new WeekDayWiseExpense(dayOfWeek, expensePOJOArrayList, dayAmount, dateExpenseHolder.getDate());
                        ArrayList<WeekDayWiseExpense> weekDayWiseExpensesArrayList = weekWiseExpenseHashMap.get(dayOfWeek);
                        weekDayWiseExpensesArrayList.add(weekDayWiseExpense);
                        weekWiseExpenseHashMap.put(dayOfWeek, weekDayWiseExpensesArrayList);
                    } else {
                        WeekDayWiseExpense weekDayWiseExpense = new WeekDayWiseExpense(dayOfWeek, expensePOJOArrayList, dayAmount, dateExpenseHolder.getDate());
                        ArrayList<WeekDayWiseExpense> weekDayWiseExpensesArrayList = new ArrayList<>();
                        weekDayWiseExpensesArrayList.add(weekDayWiseExpense);
                        weekWiseExpenseHashMap.put(dayOfWeek, weekDayWiseExpensesArrayList);
                    }
                    /**---------------------------------------------------------------------------*/

                }

                mExpenseDateTextView.setText(dayExpensePOJO.getDateOfMaxExpense());
                String dayOfWeek = null;
                try {
                    dayOfWeek = new SimpleDateFormat("EE").format(new SimpleDateFormat("dd/MM/yyyy").parse(dayExpensePOJO.getDateOfMaxExpense()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                setCatIconBackground(maxExpensePOJO.getCategories());
                showCatIcon(maxExpensePOJO.getCategories());
                mExpenseDayAmount.setText(dayOfWeek);
                mExpensiveExpenseCatTextView.setText(maxExpensePOJO.getCategories().getCategoryName());
                mExpensiveExpenseTextView.setText(String.format(Locale.getDefault(), "%.2f", maxExpensePOJO.getAmount()));
            } else {
                // no spendings this month

            }

        }

        int theme = getActivity().getSharedPreferences(SPNames.DEFAULT_SETTINGS, Context.MODE_PRIVATE).getInt("theme", BasicSettings.DEFAULT_THEME);
        if (theme == BasicSettings.LIGHT_THEME) {
            reflectedBarChart();
            reflectWeekWiseBarChart();
        } else {
            reflectedBarChartDark();
            reflectWeekWiseBarChartDark();
        }

    }

    void setCatIconBackground(Categories categories) {
        switch (categories.getColorId()) {
            case 1:
                mCatIcon.setBackgroundResource(R.drawable.circle_background_red);
                break;
            case 2:
                mCatIcon.setBackgroundResource(R.drawable.circle_background_yellow);
                break;
            case 3:
                mCatIcon.setBackgroundResource(R.drawable.circle_background_blue);
                break;
            case 4:
                mCatIcon.setBackgroundResource(R.drawable.circle_background_green);
                break;
            case 5:
                mCatIcon.setBackgroundResource(R.drawable.circle_background_green_blue);
                break;
            case 6:
                mCatIcon.setBackgroundResource(R.drawable.circle_background_pink);
                break;
            default:
                mCatIcon.setBackgroundResource(R.drawable.circle_background_blue);
                break;
        }
    }


    void showCatIcon(Categories categories) {
        switch (categories.getCategoryIconId()) {
            case 2000:
                mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                break;
            case 2001:
                mCatIcon.setImageResource(R.drawable.ic_cat_2001);
                break;
            case 2002:
                mCatIcon.setImageResource(R.drawable.ic_cat_2002);
                break;
            case 2003:
                mCatIcon.setImageResource(R.drawable.ic_cat_2003);
                break;
            case 2004:
                mCatIcon.setImageResource(R.drawable.ic_cat_2004);
                break;
            case 2005:
                mCatIcon.setImageResource(R.drawable.ic_cat_2005);
                break;
            case 2006:
                mCatIcon.setImageResource(R.drawable.ic_cat_2006);
                break;
            case 2007:
                mCatIcon.setImageResource(R.drawable.ic_cat_2007);
                break;

            case 2008:
                mCatIcon.setImageResource(R.drawable.ic_cat_2008);
                break;

            case 2009:
                mCatIcon.setImageResource(R.drawable.ic_cat_2009);
                break;

            case 2010:
                mCatIcon.setImageResource(R.drawable.ic_cat_2010);
                break;

            case 2011:
                mCatIcon.setImageResource(R.drawable.ic_cat_2011);
                break;

            case 2012:
                mCatIcon.setImageResource(R.drawable.ic_cat_2012);
                break;

            case 2013:
                mCatIcon.setImageResource(R.drawable.ic_cat_2013);
                break;

            case 2014:
                mCatIcon.setImageResource(R.drawable.ic_cat_2014);
                break;

            case 2015:
                mCatIcon.setImageResource(R.drawable.ic_cat_2015);
                break;

            case 2016:
                mCatIcon.setImageResource(R.drawable.ic_cat_2016);
                break;

            case 2017:
                mCatIcon.setImageResource(R.drawable.ic_cat_2017);
                break;

            case 2018:
                mCatIcon.setImageResource(R.drawable.ic_cat_2018);
                break;

            case 2019:
                mCatIcon.setImageResource(R.drawable.ic_cat_2019);
                break;

            case 2020:
                mCatIcon.setImageResource(R.drawable.ic_cat_2020);
                break;

            case 2021:
                mCatIcon.setImageResource(R.drawable.ic_cat_2021);
                break;

            case 2022:
                mCatIcon.setImageResource(R.drawable.ic_cat_2022);
                break;

            case 2023:
                mCatIcon.setImageResource(R.drawable.ic_cat_2023);
                break;

            case 2024:
                mCatIcon.setImageResource(R.drawable.ic_cat_2024);
                break;

            case 2025:
                mCatIcon.setImageResource(R.drawable.ic_cat_2025);
                break;

            case 2026:
                mCatIcon.setImageResource(R.drawable.ic_cat_2026);
                break;

            case 2027:
                mCatIcon.setImageResource(R.drawable.ic_cat_2027);
                break;

            case 2028:
                mCatIcon.setImageResource(R.drawable.ic_cat_2028);
                break;

            case 2029:
                mCatIcon.setImageResource(R.drawable.ic_cat_2029);
                break;

            case 2030:
                mCatIcon.setImageResource(R.drawable.ic_cat_2030);
                break;
            case 2031:
                mCatIcon.setImageResource(R.drawable.ic_cat_2031);
                break;
            case 2032:
                mCatIcon.setImageResource(R.drawable.ic_cat_2032);
                break;
            case 2033:
                mCatIcon.setImageResource(R.drawable.ic_cat_2033);
                break;
            case 2034:
                mCatIcon.setImageResource(R.drawable.ic_cat_2034);
                break;
            case 2035:
                mCatIcon.setImageResource(R.drawable.ic_cat_2035);
                break;
            case 2036:
                mCatIcon.setImageResource(R.drawable.ic_cat_2036);
                break;
            case 2037:
                mCatIcon.setImageResource(R.drawable.ic_cat_2037);
                break;
            case 2038:
                mCatIcon.setImageResource(R.drawable.ic_cat_2038);
                break;
            case 2039:
                mCatIcon.setImageResource(R.drawable.ic_cat_2039);
                break;
            case 2040:
                mCatIcon.setImageResource(R.drawable.ic_cat_2040);
                break;
            case 2041:
                mCatIcon.setImageResource(R.drawable.ic_cat_2041);
                break;
            case 2042:
                mCatIcon.setImageResource(R.drawable.ic_cat_2042);
                break;
            case 2043:
                mCatIcon.setImageResource(R.drawable.ic_cat_2043);
                break;
            case 2044:
                mCatIcon.setImageResource(R.drawable.ic_cat_2044);
                break;
            case 2045:
                mCatIcon.setImageResource(R.drawable.ic_cat_2045);
                break;
            case 2046:
                mCatIcon.setImageResource(R.drawable.ic_cat_2046);
                break;
            case 2047:
                mCatIcon.setImageResource(R.drawable.ic_cat_2047);
                break;
            default:
                mCatIcon.setImageResource(R.drawable.ic_cat_2000);
                break;
        }
    }

    void reflectCategoryWiseBarChart(String categoryKey, String catName) {
        List<BarEntry> entries = new ArrayList<>();
        Calendar refCal = new GregorianCalendar();
        refCal.set(Calendar.YEAR, currentYear);
        refCal.set(Calendar.MONTH, currentMonth);
        if (catYearMonthExpenseArrayListHashMap.containsKey(categoryKey)) {
            HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>> categoryContainerMap = catYearMonthExpenseArrayListHashMap.get(categoryKey);
            if (categoryContainerMap.containsKey(currentYear)) {
                if (categoryContainerMap.get(currentYear).containsKey(currentMonth)) {
                    ArrayList<FinanceEntryPOJO> financeEntryPOJOArrayList = categoryContainerMap.get(currentYear).get(currentMonth);
                    for (int j = 0; j < financeEntryPOJOArrayList.size(); j++) {
                        String dayOfWeek = null;
                        try {
                            dayOfWeek = new SimpleDateFormat("d").format(new SimpleDateFormat("dd/MM/yyyy").parse(financeEntryPOJOArrayList.get(j).getDate()));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        entries.add(new BarEntry(Float.valueOf(dayOfWeek), Float.valueOf(String.valueOf(financeEntryPOJOArrayList.get(j).getAmount()))));

                    }
                }
            }
        }

        XAxis xAxis = mCategoryWiseBarChart.getXAxis();
        YAxis yAxis = mCategoryWiseBarChart.getAxisLeft();
        YAxis yAxis1 = mCategoryWiseBarChart.getAxisRight();
        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f);// set custom bar width
        mCategoryWiseBarChart.getLegend().setEnabled(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        yAxis.setGranularityEnabled(true);
        yAxis.setGranularity(100f);
        yAxis.setDrawAxisLine(false);
        yAxis.setStartAtZero(true);
        yAxis1.setStartAtZero(true);
        yAxis1.setDrawAxisLine(false);
        yAxis1.setDrawLabels(false);
        yAxis.setDrawGridLines(true);
        yAxis1.setDrawGridLines(false);
        yAxis1.setDrawZeroLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        data.setValueTextSize(7);
        xAxis.setTextSize(11);
        yAxis.setTextSize(11);

        // if dates max is greater than 10 then use date, otherwise Calendar.DAY OF MONTH
        xAxis.setAxisMaximum((float) refCal.getActualMaximum(Calendar.DAY_OF_MONTH)); //TODO set to maximum date present
        xAxis.setAxisMinimum(0f);
        Description description = new Description();
        description.setText(catName + "Expenses");

        mCategoryWiseBarChart.setDescription(description);
        mCategoryWiseBarChart.setScaleEnabled(false);
        mCategoryWiseBarChart.setDrawGridBackground(false);
        mCategoryWiseBarChart.setData(data);
        mCategoryWiseBarChart.setFitBars(false); // make the x-axis fit exactly all bars
        mCategoryWiseBarChart.invalidate(); // refresh
    }

    void reflectCategoryWiseBarChartDark(String categoryKey, String catName) {
        List<BarEntry> entries = new ArrayList<>();
        Calendar refCal = new GregorianCalendar();
        refCal.set(Calendar.YEAR, currentYear);
        refCal.set(Calendar.MONTH, currentMonth);
        if (catYearMonthExpenseArrayListHashMap.containsKey(categoryKey)) {
            HashMap<Integer, HashMap<Integer, ArrayList<FinanceEntryPOJO>>> categoryContainerMap = catYearMonthExpenseArrayListHashMap.get(categoryKey);
            if (categoryContainerMap.containsKey(currentYear)) {
                if (categoryContainerMap.get(currentYear).containsKey(currentMonth)) {
                    ArrayList<FinanceEntryPOJO> financeEntryPOJOArrayList = categoryContainerMap.get(currentYear).get(currentMonth);
                    for (int j = 0; j < financeEntryPOJOArrayList.size(); j++) {
                        String dayOfWeek = null;
                        try {
                            dayOfWeek = new SimpleDateFormat("d").format(new SimpleDateFormat("dd/MM/yyyy").parse(financeEntryPOJOArrayList.get(j).getDate()));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        entries.add(new BarEntry(Float.valueOf(dayOfWeek), Float.valueOf(String.valueOf(financeEntryPOJOArrayList.get(j).getAmount()))));

                    }
                }
            }
        }

        XAxis xAxis = mCategoryWiseBarChart.getXAxis();
        YAxis yAxis = mCategoryWiseBarChart.getAxisLeft();
        YAxis yAxis1 = mCategoryWiseBarChart.getAxisRight();
        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f);// set custom bar width

        mCategoryWiseBarChart.getLegend().setEnabled(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        data.setValueTextColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setGranularityEnabled(true);
        yAxis.setGranularity(100f);
        yAxis.setDrawAxisLine(false);
        yAxis.setStartAtZero(true);
        yAxis1.setStartAtZero(true);
        yAxis1.setDrawAxisLine(false);
        yAxis1.setDrawLabels(false);
        yAxis.setDrawGridLines(true);
        yAxis1.setDrawGridLines(false);
        yAxis1.setDrawZeroLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        data.setValueTextSize(7);
        xAxis.setTextSize(11);
        yAxis.setTextSize(11);

        // if dates max is greater than 10 then use date, otherwise Calendar.DAY OF MONTH
        xAxis.setAxisMaximum((float) refCal.getActualMaximum(Calendar.DAY_OF_MONTH)); //TODO set to maximum date present
        xAxis.setAxisMinimum(0f);

        Description description = new Description();
        description.setText(catName + "Expenses");
        description.setTextColor(Color.WHITE);
        mCategoryWiseBarChart.setDescription(description);
        mCategoryWiseBarChart.setScaleEnabled(false);
        mCategoryWiseBarChart.setDrawGridBackground(false);
        mCategoryWiseBarChart.setData(data);
        mCategoryWiseBarChart.setFitBars(false); // make the x-axis fit exactly all bars
        mCategoryWiseBarChart.invalidate(); // refresh
    }

    void reflectedBarChart() {

        List<BarEntry> entries = new ArrayList<>();
        Calendar refCal = new GregorianCalendar();
        refCal.set(Calendar.YEAR, currentYear);
        refCal.set(Calendar.MONTH, currentMonth);
        ArrayList<DateExpenseHolder> dateExpenseHolderArrayList;
        if (yearMonthDateHashMap.containsKey(currentYear)) {
            if (yearMonthDateHashMap.get(currentYear).containsKey(currentMonth)) {
                HashMap<String, ArrayList<FinanceEntryPOJO>> expensePOJOHashMapArrayList = yearMonthDateHashMap.get(currentYear).get(currentMonth);
                dateExpenseHolderArrayList = new ArrayList<>();
                for (Map.Entry<String, ArrayList<FinanceEntryPOJO>> entry : expensePOJOHashMapArrayList.entrySet()) {
                    dateExpenseHolderArrayList.add(new DateExpenseHolder(entry.getKey(), entry.getValue()));
                }

                for (int i = 0; i < dateExpenseHolderArrayList.size(); i++) {
                    DateExpenseHolder dateExpenseHolder = dateExpenseHolderArrayList.get(i);
                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList = dateExpenseHolder.getExpensePOJOArrayList();
                    double dayAmount = 0.0;
                    for (int j = 0; j < expensePOJOArrayList.size(); j++) {
                        FinanceEntryPOJO expensePOJO = expensePOJOArrayList.get(j);
                        dayAmount += expensePOJO.getAmount();
                    }

                    /**---------------------------------------------------------------------------*/
                    String dayOfWeek = null;
                    try {
                        dayOfWeek = new SimpleDateFormat("d").format(new SimpleDateFormat("dd/MM/yyyy").parse(dateExpenseHolder.getDate()));

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    entries.add(new BarEntry(Float.valueOf(dayOfWeek), Float.valueOf(String.valueOf(dayAmount))));
                }

            } else {
            }
        }

        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f);

        XAxis xAxis = mCategoryWiseBarChart.getXAxis();
        YAxis yAxis = mCategoryWiseBarChart.getAxisLeft();
        YAxis yAxis1 = mCategoryWiseBarChart.getAxisRight();

        data.setBarWidth(0.9f);// set custom bar width
        mCategoryWiseBarChart.getLegend().setEnabled(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        yAxis.setGranularityEnabled(true);
        yAxis.setGranularity(100f);
        yAxis.setDrawGridLines(true);


        yAxis.setStartAtZero(true);
        yAxis.setDrawAxisLine(false);
        yAxis1.setStartAtZero(true);
        yAxis1.setDrawAxisLine(false);
        yAxis1.setDrawLabels(false);
        yAxis1.setDrawGridLines(false);
        yAxis1.setDrawZeroLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        data.setValueTextSize(7);
        xAxis.setTextSize(11);
        yAxis.setTextSize(11);

        // if dates max is greater than 10 then use date, otherwise Calendar.DAY OF MONTH
        xAxis.setAxisMaximum((float) refCal.getActualMaximum(Calendar.DAY_OF_MONTH)); //TODO set to maximum date present
        xAxis.setAxisMinimum(0f);// set custom bar width

        Description description = new Description();
        description.setText("All Expenses in Month");

        mCategoryWiseBarChart.setDescription(description);
        mCategoryWiseBarChart.setScaleEnabled(false);
        mCategoryWiseBarChart.setDrawGridBackground(false);
        mCategoryWiseBarChart.setData(data);
        mCategoryWiseBarChart.setFitBars(true); // make the x-axis fit exactly all bars
        mCategoryWiseBarChart.invalidate(); // refresh


    }

    void reflectedBarChartDark() {

        List<BarEntry> entries = new ArrayList<>();
        Calendar refCal = new GregorianCalendar();
        refCal.set(Calendar.YEAR, currentYear);
        refCal.set(Calendar.MONTH, currentMonth);
        ArrayList<DateExpenseHolder> dateExpenseHolderArrayList;
        if (yearMonthDateHashMap.containsKey(currentYear)) {
            if (yearMonthDateHashMap.get(currentYear).containsKey(currentMonth)) {
                HashMap<String, ArrayList<FinanceEntryPOJO>> expensePOJOHashMapArrayList = yearMonthDateHashMap.get(currentYear).get(currentMonth);
                dateExpenseHolderArrayList = new ArrayList<>();
                for (Map.Entry<String, ArrayList<FinanceEntryPOJO>> entry : expensePOJOHashMapArrayList.entrySet()) {
                    dateExpenseHolderArrayList.add(new DateExpenseHolder(entry.getKey(), entry.getValue()));
                }

                for (int i = 0; i < dateExpenseHolderArrayList.size(); i++) {
                    DateExpenseHolder dateExpenseHolder = dateExpenseHolderArrayList.get(i);
                    ArrayList<FinanceEntryPOJO> expensePOJOArrayList = dateExpenseHolder.getExpensePOJOArrayList();
                    double dayAmount = 0.0;
                    for (int j = 0; j < expensePOJOArrayList.size(); j++) {
                        FinanceEntryPOJO expensePOJO = expensePOJOArrayList.get(j);
                        dayAmount += expensePOJO.getAmount();
                    }

                    /**---------------------------------------------------------------------------*/
                    String dayOfWeek = null;
                    try {
                        dayOfWeek = new SimpleDateFormat("d").format(new SimpleDateFormat("dd/MM/yyyy").parse(dateExpenseHolder.getDate()));

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    entries.add(new BarEntry(Float.valueOf(dayOfWeek), Float.valueOf(String.valueOf(dayAmount))));
                }

            } else {
            }
        }

        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f);

        XAxis xAxis = mCategoryWiseBarChart.getXAxis();
        YAxis yAxis = mCategoryWiseBarChart.getAxisLeft();
        YAxis yAxis1 = mCategoryWiseBarChart.getAxisRight();

        data.setBarWidth(0.9f);// set custom bar width
        mCategoryWiseBarChart.getLegend().setEnabled(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        yAxis.setGranularityEnabled(true);
        yAxis.setGranularity(100f);
        yAxis.setDrawGridLines(true);

        data.setValueTextColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setStartAtZero(true);
        yAxis1.setStartAtZero(true);
        yAxis1.setDrawAxisLine(false);
        yAxis1.setDrawLabels(false);
        yAxis.setDrawAxisLine(false);
        yAxis1.setDrawGridLines(false);
        yAxis1.setDrawZeroLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        data.setValueTextSize(7);
        xAxis.setTextSize(11);
        yAxis.setTextSize(11);

        // if dates max is greater than 10 then use date, otherwise Calendar.DAY OF MONTH
        xAxis.setAxisMaximum((float) refCal.getActualMaximum(Calendar.DAY_OF_MONTH)); //TODO set to maximum date present
        xAxis.setAxisMinimum(0f);// set custom bar width

        Description description = new Description();
        description.setText("All Expenses in Month");
        description.setTextColor(Color.WHITE);
        mCategoryWiseBarChart.setDescription(description);
        mCategoryWiseBarChart.setScaleEnabled(false);
        mCategoryWiseBarChart.setDrawGridBackground(false);
        mCategoryWiseBarChart.setData(data);
        mCategoryWiseBarChart.setFitBars(true); // make the x-axis fit exactly all bars
        mCategoryWiseBarChart.invalidate(); // refresh


    }

    /**
     * String weekDay;
     * ArrayList<FinanceEntryPOJO> expensePOJOArrayList;
     * double totalWeekDayExpense;
     * String date;
     * <p>
     * weekWiseExpenseHashMap.put(dayOfWeek, weekDayWiseExpensesArrayList);
     */
    void reflectWeekWiseBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labelsList = new ArrayList<>();
        labelsList.add("");
        labelsList.add("Mon");
        labelsList.add("Tue");
        labelsList.add("Wed");
        labelsList.add("Thu");
        labelsList.add("Fri");
        labelsList.add("Sat");
        labelsList.add("Sun");
        labelsList.add("");


        if (yearMonthDateHashMap.containsKey(currentYear)) {
            if (yearMonthDateHashMap.get(currentYear).containsKey(currentMonth)) {
                for (Map.Entry<String, ArrayList<WeekDayWiseExpense>> entry : weekWiseExpenseHashMap.entrySet()) {
                    ArrayList<WeekDayWiseExpense> weekDayWiseExpensesArrayList = entry.getValue();
                    double dayWiseTotalExpense = 0.00;
                    for (int i = 0; i < weekDayWiseExpensesArrayList.size(); i++) {
                        dayWiseTotalExpense += weekDayWiseExpensesArrayList.get(i).totalWeekDayExpense;
                    }
                    String dayOfWeek;
                    try {
                        dayOfWeek = new SimpleDateFormat("u").format(new SimpleDateFormat("EE").parse(entry.getKey()));
                        entries.add(new BarEntry(Float.valueOf(dayOfWeek), Float.valueOf(String.valueOf(dayWiseTotalExpense))));

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
            }
        }
        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width

        XAxis xAxis = mWeekDayWiseBarChart.getXAxis();
        YAxis yAxis = mWeekDayWiseBarChart.getAxisLeft();
        YAxis yAxis1 = mWeekDayWiseBarChart.getAxisRight();

        data.setBarWidth(0.9f);// set custom bar width
        mWeekDayWiseBarChart.getLegend().setEnabled(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        yAxis.setGranularityEnabled(true);
        yAxis.setGranularity(100f);
        yAxis.setStartAtZero(true);
        yAxis1.setStartAtZero(true);
        yAxis1.setDrawAxisLine(false);
        yAxis1.setDrawLabels(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawGridLines(true);
        yAxis1.setDrawGridLines(false);
        yAxis1.setDrawZeroLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        data.setValueTextSize(7);
        xAxis.setTextSize(11);
        yAxis.setTextSize(11);

        // if dates max is greater than 10 then use date, otherwise Calendar.DAY OF MONTH
        xAxis.setAxisMaximum(8f); //TODO set to maximum date present
        xAxis.setAxisMinimum(0f);

        Description description = new Description();
        description.setText("Weekday Wise Expenses (Month)");

        String[] labels = new String[labelsList.size()];
        labels = labelsList.toArray(labels);
        mWeekDayWiseBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));


        mWeekDayWiseBarChart.setDescription(description);
        mWeekDayWiseBarChart.setScaleEnabled(false);
        mWeekDayWiseBarChart.setDrawGridBackground(false);
        mWeekDayWiseBarChart.setData(data);
        mWeekDayWiseBarChart.setFitBars(true); // make the x-axis fit exactly all bars
        mWeekDayWiseBarChart.invalidate(); // refresh
    }

    void reflectWeekWiseBarChartDark() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labelsList = new ArrayList<>();
        labelsList.add("");
        labelsList.add("Mon");
        labelsList.add("Tue");
        labelsList.add("Wed");
        labelsList.add("Thu");
        labelsList.add("Fri");
        labelsList.add("Sat");
        labelsList.add("Sun");
        labelsList.add("");


        if (yearMonthDateHashMap.containsKey(currentYear)) {
            if (yearMonthDateHashMap.get(currentYear).containsKey(currentMonth)) {
                for (Map.Entry<String, ArrayList<WeekDayWiseExpense>> entry : weekWiseExpenseHashMap.entrySet()) {
                    ArrayList<WeekDayWiseExpense> weekDayWiseExpensesArrayList = entry.getValue();
                    double dayWiseTotalExpense = 0.00;
                    for (int i = 0; i < weekDayWiseExpensesArrayList.size(); i++) {
                        dayWiseTotalExpense += weekDayWiseExpensesArrayList.get(i).totalWeekDayExpense;
                    }
                    String dayOfWeek;
                    try {
                        dayOfWeek = new SimpleDateFormat("u").format(new SimpleDateFormat("EE").parse(entry.getKey()));
                        entries.add(new BarEntry(Float.valueOf(dayOfWeek), Float.valueOf(String.valueOf(dayWiseTotalExpense))));

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
            }
        }
        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width

        XAxis xAxis = mWeekDayWiseBarChart.getXAxis();
        YAxis yAxis = mWeekDayWiseBarChart.getAxisLeft();
        YAxis yAxis1 = mWeekDayWiseBarChart.getAxisRight();

        data.setBarWidth(0.9f);// set custom bar width
        data.setValueTextSize(7);
        xAxis.setTextSize(11);
        yAxis.setTextSize(11);
        mWeekDayWiseBarChart.getLegend().setEnabled(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        yAxis.setGranularityEnabled(true);
        yAxis.setGranularity(100f);
        yAxis.setStartAtZero(true);
        yAxis1.setStartAtZero(true);
        yAxis1.setDrawAxisLine(false);
        yAxis1.setDrawLabels(false);
        data.setValueTextColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        yAxis.setTextColor(Color.WHITE);

        yAxis.setDrawGridLines(true);
        yAxis1.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        yAxis1.setDrawZeroLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // if dates max is greater than 10 then use date, otherwise Calendar.DAY OF MONTH
        xAxis.setAxisMaximum(8f); //TODO set to maximum date present
        xAxis.setAxisMinimum(0f);

        Description description = new Description();
        description.setText("Weekday Wise Expenses (Month)");
        description.setTextColor(Color.WHITE);

        String[] labels = new String[labelsList.size()];
        labels = labelsList.toArray(labels);
        mWeekDayWiseBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        mWeekDayWiseBarChart.setDescription(description);
        mWeekDayWiseBarChart.setScaleEnabled(false);
        mWeekDayWiseBarChart.setDrawGridBackground(false);
        mWeekDayWiseBarChart.setData(data);
        mWeekDayWiseBarChart.setFitBars(true); // make the x-axis fit exactly all bars
        mWeekDayWiseBarChart.invalidate(); // refresh
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mExpenseValueEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseValueEventListener);
        }
        PersonalFinancialBoardFragment.mViewCurrentMonth = currentMonth;
        PersonalFinancialBoardFragment.mViewCurrentYear = currentYear;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExpenseValueEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mExpenseValueEventListener);
        }
        PersonalFinancialBoardFragment.mViewCurrentMonth = currentMonth;
        PersonalFinancialBoardFragment.mViewCurrentYear = currentYear;
    }

    class CategoryExpensePOJO {
        Categories category;
        double totalExpense;

        public CategoryExpensePOJO() {

        }

        public CategoryExpensePOJO(Categories category, double totalExpense) {
            this.category = category;
            this.totalExpense = totalExpense;
        }

        public Categories getCategory() {
            return category;
        }

        public void setCategory(Categories category) {
            this.category = category;
        }

        public double getTotalExpense() {
            return totalExpense;
        }

        public void setTotalExpense(double totalExpense) {
            this.totalExpense = totalExpense;
        }
    }

    void updateCategoryExpenses() {
        mCategoryExpenseArrayList = new ArrayList<>();
        double overallExpense = 0.0;
        for (int position = 0; position < categoriesArrayList.size(); position++) {
            double total = 0.0;
            Categories categories = categoriesArrayList.get(position);
            if (catYearMonthExpenseArrayListHashMap.containsKey(categories.getCategoryKey())) {
                if (catYearMonthExpenseArrayListHashMap.get(categories.getCategoryKey()).containsKey(currentYear)) {
                    if (catYearMonthExpenseArrayListHashMap.get(categories.getCategoryKey()).get(currentYear).containsKey(currentMonth)) {
                        expensePOJOArrayList = catYearMonthExpenseArrayListHashMap.get(categories.getCategoryKey()).get(currentYear).get(currentMonth);
                        for (int i = 0; i < expensePOJOArrayList.size(); i++) {
                            total += expensePOJOArrayList.get(i).getAmount();
                        }

                    }
                }
            }
            overallExpense += total;
            mCategoryExpenseArrayList.add(new CategoryExpensePOJO(categories, total));
        }
        reflectPieChartData(overallExpense);
        logExpenses();
        analyzeCategories();
    }

    void logExpenses() {
        Log.d("expense_log", "-------------------------------------------------------");
        Log.d("expense_log_size", mCategoryExpenseArrayList.size() + "");

        for (int i = 0; i < mCategoryExpenseArrayList.size(); i++) {
            Log.d("expenses_log", mCategoryExpenseArrayList.get(i).getCategory().getCategoryName() + ":" + mCategoryExpenseArrayList.get(i).getTotalExpense() + "----");
        }
        Log.d("expenses_log", "------------------------------------------------------");

    }

    class CategoricalAnalysis {
        boolean isAvailable;
        Categories maxCategory;
        double max;

        public CategoricalAnalysis() {
        }

        public CategoricalAnalysis(boolean isAvailable, Categories maxCategory, double max) {
            this.isAvailable = isAvailable;
            this.maxCategory = maxCategory;
            this.max = max;
        }

        public Categories getMaxCategory() {
            return maxCategory;
        }

        public void setMaxCategory(Categories maxCategory) {
            this.maxCategory = maxCategory;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }
    }

    void analyzeCategories() {
        CategoricalAnalysis categoricalAnalysis = new CategoricalAnalysis(false, new Categories(), 0.0);
        for (int i = 0; i < mCategoryExpenseArrayList.size(); i++) {
            double tempTotal = mCategoryExpenseArrayList.get(i).getTotalExpense();
            if (tempTotal >= categoricalAnalysis.getMax()) {
                categoricalAnalysis = new CategoricalAnalysis(true, mCategoryExpenseArrayList.get(i).getCategory(), mCategoryExpenseArrayList.get(i).getTotalExpense());
            }
        }

        mMaxExpenseCatName.setText(categoricalAnalysis.getMaxCategory().getCategoryName());
        mMaxExpenseCat.setText(categoricalAnalysis.getMax() + "");
        //TODO: show maximum expenditure category
    }

    void reflectPieChartData(double overallExpense) {
        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < mCategoryExpenseArrayList.size(); i++) {
            if (mCategoryExpenseArrayList.get(i).getTotalExpense() != 0.0) {
                double catPercentage = ((mCategoryExpenseArrayList.get(i).getTotalExpense()) / overallExpense) * 100;
                Log.d("qwerty", "reflectPieChartData: " + (float) catPercentage + " -- Category name :" + mCategoryExpenseArrayList.get(i).getCategory().getCategoryName());
                Log.d("qwerty", "--expense" + mCategoryExpenseArrayList.get(i).getTotalExpense());
                Log.d("qwerty", "-----------------------------------");

                entries.add(new PieEntry((float) (catPercentage), mCategoryExpenseArrayList.get(i).getCategory().getCategoryName()));
            }

        }
        PieDataSet set = new PieDataSet(entries, "Expense Breakdown");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(set);
        Legend legend = mPieChart.getLegend();
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        legend.setWordWrapEnabled(true);
        mPieChart.setData(data);
        mPieChart.setDrawEntryLabels(false);


        mPieChart.setUsePercentValues(true);
        Description description = mPieChart.getDescription();
        description.setText("Category wise expenses for the month");
        mPieChart.setDescription(description);
        mPieChart.setDrawHoleEnabled(true);
        // mPieChart.setHoleColor(get);
        mPieChart.setTransparentCircleRadius(10);
        mPieChart.setRotationEnabled(true);
        mPieChart.setRotationAngle(0);

        mPieChart.invalidate();

    }

    void toLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public Object getElementByIndex(LinkedHashMap map, int index) {
        return map.get((map.keySet().toArray())[index]);
    }
}