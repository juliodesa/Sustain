package com.project.sustain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Anish on 3/2/17.
 */

public enum WaterCondition implements Parcelable {
    WASTE("Waste"),
    TREATABLE_CLEAR("Treatable-Clear"),
    TREATABLE_MUDDY("Treatable-Muddy"),
    POTABLE("Potable");

    private String conditionDeclared;

    WaterCondition (String conditionPassed) {
        conditionDeclared = conditionPassed;
    }

    public String toString() {
        return conditionDeclared;
    }

    public static final Parcelable.Creator<WaterCondition> CREATOR = new Parcelable.Creator<WaterCondition>() {
        public WaterCondition createFromParcel(Parcel in) {
            return WaterCondition.values()[in.readInt()];
        }
        public WaterCondition[] newArray(int size) {
            return new WaterCondition[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        out.writeInt(ordinal());
    }
}
