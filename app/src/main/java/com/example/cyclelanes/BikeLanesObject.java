package com.example.cyclelanes;

import com.google.android.gms.maps.model.LatLng;

public class BikeLanesObject {
    int objectID;
    LatLng coordinates;

    public BikeLanesObject(int objectID, LatLng coordinates) {
        this.objectID = objectID;
        this.coordinates = coordinates;
    }


    public int getObjectID() {
        return objectID;
    }

    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
}
