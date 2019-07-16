package com.android.jc_banner;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Mr.Hu(Jc) JcPlug
 * @create 2019-07-05 09:45
 * @describe
 * @update
 */
public class RecyclerViewState implements Parcelable {
    private int position;
    private float offset;
    private boolean isReverseLayout;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public boolean isReverseLayout() {
        return isReverseLayout;
    }

    public void setReverseLayout(boolean reverseLayout) {
        isReverseLayout = reverseLayout;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.position);
        dest.writeFloat(this.offset);
        dest.writeByte(this.isReverseLayout ? (byte) 1 : (byte) 0);
    }

     RecyclerViewState() {
    }

     RecyclerViewState(RecyclerViewState other) {
        position = other.position;
        offset = other.offset;
        isReverseLayout = other.isReverseLayout;
    }

     private RecyclerViewState(Parcel in) {
        this.position = in.readInt();
        this.offset = in.readFloat();
        this.isReverseLayout = in.readByte() != 0;
    }

    public static final Parcelable.Creator<RecyclerViewState> CREATOR = new Parcelable.Creator<RecyclerViewState>() {
        @Override
        public RecyclerViewState createFromParcel(Parcel source) {
            return new RecyclerViewState(source);
        }

        @Override
        public RecyclerViewState[] newArray(int size) {
            return new RecyclerViewState[size];
        }
    };
}
