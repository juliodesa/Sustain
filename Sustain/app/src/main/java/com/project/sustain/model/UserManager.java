package com.project.sustain.model;

import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;

import com.project.sustain.controllers.AuthResultListener;
import com.project.sustain.controllers.DatabaseWrapper;
import com.project.sustain.controllers.FirebaseWrapper;
import com.project.sustain.controllers.LoginResultListener;
import com.project.sustain.controllers.QuerySingleResultListener;
import com.project.sustain.controllers.RegistrationResultListener;
import com.project.sustain.controllers.UserResultListener;

/**
 * User Manager class has methods for users in relation to water reports.
 * @author Marcia
 */

public class UserManager {
    private User currentUser;
    private final DatabaseWrapper DBWrapper;
    private UserResultListener mUserResultListener = null;
    private AuthResultListener mAuthResultListener = null;
    private LoginResultListener mLoginResultListener = null;
    private RegistrationResultListener mRegistrationResultListener = null;
    private static final String TAG = "UserManager";

    public UserManager() {
        DBWrapper = new FirebaseWrapper();
        DBWrapper.connect();
    }

    public String getCurrentUserId() {
        return DBWrapper.getCurrentUserId();
    }

    public String getCurrentUserDisplayName() {
        return DBWrapper.getCurrentUserDisplayName();
    }

    public void updateCurrentUserDisplayName(String newName) {
        DBWrapper.updateCurrentUserDisplayName(newName);
    }

    public String getCurrentUserEmail() {
        return DBWrapper.getCurrentUserEmail();
    }

    public void getCurrentUser() throws UserNotAuthenticatedException {
        String userId = DBWrapper.getCurrentUserId();
        if (!userId.isEmpty()) {
            DBWrapper.setQuerySingleResultListener(new QuerySingleResultListener() {
                @Override
                public <T> void onComplete(T result) {
                    Log.d(TAG, "onComplete on getCurrentUser");
                    if (result != null) {
                        currentUser = (User) result;
                    }
                    if (mUserResultListener != null) {
                        mUserResultListener.onComplete(currentUser);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    if (mUserResultListener != null) {
                        mUserResultListener.onError(error);
                    }
                }
            });

            DBWrapper.queryDatabaseForSingleAsync("/userProfiles/" + userId, new User());
        } else {
            throw new UserNotAuthenticatedException("No user is currently logged in.");
        }
    }

    public void addUser(User user) {
        this.updateUser(user);
    }

    public void updateUser(User user) {
        DBWrapper.updateSingleRecord("/userProfiles/" + user.getUserId(), user);
    }

    public void logInUserEmailPassword(String email, String password) {
        mAuthResultListener = new AuthResultListener() {
            @Override
            public void onComplete(boolean success) {
                if (mLoginResultListener != null) {
                    mLoginResultListener.onComplete(success);
                }
            }

            @Override
            public void onError(Throwable error) {
                if (mLoginResultListener != null) {
                    mLoginResultListener.onError(error);
                }
            }
        };
        DBWrapper.setAuthResultListener(mAuthResultListener);
        DBWrapper.loginWithEmail(email, password);
    }

    public void registerWithEmailPassword(String email, String password) {
        RegistrationResultListener resultListener = new RegistrationResultListener() {
            @Override
            public void onComplete(boolean success, String result) {
                if (mRegistrationResultListener != null) {
                    mRegistrationResultListener.onComplete(success, result);
                }
            }

            @Override
            public void onError(Throwable error) {
                if (mRegistrationResultListener != null) {
                    mRegistrationResultListener.onError(error);
                }
            }
        };
        DBWrapper.setRegistrationResultListener(resultListener);
        DBWrapper.createAccountWithEmailPassword(email, password);
    }

    public void logOutUser() {
        DBWrapper.logOut();
    }


    public void setUserResultListener(UserResultListener listener) {
        this.mUserResultListener = listener;
    }

    public void removeUserResultListener() {
        this.mUserResultListener = null;
    }

    public void setLoginResultListener(LoginResultListener listener) {
        this.mLoginResultListener = listener;
    }

    public void removeLoginResultListener() { this.mLoginResultListener = null; }

    public void setRegistrationResultListener(RegistrationResultListener listener) {
        this.mRegistrationResultListener = listener;
    }

    public void removeRegistrationResultListener() {
        this.mRegistrationResultListener = null;
    }

    public void removeAllListeners() {
        mUserResultListener = null;
        mRegistrationResultListener = null;
        mAuthResultListener = null;
        mLoginResultListener = null;
        DBWrapper.removeRegistrationResultListener();
        DBWrapper.removeAuthResultListener();
        DBWrapper.removeQuerySingleResultListener();
    }
}
