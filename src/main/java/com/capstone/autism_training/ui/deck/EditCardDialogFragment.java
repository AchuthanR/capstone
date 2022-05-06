package com.capstone.autism_training.ui.deck;

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
import com.capstone.autism_training.card.CardModel;
import com.capstone.autism_training.databinding.FragmentEditCardBinding;
import com.capstone.autism_training.utilities.ImageHelper;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;

public class EditCardDialogFragment extends DialogFragment {

    public static final String TAG = EditCardDialogFragment.class.getSimpleName();

    private ActivityResultLauncher<String> mGetContent;
    private CardFragment cardFragment;
    private CardModel cardModel;
    private byte[] image = null;
    private int adapterPosition = -1;

    private FragmentEditCardBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_App);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditCardBinding.inflate(inflater, container, false);

        cardFragment = (CardFragment) getParentFragment();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(view1 -> this.dismiss());

        binding.imageView.setImageBitmap(ImageHelper.toCompressedBitmap(cardModel.image));
        binding.captionEditText.setText(cardModel.caption);
        binding.shortAnswerEditText.setText(cardModel.short_answer);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    try {
                        if (getContext() != null && uri != null) {
                            image = ImageHelper.getBitmapAsByteArray(BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(uri)));
                            binding.imageView.setImageBitmap(ImageHelper.toCompressedBitmap(image));
                        }
                    } catch (FileNotFoundException e) {
                        Snackbar.make(view, "Image not found", Snackbar.LENGTH_LONG)
                                .setAction("OKAY", view1 -> {}).show();
                        e.printStackTrace();
                    }
                });

        binding.selectImageButton.setOnClickListener(view1 -> mGetContent.launch("image/*"));

        binding.editCardButton.setOnClickListener(view1 -> {
            EditText captionEditText = binding.captionEditText;
            EditText shortAnswerEditText = binding.shortAnswerEditText;

            if (image != null && !captionEditText.getText().toString().isEmpty() && !shortAnswerEditText.getText().toString().isEmpty()) {
                long rowsAffected = cardFragment.deckTableManager.update(cardModel.id, image, captionEditText.getText().toString(), shortAnswerEditText.getText().toString());
                if (rowsAffected > 0) {
                    CardModel newCardModel = new CardModel(cardModel.id, image, captionEditText.getText().toString(), shortAnswerEditText.getText().toString());
                    cardFragment.mAdapter.changeItem(adapterPosition, newCardModel);
                    if (getParentFragment() != null && getParentFragment().getView() != null) {
                        Snackbar.make(getParentFragment().getView(), "Successfully edited the card", Snackbar.LENGTH_LONG)
                                .setAction("OKAY", view2 -> {}).show();
                    }
                    this.dismiss();
                }
                else {
                    Snackbar.make(view, "Error occurred while editing the card", Snackbar.LENGTH_LONG)
                            .setAction("OKAY", view2 -> {}).show();
                }
            }
            else {
                Snackbar.make(view, "All fields are necessary", Snackbar.LENGTH_LONG)
                        .setAction("OKAY", view2 -> {}).show();
            }
        });
    }

    public void setCardModel(CardModel cardModel) {
        this.cardModel = cardModel;
        this.image = cardModel.image;
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
