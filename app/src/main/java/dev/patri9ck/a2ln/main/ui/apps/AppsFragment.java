package dev.patri9ck.a2ln.main.ui.apps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dev.patri9ck.a2ln.databinding.FragmentAppsBinding;

public class AppsFragment extends Fragment {

    private FragmentAppsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AppsViewModel appsViewModel =
                new ViewModelProvider(this).get(AppsViewModel.class);

        binding = FragmentAppsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textApps;
        appsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}