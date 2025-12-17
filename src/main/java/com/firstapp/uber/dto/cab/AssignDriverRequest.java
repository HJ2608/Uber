package com.firstapp.uber.dto.cab;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignDriverRequest {
    private Integer cabId;
    private Integer driverId;
}
