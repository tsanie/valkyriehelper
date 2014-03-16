package org.tsanie.valkyriehelper.fragment;

import org.tsanie.valkyriehelper.MainActivity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class BaseFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    protected static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static BaseFragment newInstance(int sectionNumber) {
        BaseFragment fragment;

        switch (sectionNumber) {
        case 1:
            fragment = new LoginFragment();
            break;
            
        case 3:
            fragment = new KingsFragment();
            break;

        default:
            fragment = new SimpleFragment();
            break;
        }

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        int args = getArguments().getInt(ARG_SECTION_NUMBER);
        ((MainActivity) activity).onSectionAttached(args);
    }
}
