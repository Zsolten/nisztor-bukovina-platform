package com.bukovina.platform.accommodation.booking;

import com.bukovina.platform.accommodation.booking.service.PublicBookingRateLimitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PublicBookingRateLimitProperties.class)
class BookingConfiguration {}
