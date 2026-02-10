package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ScreenshotService {


    public static void capture(WebDriver driver, Path runFolder, int stepId) {
        try {
            Path screenshotsDir = runFolder.resolve("screenshots");
            Files.createDirectories(screenshotsDir);

            File src = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE);

            Path target = screenshotsDir.resolve("step_" + stepId + ".png");

            Files.copy(src.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            LogsManager.info("Screenshot saved: " + target.toAbsolutePath());

        } catch (Exception e) {
            LogsManager.error("Failed to capture screenshot: " + e.getMessage());
        }
    }
}
