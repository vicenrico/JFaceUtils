package it.albertus.jface.console;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scrollable;

import it.albertus.jface.JFaceMessages;
import it.albertus.jface.SwtThreadExecutor;
import it.albertus.util.Configured;
import it.albertus.util.NewLine;
import it.albertus.util.logging.LoggerFactory;

public abstract class ScrollableConsole<T extends Scrollable> extends OutputStream {

	private static final Logger logger = LoggerFactory.getLogger(ScrollableConsole.class);

	protected static final PrintStream defaultSysOut = System.out;
	protected static final PrintStream defaultSysErr = System.err;
	protected static final String newLine = NewLine.SYSTEM_LINE_SEPARATOR;

	public static class Defaults {
		public static final int GUI_CONSOLE_MAX_CHARS = 100000;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected final boolean redirectSystemStream;
	protected final T scrollable;
	protected ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	private Configured<Integer> maxChars;

	protected ScrollableConsole(final Composite parent, final Object layoutData, final boolean redirectSystemStream) {
		this.redirectSystemStream = redirectSystemStream;

		scrollable = createScrollable(parent);
		scrollable.setLayoutData(layoutData);
		scrollable.setFont(JFaceResources.getTextFont());

		if (redirectSystemStream) {
			scrollable.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(final DisposeEvent de) {
					resetStreams();
				}
			});
			redirectStreams(); // SystemConsole & System.out will print on this SWT Text Widget.
		}
	}

	protected abstract T createScrollable(Composite parent);

	protected abstract void doPrint(String value, int maxChars);

	public abstract void clear();

	public abstract boolean isEmpty();

	public abstract boolean hasSelection();

	@Override
	public void write(final int b) throws IOException {
		ensureOpen();
		buffer.write(b); // The wrapping PrintStream will flush automatically when needed.
	}

	@Override
	public void flush() throws IOException {
		ensureOpen();
		if (buffer.size() != 0) {
			print(buffer.toString()); // platform's default character set
			buffer.reset();
		}
	}

	@Override
	public void close() throws IOException {
		if (buffer != null) {
			flush();
			if (redirectSystemStream) {
				resetStreams();
			}
			buffer = null;
		}
	}

	protected void print(final String value) {
		// Dealing with null argument...
		final String toPrint;
		if (value == null) {
			toPrint = String.valueOf(value);
		}
		else {
			toPrint = value;
		}

		final int capacity = getMaxChars();

		// Actual print... (async avoids deadlocks)
		new SwtThreadExecutor(scrollable, true) {
			@Override
			protected void run() {
				doPrint(toPrint, capacity);
			}

			@Override
			protected void onError(final Exception exception) {
				failSafePrint(toPrint);
			}
		}.start();
	}

	protected void failSafePrint(final String value) {
		defaultSysOut.print(value);
	}

	protected void redirectStreams() {
		final PrintStream ps = new PrintStream(this, true); // platform's default character set
		try {
			System.setOut(ps);
			System.setErr(ps);
		}
		catch (final RuntimeException re) {
			logger.log(Level.SEVERE, JFaceMessages.get("err.console.streams.redirect"), re);
		}
	}

	protected void resetStreams() {
		try {
			System.setOut(defaultSysOut);
			System.setErr(defaultSysErr);
		}
		catch (final RuntimeException re) {
			logger.log(Level.SEVERE, JFaceMessages.get("err.console.streams.reset"), re);
		}
	}

	private void ensureOpen() throws IOException {
		if (buffer == null) {
			throw new IOException("Stream closed");
		}
	}

	public int getMaxChars() {
		try {
			return maxChars != null && maxChars.getValue() != null ? maxChars.getValue() : Defaults.GUI_CONSOLE_MAX_CHARS;
		}
		catch (final RuntimeException re) {
			logger.log(Level.WARNING, JFaceMessages.get("err.console.capacity") + ' ' + JFaceMessages.get("err.configuration.using.default", Defaults.GUI_CONSOLE_MAX_CHARS), re);
			return Defaults.GUI_CONSOLE_MAX_CHARS;
		}
	}

	public void setMaxChars(final Configured<Integer> maxChars) {
		this.maxChars = maxChars;
	}

	public void setMaxChars(final int maxChars) {
		this.maxChars = new Configured<Integer>() {
			@Override
			public Integer getValue() {
				return maxChars;
			}
		};
	}

	public T getScrollable() {
		return scrollable;
	}

}