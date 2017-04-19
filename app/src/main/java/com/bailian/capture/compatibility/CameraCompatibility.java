package com.bailian.capture.compatibility;


public class CameraCompatibility {

    public static CameraInterface getCamera() {
        return new LowCamera();
    }

}
