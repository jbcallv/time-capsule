package com.example.timecapsule;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;


public class AddMediaDialogFragment extends DialogFragment {

    private ImageButton cameraImageButton, galleryImageButton, audioImageButton;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onTakePictureClick(DialogFragment dialog);
        public void onChoosePictureClick(DialogFragment dialog);
        public void onTakeVideoClick(DialogFragment dialog);
        public void onChooseVideoClick(DialogFragment dialog);
        public void onUploadAudioClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_fragment_add_media, null);
        builder.setView(view);

        cameraImageButton = (ImageButton) view.findViewById(R.id.activity_add_media_ibtn_camera);
        galleryImageButton = (ImageButton) view.findViewById(R.id.activity_add_media_ibtn_gallery);
        audioImageButton = (ImageButton) view.findViewById(R.id.activity_add_media_ibtn_audio);

        final CharSequence[] cameraOptions = {"Take a Photo", "Record a Video"};
        final CharSequence[] galleryOptions = {"Choose a Photo", "Choose a Video"};

        AlertDialog.Builder cameraBuilder = new AlertDialog.Builder(getContext());
        AlertDialog.Builder galleryBuilder = new AlertDialog.Builder(getContext());

        cameraBuilder.setItems(cameraOptions, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (cameraOptions[item].equals("Take a Photo")) {
                    listener.onTakePictureClick(AddMediaDialogFragment.this);

                } else if (cameraOptions[item].equals("Record a Video")) {
                    listener.onTakeVideoClick(AddMediaDialogFragment.this);
                }
            }
        });

        galleryBuilder.setItems(galleryOptions, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (galleryOptions[item].equals("Choose a Photo")) {
                    listener.onChoosePictureClick(AddMediaDialogFragment.this);

                } else if (galleryOptions[item].equals("Choose a Video")) {
                    listener.onChooseVideoClick(AddMediaDialogFragment.this);
                }
            }
        });

        cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraBuilder.show();
            }
        });

        galleryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryBuilder.show();
            }
        });

        audioImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onUploadAudioClick(AddMediaDialogFragment.this);
            }
        });

        return builder.create();


    }
}
