package continmattia.notefirebase.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import continmattia.notefirebase.R;

public class AddCategoryDialogFragment extends DialogFragment {

    private static AddCategoryDialogFragment instance = null;

    public interface OnCategoryCreateListener {
        void onCategoryCreated(String name);
    }

    public static AddCategoryDialogFragment getInstance() {
        if (instance == null)
            instance = new AddCategoryDialogFragment();
        return instance;
    }

    private OnCategoryCreateListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCategoryCreateListener) {
            listener = (OnCategoryCreateListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = (inflater.inflate(R.layout.new_category_dialog, null));
        final EditText categoryNameEt = (EditText) view.findViewById(R.id.category_name_et);

        builder.setView(view)
                .setTitle("Insert a name for the category")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String categoryName = categoryNameEt.getText().toString();
                        if (!categoryName.isEmpty()) {
                            listener.onCategoryCreated(categoryNameEt.getText().toString());
                        } else {
                            Toast.makeText(getContext(), "Set a name for the category!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder.create();
    }

}
