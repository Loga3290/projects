package com.covidpredict.covidpredict.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta {
    public String date;
    public String last_updated;

}
