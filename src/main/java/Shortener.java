import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.ui.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.text.StringUtil.newBombedCharSequence;

// https://github.com/krasa/GrepConsole/issues/47#issuecomment-1694427686
public class Shortener implements ApplicationComponent {

	private static final Logger LOG = Logger.getInstance(GrepConsoleExtension.class);

	public static Pattern pattern = Pattern.compile("^\\[(\\w)\\w++\\]\\[\\d{4}-\\d{2}-\\d{2} (\\d{2}:\\d{2}:\\d{2}.\\d{4})\\d+] ");

	@Override
	public void initComponent() {
		try {
			registerFunction("shortener", new Function<String, String>() {

				/** - The text will never be empty, it may or may not end with a newline - \n
				 *  - It is possible that the stream will flush prematurely and the text will be incomplete: IDEA-70016
				 *  - Return null to remove the line
				 *  - Processing blocks application output stream, make sure to limit the length and processing time when needed using #limitAndCutNewline
				 **/
				@Override
				public String apply(String text) {
					try {
						String level = shorten(text);
						if (level != null) return level;

					} catch (com.intellij.openapi.progress.ProcessCanceledException ex) {
						ApplicationManager.getApplication().invokeLater(() -> {
							Notification notification = NotificationGroupManager.getInstance().getNotificationGroup("Grep Console").createNotification("Extension processing took too long for: " + text, MessageType.WARNING);
							Notifications.Bus.notify(notification);
						});
					}
					return text;
				}
			});

		} catch (Exception e) {
			LOG.error(e);
		}
	}

	@Nullable
	protected static String shorten(String text) {
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			String level = matcher.group(1);
			String time = matcher.group(2);
			return "[" + level + "][" + time + "]" + text.substring(matcher.end());
		}
		return text;
	}

	@Override
	public void disposeComponent() {

	}

	@NotNull
	@Override
	public String getComponentName() {
		return "GrepConsoleExtension-Shortener";
	}

	/**
	 * reflection for easier project setup
	 */
	static void registerFunction(String functionName, Function<String, String> function) {
		try {
			IdeaPluginDescriptorImpl descriptor = (IdeaPluginDescriptorImpl) PluginManager.getPlugin(PluginId.getId("GrepConsole"));
			Class<?> clazz = descriptor.getPluginClassLoader().loadClass("krasa.grepconsole.plugin.ExtensionManager");

			clazz.getMethod("registerFunction", String.class, Function.class).invoke(null, functionName, function);

			LOG.info("'" + functionName + "' registered");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}


	static CharSequence limitAndCutNewline(String text, int maxLength, int milliseconds) {
		int endIndex = text.length();
		if (text.endsWith("\n")) {
			--endIndex;
		}
		if (maxLength >= 0) {
			endIndex = Math.min(endIndex, maxLength);
		}
		String substring = text.substring(0, endIndex);

		if (milliseconds > 0) {
			return newBombedCharSequence(substring, milliseconds);
		}
		return substring;
	}


}
