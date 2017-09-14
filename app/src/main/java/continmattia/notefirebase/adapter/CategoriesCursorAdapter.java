package continmattia.notefirebase.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import continmattia.notefirebase.R;
import continmattia.notefirebase.db.CategoryHelper;

public class CategoriesCursorAdapter extends CursorAdapter {

    public interface OnCategoryInteractionListener {
        void onCategoryClicked(String id);
        void onCategoryDelete(String id);
    }

    private OnCategoryInteractionListener listener;

    public CategoriesCursorAdapter(Context context, Cursor c) {
        super(context, c, false);
        if (context instanceof OnCategoryInteractionListener) {
            listener = (OnCategoryInteractionListener) context;
        }
    }

    private class ViewHolder {
        TextView mTitle;
        ImageView mDelete;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.card_note, null);

        ViewHolder holder = new ViewHolder();
        holder.mTitle = (TextView) view.findViewById(R.id.note_title_tv);
        holder.mDelete = (ImageView) view.findViewById(R.id.delete_icon);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.mTitle.setText(cursor.getString(cursor.getColumnIndex(CategoryHelper.COL_CATEGORY_NAME)));

        final String id = cursor.getString(cursor.getColumnIndex(CategoryHelper.COL_CID));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCategoryClicked(id);
            }
        });
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCategoryDelete(id);
            }
        });
    }
}
