package cs10.apps.common.android.ui;

import androidx.fragment.app.Fragment;

public class CS_Fragment extends Fragment {

    public void doInBackground(Runnable r){
        new Thread(r).start();
    }

    public void doInForeground(Runnable r){
        if (getActivity() != null) getActivity().runOnUiThread(r);
    }
}
