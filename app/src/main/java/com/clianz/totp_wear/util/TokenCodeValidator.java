/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.clianz.totp_wear.util;

import com.clianz.totp_wear.gauth.PasscodeGenerator;

import java.security.GeneralSecurityException;

import static com.clianz.totp_wear.gauth.PasscodeGenerator.getSigningOracle;

/**
 * Validator for a OTP token codes.
 *
 * @author Ian Chan (ian.chan@clianz.com)
 */
public class TokenCodeValidator {

	/**
	 * A simple wrapper method for validating Time-based One Time Passcode.
	 *
	 * @param secretKey key generating the code.
	 * @param code token to validate.
	 * @param passcodeLength length of valid token code.
	 * @param pastInterval numbers of past interval to accept.
	 * @param futureInterval numbers of future intervals to accept.
	 * @return boolean if the token code was valid.
	 * @throws GeneralSecurityException error calculating token code.
	 */
	public boolean validateTOTP(String secretKey, String code, Integer passcodeLength, Integer pastInterval, Integer futureInterval) throws GeneralSecurityException {
		// This is not needed...
		// key = Base32String.encode(key.getBytes());
		PasscodeGenerator.Signer signer = getSigningOracle(secretKey);
		PasscodeGenerator passcodeGenerator = new PasscodeGenerator(signer, passcodeLength);

		long time = System.currentTimeMillis() / 1000 / PasscodeGenerator.INTERVAL;
		return passcodeGenerator.verifyTimeoutCode(code, time, futureInterval, pastInterval);
	}

	/**
	 * Simple wrapper to generate a TOTP from a key
	 *
	 * @param secretKey key generating the code.
	 * @param passcodeLength length of TOTP code to generate.
	 * @return TOTP code
	 * @throws GeneralSecurityException error calculating token code.
     */
	public String generateTOTP(String secretKey, Integer passcodeLength) throws GeneralSecurityException {
		PasscodeGenerator.Signer signer = getSigningOracle(secretKey);
		PasscodeGenerator passcodeGenerator = new PasscodeGenerator(signer, passcodeLength);

		long time = System.currentTimeMillis() / 1000 / PasscodeGenerator.INTERVAL;
		return passcodeGenerator.generateResponseCode(time);
	}
}
