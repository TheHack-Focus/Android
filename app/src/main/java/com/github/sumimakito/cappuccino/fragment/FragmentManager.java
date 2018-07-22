package com.github.sumimakito.cappuccino.fragment;

import android.util.Log;

import java.util.HashMap;

import androidx.fragment.app.Fragment;

public class FragmentManager {
    private static final String TAG = "FragmentManager";

    private HashMap<Class<? extends Fragment>, Fragment> cachedFragments;

    public FragmentManager() {
        cachedFragments = new HashMap<>();
    }

    public void set(Class<? extends Fragment> fragmentClass, Fragment fragment) {
        cachedFragments.put(fragmentClass, fragment);
    }

    public Fragment get(Class<? extends Fragment> fragmentClass) {
        if (cachedFragments.containsKey(fragmentClass)) {
            Log.w(TAG, fragmentClass.getCanonicalName() + ": HIT CACHE");
            return cachedFragments.get(fragmentClass);
        }
        try {
            Log.w(TAG, fragmentClass.getCanonicalName() + ": MISSED CACHE");
            Fragment fragment = fragmentClass.newInstance();
            if (fragment == null) {
                return null;
            }
            cachedFragments.put(fragmentClass, fragment);
            return fragment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SafeVarargs
    public final void release(Class<? extends Fragment>... fragmentClasses) {
        for (Class<? extends Fragment> cls : fragmentClasses) {
            cachedFragments.remove(cls);
        }
    }

    public void releaseAll() {
        cachedFragments.clear();
    }
}
