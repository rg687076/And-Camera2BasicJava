package com.example.android.camera2.basic;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

import com.example.android.camera2.basic.fragments.CameraFragment;
import com.example.android.camera2.basic.fragments.ImageViewerFragment;
import com.example.android.camera2.basic.fragments.SelectorFragment;

public class CameraActivity extends AppCompatActivity {
    public static int ANIMATION_FAST_MILLIS = 50;
    private CameraViewModel mViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mViewModel = new ViewModelProvider(this).get(CameraViewModel.class);

        /* 全画面アプリ設定 */
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarContrastEnforced(false);
        getWindow().setNavigationBarContrastEnforced(false);

        /* get camera permission */
        ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                (isGranted) -> {
                    if (isGranted) {
                        /* 権限取得 OK時 -> Fragment追加 */
                        if (null == savedInstanceState) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, SelectorFragment.newInstance())
                                    .commit();
                        }
                    }
                    else {
                        /* 権限取得 拒否時 -> ErrorダイアグOpenでアプリ終了!! */
                        ErrorDialog.newInstance(getString(R.string.request_permission)).show(getSupportFragmentManager(), "Error!!");
                    }
                });

        /* request camera permission */
        launcher.launch(android.Manifest.permission.CAMERA);
    }

    /**************************************
     * Utils
     * ************************************/
    public static class ErrorDialog extends DialogFragment {
        private static final String ARG_MESSAGE = "message";
        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Activity activity = getActivity();
            if(activity == null) throw new RuntimeException("illegal state!! activity is null!!");
            android.os.Bundle bundle = getArguments();
            if(bundle == null) throw new RuntimeException("illegal state!! bundle is null!!");

            return new AlertDialog.Builder(activity)
                    .setMessage(bundle.getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            /* コンフィグ画面時のBackイベントは、撮像画面に戻る。 */
            if(getSupportFragmentManager().getFragments().get(0) instanceof ImageViewerFragment) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, CameraFragment.newInstance(mViewModel.getCameraId(), mViewModel.getPixelFormat()))
                        .commit();
                return true;
            }
            else if(getSupportFragmentManager().getFragments().get(0) instanceof CameraFragment) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, SelectorFragment.newInstance())
                        .commit();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}