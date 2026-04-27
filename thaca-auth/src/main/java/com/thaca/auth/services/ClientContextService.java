package com.thaca.auth.services;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.thaca.auth.dtos.DeviceInfoDTO;
import com.thaca.auth.dtos.GeoInfoDTO;
import com.thaca.auth.enums.DeviceType;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

@Service
public class ClientContextService {

    private final Parser parser = new Parser();
    private final DatabaseReader cityReader;
    private final DatabaseReader anonymousReader;

    public ClientContextService(
        @Qualifier("cityDatabaseReader") @Nullable DatabaseReader cityReader,
        @Qualifier("anonymousIpDatabaseReader") @Nullable DatabaseReader anonymousReader
    ) {
        this.cityReader = cityReader;
        this.anonymousReader = anonymousReader;
    }

    public DeviceInfoDTO parse(String userAgentString) {
        if (userAgentString == null) return DeviceInfoDTO.unknown();

        Client client = parser.parse(userAgentString);

        return DeviceInfoDTO.builder()
            .browser(client.userAgent.family + " " + client.userAgent.major)
            .os(client.os.family + " " + client.os.major)
            .device(client.device.family)
            .deviceType(resolveDeviceType(client.device.family))
            .build();
    }

    private DeviceType resolveDeviceType(String deviceFamily) {
        if (deviceFamily == null) return DeviceType.UNKNOWN;
        String lower = deviceFamily.toLowerCase();
        if (lower.contains("mobile") || lower.contains("phone")) return DeviceType.MOBILE;
        if (lower.contains("tablet") || lower.contains("ipad")) return DeviceType.TABLET;
        return DeviceType.DESKTOP;
    }

    public String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public GeoInfoDTO lookup(String ipAddress) {
        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            GeoInfoDTO.GeoInfoDTOBuilder builder = GeoInfoDTO.builder();

            // 1. Get Location Info
            if (cityReader != null) {
                try {
                    CityResponse city = cityReader.city(ip);
                    String countryName =
                        city.country() != null ? city.country().names().getOrDefault("vi", "Unknown") : "Unknown";
                    String countryIso = city.country() != null ? city.country().isoCode() : "XX";
                    String cityName =
                        city.city() != null ? city.city().names().getOrDefault("vi", "Unknown") : "Unknown";

                    builder
                        .country(countryName)
                        .countryIsoCode(countryIso)
                        .city(cityName)
                        .latitude(city.location() != null ? city.location().latitude() : null)
                        .longitude(city.location() != null ? city.location().longitude() : null)
                        .approxLocation(cityName + ", " + countryName);
                } catch (Exception ignored) {}
            }

            // 2. Get Anonymous/Risk Info
            boolean vpn = false,
                proxy = false,
                tor = false,
                hosting = false;
            if (anonymousReader != null) {
                try {
                    var anon = anonymousReader.anonymousIp(ip);
                    vpn = anon.isAnonymousVpn();
                    proxy = anon.isPublicProxy();
                    tor = anon.isTorExitNode();
                    hosting = anon.isHostingProvider();
                } catch (Exception ignored) {}
            }

            builder.isVpn(vpn).isProxy(proxy).isTor(tor).isHosting(hosting);

            // 3. Risk Score logic
            int score = 0;
            if (tor) score = 100;
            else if (proxy) score = 80;
            else if (vpn) score = 50;
            else if (hosting) score = 30;

            builder.riskScore(score);

            GeoInfoDTO result = builder.build();
            return result.getCountry() != null ? result : GeoInfoDTO.unknown();
        } catch (Exception e) {
            return GeoInfoDTO.unknown();
        }
    }
}
