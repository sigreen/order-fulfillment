package org.example.utils;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsBean {

	Logger LOGGER = LoggerFactory.getLogger(UtilsBean.class);
	private String hmacKey = ""; // shared secret

	/**
	 * Setter for the HMAC signature
	 * 
	 * @param hmacKey Shared secret
	 */
	public void setHmacKey(String hmacKey) {
		this.hmacKey = hmacKey;
	}

	/**
	 * Takes the Exchange body and calculates the digital fingerprint
	 * for that body, setting the calculatedFingerprint in the Exchange header
	 * 
	 * @param exchange
	 */
	public void calculateFingerprint(Exchange exchange) {
		try {
			byte[] body = exchange.getIn().getBody(byte[].class);
			// calculate the LAU on the raw (decoded) payload and set in camel
			// header
			exchange.getIn().setHeader("calculatedFingerprint", calculateLAU(body));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

	}

	/**
	 * Calculate the Digital Fingerprint
	 * 
	 * @param payload The raw payload (in bytes)
	 * @return the LAU signature
	 * @throws Exception
	 */
	private String calculateLAU(byte[] payload) throws Exception {
		Mac m = Mac.getInstance("HmacSHA256");

		// initialize key with shared secret from SAA
		SecretKeySpec keyspec = new SecretKeySpec(this.hmacKey.getBytes(Charset.forName("US-ASCII")), "HmacSHA256");
		m.init(keyspec);

		// calculate the LAU
		byte[] lau = m.doFinal(payload);
		byte[] lau_to_encode = new byte[16];
		System.arraycopy(lau, 0, lau_to_encode, 0, 16);

		return Base64.encodeBase64String(lau_to_encode);
	}

	/**
	 * Generate an Order Fulfillment Number
	 * 
	 * @return a random order number
	 */
	public BigInteger generateOrderNumber() {
		BigInteger bigInteger = new BigInteger("9349988899999");
		BigInteger bigInteger1 = bigInteger.subtract(new BigInteger("1"));
		return randomBigInteger(bigInteger1);
	}

	/**
	 * Generate a random Big Integer
	 * 
	 * @param n The seed
	 * @return A random BigInt
	 */
	private static BigInteger randomBigInteger(BigInteger n) {
		Random rnd = new Random();
		int maxNumBitLength = n.bitLength();
		BigInteger aRandomBigInt;
		do {
			aRandomBigInt = new BigInteger(maxNumBitLength, rnd);
			// compare random number less than given number
		} while (aRandomBigInt.compareTo(n) > 0);
		return aRandomBigInt;
	}

}
