package com.capstone.autism_training.ui.deck;

import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.capstone.autism_training.R;
import com.capstone.autism_training.databinding.DialogFragmentEditDeckBinding;
import com.capstone.autism_training.deck.DeckModel;
import com.capstone.autism_training.utilities.ImageHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;

public class EditDeckDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = EditDeckDialogFragment.class.getSimpleName();

    private ActivityResultLauncher<String> mGetContent;
    private DeckFragment deckFragment;
    private DeckModel deckModel;
    private byte[] image = null;
    private int adapterPosition = -1;

    private DialogFragmentEditDeckBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.Theme_App_BottomSheet_Modal);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialogFragmentEditDeckBinding.inflate(inflater, container, false);

        deckFragment = (DeckFragment) getParentFragment();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = com.google.android.material.R.style.Animation_Design_BottomSheetDialog;
        }

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }

        binding.imageView.setImageBitmap(ImageHelper.toCompressedBitmap(deckModel.image));
        binding.nameEditText.setText(deckModel.name);
        binding.descriptionEditText.setText(deckModel.description);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    try {
                        if (getContext() != null && uri != null) {
                            image = ImageHelper.getBitmapAsByteArray(BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(uri)));
                            binding.imageView.setImageBitmap(ImageHelper.toCompressedBitmap(image));
                        }
                    } catch (FileNotFoundException e) {
                        Snackbar.make(view, "Image not found!", Snackbar.LENGTH_LONG)
                                .setAction("OKAY", view1 -> {}).show();
                        e.printStackTrace();
                    }
                });

        binding.selectImageButton.setOnClickListener(view1 -> mGetContent.launch("image/*"));

        binding.editDeckButton.setOnClickListener(view1 -> {
            EditText nameEditText = binding.nameEditText;
            EditText descriptionEditText = binding.descriptionEditText;

            if (image != null && !nameEditText.getText().toString().isEmpty() && !descriptionEditText.getText().toString().isEmpty()) {
                long rowsAffected = deckFragment.deckInfoTableManager.update(deckModel.id, deckModel.name, nameEditText.getText().toString(), image, descriptionEditText.getText().toString());
                if (rowsAffected > 0) {
                    DeckModel newDeckModel = new DeckModel(deckModel.id, image, nameEditText.getText().toString(), descriptionEditText.getText().toString());
                    deckFragment.mAdapter.changeItem(adapterPosition, newDeckModel);
                    if (getParentFragment() != null && getParentFragment().getView() != null) {
                        Snackbar.make(getParentFragment().getView(), "Successfully edited the deck", Snackbar.LENGTH_LONG)
                                .setAction("OKAY", view2 -> {}).show();
                    }
                    this.dismiss();
                }
                else {
                    Snackbar.make(view, "Error occurred while editing the deck", Snackbar.LENGTH_LONG)
                            .setAction("OKAY", view2 -> {}).show();
                }
            }
            else {
                Snackbar.make(view, "All fields are necessary", Snackbar.LENGTH_LONG)
                        .setAction("OKAY", view2 -> {}).show();
            }
        });
    }

    public void setDeckModel(DeckModel deckModel) {
        this.deckModel = deckModel;
        this.image = deckModel.image;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
