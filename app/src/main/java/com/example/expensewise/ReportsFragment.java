package com.example.expensewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.expensewise.models.TransactionItem;
import com.example.expensewise.repositories.BudgetRepository;
import com.example.expensewise.repositories.TransactionRepository;
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private MaterialButtonToggleGroup toggleGroupTimePeriod;
    private TextView tvHealthBadge, tvSavingsRate, tvSavingsAmount, tvFinancialSummary;
    private TextView tvInsightHighestCat, tvInsightDailyAvg, tvInsightBudgetUtil;
    private PieChart pieChartExpenses;
    private BarChart barChartIncomeExpense;
    private LineChart lineChartTrends;

    private SessionManager sessionManager;
    private TransactionRepository transactionRepository;
    private BudgetRepository budgetRepository;

    private long currentUserId = -1;
    private String currencySymbol = "₹";
    private String selectedPeriod = "MONTHLY"; // WEEKLY, MONTHLY, YEARLY

    public ReportsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) return;

        sessionManager = new SessionManager(getContext());
        transactionRepository = new TransactionRepository(getContext());
        budgetRepository = new BudgetRepository(getContext());

        currentUserId = sessionManager.getCurrentUserId();
        currencySymbol = sessionManager.getCurrencySymbol();

        initViews(view);
        setupListeners();
        loadReportsData();
    }

    private void initViews(View view) {
        toggleGroupTimePeriod = view.findViewById(R.id.toggleGroupTimePeriod);
        tvHealthBadge = view.findViewById(R.id.tvHealthBadge);
        tvSavingsRate = view.findViewById(R.id.tvSavingsRate);
        tvSavingsAmount = view.findViewById(R.id.tvSavingsAmount);
        tvFinancialSummary = view.findViewById(R.id.tvFinancialSummary);

        tvInsightHighestCat = view.findViewById(R.id.tvInsightHighestCat);
        tvInsightDailyAvg = view.findViewById(R.id.tvInsightDailyAvg);
        tvInsightBudgetUtil = view.findViewById(R.id.tvInsightBudgetUtil);

        pieChartExpenses = view.findViewById(R.id.pieChartExpenses);
        barChartIncomeExpense = view.findViewById(R.id.barChartIncomeExpense);
        lineChartTrends = view.findViewById(R.id.lineChartTrends);

        setupChartDefaults();
    }

    private void setupChartDefaults() {
        // Pie Chart setup
        pieChartExpenses.getDescription().setEnabled(false);
        pieChartExpenses.setUsePercentValues(true);
        pieChartExpenses.setDrawHoleEnabled(true);
        pieChartExpenses.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChartExpenses.setTransparentCircleRadius(61f);
        pieChartExpenses.setDrawCenterText(true);
        pieChartExpenses.setCenterText("Expenses");
        pieChartExpenses.setCenterTextSize(14f);
        pieChartExpenses.setRotationEnabled(true);
        pieChartExpenses.setHighlightPerTapEnabled(true);
        pieChartExpenses.getLegend().setEnabled(true);

        // Bar Chart setup
        barChartIncomeExpense.getDescription().setEnabled(false);
        barChartIncomeExpense.setDrawGridBackground(false);
        barChartIncomeExpense.getAxisRight().setEnabled(false);
        barChartIncomeExpense.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartIncomeExpense.getXAxis().setGranularity(1f);

        // Line Chart setup
        lineChartTrends.getDescription().setEnabled(false);
        lineChartTrends.setDrawGridBackground(false);
        lineChartTrends.getAxisRight().setEnabled(false);
        lineChartTrends.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartTrends.getXAxis().setGranularity(1f);
    }

    private void setupListeners() {
        toggleGroupTimePeriod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnPeriodWeekly) {
                selectedPeriod = "WEEKLY";
            } else if (checkedId == R.id.btnPeriodMonthly) {
                selectedPeriod = "MONTHLY";
            } else if (checkedId == R.id.btnPeriodYearly) {
                selectedPeriod = "YEARLY";
            }
            loadReportsData();
        });
    }

    private void loadReportsData() {
        if (currentUserId == -1 || getContext() == null) return;

        transactionRepository.getTransactionsForUser(currentUserId).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null) {
                transactions = new ArrayList<>();
            }

            processFinancialHealthAndInsights(transactions);
            setupPieChart(transactions);
            setupBarChart(transactions);
            setupLineChart(transactions);
        });
    }

    private void processFinancialHealthAndInsights(List<TransactionItem> transactions) {
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        int currentYear = cal.get(Calendar.YEAR);
        String monthPrefix = String.format(Locale.US, "%04d-%02d", currentYear, currentMonth);

        double totalIncome = 0;
        double totalExpense = 0;
        Map<String, Double> categoryExpenseMap = new HashMap<>();

        for (TransactionItem item : transactions) {
            if (item.getDate() == null) continue;
            boolean matchesPeriod = false;

            if ("WEEKLY".equals(selectedPeriod)) {
                // Approximate past 7 days or current week
                matchesPeriod = isWithinCurrentWeek(item.getDate());
            } else if ("MONTHLY".equals(selectedPeriod)) {
                matchesPeriod = item.getDate().startsWith(monthPrefix);
            } else if ("YEARLY".equals(selectedPeriod)) {
                matchesPeriod = item.getDate().startsWith(String.valueOf(currentYear));
            }

            if (matchesPeriod) {
                if ("INCOME".equalsIgnoreCase(item.getType())) {
                    totalIncome += item.getAmount();
                } else if ("EXPENSE".equalsIgnoreCase(item.getType())) {
                    totalExpense += item.getAmount();
                    String cat = item.getCategory() != null ? item.getCategory() : "Other";
                    double prev = categoryExpenseMap.getOrDefault(cat, 0.0);
                    categoryExpenseMap.put(cat, prev + item.getAmount());
                }
            }
        }

        double savingsAmount = totalIncome - totalExpense;
        double savingsRate = totalIncome > 0 ? (savingsAmount / totalIncome) * 100.0 : 0.0;

        tvSavingsAmount.setText(FinancialUtils.formatCurrency(savingsAmount, currencySymbol));
        tvSavingsRate.setText(String.format(Locale.US, "%.1f%%", savingsRate));

        if (savingsRate >= 20) {
            tvHealthBadge.setText("Excellent");
            tvHealthBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
            tvFinancialSummary.setText("Great job! You are saving a healthy percentage of your income.");
        } else if (savingsRate >= 0) {
            tvHealthBadge.setText("Good");
            tvHealthBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            tvFinancialSummary.setText("Your savings rate is positive. Try to increase savings where possible.");
        } else {
            tvHealthBadge.setText("Attention");
            tvHealthBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
            tvFinancialSummary.setText("Warning: Expenses exceed income for this period. Review spending.");
        }

        // Insights
        String highestCat = "None";
        double maxCatAmt = 0;
        for (Map.Entry<String, Double> entry : categoryExpenseMap.entrySet()) {
            if (entry.getValue() > maxCatAmt) {
                maxCatAmt = entry.getValue();
                highestCat = entry.getKey();
            }
        }
        tvInsightHighestCat.setText("Highest spending: " + highestCat + " (" + FinancialUtils.formatCurrency(maxCatAmt, currencySymbol) + ")");

        double dailyAvg = "WEEKLY".equals(selectedPeriod) ? (totalExpense / 7.0) : ("MONTHLY".equals(selectedPeriod) ? (totalExpense / 30.0) : (totalExpense / 365.0));
        tvInsightDailyAvg.setText("Average daily spending: " + FinancialUtils.formatCurrency(dailyAvg, currencySymbol));
        tvInsightBudgetUtil.setText("Total period expenses: " + FinancialUtils.formatCurrency(totalExpense, currencySymbol));
    }

    private boolean isWithinCurrentWeek(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            if (date == null) return false;
            long diff = new Date().getTime() - date.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            return days >= 0 && days <= 7;
        } catch (Exception e) {
            return false;
        }
    }

    private void setupPieChart(List<TransactionItem> transactions) {
        Map<String, Float> categoryMap = new HashMap<>();
        String monthPrefix = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        for (TransactionItem item : transactions) {
            if ("EXPENSE".equalsIgnoreCase(item.getType())) {
                if ("MONTHLY".equals(selectedPeriod) && item.getDate() != null && !item.getDate().startsWith(monthPrefix)) {
                    continue;
                }
                String cat = item.getCategory() != null ? item.getCategory() : "Other";
                float amt = (float) item.getAmount();
                categoryMap.put(cat, categoryMap.getOrDefault(cat, 0f) + amt);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            pieChartExpenses.clear();
            pieChartExpenses.setNoDataText("No expense data available for this period.");
            pieChartExpenses.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChartExpenses.setData(data);
        pieChartExpenses.invalidate();
    }

    private void setupBarChart(List<TransactionItem> transactions) {
        // Income vs Expense for last 3 months or weeks
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, new float[]{50000f, 32000f})); // Dummy or calculated grouped bars
        // Let's compute actual totals
        double income = 0;
        double expense = 0;
        for (TransactionItem item : transactions) {
            if ("INCOME".equalsIgnoreCase(item.getType())) income += item.getAmount();
            else if ("EXPENSE".equalsIgnoreCase(item.getType())) expense += item.getAmount();
        }

        List<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1f, (float) income));
        barEntries.add(new BarEntry(2f, (float) expense));

        BarDataSet dataSet = new BarDataSet(barEntries, "Amount (" + currencySymbol + ")");
        dataSet.setColors(new int[]{ContextCompat.getColor(requireContext(), R.color.secondary), ContextCompat.getColor(requireContext(), R.color.error)});

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChartIncomeExpense.setData(data);
        XAxis xAxis = barChartIncomeExpense.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"", "Total Income", "Total Expenses"}));
        barChartIncomeExpense.invalidate();
    }

    private void setupLineChart(List<TransactionItem> transactions) {
        List<Entry> entries = new ArrayList<>();
        int index = 0;
        // Sort transactions by date and map cumulative or daily amounts
        Collections.sort(transactions, (a, b) -> {
            if (a.getDate() == null || b.getDate() == null) return 0;
            return a.getDate().compareTo(b.getDate());
        });

        double runningTotal = 0;
        for (TransactionItem item : transactions) {
            if ("EXPENSE".equalsIgnoreCase(item.getType())) {
                runningTotal += item.getAmount();
                entries.add(new Entry(index++, (float) runningTotal));
            }
        }

        if (entries.isEmpty()) {
            lineChartTrends.clear();
            lineChartTrends.setNoDataText("No trend data available.");
            lineChartTrends.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Cumulative Spending");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary_dark));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.primary_light));

        LineData data = new LineData(dataSet);
        lineChartTrends.setData(data);
        lineChartTrends.invalidate();
    }
}
