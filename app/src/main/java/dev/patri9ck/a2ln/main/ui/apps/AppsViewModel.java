package dev.patri9ck.a2ln.main.ui.apps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AppsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AppsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is apps fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}