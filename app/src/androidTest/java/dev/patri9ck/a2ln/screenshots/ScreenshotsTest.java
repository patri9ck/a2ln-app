package dev.patri9ck.a2ln.screenshots;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dev.patri9ck.a2ln.R;
import dev.patri9ck.a2ln.main.ui.AppsFragment;
import dev.patri9ck.a2ln.main.ui.DevicesFragment;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar;

@RunWith(JUnit4.class)
public class ScreenshotsTest {

    @BeforeClass
    public static void before() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

        CleanStatusBar.enableWithDefaults();
    }

    @AfterClass
    public static void after() {
        CleanStatusBar.disable();
    }

    @Test
    public void devicesScreenshot() {
        FragmentScenario.launchInContainer(DevicesFragment.class, null, R.style.Theme).moveToState(Lifecycle.State.STARTED);

        Screengrab.screenshot("devices");
    }

    @Test
    public void appsScreenshot() {
        FragmentScenario.launchInContainer(AppsFragment.class, null, R.style.Theme).moveToState(Lifecycle.State.STARTED);

        Screengrab.screenshot("apps");
    }
}
