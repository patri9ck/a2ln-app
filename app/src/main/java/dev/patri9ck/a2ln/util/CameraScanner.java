package dev.patri9ck.a2ln.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class CameraScanner {

    private final AtomicBoolean scanned = new AtomicBoolean();

    private final LifecycleOwner lifecycleOwner;
    private final Preview.SurfaceProvider surfaceProvider;
    private final Context context;
    private final Consumer<Barcode> barcodeConsumer;

    private final ListenableFuture<ProcessCameraProvider> processCameraProviderFuture;

    public CameraScanner(LifecycleOwner lifecycleOwner, Preview.SurfaceProvider surfaceProvider, Context context, Consumer<Barcode> barcodeConsumer) {
        this.lifecycleOwner = lifecycleOwner;
        this.surfaceProvider = surfaceProvider;
        this.context = context;
        this.barcodeConsumer = barcodeConsumer;

        processCameraProviderFuture = ProcessCameraProvider.getInstance(context);
    }

    public void startCamera() {
        Executor mainExecutor = ContextCompat.getMainExecutor(context);

        processCameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider processCameraProvider = processCameraProviderFuture.get();

                scanned.set(false);

                Preview preview = new Preview.Builder().build();

                preview.setSurfaceProvider(surfaceProvider);

                BarcodeScanner barcodeScanner = BarcodeScanning.getClient();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(mainExecutor, imageProxy -> processImageProxy(imageProxy, barcodeScanner));

                processCameraProvider.bindToLifecycle(lifecycleOwner, new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build(), imageAnalysis, preview);
            } catch (ExecutionException | InterruptedException ignored) {}
        }, mainExecutor);
    }

    public void stopCamera() {
        processCameraProviderFuture.addListener(() -> {
            try {
                processCameraProviderFuture.get().unbindAll();

                scanned.set(false);
            } catch (ExecutionException | InterruptedException ignored) {}
        }, ContextCompat.getMainExecutor(context));
    }

    private void processImageProxy(ImageProxy imageProxy, BarcodeScanner barcodeScanner) {
        @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();

        if (image == null) {
            return;
        }

        barcodeScanner.process(InputImage.fromMediaImage(image, imageProxy.getImageInfo().getRotationDegrees())).addOnSuccessListener(barcodes -> {
            if (barcodes.isEmpty() || !scanned.compareAndSet(false, true)) {
                return;
            }

            barcodeConsumer.accept(barcodes.get(0));

            stopCamera();
        }).addOnCompleteListener(barcodes -> imageProxy.close());
    }
}
