package com.sk.simplepins.data.source;

import com.sk.simplepins.data.Pin;

import java.util.List;

/**
 * Created by Sviat on Nov 09, 2016.
 */

public interface PinsDataSource {
    interface OnAllPinsLoadedCallback {
        void onPinsLoaded(List<Pin> pins);

        void onDataNotAvailable();
    }

    interface OnPinSavedCallback {
        void onPinSaved(Pin pin);

        void onPinSaveError();
    }

    interface OnPinRemoveCallback {
        void onPinRemoveSuccess();

        void OnPinRemoveError();
    }

    interface OnPinUpdatedCallback {
        void onPinUpdated();

        void onPinUpdateError();
    }

    interface OnAllPinsRemovedCallback {
        void onAllPinsRemoved();

        void onRemoveAllPinsError();
    }

    void getPins(String uid, OnAllPinsLoadedCallback callback);

    void savePin(Pin pin, OnPinSavedCallback callback);

    void deletePin(Pin pin, OnPinRemoveCallback callback);

    void deletePin(long id, OnPinRemoveCallback callback);

    void updatePin(Pin pin, OnPinUpdatedCallback callback);

    void deleteAllPins(String uid, OnAllPinsRemovedCallback callback);
}