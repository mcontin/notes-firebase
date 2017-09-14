package continmattia.notefirebase.dialog;

import android.accounts.OnAccountsUpdateListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import continmattia.notefirebase.R;

public class AudioDialogFragment extends DialogFragment {

    private static final String LOG_TAG = "AudioRecord";
    private static String mFileName = null;

    @BindView(R.id.record_audio_btn)
    Button mRecordButton;
    private MediaRecorder mRecorder = null;

    @BindView(R.id.play_audio_btn)
    Button mPlayButton;
    private MediaPlayer mPlayer = null;

    public static AudioDialogFragment newInstance() {
        return new AudioDialogFragment();
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    boolean mStartRecording = true;

    @OnClick(R.id.record_audio_btn)
    void record() {
        onRecord(mStartRecording);
        if (mStartRecording) {
            mRecordButton.setText("Stop recording");
        } else {
            mRecordButton.setText("Start recording");
        }
        mStartRecording = !mStartRecording;
    }

    boolean mStartPlaying = true;

    @OnClick(R.id.play_audio_btn)
    void play() {
        onPlay(mStartPlaying);
        if (mStartPlaying) {
            mPlayButton.setText("Stop playing");
        } else {
            mPlayButton.setText("Start playing");
        }
        mStartPlaying = !mStartPlaying;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAudioCreatedListener) {
            listener = (OnAudioCreatedListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_record_audio, null);
        ButterKnife.bind(this, view);

        builder.setView(view)
                .setTitle("Record your audio")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onAudioCreate(mFileName);
                    }
                });

        return builder.create();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mFileName = getContext().getFilesDir().getAbsolutePath();
        mFileName += "/temp.3gp";
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private OnAudioCreatedListener listener;

    public interface OnAudioCreatedListener {
        void onAudioCreate(String audioPath);
    }

}
