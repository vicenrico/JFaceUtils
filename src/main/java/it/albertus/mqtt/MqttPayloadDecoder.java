package it.albertus.mqtt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import it.albertus.util.IOUtils;

public class MqttPayloadDecoder {

	private static final int BUFFER_SIZE = 4096;

	/**
	 * Decode a MQTT payload based on {@link MqttPayload}.
	 * 
	 * @param payload the received payload, as extracted from the received MQTT
	 *        message.
	 * @return the effective payload
	 * @throws IOException in case of malformed payload
	 * 
	 * @see MqttPayload
	 */
	@SuppressWarnings("restriction")
	public byte[] decode(final byte[] payload) throws IOException {
		final MqttPayload mqttPayload = MqttPayload.fromPayload(payload);

		// Check Content-Length header
		if (mqttPayload.getHeaders().containsKey(MqttUtils.HEADER_KEY_CONTENT_LENGTH)) {
			final int contentLength = Integer.parseInt(mqttPayload.getHeaders().getFirst(MqttUtils.HEADER_KEY_CONTENT_LENGTH));
			if (contentLength != mqttPayload.getBody().length) {
				throw new IOException(MqttUtils.HEADER_KEY_CONTENT_LENGTH + " header value does not match the actual body length (expected: " + contentLength + ", actual: " + mqttPayload.getBody().length + ").");
			}
		}

		// Decompress body if needed
		if (MqttUtils.HEADER_VALUE_GZIP.equalsIgnoreCase(mqttPayload.getHeaders().getFirst(MqttUtils.HEADER_KEY_CONTENT_ENCODING))) {
			return decompress(mqttPayload.getBody());
		}
		else {
			return mqttPayload.getBody();
		}
	}

	protected byte[] decompress(final byte[] compressed) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPInputStream gzis = null;
		try {
			gzis = new GZIPInputStream(new ByteArrayInputStream(compressed));
			IOUtils.copy(gzis, baos, BUFFER_SIZE);
			gzis.close();
		}
		catch (final IOException e) {
			throw new IllegalStateException(e); // ByteArrayOutputStream cannot throw IOException
		}
		finally {
			IOUtils.closeQuietly(gzis);
		}
		return baos.toByteArray();
	}

}