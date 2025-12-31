package android.view;

import android.os.Parcel;
import android.os.Parcelable;

public final class DisplayInfo implements Parcelable {
    public int layerStack;

    public int displayId;

    public DisplayAddress address;

    public String name;

    public int logicalWidth;

    public int logicalHeight;

    public int logicalDensityDpi;

    public int rotation;

    public int flags;

    public String uniqueId;

    public int ownerUid;

    public String ownerPackageName;

    private DisplayInfo(Parcel in) {
        logicalWidth = in.readInt();
        logicalHeight = in.readInt();
        rotation = in.readInt();
        flags = in.readInt();
        uniqueId = in.readString();
    }

    public static final Creator<DisplayInfo> CREATOR = new Creator<DisplayInfo>() {
        @Override
        public DisplayInfo createFromParcel(Parcel in) {
            return new DisplayInfo(in);
        }

        @Override
        public DisplayInfo[] newArray(int size) {
            return new DisplayInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(logicalWidth);
        dest.writeInt(logicalHeight);
        dest.writeInt(rotation);
        dest.writeInt(flags);
        dest.writeString(uniqueId);
    }
}
