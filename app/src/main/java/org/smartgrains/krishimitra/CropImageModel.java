package org.smartgrains.krishimitra;

public class CropImageModel {
    private String cropName;
    private int imageResource;

    public CropImageModel(String cropName, int imageResource) {
        this.cropName = cropName;
        this.imageResource = imageResource;
    }

    public String getCropName() {
        return cropName;
    }

    public int getImageResource() {
        return imageResource;
    }
}
