package com.capstone.autism_training.deck;

import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.autism_training.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class DeckActivity extends AppCompatActivity {

    protected RecyclerView mRecyclerView;
    protected DeckAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    private SelectionTracker<Long> selectionTracker;
    private DeckInfoTableManager deckInfoTableManager;
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        ExtendedFloatingActionButton extendedFAB = findViewById(R.id.extendedFAB);
        extendedFAB.setOnClickListener(view -> {
            AddDeckDialogFragment addDeckDialogFragment = new AddDeckDialogFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            addDeckDialogFragment.show(transaction, AddDeckDialogFragment.TAG);
        });

        mRecyclerView = findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new DeckAdapter();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        selectionTracker = new SelectionTracker.Builder<>(
                "selectionId",
                mRecyclerView,
                new DeckItemKeyProvider(mRecyclerView),
                new DeckDetailsLookup(mRecyclerView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
                .build();

        ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_deck, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_delete) {
                    new MaterialAlertDialogBuilder(DeckActivity.this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                            .setIcon(R.drawable.ic_round_delete_24)
                            .setTitle("Delete deck?")
                            .setMessage("The selected deck and the flash cards inside it will be deleted permanently.")
                            .setPositiveButton("Delete", (dialogInterface, i) -> {
                                if (selectionTracker.hasSelection()) {
                                    long id = selectionTracker.getSelection().iterator().next();
                                    selectionTracker.clearSelection();
                                    deckInfoTableManager.deleteRow(id);
                                    mAdapter.removeItem(mRecyclerView.findViewHolderForItemId(id).getAdapterPosition());
                                    Toast.makeText(DeckActivity.this, "Deleted the deck", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                            .setOnDismissListener(dialogInterface -> mode.finish())
                            .show();
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                selectionTracker.clearSelection();
            }
        };

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (!selectionTracker.getSelection().isEmpty() && actionMode == null) {
                    actionMode = toolbar.startActionMode(actionModeCallback);
                }
                else if (selectionTracker.getSelection().isEmpty() && actionMode != null) {
                    actionMode.finish();
                }
            }
        });
        mAdapter.setSelectionTracker(selectionTracker);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        deckInfoTableManager = new DeckInfoTableManager(getApplicationContext());
        deckInfoTableManager.open();
        Cursor cursor = deckInfoTableManager.fetch();

        int idIndex = cursor.getColumnIndex(DeckInfoTableHelper.ID);
        int imageIndex = cursor.getColumnIndex(DeckInfoTableHelper.IMAGE);
        int nameIndex = cursor.getColumnIndex(DeckInfoTableHelper.NAME);
        int descriptionIndex = cursor.getColumnIndex(DeckInfoTableHelper.DESCRIPTION);
        while (!cursor.isAfterLast() || cursor.isFirst()) {
            DeckModel deckModel = new DeckModel(cursor.getInt(idIndex), cursor.getBlob(imageIndex), cursor.getString(nameIndex), cursor.getString(descriptionIndex));
            mAdapter.addItem(deckModel);
            cursor.moveToNext();
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deckInfoTableManager.close();
    }
}