package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensewise.models.TransactionItem;
import com.example.expensewise.repositories.TransactionRepository;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private TextInputEditText etSearchTransactions;
    private ChipGroup chipGroupType;
    private Chip chipSort;
    private RecyclerView rvAllTransactions;
    private View layoutEmptyTransactions;
    private MaterialButton btnAddTransactionHeader, btnAddTransactionEmpty;

    private SessionManager sessionManager;
    private TransactionRepository transactionRepository;
    private TransactionAdapter transactionAdapter;

    private long currentUserId = -1;
    private final List<TransactionItem> allTransactions = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentTypeFilter = "ALL"; // ALL, INCOME, EXPENSE
    private int currentSortOption = SORT_NEWEST_FIRST;

    private static final int SORT_NEWEST_FIRST = 0;
    private static final int SORT_OLDEST_FIRST = 1;
    private static final int SORT_HIGHEST_AMOUNT = 2;
    private static final int SORT_LOWEST_AMOUNT = 3;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) return;

        sessionManager = new SessionManager(getContext());
        transactionRepository = new TransactionRepository(getContext());

        currentUserId = sessionManager.getCurrentUserId();

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeTransactions();
    }

    private void initViews(View view) {
        etSearchTransactions = view.findViewById(R.id.etSearchTransactions);
        chipGroupType = view.findViewById(R.id.chipGroupType);
        chipSort = view.findViewById(R.id.chipSort);

        rvAllTransactions = view.findViewById(R.id.rvAllTransactions);
        layoutEmptyTransactions = view.findViewById(R.id.layoutEmptyTransactions);
        btnAddTransactionHeader = view.findViewById(R.id.btnAddTransactionHeader);
        btnAddTransactionEmpty = view.findViewById(R.id.btnAddTransactionEmpty);
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(getContext(), this);
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAllTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        View.OnClickListener openAddListener = v -> {
            Intent intent = new Intent(getContext(), AddTransactionActivity.class);
            startActivity(intent);
        };
        btnAddTransactionHeader.setOnClickListener(openAddListener);
        btnAddTransactionEmpty.setOnClickListener(openAddListener);

        etSearchTransactions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim() : "";
                applyFiltersAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        chipGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipFilterIncome) {
                currentTypeFilter = "INCOME";
            } else if (checkedId == R.id.chipFilterExpense) {
                currentTypeFilter = "EXPENSE";
            } else {
                currentTypeFilter = "ALL";
            }
            applyFiltersAndSort();
        });

        chipSort.setOnClickListener(this::showSortPopupMenu);
    }

    private void showSortPopupMenu(View anchorView) {
        if (getContext() == null) return;

        PopupMenu popupMenu = new PopupMenu(getContext(), anchorView);
        popupMenu.getMenu().add(0, SORT_NEWEST_FIRST, 0, "Newest First");
        popupMenu.getMenu().add(0, SORT_OLDEST_FIRST, 1, "Oldest First");
        popupMenu.getMenu().add(0, SORT_HIGHEST_AMOUNT, 2, "Highest Amount");
        popupMenu.getMenu().add(0, SORT_LOWEST_AMOUNT, 3, "Lowest Amount");

        popupMenu.setOnMenuItemClickListener(item -> {
            currentSortOption = item.getItemId();
            switch (currentSortOption) {
                case SORT_NEWEST_FIRST:
                    chipSort.setText("Sort: Newest");
                    break;
                case SORT_OLDEST_FIRST:
                    chipSort.setText("Sort: Oldest");
                    break;
                case SORT_HIGHEST_AMOUNT:
                    chipSort.setText("Sort: High-Low");
                    break;
                case SORT_LOWEST_AMOUNT:
                    chipSort.setText("Sort: Low-High");
                    break;
            }
            applyFiltersAndSort();
            return true;
        });

        popupMenu.show();
    }

    private void observeTransactions() {
        if (currentUserId == -1) return;

        transactionRepository.getTransactionsForUser(currentUserId).observe(getViewLifecycleOwner(), list -> {
            allTransactions.clear();
            if (list != null) {
                allTransactions.addAll(list);
            }
            applyFiltersAndSort();
        });
    }

    private void applyFiltersAndSort() {
        List<TransactionItem> filteredList = new ArrayList<>();

        for (TransactionItem item : allTransactions) {
            // Type filter
            if (!"ALL".equalsIgnoreCase(currentTypeFilter)) {
                if (!currentTypeFilter.equalsIgnoreCase(item.getType())) {
                    continue;
                }
            }

            // Search query filter (matches category, description, or payment method)
            if (!TextUtils.isEmpty(currentSearchQuery)) {
                String query = currentSearchQuery.toLowerCase();
                boolean matchesCat = item.getCategory() != null && item.getCategory().toLowerCase().contains(query);
                boolean matchesDesc = item.getDescription() != null && item.getDescription().toLowerCase().contains(query);
                boolean matchesPM = item.getPaymentMethod() != null && item.getPaymentMethod().toLowerCase().contains(query);

                if (!matchesCat && !matchesDesc && !matchesPM) {
                    continue;
                }
            }

            filteredList.add(item);
        }

        // Sorting
        filteredList.sort((t1, t2) -> {
            switch (currentSortOption) {
                case SORT_OLDEST_FIRST:
                    return Long.compare(t1.getCreatedAt(), t2.getCreatedAt());
                case SORT_HIGHEST_AMOUNT:
                    return Double.compare(t2.getAmount(), t1.getAmount());
                case SORT_LOWEST_AMOUNT:
                    return Double.compare(t1.getAmount(), t2.getAmount());
                case SORT_NEWEST_FIRST:
                default:
                    return Long.compare(t2.getCreatedAt(), t1.getCreatedAt());
            }
        });

        if (filteredList.isEmpty()) {
            rvAllTransactions.setVisibility(View.GONE);
            layoutEmptyTransactions.setVisibility(View.VISIBLE);
        } else {
            rvAllTransactions.setVisibility(View.VISIBLE);
            layoutEmptyTransactions.setVisibility(View.GONE);
        }

        transactionAdapter.setTransactions(filteredList);
    }

    @Override
    public void onTransactionClick(TransactionItem transaction) {
        Intent intent = new Intent(getContext(), TransactionDetailsActivity.class);
        intent.putExtra("EXTRA_TRANSACTION_ID", transaction.getId());
        startActivity(intent);
    }

    @Override
    public void onTransactionLongClick(TransactionItem transaction) {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    transactionRepository.delete(transaction);
                    Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
