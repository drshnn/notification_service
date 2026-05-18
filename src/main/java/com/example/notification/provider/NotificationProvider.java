package com.example.notification.provider;

import com.example.notification.event.NotificationEvent;

public interface NotificationProvider {
    
    /**
     * Sends the compiled notification payload via the 3rd party provider.
     * 
     * @param event The original event detailing recipient and metadata.
     * @param compiledBody The fully resolved text/html content to send.
     * @return providerMessageId returned by the provider on success.
     * @throws RuntimeException on delivery failure.
     */
    String send(NotificationEvent event, String compiledBody);
    
    String getProviderName();
}
