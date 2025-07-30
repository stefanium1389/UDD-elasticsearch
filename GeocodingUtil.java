import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class GeocodingUtil {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public static GeoPoint geocode(String location) {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
            .queryParam("q", location)
            .queryParam("format", "json")
            .queryParam("limit", 1);

        var response = restTemplate.getForObject(builder.toUriString(), NominatimResponse[].class);

        if (response == null || response.length == 0) {
            throw new RuntimeException("No geocoding result for location: " + location);
        }

        NominatimResponse result = response[0];
        return new GeoPoint(Double.parseDouble(result.lat), Double.parseDouble(result.lon));
    }

    private static class NominatimResponse {
        public String lat;
        public String lon;
    }
}
