package com.thetestament.cread.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.linkedin.android.spyglass.mentions.Mentionable;

/**
 * Model representing a person.
 */
public class PersonMentionModel implements Mentionable {

    private String mName;
    private String mPictureURL;


    public PersonMentionModel() {

    }


    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmPictureURL() {
        return mPictureURL;
    }

    public void setmPictureURL(String mPictureURL) {
        this.mPictureURL = mPictureURL;
    }

    // --------------------------------------------------
    // Mentionable/Suggestible Implementation
    // --------------------------------------------------

    @NonNull
    @Override
    public String getTextForDisplayMode(MentionDisplayMode mode) {
        switch (mode) {
            case FULL:
                return getmName();
            case PARTIAL:
                String[] words = getmName().split(" ");
                return (words.length > 1) ? words[0] : "";
            case NONE:
            default:
                return "";
        }
    }

    @Override
    public MentionDeleteStyle getDeleteStyle() {
        // People support partial deletion
        // i.e. "John Doe" -> DEL -> "John" -> DEL -> ""
        return MentionDeleteStyle.PARTIAL_NAME_DELETE;
    }

    @Override
    public int getSuggestibleId() {
        return getmName().hashCode();
    }

    @Override
    public String getSuggestiblePrimaryText() {
        return getmName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mPictureURL);
    }

    public PersonMentionModel(Parcel in) {
        mName = in.readString();
        mPictureURL = in.readString();
    }

    public static final Parcelable.Creator<PersonMentionModel> CREATOR
            = new Parcelable.Creator<PersonMentionModel>() {
        public PersonMentionModel createFromParcel(Parcel in) {
            return new PersonMentionModel(in);
        }

        public PersonMentionModel[] newArray(int size) {
            return new PersonMentionModel[size];
        }
    };

    // --------------------------------------------------
    // PersonLoader Class (loads people from JSON file)
    // --------------------------------------------------

    /*public static class PersonLoader extends MentionsLoader<Person> {
        private static final String TAG = PersonLoader.class.getSimpleName();



        @Override
        public Person[] loadData(JSONArray arr) {
            Person[] data = new Person[arr.length()];
            try {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String first = obj.getString(Person"first");
                    String last = obj.getString("last");
                    String url = obj.getString("picture");
                    data[i] = new Person(first, last, url);
                }
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception while parsing person JSONArray", e);
            }

            return data;
        }

        // Modified to return suggestions based on both first and last name
        @Override
        public List<Person> getSuggestions(QueryToken queryToken) {
            String[] namePrefixes = queryToken.getKeywords().toLowerCase().split(" ");
            List<Person> suggestions = new ArrayList<>();
            if (mData != null) {
                for (Person suggestion : mData) {
                    String firstName = suggestion.getFirstName().toLowerCase();
                    String lastName = suggestion.getLastName().toLowerCase();
                    if (namePrefixes.length == 2) {
                        if (firstName.startsWith(namePrefixes[0]) && lastName.startsWith(namePrefixes[1])) {
                            suggestions.add(suggestion);
                        }
                    } else {
                        if (firstName.startsWith(namePrefixes[0]) || lastName.startsWith(namePrefixes[0])) {
                            suggestions.add(suggestion);
                        }
                    }
                }
            }
            return suggestions;
        }
    }*/
}

