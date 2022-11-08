package com.emaraic.jdlib;

import com.emaraic.utils.FaceDescriptor;
import com.emaraic.utils.Image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Taha Emara
 * Website: http://www.emaraic.com
 * Email : taha@emaraic.com
 * Created on: Nov 21, 2020
 */
public class Jdlib {

    private String facialLandmarksModelPath;
    private String faceEmbeddingModelPath;

    public Jdlib(String facialLandmarksModelPath, String faceEmbeddingModelPath) {
        this.facialLandmarksModelPath = facialLandmarksModelPath;
        this.faceEmbeddingModelPath = faceEmbeddingModelPath;
        loadLib();
    }

    public Jdlib(String facialLandmarksModelPath) {
        this.facialLandmarksModelPath = facialLandmarksModelPath;
        this.faceEmbeddingModelPath = null;
        loadLib();
    }

    private native long getFaceDectorHandler();

    private native long getShapePredictorHandler(String modelPath);

    private native long getFaceEmbeddingHandler(String modelPath);

    private native ArrayList<Rectangle> faceDetect(long faceDetectorHandler, byte[] pixels, int h, int w);

    private native ArrayList<FaceDescriptor> getFacialLandmarks(long shapePredictorHandler, long faceDetectorHandler, byte[] pixels, int h, int w);

    private native ArrayList<FaceDescriptor> getFaceEmbeddings(long FaceEmbeddingHandler, long shapePredictorHandler, long faceDetectorHandler, byte[] pixels, int h, int w);

    private void loadLib() {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        String name = System.mapLibraryName("Jdlib");

        String libpath = "";
        if (os.contains("linux")) {
            libpath = "/native" + File.separator + "linux" + File.separator + name;
        } else if (os.contains("mac")) {
            libpath = "/native" + File.separator + "macosx" + File.separator + name;
        } else {
            throw new java.lang.UnsupportedOperationException(os + " is not supported. Try to recompile Jdlib on your machine and then use it.");
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = Jdlib.class.getResourceAsStream(libpath);
            File fileOut = File.createTempFile(name, "");

            outputStream = new FileOutputStream(fileOut);
            byte[] buffer = new byte[1024];
            int read = -1;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.close();
            inputStream.close();
            System.load(fileOut.toString());
        } catch (Exception e) {
            System.err.println("Error During Loading Lib!");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("Error During Closing Input Stream!!");
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    System.err.println("Error During Closing Output Stream!!");
                }
            }
        }
    }

    public Optional<ArrayList<Rectangle>> detectFace(BufferedImage img) {
        Image image = new Image(img);
        return detectFace(image.pixels, image.height, image.width);
    }

    public Optional<ArrayList<Rectangle>> detectFace(byte[] pixels, int height, int width) {
        ArrayList<Rectangle> data = faceDetect(getFaceDectorHandler(), pixels, height, width);
        return Optional.ofNullable(data);
    }

    public Optional<ArrayList<FaceDescriptor>> getFaceLandmarks(BufferedImage img) {
        Image image = new Image(img);
        return getFaceLandmarks(image.pixels, image.height, image.width);
    }

    public Optional<ArrayList<FaceDescriptor>> getFaceLandmarks(byte[] pixels, int height, int width) {
        ArrayList<FaceDescriptor> data = getFacialLandmarks(getShapePredictorHandler(facialLandmarksModelPath),
                getFaceDectorHandler(), pixels, height, width);
        return Optional.ofNullable(data);
    }

    public Optional<ArrayList<FaceDescriptor>> getFaceEmbeddings(BufferedImage img) {
        if (facialLandmarksModelPath == null) {
            throw new IllegalArgumentException("Path to face embedding model isn't provided!");
        }

        Image image = new Image(img);
        return getFaceEmbeddings(image.pixels, image.height, image.width);
    }

    public Optional<ArrayList<FaceDescriptor>> getFaceEmbeddings(byte[] pixels, int height, int width) {
        if (facialLandmarksModelPath == null) {
            throw new IllegalArgumentException("Path to face embedding model isn't provided!");
        }

        ArrayList<FaceDescriptor> data = getFaceEmbeddings(getFaceEmbeddingHandler(faceEmbeddingModelPath),
                getShapePredictorHandler(facialLandmarksModelPath), getFaceDectorHandler(), pixels, height, width);
        return Optional.ofNullable(data);
    }
}
