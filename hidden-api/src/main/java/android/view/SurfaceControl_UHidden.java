package android.view;

import android.graphics.Rect;
import android.os.IBinder;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(SurfaceControl.class)
public class SurfaceControl_UHidden {
    public static void openTransaction() {
    }

    public static void closeTransaction() {
    }

    public SurfaceControl.Transaction setDisplaySurface(IBinder displayToken, Surface surface) {
        return null;
    }

    public SurfaceControl.Transaction setDisplayProjection(IBinder displayToken, int orientation, Rect layerStackRect, Rect displayRect) {
        return null;
    }

    public SurfaceControl.Transaction setDisplayLayerStack(IBinder displayToken, int layerStack) {
        return null;
    }
}
