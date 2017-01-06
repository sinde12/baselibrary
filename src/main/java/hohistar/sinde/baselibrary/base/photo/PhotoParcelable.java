package hohistar.sinde.baselibrary.base.photo;

import android.os.Parcel;
import android.os.Parcelable;

public class PhotoParcelable implements Parcelable {

    public String filePath;
    public String cloudKey;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(filePath);
        out.writeString(cloudKey);
    }

    public static final Creator<PhotoParcelable> CREATOR = new Creator<PhotoParcelable>() {
        public PhotoParcelable createFromParcel(Parcel in) {
            return new PhotoParcelable(in);
        }

        public PhotoParcelable[] newArray(int size) {
            return new PhotoParcelable[size];
        }
    };

    public PhotoParcelable() {

    }

    private PhotoParcelable(Parcel in) {
        filePath = in.readString();
        cloudKey = in.readString();
    }
}