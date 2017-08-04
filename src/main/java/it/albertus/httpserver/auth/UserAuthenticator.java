package it.albertus.httpserver.auth;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import com.sun.net.httpserver.BasicAuthenticator;

import it.albertus.httpserver.auth.config.IUserAuthenticatorConfig;
import it.albertus.jface.JFaceMessages;
import it.albertus.util.logging.LoggerFactory;

@SuppressWarnings("restriction")
public class UserAuthenticator extends BasicAuthenticator {

	private static final Logger logger = LoggerFactory.getLogger(UserAuthenticator.class);

	private static final String DEFAULT_CHARSET_NAME = "UTF-8";

	private final ThreadLocal<MessageDigest> messageDigest;
	private final IUserAuthenticatorConfig configuration;
	private Charset charset;

	public UserAuthenticator(final IUserAuthenticatorConfig configuration) {
		super(configuration.getRealm());
		this.configuration = configuration;
		final String hashAlgorithm = configuration.getPasswordHashAlgorithm();
		if (hashAlgorithm != null && !hashAlgorithm.isEmpty()) {
			this.charset = Charset.forName(DEFAULT_CHARSET_NAME);
			this.messageDigest = new ThreadLocal<MessageDigest>() {
				@Override
				protected MessageDigest initialValue() {
					try {
						return MessageDigest.getInstance(hashAlgorithm);
					}
					catch (final NoSuchAlgorithmException e) {
						throw new IllegalArgumentException(hashAlgorithm, e);
					}
				}

				@Override
				public MessageDigest get() {
					final MessageDigest md = super.get();
					md.reset();
					return md;
				}
			};
		}
		else {
			this.messageDigest = null;
		}
	}

	@Override
	public boolean checkCredentials(final String specifiedUsername, final String specifiedPassword) {
		try {
			if (specifiedUsername == null || specifiedUsername.isEmpty() || specifiedPassword == null || specifiedPassword.isEmpty()) {
				return fail();
			}

			final char[] expectedPassword = getConfiguration().getPassword(specifiedUsername);
			if (expectedPassword != null && expectedPassword.length > 0 && checkPassword(specifiedPassword, expectedPassword)) {
				return true;
			}
			else {
				logger.log(Level.parse(getConfiguration().getFailureLoggingLevel()), JFaceMessages.get("err.httpserver.authentication"), new String[] { specifiedUsername, specifiedPassword });
				return fail();
			}
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
			return fail();
		}
	}

	protected boolean checkPassword(final String provided, final char[] expected) {
		final char[] computed;
		if (messageDigest != null) {
			computed = DatatypeConverter.printHexBinary(messageDigest.get().digest(provided.getBytes(charset))).toLowerCase().toCharArray();
		}
		else {
			computed = provided.toCharArray();
		}

		boolean equal = true;
		if (computed.length != expected.length) {
			equal = false;
		}
		for (int i = 0; i < 0x400; i++) {
			if (computed[i % computed.length] != expected[i % expected.length]) {
				equal = false;
			}
		}
		return equal;
	}

	protected boolean fail() {
		try {
			TimeUnit.MILLISECONDS.sleep(configuration.getFailDelay());
		}
		catch (final InterruptedException e) {
			logger.log(Level.FINE, e.toString(), e);
			Thread.currentThread().interrupt();
		}
		return false;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

	protected ThreadLocal<MessageDigest> getMessageDigest() {
		return messageDigest;
	}

	protected IUserAuthenticatorConfig getConfiguration() {
		return configuration;
	}

}