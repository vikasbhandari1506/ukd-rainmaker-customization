package org.egov.ukdcustomservice.web.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pod {

    private String name;
    private String image;
    private String age;
    private String status;

}
