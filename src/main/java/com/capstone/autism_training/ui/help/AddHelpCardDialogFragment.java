package com.capstone.autism_training.ui.help;

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
import androidx.fragment.app.DialogFragment;

import com.capstone.autism_training.R;
import com.capstone.autism_training.databinding.FragmentAddHelpCardBinding;
import com.capstone.autism_training.help.HelpCardModel;
import com.capstone.autism_training.utilities.ImageHelper;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;

public class AddHelpCardDialogFragment extends DialogFragment {

    public static final String TAG = AddHelpCardDialogFragment.class.getSimpleName();

    public HelpFragment helpFragment;
    private ActivityResultLauncher<String> mGetContent;
    private byte[] image = null;

    private FragmentAddHelpCardBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_App);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddHelpCardBinding.inflate(inflater, container, false);

        helpFragment = (HelpFragment) getParentFragment();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(view1 -> this.dismiss());

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

        binding.addHelpCardButton.setOnClickListener(view1 -> {
            EditText nameEditText = binding.nameEditText;

            if (image != null && !nameEditText.getText().toString().isEmpty()) {
                long rowNumber = helpFragment.helpCardTableManager.insert(nameEditText.getText().toString(), image);
                if (rowNumber != -1) {
                    HelpCardModel helpCardModel = new HelpCardModel(rowNumber, nameEditText.getText().toString(), image);
                    helpFragment.mAdapter.addItem(helpCardModel);
                    if (getParentFragment() != null && getParentFragment().getView() != null) {
                        Snackbar.make(getParentFragment().getView(), "Successfully added the help card", Snackbar.LENGTH_LONG)
                                .setAction("OKAY", view2 -> {}).show();
                    }
                    this.dismiss();
                }
                else {
                    Snackbar.make(view, "Error occurred while adding the help card", Snackbar.LENGTH_LONG)
                            .setAction("OKAY", view2 -> {}).show();
                }
            }
            else {
                Snackbar.make(view, "All fields are necessary", Snackbar.LENGTH_LONG)
                        .setAction("OKAY", view2 -> {}).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
