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
import continmattia.notefirebase.db.NoteHelper;

public class NotesCursorAdapter extends CursorAdapter {

    public interface OnNoteInteractionListener {
        void onNoteClicked(String id);
        void onNoteDelete(String id);
    }

    private OnNoteInteractionListener listener;

    public NotesCursorAdapter(Context context, Cursor c) {
        super(context, c, false);
        if (context instanceof OnNoteInteractionListener) {
            listener = (OnNoteInteractionListener) context;
        }
    }

    private class ViewHolder {
        TextView mTitle;
        TextView mExcerpt;
        ImageView mDelete;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.card_note, null);

        ViewHolder holder = new ViewHolder();
        holder.mTitle = (TextView) view.findViewById(R.id.note_title_tv);
        holder.mExcerpt = (TextView) view.findViewById(R.id.note_excerpt_tv);
        holder.mDelete = (ImageView) view.findViewById(R.id.delete_icon);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndex(NoteHelper.COL_TITLE));
        holder.mTitle.setText(title);

        String content = cursor.getString(cursor.getColumnIndex(NoteHelper.COL_CONTENT));
        if (content != null) {
            String excerpt;
            if (content.length() > 60) {
                excerpt = content.substring(0, 59) + "...";
            } else {
                excerpt = content;
            }
            holder.mExcerpt.setText(excerpt);
        } else {
            holder.mExcerpt.setText("");
        }

        final String id = cursor.getString(cursor.getColumnIndex(NoteHelper.COL_CID));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNoteClicked(id);
            }
        });
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNoteDelete(id);
            }
        });
    }
}
