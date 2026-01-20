package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private Integer id;

    private String username;
    private String email;
    private String password;

    private String name;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String url;
    private String description;
    private String link;
    private String locale;
    private String nickname;
    private String slug;
    private List<String> roles;

    @JsonProperty("registered_date")
    private String registeredDate;

    private Map<String, Object> meta;
    private Map<String, Boolean> capabilities;

    @JsonProperty("extra_capabilities")
    private Map<String, Boolean> extraCapabilities;

    @JsonProperty("avatar_urls")
    private Map<String, String> avatarUrls;

    @JsonProperty("_links")
    private Map<String, Object> links;
}
