package com.drugporter.janatapharmacy.imagepicker;

public interface ImageCompressionListener {
    void onStart();

    void onCompressed(String filePath);
}
