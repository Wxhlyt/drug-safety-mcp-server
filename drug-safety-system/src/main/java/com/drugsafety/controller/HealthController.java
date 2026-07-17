package com.drugsafety.controller;

import com.drugsafety.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<Void> health() {
        return Result.success("Drug Safety System Running");
    }
}
