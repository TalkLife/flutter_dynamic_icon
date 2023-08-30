package io.github.tastelessjolt.flutterdynamicicon;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import io.flutter.Log;

public class IconChanger {
    /// Dividing the all the Activities in three parts
    // 1. All the `activity-alias`s which have `intent-filter` with action MAIN and category LAUNCHER
    //  When mSetAlternateIconName is passed `null`, all such `activity-alias`s are disabled
    //  When mSetAlternateIconName is passed an alias name, only that `activity-alias`s is enabled and
    //        the rest of the `activity-alias` are disabled
    //
    // 2. All the other legitimate `activity`s which have `intent-filter` with action MAIN and category LAUNCHER
    //  When mSetAlternateIconName is passed `null`, all such `activity`s are enabled
    //  Similarly, when mSetAlternateIconName is passed a alias name, all such `activity`s are disabled
    //
    // 3. All other `activity`s
    //  These `activity`s are not touched
    //
    static private String TAG = "flutterdynamicicon";

    public static ActivityInfo getCurrentEnabledAlias(Context context) {
        PackageManager pm = context.getPackageManager();

        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS);
            ActivityInfo enabled = null;
            for (ActivityInfo activityInfo : info.activities) {
                Log.d("IconChanger", activityInfo.name.toString());
                // Only checks among the `activity-alias`s, for current enabled alias
                if (activityInfo.targetActivity != null) {
                    boolean isEnabled = Helper.isComponentEnabled(pm, context.getPackageName(), activityInfo.name);
                    if (isEnabled) enabled = activityInfo;
                }
            }
            return enabled;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void enableIcon(Context context, String activityName) {
        PackageManager pm = context.getPackageManager();
        ActivityInfo currentlyEnabledIcon = getCurrentEnabledAlias(context);

        if (currentlyEnabledIcon == null && activityName == null) {
            // Currently enabled and request to enable activities are both the default activities
            return;
        }

        List<ComponentName> components = Helper.getComponentNames(context, activityName);
        for (ComponentName component : components) {
            if (currentlyEnabledIcon != null && currentlyEnabledIcon.name.equals(component.getClassName()))
                return;
            Log.d(TAG, String.format("Changing enabled activity-alias from %s to %s", currentlyEnabledIcon != null ? currentlyEnabledIcon.name : "default", component.getClassName()));
            pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        List<ComponentName> componentsToDisable;
        if (currentlyEnabledIcon != null) {
            componentsToDisable = Arrays.asList(new ComponentName(context.getPackageName(), currentlyEnabledIcon.name));
        } else {
            componentsToDisable = Helper.getComponentNames(context, null);
        }
        for (ComponentName toDisable : componentsToDisable) {
            pm.setComponentEnabledSetting(toDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    //dynamically change app icon for Android
    public static void setIcon(Context context, String targetIcon, List<String> activitiesArray) {
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        String packageName = context.getApplicationContext().getPackageName();


        for (String value : activitiesArray) {
            int action;
            if(value.equals(targetIcon)) {
                action = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            } else {
                action = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            }
            String className = packageName +
                    "." +
                    value;
            packageManager.setComponentEnabledSetting(
                    new ComponentName(packageName, className),
            action, PackageManager.DONT_KILL_APP);
        }
    }

    public static boolean isComponentEnabled(Context context, String clsName) {
//        final String clsName = "com.yalantis.ucrop.UCropActivity";
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        String packageName = context.getApplicationContext().getPackageName();
        ComponentName componentName = new ComponentName(packageName, clsName);
        int componentEnabledSetting = packageManager.getComponentEnabledSetting(componentName);

        switch (componentEnabledSetting) {
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                return false;
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                return true;
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
            default:
                // We need to get the application info to get the component's default state
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES
                            | PackageManager.GET_RECEIVERS
                            | PackageManager.GET_SERVICES
                            | PackageManager.GET_PROVIDERS
                            | PackageManager.GET_DISABLED_COMPONENTS);

                    List<ComponentInfo> components = new ArrayList<>();
                    if (packageInfo.activities != null) Collections.addAll(components, packageInfo.activities);
                    if (packageInfo.services != null) Collections.addAll(components, packageInfo.services);
                    if (packageInfo.providers != null) Collections.addAll(components, packageInfo.providers);

                    for (ComponentInfo componentInfo : components) {
                        if (componentInfo.name.equals(clsName)) {
                            return componentInfo.isEnabled();
                        }
                    }

                    // the component is not declared in the AndroidManifest
                    return false;
                } catch (PackageManager.NameNotFoundException e) {
                    // the package isn't installed on the device
                    return false;
                }
        }
    }


    public static void enablePackageClass(Context context, String clsName) {
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        String packageName = context.getApplicationContext().getPackageName();
        ComponentName componentName = new ComponentName(packageName, clsName);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }
}
