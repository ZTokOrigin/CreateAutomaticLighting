package com.ztok.create_auto_light;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import java.lang.reflect.Field;

public class DeployerAccess {
    private static Field modeField;

    static {
        try {
            modeField = DeployerBlockEntity.class.getDeclaredField("mode");
            modeField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // Try to find it if the name is different (e.g. obfuscated)
            // But for now assume dev environment names
            e.printStackTrace();
        }
    }

    public static Object getMode(DeployerBlockEntity be) {
        if (modeField == null) return null;
        try {
            return modeField.get(be);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
