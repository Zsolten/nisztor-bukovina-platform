package com.bukovina.platform.support.notification;

public record EncryptedManagementToken(String ciphertext, String initializationVector) {}
