/*
 * Copyright 2024 Your Company Name
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flexydemy.content.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static java.lang.String.format;

/**
 * This enum represents the functional error codes.
 */
@Getter
public enum FunctionalErrorCode {

    BAD_REQUEST(0, HttpStatus.BAD_REQUEST, "Bad Request"),
    NOT_FOUND_ENTITY_ID(1, HttpStatus.NOT_FOUND, "No record of type %s and with id %s is present in the database"),
    NOT_FOUND(1, HttpStatus.NOT_FOUND, "Not found"),
    USER_NOT_FOUND(2, HttpStatus.FORBIDDEN, "User not found for : %s"),
    NOT_NULL_ENTITY(3, HttpStatus.BAD_REQUEST, "Object %s is required."),
    NOT_NULL_FIELD(4, HttpStatus.FORBIDDEN, "The following field is required: %s"),
    JUST_MESSAGE(5, HttpStatus.BAD_REQUEST, "%s"),
    NOT_NULL_FIELDS(6, HttpStatus.BAD_REQUEST, "The following field is required : %s"),
    MUST_BE_NULL_FIELD(7, HttpStatus.BAD_REQUEST, "The following fields must be null : %s"),
    INCORRECT_LENGTH_FIELD(8, HttpStatus.BAD_REQUEST, "The following field is not the right size : %s"),
    MIN_FIELD(9, HttpStatus.BAD_REQUEST, "The following field does not respect the minimum value : %s"),
    MAX_FIELD(10, HttpStatus.BAD_REQUEST, "The following field does not respect the maximum value : %s"),
    PATTERN_FIELD(11, HttpStatus.BAD_REQUEST, "The following field does not respect the correct format : %s"),
    NOT_BLANK_OR_EMPTY_FIELD(12, HttpStatus.BAD_REQUEST, "The following field is empty : %s"),
    INVALID_JWT_TOKEN(13, HttpStatus.UNAUTHORIZED, "Token JWT expired or invalid"),
    CREDENTIAL_ALREADY_USED(14, HttpStatus.BAD_REQUEST, "%s Already exist"),
    PROPERTY_ALREADY_USED(15, HttpStatus.BAD_REQUEST, "%s Already used"),
    WRONG_PASSWORD(16, HttpStatus.FORBIDDEN, "Password or Username Incorrect"),
    RECORD_ALREADY_EXISTS(17, HttpStatus.BAD_REQUEST, "Coupon code is already exist : %s ."),
    AUTHORIZATION_FAILED(18, HttpStatus.FORBIDDEN, "Authorization failed for access token request."),
    USER_EXIST_EXCEPTION(21, HttpStatus.BAD_REQUEST,"Email already exist."),
    FILE_EXTENSION_NOT_ALLOWED(22, HttpStatus.BAD_REQUEST, "File extension not allowed : %s"),
    INVALID_MFA_CODE(27, HttpStatus.UNAUTHORIZED, "%s"),
    SENDING_MAIL_ERROR(28, HttpStatus.BAD_REQUEST, "Error while sending mail : %s"),
    USER_EMAIL_NOT_FOUND(29, HttpStatus.NOT_FOUND, "Not such a user with email %s exist in database"),
    USER_EMAIL_NOT_VERIFIED(30, HttpStatus.NOT_FOUND, "Email not verified for: %s."),
    UNAUTHORIZED_USER(42,HttpStatus.BAD_REQUEST,"Unauthorized User."),
    ACCOUNT_DISABLED(43,HttpStatus.FORBIDDEN,"Account Disabled for : %s ."),
    INVALID_REFERRAL_CODE(44, HttpStatus.FORBIDDEN, "Invalid Referral Code: '%s'."),
    PASSWORD_DOES_NOT_MATCH(45, HttpStatus.FORBIDDEN, "Current password is incorrect.")
    ;


    private final String code;
    private final HttpStatus httpStatus;
    private final String messageTemplate;

    FunctionalErrorCode(int code, HttpStatus httpStatus, String messageTemplate) {
        this.code = format("%03d", code);
        this.httpStatus = httpStatus;
        this.messageTemplate = messageTemplate;
    }
}
